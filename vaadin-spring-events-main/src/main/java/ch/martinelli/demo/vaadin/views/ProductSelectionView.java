package ch.martinelli.demo.vaadin.views;

import ch.martinelli.demo.vaadin.data.entity.ProductInfo;
import ch.martinelli.demo.vaadin.data.service.ProductInfoService;
import com.flowingcode.vaadin.addons.gridhelpers.GridHelper;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.value.ValueChangeMode;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.VaadinService;
import com.vaadin.flow.spring.data.VaadinSpringDataHelpers;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.data.domain.PageRequest;

@PageTitle("product info")
@Route(value = "product_info", layout = MainLayout.class)
//@Uses(Icon.class)
public class ProductSelectionView extends VerticalLayout implements ApplicationListener<ProductInfoAddedEvent> {
    private final ProductInfoService productInfoService;
    private Grid<ProductInfo> grid = new Grid<>();

    @Autowired
    public ProductSelectionView(ProductInfoService productInfoService) {
        this.productInfoService = productInfoService;

        H2 infoText = new H2("Select product to continue process or create new one to start new production cycle");
        infoText.setClassName("info-text");
        add(infoText);
        setHorizontalComponentAlignment(Alignment.CENTER, infoText);

        setSizeFull();

        grid.setItems(query ->
                productInfoService.list(PageRequest.of(query.getPage(), query.getPageSize(), VaadinSpringDataHelpers.toSpringDataSort(query))).stream());

        grid.addColumn(ProductInfo::getProductIdentityInfo).setHeader("name");
        grid.addColumn(ProductInfo::getDescription).setHeader("description");

        GridHelper.setSelectOnClick(grid, true);
        add(grid);

        Button btnAdd = new Button("Add", new Icon(VaadinIcon.PLUS));
        btnAdd.addThemeVariants(ButtonVariant.LUMO_SUCCESS, ButtonVariant.LUMO_SMALL);
        btnAdd.addClickListener(buttonClickEvent -> {
            Dialog dialog = createAddDialogue();
            dialog.open();
        });

        Button btnSelect = new Button("Select", new Icon(VaadinIcon.PLAY));
        btnSelect.addThemeVariants(ButtonVariant.LUMO_PRIMARY, ButtonVariant.LUMO_SMALL);
        btnSelect.addClickListener(buttonClickEvent -> {
            if (grid.getSelectionModel().getFirstSelectedItem().isEmpty()) {
                Notification.show("Select the product to continue!")
                        .addThemeVariants(NotificationVariant.LUMO_ERROR);
                return;
            }
            Dialog dialog = createSelectDialog();
            dialog.open();
        });

        add(new HorizontalLayout(btnAdd, btnSelect));
    }


    /**
     * go to todos with selected product
     */
    private Dialog createSelectDialog() {
        Dialog dialog = new Dialog();

        if (grid.getSelectionModel().getFirstSelectedItem().isEmpty()) {
            Notification.show("Can't proceed without a selected product! Select a product first")
                    .addThemeVariants(NotificationVariant.LUMO_ERROR);
            dialog.close();
        }
        ProductInfo info = grid.getSelectionModel().getFirstSelectedItem().get();


        dialog.setHeaderTitle("You are going to open the ToDo list with the next product:");


        VerticalLayout dialogLayout = new VerticalLayout();
        TextField productId = new TextField("Name");
        productId.setEnabled(false);
        productId.setValue(info.getProductIdentityInfo());
        productId.setWidthFull();

        TextField productDescription = new TextField("Description");
        productDescription.setEnabled(false);
        productDescription.setValue(info.getDescription());
        productDescription.setWidthFull();

        TextField uuid = new TextField("Id");
        uuid.setEnabled(false);
        uuid.setValue(info.getId().toString());
        uuid.setWidthFull();

        Button btnOk = new Button("Continue");


        dialogLayout.add(productId, productDescription, uuid);
        dialog.add(dialogLayout);


        btnOk.addClickListener(buttonClickEvent -> {
            //todo goto to next page with selected item
            String parameterValue = productInfoService.findProductByProductIdentityInfo(info.getProductIdentityInfo()).getId().toString();
            String param = info.getId().toString();
            dialog.close();

            VaadinService.getCurrent().getContext().setAttribute(ProductInfo.class, productInfoService.findById(info.getId()).get());
            btnOk.getUI().ifPresent(ui ->
                    ui.navigate(OperationsView.class, param));

//            btnOk.getUI().ifPresent(ui ->
//                    ui.navigate(ToDoGridView.class,
//                            new RouteParameters("operationId",parameterValue)));
        });

        Button btnCancel = new Button("Cancel");
        btnCancel.addClickListener(buttonClickEvent -> {
            dialog.close();
        });

        dialog.getFooter().add(btnOk);
        dialog.getFooter().add(btnCancel);

        return dialog;
    }

    private Dialog createAddDialogue() {
        Dialog dialog = new Dialog();
        dialog.setHeaderTitle("Add new unique name for a product");

        VerticalLayout dialogLayout = new VerticalLayout();
        TextField productId = new TextField("Enter unique name");
        TextField productDescription = new TextField("Enter an optional description");

        Button btnSave = new Button("save");

        productId.setValueChangeMode(ValueChangeMode.ON_CHANGE);
        productId.addValueChangeListener(e -> {
            if (productInfoService.findProductByProductIdentityInfo(e.getValue()) != null) {
                productId.setInvalid(true);
                productId.setErrorMessage("name already exists and can't be used");
                btnSave.setEnabled(false);

            } else {
                productId.setInvalid(false);
                productId.setErrorMessage("");
                btnSave.setEnabled(true);
            }
        });

        dialogLayout.add(productId, productDescription);
        dialog.add(dialogLayout);


        btnSave.addClickListener(buttonClickEvent -> {
            ProductInfo info = ProductInfo.builder()
                    .productIdentityInfo(productId.getValue())
                    .description(productDescription.getValue())
                    .build();
            productInfoService.createProductInfo(info);
            grid.getDataProvider().refreshAll();
            dialog.close();

//            Broadcaster.broadcast("Item added by " + author);
        });

        Button btnCancel = new Button("Cancel");
        btnCancel.addClickListener(buttonClickEvent -> {
            dialog.close();
        });

        dialog.getFooter().add(btnSave);
        dialog.getFooter().add(btnCancel);

        return dialog;
    }

    @Override
    public void onApplicationEvent(ProductInfoAddedEvent event) {
        UI.getCurrent().access(() ->
                grid.getDataProvider().refreshAll());
        Notification.show("added item " + event.toString());
    }
}

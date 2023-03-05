package ch.martinelli.demo.vaadin.views;

import ch.martinelli.demo.vaadin.data.entity.Operation;
import ch.martinelli.demo.vaadin.data.entity.ProductInfo;
import ch.martinelli.demo.vaadin.data.service.OperationService;
import ch.martinelli.demo.vaadin.data.service.SamplePersonService;
import ch.martinelli.demo.vaadin.data.service.ToDoItemsService;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.dependency.CssImport;
import com.vaadin.flow.component.dependency.Uses;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridSortOrder;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.provider.SortDirection;
import com.vaadin.flow.router.*;
import com.vaadin.flow.server.VaadinService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;

import java.util.List;
import java.util.UUID;

@PageTitle("Available Operations")
//@Route(value = "todos/:operationId", layout = MainLayout.class)
@Route(value = "operations", layout = MainLayout.class)
@Uses(Icon.class)
@CssImport(value = "./styles/dynamic-grid-row-background-color.css",
        themeFor = "vaadin-grid")
public class OperationsView extends VerticalLayout implements HasUrlParameter<String> {
    private String greenColor = "#00853D";
    private String redColor = "#e61414";
    private UUID productId;
    private ProductInfo productInfo;
    private UUID operationId;
    private Grid<Operation> grid = new Grid<>(Operation.class, false);

    private final OperationService operationService;
    private final ToDoItemsService toDoItemsService;

    @Override
    public void setParameter(BeforeEvent event, @OptionalParameter String id) {
        if (id != null && !id.isEmpty()) {
            productId = UUID.fromString(id);
        }

        productInfo = VaadinService.getCurrent().getContext().getAttribute(ProductInfo.class);
        Notification.show(String.format("Loaded: {0}", productInfo.toString()));
    }

    @Autowired
    public OperationsView(OperationService operationService, ConfigurableApplicationContext applicationContext, SamplePersonService personService, ToDoItemsService toDoItemsService) {
//        grid.setItems(testItems);
        this.operationService = operationService;
        this.toDoItemsService = toDoItemsService;


        setHeightFull();

        Grid.Column<Operation> operationNumberCol = grid.addColumn(Operation::getOperationNumber)
                .setHeader("#")
                .setWidth("75px")
                .setFlexGrow(0);

        Grid.Column<Operation> nameCol = grid.addColumn(Operation::getOperationName_ru)
                .setHeader("Name")
                .setFlexGrow(1);

        Grid.Column<Operation> descriptionCol = grid.addColumn(Operation::getOperationDescription)
                .setHeader("Description")
                .setFlexGrow(1);

        Grid.Column<Operation> actionBtCol = grid.addComponentColumn(item -> {
                    Button selectBt = new Button("Select");
                    selectBt.addClassName("select-button");
                    selectBt.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
                    selectBt.addClickListener(event -> {
                        Dialog dialog = createSelectDialog(item);
                        dialog.open();
                    });

                    return selectBt;
                })
                .setHeader("")
                .setWidth("120px")
                .setFlexGrow(0);



        //todo add sorting by number
//        grid.setItems(operationService.getAll());
        grid.setItems(query ->
                operationService.list(PageRequest.of(query.getPage(), query.getPageSize(), Sort.by("operationNumber").ascending())).stream());
        grid.sort(List.of(new GridSortOrder<Operation>(operationNumberCol, SortDirection.ASCENDING)));

        add(grid);
    }

    private Dialog createSelectDialog(Operation operation) {
        Dialog dialog = new Dialog();

        dialog.setHeaderTitle("You are going to open next operation:");


        VerticalLayout dialogLayout = new VerticalLayout();
        TextField operationNumber = new TextField("Operation #");
        operationNumber.setEnabled(false);
        operationNumber.setValue(String.valueOf(operation.getOperationNumber()));
        operationNumber.setWidthFull();

        TextField nameRu = new TextField("Name in russian");
        nameRu.setEnabled(false);
        nameRu.setValue(operation.getOperationName_ru());
        nameRu.setWidthFull();

        TextField nameEn = new TextField("Name in English");
        nameEn.setEnabled(false);
        nameEn.setValue(operation.getOperationName_en());
        nameEn.setWidthFull();

        TextField description = new TextField("Description");
        description.setEnabled(false);
        description.setValue(operation.getOperationDescription());
        description.setWidthFull();


        TextField uuid = new TextField("Id");
        uuid.setEnabled(false);
        uuid.setValue(operation.getId().toString());
        uuid.setWidthFull();

        Button createItemsAndGotoNextPage = new Button("Continue");


        dialogLayout.add(operationNumber, nameRu, nameEn, description, uuid);
        dialog.add(dialogLayout);


        createItemsAndGotoNextPage.addClickListener(buttonClickEvent -> {
            //todo goto to next page with selected item

            dialog.close();

            Operation selectedOperation = operationService.findById(operation.getId()).get();

            VaadinService.getCurrent().getContext().setAttribute(Operation.class, selectedOperation);

            toDoItemsService.build(productInfo.getProductIdentityInfo(),operation.getOperationNumber());

            String operationIdAsStringParam = selectedOperation.getId().toString();
            createItemsAndGotoNextPage.getUI().ifPresent(ui ->
                    ui.navigate(ToDoGridView.class, operationIdAsStringParam));
        });

        Button btnCancel = new Button("Cancel");
        btnCancel.addClickListener(buttonClickEvent -> {
            dialog.close();
        });

        dialog.getFooter().add(createItemsAndGotoNextPage);
        dialog.getFooter().add(btnCancel);

        return dialog;
    }
}

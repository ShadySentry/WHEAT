package com.deb.wheat.vaadin.views;

import com.deb.wheat.vaadin.data.entity.ProductInfo;
import com.deb.wheat.vaadin.data.service.ExcelGeneratorService;
import com.deb.wheat.vaadin.data.service.ProductInfoService;
import com.flowingcode.vaadin.addons.gridhelpers.GridHelper;
import com.vaadin.componentfactory.ToggleButton;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.Anchor;
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
import com.vaadin.flow.router.RouteAlias;
import com.vaadin.flow.server.StreamResource;
import com.vaadin.flow.server.VaadinService;
import com.vaadin.flow.spring.data.VaadinSpringDataHelpers;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.ApplicationListener;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.data.domain.PageRequest;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Locale;
import java.util.UUID;

@PageTitle("product info")
@Route(value = "", layout = MainLayout.class)
@RouteAlias(value = "", layout = MainLayout.class)
//@Route(value = "", layout = MainLayout.class)
//@Uses(Icon.class)
@Slf4j
public class ProductSelectionView extends VerticalLayout implements ApplicationListener<ProductInfoAddedEvent> {
    private final ProductInfoService productInfoService;
    private final ExcelGeneratorService excelGeneratorService;
    private ConfigurableApplicationContext applicationContext;
    private ApplicationEventPublisher applicationEventPublisher;


    private Grid<ProductInfo> grid = new Grid<>();
    private H2 infoText;
    private Button btnExportRu;
    private Button btnAdd;
    private Button btnSelect;
    private Button btnExportEn;
    private Grid.Column<ProductInfo> nameColumn;
    Grid.Column<ProductInfo> descriptionColumn;

    @Autowired
    public ProductSelectionView(ProductInfoService productInfoService, ExcelGeneratorService excelGeneratorService,
                                ConfigurableApplicationContext applicationContext,
                                ApplicationEventPublisher applicationEventPublisher) {
        this.applicationEventPublisher = applicationEventPublisher;
        this.productInfoService = productInfoService;
        this.excelGeneratorService = excelGeneratorService;
        this.applicationContext = applicationContext;

        MainLayout.selectedLocale = VaadinService.getCurrent().getContext().getAttribute(Locale.class);


//        H2 selectLanguageText = new H2("Select language");
//        selectLanguageText.setClassName("lang-text");

        ToggleButton toggleLanguage = new ToggleButton(MainLayout.selectedLocale.getLanguage());
        toggleLanguage.setValue(MainLayout.selectedLocale != MainLayout.localeForRu);

        toggleLanguage.getStyle().set("padding-left", "16px");

        toggleLanguage.addClickListener(event -> {
            Locale prevLocale = VaadinService.getCurrent().getContext().getAttribute(Locale.class);

            if (MainLayout.selectedLocale == null) {
                MainLayout.selectedLocale = MainLayout.localeForEn;
            } else {
                if (MainLayout.selectedLocale.getLanguage().equalsIgnoreCase(MainLayout.localeForEn.getLanguage())) {
                    MainLayout.selectedLocale = MainLayout.localeForRu;
                } else {
                    MainLayout.selectedLocale = MainLayout.localeForEn;
                }
            }
            if (prevLocale != MainLayout.selectedLocale) {
                applicationEventPublisher.publishEvent(new LocalizationChangeEvent(this));
                updateLocale();
            }
            toggleLanguage.setLabel(MainLayout.selectedLocale.getLanguage());
            VaadinService.getCurrent().getContext().setAttribute(Locale.class, MainLayout.selectedLocale);

        });
//
//        add(selectLanguageText, toggleLanguage);

        infoText = new H2(MainLayout.selectedLocale == MainLayout.localeForEn ?
                "Select product to continue process or create new one to start new production cycle" :
                "Для продолжения выберите издедлие или создатей новое для начала нового производственного цикла");
        infoText.setClassName("info-text");
        add(infoText);
        setHorizontalComponentAlignment(Alignment.CENTER, infoText);

        setSizeFull();

        grid.setItems(query ->
                productInfoService.list(PageRequest.of(query.getPage(), query.getPageSize(), VaadinSpringDataHelpers.toSpringDataSort(query))).stream());

        nameColumn = grid.addColumn(ProductInfo::getProductIdentityInfo).setHeader(MainLayout.selectedLocale == MainLayout.localeForEn ?
                "name" : "название");

        descriptionColumn = grid.addColumn(ProductInfo::getDescription).setHeader(MainLayout.selectedLocale == MainLayout.localeForEn ?
                "description" : "описание");

        GridHelper.setSelectOnClick(grid, true);

        btnAdd = new Button(MainLayout.selectedLocale == MainLayout.localeForEn ? "Add" : "Добавить", new Icon(VaadinIcon.PLUS));
        btnAdd.addThemeVariants(ButtonVariant.LUMO_SUCCESS, ButtonVariant.LUMO_SMALL);
        btnAdd.addClickListener(buttonClickEvent -> {
            Dialog dialog = createAddDialogue();
            dialog.open();
        });

        btnSelect = new Button(MainLayout.selectedLocale == MainLayout.localeForEn ? "Select" : "Выбрать", new Icon(VaadinIcon.PLAY));
        btnSelect.addThemeVariants(ButtonVariant.LUMO_PRIMARY, ButtonVariant.LUMO_SMALL);
        btnSelect.addClickListener(buttonClickEvent -> {
            if (grid.getSelectionModel().getFirstSelectedItem().isEmpty()) {
                Notification.show(MainLayout.selectedLocale == MainLayout.localeForEn ? "Select the product to continue!" :
                                "Для продолжение выберите изделие!")
                        .addThemeVariants(NotificationVariant.LUMO_ERROR);
                return;
            }
            Dialog dialog = createSelectDialog();
            dialog.open();
        });


        Anchor downloadExcelRu = new Anchor(new StreamResource("excel_ru.xlsx", () -> {
            ByteArrayOutputStream os = new ByteArrayOutputStream();
            try {
                excelGeneratorService.crateExcelFile(grid.getSelectionModel().getFirstSelectedItem().isEmpty() ? UUID.randomUUID() :
                                        grid.getSelectionModel().getFirstSelectedItem().get().getId(),
                                new Locale("ru", "RU"))
                        .write(os);

                return new ByteArrayInputStream(os.toByteArray());
            } catch (IOException e) {
                log.error(e.getMessage() + " " + e.toString());
                return InputStream.nullInputStream();
            }
        }), null);
        downloadExcelRu.setEnabled(false);


        btnExportRu = new Button(MainLayout.selectedLocale == MainLayout.localeForEn ?
                "Export to Excel RU" : "Экспортировать в Excel RU", new Icon(VaadinIcon.FILE_TEXT));
        btnExportRu.addThemeVariants(ButtonVariant.LUMO_SUCCESS, ButtonVariant.LUMO_SMALL);

        downloadExcelRu.add(btnExportRu);

        Anchor downloadExcelEn = new Anchor(new StreamResource("excel.xlsx", () -> {
            ByteArrayOutputStream os = new ByteArrayOutputStream();
            try {
                excelGeneratorService.crateExcelFile(grid.getSelectionModel().getFirstSelectedItem().isEmpty() ? UUID.randomUUID() :
                                        grid.getSelectionModel().getFirstSelectedItem().get().getId(),
                                Locale.ENGLISH)
                        .write(os);

                return new ByteArrayInputStream(os.toByteArray());
            } catch (IOException e) {
                log.error(e.getMessage() + " " + e.toString());
                return InputStream.nullInputStream();
            }
        }), null);
        downloadExcelEn.setEnabled(false);


        btnExportEn = new Button(MainLayout.selectedLocale == MainLayout.localeForEn ?
                "Export to Excel EN" : "Экспортировать в Excel EN", new Icon(VaadinIcon.FILE_TEXT));
        btnExportEn.addThemeVariants(ButtonVariant.LUMO_SUCCESS, ButtonVariant.LUMO_SMALL);

        downloadExcelEn.add(btnExportEn);

        grid.addSelectionListener(event -> {
            downloadExcelEn.setEnabled(grid.getSelectionModel().getFirstSelectedItem().isPresent());
            downloadExcelRu.setEnabled(grid.getSelectionModel().getFirstSelectedItem().isPresent());
        });
        add(grid);

        add(new HorizontalLayout(toggleLanguage, downloadExcelEn, downloadExcelRu, btnAdd, btnSelect));
    }

    private void updateLocale() {
        UI.getCurrent().access(() -> {
            infoText.setText(MainLayout.selectedLocale == MainLayout.localeForEn ?
                    "Select product to continue process or create new one to start new production cycle" :
                    "Для продолжения выберите издедлие или создатей новое для начала нового производственного цикла");
            btnExportRu.setText(MainLayout.selectedLocale == MainLayout.localeForEn ?
                    "Export to Excel RU" : "Экспортировать в Excel RU");
            btnAdd.setText(MainLayout.selectedLocale == MainLayout.localeForEn ? "Add" : "Добавить");
            btnSelect.setText(MainLayout.selectedLocale == MainLayout.localeForEn ? "Select" : "Выбрать");
            btnExportEn.setText(MainLayout.selectedLocale == MainLayout.localeForEn ?
                    "Export to Excel EN" : "Экспортировать в Excel EN");

            nameColumn.setHeader(MainLayout.selectedLocale == MainLayout.localeForEn ?
                    "name" : "название");
            descriptionColumn.setHeader(MainLayout.selectedLocale == MainLayout.localeForEn ?
                    "description" : "описание");
        });
    }


    /**
     * go to todos with selected product
     */
    private Dialog createSelectDialog() {
        Dialog dialog = new Dialog();

        if (grid.getSelectionModel().getFirstSelectedItem().isEmpty()) {
            Notification.show(MainLayout.selectedLocale == MainLayout.localeForEn ?
                            "Can't proceed without a selected product! Select a product first" :
                            "невозможно продолжить без выбранного изделия! Сначала выберите изделие")
                    .addThemeVariants(NotificationVariant.LUMO_ERROR);
            dialog.close();
        }
        ProductInfo info = grid.getSelectionModel().getFirstSelectedItem().get();


        dialog.setHeaderTitle(MainLayout.selectedLocale == MainLayout.localeForEn ?
                "You are going to open the ToDo list with the next product:" :
                "Вы собираетесь открыть список операций для изделия:");


        VerticalLayout dialogLayout = new VerticalLayout();
        TextField productId = new TextField(MainLayout.selectedLocale == MainLayout.localeForEn ? "Name" : "Название");
        productId.setEnabled(false);
        productId.setValue(info.getProductIdentityInfo());
        productId.setWidthFull();

        TextField productDescription = new TextField(MainLayout.selectedLocale == MainLayout.localeForEn ? "Description" : "Описание");
        productDescription.setEnabled(false);
        productDescription.setValue(info.getDescription());
        productDescription.setWidthFull();

        TextField uuid = new TextField("Id");
        uuid.setEnabled(false);
        uuid.setValue(info.getId().toString());
        uuid.setWidthFull();

        Button btnOk = new Button(MainLayout.selectedLocale == MainLayout.localeForEn ? "Continue" : "Продолжить");


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

        Button btnCancel = new Button(MainLayout.selectedLocale == MainLayout.localeForEn ? "Cancel" : "Отменить");
        btnCancel.addClickListener(buttonClickEvent -> {
            dialog.close();
        });

        dialog.getFooter().add(btnOk);
        dialog.getFooter().add(btnCancel);

        return dialog;
    }

    private Dialog createAddDialogue() {
        Dialog dialog = new Dialog();
        dialog.setHeaderTitle(MainLayout.selectedLocale == MainLayout.localeForEn ?"Adding a new product":
                "Добавление нового продукта");

        VerticalLayout dialogLayout = new VerticalLayout();
        TextField productId = new TextField(MainLayout.selectedLocale == MainLayout.localeForEn ?"Enter unique name"
                :"Введите уникальное имя");
        TextField productDescription = new TextField(MainLayout.selectedLocale == MainLayout.localeForEn ?"Enter an optional description":
                "Введите необязательное описание");

        Button btnSave = new Button(MainLayout.selectedLocale == MainLayout.localeForEn ?"Save":"Сохранить");

        productId.setValueChangeMode(ValueChangeMode.ON_CHANGE);
        productId.addValueChangeListener(e -> {
            if (productInfoService.findProductByProductIdentityInfo(e.getValue()) != null) {
                productId.setInvalid(true);
                productId.setErrorMessage(MainLayout.selectedLocale == MainLayout.localeForEn ?"Name already exists and can't be used":
                        "Имя уже сущевутет и неможет быть использовано");
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

        Button btnCancel = new Button(MainLayout.selectedLocale == MainLayout.localeForEn ?"Cancel":"Отмена");
        btnCancel.addClickListener(buttonClickEvent -> {
            dialog.close();
        });

        dialog.getFooter().add(btnSave);
        dialog.getFooter().add(btnCancel);

        return dialog;
    }


    private Dialog createExportDialogue() {
        Dialog dialog = new Dialog();

        if (grid.getSelectionModel().getFirstSelectedItem().isEmpty()) {
            Notification.show("Can't proceed without a selected product! Select a product first")
                    .addThemeVariants(NotificationVariant.LUMO_ERROR);
            dialog.close();
        }
        ProductInfo info = grid.getSelectionModel().getFirstSelectedItem().get();


        dialog.setHeaderTitle("You are going to export the operation to external csv");


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

        dialogLayout.add(productId, productDescription, uuid);
        dialog.add(dialogLayout);
//excel export

        Anchor downloadExcel = new Anchor(new StreamResource("excel.xlsx", () -> {
            ByteArrayOutputStream os = new ByteArrayOutputStream();
            try {
                excelGeneratorService.crateExcelFile(info.getId(), (new Locale("ru", "RU")))
                        .write(os);

                return new ByteArrayInputStream(os.toByteArray());
            } catch (IOException e) {
                log.error(e.getMessage() + " " + e.toString());
                return null;
            } finally {
                dialog.close();
            }

        }), null);

        Button btnExport = new Button("Export selected to Excel");
        btnExport.addThemeVariants(ButtonVariant.LUMO_PRIMARY);

        downloadExcel.add(btnExport);

        Button btnCancel = new Button("Cancel");
        btnCancel.addClickListener(buttonClickEvent -> {
            dialog.close();
        });

        dialog.getFooter().add(downloadExcel);
        dialog.getFooter().add(btnCancel);

        return dialog;
    }


    @Override
    public void onApplicationEvent(ProductInfoAddedEvent event) {
        UI.getCurrent().access(() ->
                grid.getDataProvider().refreshAll());
        Notification.show(MainLayout.selectedLocale == MainLayout.localeForEn ? "added item " : "добавлена запись" + event.toString());
    }
}

package com.deb.wheat.vaadin.views;

import com.deb.wheat.vaadin.data.dto.OperatorToDoItemDTO;
import com.deb.wheat.vaadin.data.entity.ActionType;
import com.deb.wheat.vaadin.data.entity.Operation;
import com.deb.wheat.vaadin.data.entity.ProductInfo;
import com.deb.wheat.vaadin.data.service.OperationService;
import com.deb.wheat.vaadin.data.service.ProductInfoService;
import com.deb.wheat.vaadin.data.service.SamplePersonService;
import com.deb.wheat.vaadin.data.service.ToDoItemsService;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.dependency.CssImport;
import com.vaadin.flow.component.dependency.Uses;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridVariant;
import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.*;
import com.vaadin.flow.server.VaadinService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.data.domain.PageRequest;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;

import static com.deb.wheat.vaadin.views.MainLayout.localeForEn;
import static com.deb.wheat.vaadin.views.MainLayout.selectedLocale;

@PageTitle("Todo list")
//@Route(value = "todos/:operationId", layout = MainLayout.class)
@Route(value = "todos", layout = MainLayout.class)
@Uses(Icon.class)
@CssImport(value = "./styles/dynamic-grid-row-background-color.css",
        themeFor = "vaadin-grid")
public class ToDoGridView extends VerticalLayout implements ApplicationListener<SamplePersonAddedEvent>, HasUrlParameter<String> {
    private String greenColor = "#00853D";
    private String redColor = "#e61414";
    private UUID operationId;
    private UUID productId;
    private Grid<OperatorToDoItemDTO> grid = new Grid<>(OperatorToDoItemDTO.class, false);
    private final OperationService operationService;

    @Override
    public void setParameter(BeforeEvent event, @OptionalParameter String id) {
        if (id != null && !id.isEmpty()) {
            operationId = UUID.fromString(id);
        }
        ProductInfo productInfo = VaadinService.getCurrent().getContext().getAttribute(ProductInfo.class);
        if (productInfo == null) {
            return;
        }

        productId = productInfo.getId();

//        pageTitle= operationService.findById(operationId).get().getOperationName_en();

        Notification.show(selectedLocale == localeForEn ? "Loaded: " : "Загружено"
                + productInfo.getProductIdentityInfo() + "   " + productInfo.getDescription());


    }

    @Autowired
    public ToDoGridView(ToDoItemsService toDoItemsService, ConfigurableApplicationContext applicationContext, OperationService operationService, ProductInfoService productInfoService) {

        this.operationService = operationService;
        setHeightFull();

        ProductInfo loadedInfo = VaadinService.getCurrent().getContext().getAttribute(ProductInfo.class);
        Operation loadedOperation = VaadinService.getCurrent().getContext().getAttribute(Operation.class);
        if (loadedInfo == null || loadedOperation == null) {
            Notification notification = new Notification(loadedInfo == null ?
                    "Select a product first!" : "Сначала необходимо выбрать изделие!");
            notification.addThemeVariants(NotificationVariant.LUMO_ERROR);
            notification.setDuration(5000);
            notification.open();
            return;
        }

        createOperationInfo(productInfoService);

        grid.setClassNameGenerator(item -> {
            if (item.getAction() == ActionType.CONFIRM) {
                return "confirmed";
            }
            if (item.getAction() == ActionType.REJECT) {
                return "rejected";
            }
            return "unprocessed";
        });


        Grid.Column<OperatorToDoItemDTO> stepCol = grid.addColumn(item -> item.getOperation() + "." + item.getActionNumber())
                .setHeader("#")
                .setWidth("75px")
                .setFlexGrow(0);

        Grid.Column<OperatorToDoItemDTO> descriptionCol = grid.addColumn(selectedLocale == localeForEn ?
                        (OperatorToDoItemDTO::getActionDescriptionEn):(OperatorToDoItemDTO::getActionDescriptionRu))
                .setHeader(selectedLocale == localeForEn ?"Description":"Описание")
                .setFlexGrow(2);

//        descriptionCol.setAutoWidth(true);


        Grid.Column<OperatorToDoItemDTO> actionBtCol = grid.addComponentColumn(item -> {
                    Span buttonsWithTimeStamp = new Span();
                    Button confirmBt = new Button(selectedLocale == localeForEn ?
                            "Confirm":"Подтвердить");
                    confirmBt.addClassName("accept-button");
                    confirmBt.addThemeVariants(ButtonVariant.LUMO_PRIMARY, ButtonVariant.LUMO_SMALL);
                    confirmBt.getStyle().set("background-color", greenColor);

                    Button rejectBt = new Button(selectedLocale == localeForEn ?
                            "Reject":"Отклонить");
                    rejectBt.addClassName("accept-button");
                    rejectBt.addThemeVariants(ButtonVariant.LUMO_PRIMARY, ButtonVariant.LUMO_SMALL);
                    rejectBt.getStyle().set("background-color", redColor);

                    Span spanActionStatus = new Span();

                    Span spanDateTime = new Span();
                    spanDateTime.setVisible(false);

// TODO: 04.03.2023 add state update and persist. also add record to log

                    confirmBt.addClickListener(e -> {
//                        spanDateTime.setText(LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy\tE HH:mm:ss")));
                        item.setAction(ActionType.CONFIRM);
                        item.setActionPerformedDT(LocalDateTime.now());

                        toDoItemsService.updateActionState(item);

                        grid.getDataProvider().refreshAll();
                        //todo update and log value
                    });

                    rejectBt.addClickListener(e -> {
                        item.setAction(ActionType.REJECT);
                        item.setActionPerformedDT(LocalDateTime.now());

                        toDoItemsService.updateActionState(item);
                        grid.getDataProvider().refreshAll();
                    });

                    if (item.getAction() == ActionType.CONFIRM || item.getAction() == ActionType.REJECT) {
                        confirmBt.setVisible(false);
                        rejectBt.setVisible(false);
                        spanDateTime.setVisible(true);

                        String actionText;
                        if(selectedLocale == localeForEn){
                            actionText = item.getAction() == ActionType.CONFIRM ? "Confirmed at " : "Rejected at ";
                        }else{
                            actionText = item.getAction() == ActionType.CONFIRM ? "Подтверждено " : "Отклонено ";
                        }

                        spanActionStatus.setText(actionText);
                        if (item.getActionPerformedDT() != null) {
                            spanDateTime.setText(item.getActionPerformedDT().format(DateTimeFormatter.ofPattern("dd/MM/yyyy\tE HH:mm:ss")));
                        }
                    }

                    buttonsWithTimeStamp.add(new VerticalLayout(confirmBt, rejectBt), new VerticalLayout(spanActionStatus, spanDateTime));
                    return buttonsWithTimeStamp;
                })
                .setHeader(selectedLocale == localeForEn?"Action":"Действие")
                .setWidth("170px")
                .setFlexGrow(0);


        grid.setItems(query -> toDoItemsService.listDtoSortByOperationNumber(productId, operationId,
                PageRequest.of(query.getPage(), query.getPageSize())).stream());

        grid.addThemeVariants(GridVariant.LUMO_WRAP_CELL_CONTENT);
        grid.sort(List.of(

        ));

//        grid.setHeight("700px");
        add(grid);

        // Register as EventListener
        // REMARK A Route is not a regular Spring Bean so this must be done!
        applicationContext.addApplicationListener(this);
    }

    private void createOperationInfo(ProductInfoService productInfoService) {
        ProductInfo loadedInfo = VaadinService.getCurrent().getContext().getAttribute(ProductInfo.class);
        Operation loadedOperation = VaadinService.getCurrent().getContext().getAttribute(Operation.class);
        if (loadedInfo == null || loadedOperation == null) {
            return;
        }
        productId = loadedInfo.getId();
        operationId = loadedOperation.getId();

        ProductInfo productInfo = productInfoService.findById(loadedInfo.getId()).get();
        Label productName = new Label(productInfo.getProductIdentityInfo());


        Operation operation = operationService.findById(loadedOperation.getId()).get();

        String operationNameText = "";
        if (operation.getOperationName_en().isEmpty()) {
            operationNameText = String.format("Операция # %s: %s",
                    operation.getOperationNumber(), operation.getOperationName_ru());
        } else {
            operationNameText = String.format("Операция # %s: %s",
                    operation.getOperationNumber(), operation.getOperationName_en());
        }

        Label operationNameLb = new Label(operationNameText);
        Label operDescriptionLb = new Label(operation.getOperationDescription());
        add(productName, new HorizontalLayout(operationNameLb, operDescriptionLb));
    }

    private List<OperatorToDoItemDTO> loadTestItems(SamplePersonService personService) {
        return List.of(
//                new OperatorToDoItemDTO(1,1,"ряд1","row1",false,null,null,)
                OperatorToDoItemDTO.builder()
                        .operation(1)
                        .actionNumber(1)
                        .actionDescriptionRu("описание 1.1 RU 123456789123 45678912345678912345678912 3456789123456789123456789123456789123456789123456789")
                        .actionDescriptionEn("description 1.1 EN")
                        .person(personService.getAny())
                        .build(),
                OperatorToDoItemDTO.builder()
                        .operation(10)
                        .actionNumber(99)
                        .actionDescriptionRu("описание 1.2 RU 123456789123 45678912345678912345678912 3456789123456789123456789123456789123456789123456789")
                        .actionDescriptionEn("description 1.2 EN")
                        .action(ActionType.CONFIRM)
                        .actionPerformedDT(LocalDateTime.now().minusHours(1))
                        .person(personService.getAny())
                        .build(),
                OperatorToDoItemDTO.builder()
                        .operation(1)
                        .actionNumber(3)
                        .actionDescriptionRu("описание 1.2 RU 123456789123 45678912345678912345678912 3456789123456789123456789123456789123456789123456789")
                        .actionDescriptionEn("description 1.2 EN")
                        .action(ActionType.REJECT)
                        .actionPerformedDT(LocalDateTime.now().minusMinutes(40))
                        .person(personService.getAny())
                        .build(),
                OperatorToDoItemDTO.builder()
                        .operation(1)
                        .actionNumber(4)
                        .actionDescriptionRu("описание 1.2 RU 123456789123 45678912345678912345678912 3456789123456789123456789123456789123456789123456789")
                        .actionDescriptionEn("description 1.2 EN")
                        .action(ActionType.REJECT)
                        .actionPerformedDT(LocalDateTime.now().minusMinutes(5))
                        .comment("comment by user")
                        .isCommented(true)
                        .person(personService.getAny())
                        .build()
        );
    }

    @Override
    public void onApplicationEvent(SamplePersonAddedEvent event) {
        UI.getCurrent().access(() -> grid.getDataProvider().refreshAll());
    }
}

package ch.martinelli.demo.vaadin.views;

import ch.martinelli.demo.vaadin.data.dto.OperatorToDoItemDTO;
import ch.martinelli.demo.vaadin.data.entity.ActionType;
import ch.martinelli.demo.vaadin.data.service.SamplePersonService;
import ch.martinelli.demo.vaadin.data.service.ToDoItemsService;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.dependency.CssImport;
import com.vaadin.flow.component.dependency.Uses;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.context.ConfigurableApplicationContext;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@PageTitle("Todo list")
@Route(value = "todos", layout = MainLayout.class)
@Uses(Icon.class)
@CssImport(value = "./styles/dynamic-grid-row-background-color.css",
        themeFor = "vaadin-grid")
public class ToDoGridView extends VerticalLayout implements ApplicationListener<SamplePersonAddedEvent> {
    private String greenColor = "#00853D";
    private String redColor = "#e61414";

    private Grid<OperatorToDoItemDTO> grid = new Grid<>(OperatorToDoItemDTO.class, false);

    @Autowired
    public ToDoGridView(ToDoItemsService toDoItemsService, ConfigurableApplicationContext applicationContext, SamplePersonService personService) {
        grid.setClassNameGenerator(item -> {
            if (item.getAction() == ActionType.CONFIRM) {
                return "confirmed";
            }
            if (item.getAction() == ActionType.REJECT) {
                return "rejected";
            }
            return "unprocessed";
        });

        Grid.Column<OperatorToDoItemDTO> operationNoCol = grid.addColumn(OperatorToDoItemDTO::getOperation);
        operationNoCol.setVisible(false);
        Grid.Column<OperatorToDoItemDTO> actionNumberCol = grid.addColumn(OperatorToDoItemDTO::getActionNumber);
        actionNumberCol.setVisible(false);

        Grid.Column<OperatorToDoItemDTO> stepCol = grid.addColumn(item -> item.getOperation() + "." + item.getActionNumber())
                .setHeader("#");
        stepCol.setAutoWidth(true);

        Grid.Column<OperatorToDoItemDTO> descriptionCol = grid.addColumn(OperatorToDoItemDTO::getActionDescriptionRu)
                .setHeader("Description");
        descriptionCol.setAutoWidth(true);
        Grid.Column<OperatorToDoItemDTO> actionBtCol = grid.addComponentColumn(item -> {

                    Span buttonsWithTimeStamp = new Span();
                    Button confirmBt = new Button("Confirm");
                    confirmBt.addClassName("accept-button");
                    confirmBt.addThemeVariants(ButtonVariant.LUMO_PRIMARY, ButtonVariant.LUMO_SMALL);
                    confirmBt.getStyle().set("background-color", greenColor);



                    Button rejectBt = new Button("Reject");
                    rejectBt.addClassName("accept-button");
                    rejectBt.addThemeVariants(ButtonVariant.LUMO_PRIMARY, ButtonVariant.LUMO_SMALL);
                    rejectBt.getStyle().set("background-color", redColor);
                    Span span = new Span(LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy\tE HH:mm:ss")));
                    span.setVisible(false);


                    confirmBt.addClickListener(e -> {
                        confirmBt.setVisible(false);
                        rejectBt.setVisible(false);
                        span.setVisible(true);
                        span.setText(LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy\tE HH:mm:ss")));
                        item.setAction(ActionType.CONFIRM);
                        item.setActionPerformedDT(LocalDateTime.now());
//                        grid.getDataProvider().refreshItem(item);
                        grid.getDataProvider().refreshAll();

                    });
                    rejectBt.addClickListener(e -> {
                        item.setAction(ActionType.REJECT);
                        grid.getDataProvider().refreshAll();

                    });

                    if(item.getAction()==ActionType.CONFIRM){
                        confirmBt.setVisible(false);
                        rejectBt.setVisible(false);
                        span.setVisible(true);
                        span.setText(item.getActionPerformedDT().format(DateTimeFormatter.ofPattern("dd/MM/yyyy\tE HH:mm:ss")));
                    }
                    buttonsWithTimeStamp.add(new VerticalLayout(confirmBt, rejectBt), span);
                    return buttonsWithTimeStamp;
                })
                .setHeader("Action");


        List<OperatorToDoItemDTO> testItems = loadTestItems(personService);
        grid.setItems(testItems);
//        grid.setItems(toDoItemsService.getOperatorToDosByOperationNumber(1));

//        grid.setItems(query -> samplePersonService.list(PageRequest.of(query.getPage(), query.getPageSize(),
//                VaadinSpringDataHelpers.toSpringDataSort(query))).stream());


        grid.setHeight("700px");
        add(grid);

        // Register as EventListener
        // REMARK A Route is not a regular Spring Bean so this must be done!
        applicationContext.addApplicationListener(this);
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
                        .operation(1)
                        .actionNumber(2)
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

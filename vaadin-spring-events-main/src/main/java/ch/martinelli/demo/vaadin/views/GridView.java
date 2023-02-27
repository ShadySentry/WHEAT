package ch.martinelli.demo.vaadin.views;

import ch.martinelli.demo.vaadin.data.entity.SamplePerson;
import ch.martinelli.demo.vaadin.data.service.SamplePersonService;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.dependency.Uses;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.router.RouteAlias;
import com.vaadin.flow.spring.data.VaadinSpringDataHelpers;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.data.domain.PageRequest;

@PageTitle("Grid")
@Route(value = "grid", layout = MainLayout.class)
@RouteAlias(value = "", layout = MainLayout.class)
@Uses(Icon.class)
public class GridView extends VerticalLayout implements ApplicationListener<SamplePersonAddedEvent> {

    private Grid<SamplePerson> grid = new Grid<>(SamplePerson.class, false);

    @Autowired
    public GridView(SamplePersonService samplePersonService, ConfigurableApplicationContext applicationContext) {
        grid.addColumn("firstName");
        grid.addColumn("lastName");
        grid.addColumn("email");
        grid.addColumn("phone");
        grid.addColumn("dateOfBirth");
        grid.addColumn("occupation");

        grid.setItems(query -> samplePersonService.list(PageRequest.of(query.getPage(), query.getPageSize(), VaadinSpringDataHelpers.toSpringDataSort(query))).stream());

        add(grid);

        // Register as EventListener
        // REMARK A Route is not a regular Spring Bean so this must be done!
        applicationContext.addApplicationListener(this);
    }

    @Override
    public void onApplicationEvent(SamplePersonAddedEvent event) {
        UI.getCurrent().access(() -> grid.getDataProvider().refreshAll());
    }
}

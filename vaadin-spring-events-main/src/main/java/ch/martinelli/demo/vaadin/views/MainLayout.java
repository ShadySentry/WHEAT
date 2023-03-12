package ch.martinelli.demo.vaadin.views;


import ch.martinelli.demo.vaadin.components.appnav.AppNav;
import ch.martinelli.demo.vaadin.components.appnav.AppNavItem;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.applayout.AppLayout;
import com.vaadin.flow.component.applayout.DrawerToggle;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.html.Footer;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Header;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.server.VaadinService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.context.ConfigurableApplicationContext;

import java.util.Locale;

/**
 * The main view is a top-level placeholder for other views.
 */
public class MainLayout extends AppLayout implements ApplicationListener<LocalizationChangeEvent> {
    private ConfigurableApplicationContext applicationContext;
    private H1 viewTitle;

    public static Locale selectedLocale;
    public final static Locale localeForRu = new Locale("ru", "RU");
    public final static Locale localeForEn = Locale.ENGLISH;
    private H2 descriptionH;

    @Autowired
    public MainLayout(ConfigurableApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
        applicationContext.addApplicationListener(this);

        createLocale();
        setPrimarySection(Section.DRAWER);
        addToNavbar(true, createHeaderContent());
        addToDrawer(createDrawerContent());
    }

    private void createLocale() {
        selectedLocale = VaadinService.getCurrent().getContext().getAttribute(Locale.class);
        if (selectedLocale == null) {
            selectedLocale = localeForEn;
            VaadinService.getCurrent().getContext().setAttribute(Locale.class, selectedLocale);
        }
    }

    private Component createHeaderContent() {
        DrawerToggle toggle = new DrawerToggle();
        toggle.addClassNames("view-toggle");
        toggle.addThemeVariants(ButtonVariant.LUMO_CONTRAST);
        toggle.getElement().setAttribute("aria-label", "Menu toggle");

        viewTitle = new H1();
        viewTitle.addClassNames("view-title");

        Header header = new Header(toggle, viewTitle);
        header.addClassNames("view-header");
        return header;
    }

    private Component createDrawerContent() {
        descriptionH = new H2(selectedLocale == localeForEn ? "Navigation" : "Навигация");
        descriptionH.addClassNames("app-name");

        com.vaadin.flow.component.html.Section section = new com.vaadin.flow.component.html.Section(descriptionH,
                createNavigation()/*, createFooter()*/);
        section.addClassNames("drawer-section");
        return section;
    }

    private AppNavItem homeNavItem;
    private AppNavItem operationNavItem;
    private AppNavItem todoListItem;

    private AppNav createNavigation() {
        // AppNav is not yet an official component.
        // For documentation, visit https://github.com/vaadin/vcf-nav#readme
        AppNav nav = new AppNav();
        nav.addClassNames("app-nav");

        homeNavItem = new AppNavItem(selectedLocale == localeForEn ? "Home" : "В начало",
                ProductSelectionView.class, "la la-columns");
        nav.addItem(homeNavItem);

        operationNavItem = new AppNavItem(selectedLocale == localeForEn ? "Operations" : "Операции",
                OperationsView.class, "la la-columns");
        nav.addItem(operationNavItem);

        todoListItem = new AppNavItem(selectedLocale == localeForEn ? "Todo list" : "Список задач",
                ToDoGridView.class, "la la-columns");
        nav.addItem(todoListItem);

        return nav;
    }

    private Footer createFooter() {
        Footer layout = new Footer();
        layout.addClassNames("app-nav-footer");

        return layout;
    }

    @Override
    protected void afterNavigation() {
        super.afterNavigation();
        viewTitle.setText(getCurrentPageTitle());
    }

    private String getCurrentPageTitle() {
        PageTitle title = getContent().getClass().getAnnotation(PageTitle.class);

        return title == null ? "" : title.value();
    }

    @Override
    public void onApplicationEvent(LocalizationChangeEvent event) {
//        viewTitle.setText();
        UI.getCurrent().access(() -> {
            descriptionH.setText(selectedLocale == localeForEn ? "Navigation" : "Навигация");
            homeNavItem.setLabel(selectedLocale == localeForEn ? "Home" : "В начало");
            operationNavItem.setLabel(selectedLocale == localeForEn ? "Operations" : "Операции");
            todoListItem.setLabel(selectedLocale == localeForEn ? "Todo list" : "Список задач");
        });
    }

}

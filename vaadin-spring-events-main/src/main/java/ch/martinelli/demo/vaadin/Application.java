package ch.martinelli.demo.vaadin;

import ch.martinelli.demo.vaadin.data.entity.ActionDescriptions;
import ch.martinelli.demo.vaadin.data.entity.Operation;
import ch.martinelli.demo.vaadin.repository.ActionDescriptionRepository;
import ch.martinelli.demo.vaadin.repository.OperationRepository;
import ch.martinelli.demo.vaadin.views.CsvImporter;
import com.vaadin.flow.component.dependency.NpmPackage;
import com.vaadin.flow.component.page.AppShellConfigurator;
import com.vaadin.flow.component.page.Push;
import com.vaadin.flow.server.PWA;
import com.vaadin.flow.theme.Theme;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;
import org.springframework.context.event.EventListener;

import java.util.List;

/**
 * The entry point of the Spring Boot application.
 *
 * Use the @PWA annotation make the application installable on phones, tablets
 * and some desktop browsers.
 *
 */
@Push // REMARK Enable Push to refres the View
@SpringBootApplication
@Theme(value = "vaadin-spring-events")
@PWA(name = "vaadin-spring-events", shortName = "vaadin-spring-events", offlineResources = {})
@NpmPackage(value = "line-awesome", version = "1.3.0")
@NpmPackage(value = "@vaadin-component-factory/vcf-nav", version = "1.0.6")
public class Application extends SpringBootServletInitializer implements AppShellConfigurator {


    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }

    @EventListener(ApplicationReadyEvent.class)
    public void runAfterStart(){
        populateRecords();
    }


    @Autowired
    OperationRepository operationRepository;
    @Autowired
    ActionDescriptionRepository actionDescriptionRepository;
    private void populateRecords() {
        List<Operation> operations = operationRepository.findAll();

        List<ActionDescriptions> descriptions = actionDescriptionRepository.findAll();
        CsvImporter importer = new CsvImporter(operationRepository, actionDescriptionRepository);

        importer.importDBData();

/*
        if(operations.size()>0 && descriptions.size()>0){
            return;
        }

        Operation operation = Operation.builder()
                .operationNumber(1)
                .operationName("Cooling chamber")
                .operationDescription("description for operation 1")
                .build();

        operation= operationRepository.save(operation);

        ActionDescriptions description1 = ActionDescriptions.builder()
                .actionNumber(1)
                .operationDescriptionRu("описание для операции 1")
                .getOperationDescriptionEn("description for operation 1")
                .build();

        description1=actionDescriptionRepository.save(description1);

        description1.setOperation(operation);
        description1=actionDescriptionRepository.save(description1);
        System.out.println(description1);
        */
    }

}

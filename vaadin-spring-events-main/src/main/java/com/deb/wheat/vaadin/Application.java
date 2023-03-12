package com.deb.wheat.vaadin;

import com.deb.wheat.vaadin.data.entity.ActionDescriptions;
import com.deb.wheat.vaadin.data.entity.Operation;
import com.deb.wheat.vaadin.data.entity.ProductInfo;
import com.deb.wheat.vaadin.data.entity.SamplePerson;
import com.deb.wheat.vaadin.data.service.SamplePersonRepository;
import com.deb.wheat.vaadin.data.service.ToDoItemsService;
import com.deb.wheat.vaadin.repository.ActionDescriptionRepository;
import com.deb.wheat.vaadin.repository.OperationRepository;
import com.deb.wheat.vaadin.repository.ProductInfoRepository;
import com.deb.wheat.vaadin.views.CsvImporter;
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
 * <p>
 * Use the @PWA annotation make the application installable on phones, tablets
 * and some desktop browsers.
 */
@Push // REMARK Enable Push to refresh the View
@SpringBootApplication
@Theme(value = "vaadin-spring-events")
@PWA(name = "SDO-Wheat-Notepad", shortName = "Notepad", offlineResources = {})
@NpmPackage(value = "line-awesome", version = "1.3.0")
@NpmPackage(value = "@vaadin-component-factory/vcf-nav", version = "1.0.6")
public class Application extends SpringBootServletInitializer implements AppShellConfigurator {


    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }

    @EventListener(ApplicationReadyEvent.class)
    public void runAfterStart() {
        populateRecords();
//        initOperation1();
        //todo remove all bellow
//        List<OperatorToDoItemDTO> items = toDoItemsService.getOperatorToDosByOperationNumber(1);
    }


    @Autowired
    OperationRepository operationRepository;
    @Autowired
    ActionDescriptionRepository actionDescriptionRepository;
    @Autowired
    ProductInfoRepository productInfoRepository;
    @Autowired
    SamplePersonRepository samplePersonRepository;

    private void populateRecords() {
        List<Operation> operations = operationRepository.findAll();

        List<ActionDescriptions> descriptions = actionDescriptionRepository.findAll();

        if (operations.size() == 0 || descriptions.size() == 0) {
            CsvImporter importer = new CsvImporter(operationRepository, actionDescriptionRepository);

            importer.importDBData();
        }
        List<ProductInfo> productInfos = productInfoRepository.findAll();
        if (productInfos.size() == 0) {
            ProductInfo productInfo = ProductInfo.builder()
                    .productIdentityInfo("sample item #1")
                    .description("item used to show initial structure")
                    .build();

            productInfo = productInfoRepository.save(productInfo);
        }
        List<SamplePerson> persons = samplePersonRepository.findAll();
        if (persons.size() == 0) {
            SamplePerson person = new SamplePerson();
            person.setFirstName("First name");
            person.setLastName("Last name");
            person.setPhone("+380965552211");
            samplePersonRepository.save(person);
        }


    }

    @Autowired
    private ToDoItemsService toDoItemsService;

    private void initOperation1() {
        ProductInfo info = productInfoRepository.findByProductIdentityInfo("sample item #1");
        Operation operation1 = operationRepository.findByOperationNumber(1);
        if (info == null || operation1 == null) {
            return;
        }

        toDoItemsService.build(info.getProductIdentityInfo(), operation1.getOperationNumber());
    }

}

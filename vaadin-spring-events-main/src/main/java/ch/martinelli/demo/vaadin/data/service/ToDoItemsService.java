package ch.martinelli.demo.vaadin.data.service;

import ch.martinelli.demo.vaadin.data.dto.OperatorToDoItemDTO;
import ch.martinelli.demo.vaadin.data.entity.ActionDescriptions;
import ch.martinelli.demo.vaadin.data.entity.Operation;
import ch.martinelli.demo.vaadin.data.entity.OperatorToDoItem;
import ch.martinelli.demo.vaadin.data.entity.ProductInfo;
import ch.martinelli.demo.vaadin.repository.ActionDescriptionRepository;
import ch.martinelli.demo.vaadin.repository.OperationRepository;
import ch.martinelli.demo.vaadin.repository.OperatorToDoItemsRepository;
import ch.martinelli.demo.vaadin.repository.ProductInfoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Positive;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class ToDoItemsService {
    @Autowired
    private  OperationRepository operationRepository;
    @Autowired
    private ActionDescriptionRepository actionDescriptionRepository;
    @Autowired
    private ProductInfoRepository productInfoRepository;
    @Autowired
    private OperatorToDoItemsRepository toDoItemsRepository;

    @Transactional
    public void build(){
        Operation operation1 = operationRepository.findByOperationNumber(1);
        List<ActionDescriptions> descriptionsSet = operation1.getActionsList();

        OperatorToDoItem item = OperatorToDoItem.builder()
                .productInfo(productInfoRepository.findByProductIdentityInfo("sample item #1"))
                .actionDescriptions(descriptionsSet.get(0))
                .operation(operation1)
                .build();

        item=toDoItemsRepository.save(item);
    }

    @Transactional
    public void build(@NotNull @NotBlank ProductInfo productInfo, @NotNull @Positive Integer operationNumber){
        Operation operation = operationRepository.findByOperationNumber(operationNumber);
        productInfo = productInfoRepository.findByProductIdentityInfo(productInfo.getProductIdentityInfo());

        if(operation==null || productInfo==null){
            return;
        }

        List<ActionDescriptions> descriptionsList = operation.getActionsList();

        if (descriptionsList.size()==0){
            return;
        }


        List<OperatorToDoItem> savedItems = new ArrayList<>();
        List<OperatorToDoItem> foundItems = toDoItemsRepository.findAll();

        for(ActionDescriptions description:descriptionsList){
            // TODO: 01.03.2023 add check if item already exists
            OperatorToDoItem item = OperatorToDoItem.builder()
                    .productInfo(productInfo)
                    .actionDescriptions(description)
                    .operation(operation)
                    .build();

            item=toDoItemsRepository.save(item);

            //todo initialize from other side of relation
//            productInfo.addToDoItem(item);
//            productInfo=productInfoRepository.save(productInfo);
            if(item.getId()!=null){
                savedItems.add(item);
            }

        }

        for(OperatorToDoItem item:savedItems){
            productInfo.addToDoItem(item);
        }
        productInfo=productInfoRepository.save(productInfo);
        productInfo.getDescription();
    }

    public List<OperatorToDoItemDTO> getOperatorToDosByOperationNumber(@Positive Integer operationNUmber){
        List<OperatorToDoItem> items = toDoItemsRepository.findAllByOperationNumber(operationNUmber);
        List<OperatorToDoItemDTO> dto= items.stream().map(OperatorToDoItem::toDto).toList();

        return dto;
    }


    public Page<OperatorToDoItem> list(Pageable pageable) {
        return toDoItemsRepository.findAll(pageable);
    }

    public Page<OperatorToDoItemDTO> listDto(Pageable pageable) {
        Page page =toDoItemsRepository.findAll(pageable);
        Page<OperatorToDoItemDTO> dtoPage = page.map(new Function<OperatorToDoItem,OperatorToDoItemDTO>(){
            @Override
            public OperatorToDoItemDTO apply(OperatorToDoItem item) {
                OperatorToDoItemDTO dto = item.toDto();
                return dto;
            }
        });


        return dtoPage;
    }
}

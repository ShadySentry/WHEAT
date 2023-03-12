package com.deb.wheat.vaadin.data.service;

import com.deb.wheat.vaadin.data.dto.OperatorToDoItemDTO;
import com.deb.wheat.vaadin.data.entity.ActionDescriptions;
import com.deb.wheat.vaadin.data.entity.Operation;
import com.deb.wheat.vaadin.data.entity.OperatorToDoItem;
import com.deb.wheat.vaadin.data.entity.ProductInfo;
import com.deb.wheat.vaadin.repository.ActionDescriptionRepository;
import com.deb.wheat.vaadin.repository.OperationRepository;
import com.deb.wheat.vaadin.repository.OperatorToDoItemsRepository;
import com.deb.wheat.vaadin.repository.ProductInfoRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Positive;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Function;

@Service
@Slf4j
public class ToDoItemsService {
    @Autowired
    private OperationRepository operationRepository;
    @Autowired
    private ActionDescriptionRepository actionDescriptionRepository;
    @Autowired
    private ProductInfoRepository productInfoRepository;
    @Autowired
    private OperatorToDoItemsRepository toDoItemsRepository;

    @Transactional
    public void build() {
        Operation operation1 = operationRepository.findByOperationNumber(1);
        List<ActionDescriptions> descriptionsSet = operation1.getActionsList();

        OperatorToDoItem item = OperatorToDoItem.builder()
                .productInfo(productInfoRepository.findByProductIdentityInfo("sample item #1"))
                .actionDescriptions(descriptionsSet.get(0))
                .operation(operation1)
                .build();

        item = toDoItemsRepository.save(item);
    }

    @Transactional
    public void build(@NotNull @NotBlank String productIdentityInfo, @NotNull @Positive Integer operationNumber) {
        Operation operation = operationRepository.findByOperationNumber(operationNumber);
        ProductInfo productInfo = productInfoRepository.findByProductIdentityInfo(productIdentityInfo);

        if (operation == null || productInfo == null) {
            log.error("Can't create ToDos. Missing arguments [operation = {};productInfo= {}] in {}",
                    operation == null ? "null" : operation.toString(),
                    productInfo == null ? "null" : productInfo.toString(),
                    this.getClass().getSimpleName());
            return;
        }

        log.info(String.format("Generating ToDoItems for operation= ${0} and productInfo= ${1}", operation, productInfo));

        List<ActionDescriptions> descriptionsList = operation.getActionsList();

        if (descriptionsList.size() == 0) {
            log.error(String.format("Can't create ToDos. Used operation without ActionDescriptions [operation = ${0}] in ${1}",
                    operation.toString(),
                    this.getClass().getSimpleName()));
            return;
        }

        List<OperatorToDoItem> savedItems = new ArrayList<>();

        Integer foundItem = toDoItemsRepository.findOneByProductInfoAndOperation(productInfo.getId(), operation.getId());

        if (foundItem > 0) {
            log.info("OperatorToDoItems already initialized for product= {} and operation= {}", productInfo, operation);

            return;
        }
        for (ActionDescriptions description : descriptionsList) {
            // TODO: 01.03.2023 add check if item already exists
            OperatorToDoItem item = OperatorToDoItem.builder()
                    .productInfo(productInfo)
                    .actionDescriptions(description)
                    .operation(operation)
                    .build();

            item = toDoItemsRepository.save(item);

            //todo initialize from other side of relation
//            productInfo.addToDoItem(item);
//            productInfo=productInfoRepository.save(productInfo);
            if (item.getId() != null) {
                savedItems.add(item);
            }

        }

        for (OperatorToDoItem item : savedItems) {
            productInfo.addToDoItem(item);
        }
        productInfo = productInfoRepository.save(productInfo);
        productInfo.getDescription();
    }

    public List<OperatorToDoItemDTO> getOperatorToDosByOperationNumber(@Positive Integer operationNUmber) {
        List<OperatorToDoItem> items = toDoItemsRepository.findAllByOperationNumber(operationNUmber);
        List<OperatorToDoItemDTO> dto = items.stream().map(OperatorToDoItem::toDto).toList();

        return dto;
    }


    public Page<OperatorToDoItem> list(Pageable pageable) {
        return toDoItemsRepository.findAll(pageable);
    }

    public Page<OperatorToDoItemDTO> listDto(Pageable pageable) {
        Page page = toDoItemsRepository.findAll(pageable);
        Page<OperatorToDoItemDTO> dtoPage = page.map(new Function<OperatorToDoItem, OperatorToDoItemDTO>() {
            @Override
            public OperatorToDoItemDTO apply(OperatorToDoItem item) {
                OperatorToDoItemDTO dto = item.toDto();
                return dto;
            }
        });


        return dtoPage;
    }

    public Page<OperatorToDoItemDTO> listDtoSortByOperationNumber(UUID productId, UUID operationId, Pageable pageable) {
        Page page = toDoItemsRepository.findAllByProductInfoAndOperation(productId, operationId, pageable);

        Page<OperatorToDoItemDTO> dtoPage = page.map(new Function<OperatorToDoItem, OperatorToDoItemDTO>() {
            @Override
            public OperatorToDoItemDTO apply(OperatorToDoItem item) {
                return item.toDto();
            }
        });

        return dtoPage;
    }

    @Transactional
    public OperatorToDoItemDTO updateActionState(OperatorToDoItemDTO toDoItemDto) {
        Operation operation = operationRepository.findByOperationNumber(toDoItemDto.getOperation());

//        Optional<ProductInfo> productInfo = productInfoRepository.findById(productIdentityId);
//        if (productInfo.isEmpty()) {
//            log.error("Cant update toDoItemDto {} ProductInfo with id {} doesnt exists", toDoItemDto, productIdentityId);
//            return null;
//        }

//        Optional<ActionDescriptions> actionDescription = actionDescriptionRepository.findByOperationId(operation.getId());
//        if (actionDescription.isEmpty()) {
//            log.error("Cant update toDoItemDto {} Reason - operation {} doesnt hove correct actionDescription", toDoItemDto, operation.toString());
//            return null;
//        }

//        OperatorToDoItem item = fromDto(toDoItemDto, operation, productInfo.get(), actionDescription.get());
        Optional<OperatorToDoItem> item = toDoItemsRepository.findById(toDoItemDto.getOperatorToDoItemId());
        if (item.isEmpty()) {
            log.error("item doesnt exists {}", toDoItemDto);
            return null;
        }

        item.get().setAction(toDoItemDto.getAction());
        item.get().setComment(toDoItemDto.getComment());
        item.get().setActionPerformedDT(toDoItemDto.getActionPerformedDT());

        OperatorToDoItem savedItem = toDoItemsRepository.save(item.get());
        if(savedItem==null){
            return null;
        }
        return savedItem.toDto();
    }

    private OperatorToDoItem fromDto(OperatorToDoItemDTO toDoItemDto, Operation operation, ProductInfo productInfo, ActionDescriptions actionDescription) {
        OperatorToDoItem converted = OperatorToDoItem.builder()
                .productInfo(productInfo)
                .operation(operation)
                .actionDescriptions(actionDescription)
                .actionPerformedDT(toDoItemDto.getActionPerformedDT())
                .action(toDoItemDto.getAction())
                .isCommented(toDoItemDto.isCommented())
                .comment(toDoItemDto.getComment())
                .build();

        return converted;
    }
}

package com.deb.wheat.vaadin.repository;

import com.deb.wheat.vaadin.data.entity.OperatorToDoItem;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface OperatorToDoItemsRepository extends JpaRepository<OperatorToDoItem, UUID> {
    @Query("select o from OperatorToDoItem o WHERE o.operation.operationNumber =?1 order by o.actionDescriptions.actionNumber")
    List<OperatorToDoItem> findAllByOperationNumber(Integer operationNUmber);

    @Query("SELECT o FROM OperatorToDoItem  o WHERE o.productInfo.id =? 1 and o.operation.id =? 2  order by o.actionDescriptions.actionNumber asc")
    Page<OperatorToDoItem> findAllByProductInfoAndOperation(UUID productId, UUID operationId, Pageable pageable);

    @Query("SELECT o FROM OperatorToDoItem  o WHERE o.productInfo.id =? 1 and o.operation.id =? 2  order by o.actionDescriptions.actionNumber asc")
    List<OperatorToDoItem> findAllByProductInfoAndOperation(UUID productId, UUID operationId);

    @Query("select COUNT(o) FROM OperatorToDoItem o WHERE o.productInfo.id= ?1 AND o.operation.id= ?2")
    Integer findOneByProductInfoAndOperation(UUID productId, UUID operationId);

    @Query("SELECT o FROM OperatorToDoItem  o WHERE o.productInfo.id =? 1 order by o.actionDescriptions.actionNumber asc")
    List<OperatorToDoItem> findAllByProductInfoId(UUID productId);
}

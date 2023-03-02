package ch.martinelli.demo.vaadin.repository;

import ch.martinelli.demo.vaadin.data.entity.OperatorToDoItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface OperatorToDoItemsRepository extends JpaRepository<OperatorToDoItem, UUID> {
    @Query("select o from OperatorToDoItem o WHERE o.operation.operationNumber =?1 order by o.actionDescriptions.actionNumber")
    List<OperatorToDoItem> findAllByOperationNumber(Integer operationNUmber);
}

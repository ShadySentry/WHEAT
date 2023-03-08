package ch.martinelli.demo.vaadin.repository;

import ch.martinelli.demo.vaadin.data.entity.Operation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface OperationRepository extends JpaRepository<Operation, UUID> {
    Operation findByOperationNumber(Integer operationNumber);

}

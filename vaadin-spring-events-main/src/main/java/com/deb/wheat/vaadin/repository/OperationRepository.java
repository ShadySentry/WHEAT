package com.deb.wheat.vaadin.repository;

import com.deb.wheat.vaadin.data.entity.Operation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface OperationRepository extends JpaRepository<Operation, UUID> {
    Operation findByOperationNumber(Integer operationNumber);

}

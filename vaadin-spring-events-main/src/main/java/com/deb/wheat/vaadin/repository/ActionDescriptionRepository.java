package com.deb.wheat.vaadin.repository;

import com.deb.wheat.vaadin.data.entity.ActionDescriptions;
import jdk.jfr.Registered;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Registered
public interface ActionDescriptionRepository extends JpaRepository<ActionDescriptions, UUID> {
    List<ActionDescriptions> findAllByActionNumber (Integer actionNumber);

    Optional<ActionDescriptions> findByOperationId(UUID operationId);
}
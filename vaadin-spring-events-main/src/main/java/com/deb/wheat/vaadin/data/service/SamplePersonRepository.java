package com.deb.wheat.vaadin.data.service;

import com.deb.wheat.vaadin.data.entity.SamplePerson;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

public interface SamplePersonRepository extends JpaRepository<SamplePerson, UUID> {

}
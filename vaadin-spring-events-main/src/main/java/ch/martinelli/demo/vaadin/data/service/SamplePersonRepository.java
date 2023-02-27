package ch.martinelli.demo.vaadin.data.service;

import ch.martinelli.demo.vaadin.data.entity.SamplePerson;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SamplePersonRepository extends JpaRepository<SamplePerson, UUID> {

}
package ch.martinelli.demo.vaadin.data.service;

import ch.martinelli.demo.vaadin.data.entity.Operation;
import ch.martinelli.demo.vaadin.repository.OperationRepository;
import ch.martinelli.demo.vaadin.views.OperationsView;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Positive;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class OperationService {
    private final OperationRepository operationRepository;

    @Autowired
    public OperationService(OperationRepository operationRepository) {
        this.operationRepository = operationRepository;
    }

    public List<Operation> getAll(){
        return operationRepository.findAll();
    }

    public Optional<Operation> findById(@NotNull UUID id){
        return operationRepository.findById(id);
    }

    public Operation findByName(@Positive Integer operationNUmber){
        return operationRepository.findByOperationNumber(operationNUmber);
    }

    public Operation save(@NotNull Operation operation){
        return operationRepository.save(operation);
    }

    public Page<Operation> list(Pageable pageable) {
        return operationRepository.findAll(pageable);
    }
}


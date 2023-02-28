package ch.martinelli.demo.vaadin.data.entity;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
public class OperatorToDoItems extends AbstractEntity {

    @ManyToOne
    private ProductInfo manufactureInfo;
    @ManyToOne
    private SamplePerson person;

    @ManyToOne
    private Operation operation;

    @ManyToOne
    private ActionDescriptions actionDescriptions;

    private LocalDateTime actionPerformedDT;
    @Enumerated(EnumType.ORDINAL)
    private ActionType action;
    private boolean isCommented;
    private String comment;

}

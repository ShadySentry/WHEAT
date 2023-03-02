package ch.martinelli.demo.vaadin.data.entity;

import lombok.*;

import javax.persistence.Entity;
import javax.persistence.ManyToOne;

@Entity
@Getter
@Setter
@AllArgsConstructor
@RequiredArgsConstructor
@Builder
public class ActionDescriptions extends AbstractEntity{

    private Integer actionNumber;
    private String actionNameRu;
    private String actionNameEn;

    @ManyToOne()
    private Operation operation;
}

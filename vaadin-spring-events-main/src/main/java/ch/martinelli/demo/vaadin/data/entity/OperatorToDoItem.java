package ch.martinelli.demo.vaadin.data.entity;

import ch.martinelli.demo.vaadin.data.dto.OperatorToDoItemDTO;
import lombok.*;

import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.ManyToOne;
import javax.transaction.Transactional;
import java.time.LocalDateTime;

@Builder
@AllArgsConstructor
@RequiredArgsConstructor
@Getter
@Setter
@Entity
public class OperatorToDoItem extends AbstractEntity {

    @ManyToOne
    private ProductInfo productInfo;
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

    @Transactional
    public OperatorToDoItemDTO toDto() {
        OperatorToDoItemDTO dto = OperatorToDoItemDTO
                .builder()
                .operation(this.operation.getOperationNumber())
                .actionNumber(this.actionDescriptions.getActionNumber())
                .actionDescriptionRu(actionDescriptions.getActionNameRu())
                .actionDescriptionEn(actionDescriptions.getActionNameEn())
                .isCommented(isCommented)
                .action(action)
                .actionPerformedDT(actionPerformedDT)
                .person(person)
                .build();

        return dto;
    }

}

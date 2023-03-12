package com.deb.wheat.vaadin.data.entity;

import lombok.*;

import javax.persistence.Entity;
import javax.persistence.OneToMany;
import java.util.List;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Operation extends AbstractEntity{

    private Integer operationNumber;
    private String operationName_ru;
    private String operationName_en;
    private String operationDescription;

    @OneToMany(mappedBy = "operation")
    private List<ActionDescriptions> actionsList;

}

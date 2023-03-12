package com.deb.wheat.vaadin.data.dto;

import com.deb.wheat.vaadin.data.entity.ActionType;
import com.deb.wheat.vaadin.data.entity.SamplePerson;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;


@Data
@AllArgsConstructor
@Builder
public class OperatorToDoItemDTO {
    private UUID operatorToDoItemId;
    private Integer operation;
    private Integer actionNumber;
    private String actionDescriptionRu;
    private String actionDescriptionEn;
    private boolean isCommented=false;
    private String comment;
    private ActionType action;
    private LocalDateTime actionPerformedDT;
    private SamplePerson person;
}

package org.exoplatform.task.dto;

import lombok.Data;
import org.exoplatform.task.domain.*;

import java.io.Serializable;


@Data
public class LabelTaskMappingDto implements Serializable {
    private Label label;

    private Task task;

    public LabelTaskMappingDto(LabelTaskMapping labelTaskMapping) {
        this.label=labelTaskMapping.getLabel();
        this.task=labelTaskMapping.getTask();
    }

}

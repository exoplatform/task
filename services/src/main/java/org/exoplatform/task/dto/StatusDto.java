package org.exoplatform.task.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.exoplatform.task.domain.Project;
import org.exoplatform.task.domain.Status;
import org.exoplatform.task.domain.Task;

import java.io.Serializable;
import java.util.*;


@Data
@NoArgsConstructor
public class StatusDto implements Serializable {
    private long id;

    private String name;

    private Integer rank;

    private List<Task> tasks;

    private Project project;

    public StatusDto(Status status) {
        this.id=status.getId();
        this.name=status.getName();
        this.rank=status.getRank();
        this.project=status.getProject();
    }

    public StatusDto(long id, String name) {
        this.id = id;
        this.name = name;
    }

    public StatusDto(long id, String name, Integer rank, Project project) {
        this.id = id;
        this.name = name;
        this.rank = rank;
        this.project = project;
    }

}

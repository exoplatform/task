package org.exoplatform.task.dto;

import lombok.Data;
import org.exoplatform.task.model.GroupKey;
import org.exoplatform.task.rest.model.TaskEntity;

import java.util.List;
import java.util.Map;

@Data
public class TasksList {
    private List<TaskDto> listTasks = null;
    private long tasksSize = 0;

    public TasksList() {
    }

    public TasksList(List<TaskDto> listTasks, long tasksSize) {
        this.listTasks = listTasks;
        this.tasksSize = tasksSize;
    }


}

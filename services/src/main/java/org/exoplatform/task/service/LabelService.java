package org.exoplatform.task.service;

import org.exoplatform.commons.utils.ListAccess;
import org.exoplatform.task.domain.Label;
import org.exoplatform.task.dto.LabelDto;
import org.exoplatform.task.dto.TaskDto;
import org.exoplatform.task.exception.EntityNotFoundException;

import java.util.List;

public interface LabelService {
    ListAccess<LabelDto> findLabelsByUser(String username);

    ListAccess<LabelDto> findLabelsByTask(long taskId, String username) throws EntityNotFoundException;

    LabelDto getLabel(long labelId);

    LabelDto createLabel(LabelDto label);

    LabelDto updateLabel(LabelDto label, List<Label.FIELDS> fields) throws EntityNotFoundException;

    void removeLabel(long labelId);


    void addTaskToLabel(TaskDto task, Long labelId) throws EntityNotFoundException;

    void removeTaskFromLabel(TaskDto task, Long labelId) throws EntityNotFoundException;
}

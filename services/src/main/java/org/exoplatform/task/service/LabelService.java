package org.exoplatform.task.service;

import java.util.List;

import org.exoplatform.task.domain.Label;
import org.exoplatform.task.dto.LabelDto;
import org.exoplatform.task.dto.TaskDto;
import org.exoplatform.task.exception.EntityNotFoundException;

public interface LabelService {
  List<LabelDto> findLabelsByUser(String username, int offset, int limit);

  List<LabelDto> findLabelsByTask(long taskId, String username, int offset, int limit);

  LabelDto getLabel(long labelId);

  LabelDto createLabel(LabelDto label);

  LabelDto updateLabel(LabelDto label, List<Label.FIELDS> fields) throws EntityNotFoundException;

  void removeLabel(long labelId);

  void addTaskToLabel(TaskDto task, Long labelId) throws EntityNotFoundException;

  void removeTaskFromLabel(TaskDto task, Long labelId) throws EntityNotFoundException;
}

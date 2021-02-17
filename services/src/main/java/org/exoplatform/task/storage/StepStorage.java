package org.exoplatform.task.storage;

import java.util.List;

import org.exoplatform.task.dto.StepDto;
import org.exoplatform.task.dto.TaskDto;
import org.exoplatform.task.exception.EntityNotFoundException;

public interface StepStorage {

  StepDto getStep(long StepId);

  List<StepDto> getSteps(long taskId, int offset, int limit);

  int countSteps(long taskId);

  StepDto addStep(TaskDto task, String username, String name) throws EntityNotFoundException;

  void removeStep(long StepId) throws EntityNotFoundException;

}

package org.exoplatform.task.service;

import java.util.List;

import org.exoplatform.task.dto.StepDto;
import org.exoplatform.task.dto.TaskDto;
import org.exoplatform.task.exception.EntityNotFoundException;

public interface StepService {

  String TASK_STEP_CREATION = "exo.task.taskStepCreation";

  StepDto getStep(long stepId);

  StepDto addStep(TaskDto task, String username, String stepText) throws EntityNotFoundException;

  StepDto updateStep(long stepId, String stepText, boolean completed) throws EntityNotFoundException;

  void removeStep(long stepId) throws EntityNotFoundException;

  List<StepDto> getSteps(long taskId, int offset, int limit);

  int countSteps(long taskId);

}

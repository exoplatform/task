package org.exoplatform.task.service.impl;

import java.util.List;
import java.util.regex.Pattern;

import javax.inject.Inject;

import org.exoplatform.commons.api.persistence.ExoTransactional;
import org.exoplatform.services.listener.ListenerService;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.task.dao.DAOHandler;
import org.exoplatform.task.domain.Label;
import org.exoplatform.task.dto.LabelDto;
import org.exoplatform.task.dto.StepDto;
import org.exoplatform.task.dto.TaskDto;
import org.exoplatform.task.exception.EntityNotFoundException;
import org.exoplatform.task.service.StepService;
import org.exoplatform.task.storage.ProjectStorage;
import org.exoplatform.task.storage.StepStorage;
import org.exoplatform.task.storage.TaskStorage;
import org.exoplatform.task.util.StorageUtil;

public class StepServiceImpl implements StepService {
  private static final Log     LOG     = ExoLogger.getExoLogger(StepServiceImpl.class);

  private static final Pattern pattern = Pattern.compile("@([^\\s]+)|@([^\\s]+)$");

  @Inject
  private TaskStorage          taskStorage;

  @Inject
  private StepStorage          stepStorage;

  @Inject
  private DAOHandler           daoHandler;

  private ListenerService      listenerService;

  private ProjectStorage projectStorage;

  public StepServiceImpl(TaskStorage taskStorage,
                         ProjectStorage projectStorage,
                         StepStorage stepStorage,
                         DAOHandler daoHandler,
                         ListenerService listenerService) {
    this.taskStorage = taskStorage;
    this.projectStorage = projectStorage;
    this.stepStorage = stepStorage;
    this.daoHandler = daoHandler;
    this.listenerService = listenerService;
  }

  @Override
  public StepDto getStep(long stepId) {
    return stepStorage.getStep(stepId);
  }

  @Override
  public List<StepDto> getSteps(long taskId, int offset, int limit) {
    return stepStorage.getSteps(taskId, offset, limit);
  }

  @Override
  public int countSteps(long taskId) {
    return stepStorage.countSteps(taskId);
  }

  @Override
  @ExoTransactional
  public StepDto addStep(TaskDto task, String username, String step) throws EntityNotFoundException {

    StepDto stepDto = stepStorage.addStep(task, username, step);

    try {
      if (stepDto.getTask().getStatus() != null) {
        listenerService.broadcast("exo.project.projectModified", null, stepDto.getTask().getStatus().getProject());
      }
    } catch (Exception e) {
      LOG.error("Error while broadcasting task creation event", e);
    }

    return stepDto;
  }

  @Override
  public StepDto updateStep(long stepId, String stepText, boolean completed) throws EntityNotFoundException {
    StepDto stepDto = getStep(stepId);
    if (stepDto == null) {
      throw new EntityNotFoundException(stepId, StepDto.class);
    }
    stepDto.setCompleted(completed);
    stepDto.setName(stepText);
    return StorageUtil.stepToDto(daoHandler.getStepHandler().update(StorageUtil.stepToEntity(stepDto)),projectStorage);
  }

  @Override
  @ExoTransactional
  public void removeStep(long stepId) throws EntityNotFoundException {

    StepDto step = stepStorage.getStep(stepId);

    if (step == null) {
      LOG.info("Can not find step with ID: " + stepId);
      throw new EntityNotFoundException(stepId, StepDto.class);
    }

    stepStorage.removeStep(stepId);
  }

}

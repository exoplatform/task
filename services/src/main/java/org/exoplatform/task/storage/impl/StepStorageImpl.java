package org.exoplatform.task.storage.impl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import javax.inject.Inject;

import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.task.dao.DAOHandler;
import org.exoplatform.task.domain.Step;
import org.exoplatform.task.dto.StepDto;
import org.exoplatform.task.dto.TaskDto;
import org.exoplatform.task.exception.EntityNotFoundException;
import org.exoplatform.task.storage.ProjectStorage;
import org.exoplatform.task.storage.StepStorage;
import org.exoplatform.task.util.StorageUtil;

public class StepStorageImpl implements StepStorage {

  private static final Log     LOG     = ExoLogger.getExoLogger(StepStorageImpl.class);

  private static final Pattern pattern = Pattern.compile("@([^\\s]+)|@([^\\s]+)$");

  @Inject
  private final DAOHandler     daoHandler;

  @Inject
  private final ProjectStorage projectStorage;

  public StepStorageImpl(DAOHandler daoHandler, ProjectStorage projectStorage) {
    this.daoHandler = daoHandler;
    this.projectStorage = projectStorage;
  }

  @Override
  public StepDto getStep(long stepId) {
    return StorageUtil.stepToDto(daoHandler.getStepHandler().find(stepId), projectStorage);
  }

  @Override
  public List<StepDto> getSteps(long taskId, int offset, int limit) {
    try {
      return Arrays.asList(daoHandler.getStepHandler().findSteps(taskId).load(offset, limit))
                   .stream()
                   .map((Step step) -> StorageUtil.stepToDto(step, projectStorage))
                   .collect(Collectors.toList());
    } catch (Exception e) {
      return new ArrayList<StepDto>();
    }
  }

  @Override
  public int countSteps(long taskId) {
    try {
      return daoHandler.getStepHandler().findSteps(taskId).getSize();
    } catch (Exception e) {
      return 0;
    }
  }

  @Override
  public StepDto addStep(TaskDto task, String username, String step) throws EntityNotFoundException {
    StepDto newStep = new StepDto();
    newStep.setTask(task);
    newStep.setAuthor(username);
    newStep.setName(step);
    newStep.setCreatedTime(new Date());
    return StorageUtil.stepToDto(daoHandler.getStepHandler().create(StorageUtil.stepToEntity(newStep)), projectStorage);
  }

  @Override
  public void removeStep(long stepId) throws EntityNotFoundException {
    daoHandler.getStepHandler().delete(StorageUtil.stepToEntity(getStep(stepId)));
  }

}

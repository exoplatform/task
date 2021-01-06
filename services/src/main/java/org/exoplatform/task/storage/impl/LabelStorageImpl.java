package org.exoplatform.task.storage.impl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import javax.inject.Inject;

import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.task.dao.DAOHandler;
import org.exoplatform.task.domain.Label;
import org.exoplatform.task.dto.LabelDto;
import org.exoplatform.task.dto.TaskDto;
import org.exoplatform.task.exception.EntityNotFoundException;
import org.exoplatform.task.storage.LabelStorage;

public class LabelStorageImpl implements LabelStorage {

  private static final Log     LOG     = ExoLogger.getExoLogger(LabelStorageImpl.class);

  private static final Pattern pattern = Pattern.compile("@([^\\s]+)|@([^\\s]+)$");

  @Inject
  private final DAOHandler     daoHandler;

  public LabelStorageImpl(DAOHandler daoHandler) {
    this.daoHandler = daoHandler;
  }

  @Override
  public List<LabelDto> findLabelsByUser(String username, int offset, int limit) {
    try {
      return Arrays.asList(daoHandler.getLabelHandler().findLabelsByUser(username).load(offset, limit))
                   .stream()
                   .map(this::labelToDto)
                   .collect(Collectors.toList());
    } catch (Exception e) {
      return new ArrayList<LabelDto>();
    }
  }

  @Override
  public List<LabelDto> findLabelsByTask(long taskId, String username, int offset, int limit) {
    try {
      return Arrays.asList(daoHandler.getLabelHandler().findLabelsByTask(taskId, username).load(offset, limit))
                   .stream()
                   .map(this::labelToDto)
                   .collect(Collectors.toList());
    } catch (Exception e) {
      return new ArrayList<LabelDto>();
    }
  }

  @Override
  public LabelDto getLabel(long labelId) {
    return labelToDto(daoHandler.getLabelHandler().find(labelId));
  }

  @Override
  public LabelDto createLabel(LabelDto label) {
    return labelToDto(daoHandler.getLabelHandler().create(labelToEntity(label)));
  }

  @Override
  public LabelDto updateLabel(LabelDto label, List<Label.FIELDS> fields) throws EntityNotFoundException {
    return null;
  }

  @Override
  public void removeLabel(long labelId) {
    daoHandler.getLabelHandler().delete(labelToEntity(getLabel(labelId)));
  }

  @Override
  public void addTaskToLabel(TaskDto task, Long labelId) throws EntityNotFoundException {
  }

  @Override
  public void removeTaskFromLabel(TaskDto task, Long labelId) throws EntityNotFoundException {

  }

  @Override
  public Label labelToEntity(LabelDto labelDto) {
    if(labelDto==null){
      return null;
    }
    Label label = new Label();
    label.setUsername(labelDto.getUsername());
    label.setName(labelDto.getName());
    label.setColor(labelDto.getColor());
    label.setHidden(labelDto.isHidden());
    label.setParent(labelToEntity(labelDto.getParent()));
    return label;
  }

  public Label mappingLabelToEntity(LabelDto labelDto) {
    if(labelDto==null){
      return null;
    }
    Label label = new Label();
    label.setId(labelDto.getId());
    label.setUsername(labelDto.getUsername());
    label.setName(labelDto.getName());
    label.setColor(labelDto.getColor());
    label.setHidden(labelDto.isHidden());
    label.setParent(labelToEntity(labelDto.getParent()));
    return label;
  }

  @Override
  public LabelDto labelToDto(Label label) {
    if(label==null){
      return null;
    }
    LabelDto labelDto = new LabelDto(label);
    labelDto.setId(label.getId());
    labelDto.setUsername(label.getUsername());
    labelDto.setName(label.getName());
    labelDto.setColor(label.getColor());
    labelDto.setHidden(label.isHidden());
    labelDto.setParent(labelToDto(label.getParent()));
    return labelDto;
  }
}

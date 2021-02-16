package org.exoplatform.task.service.impl;

import org.exoplatform.commons.api.persistence.ExoTransactional;
import org.exoplatform.commons.utils.ListAccess;
import org.exoplatform.services.listener.ListenerService;
import org.exoplatform.task.dao.DAOHandler;
import org.exoplatform.task.domain.Label;
import org.exoplatform.task.domain.LabelTaskMapping;
import org.exoplatform.task.dto.LabelDto;
import org.exoplatform.task.dto.TaskDto;
import org.exoplatform.task.exception.EntityNotFoundException;
import org.exoplatform.task.service.LabelService;
import org.exoplatform.task.storage.LabelStorage;
import org.exoplatform.task.storage.TaskStorage;

import javax.inject.Inject;
import java.util.List;

public class LabelServiceImpl implements LabelService {

    @Inject
    private DAOHandler daoHandler;

    @Inject
    private TaskStorage taskStorage;

    @Inject
    private LabelStorage labelStorage;

    private ListenerService listenerService;

    public LabelServiceImpl(LabelStorage labelStorage, TaskStorage taskStorage, DAOHandler daoHandler) {
        this.labelStorage = labelStorage;
        this.taskStorage = taskStorage;
        this.daoHandler = daoHandler;
    }


    @Override
    public List<LabelDto> findLabelsByUser(String username, int offset, int limit) {
        return labelStorage.findLabelsByUser(username,offset,limit);
    }

    @Override
    public List<LabelDto> findLabelsByTask(long taskId, String username, int offset, int limit) {
        return labelStorage.findLabelsByTask(taskId, username,offset,limit);
    }

    @Override
    public LabelDto getLabel(long labelId) {
        return labelStorage.labelToDto(daoHandler.getLabelHandler().find(labelId));
    }

    @Override
    @ExoTransactional
    public LabelDto createLabel(LabelDto label) {
        return labelStorage.createLabel(label);
    }

    @Override
    @ExoTransactional
    public LabelDto updateLabel(LabelDto label, List<Label.FIELDS> fields) throws EntityNotFoundException {
        LabelDto lb = getLabel(label.getId());
        if (lb == null) {
            throw new EntityNotFoundException(label.getId(), LabelDto.class);
        }

        //Todo: validate input and throw exception if need
        for (Label.FIELDS field : fields) {
            switch (field) {
                case NAME:
                    lb.setName(label.getName());
                    break;
                case COLOR:
                    lb.setColor(label.getColor());
                    break;
                case PARENT:
                    lb.setParent(label.getParent());
                    break;
                case HIDDEN:
                    lb.setHidden(label.isHidden());
            }
        }
        return labelStorage.labelToDto(daoHandler.getLabelHandler().update(labelStorage.mappingLabelToEntity(lb)));
    }

    @Override
    @ExoTransactional
    public void removeLabel(long labelId) {
        daoHandler.getLabelHandler().delete(labelStorage.labelToEntity(getLabel(labelId)));
    }


    @Override
    @ExoTransactional
    public void addTaskToLabel(TaskDto task, Long labelId) throws EntityNotFoundException {
        LabelTaskMapping mapping = new LabelTaskMapping();
        mapping.setLabel(labelStorage.mappingLabelToEntity(getLabel(labelId)));
        mapping.setTask(taskStorage.toEntity(task));
        daoHandler.getLabelTaskMappingHandler().create(mapping);
    }

    @Override
    @ExoTransactional
    public void removeTaskFromLabel(TaskDto task, Long labelId) throws EntityNotFoundException {
        LabelTaskMapping mapping = new LabelTaskMapping();
        mapping.setLabel(labelStorage.mappingLabelToEntity(getLabel(labelId)));
        mapping.setTask(taskStorage.toEntity(task));
        daoHandler.getLabelTaskMappingHandler().delete(mapping);
    }


}

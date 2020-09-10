package org.exoplatform.task.storage.impl;

import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.task.dao.DAOHandler;
import org.exoplatform.task.dao.StatusHandler;
import org.exoplatform.task.domain.Status;
import org.exoplatform.task.dto.ProjectDto;
import org.exoplatform.task.dto.StatusDto;
import org.exoplatform.task.exception.EntityNotFoundException;
import org.exoplatform.task.exception.NotAllowedOperationOnEntityException;
import org.exoplatform.task.storage.ProjectStorage;
import org.exoplatform.task.storage.StatusStorage;

import javax.inject.Inject;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class StatusStorageImpl implements StatusStorage {

    private static final Log LOG = ExoLogger.getExoLogger(StatusStorageImpl.class);

    private static final Pattern pattern = Pattern.compile("@([^\\s]+)|@([^\\s]+)$");


    @Inject
    private final DAOHandler daoHandler;

    @Inject
    private final ProjectStorage projectStorage;


    public StatusStorageImpl(DAOHandler daoHandler, ProjectStorage projectStorage) {
        this.daoHandler = daoHandler;
        this.projectStorage = projectStorage;
    }


    @Override
    public StatusDto getStatus(long statusId) {
        return statusToDTO(daoHandler.getStatusHandler().find(statusId));
    }

    @Override
    public StatusDto getDefaultStatus(long projectId) {
        return statusToDTO(daoHandler.getStatusHandler().findLowestRankStatusByProject(projectId));
    }

    @Override
    public List<StatusDto> getStatuses(long projectId) {
        List<Status> statusDtos = daoHandler.getStatusHandler().getStatuses(projectId);
        return statusDtos.stream().map(this::statusToDTO).collect(Collectors.toList());
    }

    @Override
    public StatusDto createStatus(ProjectDto project, String status) {
        //
        List<StatusDto> statuses = getStatuses(project.getId());
        if (statuses != null) {
            for (StatusDto st : statuses) {
                if (st.getName().equalsIgnoreCase(status)) {
                    LOG.warn("Status {} has already exists", status);
                    return st;
                }
            }
        }

        StatusDto max = statusToDTO(daoHandler.getStatusHandler().findHighestRankStatusByProject(project.getId()));
        int maxRank = max != null && max.getRank() != null ? max.getRank() : -1;

        StatusHandler handler = daoHandler.getStatusHandler();
        Status st = new Status(status, ++maxRank, projectStorage.projectToEntity(project));
        handler.create(st);
        return statusToDTO(st);
    }

    @Override
    public StatusDto removeStatus(long statusId) throws EntityNotFoundException, NotAllowedOperationOnEntityException {
        StatusHandler handler = daoHandler.getStatusHandler();
        StatusDto st = statusToDTO(handler.find(statusId));
        if (st == null) {
            throw new EntityNotFoundException(statusId, Status.class);
        }

        ProjectDto project = projectStorage.projectToDto(st.getProject());
        StatusDto altStatus = findAltStatus(st, project);
        if (altStatus == null) {
            throw new NotAllowedOperationOnEntityException(statusId, Status.class, "Delete last status");
        }
        //
        daoHandler.getTaskHandler().updateStatus(statusToEntity(st), statusToEntity(altStatus));

        //
        st.setProject(null);
        handler.delete(statusToEntity(st));
        return st;
    }

    @Override
    public StatusDto updateStatus(long statusId, String statusName) throws EntityNotFoundException, NotAllowedOperationOnEntityException {
        StatusHandler handler = daoHandler.getStatusHandler();
        StatusDto status = statusToDTO(handler.find(statusId));
        if (status == null) {
            throw new EntityNotFoundException(statusId, Status.class);
        }
        Status curr = handler.findByName(statusName, status.getProject().getId());
        if (curr != null && !status.equals(curr)) {
            throw new NotAllowedOperationOnEntityException(status.getId(), Status.class, "duplicate status name");
        }

        status.setName(statusName);
        return statusToDTO(daoHandler.getStatusHandler().update(statusToEntity(status)));
    }

    @Override
    public Status statusToEntity(StatusDto statusDto) {
        Status status = new Status();
        status.setId(statusDto.getId());
        status.setName(statusDto.getName());
        status.setRank(statusDto.getRank());
        status.setProject(statusDto.getProject());
        return status;
    }

    @Override
    public StatusDto statusToDTO(Status status) {
        StatusDto statusDto = new StatusDto();
        statusDto.setId(status.getId());
        statusDto.setName(status.getName());
        statusDto.setRank(status.getRank());
        statusDto.setProject(status.getProject());
        return statusDto;
    }

    private StatusDto findAltStatus(StatusDto st, ProjectDto project) {
        List<StatusDto> allSt = new LinkedList<StatusDto>(getStatuses(project.getId()));
        Collections.sort(allSt.stream().map(this::statusToEntity).collect(Collectors.toList()));

        StatusDto other = null;
        for (int i = 0; i < allSt.size(); i++) {
            if (allSt.get(i).equals(st)) {
                if (i > 0) {
                    other = allSt.get(i - 1);
                } else if (i + 1 < allSt.size()) {
                    other = allSt.get(i + 1);
                }
                break;
            }
        }
        return other;
    }
}

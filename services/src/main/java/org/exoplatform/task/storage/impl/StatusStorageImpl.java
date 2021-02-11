package org.exoplatform.task.storage.impl;

import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import javax.inject.Inject;
import javax.persistence.NonUniqueResultException;

import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.task.dao.DAOHandler;
import org.exoplatform.task.dao.StatusHandler;
import org.exoplatform.task.domain.Project;
import org.exoplatform.task.domain.Status;
import org.exoplatform.task.domain.Task;
import org.exoplatform.task.dto.ProjectDto;
import org.exoplatform.task.dto.StatusDto;
import org.exoplatform.task.exception.EntityNotFoundException;
import org.exoplatform.task.exception.NotAllowedOperationOnEntityException;
import org.exoplatform.task.storage.ProjectStorage;
import org.exoplatform.task.storage.StatusStorage;
import org.exoplatform.task.storage.TaskStorage;
import org.exoplatform.task.util.StorageUtil;

public class StatusStorageImpl implements StatusStorage {

  private static final Log     LOG     = ExoLogger.getExoLogger(StatusStorageImpl.class);

  private static final Pattern pattern = Pattern.compile("@([^\\s]+)|@([^\\s]+)$");

  @Inject
  private final DAOHandler     daoHandler;

  @Inject
  private final ProjectStorage projectStorage;

  public StatusStorageImpl(DAOHandler daoHandler, ProjectStorage projectStorage) {
    this.daoHandler = daoHandler;
    this.projectStorage = projectStorage;
  }

  @Override
  public StatusDto getStatus(long statusId) {
    return StorageUtil.statusToDTO(daoHandler.getStatusHandler().find(statusId),projectStorage);
  }

  @Override
  public StatusDto getDefaultStatus(long projectId) {
    Status status = null;
    try {
      status = daoHandler.getStatusHandler().findLowestRankStatusByProject(projectId);
    }catch (NonUniqueResultException e) {
      List<Status> projectStatuses = daoHandler.getStatusHandler().getStatuses(projectId);
      projectStatuses = projectStatuses.stream().sorted(Comparator.comparing(Status::getId).reversed()).sorted(Comparator.comparingInt(Status::getRank)).collect(Collectors.toList());
      for(int i = 0; i<projectStatuses.size() ; i++){
        projectStatuses.get(i).setRank(i);
      }
      daoHandler.getStatusHandler().updateAll(projectStatuses);
      status = projectStatuses.get(0);
    }
    return StorageUtil.statusToDTO(status,projectStorage);
  }

  @Override
  public List<StatusDto> getStatuses(long projectId) {
    List<Status> projectStatuses = daoHandler.getStatusHandler().getStatuses(projectId);
    Set<Integer> set = new HashSet<>();
    if(!projectStatuses.stream().allMatch(t -> set.add(t.getRank()))){
      projectStatuses = projectStatuses.stream().sorted(Comparator.comparing(Status::getId).reversed()).sorted(Comparator.comparingInt(Status::getRank)).collect(Collectors.toList());
      for(int i = 0; i<projectStatuses.size() ; i++){
        projectStatuses.get(i).setRank(i);
      }
      daoHandler.getStatusHandler().updateAll(projectStatuses);
    }
    return projectStatuses.stream().map((Status status) -> StorageUtil.statusToDTO(status,projectStorage)).collect(Collectors.toList());
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

    StatusDto max = StorageUtil.statusToDTO(daoHandler.getStatusHandler().findHighestRankStatusByProject(project.getId()),projectStorage);
    int maxRank = max != null && max.getRank() != null ? max.getRank() : -1;

    StatusHandler handler = daoHandler.getStatusHandler();
    Status st = new Status(status, ++maxRank, StorageUtil.projectToEntity(project));
    handler.create(st);
    return StorageUtil.statusToDTO(st,projectStorage);
  }

  @Override
  public StatusDto createStatus(ProjectDto project, String status, int rank) throws NotAllowedOperationOnEntityException{
    //
    List<StatusDto> statuses = getStatuses(project.getId());
    if (statuses != null) {
      for (StatusDto st : statuses) {
        if (st.getName().equalsIgnoreCase(status)) {
          LOG.warn("Status {} has already exists", status);
          throw new NotAllowedOperationOnEntityException(0, Status.class, "duplicate status name");
        }
      }
    }
    StatusHandler handler = daoHandler.getStatusHandler();
    Status st = new Status(status, rank, StorageUtil.projectToEntity(project));
    handler.create(st);
    return StorageUtil.statusToDTO(st,projectStorage);
  }

  @Override
  public void removeStatus(long statusId) throws Exception {
    StatusHandler handler = daoHandler.getStatusHandler();
    Status st = handler.find(statusId);
    if (st == null) {
      throw new EntityNotFoundException(statusId, Status.class);
    }

    Project project = st.getProject();
    Status altStatus = findAltStatus(st, project);
    if (altStatus == null) {
      throw new NotAllowedOperationOnEntityException(statusId, Status.class, "Delete last status");
    }
    List<Task> tasks = daoHandler.getTaskHandler().getByStatus(statusId);
    for(Task task : tasks){
      task.setStatus(altStatus);
    }
    daoHandler.getTaskHandler().updateAll(tasks);
    //
    st.setProject(null);
    handler.delete(st);
  }

  @Override
  public StatusDto updateStatus(long statusId, String statusName) throws EntityNotFoundException,
                                                                  NotAllowedOperationOnEntityException {
    StatusHandler handler = daoHandler.getStatusHandler();
    StatusDto status = StorageUtil.statusToDTO(handler.find(statusId),projectStorage);
    if (status == null) {
      throw new EntityNotFoundException(statusId, Status.class);
    }
    List<Status> peojectStatus = handler.getStatuses(status.getProject().getId());
    for(Status st : peojectStatus){
      if(st.getName().equals(statusName)&&st.getId()!=statusId){
        throw new NotAllowedOperationOnEntityException(status.getId(), Status.class, "duplicate status name");
      }
    }
    status.setName(statusName);
    return StorageUtil.statusToDTO(daoHandler.getStatusHandler().update(StorageUtil.statusToEntity(status)),projectStorage);
  }

  @Override
  public StatusDto updateStatus(StatusDto statusDto) throws EntityNotFoundException,
                                                                  NotAllowedOperationOnEntityException {
    StatusHandler handler = daoHandler.getStatusHandler();
    StatusDto status = StorageUtil.statusToDTO(handler.find(statusDto.getId()),projectStorage);
    if (status == null) {
      throw new EntityNotFoundException(statusDto.getId(), Status.class);
    }
    List<Status> peojectStatus = handler.getStatuses(status.getProject().getId());
    for(Status st : peojectStatus){
      if(st.getName().equals(statusDto.getName())&&st.getId()!=status.getId()){
        throw new NotAllowedOperationOnEntityException(status.getId(), Status.class, "duplicate status name");
      }
    }
    return StorageUtil.statusToDTO(daoHandler.getStatusHandler().update(StorageUtil.statusToEntity(statusDto)),projectStorage);
  }


  private Status findAltStatus(Status st, Project project) {
    List<Status> allSt = new LinkedList<Status>(daoHandler.getStatusHandler().getStatuses(project.getId()));
    Collections.sort(allSt);

    Status other = null;
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

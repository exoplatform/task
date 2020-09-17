package org.exoplatform.task.storage;

import java.util.List;

import org.exoplatform.task.domain.Status;
import org.exoplatform.task.dto.ProjectDto;
import org.exoplatform.task.dto.StatusDto;
import org.exoplatform.task.exception.EntityNotFoundException;
import org.exoplatform.task.exception.NotAllowedOperationOnEntityException;

public interface StatusStorage {

  /**
   * Return the <code>Status</code> with given <code>statusId</code>.
   *
   * @param statusId the given status id.
   * @return the status of the given statusId.
   */
  StatusDto getStatus(long statusId);

  /**
   * Return the default status of the project which is ideally the first step in
   * the project workflow.
   *
   * @param projectId the given project id.
   * @return the default status of the given project.
   */
  StatusDto getDefaultStatus(long projectId);

  /**
   * Return the list of statuses from a project with given <code>projectId</code>.
   *
   * @param projectId the given project id.
   * @return the status of the given project.
   */
  List<StatusDto> getStatuses(long projectId);

  StatusDto createStatus(ProjectDto project, String status);

  StatusDto removeStatus(long statusId) throws EntityNotFoundException, NotAllowedOperationOnEntityException;

  StatusDto updateStatus(long statusId, String statusName) throws EntityNotFoundException, NotAllowedOperationOnEntityException;

  public Status statusToEntity(StatusDto statusDto);

  public StatusDto statusToDTO(Status status);

  public List<StatusDto> listStatusToDTOs(List<Status> status);

  public List<Status> listStatusToEntitys(List<StatusDto> status);

}

package org.exoplatform.task.service;

import java.util.List;

import org.exoplatform.task.domain.Project;
import org.exoplatform.task.domain.Status;
import org.exoplatform.task.exception.EntityNotFoundException;
import org.exoplatform.task.exception.NotAllowedOperationOnEntityException;

public interface StatusService {

  void createDefaultStatuses(Project project);

  Status getStatus(long statusId);

  /**
   * Return the default status of the project which is ideally the first step in the project workflow.
   *
   * @param projectId
   *
   * @return the default status of the given project.
   */
  Status getDefaultStatus(long projectId);

  List<Status> getStatuses(long projectId);
    
  Status createStatus(Project project, String status);
  
  Status removeStatus(long statusId) throws EntityNotFoundException, NotAllowedOperationOnEntityException;
  
  Status updateStatus(long statusId, String statusName) throws EntityNotFoundException, NotAllowedOperationOnEntityException;
}

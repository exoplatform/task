package org.exoplatform.task.service;

import java.util.List;

import org.exoplatform.task.domain.Project;
import org.exoplatform.task.domain.Status;
import org.exoplatform.task.exception.EntityNotFoundException;
import org.exoplatform.task.exception.NotAllowedOperationOnEntityException;

public interface StatusService {

  /**
   * Create initial statuses for given <code>project</code>.
   *
   * The initial statuses can be configured via a system exo property <code>exo.tasks.default.status</code>.
   * If it's not configured, the default <code>{"To Do", "In Progress", "Waiting On", "Done"}</code> will be used.
   *
   * @param project
   */
  void createInitialStatuses(Project project);

  /**
   * Return the <code>Status</code> with given <code>statusId</code>.
   *
   * @param statusId
   * @return
   */
  Status getStatus(long statusId);

  /**
   * Return the default status of the project which is ideally the first step in the project workflow.
   *
   * @param projectId
   *
   * @return the default status of the given project.
   */
  Status getDefaultStatus(long projectId);

  /**
   * Return the list of statuses from a project with given <code>projectId</code>.
   *
   * @param projectId
   * @return
   */
  List<Status> getStatuses(long projectId);
    
  Status createStatus(Project project, String status);
  
  Status removeStatus(long statusId) throws EntityNotFoundException, NotAllowedOperationOnEntityException;
  
  Status updateStatus(long statusId, String statusName) throws EntityNotFoundException, NotAllowedOperationOnEntityException;
}

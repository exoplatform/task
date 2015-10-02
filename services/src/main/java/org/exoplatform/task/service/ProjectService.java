package org.exoplatform.task.service;

import java.util.List;
import java.util.Set;

import org.exoplatform.task.dao.OrderBy;
import org.exoplatform.task.domain.Project;
import org.exoplatform.task.domain.Task;
import org.exoplatform.task.exception.EntityNotFoundException;
import org.exoplatform.task.exception.ParameterEntityException;

/**
 * Created by TClement on 6/3/15.
 */
public interface ProjectService {

  /**
   * Return the project with given <code>projectId</code>.
   *
   * @param projectId
   * @return
   * @throws EntityNotFoundException
   */
  Project getProject(Long projectId) throws EntityNotFoundException;

  /**
   * Create a project with given <code>project</code> model object.
   *
   * @param project
   * @return
   */
  Project createProject(Project project);

  /**
   * Create a sub-project with given <code>project</code> model object and parent project ID.
   *
   * @param project the project metadata to create.
   * @param parentId parent project ID
   * @return
   * @throws EntityNotFoundException the project associated with <code>parentId</code> doesn't exist.
   */
  Project createProject(Project project, long parentId) throws EntityNotFoundException;

  Project updateProject(Project project);

  void removeProject(long projectId, boolean deleteChild) throws EntityNotFoundException;

  Project cloneProject(long projectId, boolean cloneTask) throws EntityNotFoundException;

  List<Project> getSubProjects(long parentId);

  List<Project> findProjects(List<String> memberships, String keyword, OrderBy order);
}

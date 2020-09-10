package org.exoplatform.task.legacy.service;

import java.util.List;
import java.util.Set;

import org.exoplatform.commons.utils.ListAccess;
import org.exoplatform.task.dao.OrderBy;
import org.exoplatform.task.dao.ProjectQuery;
import org.exoplatform.task.domain.Project;
import org.exoplatform.task.exception.EntityNotFoundException;

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

  Set<String> getManager(long projectId);
  
  Set<String> getParticipator(long projectId);
  
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

  /**
   * Update the project.
   *
   * It should throws EntityNotFoundException if the project has been removed OR not existed from database.
   * 
   * @param project
   * @return
   */
  Project updateProject(Project project);

  /**
   * Remove the project with given <code>projectId</code>,
   * and also its descendants if <code>deleteChild</code> is true.
   *
   * @param projectId
   * @param deleteChild
   * @throws EntityNotFoundException
   */
  void removeProject(long projectId, boolean deleteChild) throws EntityNotFoundException;

  /**
   * Clone a project with given <code>projectId</code>. If <code>cloneTask</code> is true,
   * it will also clone all non-completed tasks from the project.
   *
   * @param projectId The id of a project which it copies from.
   * @param cloneTask If false, it will clone only project metadata.
   *        Otherwise, it also clones all non-completed tasks from the project.
   *
   * @return The cloned project.
   * @throws EntityNotFoundException
   */
  Project cloneProject(long projectId, boolean cloneTask) throws EntityNotFoundException;

  /**
   * Return a list of children of a parent project with given <code>parentId</code>.
   *
   * @param parentId
   * @return
   */
  ListAccess<Project> getSubProjects(long parentId);

  ListAccess<Project> findProjects(ProjectQuery query);

  ListAccess<Project> findProjects(List<String> memberships, String keyword, OrderBy order);
}

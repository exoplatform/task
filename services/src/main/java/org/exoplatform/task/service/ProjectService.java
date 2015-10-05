package org.exoplatform.task.service;

import java.util.List;
import java.util.Set;

import org.exoplatform.services.security.Identity;
import org.exoplatform.task.dao.OrderBy;
import org.exoplatform.task.domain.Project;
import org.exoplatform.task.domain.Task;
import org.exoplatform.task.exception.NotAllowedOperationOnEntityException;
import org.exoplatform.task.exception.ParameterEntityException;
import org.exoplatform.task.exception.ProjectNotFoundException;

/**
 * Created by TClement on 6/3/15.
 */
public interface ProjectService {

  Project createDefaultStatusProjectWithManager(String name, String description, boolean calInteg, Long parentId, String username)
      throws ProjectNotFoundException;

  Project createDefaultStatusProjectWithAttributes(Long parentId, String name, String description,
                                                   boolean calInteg, Set<String> managers, Set<String> participators)
      throws ProjectNotFoundException;

  Project createDefaultStatusProject(Project project);

  Project createProject(Project project);

  Project updateProjectInfo(long id, String param, String[] values)
      throws ProjectNotFoundException, ParameterEntityException;
  
  Project updateProjectInfo(long id, Long parentId, String name, String description, Boolean calendarIntegrated, String color)
      throws ProjectNotFoundException, ParameterEntityException;

  void deleteProjectById(long id, boolean deleteChild) throws ProjectNotFoundException;

  void deleteProject(Project project, boolean deleteChild);

  Project cloneProjectById(long id, boolean cloneTask) throws ProjectNotFoundException;

  Project getProjectById(Long id) throws ProjectNotFoundException;

  Task createTaskToProjectId(long id, Task task) throws ProjectNotFoundException;

  List<Task> getTasksByProjectId(List<Long> ids, OrderBy orderBy);

  List<Task> getTasksWithKeywordByProjectId(List<Long> ids, OrderBy orderBy, String keyword);

  Project removePermissionFromProjectId(Long id, String permission, String type)
      throws ProjectNotFoundException, NotAllowedOperationOnEntityException;

  Project addPermissionsFromProjectId(Long id, String permissions, String type)
      throws ProjectNotFoundException, NotAllowedOperationOnEntityException;
  
  List<Project> getProjectTreeByMembership(List<String> memberships);
 
  List<Project> findProjectByKeyWord(Identity identity, String keyword, OrderBy order);

}

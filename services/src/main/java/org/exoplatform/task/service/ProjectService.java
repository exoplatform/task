package org.exoplatform.task.service;

import org.exoplatform.services.security.Identity;
import org.exoplatform.task.dao.OrderBy;
import org.exoplatform.task.domain.Project;
import org.exoplatform.task.domain.Task;

import java.util.List;
import java.util.Set;

/**
 * Created by TClement on 6/3/15.
 */
public interface ProjectService {

  Project createDefaultStatusProjectWithManager(String name, String description, Long parentId, String username);

  Project createDefaultStatusProjectWithAttributes(Long parentId, String name, String description,
                        Set<String> managers, Set<String> participators);

  Project createDefaultStatusProject(Project project);

  Project createProject(Project project);

  Project updateProjectInfo(long id, String param, String[] values);

  boolean deleteProjectById(long id);

  void deleteProject(Project project);

  Project cloneProjectById(long id, boolean cloneTask);

  Project getProjectById(Long id);

  Task createTaskToProjectId(long id, Task task);

  List<Task> getTasksByProjectId(long id, OrderBy orderBy);

  List<Task> getTasksWithKeywordByProjectId(long id, OrderBy orderBy, String keyword);

  Project removePermissionFromProjectId(Long id, String permission, String type);

  Project addPermissionsFromProjectId(Long id, String permissions, String type);

  List<Project> getProjectTreeByIdentity(Identity identity);

}

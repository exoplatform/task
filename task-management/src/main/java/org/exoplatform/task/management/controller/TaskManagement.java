/*
 * Copyright (C) 2015 eXo Platform SAS.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */

package org.exoplatform.task.management.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;

import javax.inject.Inject;

import juzu.Action;
import juzu.Path;
import juzu.Response;
import juzu.View;
import juzu.impl.common.Tools;
import juzu.request.SecurityContext;

import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.services.organization.OrganizationService;
import org.exoplatform.task.domain.Project;
import org.exoplatform.task.domain.Task;
import org.exoplatform.task.domain.UserSetting;
import org.exoplatform.task.exception.ProjectNotFoundException;
import org.exoplatform.task.exception.TaskNotFoundException;
import org.exoplatform.task.model.TaskModel;
import org.exoplatform.task.service.ProjectService;
import org.exoplatform.task.service.TaskParser;
import org.exoplatform.task.service.TaskService;
import org.exoplatform.task.service.UserService;
import org.exoplatform.task.utils.TaskUtil;

/**
 * @author <a href="mailto:tuyennt@exoplatform.com">Tuyen Nguyen The</a>.
 */
public class TaskManagement {

  private static final Log LOG = ExoLogger.getExoLogger(TaskManagement.class);
  
  @Inject
  TaskService taskService;

  @Inject
  TaskParser taskParser;

  @Inject
  UserService userService;
  
  @Inject
  OrganizationService orgService;
  
  @Inject
  ProjectService projectService;

  @Inject
  @Path("index.gtmpl")
  org.exoplatform.task.management.templates.index index;

  @Inject
  ResourceBundle bundle;
  
  @Inject
  ProjectController projectController;

  @View
  public Response.Content index(Long taskId, SecurityContext securityContext) throws ProjectNotFoundException {
    //TODO: should check if username is null?
    String username = securityContext.getRemoteUser();
    
    long currProject = -1;
    TaskModel taskModel = null;
    List<Task> tasks = null;
    Project project = null;
    if (taskId != null) {
      try {
        taskModel = TaskUtil.getTaskModel(taskId, bundle, username, taskService, 
                                                    orgService, userService, projectService);
      } catch (TaskNotFoundException e) {
        LOG.error(e);
      }
      project = taskModel.getTask().getStatus().getProject();
      currProject = project.getId();
      tasks = projectService.getTasksByProjectId(currProject, null);      
    }
    
    if (taskModel == null) {
      tasks = taskService.getIncomingTasksByUser(username, null);      
    }

    Map<String, List<Task>> groupTasks = new HashMap<String, List<Task>>();
    groupTasks.put("", tasks);

    List<Project> projects = projectController.getProjectTree();

    UserSetting setting = userService.getUserSetting(username);

    long taskNum = taskService.getTaskNum(username, null);
    long currTaskId = taskId == null ? -1 : taskId;
    
    return index.with()
        .currentProjectId(currProject)
        .taskId(currTaskId)
        .taskModel(taskModel)
        .project(project)
        .tasks(tasks)
        .taskNum(taskNum)
        .groupTasks(groupTasks)
        .keyword("")
        .groupBy("")
        .orderBy("")
        .filter("")
        .projects(projects)
        .userSetting(setting)
        .bundle(bundle)
        .ok().withCharset(Tools.UTF_8);
  }

  @Action
  public Response changeViewState(String groupBy, String orderBy) {
    return TaskManagement_.index(null);
  }

  @Action
  public Response createTask(String taskInput, String groupBy, String orderBy) {
    if(taskInput != null && !taskInput.isEmpty()) {
      taskService.createTask(taskParser.parse(taskInput));
    } else {

    }
    return TaskManagement_.index(null);
  }
}

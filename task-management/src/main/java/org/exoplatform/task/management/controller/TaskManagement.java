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

import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
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
import org.exoplatform.task.dao.OrderBy;
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
import org.exoplatform.task.utils.ProjectUtil;
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
  
  @Inject
  NavigationState navState;

  @View
  public Response.Content index(String space_group_id, SecurityContext securityContext) throws ProjectNotFoundException {
    //TODO: should check if username is null?
    String username = securityContext.getRemoteUser();
    
    long currProject = space_group_id == null ? -1 : -2;
    TaskModel taskModel = null;
    List<Task> tasks = null;
    Project project = null;
    long taskId = navState.getTaskId();
    //
    if (taskId != -1) {
      try {
        taskModel = TaskUtil.getTaskModel(taskId, false, bundle, username, taskService,
                                                    orgService, userService, projectService);
        if (taskModel.getTask().getStatus() != null) {
          project = taskModel.getTask().getStatus().getProject();
          currProject = project.getId();
          tasks = projectService.getTasksByProjectId(Arrays.asList(currProject), null);      
        }
      } catch (TaskNotFoundException e) {
        taskId = -1;
      }
    }

    List<Long> spaceProjectIds = null;
    List<Project> projects = ProjectUtil.getProjectTree(space_group_id, projectService);
    if (space_group_id != null) {
      spaceProjectIds = new LinkedList<Long>();
      for (Project p : projects) {
        spaceProjectIds.add(p.getId());
      }            
    }
    
    if (tasks == null) {
      if (space_group_id != null) {
        tasks = taskService.getToDoTasksByUser(username, spaceProjectIds, null, null, null);
      } else {
        tasks = taskService.getIncomingTasksByUser(username, new OrderBy.DESC("createdTime"));
      }
    }

    Map<String, List<Task>> groupTasks = new HashMap<String, List<Task>>();
    groupTasks.put("", tasks);

    UserSetting setting = userService.getUserSetting(username);

    long taskNum = TaskUtil.getTaskNum(username, spaceProjectIds, currProject, taskService);
    
    Map<String, String> defOrders = TaskUtil.getDefOrders(bundle);
    Map<String, String> defGroupBys = TaskUtil.getDefGroupBys(currProject, bundle);
    
    return index.with()
        .currentProjectId(currProject)
        .taskId(taskId)
        .taskModel(taskModel)
        .orders(defOrders)
        .groups(defGroupBys)
        .project(project)
        .tasks(tasks)
        .taskNum(taskNum)
        .groupTasks(groupTasks)
        .keyword("")
        .groupBy(TaskUtil.NONE)
        .orderBy("createdTime")
        .filter("")
        .projects(projects)
        .userSetting(setting)
        .userTimezone(userService.getUserTimezone(username))
        .bundle(bundle)
        .isInSpace(space_group_id != null)
        .viewType("")
        .ok().withCharset(Tools.UTF_8);
  }
  
  @Action
  public Response permalink(String space_group_id, Long taskId) {
    navState.setTaskId(taskId);
    return TaskManagement_.index(space_group_id);
  }

  @Action
  public Response changeViewState(String space_group_id, String groupBy, String orderBy) {
    return TaskManagement_.index(space_group_id);
  }

  @Action
  public Response createTask(String space_group_id, String taskInput, String groupBy, String orderBy) {
    if(taskInput != null && !taskInput.isEmpty()) {
      taskService.createTask(taskParser.parse(taskInput));
    } else {

    }
    return TaskManagement_.index(space_group_id);
  }
}

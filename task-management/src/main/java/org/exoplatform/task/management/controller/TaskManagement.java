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

import org.exoplatform.commons.utils.ListAccess;
import org.exoplatform.portal.application.PortalRequestContext;
import org.exoplatform.portal.application.RequestNavigationData;
import org.exoplatform.portal.webui.util.Util;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.services.organization.OrganizationService;
import org.exoplatform.task.dao.TaskQuery;
import org.exoplatform.task.domain.Label;
import org.exoplatform.task.domain.Project;
import org.exoplatform.task.domain.Task;
import org.exoplatform.task.domain.UserSetting;
import org.exoplatform.task.exception.EntityNotFoundException;
import org.exoplatform.task.model.GroupKey;
import org.exoplatform.task.model.TaskModel;
import org.exoplatform.task.service.ParserContext;
import org.exoplatform.task.service.ProjectService;
import org.exoplatform.task.service.TaskParser;
import org.exoplatform.task.service.TaskService;
import org.exoplatform.task.service.UserService;
import org.exoplatform.task.util.ProjectUtil;
import org.exoplatform.task.util.TaskUtil;

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
  public Response.Content index(String space_group_id, SecurityContext securityContext) throws EntityNotFoundException {
    //TODO: should check if username is null?
    String username = securityContext.getRemoteUser();
    PortalRequestContext prc = Util.getPortalRequestContext();
    String requestPath = prc.getControllerContext().getParameter(RequestNavigationData.REQUEST_PATH);

    TaskModel taskModel = null;
    List<Task> tasks = null;
    Project project = null;
    
    long currProject = space_group_id == null ? -1 : -2;
    if (space_group_id == null) {
      //. Load project ID from URL
      long id = ProjectUtil.getProjectIdFromURI(requestPath);
      if (id > 0 && prc.getControllerContext().getRequest().getQueryString() == null) {
        currProject = id;
        project = projectService.getProject(currProject);
      } else {
        currProject = -1;
      }
    } else {
      currProject = -2;
    }        

    long taskId = navState.getTaskId();
    if (taskId <= 0) {
      //. Load task ID from URL
      long id = TaskUtil.getTaskIdFromURI(requestPath);
      if (id > 0) {
        taskId = id;
      }
    }

    TaskQuery taskQuery = new TaskQuery();

    //
    if (taskId != -1) {
      try {
        taskModel = TaskUtil.getTaskModel(taskId, false, bundle, username, taskService,
                                                    orgService, userService, projectService);
        if (taskModel.getTask().getStatus() != null) {
          project = taskModel.getTask().getStatus().getProject();
          currProject = project.getId();
          //TaskQuery taskQuery = new TaskQuery();
          taskQuery.setProjectIds(Arrays.asList(currProject));
          //ListAccess<Task> listTasks = taskService.findTasks(taskQuery);
          //tasks = Arrays.asList(ListUtil.load(listTasks, 0, -1)); //taskService.findTaskByQuery(taskQuery);
        }
      } catch (EntityNotFoundException e) {
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
    
    //if (tasks == null) {
    if (taskId <= 0) {
      if (space_group_id != null) {
        //taskQuery.setIsTodo(Boolean.TRUE);
        //taskQuery.setUsername(username);
        taskQuery.setIsTodoOf(username);
        taskQuery.setProjectIds(spaceProjectIds);
        //ListAccess<Task> listTasks = taskService.getTodoTasks(username, spaceProjectIds, null, null, null);
        //tasks = Arrays.asList(ListUtil.load(listTasks, 0, -1)); //taskService.getToDoTasksByUser(username, spaceProjectIds, null, null, null);
      } else if (currProject > 0) {
        //TaskQuery taskQuery = new TaskQuery();
        taskQuery.setProjectIds(Arrays.asList(currProject));
        //ListAccess<Task> listTasks = taskService.findTasks(taskQuery);
        //tasks = Arrays.asList(ListUtil.load(listTasks, 0, -1)); //taskService.findTaskByQuery(taskQuery);
      } else {
        //taskQuery.setIsIncoming(Boolean.TRUE);
        //taskQuery.setUsername(username);
        taskQuery.setIsIncomingOf(username);
        //ListAccess<Task> listTasks = taskService.getIncomingTasks(username, new OrderBy.DESC("createdTime"));
        //tasks = Arrays.asList(ListUtil.load(listTasks, 0, -1)); //taskService.getIncomingTasksByUser(username, new OrderBy.DESC("createdTime"));
      }
    }

    Map<GroupKey, ListAccess<Task>> groupTasks = TaskUtil.findTasks(taskService, taskQuery, "", null, userService);

    UserSetting setting = userService.getUserSetting(username);

    //long taskNum = TaskUtil.getTaskNum(username, spaceProjectIds, currProject, taskService);
    long taskNum = TaskUtil.countTasks(taskService, taskQuery);

    Map<String, String> defOrders = TaskUtil.getDefOrders(bundle);
    Map<String, String> defGroupBys = TaskUtil.getDefGroupBys(currProject, bundle);
    
    List<Label> labels = TaskUtil.buildRootLabels(taskService.findLabelsByUser(username));
    
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
        .advanceSearch(false)
        .groupBy(TaskUtil.NONE)
        .orderBy("createdTime")
        .filter("")
        .projects(projects)
        .labels(labels)
        .userSetting(setting)
        .userTimezone(userService.getUserTimezone(username))
        .bundle(bundle)
        .isInSpace(space_group_id != null)
        .viewType("")
        .currentLabelId(-1)
        .taskService(taskService)
        .currentUser(username)
        .ok().withCharset(Tools.UTF_8);
  }
  
  @Action
  public Response permalink(String space_group_id, Long taskId) {
    navState.setTaskId(taskId);
    return TaskManagement_.index(space_group_id);
  }

  //TODO: this method is not used any more?
  @Action
  public Response changeViewState(String space_group_id, String groupBy, String orderBy) {
    return TaskManagement_.index(space_group_id);
  }

  @Action
  public Response createTask(String space_group_id, String taskInput, String groupBy, String orderBy, SecurityContext securityContext) {    
    if(taskInput != null && !taskInput.isEmpty()) {
      String currentUser = securityContext.getRemoteUser();
      ParserContext context = new ParserContext(userService.getUserTimezone(currentUser));
      taskService.createTask(taskParser.parse(taskInput, context));
    } else {

    }
    return TaskManagement_.index(space_group_id);
  }
}

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

import javax.inject.Inject;

import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;

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
import org.exoplatform.services.security.ConversationState;
import org.exoplatform.services.security.Identity;
import org.exoplatform.task.dao.TaskQuery;
import org.exoplatform.task.domain.Label;
import org.exoplatform.task.domain.Project;
import org.exoplatform.task.domain.Task;
import org.exoplatform.task.domain.UserSetting;
import org.exoplatform.task.exception.EntityNotFoundException;
import org.exoplatform.task.management.model.Paging;
import org.exoplatform.task.model.GroupKey;
import org.exoplatform.task.model.TaskModel;
import org.exoplatform.task.service.ParserContext;
import org.exoplatform.task.service.ProjectService;
import org.exoplatform.task.service.TaskParser;
import org.exoplatform.task.service.TaskService;
import org.exoplatform.task.service.UserService;
import org.exoplatform.task.util.ListUtil;
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
    String username = securityContext.getRemoteUser();
    PortalRequestContext prc = Util.getPortalRequestContext();
    String requestPath = prc.getControllerContext().getParameter(RequestNavigationData.REQUEST_PATH);

    String errorMessage = "";

    TaskModel taskModel = null;
    List<Task> tasks = null;
    Project project = null;

    long currProject;
    if (space_group_id == null) {
      //. Load project ID from URL
      long id = ProjectUtil.getProjectIdFromURI(requestPath);
      if (id > -100 && prc.getControllerContext().getRequest().getQueryString() == null) {
        currProject = id;
        if (id > 0) {
          try {
            project = projectService.getProject(currProject);
          } catch (EntityNotFoundException ex) {
            errorMessage = bundle.getString("popup.msg.projectNotExist");
          }
        }
      } else {
        currProject = ProjectUtil.INCOMING_PROJECT_ID;
      }
    } else {
      currProject = ProjectUtil.TODO_PROJECT_ID;
    }        

    long taskId = navState.getTaskId();
    if (taskId <= 0) {
      //. Load task ID from URL
      long id = TaskUtil.getTaskIdFromURI(requestPath);
      if (id > 0) {
        taskId = id;
      }
    }

    Identity identity = ConversationState.getCurrent().getIdentity();
    TaskQuery taskQuery = new TaskQuery();

    if (project != null && !project.canView(identity)) {
      currProject = space_group_id == null ? ProjectUtil.INCOMING_PROJECT_ID : ProjectUtil.TODO_PROJECT_ID;
      errorMessage = bundle.getString("popup.msg.noPermissionToViewProject");
    }
    
    //
    if (taskId != -1) {
      try {
        taskModel = TaskUtil.getTaskModel(taskId, false, bundle, username, taskService,
                                                    orgService, userService, projectService);
        if (taskModel.getTask().getStatus() != null) {
          project = taskModel.getTask().getStatus().getProject();
          if (project.canView(identity)) {
            currProject = project.getId();
            taskQuery.setProjectIds(Arrays.asList(currProject));
          }
        }
        if (currProject <= 0) {
          if (taskModel.getAssignee() != null && username.equals(taskModel.getAssignee().getUsername())) {
            currProject = ProjectUtil.TODO_PROJECT_ID;
            taskQuery.setIsTodoOf(username);
          } else {
            currProject = ProjectUtil.INCOMING_PROJECT_ID;
            taskQuery.setIsIncomingOf(username);
          }
        }
      } catch (EntityNotFoundException e) {
        taskId = -1;
        errorMessage = bundle.getString("popup.msg.taskNotExist");
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

        taskQuery.setIsTodoOf(username);
        taskQuery.setProjectIds(spaceProjectIds);

      } else if (currProject > 0) {
        taskQuery.setProjectIds(Arrays.asList(currProject));

      } else if (currProject == ProjectUtil.TODO_PROJECT_ID) {
        taskQuery.setIsTodoOf(username);
      } else {
        taskQuery.setIsIncomingOf(username);
      }
    }

    ListAccess<Task> listTasks = taskService.findTasks(taskQuery);

    int page = 1;
    Paging paging = new Paging(page);

    // Find the page contains current task
    if (taskModel != null) {
      page = 0;
      boolean containTask = false;
      Task[] arr = new Task[0];
      do {
        page++;
        paging = new Paging(page);
        arr = ListUtil.load(listTasks, paging.getStart(), paging.getNumberItemPerPage());
        for (Task t : arr) {
          if (t.getId() == taskId) {
            containTask = true;
            break;
          }
        }
      } while (!containTask && arr.length > 0);

      if (!containTask) {
        paging = new Paging(1);
        taskModel = null;
        taskId = -1;
        errorMessage = bundle.getString("popup.msg.noPermissionToViewTask");
      }
    }
    paging.setTotal(ListUtil.getSize(listTasks));


    Map<GroupKey, List<Task>> groupTasks = new HashMap<GroupKey, List<Task>>();
    groupTasks.put(new GroupKey("", null, 0), Arrays.asList(ListUtil.load(listTasks, paging.getStart(), paging.getNumberItemPerPage())));

    UserSetting setting = userService.getUserSetting(username);

    // Count all incoming task
    long taskNum = paging.getTotal();
    long incomNum = taskNum;
    if (currProject != ProjectUtil.INCOMING_PROJECT_ID  && space_group_id == null) {
      TaskQuery q = new TaskQuery();
      q.setIsIncomingOf(username);
      incomNum = ListUtil.getSize(taskService.findTasks(q));
    }

    Map<String, String> defOrders = TaskUtil.getDefOrders(bundle);
    Map<String, String> defGroupBys = TaskUtil.getDefGroupBys(currProject, bundle);
    
    ListAccess<Label> tmp = taskService.findLabelsByUser(username);
    List<Label> labels = TaskUtil.buildRootLabels(Arrays.asList(ListUtil.load(tmp, 0, -1)));
    
    return index.with()
        .currentProjectId(currProject)
        .taskId(taskId)
        .taskModel(taskModel)
        .orders(defOrders)
        .groups(defGroupBys)
        .project(project)
        .tasks(tasks)
        .taskNum(taskNum)
        .incomNum(incomNum)
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
        .paging(paging)
        .errorMessage(errorMessage)
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

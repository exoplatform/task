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

import java.util.*;

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
import org.exoplatform.task.dao.OrderBy;
import org.exoplatform.task.dao.TaskQuery;
import org.exoplatform.task.domain.Label;
import org.exoplatform.task.domain.Project;
import org.exoplatform.task.domain.Status;
import org.exoplatform.task.domain.Task;
import org.exoplatform.task.domain.UserSetting;
import org.exoplatform.task.exception.EntityNotFoundException;
import org.exoplatform.task.management.model.Paging;
import org.exoplatform.task.management.model.ViewState;
import org.exoplatform.task.management.model.ViewType;
import org.exoplatform.task.management.service.ViewStateService;
import org.exoplatform.task.model.GroupKey;
import org.exoplatform.task.model.TaskModel;
import org.exoplatform.task.service.ParserContext;
import org.exoplatform.task.service.ProjectService;
import org.exoplatform.task.service.StatusService;
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

  public static final int MIN_NUMBER_TASK_GROUPABLE = 2;

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
  StatusService statusService;

  @Inject
  @Path("index.gtmpl")
  org.exoplatform.task.management.templates.index index;

  @Inject
  ResourceBundle bundle;
  
  @Inject
  ProjectController projectController;
  
  @Inject
  NavigationState navState;
  
  @Inject
  ViewStateService viewStateService;

  @View
  public Response index(String space_group_id, SecurityContext securityContext) throws EntityNotFoundException {
    String username = securityContext.getRemoteUser();
    PortalRequestContext prc = Util.getPortalRequestContext();
    String requestPath = prc.getControllerContext().getParameter(RequestNavigationData.REQUEST_PATH);

    Long currProject = ProjectUtil.INCOMING_PROJECT_ID;
    String filter = null;
    Long labelId = null;

    Object[] params = ProjectUtil.parsePermalinkURL(requestPath);
    if (params != null) {
      currProject = (Long) params[0];
      filter = (String) params[1];
      labelId = (Long) params[2];

      if (labelId != null) {
        currProject = ProjectUtil.LABEL_PROJECT_ID;
      }
    }

    String errorMessage = "";

    TaskModel taskModel = null;
    Project project = null;

    if (space_group_id == null) {
      // Load project ID from URL
      long projectId = ProjectUtil.getProjectIdFromURI(requestPath);
      if (projectId > -100 && prc.getControllerContext().getRequest().getQueryString() == null) {
        currProject = projectId;
      }
    } else {
      currProject = 0L;
    }

    if (currProject > 0) {
      try {
        project = projectService.getProject(currProject);
      } catch (EntityNotFoundException ex) {
        errorMessage = bundle.getString("popup.msg.projectNotExist");
      }
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
      currProject = space_group_id == null ? ProjectUtil.INCOMING_PROJECT_ID : 0L;
      errorMessage = bundle.getString("popup.msg.noPermissionToViewProject");
    }
    
    // A permalink of a task
    if (taskId > 0) {
      try {
        taskModel = TaskUtil.getTaskModel(taskId, false, bundle, username, taskService,
                                                    orgService, userService, projectService);
        if (!TaskUtil.hasViewPermission(taskModel.getTask())) {
          taskModel = null;
          taskId = -1;
          errorMessage = bundle.getString("popup.msg.noPermissionToViewTask");
        } else {
          if (taskModel.getTask().getStatus() != null) {
            project = taskModel.getTask().getStatus().getProject();
            if (project.canView(identity)) {
              currProject = project.getId();
              taskQuery.setProjectIds(Arrays.asList(currProject));
            }
          }
          if (currProject <= 0) {
            if (isAssignedTo(taskModel, username) ||
                    (project != null && TaskUtil.hasViewOnlyPermission(taskModel.getTask()))) {
              currProject = ProjectUtil.TODO_PROJECT_ID;
              taskQuery.setIsTodoOf(username);
            } else {
              currProject = ProjectUtil.INCOMING_PROJECT_ID;
              taskQuery.setIsIncomingOf(username);
            }
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
      spaceProjectIds = new LinkedList<>();
      for (Project p : projects) {
        spaceProjectIds.add(p.getId());
      }            
    }

    Map<String, String> defOrders = TaskUtil.getDefOrders(bundle);
    Map<String, String> defGroupBys = TaskUtil.getDefGroupBys(currProject, bundle);

    String listId = ViewState.buildId(currProject, (long) -1, "incoming");
    ViewState viewState = viewStateService.getViewState(listId);
    String orderBy = viewState.getOrderBy();
    if (orderBy == null) {
      orderBy = TaskUtil.CREATED_TIME;
    }
    taskQuery.setOrderBy(Arrays.asList(new OrderBy.DESC(orderBy)));

    String groupBy =  viewState.getGroupBy();
    if (groupBy == null) {
      groupBy = TaskUtil.NONE;
    }

    String currentUser = securityContext.getRemoteUser();
    TimeZone userTimezone = userService.getUserTimezone(currentUser);

    Label label = null;

    if (taskId <= 0) {
      if (space_group_id != null) {
        taskQuery.setProjectIds(spaceProjectIds);
      } else if (currProject > 0) {
        taskQuery.setProjectIds(Arrays.asList(currProject));
      } else if (currProject == ProjectUtil.TODO_PROJECT_ID) {
        taskQuery.setIsTodoOf(username);
        if (spaceProjectIds != null) {
          taskQuery.setProjectIds(spaceProjectIds);
        }

        if (filter != null && !filter.isEmpty()) {
          if ("overDue".equalsIgnoreCase(filter)) {
            defGroupBys = TaskUtil.resolve(Arrays.asList(TaskUtil.NONE, TaskUtil.PROJECT, TaskUtil.LABEL), bundle);
            defOrders = TaskUtil.resolve(Arrays.asList(TaskUtil.TITLE, TaskUtil.PRIORITY, TaskUtil.DUEDATE), bundle);
            groupBy = TaskUtil.PROJECT;
          } else if ("today".equalsIgnoreCase(filter) || "tomorrow".equalsIgnoreCase(filter)) {
            defGroupBys = TaskUtil.resolve(Arrays.asList(TaskUtil.NONE, TaskUtil.PROJECT, TaskUtil.LABEL), bundle);
            defOrders = TaskUtil.resolve(Arrays.asList(TaskUtil.TITLE, TaskUtil.PRIORITY, TaskUtil.RANK), bundle);
          } else if ("upcoming".equalsIgnoreCase(filter)) {
            defGroupBys = TaskUtil.resolve(Arrays.asList(TaskUtil.NONE, TaskUtil.PROJECT, TaskUtil.LABEL), bundle);
            defOrders = TaskUtil.resolve(Arrays.asList(TaskUtil.TITLE, TaskUtil.PRIORITY, TaskUtil.DUEDATE, TaskUtil.RANK), bundle);
          }

          Date[] filterDate = TaskUtil.convertDueDate(filter, userTimezone);
          taskQuery.setDueDateFrom(filterDate[0]);
          taskQuery.setDueDateTo(filterDate[1]);
        }
      } else if (currProject == ProjectUtil.LABEL_PROJECT_ID && labelId != null) {
        if (labelId > 0) {
          label = taskService.getLabel(labelId);
          if (label != null && label.getUsername().equals(username)) {
            taskQuery.setLabelIds(Arrays.asList(labelId));
          } else {
            // Rollback to incoming if this label is not belong to current user
            taskQuery.setIsIncomingOf(username);
            orderBy = TaskUtil.CREATED_BY;
          }
        } else {
          taskQuery.setIsLabelOf(username);
          groupBy = TaskUtil.LABEL;
        }
      } else {
        taskQuery.setIsIncomingOf(username);
      }
    }

    //
    ViewState.Filter fd = viewStateService.getFilter(listId);
    if (taskId > 0 && taskModel != null && taskModel.getTask() != null && taskModel.getTask().isCompleted()) {
      fd.setEnabled(true);
      fd.setShowCompleted(true);
    }

    boolean advanceSearch = fd.isEnabled();
    boolean showCompleted = false;
    String keyword = "";


    ListAccess<Task> listTasks = null;
    //there are cases that we return empty list of tasks with-out querying to DB
    //1. In spaces, and no space project
    if ((spaceProjectIds != null  && spaceProjectIds.isEmpty())) {
      listTasks = TaskUtil.EMPTY_TASK_LIST;
    } else {
      if (advanceSearch) {
        keyword = fd.getKeyword();
        showCompleted = fd.isShowCompleted();
        Status status = fd.getStatus() != null ? statusService.getStatus(fd.getStatus()) : null;
        //
        TaskUtil.buildTaskQuery(taskQuery, fd.getKeyword(), fd.getLabel(), status, fd.getDue(), fd.getPriority(), fd.getAssignees(), fd.isShowCompleted(), userTimezone);
      } else {
        taskQuery.setCompleted(false);
      }
      listTasks = taskService.findTasks(taskQuery);
    }

    int page = 1;
    Paging paging = new Paging(page);

    // Find the page contains current task
    if (taskModel != null) {
      page = 0;
      boolean containTask = false;
      Task[] arr = null;
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

    long countTasks = paging.getTotal();
    if (countTasks < MIN_NUMBER_TASK_GROUPABLE) {
      groupBy = TaskUtil.NONE;
    }

    Map<GroupKey, List<Task>> groupTasks = new HashMap<GroupKey, List<Task>>();
    if (countTasks >= MIN_NUMBER_TASK_GROUPABLE && groupBy != null && !groupBy.isEmpty() && !TaskUtil.NONE.equalsIgnoreCase(groupBy)) {
      groupTasks = TaskUtil.groupTasks(Arrays.asList(ListUtil.load(listTasks, paging.getStart(), paging.getNumberItemPerPage())), groupBy, currentUser, userTimezone, bundle, taskService, userService);
    }
    if (groupTasks.isEmpty()) {
      groupTasks.put(new GroupKey("", null, 0), Arrays.asList(ListUtil.load(listTasks, paging.getStart(), paging.getNumberItemPerPage())));
    }

    UserSetting setting = userService.getUserSetting(username);

    // Count all incoming task
    long taskNum = paging.getTotal();
    long incomNum = taskNum;
    if (advanceSearch || currProject != ProjectUtil.INCOMING_PROJECT_ID) {
      TaskQuery q = new TaskQuery();
      q.setIsIncomingOf(username);
      q.setCompleted(false);
      incomNum = ListUtil.getSize(taskService.findTasks(q));
    }

    ListAccess<Label> tmp = taskService.findLabelsByUser(username);
    List<Label> labels = TaskUtil.buildRootLabels(Arrays.asList(ListUtil.load(tmp, 0, -1)));

    List<Status> projectStatus = new ArrayList<Status>();
    Map<Long, Integer> numberTasks = new HashMap<Long, Integer>();
    ViewType viewType = viewState.getViewType();
    if (ViewType.BOARD == viewType && currProject > 0) {
      projectStatus = statusService.getStatuses(currProject);
      for(List<Task> list : groupTasks.values()) {
        for (Task task : list) {
          Status st = task.getStatus();
          int num = 0;
          if (numberTasks.containsKey(st.getId())) {
            num = numberTasks.get(st.getId());
          }
          num++;
          numberTasks.put(st.getId(), num);
        }
      }
    }

    return index.with()
        .currentProjectId(currProject)
        .taskId(taskId)
        .useCalendar(TaskUtil.isCalendarEnabled())
        .taskModel(taskModel)
        .orders(defOrders)
        .groups(defGroupBys)
        .project(project)
        .projectStatuses(projectStatus)
        .tasks(null)
        .taskNum(taskNum)
        .incomNum(incomNum)
        .groupTasks(groupTasks)
        .keyword(keyword)
        .showCompleted(advanceSearch && showCompleted)
        .advanceSearch(advanceSearch)
        .groupBy(groupBy)
        .orderBy(orderBy)
        .filter("")
        .projects(projects)
        .labels(labels)
        .userSetting(setting)
        .userTimezone(userService.getUserTimezone(username))
        .bundle(bundle)
        .isInSpace(space_group_id != null)
        .viewType(viewType)
        .currentLabelId(labelId == null ? -1 : labelId)
        .currentLabelName(label != null ? label.getName() : "")
        .taskService(taskService)
        .currentUser(username)
        .paging(paging)
        .errorMessage(errorMessage)
        .numberTasksByStatus(numberTasks)
        .ok().withCharset(Tools.UTF_8);
  }

  private List<Long> recursiveGetId(List<Project> projects) {
    List<Long> results = new ArrayList<>();
    if (projects != null) {
      for (Project p : projects) {
        results.add(p.getId());

        if (p.getChildren() != null) {
          results.addAll(recursiveGetId(p.getChildren()));
        }
      }
    }

    return results;
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

  private boolean isAssignedTo(TaskModel taskModel, String username) {
    if (username == null || username.isEmpty()) {
      return false;
    }
    if (taskModel.getAssignee() != null && username.equals(taskModel.getAssignee().getUsername())) {
      return true;
    }
    if (taskModel.getTask().getCoworker() != null) {
      return taskModel.getTask().getCoworker().contains(username);
    }

    return false;
  }  
}

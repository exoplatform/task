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

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.TimeZone;

import javax.inject.Inject;

import juzu.HttpMethod;
import juzu.MimeType;
import juzu.Path;
import juzu.Resource;
import juzu.Response;
import juzu.impl.common.Tools;
import juzu.request.SecurityContext;

import org.exoplatform.commons.juzu.ajax.Ajax;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.services.organization.OrganizationService;
import org.exoplatform.services.security.ConversationState;
import org.exoplatform.services.security.Identity;
import org.exoplatform.task.dao.OrderBy;
import org.exoplatform.task.dao.TaskQuery;
import org.exoplatform.task.domain.Comment;
import org.exoplatform.task.domain.Label;
import org.exoplatform.task.domain.Priority;
import org.exoplatform.task.domain.Project;
import org.exoplatform.task.domain.Status;
import org.exoplatform.task.domain.Task;
import org.exoplatform.task.domain.TaskLog;
import org.exoplatform.task.exception.AbstractEntityException;
import org.exoplatform.task.exception.LabelNotFoundException;
import org.exoplatform.task.exception.ProjectNotFoundException;
import org.exoplatform.task.exception.TaskNotFoundException;
import org.exoplatform.task.model.CommentModel;
import org.exoplatform.task.model.GroupKey;
import org.exoplatform.task.model.TaskModel;
import org.exoplatform.task.model.User;
import org.exoplatform.task.service.ParserContext;
import org.exoplatform.task.service.ProjectService;
import org.exoplatform.task.service.StatusService;
import org.exoplatform.task.service.TaskParser;
import org.exoplatform.task.service.TaskService;
import org.exoplatform.task.service.UserService;
import org.exoplatform.task.utils.CommentUtils;
import org.exoplatform.task.utils.DateUtil;
import org.exoplatform.task.utils.ProjectUtil;
import org.exoplatform.task.utils.TaskUtil;
import org.exoplatform.task.utils.TaskUtil.DUE;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * @author <a href="mailto:tuyennt@exoplatform.com">Tuyen Nguyen The</a>.
 */
public class TaskController {
  private static final Log LOG = ExoLogger.getExoLogger(TaskController.class);

  private static final List<String> VIEW_TYPES = Arrays.asList("list", "board");

  public static final int MIN_NUMBER_TASK_GROUPABLE = 2;

  @Inject
  TaskService taskService;

  @Inject
  ProjectService projectService;

  @Inject
  StatusService statusService;

  @Inject
  TaskParser taskParser;

  @Inject
  OrganizationService orgService;

  @Inject
  UserService userService;

  @Inject
  ResourceBundle bundle;

  @Inject
  @Path("detail.gtmpl")
  org.exoplatform.task.management.templates.detail detail;
  
  @Inject
  @Path("taskLogs.gtmpl")
  org.exoplatform.task.management.templates.taskLogs taskLogs;

  @Inject
  @Path("taskComments.gtmpl")
  org.exoplatform.task.management.templates.taskComments taskComments;

  @Inject
  @Path("comments.gtmpl")
  org.exoplatform.task.management.templates.comments comments;

  @Inject
  @Path("projectTaskListView.gtmpl")
  org.exoplatform.task.management.templates.projectTaskListView taskListView;

  @Resource
  @Ajax
  @MimeType.HTML
  public Response detail(Long id, SecurityContext securityContext) {

    try {

      TaskModel model = TaskUtil.getTaskModel(id, false, bundle, securityContext.getRemoteUser(), taskService, orgService, userService, projectService);
      TimeZone userTimezone = userService.getUserTimezone(securityContext.getRemoteUser());
      
      return detail.with()
          .taskModel(model)
          .userTimezone(userTimezone)
          .bundle(bundle)
          .ok().withCharset(Tools.UTF_8);

    } catch (AbstractEntityException e) {
      return Response.status(e.getHttpStatusCode()).body(e.getMessage());
    } catch (Exception ex) {// NOSONAR
      return Response.status(500).body(ex.getMessage());
    }
  }
  
  @Resource
  @Ajax
  @MimeType.HTML
  public Response renderTaskLogs(Long taskId, SecurityContext securityContext) throws TaskNotFoundException {
    Task task = taskService.getTaskById(taskId); //Can throw TaskNotFoundException
    
    List<TaskLog> logs = new LinkedList<TaskLog>(task.getTaskLogs());
    Collections.sort(logs);
    Map<String, User> userMap = new HashMap<String, User>();
    if (logs.size() > 0) {
      for (TaskLog log : logs) {
        String author = log.getAuthor();
        if (!userMap.containsKey(author)) {
          User user = userService.loadUser(log.getAuthor());
          userMap.put(author, user);
        }
      }
    }
    return taskLogs.with()
            .bundle(bundle)
            .taskLogs(logs)
            .userMap(userMap)
            .ok().withCharset(Tools.UTF_8);
  }

  @Resource
  @Ajax
  @MimeType.HTML
  public Response renderTaskComments(Long id, Boolean loadAllComment, SecurityContext securityContext) {
    try {

      if (loadAllComment == null) {
        loadAllComment = Boolean.FALSE;
      }
      TaskModel model = TaskUtil.getTaskModel(id, loadAllComment, bundle, securityContext.getRemoteUser(), taskService, orgService, userService, projectService);

      return taskComments.with()
              .taskModel(model)
              .bundle(bundle)
              .ok().withCharset(Tools.UTF_8);

    } catch (AbstractEntityException e) {
      return Response.status(e.getHttpStatusCode()).body(e.getMessage());
    } catch (Exception ex) {// NOSONAR
      return Response.status(500).body(ex.getMessage());
    }
  }

  @Resource
  @Ajax
  @MimeType.JSON
  public Response clone(Long id) {

    try {

      Task newTask = taskService.cloneTaskById(id); //Can throw TaskNotFoundException

      JSONObject json = new JSONObject();
      json.put("id", newTask.getId()); //Can throw JSONException
      return Response.ok(json.toString());

    } catch (AbstractEntityException e) {
      return Response.status(e.getHttpStatusCode()).body(e.getMessage());
    } catch (JSONException ex) {
      return Response.status(500).body(ex.getMessage());
    }
  }

  @Resource
  @Ajax
  @MimeType.JSON
  public Response delete(Long id) {

    try {

      taskService.deleteTaskById(id);//Can throw TaskNotFoundException

      JSONObject json = new JSONObject();
      json.put("id", id); //Can throw JSONException
      return Response.ok(json.toString());

    } catch (AbstractEntityException e) {
      return Response.status(e.getHttpStatusCode()).body(e.getMessage());
    } catch (JSONException ex) {
      return Response.status(500).body(ex.getMessage());
    }
  }

  @Resource
  @Ajax
  @MimeType("text/plain")
  public Response saveTaskInfo(Long taskId, String name, String[] value, SecurityContext context) {

    try {
      TimeZone timezone = userService.getUserTimezone(context.getRemoteUser());
      Task task = taskService.updateTaskInfo(taskId, name, value, timezone, context.getRemoteUser()); //Can throw TaskNotFoundException & ParameterEntityException & StatusNotFoundException

      String response = "Update successfully";
      if ("workPlan".equalsIgnoreCase(name)) {        
        Calendar start = DateUtil.newCalendarInstance(timezone);
        start.setTime(task.getStartDate());
        Calendar end = DateUtil.newCalendarInstance(timezone);
        end.setTime(task.getEndDate());
        
        response = TaskUtil.getWorkPlan(start, end, bundle);
        if (response == null) {
          response = bundle.getString("label.noWorkPlan");
        }
      }

      return Response.ok(response);

    } catch (AbstractEntityException e) {
      return Response.status(e.getHttpStatusCode()).body(e.getMessage());
    }
  }

  @Resource
  @Ajax
  @MimeType("text/plain")
  public Response saveTaskOrder(Long taskId, Long newStatusId, Long[] orders) {
    if (taskId == null || taskId == 0) {
      return Response.status(404).body("Task not found");
    }
    Status newStatus = null;
    if (newStatusId != null && newStatusId > 0) {
      newStatus = statusService.getStatusById(newStatusId);
    }
    long[] ids = new long[orders.length];
    for (int i = 0; i < ids.length; i++) {
      ids[i] = orders[i];
    }
    taskService.updateTaskOrder(taskId, newStatus, ids);
    return Response.ok("Update successfully");
  }

  @Resource
  @Ajax
  @MimeType("text/plain")
  public Response updateCompleted(Long taskId, Boolean completed) {

    try {

      taskService.updateTaskCompleted(taskId, completed); //Can throw TaskNotFoundException & ParameterEntityException
      return Response.ok("Update successfully");

    } catch (AbstractEntityException e) {
      return Response.status(e.getHttpStatusCode()).body(e.getMessage());
    }
  }

  @Resource
  @Ajax
  @MimeType.JSON
  public Response comment(Long taskId, String comment, SecurityContext securityContext) {

    String currentUser = securityContext.getRemoteUser();
    if (currentUser == null || currentUser.isEmpty()) {
      return Response.status(401);
    }

    try {

      Comment cmt = taskService.addCommentToTaskId(taskId, currentUser, comment); //Can throw TaskNotFoundException

      //TODO:
      CommentModel model = new CommentModel(cmt, userService.loadUser(cmt.getAuthor()), CommentUtils.formatMention(cmt.getComment(), userService));

      DateFormat df = new SimpleDateFormat("MMM dd, yyyy HH:mm");
      df.setTimeZone(userService.getUserTimezone(currentUser));

      JSONObject json = new JSONObject();
      json.put("id", model.getId()); //Can throw JSONException (same for all #json.put methods below)
      JSONObject user = new JSONObject();
      user.put("username", model.getAuthor().getUsername());
      user.put("displayName", model.getAuthor().getDisplayName());
      user.put("avatar", model.getAuthor().getAvatar());
      json.put("author", user);
      json.put("comment", model.getComment());
      json.put("formattedComment", model.getFormattedComment());
      json.put("createdTime", model.getCreatedTime().getTime());
      json.put("createdTimeString", df.format(model.getCreatedTime()));
      return Response.ok(json.toString()).withCharset(Tools.UTF_8);

    } catch (AbstractEntityException e) {
      return Response.status(e.getHttpStatusCode()).body(e.getMessage());
    }
    catch (JSONException ex) {
      return Response.status(500).body(ex.getMessage());
    }
  }

  @Resource
  @Ajax
  @MimeType.HTML
  public Response loadAllComments(Long taskId, SecurityContext securityContext) {

    try {

      List<Comment> cmts = taskService.getCommentsByTaskId(taskId, 0, -1); //Can throw TaskNotFoundException

      List<CommentModel> listComments = new ArrayList<CommentModel>(cmts.size());
      for(Comment cmt : cmts) {
        org.exoplatform.task.model.User u = userService.loadUser(cmt.getAuthor());
        listComments.add(new CommentModel(cmt, u, CommentUtils.formatMention(cmt.getComment(), userService)));
      }

      org.exoplatform.task.model.User currentUser = userService.loadUser(securityContext.getRemoteUser());

      return comments.with()
          .commentCount(cmts.size())
          .comments(listComments)
          .currentUser(currentUser)
          .ok()
          .withCharset(Tools.UTF_8);

    } catch (AbstractEntityException e) {
      return Response.status(e.getHttpStatusCode()).body(e.getMessage());
    }

  }

  @Resource
  @Ajax
  @MimeType("text/plain")
  public Response deleteComment(Long commentId) {

    try {

      taskService.deleteCommentById(commentId); //Can throw CommentNotFoundException
      return Response.ok("Delete comment successfully!");

    } catch (AbstractEntityException e) {
      return Response.status(e.getHttpStatusCode()).body(e.getMessage());
    }

  }

  @Resource
  @Ajax
  @MimeType.HTML
  public Response listTasks(String space_group_id, Long projectId, Long labelId, String filterLabelIds, String tags, Long statusId, String dueDate, String priority,  
                            String assignee, Boolean completed, String keyword, Boolean advanceSearch, String groupBy, String orderBy, String filter, String viewType, SecurityContext securityContext) {
    Project project = null;
    List<Task> tasks = Collections.emptyList();

    if (projectId <= 0 || viewType == null || !VIEW_TYPES.contains(viewType)) {
      viewType = VIEW_TYPES.get(0);
    }
    boolean isBoardView = viewType.equals(VIEW_TYPES.get(1));
    
    Map<String, String> defOrders;
    Map<String, String> defGroupBys;
    if (isBoardView) {
      defGroupBys = TaskUtil.resolve(Arrays.asList(TaskUtil.NONE, TaskUtil.LABEL, /*TaskUtil.DUEDATE,*/ TaskUtil.ASSIGNEE), bundle);
      defOrders = TaskUtil.resolve(Arrays.asList(TaskUtil.DUEDATE, TaskUtil.PRIORITY, TaskUtil.RANK), bundle);
      if (orderBy == null || !defOrders.containsKey(orderBy)) {
        orderBy = TaskUtil.DUEDATE;
      }
      if (groupBy == null || !defGroupBys.containsKey(groupBy)) {
        groupBy = TaskUtil.NONE;
      }
    } else {
      defGroupBys = TaskUtil.getDefGroupBys(projectId, bundle);
      defOrders = TaskUtil.getDefOrders(bundle);
    }

    String currentUser = securityContext.getRemoteUser();
    TimeZone userTimezone = userService.getUserTimezone(currentUser);

    if (currentUser == null || currentUser.isEmpty()) {
      return Response.status(401);
    }

    Identity currIdentity = ConversationState.getCurrent().getIdentity();
    List<Long> spaceProjectIds = null;    
    if (space_group_id != null) {
      spaceProjectIds = new LinkedList<Long>();
      List<Project> projects = ProjectUtil.flattenTree(ProjectUtil.getProjectTree(space_group_id, projectService));
      for (Project p : projects) {
        if (p.canView(currIdentity)) {
          spaceProjectIds.add(p.getId());          
        }
      }      
    }
    List<Long> allProjectIds = null;
    if (projectId == 0) {
      allProjectIds = new LinkedList<Long>();
      List<Project> projects = ProjectUtil.flattenTree(ProjectUtil.getProjectTree(null, projectService));
      for (Project p : projects) {
        if (p.canView(currIdentity)) {
          allProjectIds.add(p.getId());          
        }
      }
    }

    OrderBy order = null;
    if(orderBy != null && !orderBy.trim().isEmpty()) {
      order = TaskUtil.TITLE.equals(orderBy) || TaskUtil.DUEDATE.equals(orderBy) ? new OrderBy.ASC(orderBy) : new OrderBy.DESC(orderBy);
    }
    
    TaskQuery advanceQuery = advanceSearch != null && advanceSearch ? buildTaskQuery(keyword, filterLabelIds, tags, statusId, dueDate, priority, assignee, completed, order, userTimezone) : null;

    //Get Tasks in good order
    if(projectId == ProjectUtil.INCOMING_PROJECT_ID) {
      //. Default order by CreatedDate
      if (orderBy == null || orderBy.isEmpty()) {
        orderBy = TaskUtil.CREATED_TIME;
        order = new OrderBy.DESC(orderBy);
      }

      if (advanceQuery == null) {
        tasks = taskService.getIncomingTasksByUser(currentUser, order);
      } else {
        advanceQuery.setStatusId(null);
        advanceQuery.setAssignee(currentUser);
        advanceQuery.setCoworker(currentUser);
        advanceQuery.setCreatedBy(currentUser);
        advanceQuery.setOrFields(Arrays.asList(TaskUtil.ASSIGNEE, TaskUtil.COWORKER, TaskUtil.CREATED_BY));
        advanceQuery.setOrderBy(Arrays.asList(order));
        tasks = taskService.findTaskByQuery(advanceQuery);
      }
    } else if (projectId == ProjectUtil.TODO_PROJECT_ID) {
      defGroupBys = TaskUtil.resolve(Arrays.asList(TaskUtil.NONE, TaskUtil.PROJECT, TaskUtil.LABEL, TaskUtil.DUEDATE), bundle);
      defOrders = TaskUtil.resolve(Arrays.asList(TaskUtil.TITLE, TaskUtil.STATUS, TaskUtil.DUEDATE, TaskUtil.PRIORITY, TaskUtil.RANK), bundle);
      
      //TODO: process fiter here
      Date fromDueDate = null;
      Date toDueDate = null;

      if ("overDue".equalsIgnoreCase(filter)) {
        fromDueDate = null;
        toDueDate = new Date();
        
        defGroupBys = TaskUtil.resolve(Arrays.asList(TaskUtil.NONE, TaskUtil.PROJECT, TaskUtil.LABEL), bundle);
        defOrders = TaskUtil.resolve(Arrays.asList(TaskUtil.TITLE, TaskUtil.PRIORITY, TaskUtil.DUEDATE), bundle);
        groupBy = groupBy == null || !defGroupBys.containsKey(groupBy) ? TaskUtil.PROJECT : groupBy;
        dueDate = DUE.OVERDUE.name();
      } else if ("today".equalsIgnoreCase(filter)) {
        Calendar c = DateUtil.newCalendarInstance(userTimezone);
        c.set(Calendar.HOUR, 0);
        c.set(Calendar.MINUTE, 0);
        c.set(Calendar.SECOND, 0);
        fromDueDate = c.getTime();
        c.add(Calendar.HOUR, 24);
        toDueDate = c.getTime();
        
        defGroupBys = TaskUtil.resolve(Arrays.asList(TaskUtil.NONE, TaskUtil.PROJECT, TaskUtil.LABEL), bundle);
        defOrders = TaskUtil.resolve(Arrays.asList(TaskUtil.TITLE, TaskUtil.PRIORITY, TaskUtil.RANK), bundle);
        if (orderBy == null) {
          order = new OrderBy.DESC(TaskUtil.PRIORITY);
          orderBy = TaskUtil.PRIORITY;
        }
        groupBy = groupBy == null || !defGroupBys.containsKey(groupBy) ? TaskUtil.NONE : groupBy;
        dueDate = DUE.TODAY.name();
      } else if ("tomorrow".equalsIgnoreCase(filter)) {
        Calendar c = DateUtil.newCalendarInstance(userTimezone);
        c.set(Calendar.HOUR, 0);
        c.set(Calendar.MINUTE, 0);
        c.set(Calendar.SECOND, 0);
        c.add(Calendar.HOUR, 24);
        fromDueDate = c.getTime();
        c.add(Calendar.HOUR, 24);
        toDueDate = c.getTime();
        
        defGroupBys = TaskUtil.resolve(Arrays.asList(TaskUtil.NONE, TaskUtil.PROJECT, TaskUtil.LABEL), bundle);
        defOrders = TaskUtil.resolve(Arrays.asList(TaskUtil.TITLE, TaskUtil.PRIORITY, TaskUtil.RANK), bundle);
        if (orderBy == null || !defOrders.containsKey(orderBy)) {
          order = new OrderBy.DESC(TaskUtil.PRIORITY);
          orderBy = TaskUtil.PRIORITY;
        }
        groupBy = groupBy == null || !defGroupBys.containsKey(groupBy) ? TaskUtil.NONE : groupBy;
        dueDate = DUE.TOMORROW.name();
      } else if ("upcoming".equalsIgnoreCase(filter)) {
        Calendar c = DateUtil.newCalendarInstance(userTimezone);
        c.set(Calendar.HOUR, 0);
        c.set(Calendar.MINUTE, 0);
        c.set(Calendar.SECOND, 0);
        c.add(Calendar.DATE, 2);
        fromDueDate = c.getTime();
        toDueDate = null;

        defGroupBys = TaskUtil.resolve(Arrays.asList(TaskUtil.NONE, TaskUtil.PROJECT, TaskUtil.LABEL), bundle);
        defOrders = TaskUtil.resolve(Arrays.asList(TaskUtil.TITLE, TaskUtil.PRIORITY, TaskUtil.DUEDATE, TaskUtil.RANK), bundle);
        groupBy = groupBy == null || !defGroupBys.containsKey(groupBy) ? TaskUtil.NONE : groupBy;
        dueDate = DUE.UPCOMING.name();
      }

      if (orderBy == null || !defOrders.containsKey(orderBy)) {
        orderBy = TaskUtil.DUEDATE;
        order = new OrderBy.ASC(TaskUtil.DUEDATE);
      }
      if (groupBy == null || !defGroupBys.containsKey(groupBy)) {
        groupBy = TaskUtil.DUEDATE;
      }
      
      if (advanceSearch == null) {
        tasks = taskService.getToDoTasksByUser(currentUser, spaceProjectIds, order, fromDueDate, toDueDate);        
      } else {
        advanceQuery.setOrderBy(Arrays.asList(order));
        Long[] due = convertDueDate(dueDate, userTimezone);
        advanceQuery.setDueDateFrom(due[0]);
        advanceQuery.setDueDateTo(due[1]);
        advanceQuery.setProjectIds(spaceProjectIds);
        advanceQuery.setAssignee(currentUser);
        advanceQuery.setStatusId(-1L);
        tasks = taskService.findTaskByQuery(advanceQuery);
      }
    } else if (projectId >= 0) {
      if (projectId == 0) {
        defGroupBys = TaskUtil.resolve(Arrays.asList(TaskUtil.NONE, TaskUtil.ASSIGNEE, TaskUtil.PROJECT, TaskUtil.LABEL, TaskUtil.STATUS), bundle);

        //. Default order by CreatedDate
        if (orderBy == null || orderBy.isEmpty()) {
          orderBy = TaskUtil.DUEDATE;
          order = new OrderBy.ASC(orderBy);
        }

        if (advanceQuery == null) {
          if (spaceProjectIds != null) {
            tasks = projectService.getTasksWithKeywordByProjectId(spaceProjectIds, order, keyword);                  
          } else {          
            tasks = projectService.getTasksWithKeywordByProjectId(allProjectIds, order, keyword);
          }          
        } else {
          advanceQuery.setOrderBy(Arrays.asList(order));
          if (spaceProjectIds != null) {
            advanceQuery.setProjectIds(spaceProjectIds);            
          } else {
            advanceQuery.setProjectIds(allProjectIds);
          }
          advanceQuery.setStatusId(-1L);
          tasks = taskService.findTaskByQuery(advanceQuery);
        }
      } else {
        //. Default order by CreatedDate
        if (orderBy == null || orderBy.isEmpty()) {
          orderBy = TaskUtil.DUEDATE;
          order = new OrderBy.ASC(orderBy);
        }

        if (advanceQuery == null) {
          tasks = projectService.getTasksWithKeywordByProjectId(Arrays.asList(projectId), order, keyword);          
        } else {
          advanceQuery.setOrderBy(Arrays.asList(order));
          advanceQuery.setProjectIds(Arrays.asList(projectId));
          tasks = taskService.findTaskByQuery(advanceQuery);
        }
      }
      if (projectId > 0) {
        try {
          project = projectService.getProjectById(projectId);
        } catch (ProjectNotFoundException e) {
          return Response.notFound("not found project " + projectId);
        }          
      }
    } else if (labelId != null && labelId >= 0) {
      defOrders = TaskUtil.resolve(Arrays.asList(TaskUtil.TITLE, TaskUtil.CREATED_TIME, TaskUtil.DUEDATE, TaskUtil.PRIORITY), bundle);
      if (orderBy == null || orderBy.isEmpty() || !defOrders.containsKey(orderBy)) {
        orderBy = TaskUtil.DUEDATE;
        order = new OrderBy.ASC(TaskUtil.DUEDATE);
      }

      if (labelId > 0) {
        defGroupBys = TaskUtil.resolve(Arrays.asList(TaskUtil.NONE, TaskUtil.PROJECT, TaskUtil.DUEDATE, TaskUtil.STATUS), bundle);
        if (groupBy == null || groupBy.isEmpty() || !defGroupBys.containsKey(groupBy)) {
          groupBy = TaskUtil.NONE;
        }        
      } else {
        defGroupBys = TaskUtil.resolve(Arrays.asList(TaskUtil.NONE, TaskUtil.PROJECT, TaskUtil.LABEL, TaskUtil.DUEDATE, TaskUtil.STATUS), bundle);
        if (groupBy == null || groupBy.isEmpty() || !defGroupBys.containsKey(groupBy)) {
          groupBy = TaskUtil.LABEL;
        }
      }
      
      if (advanceQuery == null) {
        try {
          tasks = taskService.findTasksByLabel(labelId, spaceProjectIds, currentUser, order);
        } catch (LabelNotFoundException ex) {
          tasks = new ArrayList<Task>();
        }
      } else {
        advanceQuery.setProjectIds(spaceProjectIds);
        advanceQuery.setOrderBy(Arrays.asList(order));
        advanceQuery.setStatusId(-1L);
        tasks = taskService.findTaskByQuery(advanceQuery);
      }
    }

    if (tasks.size() < MIN_NUMBER_TASK_GROUPABLE) {
      //. We do not have enough tasks for grouping, so, set groupBy to empty
      groupBy = "";
    }

    // Count task by status
    Map<Long, Integer> numberTasks = new HashMap<Long, Integer>();
    if (isBoardView) {
      for(Task task : tasks) {
        Status st = task.getStatus();
        int num = 0;
        if (numberTasks.containsKey(st.getId())) {
          num = numberTasks.get(st.getId());
        }
        num++;
        numberTasks.put(st.getId(), num);
      }
    }

    //Group Tasks
    Map<String, org.exoplatform.task.model.User> userMap = null;
    Map<GroupKey, List<Task>> groupTasks = new HashMap<GroupKey, List<Task>>();
    if(groupBy != null && !groupBy.isEmpty()) {
      TimeZone tz = userService.getUserTimezone(currentUser);
      groupTasks = TaskUtil.groupTasks(tasks, groupBy, currentUser, tz, bundle, taskService);
      if("assignee".equalsIgnoreCase(groupBy)) {
        userMap = new HashMap<String, org.exoplatform.task.model.User>();
        for(GroupKey key : groupTasks.keySet()) {
          String asn = (String)key.getValue();
          org.exoplatform.task.model.User user = userService.loadUser(asn);
          userMap.put(asn, user);
        }
      }
    }
    if(groupTasks.isEmpty()) {
      groupTasks.put(new GroupKey("", null, 0), tasks);
    }

    long taskNum = -1;
    if (advanceQuery == null) {
      if (allProjectIds != null) {
        taskNum = TaskUtil.getTaskNum(currentUser, allProjectIds, projectId, taskService);
      } else if (labelId != null && labelId >= 0) {
        taskNum = tasks.size();
      } else {
        taskNum = TaskUtil.getTaskNum(currentUser, spaceProjectIds, projectId, taskService);
      }      
    }

    String currentLabelName = "";
    if (advanceQuery == null) {
      if (labelId != null && labelId > 0) {
        Label label = taskService.getLabelById(labelId);
        if (label != null) {
          currentLabelName = label.getName();
        }
      }      
    }

    return taskListView
        .with()
        .orders(defOrders)
        .groups(defGroupBys)
        .currentProjectId(projectId)
        .project(project)
        .tasks(tasks)
        .taskNum(taskNum)
        .groupTasks(groupTasks)
        .keyword(keyword == null ? "" : keyword)
        .groupBy(groupBy == null ? TaskUtil.NONE : groupBy)
        .orderBy(orderBy == null ? "" : orderBy)
        .filter(filter == null ? "" : filter)
        .advanceSearch(advanceSearch == null ? false : advanceSearch)
        .bundle(bundle)
        .viewType(viewType)
        .userTimezone(userTimezone)
        .taskService(taskService)
        .currentUser(currentUser)
        .currentLabelId(labelId == null ? -1 : labelId)
        .currentLabelName(currentLabelName)
        .set("userMap", userMap)
        .set("numberTasksByStatus", numberTasks)
        .ok()
        .withCharset(Tools.UTF_8);
  }


  private Long[] convertDueDate(String dueDate, TimeZone timezone) {
    Long[] due = new Long[] {null, null};
    
    if (dueDate != null) {
      Calendar today = Calendar.getInstance(timezone);
      today.set(Calendar.HOUR_OF_DAY, 0);
      today.set(Calendar.MINUTE, 0);
      today.set(Calendar.SECOND, 0);
      today.set(Calendar.MILLISECOND, 0);

      switch (DUE.valueOf(dueDate)) {
      case OVERDUE:
        today.roll(Calendar.DATE, -1);
        due[1] = today.getTimeInMillis();
        break;
      case TODAY:
        due[0] = today.getTimeInMillis();
        today.roll(Calendar.DATE, 1);
        due[1] = today.getTimeInMillis() - 1;
        break;
      case TOMORROW:
        today.roll(Calendar.DATE, 1);
        due[0] = today.getTimeInMillis();
        today.roll(Calendar.DATE, 1);
        due[1] = today.getTimeInMillis() - 1;
        break;
      case UPCOMING:
        today.roll(Calendar.DATE, 2);
        due[0] = today.getTimeInMillis();
      }       
    }
    return due;    
  }

  @Resource(method = HttpMethod.POST)
  @Ajax
  @MimeType.JSON
  public Response createTask(Long projectId, Long labelId, String taskInput, String filter, SecurityContext securityContext) {

    if(taskInput == null || taskInput.isEmpty()) {
      return Response.content(406, "Task input must not be null or empty");
    }

    String currentUser = securityContext.getRemoteUser();
    if (currentUser == null || currentUser.isEmpty()) {
      return Response.status(401);
    }

    ParserContext context = new ParserContext(userService.getUserTimezone(currentUser));
    Task task = taskParser.parse(taskInput, context);
    task.setCreatedBy(currentUser);

    //Project task
    if(projectId > 0) {
      try {
        projectService.createTaskToProjectId(projectId, task);
      } catch (AbstractEntityException e) {
        return Response.status(e.getHttpStatusCode()).body(e.getMessage());
      }
    } else if (labelId != null && labelId > 0){
      Label label = taskService.getLabelById(labelId);
      Set<Label> labels = new HashSet<Label>();
      labels.add(label);
      task.setLabels(labels);
      label.getTasks().add(task);
      taskService.createTask(task);
    }
    else {
      task.setAssignee(currentUser);
      
      TimeZone userTimezone = userService.getUserTimezone(currentUser);
      Calendar dueDate =  DateUtil.newCalendarInstance(userTimezone);
      if ("tomorrow".equalsIgnoreCase(filter)) {
        dueDate.add(Calendar.DATE, 1);
      } else if ("upcoming".equalsIgnoreCase(filter)) {
        dueDate.add(Calendar.DATE, 7);
      } else if (!"today".equalsIgnoreCase(filter)) {
        dueDate = null;
      }
      if (dueDate != null) {
        task.setDueDate(dueDate.getTime());
      }
      
      taskService.createTask(task);
    }

    long taskNum = -1;
    if (projectId == -1) {
      //incomming
      taskNum = TaskUtil.getTaskNum(currentUser, null, projectId, taskService);
    }

    try {
      JSONObject json = new JSONObject();
      json.put("id", task.getId());
      json.put("taskNum", taskNum);
      
      return Response.ok(json.toString());
    } catch (JSONException ex) {
      return Response.status(500).body("JSONException: " + ex);
    }
  }

  @Resource
  @Ajax
  @MimeType.HTML
  public Response createTaskInListView(String space_group_id, String taskTitle, Long projectId, Long statusId, String assignee,
                                       String viewType, String groupBy, String orderBy, SecurityContext securityContext) {
    if (taskTitle == null || taskTitle.isEmpty()) {
      return Response.status(406).body("Task title is required");
    }
    Task task = new Task();
    task.setTitle(taskTitle);
    if (assignee != null && !assignee.isEmpty()) {
      task.setAssignee(assignee);
    }
    if (statusId != null) {
      Status status = statusService.getStatusById(statusId);
      if (status != null) {
        task.setStatus(status);
      }
    } else if (projectId != null) {
      Status status = statusService.findLowestRankStatusByProject(projectId);
      if (status != null) {
        task.setStatus(status);
      }
    }

    taskService.createTask(task);

    //spaceGrpId, projectId, currentLabelId, labelIds, tags, statusId, dueDate, priority, assignee, completed, keyword, advanceSearch, groupby, orderBy, filter, viewType, securityContext
    return listTasks(space_group_id, projectId, null, null, null, null, null, null, null, null, null, null, groupBy, orderBy, null, viewType, securityContext);
  }

  @Resource(method = HttpMethod.POST)
  @Ajax
  @MimeType.HTML
  public Response removeStatus(String space_group_id, Long statusId, SecurityContext securityContext) {
    try {
      Status status = statusService.getStatusById(statusId);
      Project project = status.getProject();
      if (project.getStatus().size() > 1) {
        statusService.deleteStatus(statusId);
        //spaceGrpId, projectId, currentLabelId, labelIds, tags, statusId, dueDate, priority, assignee, completed, keyword, advanceSearch, groupby, orderBy, filter, viewType, securityContext
        return listTasks(space_group_id, project.getId(), null, null, null, null, null, null, null, null, null, null, null, null, null, "board", securityContext);
      } else {
        return Response.error("Can't delete last status");
      }
    } catch (AbstractEntityException e) {
      return Response.status(e.getHttpStatusCode()).body(e.getMessage());
    }
  }

  @Resource(method = HttpMethod.POST)
  @Ajax
  @MimeType.HTML
  public Response createStatus(String space_group_id, String name, Long projectId, SecurityContext securityContext) {
    try {
      Project project = projectService.getProjectById(projectId);
      Status status = statusService.createStatus(project, name);
      //spaceGrpId, projectId, currentLabelId, labelIds, tags, statusId, dueDate, priority, assignee, completed, keyword, advanceSearch, groupby, orderBy, filter, viewType, securityContext
      return listTasks(space_group_id, projectId, null, null, null, null, null, null, null, null, null, null, null, null, null, "board", securityContext);
    } catch (AbstractEntityException e) {
      return Response.status(e.getHttpStatusCode()).body(e.getMessage());
    }
  }
  
  private TaskQuery buildTaskQuery(String keyword,
                                   String labelIds,
                                   String tags,
                                   Long statusId,
                                   String dueDate,
                                   String priority,
                                   String assignee,
                                   Boolean completed, OrderBy order, TimeZone timezone) {
    TaskQuery query = new TaskQuery();
    query.setKeyword(keyword);
    if (labelIds != null) {
      List<Long> tmp = new LinkedList<Long>();
      for (String id : labelIds.split(",")) {
        if (!(id = id.trim()).isEmpty()) {
          tmp.add(Long.parseLong(id));          
        }
      }
      query.setLabelIds(tmp);      
    }
    if (tags != null) {
      List<String> tmp = new LinkedList<String>();
      for (String t : tags.split(",")) {
        if (!(t = t.trim()).isEmpty()) {
          tmp.add(t);
        }
      }
      query.setTags(tmp);                
    }
    query.setStatusId(statusId == null ? -1L : statusId);
    Long[] due = convertDueDate(dueDate, timezone);
    query.setDueDateFrom(due[0]);
    query.setDueDateTo(due[1]);
    if (priority != null) {
      query.setPriority(Priority.valueOf(priority));      
    }
    query.setAssignee(assignee);
    query.setCompleted(completed);
    if (order != null) {
      query.setOrderBy(Arrays.asList(order));      
    }
    
    return query;
  }
}

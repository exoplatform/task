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
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;
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
import org.exoplatform.task.dao.OrderBy;
import org.exoplatform.task.domain.Comment;
import org.exoplatform.task.domain.Project;
import org.exoplatform.task.domain.Task;
import org.exoplatform.task.domain.TaskLog;
import org.exoplatform.task.exception.AbstractEntityException;
import org.exoplatform.task.exception.ProjectNotFoundException;
import org.exoplatform.task.exception.TaskNotFoundException;
import org.exoplatform.task.model.CommentModel;
import org.exoplatform.task.model.TaskModel;
import org.exoplatform.task.model.User;
import org.exoplatform.task.service.ProjectService;
import org.exoplatform.task.service.TaskParser;
import org.exoplatform.task.service.TaskService;
import org.exoplatform.task.service.UserService;
import org.exoplatform.task.utils.CommentUtils;
import org.exoplatform.task.utils.ProjectUtil;
import org.exoplatform.task.utils.TaskUtil;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * @author <a href="mailto:tuyennt@exoplatform.com">Tuyen Nguyen The</a>.
 */
public class TaskController {
  private static final Log LOG = ExoLogger.getExoLogger(TaskController.class);

  @Inject
  TaskService taskService;

  @Inject
  ProjectService projectService;

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

      return detail.with()
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
  public Response saveTaskInfo(Long taskId, String name, String[] value) {

    try {

      Task task = taskService.updateTaskInfo(taskId, name, value); //Can throw TaskNotFoundException & ParameterEntityException & StatusNotFoundException

      String response = "Update successfully";
      if ("workPlan".equalsIgnoreCase(name)) {
        response = TaskUtil.getWorkPlan(task.getStartDate(), task.getDuration(), bundle);
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
  public Response listTasks(String space_group_id, Long projectId, String keyword, String groupBy, String orderBy, String filter,
                            SecurityContext securityContext) {
    Project project = null;
    List<Task> tasks;
    
    Map<String, String> defOrders = TaskUtil.getDefOrders(bundle);
    Map<String, String> defGroupBys = TaskUtil.getDefGroupBys(projectId, bundle);

    String currentUser = securityContext.getRemoteUser();
    if (currentUser == null || currentUser.isEmpty()) {
      return Response.status(401);
    }    
    List<Long> spaceProjectIds = null;    
    if (space_group_id != null) {
      spaceProjectIds = new LinkedList<Long>();
      List<Project> projects = ProjectUtil.getProjectTree(space_group_id, projectService);
      for (Project p : projects) {
        spaceProjectIds.add(p.getId());
      }      
    }
    List<Long> allProjectIds = null;
    if (projectId == 0) {
      allProjectIds = new LinkedList<Long>();
      List<Project> projects = ProjectUtil.getProjectTree(null, projectService);
      for (Project p : projects) {
        allProjectIds.add(p.getId());
      }
    }

    OrderBy order = null;
    if(orderBy != null && !orderBy.trim().isEmpty()) {
      order = "title".equals(orderBy) ? new OrderBy.ASC(orderBy) : new OrderBy.DESC(orderBy);
    }

    //Get Tasks in good order
    if(projectId == ProjectUtil.INCOMING_PROJECT_ID) {
      tasks = taskService.getIncomingTasksByUser(currentUser, order);
    }
    else if (projectId == ProjectUtil.TODO_PROJECT_ID) {
      defGroupBys = TaskUtil.resolve(Arrays.asList(TaskUtil.NONE, TaskUtil.PROJECT, TaskUtil.DUEDATE), bundle);
      defOrders = TaskUtil.resolve(Arrays.asList(TaskUtil.TITLE, TaskUtil.STATUS, TaskUtil.DUEDATE, TaskUtil.PRIORITY, TaskUtil.RANK), bundle);
      
      //TODO: process fiter here
      Date fromDueDate = null;
      Date toDueDate = null;

      if ("overDue".equalsIgnoreCase(filter)) {
        fromDueDate = null;
        toDueDate = new Date();
        
        defGroupBys = TaskUtil.resolve(Arrays.asList(TaskUtil.NONE, TaskUtil.PROJECT), bundle);
        defOrders = TaskUtil.resolve(Arrays.asList(TaskUtil.TITLE, TaskUtil.PRIORITY, TaskUtil.DUEDATE), bundle);
        groupBy = groupBy == null || defGroupBys.containsKey(groupBy) ? TaskUtil.PROJECT : groupBy;
      } else if ("today".equalsIgnoreCase(filter)) {
        Calendar c = Calendar.getInstance();
        c.set(Calendar.HOUR, 0);
        c.set(Calendar.MINUTE, 0);
        c.set(Calendar.SECOND, 0);
        fromDueDate = c.getTime();
        c.add(Calendar.HOUR, 24);
        toDueDate = c.getTime();
        
        defGroupBys = TaskUtil.resolve(Arrays.asList(TaskUtil.NONE, TaskUtil.PROJECT), bundle);
        defOrders = TaskUtil.resolve(Arrays.asList(TaskUtil.TITLE, TaskUtil.PRIORITY, TaskUtil.RANK), bundle);
        if (orderBy == null || !defOrders.containsKey(orderBy)) {
          order = new OrderBy.DESC(TaskUtil.PRIORITY);
        }
        groupBy = groupBy == null || !defGroupBys.containsKey(groupBy) ? TaskUtil.NONE : groupBy;
      } else if ("tomorrow".equalsIgnoreCase(filter)) {
        Calendar c = Calendar.getInstance();
        c.set(Calendar.HOUR, 0);
        c.set(Calendar.MINUTE, 0);
        c.set(Calendar.SECOND, 0);
        c.add(Calendar.HOUR, 24);
        fromDueDate = c.getTime();
        c.add(Calendar.HOUR, 24);
        toDueDate = c.getTime();
        
        defGroupBys = TaskUtil.resolve(Arrays.asList(TaskUtil.NONE, TaskUtil.PROJECT), bundle);
        defOrders = TaskUtil.resolve(Arrays.asList(TaskUtil.TITLE, TaskUtil.PRIORITY, TaskUtil.RANK), bundle);
        if (orderBy == null || !defOrders.containsKey(orderBy)) {
          order = new OrderBy.DESC(TaskUtil.PRIORITY);        
        }
        groupBy = groupBy == null || !defGroupBys.containsKey(groupBy) ? TaskUtil.NONE : groupBy;
      } else if ("upcoming".equalsIgnoreCase(filter)) {
        Calendar c = Calendar.getInstance();
        c.set(Calendar.HOUR, 0);
        c.set(Calendar.MINUTE, 0);
        c.set(Calendar.SECOND, 0);
        c.add(Calendar.DATE, 2);
        fromDueDate = c.getTime();
        toDueDate = null;
        
        defGroupBys = TaskUtil.resolve(Arrays.asList(TaskUtil.NONE, TaskUtil.PROJECT), bundle);
        defOrders = TaskUtil.resolve(Arrays.asList(TaskUtil.TITLE, TaskUtil.PRIORITY, TaskUtil.DUEDATE, TaskUtil.RANK), bundle);
        groupBy = groupBy == null || !defGroupBys.containsKey(groupBy) ? TaskUtil.NONE : groupBy;
      }
      
      if (orderBy == null || !defOrders.containsKey(orderBy)) {
        order = new OrderBy.ASC(TaskUtil.DUEDATE);        
      }
      groupBy = groupBy == null || !defGroupBys.containsKey(groupBy) ? TaskUtil.DUEDATE : groupBy;
      
      tasks = taskService.getToDoTasksByUser(currentUser, spaceProjectIds, order, fromDueDate, toDueDate);
    }
    else {
      if (projectId == 0) {
        if (spaceProjectIds != null) {
          tasks = projectService.getTasksWithKeywordByProjectId(spaceProjectIds, order, keyword);                  
        } else {          
          tasks = projectService.getTasksWithKeywordByProjectId(allProjectIds, order, keyword);
        }
      } else {
        tasks = projectService.getTasksWithKeywordByProjectId(Arrays.asList(projectId), order, keyword);
      }
      if (projectId > 0) {
        try {
          project = projectService.getProjectById(projectId);
        } catch (ProjectNotFoundException e) {
          return Response.notFound("not found project " + projectId);
        }          
      }
    }

    //Group Tasks
    Map<String, org.exoplatform.task.model.User> userMap = null;
    Map<String, List<Task>> groupTasks = new HashMap<String, List<Task>>();
    if(groupBy != null && !groupBy.isEmpty()) {
      TimeZone tz = userService.getUserTimezone(currentUser);
      groupTasks = TaskUtil.groupTasks(tasks, groupBy, tz, bundle);
      if("assignee".equalsIgnoreCase(groupBy)) {
        userMap = new HashMap<String, org.exoplatform.task.model.User>();
        for(String assignee : groupTasks.keySet()) {
          org.exoplatform.task.model.User user = userService.loadUser(assignee);
          userMap.put(assignee, user);
        }
      }
    }
    if(groupTasks.isEmpty()) {
      groupTasks.put("", tasks);
    }
    
    long taskNum = 0;
    if (allProjectIds != null) {
      taskNum = TaskUtil.getTaskNum(currentUser, allProjectIds, projectId, taskService);
    } else {
      taskNum = TaskUtil.getTaskNum(currentUser, spaceProjectIds, projectId, taskService);
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
        .bundle(bundle)
        .set("userMap", userMap)
        .ok()
        .withCharset(Tools.UTF_8);
  }

  @Resource(method = HttpMethod.POST)
  @Ajax
  @MimeType.JSON
  public Response createTask(String space_group_id, Long projectId, String taskInput, SecurityContext securityContext) {

    if(taskInput == null || taskInput.isEmpty()) {
      return Response.content(406, "Task input must not be null or empty");
    }

    String currentUser = securityContext.getRemoteUser();
    if (currentUser == null || currentUser.isEmpty()) {
      return Response.status(401);
    }

    Task task = taskParser.parse(taskInput);
    task.setCreatedBy(currentUser);

    //Project task
    if(projectId > 0) {
      try {
        projectService.createTaskToProjectId(projectId, task);
      } catch (AbstractEntityException e) {
        return Response.status(e.getHttpStatusCode()).body(e.getMessage());
      }
    }
    //Incoming Task
    else {
      taskService.createTask(task);
    }

    try {
      JSONObject json = new JSONObject();
      json.put("id", task.getId());
      return Response.ok(json.toString());
    } catch (JSONException ex) {
      return Response.status(500).body("JSONException: " + ex);
    }
  }

}

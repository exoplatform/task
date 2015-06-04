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

import juzu.*;
import juzu.impl.common.Tools;
import juzu.request.SecurityContext;
import org.exoplatform.commons.juzu.ajax.Ajax;
import org.exoplatform.services.organization.OrganizationService;
import org.exoplatform.services.organization.User;
import org.exoplatform.task.dao.OrderBy;
import org.exoplatform.task.domain.Comment;
import org.exoplatform.task.domain.Project;
import org.exoplatform.task.domain.Task;
import org.exoplatform.task.management.model.CommentModel;
import org.exoplatform.task.utils.CommentUtils;
import org.exoplatform.task.utils.UserUtils;
import org.exoplatform.task.service.ProjectService;
import org.exoplatform.task.service.TaskParser;
import org.exoplatform.task.service.TaskService;
import org.exoplatform.task.service.UserService;
import org.exoplatform.task.utils.ProjectUtil;
import org.exoplatform.task.utils.TaskUtil;
import org.json.JSONException;
import org.json.JSONObject;

import javax.inject.Inject;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * @author <a href="mailto:tuyennt@exoplatform.com">Tuyen Nguyen The</a>.
 */
public class TaskController {

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
  @Path("comments.gtmpl")
  org.exoplatform.task.management.templates.comments comments;

  @Inject
  @Path("projectTaskListView.gtmpl")
  org.exoplatform.task.management.templates.projectTaskListView taskListView;

  @Resource
  @Ajax
  @MimeType.HTML
  public Response detail(Long id, SecurityContext securityContext) {
    Task task = taskService.getTaskById(id);
    StringBuilder coWorkerDisplayName = new StringBuilder();
    if(task.getCoworker() != null && task.getCoworker().size() > 0) {
      for(String userName : task.getCoworker()) {
        try {
          User user = orgService.getUserHandler().findUserByName(userName);
          if(user != null) {
            if(coWorkerDisplayName.length() > 0) {
              coWorkerDisplayName.append(", ");
            }
            coWorkerDisplayName.append(UserUtils.getDisplayName(user));
          }
        } catch (Exception ex) {
        }
      }
    }

    String assignee = "";
    if(task.getAssignee() != null) {
      try {
        User user = orgService.getUserHandler().findUserByName(task.getAssignee());
        if(user != null) {
          assignee = UserUtils.getDisplayName(user);
        }
      } catch (Exception ex) {
        return Response.status(500).body(ex.getMessage());
      }
    }

    long commentCount = taskService.getNbOfCommentsByTask(task);

    List<Comment> cmts = taskService.getCommentsByTask(task, 0, 2);
    List<CommentModel> comments = new ArrayList<CommentModel>(cmts.size());
    for(Comment c : cmts) {
      org.exoplatform.task.model.User u = userService.loadUser(c.getAuthor());
      comments.add(new CommentModel(c, u, CommentUtils.formatMention(c.getComment(), userService)));
    }

    org.exoplatform.task.model.User currentUser = userService.loadUser(securityContext.getRemoteUser());

    return detail.with()
        .task(task)
        .assigneeName(assignee)
        .coWokerDisplayName(coWorkerDisplayName.toString())
        .commentCount(commentCount)
        .comments(comments)
        .currentUser(currentUser)
        .ok().withCharset(Tools.UTF_8);
  }

  @Resource
  @Ajax
  @MimeType.JSON
  public Response clone(Long id) {

    Task newTask = taskService.cloneTaskById(id);

    if(newTask == null) {
      return Response.error("Impossible to clone task with ID: " + id);
    }

    try {
      JSONObject json = new JSONObject();
      json.put("id", newTask.getId());
      return Response.ok(json.toString());
    } catch (JSONException ex) {
      return Response.status(500).body(ex.getMessage());
    }
  }

  @Resource
  @Ajax
  @MimeType.JSON
  public Response delete(Long id) {

    if(!taskService.deleteTaskById(id)) {
      return Response.error("Impossible to delete task with ID: " + id);
    }

    try {
      JSONObject json = new JSONObject();
      json.put("id", id);
      return Response.ok(json.toString());
    } catch (JSONException ex) {
      return Response.status(500).body(ex.getMessage());
    }
  }

  @Resource
  @Ajax
  @MimeType("text/plain")
  public Response saveTaskInfo(Long taskId, String name, String[] value) {

    if (taskService.updateTaskInfo(taskId, name, value) == null ) {
      return Response.error("Impossible to save field "+name+" with value "+value+" for task ID: "+taskId);
    }

    return Response.ok("Update successfully");
  }

  @Resource
  @Ajax
  @MimeType("text/plain")
  public Response updateCompleted(Long taskId, Boolean completed) {

    if(taskService.updateTaskCompleted(taskId, completed) == null ) {
      return Response.notFound("Impossible to update field completed with value "+completed+" for task ID: " + taskId);
    }

    return Response.ok("Update successfully");
  }

  @Resource
  @Ajax
  @MimeType.JSON
  public Response comment(Long taskId, String comment, SecurityContext securityContext) {
    String currentUser = securityContext.getRemoteUser();
    if (currentUser == null || currentUser.isEmpty()) {
      return Response.status(401);
    }

    Comment cmt = taskService.addCommentToTaskId(taskId, currentUser, comment);
    if (cmt == null) {
      return Response.status(404).body("Impossible to add comment to task with ID: " + taskId);
    }

    //TODO:
    CommentModel model = new CommentModel(cmt, userService.loadUser(cmt.getAuthor()), CommentUtils.formatMention(cmt.getComment(), userService));

    DateFormat df = new SimpleDateFormat("MMM dd, yyyy HH:mm");
    try {
      JSONObject json = new JSONObject();
      json.put("id", model.getId());
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
    } catch (JSONException ex) {
      return Response.status(500).body(ex.getMessage());
    }
  }

  @Resource
  @Ajax
  @MimeType.HTML
  public Response loadAllComments(Long taskId, SecurityContext securityContext) {

    List<Comment> cmts = taskService.getCommentsByTaskId(taskId, 0, -1);

    if(cmts == null) {
      return Response.error("Impossible to load all comments for task with ID: " + taskId);
    }

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
  }

  @Resource
  @Ajax
  @MimeType("text/plain")
  public Response deleteComment(Long commentId) {

    if(!taskService.deleteCommentById(commentId)) {
      return Response.error("Impossible to delete comment with ID: " + commentId);
    }

    return Response.ok("Delete comment successfully!");

  }

  @Resource
  @Ajax
  @MimeType.HTML
  public Response listTasks(Long projectId, String keyword, String groupBy, String orderBy,
                            SecurityContext securityContext) {
    Project project = null;
    List<Task> tasks;

    String currentUser = securityContext.getRemoteUser();
    if (currentUser == null || currentUser.isEmpty()) {
      return Response.status(401);
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
      tasks = taskService.getToDoTasksByUser(currentUser, order);
    }
    else {
      tasks = projectService.getTasksWithKeywordByProjectId(projectId, order, keyword);
      project = projectService.getProjectById(projectId); //DAOHandler.getProjectHandler().find(projectId);
      if(tasks == null) {
        return Response.notFound("Impossible to get tasks for project with ID: " + projectId);
      }
    }

    //Group Tasks
    Map<String, List<Task>> groupTasks = new HashMap<String, List<Task>>();
    if(groupBy != null && !groupBy.isEmpty()) {
      groupTasks = TaskUtil.groupTasks(tasks, groupBy);
    }
    if(groupTasks.isEmpty()) {
      groupTasks.put("", tasks);
    }

    return taskListView
        .with()
        .currentProjectId(projectId)
        .project(project)
        .tasks(tasks)
        .groupTasks(groupTasks)
        .keyword(keyword == null ? "" : keyword)
        .groupBy(groupBy == null ? "" : groupBy)
        .orderBy(orderBy == null ? "" : orderBy)
        .ok()
        .withCharset(Tools.UTF_8);
  }

  @Resource(method = HttpMethod.POST)
  @Ajax
  @MimeType.HTML
  public Response createTask(Long projectId, String taskInput, SecurityContext securityContext) {

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
      projectService.createTaskToProjectId(projectId, task);
    }
    //Incoming Task
    else {
      taskService.createTask(task);
    }

    return listTasks(projectId, "", "", "", securityContext);
  }

}

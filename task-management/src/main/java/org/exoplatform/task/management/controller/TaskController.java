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

import juzu.MimeType;
import juzu.Path;
import juzu.Resource;
import juzu.Response;
import juzu.HttpMethod;
import juzu.impl.common.Tools;
import juzu.request.SecurityContext;
import org.exoplatform.commons.juzu.ajax.Ajax;
import org.exoplatform.services.organization.OrganizationService;
import org.exoplatform.services.organization.User;
import org.exoplatform.task.dao.CommentHandler;
import org.exoplatform.task.dao.OrderBy;
import org.exoplatform.task.dao.TaskQuery;
import org.exoplatform.task.domain.Comment;
import org.exoplatform.task.domain.Project;
import org.exoplatform.task.domain.Status;
import org.exoplatform.task.domain.Task;
import org.exoplatform.task.management.model.CommentModel;
import org.exoplatform.task.management.util.CommentUtils;
import org.exoplatform.task.management.util.UserUtils;
import org.exoplatform.task.service.TaskBuilder;
import org.exoplatform.task.service.TaskParser;
import org.exoplatform.task.service.TaskService;
import org.exoplatform.task.service.UserService;
import org.json.JSONException;
import org.json.JSONObject;

import javax.inject.Inject;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * @author <a href="mailto:tuyennt@exoplatform.com">Tuyen Nguyen The</a>.
 */
public class TaskController {
  public static final int INCOMING_PROJECT_ID = -1;
  public static final int TODO_PROJECT_ID = -2;


  @Inject
  TaskService taskService;

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
    Task task = taskService.getTaskHandler().find(id);
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

    long commentCount = taskService.getCommentHandler().count(task);

    List<Comment> cmts = taskService.getCommentHandler().findCommentsOfTask(task, 0, 2);
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
    Task task = taskService.getTaskHandler().find(id);
    if(task == null) {
      return Response.notFound("Can not find task with ID: " + id);
    }
    Task newTask = new TaskBuilder()
        .withTitle("[Clone] " + task.getTitle())
        .withAssignee(task.getAssignee())
        .withContext(task.getContext())
        .withCreatedBy(task.getCreatedBy())
        .withDescription(task.getDescription())
        .withDueDate(task.getDueDate())
        .withPriority(task.getPriority())
        .withStartDate(task.getStartDate())
        .withDuration(task.getDuration())
        .withStatus(task.getStatus())
        .build();
    newTask.setCoworker(task.getCoworker());
    newTask.setTags(task.getTags());
    taskService.getTaskHandler().create(newTask);
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
    Task task = taskService.getTaskHandler().find(id);
    if(task == null) {
      return Response.notFound("Can not find task with ID: " + id);
    }
    taskService.getTaskHandler().delete(task);
    try {
      JSONObject json = new JSONObject();
      json.put("id", task.getId());
      return Response.ok(json.toString());
    } catch (JSONException ex) {
      return Response.status(500).body(ex.getMessage());
    }
  }

  @Resource
  @Ajax
  @MimeType("text/plain")
  public Response saveTaskInfo(Long taskId, String name, String[] value) {
    String val = value != null && value.length > 0 ? value[0] : null;

    Task task = taskService.getTaskHandler().find(taskId);
    if(task == null) {
      return Response.notFound("Task not found with ID: " + taskId);
    }
    if("title".equalsIgnoreCase(name)) {
      task.setTitle(val);
    } else if("dueDate".equalsIgnoreCase(name)) {
      if(value == null || val.trim().isEmpty()) {
        task.setDueDate(null);
      } else {
        DateFormat df = new SimpleDateFormat("YYYY-MM-dd");
        try {
          Date date = df.parse(val);
          task.setDueDate(date);
        } catch (ParseException ex) {
          return Response.status(406).body("Can not parse date time value: " + val);
        }
      }
    } else if("status".equalsIgnoreCase(name)) {
      //TODO: load status from ID
      try {
        int status = Integer.parseInt(val);
        for(Status s : Status.STATUS) {
          if(s.getId() == status) {
            task.setStatus(s);
            break;
          }
        }
      } catch (NumberFormatException ex) {
        return Response.status(406).body("Status is unacceptable: " + val);
      }
    } else if("description".equalsIgnoreCase(name)) {
      task.setDescription(val);
    } else if("assignee".equalsIgnoreCase(name)) {
      task.setAssignee(val);
    } else if("coworker".equalsIgnoreCase(name)) {
      Set<String> coworker = new HashSet<String>();
      for(String v : value) {
        coworker.add(v);
      }
      task.setCoworker(coworker);
    } else if("tags".equalsIgnoreCase(name)) {
      Set<String> tags = new HashSet<String>();
      for(String t : value) {
        tags.add(t);
      }
      task.setTags(tags);
    } else {
      return Response.status(406).body("Field name: " + name + " is not supported");
    }

    taskService.getTaskHandler().update(task);
    return Response.ok("Update successfully");
  }

  @Resource
  @Ajax
  @MimeType("text/plain")
  public Response updateTaskStatus(Long taskId, int status) {
    Task task = taskService.getTaskHandler().find(taskId);
    if(task == null) {
      return Response.notFound("Task not found with ID: " + taskId);
    }
    for(Status s : Status.STATUS) {
      if(s.getId() == status) {
        task.setStatus(s);
        break;
      }
    }
    taskService.getTaskHandler().update(task);
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

    Task task = taskService.getTaskHandler().find(taskId);
    if (task == null) {
      return Response.status(404).body("There is no task with ID: " + taskId);
    }

    Comment cmt = new Comment();
    cmt.setTask(task);
    cmt.setAuthor(currentUser);
    cmt.setComment(comment);
    cmt.setCreatedTime(new Date());

    cmt = taskService.getCommentHandler().create(cmt);

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
    Task task = taskService.getTaskHandler().find(taskId);
    if(task == null) {
      return Response.notFound("Task does not exist with ID: " + taskId);
    }
    CommentHandler commentDAO = taskService.getCommentHandler();
    long commentCount = commentDAO.count(task);

    List<Comment> cmts = commentDAO.findCommentsOfTask(task, 0, -1);
    List<CommentModel> listComments = new ArrayList<CommentModel>(cmts.size());
    for(Comment cmt : cmts) {
      org.exoplatform.task.model.User u = userService.loadUser(cmt.getAuthor());
      listComments.add(new CommentModel(cmt, u, CommentUtils.formatMention(cmt.getComment(), userService)));
    }

    org.exoplatform.task.model.User currentUser = userService.loadUser(securityContext.getRemoteUser());

    return comments.with()
            .commentCount(commentCount)
            .comments(listComments)
            .currentUser(currentUser)
            .ok()
            .withCharset(Tools.UTF_8);
  }

  @Resource
  @Ajax
  @MimeType("text/plain")
  public Response deleteComment(Long commentId) {
    CommentHandler commentDAO = taskService.getCommentHandler();
    Comment comment = commentDAO.find(commentId);
    if(comment == null) {
      return Response.notFound("Comment is not exists with ID: " + commentId);
    }
    commentDAO.delete(comment);
    return Response.ok("Delete comment successfully!");
  }

  @Resource
  @Ajax
  @MimeType.HTML
  public Response listTasks(Long projectId, String keyword, String groupBy, String orderBy,
                            SecurityContext securityContext) {
    Project project = null;
    List<Task> tasks;

    //TODO: if username is NULL?
    String username = securityContext.getRemoteUser();

    OrderBy order = null;
    if(orderBy != null && !orderBy.trim().isEmpty()) {
      order = "title".equals(orderBy) ? new OrderBy.ASC(orderBy) : new OrderBy.DESC(orderBy);
    }

    if(projectId == INCOMING_PROJECT_ID) {
      tasks = taskService.getTaskHandler().getIncomingTask(username, order);
    } else if (projectId == TODO_PROJECT_ID) {
      tasks = taskService.getTaskHandler().getToDoTask(username, order);
    } else {
      project = taskService.getProjectHandler().find(projectId);
      if(project == null) {
        return Response.notFound("Project not found with ID: " + projectId);
      }
      TaskQuery taskQuery = new TaskQuery();
      taskQuery.setProjectId(project.getId());
      taskQuery.setKeyword(keyword);
      taskQuery.setOrderBy(order == null ? null : Arrays.asList(order));
      tasks = taskService.getTaskHandler().findTaskByQuery(taskQuery);
    }

    //
    Map<String, List<Task>> groupTasks = new HashMap<String, List<Task>>();
    if(groupBy != null && !groupBy.isEmpty()) {
      groupTasks = groupTasks(tasks, groupBy);
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

    String username = securityContext.getRemoteUser();

    Task task = taskParser.parse(taskInput);

    Project project = null;

    if(projectId > 0) {
      project = taskService.getProjectHandler().find(projectId);
      if (project == null) {
        return Response.notFound("Project not found with ID: " + projectId);
      }
      Status status = taskService.getStatusHandler().findLowestRankStatusByProject(projectId);
      task.setStatus(status);
    }

    task.setCreatedBy(username);

    if (TODO_PROJECT_ID == projectId) {
      task.setAssignee(username);
    }

    taskService.getTaskHandler().create(task);

    return listTasks(projectId, "", "", "", securityContext);
  }


  public static Map<String, List<Task>> groupTasks(List<Task> tasks, String groupBy) {
    Map<String, List<Task>> maps = new HashMap<String, List<Task>>();
    for(Task task : tasks) {
      for (String key : getGroupName(task, groupBy)) {
        List<Task> list = maps.get(key);
        if(list == null) {
          list = new LinkedList<Task>();
          maps.put(key, list);
        }
        list.add(task);
      }
    }
    return maps;
  }

  private static String[] getGroupName(Task task, String groupBy) {
    if("project".equalsIgnoreCase(groupBy)) {
      Status s = task.getStatus();
      if(s == null) {
        return new String[] {"No Project"};
      } else {
        return new String[] {s.getProject().getName()};
      }
    } else if("status".equalsIgnoreCase(groupBy)) {
      Status s = task.getStatus();
      if(s == null) {
        return new String[] {"TODO"};
      } else {
        return new String[] {s.getName()};
      }
    } else if("tag".equalsIgnoreCase(groupBy)) {
      Set<String> tags = task.getTags();
      return tags == null || tags.size() == 0 ? new String[] {"Un tagged"} : tags.toArray(new String[0]);
    } else if ("assignee".equalsIgnoreCase(groupBy)) {
      String assignee = task.getAssignee();
      return new String[] {assignee != null ? assignee : "Unassigned"};
    }
    return new String[0];
  }
}

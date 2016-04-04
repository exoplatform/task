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

import juzu.HttpMethod;
import juzu.MimeType;
import juzu.Path;
import juzu.Resource;
import juzu.Response;
import juzu.impl.common.Tools;
import juzu.request.SecurityContext;

import org.exoplatform.commons.juzu.ajax.Ajax;
import org.exoplatform.commons.utils.HTMLEntityEncoder;
import org.exoplatform.commons.utils.ListAccess;
import org.exoplatform.services.organization.OrganizationService;
import org.exoplatform.services.security.ConversationState;
import org.exoplatform.services.security.Identity;
import org.exoplatform.task.dao.OrderBy;
import org.exoplatform.task.dao.TaskQuery;
import org.exoplatform.task.domain.ChangeLog;
import org.exoplatform.task.domain.Comment;
import org.exoplatform.task.domain.Label;
import org.exoplatform.task.domain.Project;
import org.exoplatform.task.domain.Status;
import org.exoplatform.task.domain.Task;
import org.exoplatform.task.exception.EntityNotFoundException;
import org.exoplatform.task.exception.NotAllowedOperationOnEntityException;
import org.exoplatform.task.exception.ParameterEntityException;
import org.exoplatform.task.exception.UnAuthorizedOperationException;
import org.exoplatform.task.management.model.Paging;
import org.exoplatform.task.management.model.TaskFilterData;
import org.exoplatform.task.management.model.TaskFilterData.Filter;
import org.exoplatform.task.management.model.TaskFilterData.FilterKey;
import org.exoplatform.task.management.model.ViewType;
import org.exoplatform.task.management.service.ViewStateService;
import org.exoplatform.task.management.util.JsonUtil;
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
import org.exoplatform.task.util.CommentUtil;
import org.exoplatform.task.util.DateUtil;
import org.exoplatform.task.util.ListUtil;
import org.exoplatform.task.util.ProjectUtil;
import org.exoplatform.task.util.TaskUtil;
import org.exoplatform.task.util.TaskUtil.DUE;
import org.gatein.common.text.EntityEncoder;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * @author <a href="mailto:tuyennt@exoplatform.com">Tuyen Nguyen The</a>.
 */
public class TaskController extends AbstractController {

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

  //TODO: This template is not used
  @Inject
  @Path("comments.gtmpl")
  org.exoplatform.task.management.templates.comments comments;

  @Inject
  @Path("projectTaskListView.gtmpl")
  org.exoplatform.task.management.templates.projectTaskListView taskListView;

  @Inject
  @Path("confirmDeleteTask.gtmpl")
  org.exoplatform.task.management.templates.confirmDeleteTask confirmDeleteTask;
  
  @Inject
  TaskFilterData filterData;

  @Inject
  ViewStateService viewStateService;

  @Resource
  @Ajax
  @MimeType.HTML
  public Response detail(Long id, SecurityContext securityContext) throws EntityNotFoundException, UnAuthorizedOperationException {
    Task task = taskService.getTask(id);
    if (!TaskUtil.hasPermission(task)) {
      throw new UnAuthorizedOperationException(id, Task.class, getNoPermissionMsg());
    }
    TaskModel model = TaskUtil.getTaskModel(id, false, bundle, securityContext.getRemoteUser(), taskService, orgService, userService, projectService);
    TimeZone userTimezone = userService.getUserTimezone(securityContext.getRemoteUser());
  
  return detail.with()
      .taskModel(model)
      .userTimezone(userTimezone)
      .bundle(bundle)
      .ok().withCharset(Tools.UTF_8);
  }
  
  @Resource
  @Ajax
  @MimeType.JSON
  public Response findLabelByTask(Long taskId, SecurityContext securityContext) throws JSONException {
    String userId = securityContext.getRemoteUser();
    ListAccess<Label> labels = null;
    try {
      labels = taskService.findLabelsByTask(taskId, userId);
    } catch (EntityNotFoundException e) {
      LOG.error(e);
    }
    JSONArray arr = new JSONArray();
    if (labels != null) {
      for (Label lbl : ListUtil.load(labels, 0, -1)) {
        JSONObject json = new JSONObject();
        json.put("name", lbl.getName());
        json.put("color", lbl.getColor());
        arr.put(json);
      }      
    }
    return Response.ok(arr.toString()).withCharset(Tools.UTF_8);
  }

  @Resource
  @Ajax
  @MimeType.HTML
  public Response renderTaskLogs(Long taskId, SecurityContext securityContext) throws EntityNotFoundException, UnAuthorizedOperationException {
    Task task = taskService.getTask(taskId);
    if (!TaskUtil.hasPermission(task)) {
      throw new UnAuthorizedOperationException(taskId, Task.class, getNoPermissionMsg());
    }
    ChangeLog[] arr = ListUtil.load(taskService.getTaskLogs(taskId), 0, -1);

    List<ChangeLog> logs = new LinkedList<ChangeLog>(Arrays.asList(arr));
    Collections.sort(logs);
    Map<String, User> userMap = new HashMap<String, User>();
    if (logs.size() > 0) {
      for (ChangeLog log : logs) {
        String author = log.getAuthor();
        if (!userMap.containsKey(author)) {
          User user = userService.loadUser(log.getAuthor());
          userMap.put(author, user);
        }
        if ("assign".equals(log.getActionName()) || "unassign".equals(log.getActionName())) {
          String target = log.getTarget();
          if (target != null && !userMap.containsKey(target)) {
            User user = userService.loadUser(target);
            userMap.put(target, user);
          }
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
  public Response renderTaskComments(Long id, Boolean loadAllComment, SecurityContext securityContext) throws EntityNotFoundException, UnAuthorizedOperationException {
      Task task = taskService.getTask(id);
      if (!TaskUtil.hasPermission(task)) {
        throw new UnAuthorizedOperationException(id, Task.class, getNoPermissionMsg());
      }
      if (loadAllComment == null) {
        loadAllComment = Boolean.FALSE;
      }
      TaskModel model = TaskUtil.getTaskModel(id, loadAllComment, bundle, securityContext.getRemoteUser(), taskService, orgService, userService, projectService);

      return taskComments.with()
              .taskModel(model)
              .bundle(bundle)
              .ok().withCharset(Tools.UTF_8);
  }

  @Resource
  @Ajax
  @MimeType.JSON
  public Response clone(Long id) throws EntityNotFoundException, JSONException, UnAuthorizedOperationException {
      Task task = taskService.getTask(id);
      if (!TaskUtil.hasPermission(task)) {
        throw new UnAuthorizedOperationException(id, Task.class, getNoPermissionMsg());
      }
      Task newTask = taskService.cloneTask(id); //Can throw TaskNotFoundException

      JSONObject json = new JSONObject();
      json.put("id", newTask.getId()); //Can throw JSONException
      return Response.ok(json.toString());
  }

  @Resource
  @Ajax
  @MimeType.HTML
  public Response openConfirmDeleteTask(Long id) throws EntityNotFoundException, UnAuthorizedOperationException {
    Task task = taskService.getTask(id);
    if (!TaskUtil.hasPermission(task)) {
      throw new UnAuthorizedOperationException(id, Task.class, getNoPermissionMsg());
    }

    String msg = bundle.getString("popup.msg.deleteTask");
    msg = msg.replace("{}", task.getTitle());

    return confirmDeleteTask
            .with()
            .taskId(task.getId())
            .msg(msg)
            .ok().withCharset(Tools.UTF_8);
  }

  @Resource
  @Ajax
  @MimeType.JSON
  public Response delete(Long id, SecurityContext context) throws EntityNotFoundException, JSONException, UnAuthorizedOperationException {
    Task task = taskService.getTask(id);
    if (!TaskUtil.hasPermission(task)) {
      throw new UnAuthorizedOperationException(id, Task.class, getNoPermissionMsg());
    }
    taskService.removeTask(id);//Can throw TaskNotFoundException

    String username = context.getRemoteUser();
    long taskNum = getIncomingNum(username);    
    
    JSONObject json = new JSONObject();
    json.put("id", id); //Can throw JSONException
    json.put("incomNum", taskNum);
    return Response.ok(json.toString());
  }

  @Resource
  @Ajax
  @MimeType("text/plain")
  public Response saveTaskInfo(Long taskId, String name, String[] value, SecurityContext context) throws EntityNotFoundException, ParameterEntityException, UnAuthorizedOperationException {
    TimeZone timezone = userService.getUserTimezone(context.getRemoteUser());
    Task task = taskService.getTask(taskId);
    if (!TaskUtil.hasPermission(task) || 
        !TaskUtil.hasPermissionOnField(task, name, value, statusService, taskService, projectService)) {
      throw new UnAuthorizedOperationException(taskId, Task.class, getNoPermissionMsg());
    }    
    task = TaskUtil.saveTaskField(task, context.getRemoteUser(), name, value, timezone, taskService, statusService);

    String response = "Update successfully";
    if ("workPlan".equalsIgnoreCase(name)) {
      Calendar start = null;
      Calendar end = null;
      if (task.getStartDate() != null && task.getEndDate() != null) {
        start = DateUtil.newCalendarInstance(timezone);
        start.setTime(task.getStartDate());
        end = DateUtil.newCalendarInstance(timezone);
        end.setTime(task.getEndDate());
      }
      
      response = TaskUtil.getWorkPlan(start, end, bundle);
      if (response == null) {
        response = bundle.getString("label.noWorkPlan");
      }
    }
    
    //update project or assignee, or coworker, need to update Incomming task number
    if ("project".equalsIgnoreCase(name) || "assignee".equalsIgnoreCase(name) || "coworker".equalsIgnoreCase(name)) {
      String username = context.getRemoteUser();
      long taskNum = getIncomingNum(username);
      response = String.valueOf(taskNum);
    }

    return Response.ok(response);
  }

  @Resource
  @Ajax
  @MimeType("text/plain")
  public Response saveDragAndDropTask(Long taskId, Long newStatusId, String groupBy, String oldGroup, String newGroup, Boolean needUpdateOrder, Long[] orders, SecurityContext context) throws EntityNotFoundException, UnAuthorizedOperationException, ParameterEntityException {
    if (taskId == null || taskId == 0) {
      return Response.status(404).body("Task not found");
    }

    Task task = taskService.getTask(taskId);
    if (!TaskUtil.hasPermission(task)) {
      throw new UnAuthorizedOperationException(taskId, Task.class, getNoPermissionMsg());
    }

    Status newStatus = null;
    if (newStatusId != null && newStatusId > 0) {
      newStatus = statusService.getStatus(newStatusId);
      if (!newStatus.getProject().canView(ConversationState.getCurrent().getIdentity())) {
        throw new UnAuthorizedOperationException(taskId, Task.class, getNoPermissionMsg());
      }
    }

    String username = context.getRemoteUser();
    TimeZone tz = userService.getUserTimezone(context.getRemoteUser());

    if (needUpdateOrder != null && needUpdateOrder) {
      long[] ids = new long[orders.length];
      for (int i = 0; i < ids.length; i++) {
        ids[i] = orders[i];
      }
      taskService.updateTaskOrder(taskId, newStatus, ids);
      task = taskService.getTask(task.getId());
    } else {
      task = TaskUtil.saveTaskField(task, username, "status", new String[] {String.valueOf(newStatusId)}, tz, taskService, statusService);
    }

    // Update new assignee or label if need
    if (groupBy != null &&
            !TaskUtil.NONE.equalsIgnoreCase(groupBy)
            && !groupBy.trim().isEmpty()
            && !oldGroup.equalsIgnoreCase(newGroup)) {
      if (TaskUtil.ASSIGNEE.equalsIgnoreCase(groupBy)) {
        TaskUtil.saveTaskField(task, username, TaskUtil.ASSIGNEE, new String[]{newGroup}, tz, taskService, statusService);

      } else if (TaskUtil.LABEL.equalsIgnoreCase(groupBy)) {
        long oldLabelId = 0, newLabelId = 0;
        try {
          oldLabelId = oldGroup.isEmpty() ? 0 : Long.parseLong(oldGroup);
        } catch (NumberFormatException ex) {
          throw new ParameterEntityException(task.getId(), Task.class, "Label ID", oldGroup, null, ex);
        }
        try {
          newLabelId = newGroup.isEmpty() ? 0 : Long.parseLong(newGroup);
        } catch (NumberFormatException ex) {
          throw new ParameterEntityException(task.getId(), Task.class, "Label ID", newGroup, null, ex);
        }

        ListAccess<Label> list = taskService.findLabelsByTask(task.getId(), username);
        Label[] labels = ListUtil.load(list, 0, -1);
        List<String> newLabelIds = new ArrayList<String>(labels.length + 1);
        for (Label l : labels) {
          if (l.getId() == oldLabelId) continue;
          newLabelIds.add(String.valueOf(l.getId()));
        }
        if (newLabelId > 0) {
          newLabelIds.add(String.valueOf(newLabelId));
        }

        TaskUtil.saveTaskField(task, username, "labels", newLabelIds.toArray(new String[newLabelIds.size()]), tz, taskService, statusService);
      }
    }

    return Response.ok("Update successfully");
  }

  @Resource
  @Ajax
  @MimeType("text/plain")
  public Response updateCompleted(Long taskId, Boolean completed, SecurityContext securityContext) throws EntityNotFoundException, ParameterEntityException, UnAuthorizedOperationException {
    Task task = taskService.getTask(taskId);
    if (!TaskUtil.hasPermission(task)) {
      throw new UnAuthorizedOperationException(taskId, Task.class, getNoPermissionMsg());
    }
    task.setCompleted(completed);
    taskService.updateTask(task);
    
    String username = securityContext.getRemoteUser();
    long taskNum = getIncomingNum(username);    
    return Response.ok(String.valueOf(taskNum));
  }

  @Resource
  @Ajax
  @MimeType.JSON
  public Response comment(Long taskId, String comment, SecurityContext securityContext) throws EntityNotFoundException, JSONException, UnAuthorizedOperationException {
    Task task = taskService.getTask(taskId);
    String currentUser = securityContext.getRemoteUser();
    if (!TaskUtil.hasPermission(task)) {
      throw new UnAuthorizedOperationException(taskId, Task.class, getNoPermissionMsg());
    }
    Comment cmt = taskService.addComment(taskId, currentUser, comment); //Can throw TaskNotFoundException

    //TODO:
    CommentModel model = new CommentModel(cmt, userService.loadUser(cmt.getAuthor()), CommentUtil.formatMention(cmt.getComment(), userService));

    DateFormat df = new SimpleDateFormat("MMM dd, yyyy HH:mm");
    df.setTimeZone(userService.getUserTimezone(currentUser));

    EntityEncoder encoder = HTMLEntityEncoder.getInstance();
    JSONObject json = new JSONObject();
    json.put("id", model.getId()); //Can throw JSONException (same for all #json.put methods below)
    JSONObject user = new JSONObject();
    user.put("username", encoder.encode(model.getAuthor().getUsername()));
    user.put("displayName", encoder.encode(model.getAuthor().getDisplayName()));
    user.put("avatar", model.getAuthor().getAvatar());
    json.put("author", user);
    json.put("comment", encoder.encode(model.getComment()));
    json.put("formattedComment", model.getFormattedComment());
    json.put("createdTime", model.getCreatedTime().getTime());
    json.put("createdTimeString", df.format(model.getCreatedTime()));
    return Response.ok(json.toString()).withCharset(Tools.UTF_8);
  }

  //TODO: this method is not used any more?
  @Resource
  @Ajax
  @MimeType.HTML
  public Response loadAllComments(Long taskId, SecurityContext securityContext) throws EntityNotFoundException, UnAuthorizedOperationException {
      // Verify task exists
      Task task = taskService.getTask(taskId);
      if (!TaskUtil.hasPermission(task)) {
        throw new UnAuthorizedOperationException(taskId, Task.class, getNoPermissionMsg());
      }

      ListAccess<Comment> cmtAccessList = taskService.getComments(task.getId());
      Comment[] cmts = ListUtil.load(cmtAccessList, 0, -1); //Can throw TaskNotFoundException

      List<CommentModel> listComments = new ArrayList<CommentModel>(cmts.length);
      for(Comment cmt : cmts) {
        org.exoplatform.task.model.User u = userService.loadUser(cmt.getAuthor());
        listComments.add(new CommentModel(cmt, u, CommentUtil.formatMention(cmt.getComment(), userService)));
      }

      org.exoplatform.task.model.User currentUser = userService.loadUser(securityContext.getRemoteUser());

      return comments.with()
          .commentCount(cmts.length)
          .comments(listComments)
          .currentUser(currentUser)
          .ok()
          .withCharset(Tools.UTF_8);
  }

  @Resource
  @Ajax
  @MimeType("text/plain")
  public Response deleteComment(Long commentId) throws EntityNotFoundException {
    Comment comment = taskService.getComment(commentId);
    Identity currIdentity = ConversationState.getCurrent().getIdentity();
    if (TaskUtil.canDeleteComment(currIdentity, comment)) {
      taskService.removeComment(commentId);
      return Response.ok("Delete comment successfully!");
    } else {
      return Response.status(401).body("Only owner or project manager can delete the comment");
    }
  }

  @Resource
  @Ajax
  @MimeType.HTML
  public Response listTasks(String space_group_id, Long projectId, Long labelId, String filterLabelIds, String statusId, String dueDate, String priority,
                            String assignee, Boolean showCompleted, String keyword, String groupBy, String orderBy, String filter, String viewType, Integer page, SecurityContext securityContext) throws EntityNotFoundException, UnAuthorizedOperationException {

    FilterKey filterKey = FilterKey.withProject(projectId, filter == null || filter.isEmpty() ? null : DUE.valueOf(filter.toUpperCase()));
    if (labelId != null && labelId != -1L) {
      filterKey = FilterKey.withLabel(labelId);
    }
    Filter fd = filterData.getFilter(filterKey);

    boolean advanceSearch = fd.isEnabled();
    if (advanceSearch) {
      fd.updateFilterData(filterLabelIds, statusId, dueDate, priority, assignee, showCompleted, keyword);
    }
    
    Identity currIdentity = ConversationState.getCurrent().getIdentity();
    ViewType vType;
    if (projectId <= 0 || viewType == null || viewType.isEmpty()) {
      vType = viewStateService.getViewType(securityContext.getRemoteUser(), projectId);
    } else {
      vType = ViewType.getViewType(viewType);
      viewStateService.saveViewType(securityContext.getRemoteUser(), projectId, vType);
    }
    boolean isBoardView = (ViewType.BOARD == vType);

    Project project = null;
    boolean noProjPermission = false;
    List<Status> projectStatuses = Collections.emptyList();
    if (projectId != null && projectId > 0) {
      project = projectService.getProject(projectId);
      if (!project.canView(currIdentity)) {
        if (advanceSearch) {
          noProjPermission = true;
        } else {
          throw new UnAuthorizedOperationException(projectId, Project.class, getNoPermissionMsg());
        }
      } else if (isBoardView) {
        projectStatuses = statusService.getStatuses(projectId);
      }
    }
    String currentLabelName = "";
    boolean noLblPermission = false;
    if (labelId != null && labelId > 0) {
      Label label = taskService.getLabel(labelId);      
      if (!label.getUsername().equals(currIdentity.getUserId())) {
        if (advanceSearch) {
          noLblPermission = true;
        } else {
          throw new UnAuthorizedOperationException(labelId, Label.class, getNoPermissionMsg());          
        }
      } else {
        currentLabelName = label.getName();
      }
    }

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
    
    List<Long> spaceProjectIds = null;    
    if (space_group_id != null) {
      spaceProjectIds = new LinkedList<Long>();
      List<Project> projects = ProjectUtil.flattenTree(ProjectUtil.getProjectTree(space_group_id, projectService), projectService);
      for (Project p : projects) {
        if (p.canView(currIdentity)) {
          spaceProjectIds.add(p.getId());          
        }
      }
    }
    List<Long> allProjectIds = null;
    if (projectId == 0) {
      allProjectIds = new LinkedList<Long>();
      List<Project> projects = ProjectUtil.flattenTree(ProjectUtil.getProjectTree(null, projectService), projectService);
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
        
    TaskQuery taskQuery = new TaskQuery();
    if (advanceSearch) {
      Status status = fd.getStatus() != null ? statusService.getStatus(fd.getStatus()) : null;
      TaskUtil.buildTaskQuery(taskQuery, fd.getKeyword(), fd.getLabel(), status, fd.getDue(), fd.getPriority(), fd.getAssignee(), fd.isShowCompleted(), userTimezone);
    } else {
      taskQuery.setCompleted(false);
    }

    //Get Tasks in good order
    if(projectId == ProjectUtil.INCOMING_PROJECT_ID) {
      //. Default order by CreatedDate
      if (orderBy == null || orderBy.isEmpty()) {
        orderBy = TaskUtil.CREATED_TIME;
        order = new OrderBy.DESC(orderBy);
      }

      taskQuery.setIsIncomingOf(currentUser);
      taskQuery.setOrderBy(Arrays.asList(order));
    }
    else if (projectId == ProjectUtil.TODO_PROJECT_ID) {
      defGroupBys = TaskUtil.resolve(Arrays.asList(TaskUtil.NONE, TaskUtil.PROJECT, TaskUtil.LABEL, TaskUtil.DUEDATE), bundle);
      defOrders = TaskUtil.resolve(Arrays.asList(TaskUtil.TITLE, TaskUtil.STATUS, TaskUtil.DUEDATE, TaskUtil.PRIORITY, TaskUtil.RANK), bundle);

      taskQuery.setIsTodoOf(currentUser);

      //TODO: process fiter here
      if ("overDue".equalsIgnoreCase(filter)) {
        defGroupBys = TaskUtil.resolve(Arrays.asList(TaskUtil.NONE, TaskUtil.PROJECT, TaskUtil.LABEL), bundle);
        defOrders = TaskUtil.resolve(Arrays.asList(TaskUtil.TITLE, TaskUtil.PRIORITY, TaskUtil.DUEDATE), bundle);
        groupBy = groupBy == null || !defGroupBys.containsKey(groupBy) ? TaskUtil.PROJECT : groupBy;
      } else if ("today".equalsIgnoreCase(filter)) {
        defGroupBys = TaskUtil.resolve(Arrays.asList(TaskUtil.NONE, TaskUtil.PROJECT, TaskUtil.LABEL), bundle);
        defOrders = TaskUtil.resolve(Arrays.asList(TaskUtil.TITLE, TaskUtil.PRIORITY, TaskUtil.RANK), bundle);
        if (orderBy == null) {
          order = new OrderBy.DESC(TaskUtil.PRIORITY);
          orderBy = TaskUtil.PRIORITY;
        }
        groupBy = groupBy == null || !defGroupBys.containsKey(groupBy) ? TaskUtil.NONE : groupBy;
      } else if ("tomorrow".equalsIgnoreCase(filter)) {        
        defGroupBys = TaskUtil.resolve(Arrays.asList(TaskUtil.NONE, TaskUtil.PROJECT, TaskUtil.LABEL), bundle);
        defOrders = TaskUtil.resolve(Arrays.asList(TaskUtil.TITLE, TaskUtil.PRIORITY, TaskUtil.RANK), bundle);
        if (orderBy == null || !defOrders.containsKey(orderBy)) {
          order = new OrderBy.DESC(TaskUtil.PRIORITY);
          orderBy = TaskUtil.PRIORITY;
        }
        groupBy = groupBy == null || !defGroupBys.containsKey(groupBy) ? TaskUtil.NONE : groupBy;
      } else if ("upcoming".equalsIgnoreCase(filter)) {        
        defGroupBys = TaskUtil.resolve(Arrays.asList(TaskUtil.NONE, TaskUtil.PROJECT, TaskUtil.LABEL), bundle);
        defOrders = TaskUtil.resolve(Arrays.asList(TaskUtil.TITLE, TaskUtil.PRIORITY, TaskUtil.DUEDATE, TaskUtil.RANK), bundle);
        groupBy = groupBy == null || !defGroupBys.containsKey(groupBy) ? TaskUtil.NONE : groupBy;
      }

      if (orderBy == null || !defOrders.containsKey(orderBy)) {
        orderBy = TaskUtil.DUEDATE;
        order = new OrderBy.ASC(TaskUtil.DUEDATE);
      }
      if (groupBy == null || !defGroupBys.containsKey(groupBy)) {
        groupBy = TaskUtil.DUEDATE;
      }
      
      Date[] filterDate = TaskUtil.convertDueDate(filter, userTimezone);
      taskQuery.setDueDateFrom(filterDate[0]);
      taskQuery.setDueDateTo(filterDate[1]);
      taskQuery.setOrderBy(Arrays.asList(order));

    }
    else if (projectId >= 0) {
      if (!advanceSearch) {
        taskQuery.setKeyword(keyword);        
      }
      if (projectId == 0) {
        defGroupBys = TaskUtil.resolve(Arrays.asList(TaskUtil.NONE, TaskUtil.ASSIGNEE, TaskUtil.PROJECT, TaskUtil.LABEL, TaskUtil.DUEDATE, TaskUtil.STATUS), bundle);

        if (orderBy == null || orderBy.isEmpty()) {
          orderBy = TaskUtil.DUEDATE;
          order = new OrderBy.ASC(orderBy);
        }
                
        if (spaceProjectIds != null) {
          taskQuery.setProjectIds(spaceProjectIds);
        } else {
          taskQuery.setProjectIds(allProjectIds);
        }
      } else {
        //. Default order by CreatedDate
        if (orderBy == null || orderBy.isEmpty()) {
          orderBy = TaskUtil.DUEDATE;
          order = new OrderBy.ASC(orderBy);
        }
        if (!noProjPermission) {
          taskQuery.setProjectIds(Arrays.asList(projectId));          
        }
      }
      taskQuery.setOrderBy(Arrays.asList(order));

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
        if (!noLblPermission) {
          taskQuery.setLabelIds(Arrays.asList(labelId));          
        }
      } else {
        defGroupBys = TaskUtil.resolve(Arrays.asList(TaskUtil.NONE, TaskUtil.PROJECT, TaskUtil.LABEL, TaskUtil.DUEDATE, TaskUtil.STATUS), bundle);
        if (groupBy == null || groupBy.isEmpty() || !defGroupBys.containsKey(groupBy)) {
          groupBy = TaskUtil.LABEL;
        }
        taskQuery.setIsLabelOf(currentUser);
      }
      
      if (spaceProjectIds != null) {
        taskQuery.setProjectIds(spaceProjectIds);
      }
      taskQuery.setOrderBy(Arrays.asList(order));
    }

    page = page == null ? 1 : page;
    if (page <= 0) {
      page = 1;
    }
    Paging paging = new Paging(page);
    
    ListAccess<Task> listTasks = null;
    //there are cases that we return empty list of tasks with-out querying to DB
    //1. In spaces, and no space project
    //2. not in spaces, no accessible project, and projectId = 0
    //3. no label permission and user is querying for tasks in that label
    //4. no project permission and user is querying for tasks in that project
    if ((spaceProjectIds != null  && spaceProjectIds.isEmpty()) || (projectId != null && projectId == 0 && allProjectIds.isEmpty()) ||
        (noLblPermission && labelId != null && labelId > 0) || (noProjPermission && projectId != null && projectId > 0)) {
      listTasks = TaskUtil.EMPTY_TASK_LIST;
    } else {
      listTasks = taskService.findTasks(taskQuery);      
    }
    paging.setTotal(ListUtil.getSize(listTasks));

    //
    if (paging.getStart() >= paging.getTotal()) {
      paging = new Paging(1);
      paging.setTotal(ListUtil.getSize(listTasks));
    }

    long countTasks = paging.getTotal();
    if (countTasks < MIN_NUMBER_TASK_GROUPABLE) {
      groupBy = TaskUtil.NONE;
    }

    //Map<GroupKey, ListAccess<Task>> groupTasks = TaskUtil.findTasks(taskService, taskQuery, groupBy, userTimezone, userService);
    Map<GroupKey, List<Task>> groupTasks = new HashMap<GroupKey, List<Task>>();
    if (countTasks >= MIN_NUMBER_TASK_GROUPABLE && groupBy != null && !groupBy.isEmpty() && !TaskUtil.NONE.equalsIgnoreCase(groupBy)) {
      groupTasks = TaskUtil.groupTasks(Arrays.asList(ListUtil.load(listTasks, paging.getStart(), paging.getNumberItemPerPage())), groupBy, currentUser, userTimezone, bundle, taskService, userService);
    }
    if (groupTasks.isEmpty()) {
      groupTasks.put(new GroupKey("", null, 0), Arrays.asList(ListUtil.load(listTasks, paging.getStart(), paging.getNumberItemPerPage())));
    }

    //TODO: this block code is not good
    // Count task by status
    Map<Long, Integer> numberTasks = new HashMap<Long, Integer>();
    if (isBoardView) {
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
    
    long taskNum = countTasks;
    long incomNum = -1;
    if (projectId == ProjectUtil.INCOMING_PROJECT_ID && advanceSearch) {
      TaskQuery q = new TaskQuery();
      q.setIsIncomingOf(currentUser);
      q.setCompleted(false);
      incomNum = ListUtil.getSize(taskService.findTasks(q));
    }

    return taskListView
        .with()
        .orders(defOrders)
        .groups(defGroupBys)
        .currentProjectId(projectId)
        .project(project)
        .projectStatuses(projectStatuses)
        .tasks(new ArrayList<Task>())
        .taskNum(taskNum)
        .incomNum(incomNum)
        .groupTasks(groupTasks)
        .keyword(keyword == null ? "" : keyword)
        .showCompleted(advanceSearch && fd.isShowCompleted())
        .groupBy(groupBy == null ? TaskUtil.NONE : groupBy)
        .orderBy(orderBy == null ? "" : orderBy)
        .filter(filter == null ? "" : filter)
        .advanceSearch(advanceSearch)
        .bundle(bundle)
        .viewType(vType)
        .userTimezone(userTimezone)
        .taskService(taskService)
        .currentUser(currentUser)
        .currentLabelId(labelId == null ? -1 : labelId)
        .currentLabelName(currentLabelName)
        .paging(paging)
        .set("userMap", new HashMap<String, User>())
        .set("numberTasksByStatus", numberTasks)
        .ok()
        .withCharset(Tools.UTF_8);
  }

  @Resource(method = HttpMethod.POST)
  @Ajax
  @MimeType.JSON
  public Response createTask(Long projectId, Long labelId, String taskInput, String filter, SecurityContext securityContext) throws EntityNotFoundException, JSONException, UnAuthorizedOperationException {

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

    Identity identity = ConversationState.getCurrent().getIdentity();
    //Project task
    if(projectId > 0) {
      Project project = projectService.getProject(projectId);
      if (!project.canView(identity)) {
        throw new UnAuthorizedOperationException(projectId, Project.class, getNoPermissionMsg());
      }
      Status status = statusService.getDefaultStatus(projectId);
      if (status == null) {
        throw new EntityNotFoundException(projectId, Project.class);
      }
      task.setStatus(status);
      //taskService.createTask(task);
      //projectService.createTaskToProjectId(projectId, task);
    } else if (labelId == null || labelId < 0) {
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
      
      //taskService.createTask(task);
    }
    
    Label label = null;
    if (labelId != null && labelId > 0) {
      label = taskService.getLabel(labelId);
      if (!label.getUsername().equals(currentUser)) {
        throw new UnAuthorizedOperationException(labelId, Label.class, getNoPermissionMsg());
      }
    }

    taskService.createTask(task);
    if (label != null) {
      taskService.addTaskToLabel(task.getId(), labelId);
    }

    JSONObject detail = JsonUtil.buildTaskJSon(task, bundle);

    JSONObject json = new JSONObject();
    json.put("id", task.getId());

    json.put("detail", detail);

    return Response.ok(json.toString()).withCharset(Tools.UTF_8);
  }

  @Resource
  @Ajax
  @MimeType.JSON
  public Response createTaskInListView(String space_group_id, String taskTitle, Long projectId, Long statusId, String assignee,
                                       String viewType, String groupBy, String orderBy, SecurityContext securityContext) throws EntityNotFoundException, UnAuthorizedOperationException {
    if (taskTitle == null || taskTitle.isEmpty()) {
      return Response.status(406).body("Task title is required");
    }
    Task task = new Task();
    task.setTitle(taskTitle);
    if (assignee != null && !assignee.isEmpty()) {
      task.setAssignee(assignee);
    }
    Identity identity = ConversationState.getCurrent().getIdentity();
    if (statusId != null) {
      Status status = statusService.getStatus(statusId);
      if (!status.getProject().canView(identity)) {
        throw new UnAuthorizedOperationException(statusId, Status.class, getNoPermissionMsg());
      }
      if (status != null) {
        task.setStatus(status);
      }
    } else if (projectId != null) {
      Project project = projectService.getProject(projectId);
      if (!project.canView(identity)) {
        throw new UnAuthorizedOperationException(projectId, Project.class, getNoPermissionMsg());
      }
      Status status = statusService.getDefaultStatus(projectId);
      if (status != null) {
        task.setStatus(status);
      }
    }

    taskService.createTask(task);

    JSONObject json = JsonUtil.buildTaskJSon(task, bundle);
    return Response.ok(json.toString()).withCharset(Tools.UTF_8);

    //spaceGrpId, projectId, currentLabelId, labelIds, statusId, dueDate, priority, assignee, completed, keyword, advanceSearch, groupby, orderBy, filter, viewType, securityContext
    //return listTasks(space_group_id, projectId, null, null, null, null, null, null, null, null, null, groupBy, orderBy, null, viewType, 1, securityContext);
  }

  @Resource(method = HttpMethod.POST)
  @Ajax
  @MimeType.HTML
  public Response removeStatus(String space_group_id, Long statusId, SecurityContext securityContext) throws EntityNotFoundException, NotAllowedOperationOnEntityException, UnAuthorizedOperationException {
    // Load status to get projectId
    Status status = statusService.getStatus(statusId);
    Identity identity = ConversationState.getCurrent().getIdentity();
    Project project = status.getProject();
    if (!project.canEdit(identity)) {
      throw new UnAuthorizedOperationException(statusId, Status.class, getNoPermissionMsg());
    }

    //
    statusService.removeStatus(statusId);

    //spaceGrpId, projectId, currentLabelId, labelIds, statusId, dueDate, priority, assignee, completed, keyword, advanceSearch, groupby, orderBy, filter, viewType, securityContext
    return listTasks(space_group_id, project.getId(), null, null, null, null, null, null, null, null, null, null, null, "board", 0, securityContext);
  }


  @Resource(method = HttpMethod.POST)
  @Ajax
  @MimeType.HTML
  public Response createStatus(String space_group_id, String name, Long projectId, SecurityContext securityContext) throws EntityNotFoundException, UnAuthorizedOperationException {
    Project project = projectService.getProject(projectId);
    if (!project.canEdit(ConversationState.getCurrent().getIdentity())) {
      throw new UnAuthorizedOperationException(projectId, Task.class, getNoPermissionMsg());
    }
    statusService.createStatus(project, name);
    //spaceGrpId, projectId, currentLabelId, labelIds, statusId, dueDate, priority, assignee, completed, keyword, advanceSearch, groupby, orderBy, filter, viewType, securityContext
    return listTasks(space_group_id, projectId, null, null, null, null, null, null, null, null, null, null, null, "board", 0, securityContext);
  }

  private long getIncomingNum(String username) {
    TaskQuery taskQuery = new TaskQuery();
    taskQuery.setIsIncomingOf(username);
    taskQuery.setCompleted(false);
    ListAccess<Task> taskListAccess = taskService.findTasks(taskQuery);
    return ListUtil.getSize(taskListAccess);
  }
}

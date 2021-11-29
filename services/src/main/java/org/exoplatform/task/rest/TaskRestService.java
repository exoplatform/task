package org.exoplatform.task.rest;

import io.swagger.annotations.*;
import org.apache.commons.lang3.StringUtils;
import org.exoplatform.commons.utils.ListAccess;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.services.rest.resource.ResourceContainer;
import org.exoplatform.services.security.ConversationState;
import org.exoplatform.services.security.Identity;
import org.exoplatform.social.core.space.model.Space;
import org.exoplatform.social.core.space.spi.SpaceService;
import org.exoplatform.task.dao.TaskQuery;
import org.exoplatform.task.dto.*;
import org.exoplatform.task.service.UserService;
import org.exoplatform.task.model.GroupKey;
import org.exoplatform.task.model.User;
import org.exoplatform.task.rest.model.*;
import org.exoplatform.task.service.*;
import org.exoplatform.task.storage.CommentStorage;
import org.exoplatform.task.util.CommentUtil;
import org.exoplatform.task.util.TaskUtil;
import org.exoplatform.task.util.UserUtil;
import org.json.JSONArray;
import org.json.JSONObject;

import javax.annotation.security.RolesAllowed;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.net.URLDecoder;
import java.util.*;
import java.util.stream.Collectors;

@Path("/tasks")
@Api(value = "/tasks", description = "Managing tasks")
@RolesAllowed("users")
public class TaskRestService implements ResourceContainer {

  private static final int DEFAULT_LIMIT = 20;

  private static final Log LOG           = ExoLogger.getLogger(TaskRestService.class);

  private TaskService      taskService;

  private CommentService   commentService;

  private ProjectService   projectService;

  private StatusService    statusService;

  private UserService      userService;

  private SpaceService     spaceService;

  private LabelService     labelService;

  private CommentStorage   commentStorage;

  private static final String PERCENT_ENCODED_REGEX = "%(?![0-9a-fA-F]{2})";

  public TaskRestService(TaskService taskService,
                         CommentService commentService,
                         ProjectService projectService,
                         StatusService statusService,
                         UserService userService,
                         SpaceService spaceService,
                         LabelService labelService) {
    this.taskService = taskService;
    this.commentService = commentService;
    this.projectService = projectService;
    this.statusService = statusService;
    this.userService = userService;
    this.spaceService = spaceService;
    this.labelService = labelService;
  }


  private enum TaskType {
    ALL, INCOMING, OVERDUE, WATCHED, COLLABORATED, ASSIGNED
  }



  @GET
  @Path("{id}")
  @Produces(MediaType.APPLICATION_JSON)
  @RolesAllowed("users")
  @ApiOperation(value = "Get task by id", httpMethod = "GET", response = Response.class, notes = "This get the task if the authenticated user has permissions to view the objects linked to this task.")
  @ApiResponses(value = { @ApiResponse(code = 200, message = "Request fulfilled"),
          @ApiResponse(code = 400, message = "Invalid query input"), @ApiResponse(code = 403, message = "Unauthorized operation"),
          @ApiResponse(code = 404, message = "Resource not found") })
  public Response getTaskById(@ApiParam(value = "Task id", required = true) @PathParam("id") long id) {
    try {
    TaskDto task = taskService.getTask(id);
    if (task == null) {
      return Response.status(Response.Status.NOT_FOUND).build();
    }
    if (!TaskUtil.hasEditPermission(taskService,task)) {
      return Response.status(Response.Status.FORBIDDEN).build();
    }
    return Response.ok(task).build();
    } catch (Exception e) {
      LOG.error("Can't get Task By Id {}", id, e);
      return Response.serverError().entity(e.getMessage()).build();
    }
  }


  @GET
  @RolesAllowed("users")
  @Produces(MediaType.APPLICATION_JSON)
  @ApiOperation(value = "Gets uncompleted tasks of the authenticated user", httpMethod = "GET", response = Response.class, notes = "This returns uncompleted tasks of the authenticated user in the following cases: <br/><ul><li>The authenticated is the creator of the task</li><li>The authenticated is the assignee of the task</li><li>The authenticated is a coworker of the task</li></ul>")
  @ApiResponses(value = { @ApiResponse(code = 200, message = "Request fulfilled") })
  public Response getTasks(@ApiParam(value = "Type of task to get (all, incoming, overdue)", required = false) @QueryParam("status") String status,
                           @ApiParam(value = "Search term", required = false) @QueryParam("q") String query,
                           @ApiParam(value = "Offset", required = false, defaultValue = "0") @QueryParam("offset") int offset,
                           @ApiParam(value = "Limit", required = false, defaultValue = "20") @QueryParam("limit") int limit,
                           @ApiParam(value = "Returning the number of tasks or not", defaultValue = "false") @QueryParam("returnSize") boolean returnSize,
                           @ApiParam(value = "Returning All Details", defaultValue = "false") @QueryParam("returnDetails") boolean returnDetails) {

      try {
      Identity currentId = ConversationState.getCurrent().getIdentity();
      String currentUser = currentId.getUserId();
      List<String> memberships = new LinkedList<>();
      memberships.addAll(UserUtil.getMemberships(currentId));

    long tasksSize;
    List<TaskDto> tasks = null;
    if (StringUtils.isBlank(query)) {
      TaskType taskType;
      try {
        taskType = TaskType.valueOf(status.toUpperCase());
      } catch (Exception e) {
        taskType = TaskType.ALL;
      }
      if (limit <= 0) {
        limit = DEFAULT_LIMIT;
      }
      switch (taskType) {
      case INCOMING: {
        tasks = taskService.getIncomingTasks(currentUser, offset, limit);
        tasksSize = taskService.countIncomingTasks(currentUser);
        break;
      }
      case OVERDUE: {
        tasks = taskService.getOverdueTasks(currentUser, limit);
        tasksSize = taskService.countOverdueTasks(currentUser);
        break;
      }
      case WATCHED: {
        tasks = taskService.getWatchedTasks(currentUser, limit);
        tasksSize = taskService.countWatchedTasks(currentUser);
        break;
      }
        case COLLABORATED: {
        tasks = taskService.getCollaboratedTasks(currentUser, limit);
        tasksSize = taskService.countCollaboratedTasks(currentUser);
        break;
      }
        case ASSIGNED: {
        tasks = taskService.getAssignedTasks(currentUser, limit);
        tasksSize = taskService.countAssignedTasks(currentUser);
        break;
      }
      default: {
        tasks = taskService.getUncompletedTasks(currentUser, limit);
        tasksSize = taskService.countUncompletedTasks(currentUser);
      }
      }
    } else {
      tasks = taskService.findTasksByMemberShips(currentUser, memberships, query, limit);
      tasksSize = taskService.countTasks(currentUser, query);
    }
        return Response.ok(new PaginatedTaskList(tasks.stream().map(task -> getTaskDetails((TaskDto) task, currentId)).collect(Collectors.toList()),tasksSize)).build();
  } catch (Exception e) {
    LOG.error("Can't Gets uncompleted tasks of the authenticated user", e);
    return Response.serverError().entity(e.getMessage()).build();
  }
  }


  @GET
  @RolesAllowed("users")
  @Path("filter")
  @Produces(MediaType.APPLICATION_JSON)
  @ApiOperation(value = "Gets  a specific task", httpMethod = "GET", response = Response.class, notes = "This returns  a specific task")
  @ApiResponses(value = { @ApiResponse(code = 200, message = "Request ful"),
          @ApiResponse(code = 401, message = "Unauthorized operation"),
          @ApiResponse(code = 404, message = "Resource not found") })
  public Response filterTasks(@ApiParam(value = "Type of task to get (all, incoming, overdue)", required = false) @QueryParam("statusDto") String status,
                              @ApiParam(value = "projectId term", required = false,defaultValue = "-2") @QueryParam("projectId") long projectId,
                              @ApiParam(value = "Search term", required = false) @QueryParam("query") String query,
                              @ApiParam(value = "dueCategory term", required = false) @QueryParam("dueCategory") String dueCategory,
                              @ApiParam(value = "priority term", required = false) @QueryParam("priority") String priority,
                              @ApiParam(value = "assignee term", required = false) @QueryParam("assignee") String assignee,
                              @ApiParam(value = "coworker term", required = false) @QueryParam("coworker") String coworker,
                              @ApiParam(value = "watchers term", required = false) @QueryParam("watcher") String watcher,
                              @ApiParam(value = "showCompletedTasks term", defaultValue = "false") @QueryParam("showCompletedTasks") boolean showCompletedTasks,
                              @ApiParam(value = "statusId term", required = false) @QueryParam("statusId") String statusId,
                              @ApiParam(value = "space_group_id term", required = false) @QueryParam("space_group_id") String space_group_id,
                              @ApiParam(value = "groupBy term", required = false) @QueryParam("groupBy") String groupBy,
                              @ApiParam(value = "orderBy term", required = false) @QueryParam("orderBy") String orderBy,
                              @ApiParam(value = "dueDate term", required = false) @QueryParam("dueDate") String dueDate,
                              @ApiParam(value = "labelId term", required = false) @QueryParam("labelId") Long labelId,
                              @ApiParam(value = "filterLabelIds term", required = false) @QueryParam("filterLabelIds") String filterLabelIds,
                              @ApiParam(value = "Offset", required = false, defaultValue = "0") @QueryParam("offset") int offset,
                              @ApiParam(value = "Limit", required = false, defaultValue = "20") @QueryParam("limit") int limit,
                              @ApiParam(value = "Returning the number of tasks or not", defaultValue = "false") @QueryParam("returnSize") boolean returnSize,
                              @ApiParam(value = "Returning All Details", defaultValue = "false") @QueryParam("returnDetails") boolean returnDetails) {

    try {
    String listId = ViewState.buildId(projectId, labelId, dueCategory);

    Identity currIdentity = ConversationState.getCurrent().getIdentity();

    if(assignee!=null && assignee.equals("ME")){
      assignee=currIdentity.getUserId();
    }

    if(coworker!=null && coworker.equals("ME")){
      coworker=currIdentity.getUserId();
    }

    if(watcher!=null && watcher.equals("ME")){
      watcher=currIdentity.getUserId();
    }

    Long statusIdLong = null;
    if (org.apache.commons.lang.StringUtils.isNotBlank(statusId)) {
      StatusDto statusDto = statusService.getStatus(Long.parseLong(statusId));
      if (statusDto == null || !statusDto.getProject().canView(currIdentity)) {
        return Response.status(Response.Status.NOT_FOUND).build();
      }
      statusIdLong=statusDto.getId();
    }
    ViewState.Filter filter = new ViewState.Filter(listId);
    filter.updateFilterData(filterLabelIds, statusId, dueDate, priority, assignee, coworker, watcher, showCompletedTasks, query);

    ProjectDto project = null;
    boolean noProjPermission = false;
    boolean advanceSearch = true;
    if ( projectId > 0) {
      project = projectService.getProject(projectId);
      if (!project.canView(currIdentity)) {
        if (advanceSearch) {
          noProjPermission = true;
        } else {
          return Response.status(Response.Status.NOT_FOUND).build();
        }
      }
    }
    boolean noLblPermission = false;
    if (labelId != null && labelId > 0) {
      LabelDto label = labelService.getLabel(labelId);
      if (!label.getUsername().equals(currIdentity.getUserId())) {
        if (advanceSearch) {
          noLblPermission = true;
        } else {
          return Response.status(Response.Status.NOT_FOUND).build();
        }
      }
    }
    String currentUser = currIdentity.getUserId();
    TimeZone userTimezone = userService.getUserTimezone(currentUser);

    if (currentUser == null || currentUser.isEmpty()) {
      return Response.status(Response.Status.NOT_FOUND).build();
    }

    TasksList tasks =  taskService.filterTasks(query, projectId, filter.getKeyword(), filter.getLabel(), filter.getDue(),  filter.getPriority(), filter.getAssignees(), filter.getCoworkers(), filter.getWatchers(), labelId, statusIdLong, currIdentity, dueCategory, space_group_id , userTimezone, filter.isShowCompleted(), advanceSearch, noProjPermission, noLblPermission, orderBy,  groupBy, offset, limit);

    Map<GroupKey, List<TaskEntity>> groupTasks = new HashMap<GroupKey, List<TaskEntity>>();
    if (groupBy != null && groupBy!= TaskUtil.DUEDATE && !groupBy.isEmpty() && !TaskUtil.NONE.equalsIgnoreCase(groupBy)) {
      groupTasks = TaskUtil.groupTasks(tasks.getListTasks().stream().map(task -> getTaskDetails((TaskDto) task, currIdentity)).collect(Collectors.toList()), groupBy, currIdentity, userTimezone, labelService, userService);
      return Response.ok(new FiltreTaskList(groupTasks)).build();
    }
    return Response.ok(new PaginatedTaskList(tasks.getListTasks().stream().map(task -> getTaskDetails((TaskDto) task, currIdentity)).collect(Collectors.toList()),tasks.getTasksSize())).build();
  } catch (Exception e) {
        LOG.error("Can't filter Tasks", e);
        return Response.serverError().entity(e.getMessage()).build();
        }
}

  @GET
  @Path("project/{id}")
  @RolesAllowed("users")
  @Produces(MediaType.APPLICATION_JSON)
  @ApiOperation(value = "Gets tasks by projectIdr", httpMethod = "GET", response = Response.class, notes = "This returns list of tasks by project")
  @ApiResponses(value = { @ApiResponse(code = 200, message = "Request fulfilled") })
  public Response getTasksByProjectId(@ApiParam(value = "Id", required = true, defaultValue = "0") @PathParam("id") Long id,
                                      @ApiParam(value = "Offset", required = false, defaultValue = "0") @QueryParam("offset") int offset,
                                      @ApiParam(value = "Limit", required = false, defaultValue = "0") @QueryParam("limit") int limit,
                                      @ApiParam(value = "Returning the Completed tasks", defaultValue = "false") @QueryParam("completed") boolean completed,
                                      @ApiParam(value = "Returning the number of tasks or not", defaultValue = "false") @QueryParam("returnSize") boolean returnSize,
                                      @ApiParam(value = "Returning All Details", defaultValue = "false") @QueryParam("returnDetails") boolean returnDetails) {
    try {
    Identity currentUser = ConversationState.getCurrent().getIdentity();
    ProjectDto project = projectService.getProject(id);
    if (project==null || !project.canView(ConversationState.getCurrent().getIdentity())) {
      return Response.status(Response.Status.UNAUTHORIZED).build();
    }
    long tasksSize;
    List<?> tasks = null;
    TaskQuery taskQuery = new TaskQuery();
    List<Long> allProjectIds = new ArrayList<Long>();
    allProjectIds.add(id);
    if (limit == 0) {
      limit = -1;
    }
    taskQuery.setProjectIds(allProjectIds);
    taskQuery.setCompleted(completed);
    tasks = taskService.findTasks(taskQuery,limit,offset);

    if (returnSize) {
      JSONObject tasksSizeJsonObject = new JSONObject();
      if (returnDetails) {
        tasksSizeJsonObject.put("tasks",
                                tasks.stream()
                                     .map(task -> getTaskDetails((TaskDto) task, currentUser))
                                     .collect(Collectors.toList()));
      } else {
        tasksSizeJsonObject.put("tasks", tasks);
      }
      return Response.ok(tasksSizeJsonObject).build();
    } else {
      if (returnDetails) {
        return Response.ok(tasks.stream().map(task -> getTaskDetails((TaskDto) task, currentUser)).collect(Collectors.toList()))
                       .build();
      }
      return Response.ok(tasks).build();
    }
        } catch (Exception e) {
        LOG.error("Can't get Tasks By ProjectId {}", id, e);
        return Response.serverError().entity(e.getMessage()).build();
        }
  }

  @POST
  @Produces(MediaType.APPLICATION_JSON)
  @RolesAllowed("users")
  @ApiOperation(value = "Add a new task", httpMethod = "POST", response = Response.class, notes = "This adds a new task.")
  @ApiResponses(value = { @ApiResponse(code = 200, message = "Request fulfilled"),
      @ApiResponse(code = 400, message = "Invalid query input"), @ApiResponse(code = 403, message = "Unauthorized operation"),
      @ApiResponse(code = 404, message = "Resource not found") })
  public Response addTask(@ApiParam(value = "task object to be updated", required = true) TaskDto task) {

    if (task == null) {
      return Response.status(Response.Status.BAD_REQUEST).build();
    }
    try {
    Identity identity = ConversationState.getCurrent().getIdentity();
    task.setCreatedBy(identity.getUserId());
    task.setCreatedTime(new Date());
    if(task.getStatus()==null||task.getStatus().getProject()==null){
      if (task.getAssignee() == null) {
        task.setAssignee(identity.getUserId());
      }
      task.setStatus(null);
    }
    if(task.getStatus()!=null&&(task.getStatus().getId()==null||task.getStatus().getId()==0)&&task.getStatus().getProject()!=null) {
      long projectId = task.getStatus().getProject().getId();
      ProjectDto projectDto = projectService.getProject(projectId);
      if(projectDto==null){
        LOG.warn("Task's project not found");
        return Response.status(Response.Status.BAD_REQUEST).build();
      }
      if (!projectService.getProject(projectId).canView(identity)) {
        LOG.warn("User {} attempts to create a task under a non authorized project {}", identity.getUserId(), projectDto.getName());
        return Response.status(Response.Status.UNAUTHORIZED).build();
      }

      task.setStatus(statusService.getDefaultStatus(projectId));
    }
    task = taskService.createTask(task);
    return Response.ok(task).build();
        } catch (Exception e) {
        LOG.error("Can't add Task", e);
        return Response.serverError().entity(e.getMessage()).build();
        }
  }

  @POST
  @Path("clone/{taskId}")
  @Produces(MediaType.APPLICATION_JSON)
  @RolesAllowed("users")
  @ApiOperation(value = "Clones a specific task by id", httpMethod = "POST", response = Response.class, notes = "This clones the task if the authenticated user has permissions to edit the objects linked to this task.")
  @ApiResponses(value = {@ApiResponse(code = 200, message = "Request fulfilled"),
          @ApiResponse(code = 400, message = "Invalid query input"), @ApiResponse(code = 403, message = "Unauthorized operation"),
          @ApiResponse(code = 404, message = "Resource not found")})
  public Response cloneTask(@ApiParam(value = "Task id", required = true) @PathParam("taskId") long taskId) {
    try {
    TaskDto task = taskService.getTask(taskId);
    if (task == null) {
      return Response.status(Response.Status.BAD_REQUEST).build();
    }
    if (!TaskUtil.hasEditPermission(taskService,task)) {
      return Response.status(Response.Status.FORBIDDEN).build();
    }
    TaskDto newTask = taskService.cloneTask(taskId);
    if (newTask == null) {
      return Response.status(Response.Status.NOT_FOUND).build();
    }
    
    return Response.ok(newTask).build();
        } catch (Exception e) {
        LOG.error("Can't clone Task {}", taskId, e);
        return Response.serverError().entity(e.getMessage()).build();
        }
  }

  @PUT
  @Path("{id}")
  @Produces(MediaType.APPLICATION_JSON)
  @RolesAllowed("users")
  @ApiOperation(value = "Updates a specific task by id", httpMethod = "PUT", response = Response.class, notes = "This updates the task if the authenticated user has permissions to view the objects linked to this task.")
  @ApiResponses(value = { @ApiResponse(code = 200, message = "Request fulfilled"),
      @ApiResponse(code = 400, message = "Invalid query input"), @ApiResponse(code = 403, message = "Unauthorized operation"),
      @ApiResponse(code = 404, message = "Resource not found") })
  public Response updateTaskById(@ApiParam(value = "Task id", required = true) @PathParam("id") long id,
                                 @ApiParam(value = "task object to be updated", required = true) TaskDto updatedTask) {
    if (updatedTask == null) {
      return Response.status(Response.Status.BAD_REQUEST).build();
    }
    try {
    TaskDto task = taskService.getTask(id);
    if (task == null) {
      return Response.status(Response.Status.NOT_FOUND).build();
    }
    if (!TaskUtil.hasEditPermission(taskService,task)) {
      return Response.status(Response.Status.FORBIDDEN).build();
    }
    task = taskService.updateTask(updatedTask);
    return Response.ok(task).build();
        } catch (Exception e) {
        LOG.error("Can't update Task {}", id, e);
        return Response.serverError().entity(e.getMessage()).build();
        }
  }


  @DELETE
  @Path("{id}")
  @Produces(MediaType.APPLICATION_JSON)
  @RolesAllowed("users")
  @ApiOperation(value = "Updates a specific task by id", httpMethod = "PUT", response = Response.class, notes = "This updates the task if the authenticated user has permissions to view the objects linked to this task.")
  @ApiResponses(value = { @ApiResponse(code = 200, message = "Request fulfilled"),
      @ApiResponse(code = 400, message = "Invalid query input"), @ApiResponse(code = 403, message = "Unauthorized operation"),
      @ApiResponse(code = 404, message = "Resource not found") })
  public Response deleteTaskById(@ApiParam(value = "Task id", required = true) @PathParam("id") long id) {
    try {
    TaskDto task = taskService.getTask(id);
    if (task == null) {
      return Response.status(Response.Status.NOT_FOUND).build();
    }
    if (!TaskUtil.hasEditPermission(taskService,task)) {
      return Response.status(Response.Status.FORBIDDEN).build();
    }
    taskService.removeTask(id);
    return Response.ok().build();
        } catch (Exception e) {
        LOG.error("Can't delete Task {}", id, e);
        return Response.serverError().entity(e.getMessage()).build();
        }
  }


  @GET
  @Path("labels")
  @RolesAllowed("users")
  @Produces(MediaType.APPLICATION_JSON)
  @ApiOperation(value = "Gets labels of the authenticated user", httpMethod = "GET", response = Response.class, notes = "This returns labels of the authenticated user")
  @ApiResponses(value = { @ApiResponse(code = 200, message = "Request fulfilled") })
  public Response getLabels() {
    try {
    String currentUser = ConversationState.getCurrent().getIdentity().getUserId();
    return Response.ok(labelService.findLabelsByUser(currentUser, 0, -1)).build();
        } catch (Exception e) {
        LOG.error("Can't get Labels", e);
        return Response.serverError().entity(e.getMessage()).build();
        }
  }
  @GET
  @Path("labels/project/{id}")
  @RolesAllowed("users")
  @Produces(MediaType.APPLICATION_JSON)
  @ApiOperation(value = "Gets labels of the given project", httpMethod = "GET", response = Response.class, notes = "This returns labels of the given project")
  @ApiResponses(value = { @ApiResponse(code = 200, message = "Request fulfilled") })
  public Response getLabelsByProjectId(@ApiParam(value = "project id", required = true) @PathParam("id") long id) {
    try {
      Identity currentUser = ConversationState.getCurrent().getIdentity();
      ProjectDto project = projectService.getProject(id);
      if (project == null) {
        return Response.status(Response.Status.NOT_FOUND).build();
      }
      if (!project.canView(currentUser)) {
        return Response.status(Response.Status.FORBIDDEN).build();
      }
    return Response.ok(labelService.findLabelsByProject(id, currentUser,0, -1)).build();
        } catch (Exception e) {
        LOG.error("Can't get Labels", e);
        return Response.serverError().entity(e.getMessage()).build();
        }
  }

  @GET
  @Path("labels/{id}")
  @RolesAllowed("users")
  @Produces(MediaType.APPLICATION_JSON)
  @ApiOperation(value = "Gets labels of a specific task by id", httpMethod = "GET", response = Response.class, notes = "This returns labels of a specific task by id")
  @ApiResponses(value = { @ApiResponse(code = 200, message = "Request fulfilled"),
      @ApiResponse(code = 500, message = "Internal server error") })
  public Response getLabelsByTaskId(@ApiParam(value = "Task id", required = true) @PathParam("id") long id) {
    try {
    Identity currentUser = ConversationState.getCurrent().getIdentity();
      TaskDto task = taskService.getTask(id);
      if (task == null) {
        return Response.status(Response.Status.NOT_FOUND).build();
      }
    List<LabelDto> labels = new ArrayList<>();
    if(task.getStatus()!=null && task.getStatus().getProject()!=null){
      labels = labelService.findLabelsByTask(task, task.getStatus().getProject().getId(), currentUser,0, -1);
    }
    return Response.ok(labels).build();
        } catch (Exception e) {
        LOG.error("Can't get Labels By TaskId {}", id, e);
        return Response.serverError().entity(e.getMessage()).build();
        }
  }

  @POST
  @Path("labels/{id}")
  @RolesAllowed("users")
  @ApiOperation(value = "Adds a specific task by id to a label", httpMethod = "POST", response = Response.class, notes = "This adds a specific task by id to a label")
  @ApiResponses(value = { @ApiResponse(code = 200, message = "Request fulfilled"),
      @ApiResponse(code = 400, message = "Invalid query input"), @ApiResponse(code = 403, message = "Unauthorized operation"),
      @ApiResponse(code = 404, message = "Resource not found") })
  public Response addTaskToLabel(@ApiParam(value = "label", required = true) LabelDto addedLabel,
                                 @ApiParam(value = "Task id", required = true) @PathParam("id") long id) {
    try {
    Identity currentUser = ConversationState.getCurrent().getIdentity();
    if (addedLabel == null) {
      return Response.status(Response.Status.BAD_REQUEST).build();
    }
    TaskDto task = taskService.getTask(id);
    if (task == null) {
      return Response.status(Response.Status.NOT_FOUND).build();
    }
    if (!TaskUtil.hasEditPermission(taskService,task)) {
      return Response.status(Response.Status.FORBIDDEN).build();
    }
      try {
        ProjectDto project = projectService.getProject(task.getStatus().getProject().getId());
        if (project == null) {
          return Response.status(Response.Status.NOT_FOUND).build();
        }
        if (!project.canView(currentUser)) {
          return Response.status(Response.Status.FORBIDDEN).build();
        }
        addedLabel.setProject(project);
      }catch (Exception e) {
        return Response.status(Response.Status.BAD_REQUEST).build();
      }
    if (addedLabel.getId() == 0) {// Create a new label and add a task to it

      addedLabel.setUsername(currentUser.getUserId());
      LabelDto label = labelService.createLabel(addedLabel);
      labelService.addTaskToLabel(task, label.getId());
    } else {// Add a task to an existing label
      labelService.addTaskToLabel(task, addedLabel.getId());
    }
    return Response.ok(addedLabel).build();
        } catch (Exception e) {
        LOG.error("Can't add Task {} To Label", id, e);
        return Response.serverError().entity(e.getMessage()).build();
        }
  }

  @DELETE
  @Path("labels/{id}/{labelId}")
  @RolesAllowed("users")
  @Produces(MediaType.APPLICATION_JSON)
  @ApiOperation(value = "Deletes a specific task association to a specific label", httpMethod = "DELETE", response = Response.class, notes = "This deletes a specific task association to a specific label")
  @ApiResponses(value = { @ApiResponse(code = 200, message = "Request fulfilled"),
      @ApiResponse(code = 403, message = "Unauthorized operation"), @ApiResponse(code = 404, message = "Resource not found") })
  public Response removeTaskFromLabel(@ApiParam(value = "label id", required = true) @PathParam("labelId") long labelId,
                                      @ApiParam(value = "Task id", required = true) @PathParam("id") long id) {
    try {
    TaskDto task = taskService.getTask(id);
    if (task == null) {
      return Response.status(Response.Status.NOT_FOUND).build();
    }
    LabelDto label = labelService.getLabel(labelId);
    if (label == null) {
      return Response.status(Response.Status.NOT_FOUND).build();
    }
    if (!TaskUtil.hasEditPermission(taskService,task)) {
      return Response.status(Response.Status.FORBIDDEN).build();
    }
    labelService.removeTaskFromLabel(task, labelId);
    return Response.ok().build();
        } catch (Exception e) {
        LOG.error("Can't remove Task {} From Label {}", id, labelId, e);
        return Response.serverError().entity(e.getMessage()).build();
        }
  }

  @POST
  @Path("labels")
  @RolesAllowed("users")
  @ApiOperation(value = "Adds a specific task by id to a label", httpMethod = "POST", response = Response.class, notes = "This adds a specific task by id to a label")
  @ApiResponses(value = { @ApiResponse(code = 200, message = "Request fulfilled"),
      @ApiResponse(code = 400, message = "Invalid query input"), @ApiResponse(code = 403, message = "Unauthorized operation"),
      @ApiResponse(code = 404, message = "Resource not found") })
  public Response addLabel(@ApiParam(value = "label", required = true) LabelDto addedLabel) {
    try {
    Identity currentUser = ConversationState.getCurrent().getIdentity();
    if (addedLabel == null) {
      return Response.status(Response.Status.BAD_REQUEST).build();
    }

      try {
        ProjectDto project = projectService.getProject(addedLabel.getProject().getId());
        if (project == null) {
          return Response.status(Response.Status.NOT_FOUND).build();
        }
        if (!project.canEdit(currentUser)) {
          return Response.status(Response.Status.FORBIDDEN).build();
        }
      }catch (Exception e) {
        return Response.status(Response.Status.BAD_REQUEST).build();
      }
    if (addedLabel.getId() == 0) {// Create a new label and add a task to it
      addedLabel.setUsername(currentUser.getUserId());
      LabelDto label = labelService.createLabel(addedLabel);
    }
    return Response.ok(addedLabel).build();
        } catch (Exception e) {
        LOG.error("Can't add  Label", e);
        return Response.serverError().entity(e.getMessage()).build();
        }
  }
  @PUT
  @Path("labels/{labelId}")
  @RolesAllowed("users")
  @ApiOperation(value = "Adds a specific task by id to a label", httpMethod = "POST", response = Response.class, notes = "This adds a specific task by id to a label")
  @ApiResponses(value = { @ApiResponse(code = 200, message = "Request fulfilled"),
      @ApiResponse(code = 400, message = "Invalid query input"), @ApiResponse(code = 403, message = "Unauthorized operation"),
      @ApiResponse(code = 404, message = "Resource not found") })
  public Response editLabel(@ApiParam(value = "label id", required = true) @PathParam("labelId") long labelId,
                            @ApiParam(value = "label", required = true) LabelDto label) {
    try {
    Identity currentUser = ConversationState.getCurrent().getIdentity();
    if (label == null) {
      return Response.status(Response.Status.BAD_REQUEST).build();
    }

      try {
        ProjectDto project = projectService.getProject(label.getProject().getId());
        if (project == null) {
          return Response.status(Response.Status.NOT_FOUND).build();
        }
        if (!project.canEdit(currentUser)) {
          return Response.status(Response.Status.FORBIDDEN).build();
        }
      }catch (Exception e) {
        return Response.status(Response.Status.BAD_REQUEST).build();
      }
    if (labelService.getLabel(labelId)==null) {
      return Response.status(Response.Status.NOT_FOUND).build();
    }
      label = labelService.updateLabel(label);
    return Response.ok(label).build();
        } catch (Exception e) {
        LOG.error("Can't add  Label", e);
        return Response.serverError().entity(e.getMessage()).build();
        }
  }

  @DELETE
  @Path("labels/{labelId}")
  @RolesAllowed("users")
  @Produces(MediaType.APPLICATION_JSON)
  @ApiOperation(value = "Deletes a specific  label", httpMethod = "DELETE", response = Response.class, notes = "This deletes a specific task association to a specific label")
  @ApiResponses(value = { @ApiResponse(code = 200, message = "Request fulfilled"),
      @ApiResponse(code = 403, message = "Unauthorized operation"), @ApiResponse(code = 404, message = "Resource not found") })
  public Response removeLabel(@ApiParam(value = "label id", required = true) @PathParam("labelId") long labelId) {
    try {
      Identity currentUser = ConversationState.getCurrent().getIdentity();
    LabelDto label = labelService.getLabel(labelId);
    if (label == null) {
      return Response.status(Response.Status.NOT_FOUND).build();
    }
      try {
        ProjectDto project = projectService.getProject(label.getProject().getId());
        if (project == null) {
          return Response.status(Response.Status.NOT_FOUND).build();
        }
        if (!project.canEdit(currentUser)) {
          return Response.status(Response.Status.FORBIDDEN).build();
        }
      }catch (Exception e) {
        return Response.status(Response.Status.BAD_REQUEST).build();
      }
    labelService.removeLabel(labelId);
    return Response.ok().build();
        } catch (Exception e) {
        LOG.error("Can't remove Label {}",  labelId, e);
        return Response.serverError().entity(e.getMessage()).build();
        }
  }

  @GET
  @Path("logs/{id}")
  @RolesAllowed("users")
  @Produces(MediaType.APPLICATION_JSON)
  @ApiOperation(value = "Gets a logs of a specific task", httpMethod = "GET", response = Response.class, notes = "This returns a logs of a specific task")
  @ApiResponses(value = { @ApiResponse(code = 200, message = "Request fulfilled"),
      @ApiResponse(code = 401, message = "Unauthorized operation"), @ApiResponse(code = 404, message = "Resource not found") })
  public Response getTaskLogs(@ApiParam(value = "Task id", required = true) @PathParam("id") long id,
                              @ApiParam(value = "Offset", required = false, defaultValue = "0") @QueryParam("offset") int offset,
                              @ApiParam(value = "Limit", required = false, defaultValue = "-1") @QueryParam("limit") int limit) {
    try {
    TaskDto task = taskService.getTask(id);
    if (task == null) {
      return Response.status(Response.Status.NOT_FOUND).build();
    }
    if (!TaskUtil.hasViewPermission(taskService,task)) {
      return Response.status(Response.Status.UNAUTHORIZED).build();
    }
    if (limit == 0) {
      limit = -1;
    }
    List<ChangeLogEntry> arr = taskService.getTaskLogs(id, offset, limit);
    if (arr == null) {
      return Response.ok(Collections.emptyList()).build();
    }
    List<ChangeLogEntry> logs = new LinkedList<ChangeLogEntry>(arr);
    return Response.ok(logs).build();
        } catch (Exception e) {
        LOG.error("Can't get Task {} Logs", id, e);
        return Response.serverError().entity(e.getMessage()).build();
        }
  }

  @GET
  @Path("comments/{id}")
  @RolesAllowed("users")
  @Produces(MediaType.APPLICATION_JSON)
  @ApiOperation(value = "Gets a comment list of a specific task", httpMethod = "GET", response = Response.class, notes = "This returns a comment list of a specific task")
  @ApiResponses(value = { @ApiResponse(code = 200, message = "Request fulfilled"),
      @ApiResponse(code = 403, message = "Unauthorized operation"), @ApiResponse(code = 404, message = "Resource not found") })
  public Response getTaskComments(@ApiParam(value = "Task id", required = true) @PathParam("id") long id,
                                  @ApiParam(value = "Offset", required = false, defaultValue = "0") @QueryParam("offset") int offset,
                                  @ApiParam(value = "Limit", required = false, defaultValue = "-1") @QueryParam("limit") int limit) {
    try {
    TaskDto task = taskService.getTask(id);
    if (task == null) {
      return Response.status(Response.Status.NOT_FOUND).build();
    }
    if (!TaskUtil.hasViewPermission(taskService,task)) {
      return Response.status(Response.Status.UNAUTHORIZED).build();
    }
    if (limit == 0) {
      limit = -1;
    }
    String currentUser = ConversationState.getCurrent().getIdentity().getUserId();
    List<CommentDto> comments = commentService.getCommentsWithSubs(id, offset, limit);
    List<CommentEntity> commentModelsList = new ArrayList<CommentEntity>();
    for (CommentDto comment : comments) {
      CommentEntity commentModel = addCommentModel(comment, commentModelsList, TaskUtil.getUserLanguage(currentUser));
      if (comment.getSubComments()!=null&&!comment.getSubComments().isEmpty()) {
        List<CommentEntity> subCommentsModelsList = new ArrayList<>();
        for (CommentDto subComment :comment.getSubComments()) {
          addCommentModel(subComment, subCommentsModelsList, TaskUtil.getUserLanguage(currentUser));
        }
        commentModel.setSubComments(subCommentsModelsList);
      }
    }
    Collections.reverse(commentModelsList);
    return Response.ok(commentModelsList).build();
        } catch (Exception e) {
        LOG.error("Can't get Task {} Comments", id, e);
        return Response.serverError().entity(e.getMessage()).build();
        }
  }

  @POST
  @Path("comments/{id}")
  @RolesAllowed("users")
  @ApiOperation(value = "Adds comment to a specific task by id", httpMethod = "POST", response = Response.class, notes = "This Adds comment to a specific task by id")
  @ApiResponses(value = { @ApiResponse(code = 200, message = "Request fulfilled"),
      @ApiResponse(code = 400, message = "Invalid query input"), @ApiResponse(code = 403, message = "Unauthorized operation"),
      @ApiResponse(code = 404, message = "Resource not found") })
  public Response addTaskComment(@ApiParam(value = "Comment text", required = true) String commentText,
                                 @ApiParam(value = "Task id", required = true) @PathParam("id") long id) {
    try {
    String currentUser = ConversationState.getCurrent().getIdentity().getUserId();
    TaskDto task = taskService.getTask(id);
    if (task == null) {
      return Response.status(Response.Status.NOT_FOUND).build();
    }
    if (commentText == null || commentText.isEmpty()) {
      return Response.status(Response.Status.BAD_REQUEST).build();
    }
    if (!TaskUtil.hasEditPermission(taskService,task)) {
      return Response.status(Response.Status.FORBIDDEN).build();
    }
    commentText = commentText.replaceAll(PERCENT_ENCODED_REGEX, "%25");
    commentText = commentText.replaceAll("\\+", "%2b");
    commentText = URLDecoder.decode(commentText, "UTF-8");
    CommentDto addedComment = commentService.addComment(task, currentUser, commentText);
    if (addedComment != null) {
      addedComment = commentService.getComment(addedComment.getId());
    }
    CommentEntity commentEntity = new CommentEntity(addedComment, userService.loadUser(currentUser), CommentUtil.formatMention(commentText, TaskUtil.getUserLanguage(currentUser), userService));
    return Response.ok(commentEntity).build();
        } catch (Exception e) {
        LOG.error("Can't add Comment to Task {}", id, e);
        return Response.serverError().entity(e.getMessage()).build();
        }
  }

  @POST
  @Path("comments/{commentId}/{id}")
  @RolesAllowed("users")
  @ApiOperation(value = "Adds a sub comment to a specific parent comment by id and a specific task by id", httpMethod = "POST", response = Response.class, notes = "This Adds sub comment to a parent comment in specific task by id")
  @ApiResponses(value = { @ApiResponse(code = 200, message = "Request fulfilled"),
      @ApiResponse(code = 400, message = "Invalid query input"), @ApiResponse(code = 403, message = "Unauthorized operation"),
      @ApiResponse(code = 404, message = "Resource not found") })
  public Response addTaskSubComment(@ApiParam(value = "Comment text", required = true) String commentText,
                                    @ApiParam(value = "Comment id", required = true) @PathParam("commentId") long commentId,
                                    @ApiParam(value = "Task id", required = true) @PathParam("id") long id) {
    try {
    String currentUser = ConversationState.getCurrent().getIdentity().getUserId();
    TaskDto task = taskService.getTask(id);
    if (task == null) {
      return Response.status(Response.Status.NOT_FOUND).build();
    }
    if (!TaskUtil.hasEditPermission(taskService,task)) {
      return Response.status(Response.Status.FORBIDDEN).build();
    }
    if (commentText == null || commentText.isEmpty()) {
      return Response.status(Response.Status.BAD_REQUEST).build();
    }

    commentText = commentText.replaceAll(PERCENT_ENCODED_REGEX, "%25");
    commentText = commentText.replaceAll("\\+", "%2b");
    commentText = URLDecoder.decode(commentText, "UTF-8");
    CommentDto addedComment = commentService.addComment(task, commentId, currentUser, commentText);
    if (addedComment != null) {
      addedComment = commentService.getComment(addedComment.getId());
    }
    CommentEntity commentEntity = new CommentEntity(addedComment, userService.loadUser(currentUser), CommentUtil.formatMention(commentText, TaskUtil.getUserLanguage(currentUser), userService));
    return Response.ok(commentEntity).build();
        } catch (Exception e) {
        LOG.error("Can't add SubComment to Task {}", id, e);
        return Response.serverError().entity(e.getMessage()).build();
        }
  }

  @DELETE
  @Path("comments/{commentId}")
  @RolesAllowed("users")
  @ApiOperation(value = "Deletes a specific task comment by id", httpMethod = "DELETE", response = Response.class, notes = "This deletes a specific task comment by id")
  @ApiResponses(value = { @ApiResponse(code = 200, message = "Request fulfilled"),
      @ApiResponse(code = 403, message = "Unauthorized operation"), @ApiResponse(code = 404, message = "Resource not found") })
  public Response deleteComment(@ApiParam(value = "Comment id", required = true) @PathParam("commentId") long commentId) {
    try {
    CommentDto comment = commentService.getComment(commentId);
    if (comment == null) {
      return Response.status(Response.Status.NOT_FOUND).build();
    }
    Identity currentIdentity = ConversationState.getCurrent().getIdentity();
    if (!TaskUtil.canDeleteComment(currentIdentity, comment)) {
      return Response.status(Response.Status.FORBIDDEN).build();
    }
    commentService.removeComment(commentId);
    return Response.ok(comment).build();
        } catch (Exception e) {
        LOG.error("Can't delete Comment {}", commentId, e);
        return Response.serverError().entity(e.getMessage()).build();
        }
  }

  @GET
  @Path("usersToMention/{query}")
  @RolesAllowed("users")
  @Produces(MediaType.APPLICATION_JSON)
  @ApiOperation(value = "Gets users to mention in comment", httpMethod = "GET", response = Response.class, notes = "This returns users to mention in comment")
  @ApiResponses(value = { @ApiResponse(code = 200, message = "Request fulfilled") })
  public Response findUsersToMention(@ApiParam(value = "Query", required = true) @PathParam("query") String query) {
    try {
    ListAccess<User> list = userService.findUserByName(query);
    JSONArray usersJsonArray = new JSONArray();
    for (User user : list.load(0, UserUtil.SEARCH_LIMIT)) {
      JSONObject userJson = new JSONObject();
      String fullName = user.getDisplayName();
      if(taskService.isExternal(user.getUsername())){
        fullName += " " + "(" + TaskUtil.getResourceBundleLabel(new Locale(TaskUtil.getUserLanguage(user.getUsername())), "external.label.tag") + ")";
      }
      userJson.put("id", "@" + user.getUsername());
      userJson.put("name", fullName);
      userJson.put("avatar", user.getAvatar());
      userJson.put("type", "contact");
      usersJsonArray.put(userJson);
    }
    return Response.ok(usersJsonArray.toString()).build();
        } catch (Exception e) {
        LOG.error("Can't findUsersToMention", e);
        return Response.serverError().entity(e.getMessage()).build();
        }
  }

  @PUT
  @Path("updateCompleted/{idTask}")
  @Produces(MediaType.APPLICATION_JSON)
  @RolesAllowed("users")
  @ApiOperation(value = "Updates a specific task by id", httpMethod = "PUT", response = Response.class, notes = "This updates the task if the authenticated user has permissions to view the objects linked to this task.")
  @ApiResponses(value = { @ApiResponse(code = 200, message = "Request fulfilled"),
          @ApiResponse(code = 400, message = "Invalid query input"), @ApiResponse(code = 403, message = "Unauthorized operation"),
          @ApiResponse(code = 404, message = "Resource not found") })
  public Response updateCompleted(@ApiParam(value = "Task id", required = true) @PathParam("idTask") long idTask,
                                  @ApiParam(value = "isCompleted", defaultValue = "false") @QueryParam("isCompleted") boolean isCompleted) {
    try {
    TaskDto task = taskService.getTask(idTask);
    if (!TaskUtil.hasEditPermission(taskService,task)) {
      return Response.status(Response.Status.FORBIDDEN).build();
    }
    task.setCompleted(isCompleted);
    taskService.updateTask(task);

    return Response.ok(task).build();
        } catch (Exception e) {
        LOG.error("Can't set task {} as Completed", idTask, e);
        return Response.serverError().entity(e.getMessage()).build();
        }
  }


  private CommentEntity addCommentModel(CommentDto comment, List<CommentEntity> commentModelsList, String lang) {

    User user = userService.loadUser(comment.getAuthor());
    StatusDto taskStatus = comment.getTask().getStatus();
    if (taskStatus != null) {
      comment.getTask().setStatus(taskStatus.clone());// To be checked
    }
    CommentEntity commentEntity = new CommentEntity(comment, user, CommentUtil.formatMention(comment.getComment(), lang, userService));
    if (commentEntity.getSubComments() == null) {
      commentEntity.setSubComments(new ArrayList<>());
    }
    commentModelsList.add(commentEntity);
    return commentEntity;
  }


  private TaskEntity getTaskDetails(TaskDto task, Identity userIdentity) {

    long taskId = task.getId();
    int commentCount;
    try {
      commentCount = commentService.countComments(taskId);
    } catch (Exception e) {
      LOG.warn("Error retrieving task '{}' comments count", taskId, e);
      commentCount = 0;
    }
    TaskEntity taskEntity = new TaskEntity(((TaskDto) task), commentCount);
    if (task.getStatus() != null && task.getStatus().getProject() != null) {
      List<LabelDto> labels = new ArrayList<>();
      try {
        labels = labelService.findLabelsByTask(task, task.getStatus().getProject().getId(), userIdentity,0, -1);
      } catch (Exception e) {
        LOG.warn("Error retrieving task '{}' labels", taskId, e);
      }
      SpaceEntity space = getProjectSpace(task.getStatus().getProject());
      taskEntity.setLabels(labels);
      taskEntity.setSpace(space);
    }
    return taskEntity;
  }

  private SpaceEntity getProjectSpace(ProjectDto project) {
    for (String permission : projectService.getManager(project.getId())) {
      int index = permission.indexOf(':');
      if (index > -1) {
        String groupId = permission.substring(index + 1);
        Space space = spaceService.getSpaceByGroupId(groupId);
        if(space!=null){
          return new SpaceEntity(space.getId(), space.getDisplayName(), space.getGroupId(), space.getUrl(), space.getPrettyName(), space.getAvatarUrl());
        }
      }
    }
    return null;
  }
}

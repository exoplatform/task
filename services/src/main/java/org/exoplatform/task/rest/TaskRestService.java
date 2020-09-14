package org.exoplatform.task.rest;

import java.net.URLDecoder;
import java.util.*;
import java.util.stream.Collectors;

import javax.annotation.security.RolesAllowed;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.apache.commons.lang3.StringUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import org.exoplatform.commons.utils.CommonsUtils;
import org.exoplatform.commons.utils.ListAccess;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.services.rest.resource.ResourceContainer;
import org.exoplatform.services.security.ConversationState;
import org.exoplatform.services.security.Identity;
import org.exoplatform.social.core.space.model.Space;
import org.exoplatform.social.core.space.spi.SpaceService;
import org.exoplatform.task.domain.Project;
import org.exoplatform.task.domain.Status;
import org.exoplatform.task.dto.*;
import org.exoplatform.task.exception.EntityNotFoundException;
import org.exoplatform.task.legacy.service.UserService;
import org.exoplatform.task.model.User;
import org.exoplatform.task.rest.model.CommentEntity;
import org.exoplatform.task.rest.model.TaskEntity;
import org.exoplatform.task.service.*;
import org.exoplatform.task.storage.CommentStorage;
import org.exoplatform.task.util.*;

import io.swagger.annotations.*;

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
    ALL, INCOMING, OVERDUE
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
                           @ApiParam(value = "Returning All Details", defaultValue = "false") @QueryParam("returnDetails") boolean returnDetails) throws Exception {
    String currentUser = ConversationState.getCurrent().getIdentity().getUserId();

    long tasksSize;
    List<?> tasks = null;
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
      default: {
        tasks = taskService.getUncompletedTasks(currentUser, limit);
        tasksSize = taskService.countUncompletedTasks(currentUser);
      }
      }
    } else {
      tasks = taskService.findTasks(currentUser, query, limit);
      tasks = tasks.stream().map(task -> {
        long taskId = ((TaskDto) task).getId();
        int commentCount;
        try {
          commentCount = commentService.countComments(taskId);
        } catch (Exception e) {
          LOG.warn("Error retrieving task '{}' comments count", taskId, e);
          commentCount = 0;
        }
        return new TaskEntity(((TaskDto) task), commentCount);
      }).collect(Collectors.toList());
      tasksSize = taskService.countTasks(currentUser, query);
    }

    if (returnSize) {
      JSONObject tasksSizeJsonObject = new JSONObject();
      tasksSizeJsonObject.put("size", tasksSize);
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
                                 @ApiParam(value = "task object to be updated", required = true) TaskDto updatedTask) throws Exception {
    if (updatedTask == null) {
      return Response.status(Response.Status.BAD_REQUEST).build();
    }
    TaskDto task = taskService.getTask(id);
    if (task == null) {
      return Response.status(Response.Status.NOT_FOUND).build();
    }
    if (!TaskUtil.hasEditPermission(task)) {
      return Response.status(Response.Status.FORBIDDEN).build();
    }
    task = taskService.updateTask(updatedTask);
    return Response.ok(task).build();
  }

  @GET
  @Path("projects")
  @RolesAllowed("users")
  @Produces(MediaType.APPLICATION_JSON)
  @ApiOperation(value = "Gets projects", httpMethod = "GET", response = Response.class, notes = "This returns projects of the authenticated user")
  @ApiResponses(value = { @ApiResponse(code = 200, message = "Request fulfilled"),
      @ApiResponse(code = 500, message = "Internal server error") })
  public Response getProjects() {
    List<ProjectDto> projects = ProjectUtil.getProjectTree(null, projectService);
    JSONArray projectsJsonArray = new JSONArray();
    try {
      projectsJsonArray = buildJSON(projectsJsonArray, projects);
    } catch (Exception e) {
      LOG.error("Error getting projects", e);
      return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
    }
    return Response.ok(projectsJsonArray.toString()).build();
  }

  @GET
  @Path("projects/status/{id}")
  @RolesAllowed("users")
  @Produces(MediaType.APPLICATION_JSON)
  @ApiOperation(value = "Gets the default status by project id", httpMethod = "GET", response = Response.class, notes = "This returns the default status by project id")
  @ApiResponses(value = { @ApiResponse(code = 200, message = "Request fulfilled"),
      @ApiResponse(code = 403, message = "Unauthorized operation"), @ApiResponse(code = 500, message = "Internal server error") })
  public Response getDefaultStatusByProjectId(@ApiParam(value = "Project id", required = true) @PathParam("id") long id) throws EntityNotFoundException {
    Identity currentUser = ConversationState.getCurrent().getIdentity();
    ProjectDto project = projectService.getProject(id);
    if (project == null) {
      return Response.status(Response.Status.NOT_FOUND).build();
    }
    if (!project.canView(currentUser)) {
      return Response.status(Response.Status.UNAUTHORIZED).build();
    }
    StatusDto status = statusService.getDefaultStatus(id);
    return Response.ok(status).build();
  }

  @GET
  @Path("projects/statuses/{id}")
  @RolesAllowed("users")
  @Produces(MediaType.APPLICATION_JSON)
  @ApiOperation(value = "Gets the statuses by project id", httpMethod = "GET", response = Response.class, notes = "This returns the statuses by project id")
  @ApiResponses(value = { @ApiResponse(code = 200, message = "Request fulfilled"),
      @ApiResponse(code = 403, message = "Unauthorized operation"), @ApiResponse(code = 404, message = "Resource not found") })
  public Response getStatusesByProjectId(@ApiParam(value = "Project id", required = true) @PathParam("id") long id) throws EntityNotFoundException {
    Identity currentUser = ConversationState.getCurrent().getIdentity();
    ProjectDto project = projectService.getProject(id);
    if (project == null) {
      return Response.status(Response.Status.NOT_FOUND).build();
    }
    if (!project.canView(currentUser)) {
      return Response.status(Response.Status.UNAUTHORIZED).build();
    }
    List<StatusDto> projectStatuses = statusService.getStatuses(id);
    return Response.ok(projectStatuses).build();
  }

  @GET
  @Path("labels")
  @RolesAllowed("users")
  @Produces(MediaType.APPLICATION_JSON)
  @ApiOperation(value = "Gets labels of the authenticated user", httpMethod = "GET", response = Response.class, notes = "This returns labels of the authenticated user")
  @ApiResponses(value = { @ApiResponse(code = 200, message = "Request fulfilled") })
  public Response getLabels() {
    String currentUser = ConversationState.getCurrent().getIdentity().getUserId();
    return Response.ok(labelService.findLabelsByUser(currentUser, 0, -1)).build();
  }

  @GET
  @Path("labels/{id}")
  @RolesAllowed("users")
  @Produces(MediaType.APPLICATION_JSON)
  @ApiOperation(value = "Gets labels of a specific task by id", httpMethod = "GET", response = Response.class, notes = "This returns labels of a specific task by id")
  @ApiResponses(value = { @ApiResponse(code = 200, message = "Request fulfilled"),
      @ApiResponse(code = 500, message = "Internal server error") })
  public Response getLabelsByTaskId(@ApiParam(value = "Task id", required = true) @PathParam("id") long id) {
    String currentUser = ConversationState.getCurrent().getIdentity().getUserId();
    return Response.ok(labelService.findLabelsByTask(id, currentUser, 0, -1)).build();
  }

  @POST
  @Path("labels/{id}")
  @RolesAllowed("users")
  @ApiOperation(value = "Adds a specific task by id to a label", httpMethod = "POST", response = Response.class, notes = "This adds a specific task by id to a label")
  @ApiResponses(value = { @ApiResponse(code = 200, message = "Request fulfilled"),
      @ApiResponse(code = 400, message = "Invalid query input"), @ApiResponse(code = 403, message = "Unauthorized operation"),
      @ApiResponse(code = 404, message = "Resource not found") })
  public Response addTaskToLabel(@ApiParam(value = "label", required = true) LabelDto addedLabel,
                                 @ApiParam(value = "Task id", required = true) @PathParam("id") long id) throws Exception {
    String currentUser = ConversationState.getCurrent().getIdentity().getUserId();
    if (addedLabel == null) {
      return Response.status(Response.Status.BAD_REQUEST).build();
    }
    TaskDto task = taskService.getTask(id);
    if (task == null) {
      return Response.status(Response.Status.NOT_FOUND).build();
    }
    if (!TaskUtil.hasEditPermission(task)) {
      return Response.status(Response.Status.FORBIDDEN).build();
    }
    if (addedLabel.getId() == 0) {// Create a new label and add a task to it
      addedLabel.setUsername(currentUser);
      LabelDto label = labelService.createLabel(addedLabel);
      labelService.addTaskToLabel(task, label.getId());
    } else {// Add a task to an existing label
      labelService.addTaskToLabel(task, addedLabel.getId());
    }
    return Response.ok(addedLabel).build();
  }

  @DELETE
  @Path("labels/{id}/{labelId}")
  @RolesAllowed("users")
  @ApiOperation(value = "Deletes a specific task association to a specific label", httpMethod = "DELETE", response = Response.class, notes = "This deletes a specific task association to a specific label")
  @ApiResponses(value = { @ApiResponse(code = 200, message = "Request fulfilled"),
      @ApiResponse(code = 403, message = "Unauthorized operation"), @ApiResponse(code = 404, message = "Resource not found") })
  public Response removeTaskFromLabel(@ApiParam(value = "label id", required = true) @PathParam("labelId") long labelId,
                                      @ApiParam(value = "Task id", required = true) @PathParam("id") long id) throws Exception {
    TaskDto task = taskService.getTask(id);
    if (task == null) {
      return Response.status(Response.Status.NOT_FOUND).build();
    }
    LabelDto label = labelService.getLabel(labelId);
    if (label == null) {
      return Response.status(Response.Status.NOT_FOUND).build();
    }
    if (!TaskUtil.hasEditPermission(task)) {
      return Response.status(Response.Status.FORBIDDEN).build();
    }
    labelService.removeTaskFromLabel(task, labelId);
    return Response.ok(label).build();
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
                              @ApiParam(value = "Limit", required = false, defaultValue = "-1") @QueryParam("limit") int limit) throws Exception {
    TaskDto task = taskService.getTask(id);
    if (task == null) {
      return Response.status(Response.Status.NOT_FOUND).build();
    }
    if (!TaskUtil.hasViewPermission(task)) {
      return Response.status(Response.Status.UNAUTHORIZED).build();
    }
    if (limit == 0) {
      limit = -1;
    }
    ChangeLogEntry[] arr = ListUtil.load(taskService.getTaskLogs(id), offset, limit);
    if (arr == null) {
      return Response.ok(Collections.emptyList()).build();
    }
    List<ChangeLogEntry> logs = new LinkedList<ChangeLogEntry>(Arrays.asList(arr));
    return Response.ok(logs).build();
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
                                  @ApiParam(value = "Limit", required = false, defaultValue = "-1") @QueryParam("limit") int limit) throws Exception {
    TaskDto task = taskService.getTask(id);
    if (task == null) {
      return Response.status(Response.Status.NOT_FOUND).build();
    }
    if (!TaskUtil.hasViewPermission(task)) {
      return Response.status(Response.Status.UNAUTHORIZED).build();
    }
    if (limit == 0) {
      limit = -1;
    }
    List<CommentDto> comments = commentService.getComments(id, offset, limit);
    commentService.loadSubComments(comments);

    List<CommentEntity> commentModelsList = new ArrayList<CommentEntity>();
    for (CommentDto comment : comments) {
      CommentEntity commentModel = addCommentModel(comment, commentModelsList);
      if (!comment.getSubComments().isEmpty()) {
        List<CommentEntity> subCommentsModelsList = new ArrayList<>();
        for (CommentDto subComment : commentStorage.listCommentsToDtos(comment.getSubComments())) {
          addCommentModel(subComment, subCommentsModelsList);
        }
        commentModel.setSubComments(subCommentsModelsList);
      }
    }
    Collections.reverse(commentModelsList);
    return Response.ok(commentModelsList).build();
  }

  @POST
  @Path("comments/{id}")
  @RolesAllowed("users")
  @ApiOperation(value = "Adds comment to a specific task by id", httpMethod = "POST", response = Response.class, notes = "This Adds comment to a specific task by id")
  @ApiResponses(value = { @ApiResponse(code = 200, message = "Request fulfilled"),
      @ApiResponse(code = 400, message = "Invalid query input"), @ApiResponse(code = 403, message = "Unauthorized operation"),
      @ApiResponse(code = 404, message = "Resource not found") })
  public Response addTaskComment(@ApiParam(value = "Comment text", required = true) String commentText,
                                 @ApiParam(value = "Task id", required = true) @PathParam("id") long id) throws Exception {
    String currentUser = ConversationState.getCurrent().getIdentity().getUserId();
    TaskDto task = taskService.getTask(id);
    if (task == null) {
      return Response.status(Response.Status.NOT_FOUND).build();
    }
    if (commentText == null || commentText.isEmpty()) {
      return Response.status(Response.Status.BAD_REQUEST).build();
    }
    if (!TaskUtil.hasEditPermission(task)) {
      return Response.status(Response.Status.FORBIDDEN).build();
    }
    commentText = URLDecoder.decode(commentText, "UTF-8");
    CommentDto addedComment = commentService.addComment(task, currentUser, commentText);
    if (addedComment != null) {
      addedComment = commentService.getComment(addedComment.getId());
    }
    CommentEntity commentEntity = new CommentEntity(addedComment, userService.loadUser(currentUser), commentText);
    return Response.ok(commentEntity).build();
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
                                    @ApiParam(value = "Task id", required = true) @PathParam("id") long id) throws Exception {

    String currentUser = ConversationState.getCurrent().getIdentity().getUserId();
    TaskDto task = taskService.getTask(id);
    if (task == null) {
      return Response.status(Response.Status.NOT_FOUND).build();
    }
    if (!TaskUtil.hasEditPermission(task)) {
      return Response.status(Response.Status.FORBIDDEN).build();
    }
    if (commentText == null || commentText.isEmpty()) {
      return Response.status(Response.Status.BAD_REQUEST).build();
    }

    commentText = URLDecoder.decode(commentText, "UTF-8");
    CommentDto addedComment = commentService.addComment(task, commentId, currentUser, commentText);
    if (addedComment != null) {
      addedComment = commentService.getComment(addedComment.getId());
    }
    CommentEntity commentEntity = new CommentEntity(addedComment, userService.loadUser(currentUser), commentText);
    return Response.ok(commentEntity).build();
  }

  @DELETE
  @Path("comments/{commentId}")
  @RolesAllowed("users")
  @ApiOperation(value = "Deletes a specific task comment by id", httpMethod = "DELETE", response = Response.class, notes = "This deletes a specific task comment by id")
  @ApiResponses(value = { @ApiResponse(code = 200, message = "Request fulfilled"),
      @ApiResponse(code = 403, message = "Unauthorized operation"), @ApiResponse(code = 404, message = "Resource not found") })
  public Response deleteComment(@ApiParam(value = "Comment id", required = true) @PathParam("commentId") long commentId) throws Exception {
    CommentDto comment = commentService.getComment(commentId);
    if (comment == null) {
      return Response.status(Response.Status.NOT_FOUND).build();
    }
    Identity currentIdentity = ConversationState.getCurrent().getIdentity();
    if (!TaskUtil.canDeleteComment(currentIdentity, commentStorage.commentToEntity(comment))) {
      return Response.status(Response.Status.FORBIDDEN).build();
    }
    commentService.removeComment(commentId);
    return Response.ok(comment).build();
  }

  @GET
  @Path("usersToMention/{query}")
  @RolesAllowed("users")
  @Produces(MediaType.APPLICATION_JSON)
  @ApiOperation(value = "Gets users to mention in comment", httpMethod = "GET", response = Response.class, notes = "This returns users to mention in comment")
  @ApiResponses(value = { @ApiResponse(code = 200, message = "Request fulfilled") })
  public Response findUsersToMention(@ApiParam(value = "Query", required = true) @PathParam("query") String query) throws Exception {
    ListAccess<User> list = userService.findUserByName(query);
    JSONArray usersJsonArray = new JSONArray();
    for (User user : list.load(0, UserUtil.SEARCH_LIMIT)) {
      JSONObject userJson = new JSONObject();
      userJson.put("id", "@" + user.getUsername());
      userJson.put("name", user.getDisplayName());
      userJson.put("avatar", user.getAvatar());
      userJson.put("type", "contact");
      usersJsonArray.put(userJson);
    }
    return Response.ok(usersJsonArray.toString()).build();
  }

  @GET
  @Path("users/{query}/{projectName}")
  @RolesAllowed("users")
  @Produces(MediaType.APPLICATION_JSON)
  @ApiOperation(value = "Gets users by query and project name", httpMethod = "GET", response = Response.class, notes = "This returns users by query and project name")
  @ApiResponses(value = { @ApiResponse(code = 200, message = "Request fulfilled") })
  public Response getUsersByQueryAndProjectName(@ApiParam(value = "Query", required = true) @PathParam("query") String query,
                                                @ApiParam(value = "projectName", required = true) @PathParam("projectName") String projectName) throws Exception {
    ListAccess<User> usersList = userService.findUserByName(query);
    JSONArray usersJsonArray = new JSONArray();
    for (User user : usersList.load(0, UserUtil.SEARCH_LIMIT)) {
      JSONObject userJson = new JSONObject();
      Space space = spaceService.getSpaceByPrettyName(projectName);
      if (space == null || spaceService.isMember(space, user.getUsername())) {
        userJson.put("username", user.getUsername());
        userJson.put("fullname", user.getDisplayName());
        userJson.put("avatar", user.getAvatar());
        usersJsonArray.put(userJson);
      }
    }
    return Response.ok(usersJsonArray.toString()).build();
  }

  private CommentEntity addCommentModel(CommentDto comment, List<CommentEntity> commentModelsList) {
    User user = userService.loadUser(comment.getAuthor());
    Status taskStatus = comment.getTask().getStatus();
    if (taskStatus != null) {
      comment.getTask().setStatus(taskStatus.clone());// To be checked
    }
    CommentEntity commentEntity = new CommentEntity(comment, user, CommentUtil.formatMention(comment.getComment(), userService));
    if (commentEntity.getSubComments() == null) {
      commentEntity.setSubComments(new ArrayList<>());
    }
    commentModelsList.add(commentEntity);
    return commentEntity;
  }

  private JSONArray buildJSON(JSONArray projectsJsonArray, List<ProjectDto> projects) throws JSONException {
    Identity currentUser = ConversationState.getCurrent().getIdentity();
    for (ProjectDto project : projects) {
      if (project.canView(currentUser)) {
        long projectId = project.getId();
        JSONObject projectJson = new JSONObject();
        List<Object[]> statusObjects = taskService.countTaskStatusByProject(projectId);
        if (statusObjects != null && statusObjects.size() > 0) {
          JSONArray statusStats = new JSONArray();
          for (Object[] result : statusObjects) {
            JSONObject statJson = new JSONObject();
            statJson.put("status", (String) result[0]);
            statJson.put("taskNumber", ((Number) result[1]).intValue());
            statusStats.put(statJson);
          }
          projectJson.put("statusStats", statusStats);
        }
        Space space = null;
        Set<String> projectManagers = projectService.getManager(projectId);
        Set<String> managers = new LinkedHashSet();
        if (projectManagers.size() > 0) {
          for (String permission : projectService.getManager(projectId)) {
            int index = permission.indexOf(':');
            if (index > -1) {
              String groupId = permission.substring(index + 1);
              space = spaceService.getSpaceByGroupId(groupId);
              managers.addAll(Arrays.asList(space.getManagers()));
            } else {
              managers.add(permission);
            }
          }
        }
        if (managers.size() > 0) {
          JSONArray managersJsonArray = new JSONArray();
          for (String usr : managers) {
            JSONObject manager = new JSONObject();
            User user_ = UserUtil.getUser(usr);
            manager.put("username", user_.getUsername());
            manager.put("email", user_.getEmail());
            manager.put("displayName", user_.getDisplayName());
            manager.put("avatar", user_.getAvatar());
            manager.put("url", user_.getUrl());
            manager.put("enable", user_.isEnable());
            manager.put("deleted", user_.isDeleted());
            managersJsonArray.put(manager);
          }
          projectJson.put("managerIdentities", managersJsonArray);
        }
        if(space!=null){
          JSONObject spaceJson = new JSONObject();
          spaceJson.put("prettyName",space.getPrettyName());
          spaceJson.put("url",space.getUrl());
          spaceJson.put("displayName",space.getDisplayName());
          spaceJson.put("id",space.getId());
          spaceJson.put("avatarUrl",space.getAvatarUrl());
          spaceJson.put("description",space.getDescription());
          projectJson.put("space", space);
        }

        projectJson.put("id", projectId);
        projectJson.put("name", project.getName());
        projectJson.put("color", project.getColor());
        projectJson.put("participator", projectService.getParticipator(projectId));
        projectJson.put("hiddenOn", project.getHiddenOn());
        projectJson.put("manager", projectService.getManager(projectId));
        projectJson.put("children", projectService.getSubProjects(projectId, 0, -1));
        projectJson.put("dueDate", project.getDueDate());
        projectJson.put("calendarIntegrated", project.isCalendarIntegrated());
        projectJson.put("description", project.getDescription());
        projectJson.put("status", statusService.getStatus(projectId));
        projectsJsonArray.put(projectJson);
        if (projectService.getSubProjects(projectId, 0, -1) != null) {
          List<ProjectDto> children = projectService.getSubProjects(projectId, 0, -1);
          buildJSON(projectsJsonArray, children);
        }
      }
    }
    return projectsJsonArray;
  }
  /*
   * private ChangeLogEntry changeLogToChangeLogEntry(ChangeLog changeLog) {
   * return new ChangeLogEntry(changeLog,userService); } private
   * List<ChangeLogEntry> changeLogsToChangeLogEntries(List<ChangeLog> ChangeLogs)
   * { return ChangeLogs.stream() .filter(Objects::nonNull)
   * .map(this::changeLogToChangeLogEntry) .collect(Collectors.toList()); }
   */

  private TaskEntity getTaskDetails(TaskDto task, String userName) {

    long taskId = task.getId();
    int commentCount;
    try {
      commentCount = commentService.countComments(taskId);
    } catch (Exception e) {
      LOG.warn("Error retrieving task '{}' comments count", taskId, e);
      commentCount = 0;
    }
    List<LabelDto> labels = new ArrayList<>();
    try {
      labels = labelService.findLabelsByTask(taskId, userName, 0, -1);
    } catch (Exception e) {
      LOG.warn("Error retrieving task '{}' labels", taskId, e);
    }
    Space space = null;
    if (task.getStatus() != null && task.getStatus().getProject() != null) {
      space = getProjectSpace(task.getStatus().getProject(), CommonsUtils.getService(SpaceService.class));
    }

    TaskEntity taskEntity = new TaskEntity(((TaskDto) task), commentCount);
    taskEntity.setLabels(labels);
    taskEntity.setSpace(space);
    return taskEntity;
  }

  private Space getProjectSpace(Project project, SpaceService spaceService) {
    for (String permission : projectService.getManager(project.getId())) {
      int index = permission.indexOf(':');
      if (index > -1) {
        String groupId = permission.substring(index + 1);
        Space space = spaceService.getSpaceByGroupId(groupId);
        return space;

      }
    }

    return null;
  }
}

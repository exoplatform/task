package org.exoplatform.task.rest;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;

import java.util.*;
import java.util.stream.Collectors;

import javax.annotation.security.RolesAllowed;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.exoplatform.task.domain.*;
import org.exoplatform.task.dto.ChangeLogEntry;
import org.exoplatform.task.model.CommentModel;
import org.exoplatform.task.model.User;
import org.exoplatform.task.util.CommentUtil;
import org.exoplatform.task.service.StatusService;
import org.exoplatform.task.service.UserService;
import org.exoplatform.task.util.*;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import org.exoplatform.commons.utils.ListAccess;
import org.exoplatform.services.rest.resource.ResourceContainer;
import org.exoplatform.services.security.ConversationState;
import org.exoplatform.services.security.Identity;
import org.exoplatform.task.exception.EntityNotFoundException;
import org.exoplatform.task.service.ProjectService;
import org.exoplatform.task.service.TaskService;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;

@Path("/tasks")
@Api(value = "/tasks", description = "Managing tasks")
@RolesAllowed("users")
public class TaskRestService implements ResourceContainer {


  private static final int DEFAULT_LIMIT = 20;

  private static final Log LOG = ExoLogger.getLogger(TaskRestService.class);

  private TaskService    taskService;

  private ProjectService projectService;
  
  private StatusService statusService;
  
  private UserService userService;

  public TaskRestService(TaskService taskService, ProjectService projectService, StatusService statusService,UserService userService) {
    this.taskService = taskService;
    this.projectService = projectService;
    this.statusService = statusService;
    this.userService = userService;
  }

  private enum TaskType {
    ALL,
    INCOMING,
    OVERDUE
  }

  @GET
  @RolesAllowed("users")
  @Produces(MediaType.APPLICATION_JSON)
  @ApiOperation(value = "Gets uncompleted tasks of the authenticated user",
      httpMethod = "GET",
      response = Response.class,
      notes = "This returns uncompleted tasks of the authenticated user in the following cases: <br/><ul><li>The authenticated is the creator of the task</li><li>The authenticated is the assignee of the task</li><li>The authenticated is a coworker of the task</li></ul>")
  @ApiResponses(value = {
      @ApiResponse(code = 200, message = "Request fulfilled") })
  public Response getTasks(@ApiParam(value = "Type of task to get (all, incoming, overdue)", required = false) @QueryParam("status") String status,
                           @ApiParam(value = "Offset", required = false, defaultValue = "0") @QueryParam("offset") int offset,
                           @ApiParam(value = "Limit", required = false, defaultValue = "20") @QueryParam("limit") int limit,
                           @ApiParam(value = "Returning the number of tasks or not", defaultValue = "false") @QueryParam("returnSize") boolean returnSize) throws Exception {
    String currentUser = ConversationState.getCurrent().getIdentity().getUserId();
    List<Task> tasks = new ArrayList<Task>();
    Long tasksSize;
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
      ListAccess<Task> tasksListAccess = taskService.getIncomingTasks(currentUser);
      tasks = Arrays.asList(tasksListAccess.load(offset, limit));
      tasksSize = Long.valueOf(tasksListAccess.getSize());
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
    // TODO Need to return always tasks and size
    if (returnSize) {
      JSONObject tasksSizeJsonObject = new JSONObject();
      tasksSizeJsonObject.put("size", tasksSize);
      return Response.ok(tasksSizeJsonObject.toString()).build();
    } else {
      return Response.ok(tasks).build();
    }
  }

  @PUT
  @Path("{id}")
  @Produces(MediaType.APPLICATION_JSON)
  @RolesAllowed("users")
  @ApiOperation(value = "Updates a specific task by id",
      httpMethod = "PUT",
      response = Response.class,
      notes = "This updates the task if the authenticated user has permissions to view the objects linked to this task.")
  @ApiResponses(value = {
      @ApiResponse(code = 200, message = "Request fulfilled"),
      @ApiResponse(code = 400, message = "Invalid query input"),
      @ApiResponse(code = 403, message = "Unauthorized operation"),
      @ApiResponse(code = 404, message = "Resource not found") })
  public Response updateTaskById(@ApiParam(value = "Task id", required = true) @PathParam("id") long id,
                                 @ApiParam(value = "task object to be updated", required = true) Task updatedTask) throws Exception {
    if (updatedTask == null) {
      return Response.status(Response.Status.BAD_REQUEST).build();
    }
    Task task = taskService.getTask(id);
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
  @ApiOperation(value = "Gets projects",
      httpMethod = "GET",
      response = Response.class,
      notes = "This returns projects of the authenticated user")
  @ApiResponses(value = {
      @ApiResponse(code = 200, message = "Request fulfilled"),
      @ApiResponse(code = 500, message = "Internal server error") })
  public Response getProjects() {
    List<Project> projects = ProjectUtil.getProjectTree(null, projectService);
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
  @ApiOperation(value = "Gets the default status by project id",
          httpMethod = "GET",
          response = Response.class,
          notes = "This returns the default status by project id")
  @ApiResponses(value = {
          @ApiResponse(code = 200, message = "Request fulfilled"),
          @ApiResponse(code = 403, message = "Unauthorized operation"),
          @ApiResponse(code = 500, message = "Internal server error")})
  public Response getDefaultStatusByProjectId(@ApiParam(value = "Project id", required = true) @PathParam("id") long id) throws EntityNotFoundException {
    Identity currentUser = ConversationState.getCurrent().getIdentity();
    Project project = projectService.getProject(id);
    if (project == null) {
      return Response.status(Response.Status.NOT_FOUND).build();
    }
    if (!project.canView(currentUser)) {
      return Response.status(Response.Status.UNAUTHORIZED).build();
    }
    Status status = statusService.getDefaultStatus(id);
    return Response.ok(status).build();
  }

  @GET
  @Path("labels")
  @RolesAllowed("users")
  @Produces(MediaType.APPLICATION_JSON)
  @ApiOperation(value = "Gets labels of the authenticated user",
      httpMethod = "GET",
      response = Response.class,
      notes = "This returns labels of the authenticated user")
  @ApiResponses(value = {
      @ApiResponse(code = 200, message = "Request fulfilled") })
  public Response getLabels() {
    String currentUser = ConversationState.getCurrent().getIdentity().getUserId();
    List<Label> labels = new ArrayList<Label>();
    labels = Arrays.asList(ListUtil.load(taskService.findLabelsByUser(currentUser), 0, -1));
    return Response.ok(labels).build();
  }

  @GET
  @Path("labels/{id}")
  @RolesAllowed("users")
  @Produces(MediaType.APPLICATION_JSON)
  @ApiOperation(value = "Gets labels of a specific task by id",
      httpMethod = "GET",
      response = Response.class,
      notes = "This returns labels of a specific task by id")
  @ApiResponses(value = {
      @ApiResponse(code = 200, message = "Request fulfilled"),
      @ApiResponse(code = 500, message = "Internal server error") })
  public Response getLabelsByTaskId(@ApiParam(value = "Task id", required = true) @PathParam("id") long id) {
    String currentUser = ConversationState.getCurrent().getIdentity().getUserId();
    List<Label> labels = new ArrayList<Label>();
    try {
      labels = Arrays.asList(ListUtil.load(taskService.findLabelsByTask(id, currentUser), 0, -1));
    } catch (EntityNotFoundException e) {
      LOG.error("Error getting label by task id", e);
      return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
    }
    return Response.ok(labels).build();
  }

  @POST
  @Path("labels/{id}")
  @RolesAllowed("users")
  @ApiOperation(value = "Adds a specific task by id to a label",
      httpMethod = "POST",
      response = Response.class,
      notes = "This adds a specific task by id to a label")
  @ApiResponses(value = {
      @ApiResponse(code = 200, message = "Request fulfilled"),
      @ApiResponse(code = 400, message = "Invalid query input"),
      @ApiResponse(code = 403, message = "Unauthorized operation"),
      @ApiResponse(code = 404, message = "Resource not found") })
  public Response addTaskToLabel(@ApiParam(value = "label", required = true) Label addedLabel,
                                 @ApiParam(value = "Task id", required = true) @PathParam("id") long id) throws Exception {
    String currentUser = ConversationState.getCurrent().getIdentity().getUserId();
    if (addedLabel == null) {
      return Response.status(Response.Status.BAD_REQUEST).build();
    }
    Task task = taskService.getTask(id);
    if (task == null) {
      return Response.status(Response.Status.NOT_FOUND).build();
    }
    if (!TaskUtil.hasEditPermission(task)) {
      return Response.status(Response.Status.FORBIDDEN).build();
    }
    if (addedLabel.getId() == 0) {// Create a new label and add a task to it
      addedLabel.setUsername(currentUser);
      Label label = taskService.createLabel(addedLabel);
      taskService.addTaskToLabel(id, label.getId());
    } else {// Add a task to an existing label
      taskService.addTaskToLabel(id, addedLabel.getId());
    }
    return Response.ok(addedLabel).build();
  }

  @DELETE
  @Path("labels/{id}/{labelId}")
  @RolesAllowed("users")
  @ApiOperation(value = "Deletes a specific task association to a specific label",
      httpMethod = "DELETE",
      response = Response.class,
      notes = "This deletes a specific task association to a specific label")
  @ApiResponses(value = {
      @ApiResponse(code = 200, message = "Request fulfilled"),
      @ApiResponse(code = 403, message = "Unauthorized operation"),
      @ApiResponse(code = 404, message = "Resource not found") })
  public Response removeTaskFromLabel(@ApiParam(value = "label id", required = true) @PathParam("labelId") long labelId,
                                      @ApiParam(value = "Task id", required = true) @PathParam("id") long id) throws Exception {
    Task task = taskService.getTask(id);
    if (task == null) {
      return Response.status(Response.Status.NOT_FOUND).build();
    }
    Label label = taskService.getLabel(labelId);
    if (label == null) {
      return Response.status(Response.Status.NOT_FOUND).build();
    }
    if (!TaskUtil.hasEditPermission(task)) {
      return Response.status(Response.Status.FORBIDDEN).build();
    }
    taskService.removeTaskFromLabel(id, labelId);
    return Response.ok(label).build();
  }

  @GET
  @Path("logs/{id}")
  @RolesAllowed("users")
  @Produces(MediaType.APPLICATION_JSON)
  @ApiOperation(value = "Gets a logs of a specific task",
          httpMethod = "GET",
          response = Response.class,
          notes = "This returns a logs of a specific task")
  @ApiResponses(value = {
          @ApiResponse(code = 200, message = "Request fulfilled"),
          @ApiResponse(code = 401, message = "Unauthorized operation"),
          @ApiResponse(code = 404, message = "Resource not found")})
  public Response getTaskLogs(@ApiParam(value = "Task id", required = true) @PathParam("id") long id,
                              @ApiParam(value = "Offset", required = false, defaultValue = "0") @QueryParam("offset") int offset,
                              @ApiParam(value = "Limit", required = false, defaultValue = "-1") @QueryParam("limit") int limit) throws Exception {
    Task task = taskService.getTask(id);
    if (task == null) {
      return Response.status(Response.Status.NOT_FOUND).build();
    }
    if (!TaskUtil.hasViewPermission(task)) {
      return Response.status(Response.Status.UNAUTHORIZED).build();
    }
    if (limit == 0) {
      limit = -1;
    }
    ChangeLog[] arr = ListUtil.load(taskService.getTaskLogs(id), offset, limit);
    if (arr == null) {
      return Response.ok(Collections.emptyList()).build();
    }
    List<ChangeLog> logs = new LinkedList<ChangeLog>(Arrays.asList(arr));

    Collections.sort(logs);

    List<ChangeLogEntry> changeLogEntries = changeLogsToChangeLogEntries(logs);
    
    return Response.ok(changeLogEntries).build();
  }

  @GET
  @Path("comments/{id}")
  @RolesAllowed("users")
  @Produces(MediaType.APPLICATION_JSON)
  @ApiOperation(value = "Gets a comment list of a specific task",
          httpMethod = "GET",
          response = Response.class,
          notes = "This returns a comment list of a specific task")
  @ApiResponses(value = {
          @ApiResponse(code = 200, message = "Request fulfilled"),
          @ApiResponse(code = 403, message = "Unauthorized operation"),
          @ApiResponse(code = 404, message = "Resource not found")})
  public Response getTaskComments(@ApiParam(value = "Task id", required = true) @PathParam("id") long id,
                                  @ApiParam(value = "Offset", required = false, defaultValue = "0") @QueryParam("offset") int offset,
                                  @ApiParam(value = "Limit", required = false, defaultValue = "-1") @QueryParam("limit") int limit) throws Exception {
    Task task = taskService.getTask(id);
    if (task == null) {
      return Response.status(Response.Status.NOT_FOUND).build();
    }
    if (!TaskUtil.hasViewPermission(task)) {
      return Response.status(Response.Status.UNAUTHORIZED).build();
    }
    ListAccess<Comment> commentsListAccess = taskService.getComments(id);
    if (limit == 0) {
      limit = -1;
    }
//    Comment[] comments = ListUtil.load(commentsListAccess, offset, limit); To be replaced for other methods
    List<Comment> comments = Arrays.asList(commentsListAccess.load(offset, limit));
    taskService.loadSubComments(comments);

    List<CommentModel> commentModelsList = new ArrayList<CommentModel>();
    for (Comment comment : comments) {
      CommentModel commentModel = addCommentModel(comment, commentModelsList);
      if (!comment.getSubComments().isEmpty()) {
        List<CommentModel> subCommentsModelsList = new ArrayList<>();
        for (Comment subComment : comment.getSubComments()) {
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
  @ApiOperation(value = "Adds comment to a specific task by id",
          httpMethod = "POST",
          response = Response.class,
          notes = "This Adds comment to a specific task by id")
  @ApiResponses(value = {
          @ApiResponse(code = 200, message = "Request fulfilled"),
          @ApiResponse(code = 400, message = "Invalid query input"),
          @ApiResponse(code = 403, message = "Unauthorized operation"),
          @ApiResponse(code = 404, message = "Resource not found") })
  public Response addTaskComment(@ApiParam(value = "Comment text", required = true) String commentText,
                                 @ApiParam(value = "Task id", required = true) @PathParam("id") long id) throws Exception {
    String currentUser = ConversationState.getCurrent().getIdentity().getUserId();
    Task task = taskService.getTask(id);
    if (task == null) {
      return Response.status(Response.Status.NOT_FOUND).build();
    }
    if (!TaskUtil.hasEditPermission(task)) {
      return Response.status(Response.Status.FORBIDDEN).build();
    }
    commentText = commentText.substring(1, commentText.length() - 1);
    Comment addedComment = taskService.addComment(id, currentUser, commentText);
    CommentModel commentModel = new CommentModel(addedComment, userService.loadUser(currentUser), commentText);
    return Response.ok(commentModel).build();
  }

  @POST
  @Path("comments/{commentId}/{id}")
  @RolesAllowed("users")
  @ApiOperation(value = "Adds a sub comment to a specific parent comment by id and a specific task by id",
          httpMethod = "POST",
          response = Response.class,
          notes = "This Adds sub comment to a parent comment in specific task by id")
  @ApiResponses(value = {
          @ApiResponse(code = 200, message = "Request fulfilled"),
          @ApiResponse(code = 400, message = "Invalid query input"),
          @ApiResponse(code = 403, message = "Unauthorized operation"),
          @ApiResponse(code = 404, message = "Resource not found")})
  public Response addTaskSubComment(@ApiParam(value = "Comment text", required = true) String commentText,
                                    @ApiParam(value = "Comment id", required = true) @PathParam("commentId") long commentId,
                                    @ApiParam(value = "Task id", required = true) @PathParam("id") long id) throws Exception {

    String currentUser = ConversationState.getCurrent().getIdentity().getUserId();
    Task task = taskService.getTask(id);
    if (task == null) {
      return Response.status(Response.Status.NOT_FOUND).build();
    }
    if (!TaskUtil.hasEditPermission(task)) {
      return Response.status(Response.Status.FORBIDDEN).build();
    }
    commentText = commentText.substring(1, commentText.length() - 1);
    Comment addedComment = taskService.addComment(id, commentId, currentUser, commentText);
    CommentModel commentModel = new CommentModel(addedComment, userService.loadUser(currentUser), commentText);
    return Response.ok(commentModel).build();
  }

  @DELETE
  @Path("comments/{commentId}")
  @RolesAllowed("users")
  @ApiOperation(value = "Deletes a specific task comment by id",
          httpMethod = "DELETE",
          response = Response.class,
          notes = "This deletes a specific task comment by id")
  @ApiResponses(value = {
          @ApiResponse(code = 200, message = "Request fulfilled"),
          @ApiResponse(code = 403, message = "Unauthorized operation"),
          @ApiResponse(code = 404, message = "Resource not found")})
  public Response deleteComment(@ApiParam(value = "Comment id", required = true) @PathParam("commentId") long commentId) throws Exception {
    Comment comment = taskService.getComment(commentId);
    if (comment == null) {
      return Response.status(Response.Status.NOT_FOUND).build();
    }
    Identity currentIdentity = ConversationState.getCurrent().getIdentity();
    if (!TaskUtil.canDeleteComment(currentIdentity, comment)) {
      return Response.status(Response.Status.FORBIDDEN).build();
    }
    taskService.removeComment(commentId);
    return Response.ok(comment).build();
  }

  private CommentModel addCommentModel(Comment comment, List<CommentModel> commentModelsList) {
    User user = userService.loadUser(comment.getAuthor());
    Status taskStatus = comment.getTask().getStatus();
    if (taskStatus != null) {
      comment.getTask().setStatus(taskStatus.clone());// To be checked
    }
    CommentModel commentModel = new CommentModel(comment, user, CommentUtil.formatMention(comment.getComment(), userService));
    if (commentModel.getSubComments() == null) {
      commentModel.setSubComments(new ArrayList<>());
    }
    commentModelsList.add(commentModel);
    return commentModel;
  }

  private JSONArray buildJSON(JSONArray projectsJsonArray, List<Project> projects) throws JSONException {
    Identity currentUser = ConversationState.getCurrent().getIdentity();
    for (Project project : projects) {
      if (project.canView(currentUser)) {
        long projectId = project.getId();
        JSONObject projectJson = new JSONObject();
        projectJson.put("id", projectId);
        projectJson.put("name", project.getName());
        projectJson.put("color", project.getColor());
        projectJson.put("participator", projectService.getParticipator(projectId));
        projectJson.put("hiddenOn", project.getHiddenOn());
        projectJson.put("manager", projectService.getManager(projectId));
        projectJson.put("children", projectService.getSubProjects(projectId));
        projectJson.put("dueDate", project.getDueDate());
        projectJson.put("calendarIntegrated", project.isCalendarIntegrated());
        projectJson.put("description", project.getDescription());
        projectJson.put("status", statusService.getStatus(projectId));
        projectsJsonArray.put(projectJson);
        if (projectService.getSubProjects(projectId) != null) {
          List<Project> children = Arrays.asList(ListUtil.load(projectService.getSubProjects(projectId), 0, -1));
          buildJSON(projectsJsonArray, children);
        }
      }
    }
    return projectsJsonArray;
  }
  private ChangeLogEntry changeLogToChangeLogEntry(ChangeLog changeLog) {
    return new ChangeLogEntry(changeLog,userService);
  }

  private List<ChangeLogEntry> changeLogsToChangeLogEntries(List<ChangeLog> ChangeLogs) {
    return ChangeLogs.stream()
                     .filter(Objects::nonNull)
                     .map(this::changeLogToChangeLogEntry)
                     .collect(Collectors.toList());
  }
}

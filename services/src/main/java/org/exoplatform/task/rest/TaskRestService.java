package org.exoplatform.task.rest;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

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

import org.exoplatform.task.domain.Status;
import org.exoplatform.task.service.StatusService;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import org.exoplatform.commons.utils.ListAccess;
import org.exoplatform.services.rest.resource.ResourceContainer;
import org.exoplatform.services.security.ConversationState;
import org.exoplatform.services.security.Identity;
import org.exoplatform.task.domain.Label;
import org.exoplatform.task.domain.Project;
import org.exoplatform.task.domain.Task;
import org.exoplatform.task.exception.EntityNotFoundException;
import org.exoplatform.task.service.ProjectService;
import org.exoplatform.task.service.TaskService;
import org.exoplatform.task.util.ListUtil;
import org.exoplatform.task.util.ProjectUtil;
import org.exoplatform.task.util.TaskUtil;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;

@Path("/tasks")
@Api(value = "/tasks", description = "Managing tasks")
@RolesAllowed("users")
public class TaskRestService implements ResourceContainer {


  private static final Log LOG = ExoLogger.getLogger(TaskRestService.class);

  private TaskService    taskService;

  private ProjectService projectService;
  
  private StatusService statusService;

  public TaskRestService(TaskService taskService, ProjectService projectService, StatusService statusService) {
    this.taskService = taskService;
    this.projectService = projectService;
    this.statusService = statusService;
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
    switch (taskType) {
    case INCOMING: {
      ListAccess<Task> tasksListAccess = taskService.getIncomingTasks(currentUser);
      tasks = Arrays.asList(tasksListAccess.load(offset, limit));
      tasksSize = Long.valueOf(tasksListAccess.getSize());
      break;
    }
    case OVERDUE: {
      tasks = taskService.getOverdueTasks(currentUser);
      tasksSize = taskService.countOverdueTasks(currentUser);
      break;
    }
    default: {
      tasks = taskService.getUncompletedTasks(currentUser);
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
}

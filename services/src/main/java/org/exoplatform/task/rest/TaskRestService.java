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
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.json.JSONObject;

import com.google.gson.JsonArray;

import org.exoplatform.commons.utils.ListAccess;
import org.exoplatform.services.rest.resource.ResourceContainer;
import org.exoplatform.services.security.ConversationState;
import org.exoplatform.task.domain.Task;
import org.exoplatform.task.service.TaskService;

@Path("/tasks")
@Api(value = "/tasks", description = "Managing tasks")
@RolesAllowed("users")
public class TaskRestService implements ResourceContainer {

  private TaskService taskService;

  public TaskRestService(TaskService tservice) {
    this.taskService = tservice;
  }

  private enum TaskType {
    ALL,
    INCOMING,
    OVERDUE
  }

  @GET
  @RolesAllowed("users")
  @Produces(MediaType.APPLICATION_JSON)
  @ApiOperation(value = "Gets uncompleted tasks",
      httpMethod = "GET",
      response = Response.class,
      notes = "This returns uncompleted tasks of the authenticated user in the following cases: <br/><ul><li>The authenticated is the creator of the task</li><li>The authenticated is the assignee of the task</li><li>The authenticated is a coworker of the task</li></ul>")
  @ApiResponses(value = {
      @ApiResponse(code = 403, message = "Unauthorized operation"),
      @ApiResponse(code = 500, message = "Internal server error") })
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
    //TODO Need to return always tasks and size  
    if (returnSize) {
      JSONObject tasksSizeJsonObject = new JSONObject();
      tasksSizeJsonObject.put("size", tasksSize);
      return Response.ok(tasksSizeJsonObject.toString()).build();
    } else {
      return Response.ok(tasks).build();
    }
  }
}

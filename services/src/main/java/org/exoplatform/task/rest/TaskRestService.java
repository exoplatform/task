package org.exoplatform.task.rest;


import io.swagger.annotations.*;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.rest.resource.ResourceContainer;
import org.exoplatform.services.security.ConversationState;
import org.exoplatform.task.domain.Task;
import org.exoplatform.task.service.TaskService;
import org.exoplatform.services.log.Log;
import org.exoplatform.task.util.DateUtil;
import org.exoplatform.task.util.TaskUtil;
import org.json.JSONObject;

import javax.annotation.security.RolesAllowed;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;
import java.util.stream.Collectors;

@Path("/task")
@Api(value = "/task", description = "Manages task")
@RolesAllowed("users")
public class TaskRestService implements ResourceContainer {

    private static final Log LOG = ExoLogger.getLogger(TaskRestService.class);

    private TaskService taskService;

    public TaskRestService(TaskService tservice) {
        this.taskService = tservice;
    }

    @GET
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Path("list")
    @RolesAllowed("users")
    @ApiOperation(value = "Retrieves the list of task for current user", httpMethod = "GET", response = Response.class, consumes = "application/json", produces = "application/json", notes = "returns a list of tasks")
    @ApiResponses(value = {
            @ApiResponse(code = 403, message = "Unauthorized operation"),
            @ApiResponse(code = 500, message = "Internal server error")})
    public Response getTasks(@ApiParam(value = "uncompleted tasks") @DefaultValue("false") @QueryParam("uncompleted") boolean uncompleted) {
        ConversationState conversationState = ConversationState.getCurrent();
        String currentUser = conversationState.getIdentity().getUserId();
        try {
            if (uncompleted) {
                List<Task> tasks = taskService.findByAssigned(currentUser);
                tasks = tasks.stream().filter(task -> !task.isCompleted()).collect(Collectors.toList());
                return Response.ok(tasks).build();
            } else {
                List<Task> tasks = taskService.findByAssigned(currentUser);
                return Response.ok(tasks).build();
            }
        } catch (Exception e) {
            LOG.error("Error listing tasks", e);
            return Response.status(500).build();
        }
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("tasks")
    @RolesAllowed("users")
    @ApiOperation(value = "Return the size of task for current user by status", httpMethod = "GET", response = Response.class, consumes = "application/json", produces = "application/json")
    @ApiResponses(value = {
            @ApiResponse(code = 403, message = "Unauthorized operation"),
            @ApiResponse(code = 500, message = "Internal server error")})
    public Response countTasksByStatus(@ApiParam(value = "tasks status", required = true) @QueryParam("status") String status) {
        ConversationState conversationState = ConversationState.getCurrent();
        String currentUser = conversationState.getIdentity().getUserId();
        try {
            Long numTasks = taskService.countTaskByStatus(status, currentUser);
            JSONObject tasksSize = new JSONObject();
            tasksSize.put("size", numTasks);
            return Response.ok(tasksSize.toString()).build();

        } catch (Exception e) {
            LOG.error("Error count tasks", e);
            return Response.status(500).build();
        }
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("overdue")
    @RolesAllowed("users")
    @ApiOperation(value = "Return the size of overdue task for current user", httpMethod = "GET", response = Response.class, consumes = "application/json", produces = "application/json")
    @ApiResponses(value = {
            @ApiResponse(code = 403, message = "Unauthorized operation"),
            @ApiResponse(code = 500, message = "Internal server error")})
    public Response countOverdueTasks() {
        ConversationState conversationState = ConversationState.getCurrent();
        String currentUser = conversationState.getIdentity().getUserId();
        try {
            List<Task> tasks = taskService.findByAssigned(currentUser);
            Long overdueTaskSize = tasks
                    .stream()
                    .filter(task -> isOverdueTask(task.getDueDate()))
                    .count();
            JSONObject tasksSize = new JSONObject();
            tasksSize.put("size", overdueTaskSize);
            return Response.ok(tasksSize.toString()).build();
        } catch (Exception e) {
            LOG.error("Error count tasks", e);
            return Response.status(500).build();
        }
    }

    private boolean isOverdueTask(Date date) {
        Calendar calendar = null;
        ConversationState conversationState = ConversationState.getCurrent();
        String currentUser = conversationState.getIdentity().getUserId();
        TimeZone tz = TaskUtil.getUserTimezone(currentUser);
        if (date != null) {
            calendar = Calendar.getInstance(tz);
            calendar.setTime(date);
            return DateUtil.isOverdue(calendar);
        } else {
            return false;
        }
    }
}

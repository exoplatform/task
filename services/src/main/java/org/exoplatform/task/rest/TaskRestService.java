package org.exoplatform.task.rest;

import io.swagger.annotations.*;
import org.apache.commons.lang3.StringUtils;
import org.exoplatform.commons.utils.CommonsUtils;
import org.exoplatform.commons.utils.ListAccess;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.services.rest.resource.ResourceContainer;
import org.exoplatform.services.security.ConversationState;
import org.exoplatform.services.security.Identity;
import org.exoplatform.social.core.space.model.Space;
import org.exoplatform.social.core.space.spi.SpaceService;
import org.exoplatform.task.dao.OrderBy;
import org.exoplatform.task.dao.StatusHandler;
import org.exoplatform.task.dao.TaskQuery;
import org.exoplatform.task.domain.Project;
import org.exoplatform.task.domain.Status;
import org.exoplatform.task.dto.*;
import org.exoplatform.task.legacy.service.UserService;
import org.exoplatform.task.model.User;
import org.exoplatform.task.rest.model.*;
import org.exoplatform.task.service.*;
import org.exoplatform.task.service.impl.ViewStateService;
import org.exoplatform.task.storage.CommentStorage;
import org.exoplatform.task.storage.StatusStorage;
import org.exoplatform.task.util.*;
import org.json.JSONArray;
import org.json.JSONException;
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

  private ViewStateService viewStateService;

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

  public TaskRestService(TaskService taskService,
                         CommentService commentService,
                         ProjectService projectService,
                         StatusService statusService,
                         UserService userService,
                         SpaceService spaceService,
                         LabelService labelService,
                         ViewStateService viewStateService) {
    this.taskService = taskService;
    this.commentService = commentService;
    this.projectService = projectService;
    this.statusService = statusService;
    this.userService = userService;
    this.spaceService = spaceService;
    this.labelService = labelService;
    this.viewStateService=viewStateService;
  }


  private enum TaskType {
    ALL, INCOMING, OVERDUE
  }



  @GET
  @Path("{id}")
  @Produces(MediaType.APPLICATION_JSON)
  @RolesAllowed("users")
  @ApiOperation(value = "Get task by id", httpMethod = "GET", response = Response.class, notes = "This get the task if the authenticated user has permissions to view the objects linked to this task.")
  @ApiResponses(value = { @ApiResponse(code = 200, message = "Request fulfilled"),
          @ApiResponse(code = 400, message = "Invalid query input"), @ApiResponse(code = 403, message = "Unauthorized operation"),
          @ApiResponse(code = 404, message = "Resource not found") })
  public Response getTaskById(@ApiParam(value = "Task id", required = true) @PathParam("id") long id) throws Exception {

    TaskDto task = taskService.getTask(id);
    if (task == null) {
      return Response.status(Response.Status.NOT_FOUND).build();
    }
    if (!TaskUtil.hasEditPermission(task)) {
      return Response.status(Response.Status.FORBIDDEN).build();
    }
    return Response.ok(task).build();
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
      default: {
        tasks = taskService.getUncompletedTasks(currentUser, limit);
        tasksSize = taskService.countUncompletedTasks(currentUser);
      }
      }
    } else {
      tasks = taskService.findTasks(currentUser, query, limit);
      /*tasks = tasks.stream().map(task -> {
        long taskId = ((TaskDto) task).getId();
        int commentCount;
        try {
          commentCount = commentService.countComments(taskId);
        } catch (Exception e) {
          LOG.warn("Error retrieving task '{}' comments count", taskId, e);
          commentCount = 0;
        }
        return new TaskEntity(((TaskDto) task), commentCount);
      }).collect(Collectors.toList());*/
      tasksSize = taskService.countTasks(currentUser, query);
    }
    if (returnSize) {
      if (returnDetails) {
        return Response.ok(new PaginatedTaskList(tasks.stream().map(task -> getTaskDetails((TaskDto) task, currentUser)).collect(Collectors.toList()),tasksSize)).build();
      } else {
        return Response.ok(tasks.stream().map(task -> getTaskDetails((TaskDto) task, currentUser)).collect(Collectors.toList())).build();      }

    } else {
      if (returnDetails) {
        return Response.ok(new PaginatedTaskList(tasks.stream().map(task -> getTaskDetails((TaskDto) task, currentUser)).collect(Collectors.toList()),tasksSize)).build();
      }
      return Response.ok(tasks).build();
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
                              @ApiParam(value = "showCompleted term", defaultValue = "false") @QueryParam("showCompleted") boolean showCompleted,
                              @ApiParam(value = "statusId term", required = false) @QueryParam("statusId") String statusId,
                              @ApiParam(value = "space_group_id term", required = false) @QueryParam("space_group_id") String space_group_id,
                              @ApiParam(value = "groupBy term", required = false) @QueryParam("groupBy") String groupBy,
                              @ApiParam(value = "orderBy term", required = false) @QueryParam("orderBy") String orderBy,
                              @ApiParam(value = "dueDate term", required = false) @QueryParam("dueDate") String dueDate,
                              @ApiParam(value = "page term", required = false) @QueryParam("page") Integer page,
                              @ApiParam(value = "viewType", required = false) @QueryParam("viewType") String viewType,
                              @ApiParam(value = "labelId term", required = false) @QueryParam("labelId") Long labelId,
                              @ApiParam(value = "filterLabelIds term", required = false) @QueryParam("filterLabelIds") String filterLabelIds,
                              @ApiParam(value = "Offset", required = false, defaultValue = "0") @QueryParam("offset") int offset,
                              @ApiParam(value = "Limit", required = false, defaultValue = "20") @QueryParam("limit") int limit,
                              @ApiParam(value = "Returning the number of tasks or not", defaultValue = "false") @QueryParam("returnSize") boolean returnSize,
                              @ApiParam(value = "Returning All Details", defaultValue = "false") @QueryParam("returnDetails") boolean returnDetails) throws Exception {

    String listId = ViewState.buildId(projectId, labelId, dueCategory);
    ViewState viewState = viewStateService.getViewState(listId);
    if (orderBy == null) {
      orderBy = viewState.getOrderBy();
    } else {
      viewState.setOrderBy(orderBy);
    }
    if (groupBy == null) {
      groupBy = viewState.getGroupBy();
    } else {
      viewState.setGroupBy(groupBy);
    }

    Identity currIdentity = ConversationState.getCurrent().getIdentity();
    ViewType vType;
    if (projectId <= 0 || viewType == null || viewType.isEmpty()) {
      vType = viewState.getViewType();
    } else {
      vType = ViewType.getViewType(viewType);
      viewState.setViewType(vType);
    }

    Long statusIdLong = null;
    if (org.apache.commons.lang.StringUtils.isNotBlank(statusId)) {
      StatusDto statusDto = statusService.getStatus(Long.parseLong(statusId));
      if (statusDto == null || !statusDto.getProject().canView(currIdentity)) {
        return Response.status(Response.Status.NOT_FOUND).build();
      }
    }

    viewStateService.saveViewState(viewState);
    boolean isBoardView = (ViewType.BOARD == vType);

    ViewState.Filter filter = viewStateService.getFilter(listId);
    filter.updateFilterData(filterLabelIds, statusId, dueDate, priority, assignee, showCompleted, query);
    viewStateService.saveFilter(filter);

    ProjectDto project = null;
    boolean noProjPermission = false;
    boolean advanceSearch = true;
    List<StatusDto> projectStatuses = Collections.emptyList();
    if ( projectId > 0) {
      project = projectService.getProject(projectId);
      if (!project.canView(currIdentity)) {
        if (advanceSearch) {
          noProjPermission = true;
        } else {
          return Response.status(Response.Status.NOT_FOUND).build();
        }
      } else if (isBoardView) {
        projectStatuses = statusService.getStatuses(projectId);
      }
    }
    String currentLabelName = "";
    boolean noLblPermission = false;
    if (labelId != null && labelId > 0) {
      LabelDto label = labelService.getLabel(labelId);
      if (!label.getUsername().equals(currIdentity.getUserId())) {
        if (advanceSearch) {
          noLblPermission = true;
        } else {
          return Response.status(Response.Status.NOT_FOUND).build();
        }
      } else {
        currentLabelName = label.getName();
      }
    }

    List<String> defOrders;
    List<String> defGroupBys;
    if (isBoardView) {
      defGroupBys = Arrays.asList(TaskUtil.NONE, TaskUtil.LABEL, /*TaskUtil.DUEDATE,*/ TaskUtil.ASSIGNEE);
      defOrders =  Arrays.asList(TaskUtil.DUEDATE, TaskUtil.PRIORITY, TaskUtil.RANK);
      if (orderBy == null || !defOrders.contains(orderBy)) {
        orderBy = TaskUtil.DUEDATE;
      }
      if (groupBy == null || !defGroupBys.contains(groupBy)) {
        groupBy = TaskUtil.NONE;
      }
    } else {
     /* defGroupBys = TaskUtil.getDefGroupBys(projectId, bundle);
      defOrders = TaskUtil.getDefOrders(bundle);*/
    }

    String currentUser = ConversationState.getCurrent().getIdentity().getUserId();
    TimeZone userTimezone = userService.getUserTimezone(currentUser);

    if (currentUser == null || currentUser.isEmpty()) {
      return Response.status(Response.Status.NOT_FOUND).build();
    }

    List<Long> spaceProjectIds = null;
    if (space_group_id != null) {
      spaceProjectIds = new LinkedList<Long>();
      List<ProjectDto> projects = ProjectUtil.flattenTree(ProjectUtil.getProjectTree(space_group_id, projectService,offset,limit), projectService,offset,limit);
      for (ProjectDto p : projects) {
        if (p.canView(currIdentity)) {
          spaceProjectIds.add(p.getId());
        }
      }
    }
    List<Long> allProjectIds = null;
    if (projectId == 0) {
      allProjectIds = new LinkedList<Long>();
      List<ProjectDto> projects = ProjectUtil.flattenTree(ProjectUtil.getProjectTree(null, projectService,offset,limit), projectService,offset,limit);
      for (ProjectDto p : projects) {
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
      StatusDto statusDto = filter.getStatus() != null ? statusService.getStatus(filter.getStatus()) : null;
      TaskUtil.buildTaskQuery(taskQuery, filter.getKeyword(), filter.getLabel(), statusDto, filter.getDue(), filter.getPriority(), filter.getAssignees(), filter.isShowCompleted(), userTimezone);
    } else {
      taskQuery.setKeyword(query);
      taskQuery.setCompleted(false);
    }

    //Get Tasks in good order
    if(projectId == ProjectUtil.INCOMING_PROJECT_ID) {
      //. Default order by CreatedDate
      if (orderBy == null || orderBy.isEmpty()) {
        orderBy = TaskUtil.CREATED_TIME;
      }
      order = new OrderBy.DESC(orderBy);

      taskQuery.setIsIncomingOf(currentUser);
      taskQuery.setOrderBy(Arrays.asList(order));
    } else if (projectId == ProjectUtil.TODO_PROJECT_ID) {
      defGroupBys =  Arrays.asList(TaskUtil.NONE, TaskUtil.PROJECT, TaskUtil.LABEL, TaskUtil.DUEDATE);
      defOrders =  Arrays.asList(TaskUtil.TITLE, TaskUtil.STATUS, TaskUtil.DUEDATE, TaskUtil.PRIORITY, TaskUtil.RANK);

      taskQuery.setIsTodoOf(currentUser);

      //TODO: process fiter here
      if ("overDue".equalsIgnoreCase(dueCategory)) {
        defGroupBys = Arrays.asList(TaskUtil.NONE, TaskUtil.PROJECT, TaskUtil.LABEL);
        defOrders = Arrays.asList(TaskUtil.TITLE, TaskUtil.PRIORITY, TaskUtil.DUEDATE);
        groupBy = groupBy == null || !defGroupBys.contains(groupBy) ? TaskUtil.PROJECT : groupBy;
      } else if ("today".equalsIgnoreCase(dueCategory)) {
        defGroupBys =  Arrays.asList(TaskUtil.NONE, TaskUtil.PROJECT, TaskUtil.LABEL);
        defOrders =  Arrays.asList(TaskUtil.TITLE, TaskUtil.PRIORITY, TaskUtil.RANK);
        if (orderBy == null) {
          order = new OrderBy.DESC(TaskUtil.PRIORITY);
          orderBy = TaskUtil.PRIORITY;
        }
        groupBy = groupBy == null || !defGroupBys.contains(groupBy) ? TaskUtil.NONE : groupBy;
      } else if ("tomorrow".equalsIgnoreCase(dueCategory)) {
        defGroupBys = Arrays.asList(TaskUtil.NONE, TaskUtil.PROJECT, TaskUtil.LABEL);
        defOrders = Arrays.asList(TaskUtil.TITLE, TaskUtil.PRIORITY, TaskUtil.RANK);
        if (orderBy == null || !defOrders.contains(orderBy)) {
          order = new OrderBy.DESC(TaskUtil.PRIORITY);
          orderBy = TaskUtil.PRIORITY;
        }
        groupBy = groupBy == null || !defGroupBys.contains(groupBy) ? TaskUtil.NONE : groupBy;
      } else if ("upcoming".equalsIgnoreCase(dueCategory)) {
        defGroupBys = Arrays.asList(TaskUtil.NONE, TaskUtil.PROJECT, TaskUtil.LABEL);
        defOrders =  Arrays.asList(TaskUtil.TITLE, TaskUtil.PRIORITY, TaskUtil.DUEDATE, TaskUtil.RANK);
        groupBy = groupBy == null || !defGroupBys.contains(groupBy) ? TaskUtil.NONE : groupBy;
      }

      if (orderBy == null || !defOrders.contains(orderBy)) {
        orderBy = TaskUtil.DUEDATE;
        order = new OrderBy.ASC(TaskUtil.DUEDATE);
      }
      if (groupBy == null || !defGroupBys.contains(groupBy)) {
        groupBy = TaskUtil.DUEDATE;
      }

      Date[] filterDate = TaskUtil.convertDueDate(dueCategory, userTimezone);
      taskQuery.setDueDateFrom(filterDate[0]);
      taskQuery.setDueDateTo(filterDate[1]);
      taskQuery.setOrderBy(Arrays.asList(order));

    } else if (projectId >= 0) {
      if (projectId == 0) {
        defGroupBys = Arrays.asList(TaskUtil.NONE, TaskUtil.ASSIGNEE, TaskUtil.PROJECT, TaskUtil.LABEL, TaskUtil.DUEDATE, TaskUtil.STATUS);

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
      defOrders =  Arrays.asList(TaskUtil.TITLE, TaskUtil.CREATED_TIME, TaskUtil.DUEDATE, TaskUtil.PRIORITY);
      if (orderBy == null || orderBy.isEmpty() || !defOrders.contains(orderBy)) {
        orderBy = TaskUtil.DUEDATE;
        order = new OrderBy.ASC(TaskUtil.DUEDATE);
      }

      if (labelId > 0) {
        defGroupBys = Arrays.asList(TaskUtil.NONE, TaskUtil.PROJECT, TaskUtil.DUEDATE, TaskUtil.STATUS);
        if (groupBy == null || groupBy.isEmpty() || !defGroupBys.contains(groupBy)) {
          groupBy = TaskUtil.NONE;
        }
        if (!noLblPermission) {
          taskQuery.setLabelIds(Arrays.asList(labelId));
        }
      } else {
        defGroupBys = Arrays.asList(TaskUtil.NONE, TaskUtil.PROJECT, TaskUtil.LABEL, TaskUtil.DUEDATE, TaskUtil.STATUS);
        if (groupBy == null || groupBy.isEmpty() || !defGroupBys.contains(groupBy)) {
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

    List<TaskDto> listTasks = null;
    if ((spaceProjectIds != null  && spaceProjectIds.isEmpty()) || ( projectId == 0 && allProjectIds.isEmpty()) ||
            (noLblPermission && labelId != null && labelId > 0) || (noProjPermission &&  projectId > 0)) {
      listTasks = new ArrayList<>();
    } else {
      listTasks = taskService.findTasks(taskQuery,offset,limit);
    }

    long tasksSize = listTasks.size();

    return Response.ok(new PaginatedTaskList(listTasks.stream().map(task -> getTaskDetails((TaskDto) task, currentUser)).collect(Collectors.toList()),tasksSize)).build();
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
                                      @ApiParam(value = "Returning the number of tasks or not", defaultValue = "false") @QueryParam("returnSize") boolean returnSize,
                                      @ApiParam(value = "Returning All Details", defaultValue = "false") @QueryParam("returnDetails") boolean returnDetails) throws Exception {
    String currentUser = ConversationState.getCurrent().getIdentity().getUserId();

    long tasksSize;
    List<?> tasks = null;
    TaskQuery taskQuery = new TaskQuery();
    List<Long> allProjectIds = new ArrayList<Long>();
    allProjectIds.add(id);
    if (limit == 0) {
      limit = -1;
    }
    taskQuery.setProjectIds(allProjectIds);
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
  }

  @POST
  @Produces(MediaType.APPLICATION_JSON)
  @RolesAllowed("users")
  @ApiOperation(value = "Add a new task", httpMethod = "POT", response = Response.class, notes = "This adds a new task.")
  @ApiResponses(value = { @ApiResponse(code = 200, message = "Request fulfilled"),
      @ApiResponse(code = 400, message = "Invalid query input"), @ApiResponse(code = 403, message = "Unauthorized operation"),
      @ApiResponse(code = 404, message = "Resource not found") })
  public Response addTask(@ApiParam(value = "task object to be updated", required = true) TaskDto task) throws Exception {
    if (task == null) {
      return Response.status(Response.Status.BAD_REQUEST).build();
    }
    String currentUser = ConversationState.getCurrent().getIdentity().getUserId();
    task.setCreatedBy(currentUser);
    task.setCreatedTime(new Date());
    if(task.getStatus()==null||task.getStatus().getProject()==null){
      task.setAssignee(currentUser);
      task.setStatus(null);
    }
    if(task.getStatus()!=null&&task.getStatus().getId()==0&&task.getStatus().getProject()!=null) {
      StatusStorage statusStorage = CommonsUtils.getService(StatusStorage.class);
      task.setStatus(statusStorage.statusToEntity(statusService.getDefaultStatus(task.getStatus().getProject().getId())));
    }
    task = taskService.createTask(task);
    return Response.ok(task).build();
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
    List<ChangeLogEntry> arr = taskService.getTaskLogs(id, offset, limit);
    if (arr == null) {
      return Response.ok(Collections.emptyList()).build();
    }
    List<ChangeLogEntry> logs = new LinkedList<ChangeLogEntry>(arr);
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
      if (comment.getSubComments()!=null&&!comment.getSubComments().isEmpty()) {
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
    if (!TaskUtil.canDeleteComment(currentIdentity, comment)) {
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
        JSONArray statusStats = new JSONArray();
        if (statusObjects != null && statusObjects.size() > 0) {

          for (Object[] result : statusObjects) {
            JSONObject statJson = new JSONObject();
            statJson.put("status", (String) result[0]);
            statJson.put("taskNumber", ((Number) result[1]).intValue());
            statusStats.put(statJson);
          }

        }
        projectJson.put("statusStats", statusStats);
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
        if (space != null) {
          JSONObject spaceJson = new JSONObject();
          spaceJson.put("prettyName", space.getPrettyName());
          spaceJson.put("url", space.getUrl());
          spaceJson.put("displayName", space.getDisplayName());
          spaceJson.put("id", space.getId());
          spaceJson.put("avatarUrl", space.getAvatarUrl());
          spaceJson.put("description", space.getDescription());
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
 /*   try {
      labels = labelService.findLabelsByTask(taskId, userName, 0, -1);
    } catch (Exception e) {
      LOG.warn("Error retrieving task '{}' labels", taskId, e);
    }*/
    Space space = null;
    if (task.getStatus() != null && task.getStatus().getProject() != null) {
      space = getProjectSpace(task.getStatus().getProject());
    }

    TaskEntity taskEntity = new TaskEntity(((TaskDto) task), commentCount);
    taskEntity.setLabels(labels);
    taskEntity.setSpace(space);
    return taskEntity;
  }

  private Space getProjectSpace(Project project) {
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

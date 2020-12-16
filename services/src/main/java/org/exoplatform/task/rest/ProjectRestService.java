package org.exoplatform.task.rest;

import io.swagger.annotations.*;
import org.apache.commons.lang3.StringUtils;
import org.exoplatform.commons.utils.HTMLEntityEncoder;
import org.exoplatform.commons.utils.ListAccess;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.services.rest.resource.ResourceContainer;
import org.exoplatform.services.security.ConversationState;
import org.exoplatform.services.security.Identity;
import org.exoplatform.social.core.space.model.Space;
import org.exoplatform.social.core.space.spi.SpaceService;
import org.exoplatform.task.dao.ProjectQuery;
import org.exoplatform.task.dto.ProjectDto;
import org.exoplatform.task.dto.StatusDto;
import org.exoplatform.task.exception.EntityNotFoundException;
import org.exoplatform.task.exception.ParameterEntityException;
import org.exoplatform.task.exception.UnAuthorizedOperationException;
import org.exoplatform.task.legacy.service.UserService;
import org.exoplatform.task.model.User;
import org.exoplatform.task.service.*;
import org.exoplatform.task.util.ProjectUtil;
import org.exoplatform.task.util.StringUtil;
import org.exoplatform.task.util.UserUtil;
import org.gatein.common.text.EntityEncoder;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import javax.annotation.security.RolesAllowed;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.*;

@Path("/projects")
@Api(value = "/projects", description = "Managing tasks")
@RolesAllowed("users")
public class ProjectRestService implements ResourceContainer {

  private static final Log LOG           = ExoLogger.getLogger(ProjectRestService.class);

  private TaskService taskService;

  private ProjectService projectService;

  private StatusService statusService;

  private UserService userService;

  private SpaceService spaceService;

  private LabelService labelService;

  private CommentService commentService;

  public ProjectRestService(TaskService taskService,
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
  @Path("projects")
  @RolesAllowed("users")
  @Produces(MediaType.APPLICATION_JSON)
  @ApiOperation(value = "Gets projects", httpMethod = "GET", response = Response.class, notes = "This returns projects of the authenticated user")
  @ApiResponses(value = { @ApiResponse(code = 200, message = "Request fulfilled"),
      @ApiResponse(code = 500, message = "Internal server error") })
  public Response getProjects(@ApiParam(value = "Search term", required = false, defaultValue = "null") @QueryParam("q") String query,
                              @ApiParam(value = "Space Name", required = false, defaultValue = "null") @QueryParam("spaceName") String spaceName,
                              @ApiParam(value = "Filter", required = false, defaultValue = "") @QueryParam("projectsFilter") String projectsFilter,
                              @ApiParam(value = "Offset", required = false, defaultValue = "0") @QueryParam("offset") int offset,
                              @ApiParam(value = "Limit", required = false, defaultValue = "-1") @QueryParam("limit") int limit,
                              @ApiParam(value = "Participator Need", required = false, defaultValue = "false") @QueryParam("participatorParam") boolean participatorParam) {
    if (limit == 0) {
      limit = -1;
    }

    List<String> memberships = new LinkedList<String>();
    ConversationState state = ConversationState.getCurrent();
    Identity identity = state.getIdentity();
    List<ProjectDto> projects = new ArrayList<>();
    int projectNumber = 0;
    if(projectsFilter!=null && projectsFilter.equals("MANAGED")){
      List<String> managers = new ArrayList<>();
      managers.add(identity.getUserId());
      ProjectQuery projectQuery = new ProjectQuery();
      projectQuery.setManager(managers);
      projectQuery.setKeyword(query);
      List<ProjectDto> projectDtoList = projectService.findProjects(projectQuery, offset, limit);
      projectNumber = projectService.countProjects(projectQuery);
      projects = ProjectUtil.getProjectTree(projectDtoList, identity);
    }else if(projectsFilter!=null && projectsFilter.equals("COLLABORATED")){
      List<ProjectDto> projectDtoList = projectService.findCollaboratedProjects(identity.getUserId(),query,offset, limit);
      projectNumber = projectService.countCollaboratedProjects(identity.getUserId(),query);
      projects = ProjectUtil.getProjectTree(projectDtoList, identity);
    }else if(projectsFilter!=null && projectsFilter.equals("WITH_TASKS")){
      memberships.addAll(UserUtil.getMemberships(identity));
      List<ProjectDto> projectDtoList = projectService.findNotEmptyProjects(memberships,query,offset, limit);
      projectNumber = projectService.countNotEmptyProjects(memberships,query);
      projects = ProjectUtil.getProjectTree(projectDtoList, identity);
    }else {
      if(StringUtils.isNoneEmpty(spaceName)){
        Space space = spaceService.getSpaceByPrettyName(spaceName);
        if(space!=null){
          memberships.addAll(UserUtil.getSpaceMemberships(space.getGroupId()));
        }
      }else{
        memberships.addAll(UserUtil.getMemberships(identity));
      }
      List<ProjectDto> projectDtoList = projectService.findProjects(memberships,query,null,offset, limit);
      projects = ProjectUtil.getProjectTree(projectDtoList, identity);
      projectNumber = projectService.countProjects(memberships, query);
    }
    JSONObject global = new JSONObject();
    JSONArray projectsJsonArray = new JSONArray();
    try {
      projectsJsonArray = buildJSON(projectsJsonArray, projects, participatorParam);
      global.put("projects",projectsJsonArray);
      global.put("projectNumber",projectNumber);
    } catch (Exception e) {
      LOG.error("Error getting projects", e);
      return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
    }
    return Response.ok(global.toString()).build();
  }


  @GET
  @Path("projects/{id}")
  @RolesAllowed("users")
  @Produces(MediaType.APPLICATION_JSON)
  @ApiOperation(value = "Gets  project by id", httpMethod = "GET", response = Response.class, notes = "This returns the default status by project id")
  @ApiResponses(value = { @ApiResponse(code = 200, message = "Request fulfilled"),
      @ApiResponse(code = 403, message = "Unauthorized operation"), @ApiResponse(code = 500, message = "Internal server error") })
  public Response getProjectById(@ApiParam(value = "Project id", required = true) @PathParam("id") long id,
                                 @ApiParam(value = "Participator Need", required = false, defaultValue = "false") @QueryParam("participatorParam") boolean participatorParam) throws EntityNotFoundException {
    Identity currentUser = ConversationState.getCurrent().getIdentity();
    ProjectDto project = projectService.getProject(id);
    if (project == null) {
      return Response.status(Response.Status.NOT_FOUND).build();
    }
    if (!project.canView(currentUser)) {
      return Response.status(Response.Status.UNAUTHORIZED).build();
    }
    try{
    return Response.ok(buildJsonProject(project, participatorParam).toString()).build();
    } catch (JSONException e) {
      LOG.error("Canit get Project with id {}, id",e);
    }
    return Response.status(Response.Status.NOT_FOUND).build();
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
  @Path("project/statistics/{id}")
  @RolesAllowed("users")
  @Produces(MediaType.APPLICATION_JSON)
  @ApiOperation(value = "Gets users by query and project name", httpMethod = "GET", response = Response.class, notes = "This returns users by query and project name")
  @ApiResponses(value = { @ApiResponse(code = 200, message = "Request fulfilled") })
  public Response getProjectsStatistics(@ApiParam(value = "id", required = true) @PathParam("id") long id ) throws Exception {

    HashMap<String, Integer> hm = new HashMap<String, Integer>();
     for(StatusDto statusDto : statusService.getStatuses(id)){
       hm.put(statusDto.getName(),0);
     }
    int tasksNum = 0;
    JSONObject projectJson = new JSONObject();
    List<Object[]> statusObjects = taskService.countTaskStatusByProject(id);
    JSONArray statusStats = new JSONArray();
    if (statusObjects != null && statusObjects.size() > 0) {

      for (Object[] result : statusObjects) {
        hm.put((String) result[0],((Number) result[1]).intValue());
        tasksNum+=((Number) result[1]).intValue();
      }
      for (Map.Entry me : hm.entrySet()) {
        JSONObject statJson = new JSONObject();
        statJson.put("name",me.getKey());
        statJson.put("value",  me.getValue());
        statusStats.put(statJson);      }
    }
    projectJson.put("statusStats", statusStats);
    projectJson.put("totalNumberTasks", tasksNum);
    return Response.ok(projectJson.toString()).build();
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


  private JSONArray buildJSON(JSONArray projectsJsonArray, List<ProjectDto> projects, boolean participatorParam) throws JSONException {
    Identity currentUser = ConversationState.getCurrent().getIdentity();
    for (ProjectDto project : projects) {
      if (project.canView(currentUser)) {
        projectsJsonArray.put(buildJsonProject(project,participatorParam));
      }
    }
    return projectsJsonArray;
  }

  private JSONObject buildJsonProject(ProjectDto project, boolean participatorParam) throws JSONException{
    
      long projectId = project.getId();
      Space space = null;
      Set<String> projectManagers = projectService.getManager(projectId);
      Set<String> managers = new LinkedHashSet();
      Set<String> projectParticipators = projectService.getParticipator(projectId);
      Set<String> participators = new LinkedHashSet();
    JSONObject projectJson = new JSONObject();

    if (projectManagers.size() > 0) {
        for (String permission : projectService.getManager(projectId)) {
          int index = permission.indexOf(':');
          if (index > -1) {
            String groupId = permission.substring(index + 1);
            space = spaceService.getSpaceByGroupId(groupId);
            if(space!=null) managers.addAll(Arrays.asList(space.getManagers()));
          } else {
            managers.add(permission);
          }
        }
      }

    if (projectParticipators.size() > 0 && participatorParam) {
      for (String permission : projectService.getParticipator(projectId)) {
        int index = permission.indexOf(':');
        if (index > -1) {
          String groupId = permission.substring(index + 1);
          space = spaceService.getSpaceByGroupId(groupId);
          if(space!=null)  participators.addAll(Arrays.asList(space.getMembers()));
        } else {
          participators.add(permission);
        }
      }
      if (participators.size() > 0) {
        JSONArray participatorsJsonArray = new JSONArray();
        for (String usr : participators) {
          JSONObject participator = new JSONObject();
          User user_ = UserUtil.getUser(usr);
          participator.put("username", user_.getUsername());
          participator.put("email", user_.getEmail());
          participator.put("displayName", user_.getDisplayName());
          participator.put("avatar", user_.getAvatar());
          participator.put("url", user_.getUrl());
          participator.put("enable", user_.isEnable());
          participator.put("deleted", user_.isDeleted());
          participatorsJsonArray.put(participator);
        }
        projectJson.put("participatorIdentities", participatorsJsonArray);
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
      //projectJson.put("children", projectService.getSubProjects(projectId, 0, -1));
      projectJson.put("dueDate", project.getDueDate());
      projectJson.put("description", project.getDescription());
      projectJson.put("status", statusService.getStatus(projectId));
      projectJson.put("canManage", project.canEdit(ConversationState.getCurrent().getIdentity()));
      return projectJson;
  }

  @POST
  @Path("createproject")
  @RolesAllowed("users")
  @ApiOperation(value = "Adds a project", httpMethod = "POST", response = Response.class, notes = "This Adds project")
  @ApiResponses(value = {@ApiResponse(code = 200, message = "Request fulfilled"),
          @ApiResponse(code = 400, message = "Invalid query input"),
          @ApiResponse(code = 403, message = "Unauthorized operation"),
          @ApiResponse(code = 404, message = "Resource not found")})
  public Response createProject(@ApiParam(value = "ProjectDto", required = true) ProjectDto projectDto) throws EntityNotFoundException, JSONException, UnAuthorizedOperationException {

    String currentUser = ConversationState.getCurrent().getIdentity().getUserId();
    if (currentUser == null) {
      return Response.status(Response.Status.FORBIDDEN).build();
    }

    if (projectDto.getName() == null || projectDto.getName().isEmpty()) {
      return Response.status(Response.Status.NOT_FOUND).build();
    }
    String description = StringUtil.encodeInjectedHtmlTag(projectDto.getDescription());
    ProjectDto project;
    Space space = null;
    if(StringUtils.isNotBlank(projectDto.getSpaceName())){
      space = spaceService.getSpaceByPrettyName(projectDto.getSpaceName());
    }
    if (space != null) {
      List<String> memberships = UserUtil.getSpaceMemberships(space.getId());
      Set<String> managers = new HashSet<String>(Arrays.asList(currentUser, memberships.get(0)));
      Set<String> participators = new HashSet<String>(Arrays.asList(memberships.get(1)));
      project = ProjectUtil.newProjectInstanceDto(projectDto.getName(), description, managers, participators);
    } else if ((projectDto.getManager()!=null && projectDto.getManager().size() !=0 )|| (projectDto.getParticipator()!=null && projectDto.getParticipator().size() !=0)){
      Set<String> managers = new HashSet<String>();
      if (projectDto.getManager().size() ==0 ){
        managers.add(currentUser);
      }else{
        projectDto.getManager().forEach(name -> {
          managers.add(name);
        });
      }
      Set<String> paticipator = new HashSet<String>();
      if (projectDto.getParticipator()!=null) {
        projectDto.getParticipator().forEach(name -> {
          paticipator.add(name);
        });
      }
      project = ProjectUtil.newProjectInstanceDto(projectDto.getName(), description, managers, paticipator);
    }else{
      project = ProjectUtil.newProjectInstanceDto(projectDto.getName(), description, currentUser);
    }
    if (projectDto.getParent() != null) {
      Long parentId = projectDto.getParent().getId();
      ProjectDto parent = projectService.getProject(parentId);
      if (!parent.canEdit(ConversationState.getCurrent().getIdentity())) {
        return Response.status(Response.Status.UNAUTHORIZED).build();
      }
      project = projectService.createProject(project, parentId);
    } else {
      project = projectService.createProject(project);
      statusService.createInitialStatuses(project);
    }

    EntityEncoder encoder = HTMLEntityEncoder.getInstance();
    JSONObject result = new JSONObject();
    result.put("id", project.getId());
    result.put("name", encoder.encode(project.getName()));
    result.put("color", "transparent");

    return Response.ok(result.toString()).build();
  }


  @PUT
  @Path("updateproject/{projectId}")
  @Produces(MediaType.APPLICATION_JSON)
  @RolesAllowed("users")
  @ApiOperation(value = "Update Project", httpMethod = "POST", response = Response.class, notes = "This Update Project info")
  @ApiResponses(value = { @ApiResponse(code = 200, message = "Request fulfilled"),
  @ApiResponse(code = 400, message = "Invalid query input"),
  @ApiResponse(code = 403, message = "Unauthorized operation"),
  @ApiResponse(code = 404, message = "Resource not found") })
  public Response updateProject(@ApiParam(value = "projectId", required = true) @PathParam("projectId") long projectId,
                                  @ApiParam(value = "Project", required = true) ProjectDto projectDto)
          throws EntityNotFoundException, ParameterEntityException, UnAuthorizedOperationException {
    if(projectDto.getName() == null) {
      return Response.status(Response.Status.NOT_FOUND).build();
    }
    Identity identity = ConversationState.getCurrent().getIdentity();
    if (projectDto.getParent()!= null && !projectDto.getParent().toString() .isEmpty()) {
      Long parentId = Long.parseLong(projectDto.getParent().toString());
      try {
        if (!projectService.getProject(parentId).canEdit(identity)) {
          return Response.status(Response.Status.UNAUTHORIZED).build();
        }
      } catch (EntityNotFoundException ex) {
      }
    }
    if (!projectService.getProject(projectId).canEdit(identity)) {
      return Response.status(Response.Status.UNAUTHORIZED).build();
    }

    Map<String, String[]> fields = new HashMap<String, String[]>();
    Map<String, Set<String>> fieldsSet = new HashMap<>();
    fields.put("name", new String[] {projectDto.getName()});
    String description = StringUtil.encodeInjectedHtmlTag(projectDto.getDescription());
    fields.put("description", new String[] {description});
    if (projectDto.getParent()!=null) {
      fields.put("parent", new String[]{projectDto.getParent().toString()});
    }
    if (projectDto.getManager()!=null){
      Set<String> managers = new HashSet<String>();
      projectDto.getManager().forEach(name -> {
        managers.add(name);
      });
      String[] array = managers.toArray(new String[0]);
      fieldsSet.put("manager",managers);
      fields.put("manager", array);
    }

    if (projectDto.getParticipator()!=null){
      Set<String> participators = new HashSet<String>();
      projectDto.getParticipator().forEach(name -> {
        participators.add(name);
      });
      String[] array = participators.toArray(new String[0]);
      fieldsSet.put("participator",participators);
      fields.put("participator", array);
    }

    ProjectDto project = ProjectUtil.saveProjectField(projectService, projectId, fields);
    projectService.updateProject(project);
    return Response.ok(Response.Status.OK).build();
  }

  @DELETE
  @Path("{projectId}")
  @Consumes(MediaType.APPLICATION_JSON)
  @RolesAllowed("users")
  @ApiOperation(value = "Delete Project", httpMethod = "DELETE", response = Response.class, notes = "This deletes the Project", consumes = "application/json")
  @ApiResponses(value = { @ApiResponse(code = 200, message = "Project deleted"),
          @ApiResponse(code = 400, message = "Invalid query input"),
          @ApiResponse(code = 401, message = "User not authorized to delete the Project"),
          @ApiResponse(code = 500, message = "Internal server error") })
  public Response deleteProject(@ApiParam(value = "projectId", required = true) @PathParam("projectId") Long projectId,
                                @ApiParam(value = "deleteChild", defaultValue = "false") @QueryParam("deleteChild")Boolean deleteChild,
                                @ApiParam(value = "Offset", required = false, defaultValue = "0") @QueryParam("offset") int offset,
                                @ApiParam(value = "Limit", required = false, defaultValue = "-1") @QueryParam("limit") int limit) throws EntityNotFoundException, UnAuthorizedOperationException {
    Identity identity = ConversationState.getCurrent().getIdentity();
    ProjectDto project = projectService.getProject(projectId);
    if (!project.canEdit(identity)) {
      return Response.status(Response.Status.UNAUTHORIZED).build();
    } else if (deleteChild) {
      List<ProjectDto> childs = projectService.getSubProjects(projectId,offset,limit);
      for (ProjectDto child : childs) {
        if (!child.canEdit(identity)) {
          return Response.status(Response.Status.UNAUTHORIZED).build();
        }
      }
    }

    projectService.removeProject(projectId, deleteChild);
    return Response.ok(Response.Status.OK).build();
  }

  @POST
  @Path("cloneproject")
  @RolesAllowed("users")
  @ApiOperation(value = "Clone a project", httpMethod = "POST", response = Response.class, notes = "This Clone project")
  @ApiResponses(value = {@ApiResponse(code = 200, message = "Request fulfilled"),
  @ApiResponse(code = 400, message = "Invalid query input"),
  @ApiResponse(code = 403, message = "Unauthorized operation"),
  @ApiResponse(code = 404, message = "Resource not found")})
  public Response cloneProject(@ApiParam(value = "ProjectDto", required = true) ProjectDto projectDto) throws Exception {

    ProjectDto currentProject = projectDto;
    if (!currentProject.canEdit(ConversationState.getCurrent().getIdentity())) {
      return Response.status(Response.Status.UNAUTHORIZED).build();
    }
    ProjectDto project = projectService.cloneProject(projectDto.getId(), Boolean.parseBoolean("true")); //Can throw ProjectNotFoundException

    EntityEncoder encoder = HTMLEntityEncoder.getInstance();
    JSONObject result = new JSONObject();
    result.put("id", project.getId());
    result.put("name", encoder.encode(project.getName()));
    result.put("color", project.getColor());

    return Response.ok(Response.Status.OK).build();
  }


  @PUT
  @Path("changeProjectColor/{projectId}")
  @Produces(MediaType.APPLICATION_JSON)
  @RolesAllowed("users")
  @ApiOperation(value = "Change Project Color", httpMethod = "POST", response = Response.class, notes = "This change Project Color")
  @ApiResponses(value = { @ApiResponse(code = 200, message = "Request fulfilled"),
          @ApiResponse(code = 400, message = "Invalid query input"),
          @ApiResponse(code = 403, message = "Unauthorized operation"),
          @ApiResponse(code = 404, message = "Resource not found") })
  public Response changeProjectColor(@ApiParam(value = "projectId", required = true) @PathParam("projectId") Long projectId,
                                     @ApiParam(value = "color", required = false, defaultValue = "null") @QueryParam("color") String color)
          throws EntityNotFoundException, ParameterEntityException, UnAuthorizedOperationException {
    Map<String, String[]> fields = new HashMap<String, String[]>();
    fields.put("color", new String[] {color});

    ProjectDto project = projectService.getProject(projectId);
    if (!project.canEdit(ConversationState.getCurrent().getIdentity())) {
      return Response.status(Response.Status.UNAUTHORIZED).build();
    }

    project = ProjectUtil.saveProjectField(projectService, projectId, fields);
    projectService.updateProject(project);
    return Response.ok(Response.Status.OK).build();
  }


}

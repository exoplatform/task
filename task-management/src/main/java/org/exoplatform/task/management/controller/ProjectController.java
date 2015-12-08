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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;

import juzu.MimeType;
import juzu.Path;
import juzu.Resource;
import juzu.Response;
import juzu.impl.common.Tools;
import juzu.request.SecurityContext;

import org.exoplatform.common.http.HTTPStatus;
import org.exoplatform.commons.juzu.ajax.Ajax;
import org.exoplatform.commons.utils.HTMLEntityEncoder;
import org.exoplatform.commons.utils.ListAccess;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.services.organization.Group;
import org.exoplatform.services.organization.MembershipHandler;
import org.exoplatform.services.organization.MembershipType;
import org.exoplatform.services.organization.MembershipTypeHandler;
import org.exoplatform.services.organization.OrganizationService;
import org.exoplatform.services.organization.Query;
import org.exoplatform.services.organization.UserHandler;
import org.exoplatform.services.security.ConversationState;
import org.exoplatform.services.security.Identity;
import org.exoplatform.task.domain.Project;
import org.exoplatform.task.domain.UserSetting;
import org.exoplatform.task.exception.EntityNotFoundException;
import org.exoplatform.task.exception.ParameterEntityException;
import org.exoplatform.task.exception.UnAuthorizedOperationException;
import org.exoplatform.task.model.Permission;
import org.exoplatform.task.model.User;
import org.exoplatform.task.model.UserGroup;
import org.exoplatform.task.service.ProjectService;
import org.exoplatform.task.service.StatusService;
import org.exoplatform.task.service.UserService;
import org.exoplatform.task.util.ListUtil;
import org.exoplatform.task.util.ProjectUtil;
import org.exoplatform.task.util.UserUtil;
import org.gatein.common.text.EntityEncoder;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * @author <a href="mailto:tuyennt@exoplatform.com">Tuyen Nguyen The</a>.
 */
public class ProjectController extends AbstractController {

  @Inject
  ResourceBundle bundle;
  
  @Inject
  ProjectService projectService;

  @Inject
  StatusService statusService;

  @Inject
  UserService userService;

  @Inject
  OrganizationService orgService;

  @Inject
  @Path("projectForm.gtmpl")
  org.exoplatform.task.management.templates.projectForm form;

  @Inject
  @Path("projectDetail.gtmpl")
  org.exoplatform.task.management.templates.projectDetail detail;

  @Inject
  @Path("listProjects.gtmpl")
  org.exoplatform.task.management.templates.listProjects listProjects;

  @Inject
  @Path("shareDialog.gtmpl")
  org.exoplatform.task.management.templates.shareDialog shareDialog;
  
  @Inject
  @Path("confirmDeleteProject.gtmpl")
  org.exoplatform.task.management.templates.confirmDeleteProject confirmDeleteProject;

  @Inject
  @Path("userSelectorDialog.gtmpl")
  org.exoplatform.task.management.templates.userSelectorDialog userSelectorDialog;

  @Inject
  @Path("groupSelectorDialog.gtmpl")
  org.exoplatform.task.management.templates.groupSelectorDialog groupSelectorDialog;

  @Inject
  @Path("projectSearchResult.gtmpl")
  org.exoplatform.task.management.templates.projectSearchResult projectSearchResult;

  @Inject
  @Path("permissionSuggest.gtmpl")
  org.exoplatform.task.management.templates.permissionSuggest permissionSuggest;

  private static final Log LOG = ExoLogger.getExoLogger(ProjectController.class);

  @Resource
  @Ajax
  @MimeType.HTML
  public Response projectForm(Long parentId, SecurityContext securityContext) throws UnAuthorizedOperationException {    
    Project parent;
    try {
      parent = projectService.getProject(parentId);
      Identity identity = ConversationState.getCurrent().getIdentity();
      if (!parent.canEdit(identity)) {
        throw new UnAuthorizedOperationException(parentId, Project.class, getNoPermissionMsg());
      }
    } catch (EntityNotFoundException e) {
      parent = new Project();
    }
    
    User user = userService.loadUser(securityContext.getRemoteUser());    
    
    return form
        .with()
        .breadcumbs(ProjectUtil.buildBreadcumbs(parent.getId(), projectService, bundle))
        .parent(parent)        
        .user(user)
        .ok().withCharset(Tools.UTF_8);
  }

  @Resource
  @Ajax
  @MimeType.JSON
  public Response createProject(String space_group_id, String name, String description, Long parentId, Boolean calInteg, SecurityContext securityContext) throws EntityNotFoundException, JSONException, UnAuthorizedOperationException {

    String currentUser = securityContext.getRemoteUser();
    if(currentUser == null) {
      return Response.status(401).body("You must login to create new project");
    }

    if(name == null || name.isEmpty()) {
      return Response.status(412).body("Name of project is required");
    }

    Project project;
    if (space_group_id  != null) {
      List<String> memberships = UserUtil.getSpaceMemberships(space_group_id);
      Set<String> managers = new HashSet<String>(Arrays.asList(currentUser, memberships.get(0)));
      Set<String> participators = new HashSet<String>(Arrays.asList(memberships.get(1)));
      //project = projectService.createDefaultStatusProjectWithAttributes(parentId, name, description, managers, participators);
      project = ProjectUtil.newProjectInstance(name, description, managers, participators);
    } else {
      project = ProjectUtil.newProjectInstance(name, description, currentUser);
      //project = projectService.createDefaultStatusProjectWithManager(name, description, parentId, currentUser); //Can throw ProjectNotFoundException
    }
    
    calInteg = calInteg == null ? false : calInteg;
    project.setCalendarIntegrated(calInteg);

    if (parentId != null && parentId > 0) {
      Project parent = projectService.getProject(parentId);
      if (!parent.canEdit(ConversationState.getCurrent().getIdentity())) {
        throw new UnAuthorizedOperationException(parentId, Project.class, getNoPermissionMsg());
      }
      project = projectService.createProject(project, parentId);
    } else {
      project = projectService.createProject(project);
      statusService.createInitialStatuses(project);
    }

    EntityEncoder encoder = HTMLEntityEncoder.getInstance();
    JSONObject result = new JSONObject();
    result.put("id", project.getId());//Can throw JSONException (same for all #json.put methods below)
    result.put("name", encoder.encode(project.getName()));
    result.put("color", "transparent");

    return Response.ok(result.toString()).withCharset(Tools.UTF_8);
  }

  @Resource
  @Ajax
  @MimeType.JSON
  public Response cloneProject(Long id, String cloneTask, SecurityContext securityContext) throws EntityNotFoundException, JSONException, UnAuthorizedOperationException {

    Project currentProject = projectService.getProject(id);
    if (!currentProject.canEdit(ConversationState.getCurrent().getIdentity())) {
      throw new UnAuthorizedOperationException(id, Project.class, getNoPermissionMsg());
    }
    Project project = projectService.cloneProject(id, Boolean.parseBoolean(cloneTask)); //Can throw ProjectNotFoundException

    EntityEncoder encoder = HTMLEntityEncoder.getInstance();
    JSONObject result = new JSONObject();
    result.put("id", project.getId());
    result.put("name", encoder.encode(project.getName()));
    result.put("color", project.getColor());

    return Response.ok(result.toString()).withCharset(Tools.UTF_8);
  }

  @Resource
  @Ajax
  @MimeType.HTML
  public Response openConfirmDelete(Long id) throws EntityNotFoundException, UnAuthorizedOperationException {
    Project project = projectService.getProject(id); //Can throw ProjectNotFoundException
    if (project != null) {
      if (!project.canEdit(ConversationState.getCurrent().getIdentity())) {
        throw new UnAuthorizedOperationException(id, Project.class, getNoPermissionMsg());
      }
      EntityEncoder encoder = HTMLEntityEncoder.getInstance();
      String msg = bundle.getString("popup.msg.deleteProject");
      msg = msg.replace("{}", encoder.encode(project.getName()));

      return confirmDeleteProject.with().pid(project.getId()).msg(msg)
          .ok().withCharset(Tools.UTF_8);
    } else {
      return Response.status(404);
    }
  }
  
  @Resource
  @Ajax
  @MimeType.HTML
  public Response openShareDialog(Long id) throws EntityNotFoundException, UnAuthorizedOperationException {
    Project project = projectService.getProject(id); //Can throw ProjectNotFoundException
    if (!project.canEdit(ConversationState.getCurrent().getIdentity())) {
      throw new UnAuthorizedOperationException(id, Project.class, getNoPermissionMsg());
    }
    return renderShareDialog(project, "");
  }

  /*@Resource
  @Ajax
  @MimeType.HTML
  public Response removePermission(Long id, String permission, String type) {

    try {

      Project project = projectService.removePermissionFromProjectId(id, permission, type); //Can throw ProjectNotFoundException & NotAllowedOperationOnEntityException
      return renderShareDialog(project, type);

    } catch (AbstractEntityException e) {
      return Response.status(e.getHttpStatusCode()).body(e.getMessage());
    }
  }*/

  @Resource
  @Ajax
  @MimeType.HTML
  public Response openUserSelector(Long id, String type, Integer page, String groupId, String keyword, String filter) throws EntityNotFoundException, Exception {
    int pageSize = 10;
    int total = 0;
    if (page == null) {
      page = 0;
    }
    boolean hasNext = true;
    Project project = projectService.getProject(id); //Can throw ProjectNotFoundException

    UserHandler uHandler = orgService.getUserHandler();
    List<org.exoplatform.services.organization.User> tmp = null;
    if (keyword != null && !keyword.isEmpty()) {
      String searchKeyword = keyword;
      if (searchKeyword.indexOf("*") < 0) {
        if (searchKeyword.charAt(0) != '*')
          searchKeyword = "*" + searchKeyword;
        if (searchKeyword.charAt(searchKeyword.length() - 1) != '*')
          searchKeyword += "*";
      }
      searchKeyword = searchKeyword.replace('?', '_');
      Query q = new Query();
      if ("userName".equals(filter)) {
        q.setUserName(searchKeyword);
      }
      if ("lastName".equals(filter)) {
        q.setLastName(searchKeyword);
      }
      if ("firstName".equals(filter)) {
        q.setFirstName(searchKeyword);
      }
      if ("email".equals(filter)) {
        q.setEmail(searchKeyword);
      }
      ListAccess<org.exoplatform.services.organization.User> users = uHandler.findUsersByQuery(q);
      total = ListUtil.getSize(users);
      if (pageSize > total) {
        pageSize = total;
      }
      tmp = new ArrayList<org.exoplatform.services.organization.User>();
      org.exoplatform.services.organization.User[] uArr = new org.exoplatform.services.organization.User[0];
      try {
        uArr = users.load(page * pageSize, pageSize);
      } catch (IllegalArgumentException ex) {
        //workaround because uHandler doesn't support search using keyword and group in one query
        page = page - 1;
        uArr = users.load(page * pageSize, pageSize);
        hasNext = false;
      }
      for (org.exoplatform.services.organization.User u : uArr) {
        tmp.add(u);
      }
    }

    if (groupId != null && !groupId.isEmpty()) {
      if (tmp == null) {
        ListAccess<org.exoplatform.services.organization.User> users = uHandler.findUsersByGroupId(groupId);
        total = users.getSize();
        if (pageSize > total) {
          pageSize = total;
        }
        tmp = new ArrayList<org.exoplatform.services.organization.User>();
        org.exoplatform.services.organization.User[] uArr = new org.exoplatform.services.organization.User[0];
        try {
          uArr = users.load(page * pageSize, pageSize);
        } catch (IllegalArgumentException ex) {
          //workaround because uHandler doesn't support search using keyword and group in one query
          page = page - 1;
          uArr = users.load(page * pageSize, pageSize);
          hasNext = false;
        }
        for (org.exoplatform.services.organization.User u : uArr) {
          tmp.add(u);
        }
      } else {
        MembershipHandler memberShipHandler = orgService.getMembershipHandler();
        List<org.exoplatform.services.organization.User> results = new CopyOnWriteArrayList();
        results.addAll(tmp);
        for (org.exoplatform.services.organization.User u : tmp) {
          if (memberShipHandler.findMembershipsByUserAndGroup(u.getUserName(), groupId).size() == 0) {
            results.remove(u);
          }
        }
        tmp = results;
        total = tmp.size() == 0 ? 0 : -1;
      }
    }
    if (tmp == null) {
      ListAccess<org.exoplatform.services.organization.User> users = uHandler.findAllUsers();
      total = users.getSize();
      if (pageSize > total) {
        pageSize = total;
      }
      tmp = new ArrayList<org.exoplatform.services.organization.User>();
      for (org.exoplatform.services.organization.User u : users.load(page * pageSize, pageSize)) {
        tmp.add(u);
      }
    }

    Set<User> allUsers = new HashSet<User>();
    if (tmp != null) {
      for (org.exoplatform.services.organization.User u : tmp) {
        User user = userService.loadUser(u.getUserName());
        allUsers.add(user);
      }
    }
    if ("manager".equals(type)) {
      allUsers.removeAll(project.getManager());
      total -= project.getManager().size();
    } else {
      allUsers.removeAll(project.getParticipator());
      total -= project.getParticipator().size();
    }

    int totalPage = total >= 0 ? (int)Math.ceil(total / pageSize) : -1;

    return userSelectorDialog.with()
            .type(type)
            .users(allUsers)
            .groupId(groupId == null ? "" : groupId)
            .keyword(keyword == null ? "" : keyword)
            .filter(filter == null ? "" : filter)
            .currentPage(page)
            .totalPage(totalPage)
            .hasNext(hasNext)
            .ok();

  }

  @Resource
  @Ajax
  @MimeType.HTML
  public Response openGroupSelector(String type, Boolean onlyGroup) {

    Collection groups, msTypes;
    try {
      groups = orgService.getGroupHandler().getAllGroups();
      msTypes = orgService.getMembershipTypeHandler().findMembershipTypes();
    } catch (Exception e) {// NOSONAR
      return Response.status(503).body(e.getMessage());
    }

    List<UserGroup> allGroups = new ArrayList<UserGroup>();
    if (groups != null) {
      allGroups = UserUtil.buildGroupTree(groups);
    }

    List<String> allMSTypes = new ArrayList<String>();
    if (msTypes != null) {
      for (Object mst : msTypes) {
        allMSTypes.add(((MembershipType)mst).getName());
      }
    }

    if (onlyGroup == null) {
      onlyGroup = Boolean.FALSE;
    }

    return groupSelectorDialog.with().type(type)
        .groups(allGroups)
        .membershipTypes(allMSTypes)
        .groupOnly(onlyGroup)
        .ok();

  }

  @Resource
  @Ajax
  @MimeType.HTML
  public Response findPermission(String keyword) {
    List<Permission> permissions = new ArrayList<Permission>();

    try {
      ListAccess<User> list = userService.findUserByName(keyword);
      for(User u : list.load(0, UserUtil.SEARCH_LIMIT)) {
        permissions.add(new Permission(Permission.USER, u.getUsername(), u.getDisplayName()));
      }
    } catch (Exception ex) {
      LOG.warn(ex);
    }

    if (permissions.size() < UserUtil.SEARCH_LIMIT) {
      try {
        for (Group g : orgService.getGroupHandler().getAllGroups()) {
          if (g.getLabel().toLowerCase().contains(keyword)) {
            String perm = "*:" + g.getId();
            String displayName = new StringBuilder(bundle.getString("label.any")).append(" ")
                                        .append(bundle.getString("label.in")).append(" ")
                                        .append(g.getLabel())
                                        .toString();

            Permission p = new Permission(Permission.GROUP, perm, displayName);
            p.setGroupId(g.getId());
            p.setGroupName(g.getLabel());
            p.setMembershipType(MembershipTypeHandler.ANY_MEMBERSHIP_TYPE);

            permissions.add(p);
          }
          if (permissions.size() == UserUtil.SEARCH_LIMIT) {
            break;
          }
        }
      } catch (Exception ex) {
        LOG.warn(ex);
      }
    }

    return permissionSuggest.with()
            .keyword(keyword)
            .permissions(permissions)
            .ok()
            .withCharset(Tools.UTF_8);
  }

  @Resource
  @Ajax
  @MimeType.HTML
  public Response savePermission(Long id, String[] permissions, String type) throws EntityNotFoundException, ParameterEntityException, UnAuthorizedOperationException {
    String name = "manager".equals(type) ? type : "participator";
    Map<String, String[]> fields = new HashMap<String, String[]>();
    fields.put(name, permissions);
    //
    Project project = projectService.getProject(id);
    if (!project.canEdit(ConversationState.getCurrent().getIdentity())) {
      throw new UnAuthorizedOperationException(id, Project.class, getNoPermissionMsg());
    }
    project = ProjectUtil.saveProjectField(projectService, id, fields);    
    projectService.updateProject(project);

    return renderShareDialog(project, "");
  }

  @Resource
  @Ajax
  @MimeType.HTML
  public Response projectTree(String space_group_id, SecurityContext securityContext) {
    List<Project> projects = ProjectUtil.getProjectTree(space_group_id, projectService);
    UserSetting setting = userService.getUserSetting(securityContext.getRemoteUser());
    return listProjects
        .with()
        .userSetting(setting)
        .projects(projects)
        .currentProjectId(-100)
        .ok().withCharset(Tools.UTF_8);
  }
  
  @Resource
  @Ajax
  @MimeType.JSON
  public Response projectTreeAsJSON(String space_group_id) throws JSONException {
    List<Project> projects = ProjectUtil.getProjectTree(space_group_id, projectService);
    Project root = new Project();
    root.setName(bundle.getString("label.projects"));
    projects.add(0, root);
    //
    JSONArray array = new JSONArray();
    buildJSON(array, projects);
    return Response.ok(array.toString()).withCharset(Tools.UTF_8);
  }
  
  @Resource
  @Ajax
  @MimeType.HTML
  public Response getBreadCumbs(Long id, Boolean isBreadcrumb) throws UnAuthorizedOperationException {
    String breadcrumbs = "";
    Project p = null;
    try {      
      p = projectService.getProject(id);
      if (!p.canEdit(ConversationState.getCurrent().getIdentity())) {
        throw new UnAuthorizedOperationException(id, Project.class, getNoPermissionMsg());
      }
    } catch (EntityNotFoundException e) {
    }
    
    
    EntityEncoder encoder = HTMLEntityEncoder.getInstance();
    if (isBreadcrumb == null || isBreadcrumb) {
      breadcrumbs = ProjectUtil.buildBreadcumbs(id, projectService, bundle);
    } else {
      if (p != null) {
        breadcrumbs = new StringBuilder("<li class=\"active\"><a class=\"project-name\" href=\"javascript:void(0)\">")
        .append(encoder.encode(p.getName()))
        .append("</a></li>")
        .toString();
      } else {
        breadcrumbs = new StringBuilder("<li class=\"muted\">")
        .append(bundle.getString("label.noProject"))
        .append("</li>")
        .toString();
      }
    }
    return Response.ok(breadcrumbs.toString()).withCharset(Tools.UTF_8);
  }

  private JSONArray buildJSON(JSONArray array, List<Project> projects) throws JSONException {
    for(Project p : projects) {
        JSONObject json = new JSONObject();
        json.put("id", p.getId());
        json.put("text", p.getName());
        array.put(json);
        if (p.getChildren() != null) {
          buildJSON(array, p.getChildren());
        }
    }
    
    return array;
  }

  @Resource
  @Ajax
  @MimeType.HTML
  public Response projectDetail(Long id) throws EntityNotFoundException, UnAuthorizedOperationException {
    Project project = projectService.getProject(id); //Can throw ProjectNotFoundException
    if (!project.canEdit(ConversationState.getCurrent().getIdentity())) {
      throw new UnAuthorizedOperationException(id, Project.class, getNoPermissionMsg());
    }
    
    List<String> groups = new LinkedList<String>();
    Map<String, User> users = new HashMap<String, User>();
    if(project.getManager() != null && !project.getManager().isEmpty()) {
      for(String man : project.getManager()) {
        Permission per = Permission.parse(man, orgService);
        if (per.getType() == Permission.USER) {
          User user = userService.loadUser(man);
          users.put(man, user);
        } else {
          groups.add(per.getDisplayName());
        }
      }
    }

    Project parent = project.getParent();
    if (parent == null) {
      parent = new Project();
    }

    return detail
        .with()
        .breadcumbs(ProjectUtil.buildBreadcumbs(parent.getId(), projectService, bundle))
        .parent(parent)
        .project(project)
        .userMap(users)
        .groups(groups)
        .ok()
        .withCharset(Tools.UTF_8);
  }

  @Resource
  @Ajax
  @MimeType("text/plain")
  public Response saveProjectInfo(Long projectId, String parent, String name, String description, String calendarIntegrated) 
        throws EntityNotFoundException, ParameterEntityException, UnAuthorizedOperationException {
    if(name == null) {
      return Response.status(406).body("Field name is required");
    }
    Identity identity = ConversationState.getCurrent().getIdentity();
    if (parent != null && !parent.isEmpty()) {
      Long parentId = Long.parseLong(parent);
      try {
        if (!projectService.getProject(parentId).canEdit(identity)) {
          throw new UnAuthorizedOperationException(parentId, Project.class, getNoPermissionMsg());
        }
      } catch (EntityNotFoundException ex) {        
      }
    }
    if (!projectService.getProject(projectId).canEdit(identity)) {
      throw new UnAuthorizedOperationException(projectId, Project.class, getNoPermissionMsg());
    }

    Map<String, String[]> fields = new HashMap<String, String[]>();
    fields.put("name", new String[] {name});
    fields.put("description", new String[] {description});
    fields.put("parent", new String[] {parent});
    fields.put("calendarIntegrated", new String[] {calendarIntegrated});
    Project project = ProjectUtil.saveProjectField(projectService, projectId, fields);
    //Can throw ProjectNotFoundException & NotAllowedOperationOnEntityException
    projectService.updateProject(project); 
    return Response.ok("Update successfully");
  }
  
  @Resource
  @Ajax
  @MimeType("text/plain")
  public Response changeProjectColor(Long projectId, String color) 
      throws EntityNotFoundException, ParameterEntityException, UnAuthorizedOperationException {
    Map<String, String[]> fields = new HashMap<String, String[]>();
    fields.put("color", new String[] {color});
    //
    Project project = projectService.getProject(projectId);
    if (!project.canEdit(ConversationState.getCurrent().getIdentity())) {
      throw new UnAuthorizedOperationException(projectId, Project.class, getNoPermissionMsg());
    }
    project = ProjectUtil.saveProjectField(projectService, projectId, fields);      
    //Can throw ProjectNotFoundException & NotAllowedOperationOnEntityException
    projectService.updateProject(project); 
    return Response.ok("Update successfully");
  }

  @Resource
  @Ajax
  @MimeType("text/plain")
  public Response deleteProject(Long projectId, Boolean deleteChild) throws EntityNotFoundException, UnAuthorizedOperationException {
    Identity identity = ConversationState.getCurrent().getIdentity();
    Project project = projectService.getProject(projectId); //Can throw ProjectNotFoundException
    if (!project.canEdit(identity)) {
      throw new UnAuthorizedOperationException(projectId, Project.class, getNoPermissionMsg());
    } else if (deleteChild) {
      ListAccess<Project> childs = projectService.getSubProjects(projectId);
      for (Project child : ListUtil.load(childs, 0, -1)) {
        if (!child.canEdit(identity)) {
          throw new UnAuthorizedOperationException(child.getId(), Project.class, getNoPermissionMsg());
        }
      }
    }
    
    projectService.removeProject(projectId, deleteChild);
    return Response.ok("Delete project successfully");
  }

  @Resource
  @Ajax
  @MimeType.HTML
  public Response findProject(String keyword, Long currentProject) {
    Identity identity = ConversationState.getCurrent().getIdentity();
    ListAccess<Project> tmp = projectService.findProjects(UserUtil.getMemberships(identity), keyword, null);
    List<Project> projects = Arrays.asList(ListUtil.load(tmp, 0, -1));
    projects = ProjectUtil.buildRootProjects(projects);

    return projectSearchResult
            .with()
            .keyword(keyword)
            .projects(projects)
            .identity(identity)
            .currentProjectId(currentProject)
            .ok();
  }

  private Response renderShareDialog(Project project, String editingField) {
    List<Permission> managers = new ArrayList<Permission>();
    List<Permission> participants = new ArrayList<Permission>();

    if (project.getManager() != null && project.getManager().size() > 0) {
      for (String permission : project.getManager()) {
        Permission p = Permission.parse(permission, orgService);
        if (p != null) {
          managers.add(p);
        }
      }
    }

    if (project.getParticipator() != null && project.getParticipator().size() > 0) {
      for (String permission : project.getParticipator()) {
        Permission p = Permission.parse(permission, orgService);
        if (p != null) {
          participants.add(p);
        }
      }
    }

    List<String> msTypes = new ArrayList<String>();
    try {
      Collection<MembershipType> membershipTypes = orgService.getMembershipTypeHandler().findMembershipTypes();
      for(MembershipType type : membershipTypes) {
        msTypes.add(type.getName());
      }
    } catch (Exception ex) {//NOSONAR
      LOG.error(ex);
    }

    return shareDialog.with().pid(project.getId())
        .participants(participants)
        .managers(managers)
        .msTypes(msTypes)
        .editingField(editingField == null ? "" : editingField)
        .ok().withCharset(Tools.UTF_8);
  }
  
  @Resource
  @Ajax
  @MimeType.HTML
  public Response openWarningDialog(String msg) {
    return buildMSGDialog(msg, MSG_TYPE.WARNING);
  }
}

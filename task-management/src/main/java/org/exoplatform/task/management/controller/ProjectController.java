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

import juzu.MimeType;
import juzu.Path;
import juzu.Resource;
import juzu.Response;
import juzu.impl.common.Tools;
import juzu.request.SecurityContext;

import org.exoplatform.commons.juzu.ajax.Ajax;
import org.exoplatform.commons.utils.ListAccess;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.services.organization.Group;
import org.exoplatform.services.organization.MembershipHandler;
import org.exoplatform.services.organization.MembershipType;
import org.exoplatform.services.organization.OrganizationService;
import org.exoplatform.services.organization.Query;
import org.exoplatform.services.organization.UserHandler;
import org.exoplatform.services.security.ConversationState;
import org.exoplatform.services.security.Identity;
import org.exoplatform.task.domain.Project;
import org.exoplatform.task.domain.UserSetting;
import org.exoplatform.task.exception.AbstractEntityException;
import org.exoplatform.task.exception.ProjectNotFoundException;
import org.exoplatform.task.model.Permission;
import org.exoplatform.task.model.User;
import org.exoplatform.task.model.UserGroup;
import org.exoplatform.task.service.ProjectService;
import org.exoplatform.task.service.UserService;
import org.exoplatform.task.utils.ProjectUtil;
import org.exoplatform.task.utils.UserUtils;
import org.exoplatform.webui.organization.AccessGroupListAccess;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import javax.inject.Inject;

import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * @author <a href="mailto:tuyennt@exoplatform.com">Tuyen Nguyen The</a>.
 */
public class ProjectController {

  @Inject
  ResourceBundle bundle;
  
  @Inject
  ProjectService projectService;

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
  public Response projectForm(Long parentId, SecurityContext securityContext) {    
    Project parent;
    try {
      parent = projectService.getProjectById(parentId);
    } catch (ProjectNotFoundException e) {
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
  public Response createProject(String space_group_id, String name, String description, Long parentId, SecurityContext securityContext) {

    String currentUser = securityContext.getRemoteUser();
    if(currentUser == null) {
      return Response.status(401).body("You must login to create new project");
    }

    if(name == null || name.isEmpty()) {
      return Response.status(412).body("Name of project is required");
    }

    try {
      Project project;
      if (space_group_id  != null) {
        List<String> memberships = UserUtils.getSpaceMemberships(space_group_id);
        Set<String> managers = new HashSet<String>(Arrays.asList(currentUser, memberships.get(0)));
        Set<String> participators = new HashSet<String>(Arrays.asList(memberships.get(1)));
        project = projectService.createDefaultStatusProjectWithAttributes(parentId, name, description, managers, participators);
      } else {
        project = projectService.createDefaultStatusProjectWithManager(name, description, parentId, currentUser); //Can throw ProjectNotFoundException        
      }
      JSONObject result = new JSONObject();
      result.put("id", project.getId());//Can throw JSONException (same for all #json.put methods below)
      result.put("name", project.getName());
      result.put("color", "transparent");

      return Response.ok(result.toString()).withCharset(Tools.UTF_8);

    } catch (AbstractEntityException e) {
      return Response.status(e.getHttpStatusCode()).body(e.getMessage());
    } catch (JSONException ex) {
      return Response.status(500).body(ex.getMessage());
    }
  }

  @Resource
  @Ajax
  @MimeType.JSON
  public Response cloneProject(Long id, String cloneTask, SecurityContext securityContext) {

    try {

      Project project = projectService.cloneProjectById(id, Boolean.parseBoolean(cloneTask)); //Can throw ProjectNotFoundException

      JSONObject result = new JSONObject();
      result.put("id", project.getId());
      result.put("name", project.getName());
      result.put("color", project.getColor());

      return Response.ok(result.toString()).withCharset(Tools.UTF_8);

    } catch (AbstractEntityException e) {
      return Response.status(e.getHttpStatusCode()).body(e.getMessage());
    } catch (JSONException ex) {
      return Response.status(500).body(ex.getMessage());
    }
  }

  @Resource
  @Ajax
  @MimeType.HTML
  public Response openConfirmDelete(Long id) {
    try {
      Project project = projectService.getProjectById(id); //Can throw ProjectNotFoundException
      if (project != null) {
        String msg = bundle.getString("popup.msg.deleteProject");
        msg = msg.replace("{}", project.getName());

        return confirmDeleteProject.with().pid(project.getId()).msg(msg)
            .ok().withCharset(Tools.UTF_8);        
      } else {
        return Response.status(404);
      }
    } catch (AbstractEntityException e) {
      return Response.status(e.getHttpStatusCode()).body(e.getMessage());
    }
  }
  
  @Resource
  @Ajax
  @MimeType.HTML
  public Response openShareDialog(Long id) {

    try {
      Project project = projectService.getProjectById(id); //Can throw ProjectNotFoundException
      return renderShareDialog(project, "");

    } catch (AbstractEntityException e) {
      return Response.status(e.getHttpStatusCode()).body(e.getMessage());
    }
  }

  @Resource
  @Ajax
  @MimeType.HTML
  public Response removePermission(Long id, String permission, String type) {

    try {

      Project project = projectService.removePermissionFromProjectId(id, permission, type); //Can throw ProjectNotFoundException & NotAllowedOperationOnEntityException
      return renderShareDialog(project, type);

    } catch (AbstractEntityException e) {
      return Response.status(e.getHttpStatusCode()).body(e.getMessage());
    }
  }

  @Resource
  @Ajax
  @MimeType.HTML
  public Response openUserSelector(Long id, String type, String groupId, String keyword, String filter) {

    try {

      Project project = projectService.getProjectById(id); //Can throw ProjectNotFoundException

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
        tmp = new ArrayList<org.exoplatform.services.organization.User>();
        for (org.exoplatform.services.organization.User u : users.load(0, users.getSize())) {
          tmp.add(u);
        }
      }

      if (groupId != null && !groupId.isEmpty()) {
        if (tmp == null) {
          ListAccess<org.exoplatform.services.organization.User> users = uHandler.findUsersByGroupId(groupId);
          tmp = new ArrayList<org.exoplatform.services.organization.User>();
          for (org.exoplatform.services.organization.User u : users.load(0, users.getSize())) {
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
        }
      }
      if (tmp == null) {
        ListAccess<org.exoplatform.services.organization.User> users = uHandler.findAllUsers();
        tmp = new ArrayList<org.exoplatform.services.organization.User>();
        for (org.exoplatform.services.organization.User u : users.load(0, users.getSize())) {
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
      allUsers.removeAll("manager".equals(type) ? project.getManager() : project.getParticipator());

      return userSelectorDialog.with()
          .type(type)
          .users(allUsers)
          .groupId(groupId == null ? "" : groupId)
          .keyword(keyword == null ? "" : keyword)
          .filter(filter == null ? "" : filter)
          .ok();

    } catch (AbstractEntityException e) {
      return Response.status(e.getHttpStatusCode()).body(e.getMessage());
    } catch (Exception ex) {// NOSONAR
      return Response.status(500).body(ex.getMessage());
    }

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
      allGroups = UserUtils.buildGroupTree(groups);
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
      String userKeyword = "*" + keyword + "*";
      Query query = new Query();
      query.setUserName(userKeyword);
      //query.setFirstName(userKeyword);
      //query.setLastName(userKeyword);
      ListAccess<org.exoplatform.services.organization.User> users = orgService.getUserHandler().findUsersByQuery(query);
      for(org.exoplatform.services.organization.User u : users.load(0, users.getSize())) {
        permissions.add(Permission.parse(u.getUserName(), orgService));
      }
    } catch (Exception ex) {
      LOG.warn(ex);
    }

    try {
      for(Group g : orgService.getGroupHandler().getAllGroups()) {
        if (g.getLabel().contains(keyword)) {
          String perm = "*:" + g.getId();
          permissions.add(Permission.parse(perm, orgService));
        }
      }
    } catch (Exception ex) {
      LOG.warn(ex);
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
  public Response savePermission(Long id, String[] permissions, String type) {
    try {

      String name = "manager".equals(type) ? type : "participator";
      Project project = projectService.updateProjectInfo(id, name, permissions); //Can throw ProjectNotFoundException & NotAllowedOperationOnEntityException
      return renderShareDialog(project, "");

    } catch (AbstractEntityException e) {
      return Response.status(e.getHttpStatusCode()).body(e.getMessage());
    }

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
  public Response getBreadCumbs(Long id, Boolean isBreadcrumb) {
    String breadcrumbs = "";
    if (isBreadcrumb == null || isBreadcrumb) {
      breadcrumbs = ProjectUtil.buildBreadcumbs(id, projectService, bundle);
    } else {
      try {
        Project p = projectService.getProjectById(id);
        breadcrumbs = new StringBuilder("<li class=\"active\"><a class=\"project-name\" href=\"javascript:void(0)\">")
                          .append(p.getName())
                          .append("</a></li>")
                          .toString();
      } catch (ProjectNotFoundException ex) {
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
  public Response projectDetail(Long id) {
    try {
      Project project = projectService.getProjectById(id); //Can throw ProjectNotFoundException

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

    } catch (AbstractEntityException e) {
      return Response.status(e.getHttpStatusCode()).body(e.getMessage());
    }
  }

  @Resource
  @Ajax
  @MimeType("text/plain")
  public Response saveProjectInfo(Long projectId, String name, String[] value) {

    if(name == null) {
      return Response.status(406).body("Field name is required");
    }

    try {

      projectService.updateProjectInfo(projectId, name, value); //Can throw ProjectNotFoundException & NotAllowedOperationOnEntityException
      return Response.ok("Update successfully");

    } catch (AbstractEntityException e) {
      return Response.status(e.getHttpStatusCode()).body(e.getMessage());
    }
  }


  @Resource
  @Ajax
  @MimeType("text/plain")
  public Response deleteProject(Long projectId, Boolean deleteChild) {
    try {
      projectService.deleteProjectById(projectId, deleteChild); //Can throw ProjectNotFoundException
      return Response.ok("Delete project successfully");
    } catch (AbstractEntityException e) {
      return Response.status(e.getHttpStatusCode()).body(e.getMessage());
    }
  }

  @Resource
  @Ajax
  @MimeType.HTML
  public Response findProject(String keyword, Long currentProject) {
    Identity identity = ConversationState.getCurrent().getIdentity();
    List<Project> projects = projectService.findProjectByKeyWord(identity, keyword);
    projects = ProjectUtil.buildRootProjects(projects);

    return projectSearchResult
            .with()
            .keyword(keyword)
            .projects(projects)
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
}

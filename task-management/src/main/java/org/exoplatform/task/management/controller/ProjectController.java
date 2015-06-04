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
import org.exoplatform.services.organization.MembershipType;
import org.exoplatform.services.organization.OrganizationService;
import org.exoplatform.services.security.ConversationState;
import org.exoplatform.services.security.Identity;
import org.exoplatform.task.domain.Project;
import org.exoplatform.task.model.User;
import org.exoplatform.task.service.ProjectService;
import org.exoplatform.task.service.UserService;
import org.json.JSONException;
import org.json.JSONObject;

import javax.inject.Inject;
import java.util.*;

/**
 * @author <a href="mailto:tuyennt@exoplatform.com">Tuyen Nguyen The</a>.
 */
public class ProjectController {

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
  @Path("userSelectorDialog.gtmpl")
  org.exoplatform.task.management.templates.userSelectorDialog userSelectorDialog;

  @Inject
  @Path("groupSelectorDialog.gtmpl")
  org.exoplatform.task.management.templates.groupSelectorDialog groupSelectorDialog;

  private static final Log LOG = ExoLogger.getExoLogger(ProjectController.class);

  @Resource
  @Ajax
  @MimeType.HTML
  public Response projectForm(Long parentId) {
    return form
        .with()
        .parentId(parentId)
        .ok();
  }

  @Resource
  @Ajax
  @MimeType.JSON
  public Response createProject(String name, String description, Long parentId, SecurityContext securityContext) {

    String currentUser = securityContext.getRemoteUser();
    if(currentUser == null) {
      return Response.status(401).body("You must login to create new project");
    }

    if(name == null || name.isEmpty()) {
      return Response.status(412).body("Name of project is required");
    }

    Project project = projectService.createDefaultStatusProjectWithManager(name, description, parentId, currentUser);

    if (project == null) {
      return Response.error("Impossible to create project");
    }

    JSONObject result = new JSONObject();
    try {
      result.put("id", project.getId());
      result.put("name", project.getName());
      result.put("color", "transparent");
    } catch (JSONException ex) {
      return Response.status(500).body(ex.getMessage());
    }
    return Response.ok(result.toString()).withCharset(Tools.UTF_8);
  }

  @Resource
  @Ajax
  @MimeType.JSON
  public Response cloneProject(Long id, String cloneTask, SecurityContext securityContext) {

    Project project = projectService.cloneProjectById(id, Boolean.parseBoolean(cloneTask));

    JSONObject result = new JSONObject();

    if (project == null) {
      return Response.error("Impossible to create project with id: "+id);
    }

    try {
      result.put("id", project.getId());
      result.put("name", project.getName());
      result.put("color", project.getColor());
    } catch (JSONException ex) {
      return Response.status(500).body(ex.getMessage());
    }
    return Response.ok(result.toString()).withCharset(Tools.UTF_8);
  }

  @Resource
  @Ajax
  @MimeType.HTML
  public Response openShareDialog(Long id) {
    Project project = projectService.getProjectById(id);

    if (project != null) {
      return renderShareDialog(project);
    } else {
      return Response.status(404).body("no projectId " + id);
    }
  }

  @Resource
  @Ajax
  @MimeType.HTML
  public Response removePermission(Long id, String permission, String type) {

    Project project = projectService.removePermissionFromProjectId(id, permission, type);

    if (project != null) {
      return renderShareDialog(project);
    } else {
      return Response.error("Impossible to remove permission from project with id: " + id);
    }
  }

  @Resource
  @Ajax
  @MimeType.HTML
  public Response openUserSelector(Long id, String type) {
    Project project = projectService.getProjectById(id);

    if (project != null) {
      org.exoplatform.services.organization.User[] users = null;
      try {
        ListAccess<org.exoplatform.services.organization.User> tmp = orgService.getUserHandler().findAllUsers();
        users = tmp.load(0, tmp.getSize());
      } catch (Exception e) {
        return Response.status(503).body(e.getMessage());
      }

      Set<String> allUsers = new HashSet<String>();
      if (users != null) {
        for (org.exoplatform.services.organization.User u : users) {
          allUsers.add(u.getUserName());
        }
      }
      allUsers.removeAll("manager".equals(type) ? project.getManager() : project.getParticipator());

      return userSelectorDialog.with().type(type)
          .users(allUsers).ok();
    } else {
      return Response.status(404).body("no projectId " + id);
    }
  }

  @Resource
  @Ajax
  @MimeType.HTML
  public Response openGroupSelector(Long id, String type) {
    Project project = projectService.getProjectById(id);

    if (project != null) {
      Collection groups, msTypes;
      try {
        groups = orgService.getGroupHandler().getAllGroups();
        msTypes = orgService.getMembershipTypeHandler().findMembershipTypes();
      } catch (Exception e) {
        return Response.status(503).body(e.getMessage());
      }

      Set<String> allGroups = new HashSet<String>();
      if (groups != null) {
        for (Object g : groups) {
          allGroups.add(((Group)g).getId());
        }
      }

      Set<String> allMSTypes = new HashSet<String>();
      if (msTypes != null) {
        for (Object mst : msTypes) {
          allMSTypes.add(((MembershipType)mst).getName());
        }
      }

      return groupSelectorDialog.with().type(type)
          .groups(allGroups).membershipTypes(allMSTypes).ok();
    } else {
      return Response.status(404).body("no projectId " + id);
    }
  }

  @Resource
  @Ajax
  @MimeType.HTML
  public Response addPermission(Long id, String permissions, String type) {

    Project project = projectService.addPermissionsFromProjectId(id,permissions, type);

    if (project != null) {
      return renderShareDialog(project);
    } else {
      return Response.status(404).body("no projectId {}, or permission is null " + id);
    }
  }

  @Resource
  @Ajax
  @MimeType.HTML
  public Response projectTree() {
    List<Project> projects = getProjectTree();

    return listProjects
        .with()
        .projects(projects)
        .ok();
  }

  public List<Project> getProjectTree() {

    ConversationState state = ConversationState.getCurrent();
    Identity identity = state.getIdentity();

    return projectService.getProjectTreeByIdentity(identity);
  }

  @Resource
  @Ajax
  @MimeType.HTML
  public Response projectDetail(Long id) {
    Project project = projectService.getProjectById(id);
    if(project == null) {
      return Response.notFound("Project does not exist with ID: " + id);
    }

    Map<String, User> users = new HashMap<String, User>();
    if(project.getManager() != null && !project.getManager().isEmpty()) {
      for(String username : project.getManager()) {
        User user = userService.loadUser(username);
        users.put(username, user);
      }
    }

    return detail
        .with()
        .project(project)
        .userMap(users)
        .ok()
        .withCharset(Tools.UTF_8);
  }

  @Resource
  @Ajax
  @MimeType("text/plain")
  public Response saveProjectInfo(Long projectId, String name, String[] value) {
    if(name == null) {
      return Response.status(406).body("Field name is required");
    }

    if (projectService.updateProjectInfo(projectId, name, value) == null) return Response.error("Impossible to update info for project with ID:  " + projectId);

    return Response.ok("Update successfully");
  }


  @Resource
  @Ajax
  @MimeType("text/plain")
  public Response deleteProject(Long projectId) {

    if (!projectService.deleteProjectById(projectId)) {
      return Response.error("Impossible to delete project with ID: " + projectId);
    }

    return Response.ok("Delete project successfully");
  }

  private Response renderShareDialog(Project project) {
    return shareDialog.with().pid(project.getId())
        .participants(project.getParticipator())
        .managers(project.getManager())
        .ok().withCharset(Tools.UTF_8);
  }
}

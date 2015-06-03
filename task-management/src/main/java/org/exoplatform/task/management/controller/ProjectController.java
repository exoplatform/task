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

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.inject.Inject;

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
import org.exoplatform.services.security.MembershipEntry;
import org.exoplatform.task.dao.ProjectHandler;
import org.exoplatform.task.dao.StatusHandler;
import org.exoplatform.task.domain.Project;
import org.exoplatform.task.domain.Status;
import org.exoplatform.task.model.User;
import org.exoplatform.task.service.TaskService;
import org.exoplatform.task.service.UserService;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * @author <a href="mailto:tuyennt@exoplatform.com">Tuyen Nguyen The</a>.
 */
public class ProjectController {

  @Inject
  TaskService taskService;

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
    if(name == null || name.isEmpty()) {
      return Response.status(412).body("Name of project is required");
    }

    String currentUser = securityContext.getRemoteUser();
    if(currentUser == null) {
      return Response.status(401).body("You must login to create new project");
    }

    Set<String> managers = new HashSet<String>();
    managers.add(currentUser);

    Project project = createProject(parentId, name, description, Collections.<Status>emptySet(), managers, 
                                    Collections.<String>emptySet());
    if (project == null) {
      return Response.content(406, "Can not find project id for parentID = " + parentId);
    }
    createDefaultStatus(project);
    
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
  
  private Project createProject(Long parentId, String name, String description, Set<Status> status, 
                                Set<String> managers, Set<String> participators) {
    Project project = new Project(name, description, status, managers, participators);

    //
    if (parentId != null && parentId > 0) {
      Project parent = taskService.getProjectHandler().find(parentId);
      if (parent == null) {
        return null;
      }
      // TODO: copy all permission from parent project to current project
      project.setParent(parent);
    }

    project = taskService.getProjectHandler().create(project);    
    return project;
  }
  
  private void createDefaultStatus(Project project) {
    StatusHandler statusHandler = taskService.getStatusHandler();

    //. Create status for project
    Status status = new Status();
    status.setName("Todo");
    status.setRank(1);
    status.setProject(project);
    statusHandler.create(status);

    status = new Status();
    status.setName("In Progress");
    status.setRank(2);
    status.setProject(project);
    statusHandler.create(status);

    status = new Status();
    status.setName("Done");
    status.setRank(3);
    status.setProject(project);
    statusHandler.create(status);
  }
   
  @Resource
  @Ajax
  @MimeType.JSON
  public Response cloneProject(Long id, String cloneTask, SecurityContext securityContext) {
    Project project = taskService.getProjectHandler().cloneProject(id, Boolean.parseBoolean(cloneTask)); 

    JSONObject result = new JSONObject();
    if (project != null) {
      try {
        result.put("id", project.getId());
        result.put("name", project.getName());
        result.put("color", project.getColor());
      } catch (JSONException ex) {
        return Response.status(500).body(ex.getMessage());
      }
      return Response.ok(result.toString()).withCharset(Tools.UTF_8);      
    } else {
      return Response.status(404).body("Can not clone, no projectId " + id); 
    }
  }
  
  @Resource
  @Ajax
  @MimeType.HTML
  public Response openShareDialog(Long id) {
    Project project = taskService.getProjectHandler().find(id); 
    
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
    Project project = taskService.getProjectHandler().find(id); 
    
    if (project != null) {
      if ("manager".equals(type)) {
        if (project.getManager().size() > 1) {
          project.getManager().remove(permission);
        } else {
          return Response.status(401).body("Not allow to remove last manager");
        }
      } else {
        project.getParticipator().remove(permission);
      }
      taskService.getProjectHandler().update(project);
      //
      return renderShareDialog(project);
    } else {
      return Response.status(404).body("no projectId " + id); 
    }
  }
  
  @Resource
  @Ajax
  @MimeType.HTML
  public Response openUserSelector(Long id, String type) {
    Project project = taskService.getProjectHandler().find(id); 
    
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
    Project project = taskService.getProjectHandler().find(id); 
    
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
    Project project = taskService.getProjectHandler().find(id); 
    
    if (project != null && permissions != null) {
      Set<String> per = new HashSet<String>();
      MembershipEntry entry = MembershipEntry.parse(permissions);
      if (entry != null) {
        per.add(entry.toString());
      } else {
        String[] users = permissions.split(",");
        for (int i = 0; i < users.length; i++) {
          per.add(users[i]);
        }
      }
      
      if ("manager".equals(type)) {
        project.getManager().addAll(per);
      } else {
        project.getParticipator().addAll(per);
      }
      taskService.getProjectHandler().update(project);
      //
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
    
    List<String> memberships = getMemberships(identity);
    List<Project> projects = taskService.getProjectHandler().findAllByMemberships(memberships);
    //
    return buildRootProjects(projects);
  }

  private List<String> getMemberships(Identity identity) {
    Map<String, List<MembershipEntry>> gms = new HashMap<String, List<MembershipEntry>>();
    for (MembershipEntry m : identity.getMemberships()) {
      List<MembershipEntry> ms = gms.get(m.getGroup());
      if (ms == null) {
        ms = new LinkedList<MembershipEntry>();
        gms.put(m.getGroup(), ms);
        //
        ms.add(new MembershipEntry(m.getGroup(), MembershipEntry.ANY_TYPE));
      }
      if (!m.getMembershipType().equals(MembershipEntry.ANY_TYPE)) {
        ms.add(m);
      }
    }
    
    List<String> memberships = new ArrayList<String>();
    String userName = identity.getUserId();
    memberships.add(userName);
    for (List<MembershipEntry> ms : gms.values()) {
      for (MembershipEntry m : ms) {
        memberships.add(m.toString());
      }
    }
    return memberships;
  }

  private List<Project> buildRootProjects(List<Project> projects) {
    if (projects == null) return projects;
    
    Set<Project> rootPRJs = new LinkedHashSet<Project>();
    Set<Project> childs = new LinkedHashSet<Project>();
    for (Project p : projects) {
      while(true) {
        Project parent = p.getParent();
        if (parent == null) {
          rootPRJs.add(p);
          break;
        } else {
          childs.add(p);
          p = parent;
        }
      }
    }
    
    List<Project> parents = new LinkedList<Project>(rootPRJs);
    do {
      List<Project> tmpParents = new LinkedList<Project>();
      for (Project p : parents) {
        List<Project> tmp = new LinkedList<Project>();
        for (Project c : childs) {
          if (c.getParent().equals(p)) {
            tmp.add(c);
          }
        }
        p.setChildren(tmp);
        tmpParents.addAll(tmp);
        childs.removeAll(tmp);
      }
      parents = tmpParents;
    } while (!parents.isEmpty() && !childs.isEmpty());
    
    return new LinkedList<Project>(rootPRJs);
  }

  @Resource
  @Ajax
  @MimeType.HTML
  public Response projectDetail(Long id) {
    Project project = taskService.getProjectHandler().find(id);
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
    String val = value != null && value.length > 0 ? value[0] : null;

    ProjectHandler handler = taskService.getProjectHandler();
    Project project = handler.find(projectId);
    if(project == null) {
      return Response.notFound("Project does not exist with ID: " + projectId);
    }

    if("name".equalsIgnoreCase(name)) {
      if(val == null || val.isEmpty()) {
        return Response.status(406).body("Name of project must not empty");
      }
      project.setName(val);
    } else if("manager".equalsIgnoreCase(name)) {
      Set<String> manager = new HashSet<String>();
      if(value != null) {
        for (String v : value) {
          manager.add(v);
        }
      }
      project.setManager(manager);
    } else if("participator".equalsIgnoreCase(name)) {
      Set<String> participator = new HashSet<String>();
      if(value != null || true) {
        for (String v : value) {
          participator.add(v);
        }
      }
      project.setParticipator(participator);
    } else if("dueDate".equalsIgnoreCase(name)) {
      if(val == null || val.isEmpty()) {
        project.setDueDate(null);
      } else {
        DateFormat df = new SimpleDateFormat("yyyy-MM-dd");
        try {
          Date date = df.parse(val);
          project.setDueDate(date);
        } catch (ParseException e) {
          return Response.status(500).body("can not parse date string: " + val);
        }
      }
    } else if("description".equalsIgnoreCase(name)) {
      project.setDescription(val);
    } else {
      return Response.status(406).body("Project does not contain field: " + name);
    }

    handler.update(project);

    return Response.ok("Update successfully");
  }


  @Resource
  @Ajax
  @MimeType("text/plain")
  public Response deleteProject(Long projectId) {
    ProjectHandler handler = taskService.getProjectHandler();
    Project project = handler.find(projectId);

    if (project == null) {
      return Response.notFound("Project does not exist with ID: " + projectId);
    }

    // Update all child project
    if(project.getChildren() != null) {
      for(Project child : project.getChildren()) {
        child.setParent(null);
        handler.update(child);
      }
    }

    handler.delete(project);

    return Response.ok("Delete project successfully");
  }
  
  private Response renderShareDialog(Project project) {
    return shareDialog.with().pid(project.getId())
                                    .participants(project.getParticipator())
                                    .managers(project.getManager())
                                    .ok().withCharset(Tools.UTF_8);
  }
}

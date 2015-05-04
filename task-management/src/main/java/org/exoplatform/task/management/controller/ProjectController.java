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
import org.exoplatform.task.dao.ProjectHandler;
import org.exoplatform.task.dao.StatusHandler;
import org.exoplatform.task.domain.Project;
import org.exoplatform.task.domain.Status;
import org.exoplatform.task.model.User;
import org.exoplatform.task.service.TaskService;
import org.exoplatform.task.service.UserService;
import org.json.JSONException;
import org.json.JSONObject;

import javax.inject.Inject;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * @author <a href="mailto:tuyennt@exoplatform.com">Tuyen Nguyen The</a>.
 */
public class ProjectController {

  @Inject
  TaskService taskService;

  @Inject
  UserService userService;

  @Inject
  @Path("projectForm.gtmpl")
  org.exoplatform.task.management.templates.projectForm form;

  @Inject
  @Path("projectDetail.gtmpl")
  org.exoplatform.task.management.templates.projectDetail detail;

  @Inject
  @Path("listProjects.gtmpl")
  org.exoplatform.task.management.templates.listProjects listProjects;

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

    Set<Status> statuses = new HashSet<Status>();

    Project project = new Project(name, description, statuses, managers, Collections.<String>emptySet());

    //
    if (parentId != null && parentId > 0) {
      Project parent = taskService.getProjectHandler().find(parentId);
      if (parent == null) {
        return Response.content(406, "Can not find project id for parentID = " + parentId);
      }
      // TODO: copy all permission from parent project to current project
      project.setParent(parent);
    }

    project = taskService.getProjectHandler().create(project);

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
  @MimeType.HTML
  public Response projectTree() {

    List<Project> projects = taskService.getProjectHandler().findSubProjects(null);

    return listProjects
            .with()
            .projects(projects)
            .ok();
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
}

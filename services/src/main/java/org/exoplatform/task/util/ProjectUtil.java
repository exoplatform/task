/* 
* Copyright (C) 2003-2015 eXo Platform SAS.
*
* This program is free software: you can redistribute it and/or modify
* it under the terms of the GNU Lesser General Public License as published by
* the Free Software Foundation, either version 3 of the License, or
* (at your option) any later version.
*
* This program is distributed in the hope that it will be useful,
* but WITHOUT ANY WARRANTY; without even the implied warranty of
* MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
* GNU Lesser General Public License for more details.
*
* You should have received a copy of the GNU Lesser General Public License
* along with this program. If not, see http://www.gnu.org/licenses/ .
*/
package org.exoplatform.task.util;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.Set;

import org.exoplatform.calendar.model.Calendar;
import org.exoplatform.commons.utils.ListAccess;
import org.exoplatform.container.ExoContainer;
import org.exoplatform.container.ExoContainerContext;
import org.exoplatform.portal.mop.SiteKey;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.services.security.ConversationState;
import org.exoplatform.services.security.Identity;
import org.exoplatform.task.dao.TaskQuery;
import org.exoplatform.task.domain.Project;
import org.exoplatform.task.domain.Status;
import org.exoplatform.task.domain.Task;
import org.exoplatform.task.exception.EntityNotFoundException;
import org.exoplatform.task.exception.ParameterEntityException;
import org.exoplatform.task.service.ProjectService;
import org.exoplatform.task.service.TaskService;
import org.exoplatform.web.controller.router.Router;

/**
 * Created by The eXo Platform SAS
 * Author : Thibault Clement
 * tclement@exoplatform.com
 * 6/3/15
 */
public final class ProjectUtil {
  private static final Log LOG = ExoLogger.getExoLogger(ProjectUtil.class);

  public static final String URL_PROJECT_DETAIL = "/projectDetail/";
  public static final int INCOMING_PROJECT_ID = -1;
  public static final int TODO_PROJECT_ID = -2;
  public static final int LABEL_PROJECT_ID = -5;

  public static final String NAME = "name";

  public static final String DUE_DATE = "dueDate";
  
  private static final String NAME_SUFFIX = " Tasks";

  private ProjectUtil() {
  }
  
  public static Calendar buildCalendar(Calendar calendar, Project project) {
    if (project == null || calendar == null) {
      return null;
    }
    if (project.getColor() != null) {
      calendar.setCalendarColor(project.getColor());      
    }
    calendar.setDescription(project.getDescription());
    calendar.setEditPermission(null);
    //ProjectService service = ExoContainerContext.getCurrentContainer().getComponentInstanceOfType(ProjectService.class);
    TaskService taskService = ExoContainerContext.getCurrentContainer().getComponentInstanceOfType(TaskService.class);
    TaskQuery taskQuery = new TaskQuery();
    taskQuery.setProjectIds(Arrays.asList(project.getId()));
    //List<Task> tasks = service.getTasksByProjectId(Arrays.asList(project.getId()), null);
    ListAccess<Task> listTask = taskService.findTasks(taskQuery);
    calendar.setHasChildren(ListUtil.getSize(listTask) > 0);
    calendar.setId(String.valueOf(project.getId()));
    calendar.setName(project.getName() + NAME_SUFFIX);
    Set<String> permissions = new HashSet<String>();
    if (project.getManager() != null) {
      permissions.addAll(project.getManager());      
    }
    if (project.getParticipator() != null) {
      permissions.addAll(project.getParticipator());      
    }
    calendar.setViewPermission(permissions.toArray(new String[permissions.size()]));    

    return calendar;
  }
  
  public static List<Project> getProjectTree(String space_group_id, ProjectService projectService) {
    List<String> memberships = new LinkedList<String>();
    if (space_group_id == null) {
      ConversationState state = ConversationState.getCurrent();
      Identity identity = state.getIdentity();
      memberships.addAll(UserUtil.getMemberships(identity));      
    } else {
      memberships.addAll(UserUtil.getSpaceMemberships(space_group_id));
    }

    List<Project> projects = projectService.findProjects(memberships, null, null);
    return ProjectUtil.buildRootProjects(projects);

    //return projectService.getProjectTreeByMembership(memberships);
  }

  public static List<Project> buildRootProjects(List<Project> projects) {
    if (projects == null) return projects;

    Map<Long, Project> maps = new HashMap<Long, Project>();
    Set<Project> rootPRJs = new LinkedHashSet<Project>();
    //Set<Project> childs = new LinkedHashSet<Project>();
    for (Project p : projects) {
      while(true) {
        if (!maps.containsKey(p.getId())) {
          maps.put(p.getId(), p);
        }
        Project parent = p.getParent();
        if (parent == null) {
          rootPRJs.add(p);
          break;
        } else {
          if (maps.containsKey(parent.getId())) {
            parent = maps.get(parent.getId());
          }
          if (!parent.getChildren().contains(p)) {
            parent.getChildren().add(p);
          }
          p = parent;
        }
      }
    }

    /*List<Project> parents = new LinkedList<Project>(rootPRJs);
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
    } while (!parents.isEmpty() && !childs.isEmpty());*/

    return new LinkedList<Project>(rootPRJs);
  }
  
  public static List<Project> flattenTree(List<Project> projectTree, ProjectService projectService) {
    if (projectTree == null) {
      return null;
    }

    List<Project> projects = new LinkedList<Project>();
    for (Project p : projectTree) {
      projects.add(p);
      List<Project> children = projectService.getSubProjects(p.getId());
      if (children != null && !children.isEmpty()) {
        projects.addAll(flattenTree(children, projectService));
      }
    }    
    return projects;
  }


  //TODO: should move this method to web module
  public static String buildBreadcumbs(Long id, ProjectService projectService, ResourceBundle bundle) {
    Project project = null;
    if (id > 0) {
      try {
        project = projectService.getProject(id);
      } catch (EntityNotFoundException e) {
        LOG.warn("project {} not found", id);
      }
    }

    StringBuilder builder = new StringBuilder();
    if (project != null) {
      Project tmp = project;
      while (tmp != null) {
        StringBuilder el = new StringBuilder();
        if (builder.length() == 0) {
          el.append("<li class=\"active\">").append(tmp.getName()).append("</li>");
        } else {
          el.append("<li>")
                  .append("<a class=\"Selected\" title=\"\" data-placement=\"bottom\" rel=\"tooltip\" href=\"#\" data-original-title=\"")
                  .append(tmp.getName())
                  .append("\">")
                  .append(tmp.getName())
                  .append("</a>")
                  .append("<span class=\"uiIconMiniArrowRight\"></span>")
                  .append("</li>");
        }
        builder.insert(0, el.toString());
        tmp = tmp.getParent();
      }
    }

    String label = bundle.getString("label.projects");
    StringBuilder el = new StringBuilder();
    if (builder.length() == 0) {
      el.append("<li class=\"active\">").append(label).append("</li>");
    } else {
      el.append("<li>")
              .append("<a class=\"Selected\" title=\"\" data-placement=\"bottom\" rel=\"tooltip\" href=\"#\" data-original-title=\"")
              .append(label)
              .append("\">")
              .append(label)
              .append("</a>")
              .append("<span class=\"uiIconMiniArrowRight\"></span>")
              .append("</li>");
    }
    builder.insert(0, el.toString());

    return builder.toString();
  }

  public static String buildProjectURL(Project project, SiteKey siteKey, ExoContainer container, Router router) {
    if (project == null) {
      return "#";
    }

    StringBuilder urlBuilder = new StringBuilder(ResourceUtil.buildBaseURL(siteKey, container, router));
    if (urlBuilder.length() <= 1) {
      return urlBuilder.toString();
    } else {
      return urlBuilder.append(URL_PROJECT_DETAIL)
          .append(project.getId())
          .toString();
    }
  }

  public static long getProjectIdFromURI(String requestPath) {
    long taskId = -1;
    int index = requestPath.indexOf(URL_PROJECT_DETAIL);
    if (index >= 0) {
      index = index + URL_PROJECT_DETAIL.length();
      int lastIndex = requestPath.indexOf('/', index);
      String id;
      if (lastIndex > 0) {
        id = requestPath.substring(index, lastIndex);
      } else {
        id = requestPath.substring(index);
      }
      try {
        taskId = Long.parseLong(id);
      } catch (NumberFormatException ex) {
        taskId = -1;
      }
    }
    return taskId;
  }

  public static Project newProjectInstance(String name, String description, String username) {
    Set<String> managers = new HashSet<String>();
    managers.add(username);
    return newProjectInstance(name, description, managers, Collections.<String>emptySet());
  }

  public static Project newProjectInstance(String name, String description, Set<String> managers, Set<String> participators) {
    Project p = new Project(name, description, new HashSet<Status>(), managers, participators);
    return p;
  }

  public static Project saveProjectField(ProjectService projService, long projectId, Map<String, String[]> fields)
      throws EntityNotFoundException, ParameterEntityException {

    Project project = projService.getProject(projectId);
    //
    for (Map.Entry<String, String[]> field : fields.entrySet()) {
      String fieldName = field.getKey();
      String[] values = field.getValue();
      
      String val = values != null && values.length > 0 ? values[0] : null;
      
      if("name".equalsIgnoreCase(fieldName)) {
        if(val == null || val.isEmpty()) {
          LOG.info("Name of project must not empty");
          throw new ParameterEntityException(projectId, Project.class, fieldName, val, "must not be empty", null);
        }
        project.setName(val);
      } else if("manager".equalsIgnoreCase(fieldName)) {
        Set<String> manager = new HashSet<String>();
        if(values != null) {
          for (String v : values) {
            manager.add(v);
          }
        }
        project.setManager(manager);
      } else if("participator".equalsIgnoreCase(fieldName)) {
        Set<String> participator = new HashSet<String>();
        if(values != null || true) {
          for (String v : values) {
            participator.add(v);
          }
        }
        project.setParticipator(participator);
      } else if("dueDate".equalsIgnoreCase(fieldName)) {
        if(val == null || val.isEmpty()) {
          project.setDueDate(null);
        } else {
          DateFormat df = new SimpleDateFormat("yyyy-MM-dd");
          try {
            Date date = df.parse(val);
            project.setDueDate(date);
          } catch (ParseException e) {
            LOG.info("can not parse date string: " + val);
            throw new ParameterEntityException(projectId, Project.class, fieldName, val, "cannot be parse to date", e);
          }
        }
      } else if("description".equalsIgnoreCase(fieldName)) {
        project.setDescription(val);
      } else if ("color".equalsIgnoreCase(fieldName)) {
        project.setColor(val);
      } else if ("calendarIntegrated".equalsIgnoreCase(fieldName)) {
        project.setCalendarIntegrated(Boolean.parseBoolean(val));
      } else if ("parent".equalsIgnoreCase(fieldName)) {
        try {
          long pId = Long.parseLong(val);
          if (pId == 0) {
            project.setParent(null);
          } else if (pId == project.getId()) {
            throw new ParameterEntityException(pId, Project.class, fieldName, val, "project can not be child of itself", null);
          } else {
            Project parent = projService.getProject(pId);
            project.setParent(parent);
          }
        } catch (NumberFormatException ex) {
          LOG.info("can not parse date string: " + val);
          throw new ParameterEntityException(projectId, Project.class, fieldName, val, "cannot be parse to Long", ex);
        }
      } else {
        LOG.info("Field name: " + fieldName + " is not supported for entity Project");
        throw new ParameterEntityException(projectId, Project.class, fieldName, val, "is not supported for the entity Project", null);
      }      
    }

    return project;
  }
}


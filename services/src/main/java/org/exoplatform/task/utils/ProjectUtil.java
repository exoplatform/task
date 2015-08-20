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
package org.exoplatform.task.utils;

import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.services.security.ConversationState;
import org.exoplatform.services.security.Identity;
import org.exoplatform.task.domain.Project;
import org.exoplatform.task.exception.ProjectNotFoundException;
import org.exoplatform.task.service.ProjectService;

import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.ResourceBundle;
import java.util.Set;

/**
 * Created by The eXo Platform SAS
 * Author : Thibault Clement
 * tclement@exoplatform.com
 * 6/3/15
 */
public final class ProjectUtil {
  private static final Log LOG = ExoLogger.getExoLogger(ProjectUtil.class);

  public static final int INCOMING_PROJECT_ID = -1;
  public static final int TODO_PROJECT_ID = -2;

  private ProjectUtil() {
  }
  
  public static List<Project> getProjectTree(String space_group_id, ProjectService projectService) {
    List<String> memberships = new LinkedList<String>();
    if (space_group_id == null) {
      ConversationState state = ConversationState.getCurrent();
      Identity identity = state.getIdentity();
      memberships.addAll(UserUtils.getMemberships(identity));      
    } else {
      memberships.addAll(UserUtils.getSpaceMemberships(space_group_id));
    }    
    
    return projectService.getProjectTreeByMembership(memberships);
  }

  public static List<Project> buildRootProjects(List<Project> projects) {
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
  
  public static List<Project> flattenTree(List<Project> projectTree) {
    if (projectTree == null) {
      return null;
    }

    List<Project> projects = new LinkedList<Project>();
    for (Project p : projectTree) {
      projects.add(p);
      if (!p.getChildren().isEmpty()) {
        projects.addAll(flattenTree(p.getChildren()));
      }
    }    
    return projects;
  }


  //TODO: should move this method to web module
  public static String buildBreadcumbs(Long id, ProjectService projectService, ResourceBundle bundle) {
    Project project = null;
    if (id > 0) {
      try {
        project = projectService.getProjectById(id);
      } catch (ProjectNotFoundException e) {
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
}


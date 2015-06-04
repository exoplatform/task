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

import org.exoplatform.task.domain.Project;

import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

/**
 * Created by The eXo Platform SAS
 * Author : Thibault Clement
 * tclement@exoplatform.com
 * 6/3/15
 */
public final class ProjectUtil {

  public static final int INCOMING_PROJECT_ID = -1;
  public static final int TODO_PROJECT_ID = -2;

  private ProjectUtil() {
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

}


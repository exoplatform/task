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

import org.exoplatform.task.domain.Status;
import org.exoplatform.task.domain.Task;

import java.util.*;

/**
 * Created by The eXo Platform SAS
 * Author : Thibault Clement
 * tclement@exoplatform.com
 * 6/3/15
 */
public final class TaskUtil {

  private TaskUtil() {
  }

  public static Map<String, List<Task>> groupTasks(List<Task> tasks, String groupBy) {
    Map<String, List<Task>> maps = new HashMap<String, List<Task>>();
    for(Task task : tasks) {
      for (String key : getGroupName(task, groupBy)) {
        List<Task> list = maps.get(key);
        if(list == null) {
          list = new LinkedList<Task>();
          maps.put(key, list);
        }
        list.add(task);
      }
    }
    return maps;
  }

  private static String[] getGroupName(Task task, String groupBy) {
    if("project".equalsIgnoreCase(groupBy)) {
      Status s = task.getStatus();
      if(s == null) {
        return new String[] {"No Project"};
      } else {
        return new String[] {s.getProject().getName()};
      }
    } else if("status".equalsIgnoreCase(groupBy)) {
      Status s = task.getStatus();
      if(s == null) {
        return new String[] {"TODO"};
      } else {
        return new String[] {s.getName()};
      }
    } else if("tag".equalsIgnoreCase(groupBy)) {
      Set<String> tags = task.getTags();
      return tags == null || tags.size() == 0 ? new String[] {"Un tagged"} : tags.toArray(new String[0]);
    } else if ("assignee".equalsIgnoreCase(groupBy)) {
      String assignee = task.getAssignee();
      return new String[] {assignee != null ? assignee : "Unassigned"};
    }
    return new String[0];
  }

}


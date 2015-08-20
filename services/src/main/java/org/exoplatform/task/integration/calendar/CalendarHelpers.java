/**
 * Copyright (C) 2015 eXo Platform SAS.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Affero General Public License
 * as published by the Free Software Foundation; either version 3
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, see<http://www.gnu.org/licenses/>.
 **/
  
package org.exoplatform.task.integration.calendar;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.exoplatform.calendar.model.Calendar;
import org.exoplatform.container.ExoContainerContext;
import org.exoplatform.task.domain.Project;
import org.exoplatform.task.domain.Task;
import org.exoplatform.task.service.ProjectService;

public class CalendarHelpers {
  public static Calendar buildCalendar(Calendar calendar, Project project) {
    if (project == null || calendar == null) {
      return null;
    }

    calendar.setCalendarColor(project.getColor());
    calendar.setDescription(project.getDescription());
    calendar.setEditPermission(null);
    ProjectService service = ExoContainerContext.getCurrentContainer().getComponentInstanceOfType(ProjectService.class);
    List<Task> tasks = service.getTasksByProjectId(Arrays.asList(project.getId()), null);
    calendar.setHasChildren(tasks.size() > 0);    
    calendar.setId(String.valueOf(project.getId()));
    calendar.setName(project.getName());
    Set<String> permissions = new HashSet<String>();
    permissions.addAll(project.getManager());
    permissions.addAll(project.getParticipator());
    calendar.setViewPermission(permissions.toArray(new String[permissions.size()]));    

    return calendar;
  }
}

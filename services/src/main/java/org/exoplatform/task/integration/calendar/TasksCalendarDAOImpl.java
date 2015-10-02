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
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import org.exoplatform.calendar.model.Calendar;
import org.exoplatform.calendar.model.query.CalendarQuery;
import org.exoplatform.calendar.storage.CalendarDAO;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.services.security.Identity;
import org.exoplatform.task.domain.Project;
import org.exoplatform.task.service.ProjectService;
import org.exoplatform.task.util.ProjectUtil;
import org.exoplatform.task.util.UserUtil;

public class TasksCalendarDAOImpl implements CalendarDAO {
  
  private ProjectService projectService;

  private static final Log          LOG   = ExoLogger.getExoLogger(TasksCalendarDAOImpl.class);
  
  public final Calendar DF_CALENDAR;
  {
    DF_CALENDAR = newInstance();
    DF_CALENDAR.setId(String.valueOf(ProjectUtil.TODO_PROJECT_ID));
    DF_CALENDAR.setHasChildren(true);
    DF_CALENDAR.setName("Tasks");
  }

  public TasksCalendarDAOImpl(ProjectService projectService) {
    this.projectService = projectService;
  }

  @Override
  public Calendar getById(String id) {
    try {
      Calendar cal = newInstance();
      if (DF_CALENDAR.getId().equals(id)) {
        return DF_CALENDAR;
      } else {
        Project project = projectService.getProject(Long.valueOf(id));
        if (project.isCalendarIntegrated()) {
          return ProjectUtil.buildCalendar(cal, project);          
        }
      }
    } catch (Exception ex) {
      LOG.error("Exception while loading calendar by ID", ex);
    }    
    return null;    
  }

  @Override
  public List<Calendar> findCalendars(CalendarQuery query) {   
    Identity identity = query.getIdentity();
    List<String> permissions = UserUtil.getMemberships(identity);
        
    //List<Project> projects = projectService.getProjectTreeByMembership(permissions);
    List<Project> projects = projectService.findProjects(permissions, null, null);

    projects = ProjectUtil.flattenTree(projects, projectService);

    if (query.getExclusions() != null) {
      projects = filterExclusions(projects, query.getExclusions());      
    }
    
    List<Calendar> calendars = new LinkedList<Calendar>();
    if (query.getExclusions() == null || !Arrays.asList(query.getExclusions()).contains(DF_CALENDAR.getId())) {
      calendars.add(DF_CALENDAR);
    }    
    for (Project p : projects) {
      if (p.isCalendarIntegrated()) {
        Calendar cal = newInstance();
        calendars.add(ProjectUtil.buildCalendar(cal, p));        
      }
    }    
    
    return calendars;
  }
  
  private List<Project> filterExclusions(List<Project> projects, String[] excludesId) {
    for (String id : excludesId) {
      Iterator<Project> iter = projects.iterator();
      while (iter.hasNext()) {
        Project p = iter.next();
        if (p.getId() == Long.valueOf(id)) {
          iter.remove();
        }
      }
    }
    return projects;
  }

  @Override
  public Calendar save(Calendar calendar) {
    throw new UnsupportedOperationException();
  }

  @Override
  public Calendar update(Calendar cal) {
    throw new UnsupportedOperationException();
  }

  @Override
  public Calendar remove(String id) {
    throw new UnsupportedOperationException();
  }

  @Override
  public Calendar newInstance() {
    Calendar c = new Calendar("");
    c.setDS(TasksStorage.TASKS_STORAGE);
    return c;
  }
}

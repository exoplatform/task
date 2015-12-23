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

import org.exoplatform.calendar.model.Calendar;
import org.exoplatform.calendar.model.query.CalendarQuery;
import org.exoplatform.calendar.storage.CalendarDAO;
import org.exoplatform.commons.utils.HTMLEntityEncoder;
import org.exoplatform.commons.utils.ListAccess;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.services.security.Identity;
import org.exoplatform.task.dao.TaskQuery;
import org.exoplatform.task.domain.Project;
import org.exoplatform.task.domain.Task;
import org.exoplatform.task.service.ProjectService;
import org.exoplatform.task.service.TaskService;
import org.exoplatform.task.util.ListUtil;
import org.exoplatform.task.util.ProjectUtil;
import org.exoplatform.task.util.UserUtil;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

public class TasksCalendarDAOImpl implements CalendarDAO {

  private static final String NAME_SUFFIX = " Tasks";
  
  private ProjectService projectService;
  private TaskService taskService;

  private static final Log          LOG   = ExoLogger.getExoLogger(TasksCalendarDAOImpl.class);
  
  public final Calendar DF_CALENDAR;
  {
    DF_CALENDAR = newInstance();
    DF_CALENDAR.setId(String.valueOf(ProjectUtil.TODO_PROJECT_ID));
    DF_CALENDAR.setHasChildren(true);
    DF_CALENDAR.setName("Tasks");
  }

  public TasksCalendarDAOImpl(ProjectService projectService, TaskService taskService) {
    this.projectService = projectService;
    this.taskService = taskService;
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
          return buildCalendar(cal, project);
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
        
    ListAccess<Project> tmp = projectService.findProjects(permissions, null, null);
    List<Project> projects = new LinkedList<Project>();
    try {
      projects = Arrays.asList(tmp.load(0, -1));
    } catch (Exception ex) {
      LOG.error("Can't load project list", ex);
    }
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
        calendars.add(buildCalendar(cal, p));
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

  private Calendar buildCalendar(Calendar calendar, Project project) {
    if (project == null || calendar == null) {
      return null;
    }
    HTMLEntityEncoder encoder = HTMLEntityEncoder.getInstance();
    if (project.getColor() != null) {
      calendar.setCalendarColor(encoder.encodeHTMLAttribute(project.getColor()));
    }
    calendar.setDescription(project.getDescription());
    calendar.setEditPermission(null);
    //ProjectService service = ExoContainerContext.getCurrentContainer().getComponentInstanceOfType(ProjectService.class);
    //TaskService taskService = ExoContainerContext.getCurrentContainer().getComponentInstanceOfType(TaskService.class);
    TaskQuery taskQuery = new TaskQuery();
    taskQuery.setProjectIds(Arrays.asList(project.getId()));
    //List<Task> tasks = service.getTasksByProjectId(Arrays.asList(project.getId()), null);
    ListAccess<Task> listTask = taskService.findTasks(taskQuery);
    calendar.setHasChildren(ListUtil.getSize(listTask) > 0);
    calendar.setId(String.valueOf(project.getId()));
    calendar.setName(encoder.encode(project.getName()) + NAME_SUFFIX);
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
}

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

import org.exoplatform.calendar.storage.CalendarDAO;
import org.exoplatform.calendar.storage.EventDAO;
import org.exoplatform.calendar.storage.Storage;
import org.exoplatform.container.component.BaseComponentPlugin;
import org.exoplatform.task.legacy.service.ProjectService;
import org.exoplatform.task.legacy.service.TaskService;

public class TasksStorage extends BaseComponentPlugin implements Storage {

  public static final String TASKS_STORAGE = "tasks";

  private CalendarDAO calendarDAO;
  private EventDAO eventDAO;
  
  @Override
  public String getId() {
    return TASKS_STORAGE;
  }

  public TasksStorage(ProjectService projectService, TaskService taskService) {
    calendarDAO = new TasksCalendarDAOImpl(projectService, taskService);
    eventDAO = new TasksEventDAOImpl(taskService);
  }

  @Override
  public CalendarDAO getCalendarDAO() {
    return calendarDAO;
  }

  @Override
  public EventDAO getEventDAO() {
    return eventDAO;
  }
}

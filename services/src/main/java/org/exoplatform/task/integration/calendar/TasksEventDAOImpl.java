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

package org.exoplatform.task.integration.calendar;

import java.util.LinkedList;
import java.util.List;

import org.exoplatform.calendar.model.Event;
import org.exoplatform.calendar.model.query.EventQuery;
import org.exoplatform.calendar.service.Utils;
import org.exoplatform.calendar.storage.EventDAO;
import org.exoplatform.commons.utils.ListAccess;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.task.dao.OrderBy;
import org.exoplatform.task.domain.Task;
import org.exoplatform.task.service.ProjectService;
import org.exoplatform.task.service.TaskService;
import org.exoplatform.task.utils.TaskUtil;

public class TasksEventDAOImpl implements EventDAO {

  private ProjectService projectService;
  
  private TaskService taskService;

  private static final Log LOG = ExoLogger.getExoLogger(TasksCalendarDAOImpl.class);

  public TasksEventDAOImpl(TaskService taskService, ProjectService projectService) {
    this.taskService = taskService;
    this.projectService = projectService;
  }

  @Override
  public Event getById(String id) {
    try {
      Task task = taskService.getTaskById(Long.valueOf(id));
      if (task.getStartDate() != null || task.getDueDate() != null) {
        Event event = newInstance();
        return TaskUtil.buildEvent(event, task);        
      } else {
        LOG.warn("Can't map task: {} to event due to no workplan", id);
      }
    } catch (Exception e) {
      LOG.error(e.getMessage(), e);
    }
    return null;
  }

  @Override
  public ListAccess<Event> findEventsByQuery(EventQuery query) {
     List<Long> ids = new LinkedList<Long>();
     for (String calId : query.getCalendarIds()) {
       try {
         ids.add(Long.valueOf(calId));
       } catch (Exception ex) {         
       }
     }
     OrderBy orderBy = null;
     if (query.getOrderBy() != null) {
       if (Utils.ASCENDING.equals(query.getOrderType())) {
         orderBy = new OrderBy(query.getOrderBy()[0], true);         
       } else {
         orderBy = new OrderBy(query.getOrderBy()[0], false);
       }
     }
     
     List<Task> tasks = projectService.getTasksByProjectId(ids, orderBy);
     
     final List<Event> events = new LinkedList<Event>();
     if (query.getFromDate() != null || query.getToDate() != null) {
       for (Task t : tasks) {
         if (t.getStartDate() != null) {
           if (query.getFromDate() != null && query.getFromDate() <= t.getStartDate().getTime() + t.getDuration()) {
             events.add(TaskUtil.buildEvent(newInstance(), t));
           } else if (query.getToDate() != null && query.getToDate() >= t.getStartDate().getTime()) {
             events.add(TaskUtil.buildEvent(newInstance(), t));
           } else {
             events.add(TaskUtil.buildEvent(newInstance(), t));
           }
         } else if (t.getDueDate() != null) {
           if (query.getFromDate() != null && query.getFromDate() <= t.getDueDate().getTime()) {
             events.add(TaskUtil.buildEvent(newInstance(), t));
           } else if (query.getToDate() != null && query.getToDate() >= t.getCreatedTime().getTime()) {
             events.add(TaskUtil.buildEvent(newInstance(), t));
           } else {
             events.add(TaskUtil.buildEvent(newInstance(), t));
           }
         }
       }       
     }
     
    return new ListAccess<Event>() {
      @Override
      public int getSize() throws Exception {
        return events.size();
      }

      @Override
      public Event[] load(int offset, int limit) throws Exception, IllegalArgumentException {
        return Utils.subArray(events.toArray(new Event[getSize()]), offset, limit);
      }
    };
  }
  
  @Override
  public Event save(Event event) {
    throw new UnsupportedOperationException();
  }

  public Event update(Event event) {
    throw new UnsupportedOperationException();
  }

  @Override
  public Event remove(String id) {
    throw new UnsupportedOperationException();
  }

  @Override
  public Event newInstance() {
    Event event = new Event();
    return event;
  }
}

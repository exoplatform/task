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

import java.util.Date;
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
import org.exoplatform.task.dao.TaskQuery;
import org.exoplatform.task.domain.Task;
import org.exoplatform.task.service.ProjectService;
import org.exoplatform.task.service.TaskService;
import org.exoplatform.task.utils.ProjectUtil;
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
      if (task.getStartDate() != null) {
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
     if (query.getCalendarIds() != null) {
       for (String calId : query.getCalendarIds()) {
         try {
           Long id = Long.valueOf(calId);
           ids.add(id);
         } catch (Exception ex) {
         }
       }
     }
     
     List<OrderBy> orderBy = new LinkedList<OrderBy>();
     if (query.getOrderBy() != null) {
       boolean ascending = Utils.ASCENDING.equals(query.getOrderType()); 
       for (String by : query.getOrderBy()) {
         orderBy.add(new OrderBy(by, ascending));
       }
     }
     
     TaskQuery taskQuery = new TaskQuery();
     taskQuery.setProjectIds(ids);
     taskQuery.setOrderBy(orderBy);
     if (query.getFromDate() != null) {
       taskQuery.setStartDate(new Date(query.getFromDate()));
     }
     if (query.getToDate() != null) {
       taskQuery.setEndDate(new Date(query.getToDate()));
     }
     taskQuery.setKeyword(query.getText());
     taskQuery.setAssignee(query.getOwner());

     List<Task> tasks = taskService.findTaskByQuery(taskQuery);
     final List<Event> events = new LinkedList<Event>();
     for (Task t : tasks) {
       Event event = newInstance();
       events.add(TaskUtil.buildEvent(event, t));
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

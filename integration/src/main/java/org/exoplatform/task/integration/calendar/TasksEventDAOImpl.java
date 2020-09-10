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

import java.util.*;

import org.exoplatform.calendar.model.Event;
import org.exoplatform.calendar.model.query.EventQuery;
import org.exoplatform.calendar.service.CalendarService;
import org.exoplatform.calendar.service.Utils;
import org.exoplatform.calendar.storage.EventDAO;
import org.exoplatform.commons.utils.ListAccess;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.task.dao.OrderBy;
import org.exoplatform.task.dao.TaskQuery;
import org.exoplatform.task.domain.Task;
import org.exoplatform.task.legacy.service.TaskService;
import org.exoplatform.task.util.*;

public class TasksEventDAOImpl implements EventDAO {
  
  private TaskService taskService;

  private static final Log LOG = ExoLogger.getExoLogger(TasksCalendarDAOImpl.class);

  public TasksEventDAOImpl(TaskService taskService) {
    this.taskService = taskService;
  }

  @Override
  public Event getById(String id) {
    try {
      Task task = taskService.getTask(Long.valueOf(id));
      if (task.getStartDate() != null) {
        if (task.isCalendarIntegrated()) {
          Event event = newInstance();
          return CalendarIntegrationUtil.buildEvent(event, task);
        }
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
       if(ids.isEmpty() && query.getCalendarIds().length > 0) {
         return null;
       }
     }
     
     List<OrderBy> orderBy = new LinkedList<OrderBy>();
     if (query.getOrderBy() != null) {
       boolean ascending = Utils.ASCENDING.equals(query.getOrderType()); 
       for (String by : query.getOrderBy()) {
         if (Utils.EXO_SUMMARY.equals(by)) {
           by = "title";
         } else if (Utils.EXO_DESCRIPTION.equals(by)) {
           by = "description";
         } else if (Utils.EXO_FROM_DATE_TIME.equals(by)) {
           by = "startDate";
         } else if (Utils.EXO_TO_DATE_TIME.equals(by)) {
           by = "endDate";
         } else if (Utils.EXO_PRIORITY.equals(by)) {
           by = "priority";
         } else {           
           by = null;
         }
         if (by != null) {
           orderBy.add(new OrderBy(by, ascending));           
         }
       }
     }
     
     TaskQuery taskQuery = new TaskQuery();
     taskQuery.setOrderBy(orderBy);
     if (query.getFromDate() != null) {
       taskQuery.setStartDate(new Date(query.getFromDate()));
     }
     if (query.getToDate() != null) {
       taskQuery.setEndDate(new Date(query.getToDate()));
     }
     taskQuery.setKeyword(query.getText());
     taskQuery.setCalendarIntegrated(true);     

     Task[] tasks = new Task[0];
     if ((query.getCalendarIds() == null || ids.size() > 0) && (query.getCategoryIds() == null || (query.getCategoryIds().length == 1 && 
         query.getCategoryIds()[0].equals(CalendarService.DEFAULT_EVENTCATEGORY_ID_ALL))) &&  
         (query.getEventType() == null || query.getEventType().equals(Event.TYPE_TASK))) {
       //
       if (ids.size() > 0) {
         if (ids.contains(Long.valueOf(ProjectUtil.TODO_PROJECT_ID))) {
           ids.remove(Long.valueOf(ProjectUtil.TODO_PROJECT_ID));
           if (ids.size() > 0) {
             taskQuery.setAssigneeOrCoworkerOrInProject(query.getOwner(), ids);
           } else {
             taskQuery.setAssigneeOrCoworker(Arrays.asList(query.getOwner()));
           }
         } else {
           taskQuery.setProjectIds(ids);
         }         
       }

       tasks = ListUtil.load(taskService.findTasks(taskQuery), 0, -1); //taskService.findTaskByQuery(taskQuery);
     }
     final List<Event> events = new LinkedList<Event>();
     for (Task t : tasks) {
       Event event = newInstance();
       events.add(CalendarIntegrationUtil.buildEvent(event, t));
       if (event.getCalendarId() == null || !ids.contains(Long.valueOf(event.getCalendarId()))) {
         event.setCalendarId(String.valueOf(ProjectUtil.TODO_PROJECT_ID));
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

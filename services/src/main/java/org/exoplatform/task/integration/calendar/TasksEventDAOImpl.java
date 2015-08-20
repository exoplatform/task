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

import org.exoplatform.calendar.model.Event;
import org.exoplatform.calendar.model.query.EventQuery;
import org.exoplatform.calendar.storage.EventDAO;
import org.exoplatform.calendar.storage.Storage;
import org.exoplatform.commons.utils.ListAccess;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;

public class TasksEventDAOImpl implements EventDAO {

  private final Storage    context;

  private static final Log LOG = ExoLogger.getExoLogger(TasksCalendarDAOImpl.class);

  public TasksEventDAOImpl(TasksStorage storage) {
    this.context = storage;
  }

  @Override
  public Event getById(String id) {
    // try {
    // return dataStorage.getEventById(id);
    // } catch (Exception ex) {
    // LOG.error(ex);
    // }
    return null;
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

  @Override
  public ListAccess<Event> findEventsByQuery(EventQuery query) {
    // final List<CalendarEvent> events = new LinkedList<CalendarEvent>();
    // org.exoplatform.calendar.service.EventQuery eventQuery =
    // buildEvenQuery(query);
    //
    // int type = Calendar.Type.UNDEFINED.type();
    // if (query instanceof JCREventQuery) {
    // type = ((JCREventQuery)query).getCalType();
    // }
    // try {
    // if (Calendar.Type.UNDEFINED.type() == type ||
    // Calendar.Type.PERSONAL.type() == type) {
    // events.addAll(dataStorage.getUserEvents(query.getOwner(), eventQuery));
    // }
    //
    // if (Calendar.Type.UNDEFINED.type() == type || Calendar.Type.GROUP.type()
    // == type) {
    // events.addAll(dataStorage.getPublicEvents(eventQuery));
    // }
    // } catch (Exception ex) {
    // LOG.error("Can't query for event", ex);
    // }

    return null;
  }
}

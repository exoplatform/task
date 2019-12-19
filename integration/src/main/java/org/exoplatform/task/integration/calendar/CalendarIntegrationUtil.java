package org.exoplatform.task.integration.calendar;

import org.exoplatform.calendar.model.Event;
import org.exoplatform.calendar.service.CalendarService;
import org.exoplatform.commons.utils.HTMLEntityEncoder;
import org.exoplatform.task.domain.Task;
import org.exoplatform.task.util.ProjectUtil;

public class CalendarIntegrationUtil {

  private CalendarIntegrationUtil() {
  }

  public static Event buildEvent(Event event, Task task) {
    HTMLEntityEncoder encoder = HTMLEntityEncoder.getInstance();
    if (task.getStatus() != null) {
      event.setCalendarId(String.valueOf(task.getStatus().getProject().getId()));
      // TODO: how to process localization for status name when build calendar
      // event
      event.setEventState(encoder.encode(task.getStatus().getName()));
    } else {
      // usage of default task calendar when no status is set
      event.setCalendarId(String.valueOf(ProjectUtil.TODO_PROJECT_ID));
    }
    event.setDescription(task.getDescription());
    event.setEventCategoryId(CalendarService.DEFAULT_EVENTCATEGORY_ID_ALL);
    event.setEventCategoryName(CalendarService.DEFAULT_EVENTCATEGORY_NAME_ALL);
    event.setEventType(Event.TYPE_TASK);
    if (task.getStartDate() != null) {
      event.setFromDateTime(task.getStartDate());
      event.setToDateTime(task.getEndDate());
    } else {
      throw new IllegalStateException("Can't build event with a task that doesn't have workplan");
    }
    event.setId(String.valueOf(task.getId()));
    if (task.getPriority() != null) {
      switch (task.getPriority()) {
      case HIGH:
        event.setPriority(Event.PRIORITY_HIGH);
        break;
      case NORMAL:
        event.setPriority(Event.PRIORITY_NORMAL);
        break;
      case LOW:
        event.setPriority(Event.PRIORITY_LOW);
        break;
      default:
        event.setPriority(Event.PRIORITY_NONE);
      }
    }
    event.setSummary(task.getTitle());
    String assignee = task.getAssignee();
    if (assignee != null) {
      assignee = encoder.encode(assignee);
    }
    event.setTaskDelegator(assignee);
    return event;
  }

}

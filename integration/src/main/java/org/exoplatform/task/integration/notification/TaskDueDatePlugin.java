/*
 * Copyright (C) 2003-2015 eXo Platform SAS.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package org.exoplatform.task.integration.notification;

import org.exoplatform.commons.api.notification.NotificationContext;
import org.exoplatform.commons.api.notification.model.NotificationInfo;
import org.exoplatform.container.xml.InitParams;
import org.exoplatform.task.domain.Task;
import org.exoplatform.task.dto.TaskDto;

import java.util.Calendar;
import java.util.TimeZone;

public class TaskDueDatePlugin extends AbstractNotificationPlugin {
  
  public TaskDueDatePlugin(InitParams initParams) {
    super(initParams);
  }

  public static final String ID = "TaskDueDatePlugin";
  
  @Override
  public String getId() {
    return ID;
  }

  @Override
  public NotificationInfo makeNotification(NotificationContext ctx) {
    NotificationInfo info = super.makeNotification(ctx);
    TaskDto task = ctx.value(NotificationUtils.TASK);

    Calendar grpDueDate = Calendar.getInstance(TimeZone.getTimeZone("GMT0"));
    grpDueDate.setTime(task.getDueDate());
    grpDueDate.set(Calendar.HOUR_OF_DAY, 0);
    grpDueDate.set(Calendar.MINUTE, 0);
    grpDueDate.set(Calendar.SECOND, 0);
    grpDueDate.set(Calendar.MILLISECOND, 0);
    
    //workaround: notification service has not implement query with filter
    //that allow to query with group of notifications by attributes
    String actId = info.getValueOwnerParameter(NotificationUtils.ACTIVITY_ID);
    info.with(NotificationUtils.ACTIVITY_ID, actId + String.valueOf(grpDueDate.getTimeInMillis()));
    
    //add dueDate for rendering
    info.with(NotificationUtils.DUE_DATE, String.valueOf(task.getDueDate().getTime()));
    return info;
  }

  @Override
  public boolean isValid(NotificationContext ctx) {
    TaskDto task = ctx.value(NotificationUtils.TASK);
    return task.getDueDate() != null && ((task.getAssignee() != null && !task.getAssignee().isEmpty()) ||
        (task.getCoworker() != null && task.getCoworker().size() > 0)||(task.getWatcher() != null && task.getWatcher().size() > 0));
  }
}

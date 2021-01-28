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
import org.exoplatform.container.xml.InitParams;
import org.exoplatform.task.domain.Task;
import org.exoplatform.task.dto.TaskDto;

public class TaskEditionPlugin extends AbstractNotificationPlugin {

  public TaskEditionPlugin(InitParams initParams) {
    super(initParams);
  }

  public static final String ID = "TaskEditionPlugin";

  @Override
  public String getId() {
    return ID;
  }

  @Override
  public boolean isValid(NotificationContext ctx) {
    TaskDto task = ctx.value(NotificationUtils.TASK);
    return((task.getAssignee() != null && !task.getAssignee().isEmpty()) ||
            (task.getCoworker() != null && task.getCoworker().size() > 0)||(task.getWatcher() != null && task.getWatcher().size() > 0) || (task.getCreatedBy() != null && !task.getCreatedBy().isEmpty()));
  }
}

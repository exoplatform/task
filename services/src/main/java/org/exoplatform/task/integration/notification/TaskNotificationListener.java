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
  
package org.exoplatform.task.integration.notification;

import java.util.HashSet;
import java.util.Set;

import org.exoplatform.commons.api.notification.NotificationContext;
import org.exoplatform.commons.api.notification.model.PluginKey;
import org.exoplatform.commons.notification.impl.NotificationContextImpl;
import org.exoplatform.services.security.ConversationState;
import org.exoplatform.task.domain.Task;
import org.exoplatform.task.service.TaskListener;
import org.exoplatform.task.service.impl.TaskEvent;
import org.exoplatform.task.service.impl.TaskEvent.Type;

public class TaskNotificationListener implements TaskListener {

  @Override
  public void event(TaskEvent event) {    
    Type type = event.getType();

    switch (type) {
    case EDIT_ASSIGNEE:
      assignEvt(event);
      break;
    case EDIT_COWORKER:
      coworkerEvt(event);
      break;
    case EDIT_DUEDATE:
      dueDateEvt(event);
      break;
    case MARK_DONE:
      completedEvt(event);
      break;
    default:
      return;
    }
  }

  private void completedEvt(TaskEvent event) {
    NotificationContext ctx = buildContext(event);

    dispatch(ctx, TaskCompletedPlugin.ID);
  }

  private void dueDateEvt(TaskEvent event) {
    NotificationContext ctx = buildContext(event);

    dispatch(ctx, TaskDueDatePlugin.ID);
  }

  private void coworkerEvt(TaskEvent event) {
    NotificationContext ctx = buildContext(event);
    
    Set<String> oldVal = (Set<String>)event.getOldVal();
    Set<String> newVal = (Set<String>)event.getNewVal();
    newVal.removeAll(oldVal);    
    ctx.append(NotificationUtils.COWORKER, newVal);
    
    dispatch(ctx, TaskCoworkerPlugin.ID);
  }

  private void assignEvt(TaskEvent event) {
    NotificationContext ctx = buildContext(event);

    dispatch(ctx, TaskAssignPlugin.ID);
  }
  
  private NotificationContext buildContext(TaskEvent event) {
    Task task = event.getTask();
    //workaround to init hibernate collection
    task.getCoworker().size();
    NotificationContext ctx = NotificationContextImpl.cloneInstance().append(NotificationUtils.TASK, task);
    
    String creator = ConversationState.getCurrent().getIdentity().getUserId();    
    ctx.append(NotificationUtils.CREATOR, creator);
    return ctx;
  }

  private void dispatch(NotificationContext ctx, String pluginId) {    
    ctx.getNotificationExecutor().with(ctx.makeCommand(PluginKey.key(pluginId)))
                                 .execute(ctx);
  }

}

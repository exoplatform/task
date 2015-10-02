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
  
package org.exoplatform.task.service.impl;

import java.util.Set;

import org.apache.commons.lang.StringUtils;

import org.exoplatform.commons.api.notification.NotificationContext;
import org.exoplatform.commons.api.notification.model.PluginKey;
import org.exoplatform.commons.notification.impl.NotificationContextImpl;
import org.exoplatform.services.listener.Event;
import org.exoplatform.services.listener.Listener;
import org.exoplatform.services.security.ConversationState;
import org.exoplatform.task.domain.Task;
import org.exoplatform.task.exception.EntityNotFoundException;
import org.exoplatform.task.integration.notification.NotificationUtils;
import org.exoplatform.task.integration.notification.TaskAssignPlugin;
import org.exoplatform.task.integration.notification.TaskCompletedPlugin;
import org.exoplatform.task.integration.notification.TaskDueDatePlugin;
import org.exoplatform.task.service.Payload;
import org.exoplatform.task.service.TaskService;

public class TaskLoggingListener extends Listener<TaskService, Payload> {

  @Override
  public void onEvent(Event<TaskService, Payload> event) throws Exception {
    TaskService service = event.getSource();
    String username = ConversationState.getCurrent().getIdentity().getUserId();
    Payload data = event.getData();

    Object oldTask = data.before();
    Object newTask = data.after();

    if (oldTask == null && newTask != null) {
      service.addTaskLog(((Task)newTask).getId(), username, "log.created", "");
    }

    if (oldTask != null && newTask != null) {
      logTaskUpdate(service, username, (Task)oldTask, (Task)newTask);
    }
  }

  private void logTaskUpdate(TaskService service, String username, Task before, Task after) throws EntityNotFoundException {
    if (!before.getStartDate().equals(after.getStartDate()) || !before.getEndDate().equals(after.getEndDate())) {
      service.addTaskLog(after.getId(), username, "log.edit_workplan", "");
    }
    if (!before.getTitle().equals(after.getTitle())) {
      service.addTaskLog(after.getId(), username, "log.edit_title", "");
    }
    if (!before.getDueDate().equals(after.getDueDate())) {
      service.addTaskLog(after.getId(), username, "log.edit_duedate", "");

      NotificationContext ctx = buildContext(after);
      dispatch(ctx, TaskDueDatePlugin.ID);
    }
    if (!before.getStatus().equals(after.getStatus())) {
      service.addTaskLog(after.getId(), username, "log.edit_status", after.getStatus().getName());
    }
    if (!before.getDescription().equals(after.getDescription())) {
      service.addTaskLog(after.getId(), username, "log.edit_description", "");
    }
    if (!(before.isCompleted() == after.isCompleted())) {
      service.addTaskLog(after.getId(), username, "log.mark_done", "");

      NotificationContext ctx = buildContext(after);
      dispatch(ctx, TaskCompletedPlugin.ID);
    }
    if (!before.getAssignee().equals(after.getAssignee())) {
      service.addTaskLog(after.getId(), username, "log.assign", after.getAssignee());

      NotificationContext ctx = buildContext(after);
      dispatch(ctx, TaskAssignPlugin.ID);
    }
    if (!before.getTag().equals(after.getTag())) {
      Set<String> tags = after.getTag();
      tags.removeAll(before.getTag());
      service.addTaskLog(after.getId(), username, "log.add_label", StringUtils.join(tags, ","));
    }
    if (!before.getStatus().getProject().equals(after.getStatus().getProject())) {
      service.addTaskLog(after.getId(), username, "log.edit_project", after.getStatus().getProject().getName());
    }
    // Coworker
//    if (!before.getCoworker().equals(after.getCoworker())) {
//      NotificationContext ctx = buildContext(after);
//      
//      Set<String> oldVal = before.getCoworker();
//      Set<String> newVal = after.getCoworker();
//      newVal.removeAll(oldVal);    
//      ctx.append(NotificationUtils.COWORKER, newVal);
//      
//      dispatch(ctx, TaskCoworkerPlugin.ID);
//    }
  }

  
  private NotificationContext buildContext(Task task) {
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

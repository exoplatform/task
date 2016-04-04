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
  
package org.exoplatform.task.integration;

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
import org.exoplatform.task.integration.notification.TaskCoworkerPlugin;
import org.exoplatform.task.integration.notification.TaskDueDatePlugin;
import org.exoplatform.task.service.TaskPayload;
import org.exoplatform.task.service.TaskService;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * TODO: please reconsider where should this class stay? I move it to integration module because it depends on these class
 */
public class TaskLoggingListener extends Listener<TaskService, TaskPayload> {

  @Override
  public void onEvent(Event<TaskService, TaskPayload> event) throws Exception {
    TaskService service = event.getSource();
    String username = ConversationState.getCurrent().getIdentity().getUserId();
    TaskPayload data = event.getData();

    Object oldTask = data.before();
    Object newTask = data.after();

    if (oldTask == null && newTask != null) {
      service.addTaskLog(((Task)newTask).getId(), username, "created", "");
    }

    if (oldTask != null && newTask != null) {
      logTaskUpdate(service, username, (Task)oldTask, (Task)newTask);
    }
  }

  private void logTaskUpdate(TaskService service, String username, Task before, Task after) throws EntityNotFoundException {
    if (isDiff(before.getStartDate(), after.getStartDate()) || isDiff(before.getEndDate(), after.getEndDate())) {
      service.addTaskLog(after.getId(), username, "edit_workplan", "");
    }
    if (isDiff(before.getTitle(), after.getTitle())) {
      service.addTaskLog(after.getId(), username, "edit_title", "");
    }
    if (isDiff(before.getDueDate(), after.getDueDate())) {
      service.addTaskLog(after.getId(), username, "edit_duedate", "");

      NotificationContext ctx = buildContext(after);
      dispatch(ctx, TaskDueDatePlugin.ID);
    }
    if (isDiff(before.getDescription(), after.getDescription())) {
      service.addTaskLog(after.getId(), username, "edit_description", "");
    }
    if (isDiff(before.isCompleted(), after.isCompleted())) {
      service.addTaskLog(after.getId(), username, "mark_done", "");

      NotificationContext ctx = buildContext(after);
      dispatch(ctx, TaskCompletedPlugin.ID);
    }
    if (isDiff(before.getAssignee(), after.getAssignee())) {
      if (after.getAssignee() != null && !after.getAssignee().trim().isEmpty()) {
        service.addTaskLog(after.getId(), username, "assign", after.getAssignee());
      } else {
        service.addTaskLog(after.getId(), username, "unassign", before.getAssignee());
      }

      NotificationContext ctx = buildContext(after);
      dispatch(ctx, TaskAssignPlugin.ID);
    }

    if (after.getCoworker() != null && !after.getCoworker().isEmpty()) {
      Set<String> receiver = new HashSet<String>();
      Set<String> coworkers = before.getCoworker();
      if (coworkers == null) {
        coworkers = Collections.emptySet();
      }
      for (String user : after.getCoworker()) {
        if (!coworkers.contains(user)) {
          receiver.add(user);
        }
      }

      if (!receiver.isEmpty()) {
        NotificationContext ctx = buildContext(after);
        ctx.append(NotificationUtils.COWORKER, receiver);
        dispatch(ctx, TaskCoworkerPlugin.ID);
      }
    }

    if (isProjectChange(before, after)) {
      if (after.getStatus() != null) {
        service.addTaskLog(after.getId(), username, "edit_project", after.getStatus().getProject().getName());
      } else {
        service.addTaskLog(after.getId(), username, "remove_project", "");
      }

    } else if (isDiff(before.getStatus(), after.getStatus())) {
      service.addTaskLog(after.getId(), username, "edit_status", after.getStatus().getName());
    }
  }

  private boolean isProjectChange(Task before, Task after) {
    if (!isDiff(before.getStatus(), after.getStatus())) {
      return false;

    } else if(before.getStatus() != null) {
      if (after.getStatus() == null) {
        return true;
      } else {
        return !before.getStatus().getProject().equals(after.getStatus().getProject());
      }
    } else {
      return true;
    }
  }
  private boolean isDiff(Object before, Object after) {
    if (before == after) {
      return false;
    }
    if (before != null) {
      return !before.equals(after);
    } else {
      return !after.equals(before);
    }
  }
  
  private NotificationContext buildContext(Task task) {
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

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
import org.exoplatform.commons.utils.CommonsUtils;
import org.exoplatform.services.listener.Event;
import org.exoplatform.services.listener.Listener;
import org.exoplatform.services.security.ConversationState;
import org.exoplatform.task.dto.TaskDto;
import org.exoplatform.task.exception.EntityNotFoundException;
import org.exoplatform.task.integration.notification.*;
import org.exoplatform.task.service.TaskPayload;
import org.exoplatform.task.service.TaskService;

import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

/**
 * TODO: please reconsider where should this class stay? I move it to integration module because it depends on these class
 */
public class TaskLoggingListener extends Listener<TaskService, TaskPayload> {

  private org.exoplatform.task.service.TaskService taskService;

  public TaskLoggingListener(org.exoplatform.task.service.TaskService taskService) {
    this.taskService = taskService;
  }

  @Override
  public void onEvent(Event<TaskService, TaskPayload> event) throws Exception {
    TaskService service = event.getSource();
    if(service == null){
      service = CommonsUtils.getService(TaskService.class);
    }
    String username = ConversationState.getCurrent().getIdentity().getUserId();
    TaskPayload data = event.getData();

    TaskDto oldTask = data.before();
    TaskDto newTask = data.after();

    if (oldTask == null && newTask != null) {
      service.addTaskLog(newTask.getId(), username, "created", "");
      notifyAssignee(null, newTask, username);
      notifyCoworker(null, newTask, username);
    }

    if (oldTask != null && newTask != null) {
      logTaskUpdate(service, username, (TaskDto)oldTask, (TaskDto)newTask);
    }
  }

  private void logTaskUpdate(TaskService service, String username, TaskDto before, TaskDto after) throws EntityNotFoundException {
    if (isDateDiff(before.getStartDate(), after.getStartDate()) || isDateDiff(before.getEndDate(), after.getEndDate())) {
      service.addTaskLog(after.getId(), username, "edit_workplan", "");

      NotificationContext ctx = buildContext(after);
      ctx.append(NotificationUtils.ACTION_NAME, "edit_workplan");
      dispatch(ctx, TaskEditionPlugin.ID);
    }
    if (isDiff(before.getTitle(), after.getTitle())) {
      service.addTaskLog(after.getId(), username, "edit_title", "");

      NotificationContext ctx = buildContext(after);
      dispatch(ctx, TaskEditionPlugin.ID);
      ctx.append(NotificationUtils.ACTION_NAME, "edit_title");
    }
    if (isDateDiff(before.getDueDate(), after.getDueDate())) {
      service.addTaskLog(after.getId(), username, "edit_duedate", "");

      NotificationContext ctx = buildContext(after);
      dispatch(ctx, TaskDueDatePlugin.ID);
    }
    if (isDiff(before.getDescription(), after.getDescription())) {
      service.addTaskLog(after.getId(), username, "edit_description", "");

      NotificationContext ctx = buildContext(after);
      ctx.append(NotificationUtils.ACTION_NAME, "edit_description");
      dispatch(ctx, TaskEditionPlugin.ID);
    }
    if(isDiff(before.getPriority(),after.getPriority())) {
      service.addTaskLog(after.getId(), username ,"edit_priority","");

      NotificationContext ctx = buildContext(after);
      ctx.append(NotificationUtils.ACTION_NAME, "edit_priority");
      dispatch(ctx, TaskEditionPlugin.ID);
    }
    if (isDiff(before.isCompleted(), after.isCompleted())) {
      service.addTaskLog(after.getId(), username, "mark_done", "");

      NotificationContext ctx = buildContext(after);
      dispatch(ctx, TaskCompletedPlugin.ID);
    }
    // Notify assignee if any
    notifyAssignee(before, after, username);
    // Notify coworker if any
    notifyCoworker(before, after, username);

    if (isProjectChange(before, after)) {
      if (after.getStatus() != null) {
        service.addTaskLog(after.getId(), username, "edit_project", after.getStatus().getProject().getName());
      } else {
        service.addTaskLog(after.getId(), username, "remove_project", "");
      }

    } else if (before.getStatus().getId() != after.getStatus().getId()) {
      service.addTaskLog(after.getId(), username, "edit_status", after.getStatus().getName());

      NotificationContext ctx = buildContext(after);
      ctx.append(NotificationUtils.ACTION_NAME, "edit_status");
      dispatch(ctx,TaskEditionPlugin.ID);
    }
  }

  private boolean isDateDiff(Date dateBefore, Date dateAfter) {
    if(dateBefore != null && dateAfter == null) {
      return true;
    }
    if(dateBefore == null && dateAfter != null) {
      return true;
    }
    if(dateBefore != null) {
      return dateBefore.getTime() != dateAfter.getTime();
    }
    return false;
  }

  private void notifyAssignee(TaskDto before, TaskDto after, String username) throws EntityNotFoundException {
    if (before == null || isDiff(before.getAssignee(), after.getAssignee())) {
      if (StringUtils.isNotBlank(after.getAssignee())) {
        taskService.addTaskLog(after.getId(), username, "assign", after.getAssignee());
      } else if(before != null){
        taskService.addTaskLog(after.getId(), username, "unassign", before.getAssignee());
      }
      if(after.getAssignee() != null && !username.equals(after.getAssignee())) {
        NotificationContext ctx = buildContext(after);
        dispatch(ctx, TaskAssignPlugin.ID);
      }
    }
  }

  private boolean isProjectChange(TaskDto before, TaskDto after) {
    if (before.getStatus().getId()==after.getStatus().getId()) {
      return false;

    } else if(before.getStatus() != null) {
      if (after.getStatus() == null) {
        return true;
      } else {
        return before.getStatus().getProject().getId()!=after.getStatus().getProject().getId();
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
      return true;
    }
  }
  
  private NotificationContext buildContext(TaskDto task) {
    NotificationContext ctx = NotificationContextImpl.cloneInstance().append(NotificationUtils.TASK, task);
    
    String creator = ConversationState.getCurrent().getIdentity().getUserId();    
    ctx.append(NotificationUtils.CREATOR, creator);
    return ctx;
  }

  private void dispatch(NotificationContext ctx, String pluginId) {    
    ctx.getNotificationExecutor().with(ctx.makeCommand(PluginKey.key(pluginId)))
                                 .execute(ctx);
  }

  private void notifyCoworker(TaskDto before, TaskDto after, String username) {
    Set<String> receiver = new HashSet<String>();
    Set<String> coworkers = new HashSet<String>();
    if(before != null) {
      coworkers = before.getCoworker();
      if (coworkers == null) {
        coworkers = Collections.emptySet();
      }
    }
    if (after.getCoworker() != null && !after.getCoworker().isEmpty()) {
      for (String user : after.getCoworker()) {
        if (!coworkers.contains(user) && !user.equals(username)) {
          receiver.add(user);
        }
      }
    }

    if (!receiver.isEmpty()) {
      NotificationContext ctx = buildContext(after);
      ctx.append(NotificationUtils.COWORKER, receiver);
      dispatch(ctx, TaskCoworkerPlugin.ID);
    }
  }
}

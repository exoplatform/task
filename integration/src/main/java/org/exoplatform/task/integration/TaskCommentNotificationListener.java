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

package org.exoplatform.task.integration;

import org.exoplatform.commons.api.notification.NotificationContext;
import org.exoplatform.commons.api.notification.command.NotificationCommand;
import org.exoplatform.commons.api.notification.model.PluginKey;
import org.exoplatform.commons.notification.impl.NotificationContextImpl;
import org.exoplatform.container.PortalContainer;
import org.exoplatform.services.listener.Event;
import org.exoplatform.services.listener.Listener;
import org.exoplatform.services.security.ConversationState;
import org.exoplatform.task.dto.CommentDto;
import org.exoplatform.task.dto.TaskDto;
import org.exoplatform.task.integration.notification.NotificationUtils;
import org.exoplatform.task.integration.notification.TaskCommentPlugin;
import org.exoplatform.task.integration.notification.TaskMentionPlugin;
import org.exoplatform.task.service.CommentService;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * @author <a href="mailto:tuyennt@exoplatform.com">Tuyen Nguyen The</a>.
 */
public class TaskCommentNotificationListener extends Listener<TaskDto, CommentDto> {

  @Override
  public void onEvent(Event<TaskDto, CommentDto> event) throws Exception {
    TaskDto task = event.getSource();
    CommentDto comment = event.getData();
    //. How to send notification
    NotificationContext ctx = buildContext(task, comment);
    dispatch(ctx, TaskCommentPlugin.ID, TaskMentionPlugin.ID);
  }

  private NotificationContext buildContext(TaskDto task, CommentDto comment) {
    NotificationContext ctx = NotificationContextImpl.cloneInstance()
            .append(NotificationUtils.COMMENT, comment)
            .append(NotificationUtils.TASK, task);

    String creator = ConversationState.getCurrent().getIdentity().getUserId();
    ctx.append(NotificationUtils.CREATOR, creator);

    //. Receiver
    Set<String> receiver = new HashSet<String>();

    // Task creator
    receiver.add(task.getCreatedBy());

    // Assignee , Coworker , and watcher
    if (task.getAssignee() != null && !task.getAssignee().isEmpty()) {
      receiver.add(task.getAssignee());
    }
    Set<String> coworker = task.getCoworker();
    if (coworker != null && coworker.size() > 0) {
      receiver.addAll(coworker);
    }
    Set<String> watcher = task.getWatcher() ;
    if (watcher != null && watcher.size() > 0) {
      receiver.addAll(watcher);
    }

    // All user who commented on this task
    CommentService commentService = PortalContainer.getInstance().getComponentInstanceOfType(CommentService.class);
    List<CommentDto> comments = commentService.getComments(task.getId(), 0, 0);
    if (comments != null && comments.size() > 0) {
      for (CommentDto c : comments) {
        receiver.add(c.getAuthor());
        List<CommentDto> subComments = c.getSubComments();
        if (subComments != null) {
          for (CommentDto subComment : subComments) {
            receiver.add(subComment.getAuthor());
          }
        }
      }
    }

    // Remove the user who create this comment, he should not receive the notification
    receiver.remove(creator);

    // Get all mentioned
    Set<String> mentioned = comment.getMentionedUsers();
    if(mentioned==null){
      mentioned = new HashSet<String>();
    }
    mentioned.remove(creator);
    ctx.append(NotificationUtils.RECEIVERS, receiver);
    ctx.append(NotificationUtils.MENTIONED, mentioned);

    return ctx;
  }

  private void dispatch(NotificationContext ctx, String... pluginId) {
    List<NotificationCommand> commands = new ArrayList<NotificationCommand>(pluginId.length);
    for (String p : pluginId) {
      commands.add(ctx.makeCommand(PluginKey.key(p)));
    }

    ctx.getNotificationExecutor().with(commands).execute(ctx);
  }

}

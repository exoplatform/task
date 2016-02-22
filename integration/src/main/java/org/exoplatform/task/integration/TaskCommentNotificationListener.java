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
import org.exoplatform.commons.utils.ListAccess;
import org.exoplatform.services.listener.Event;
import org.exoplatform.services.listener.Listener;
import org.exoplatform.services.security.ConversationState;
import org.exoplatform.task.domain.Comment;
import org.exoplatform.task.domain.Task;
import org.exoplatform.task.integration.notification.NotificationUtils;
import org.exoplatform.task.integration.notification.TaskCommentPlugin;
import org.exoplatform.task.integration.notification.TaskMentionPlugin;
import org.exoplatform.task.service.TaskService;
import org.exoplatform.task.util.ListUtil;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author <a href="mailto:tuyennt@exoplatform.com">Tuyen Nguyen The</a>.
 */
public class TaskCommentNotificationListener extends Listener<TaskService, Comment> {

  private Pattern MENTION_REGEX = Pattern.compile("(\\s|^)(@)([\\S]+)");

  @Override
  public void onEvent(Event<TaskService, Comment> event) throws Exception {
    TaskService taskService = event.getSource();
    Comment comment = event.getData();

    //. How to send notification
    NotificationContext ctx = buildContext(taskService, comment);
    dispatch(ctx, TaskCommentPlugin.ID, TaskMentionPlugin.ID);
  }

  private NotificationContext buildContext(TaskService taskService, Comment comment) {
    Task task = comment.getTask();

    NotificationContext ctx = NotificationContextImpl.cloneInstance()
            .append(NotificationUtils.COMMENT, comment)
            .append(NotificationUtils.TASK, task);

    String creator = ConversationState.getCurrent().getIdentity().getUserId();
    ctx.append(NotificationUtils.CREATOR, creator);

    //. Receiver
    Set<String> receiver = new HashSet<String>();

    // Task creator
    receiver.add(task.getCreatedBy());

    // Assignee and Coworker
    if (task.getAssignee() != null && !task.getAssignee().isEmpty()) {
      receiver.add(task.getAssignee());
    }
    Set<String> coworker = taskService.getCoworker(task.getId());
    if (coworker != null && coworker.size() > 0) {
      receiver.addAll(coworker);
    }

    // All user who commented on this task
    ListAccess<Comment> comments = taskService.getComments(task.getId());
    if (comments != null && ListUtil.getSize(comments) > 0) {
      for (Comment c : ListUtil.load(comments, 0, -1)) {
        receiver.add(c.getAuthor());
      }
    }

    // Remove the user who create this comment, he should not receive the notification
    receiver.remove(creator);

    // Get all mentioned
    Set<String> mentioned = getMentioned(comment.getComment());
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

  private Set<String> getMentioned(String text) {
    Set<String> set = new HashSet<String>();

    if (text == null || text.isEmpty()) {
      return set;
    }

    Matcher m = MENTION_REGEX.matcher(text);
    while (m.find()) {
      set.add(m.group(3));
    }

    return set;
  }
}

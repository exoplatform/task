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

import org.exoplatform.commons.api.notification.model.ArgumentLiteral;
import org.exoplatform.task.domain.Comment;
import org.exoplatform.task.domain.Task;
import org.exoplatform.task.dto.CommentDto;
import org.exoplatform.task.dto.TaskDto;

import java.util.Set;

public class NotificationUtils {
  public final static ArgumentLiteral<TaskDto> TASK = new ArgumentLiteral<TaskDto>(TaskDto.class, "task");
  public final static ArgumentLiteral<CommentDto> COMMENT = new ArgumentLiteral<CommentDto>(CommentDto.class, "comment");
  public final static ArgumentLiteral<String> CREATOR = new ArgumentLiteral<String>(String.class, "creator");
  public static final ArgumentLiteral<Set> COWORKER = new ArgumentLiteral<Set>(Set.class, "coworker");
  public static final ArgumentLiteral<Set> WATCHER = new ArgumentLiteral<Set>(Set.class, "watcher");
  public static final ArgumentLiteral<Set> MENTIONED = new ArgumentLiteral<Set>(Set.class, "mentioned");
  public static final ArgumentLiteral<Set> RECEIVERS = new ArgumentLiteral<Set>(Set.class, "receivers");
  public static final ArgumentLiteral<String> ACTION_NAME = new ArgumentLiteral<>(String.class, "actionName");
  public static final String TASKS = "tasks";
  public final static String TASK_TITLE = "taskName";
  public static final String TASK_CREATOR = "taskCreator";
  public final static String TASK_DESCRIPTION = "taskDescription";
  public final static String DUE_DATE = "dueDate";
  public final static String PROJECT_NAME = "projectName";
  public final static String COUNT = "count";
  public final static String COMMENT_TEXT = "commentText";
  //workaround because WebNotificationStorage#get(filter) has not been implemented properly
  //in Task we use this to group task notifications, used in WebNotificationStorage#getUnreadNotification
  public final static String ACTIVITY_ID = "activityId";
  public static final String TASK_URL = "taskUrl";
  public static final String PROJECT_URL = "projectUrl";
  public static final String TASK_ASSIGNEE = "taskAssignee";
  public static final String ADDED_COWORKER = "addedCoworker";
  public static final String TASK_COWORKERS = "taskCoworkers";
  public static final String TASK_WATCHERS="taskWatchers";
  public static final String MENTIONED_USERS = "mentionedUsers";

}

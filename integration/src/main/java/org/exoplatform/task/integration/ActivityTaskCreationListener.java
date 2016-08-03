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
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package org.exoplatform.task.integration;

import org.apache.commons.lang.StringEscapeUtils;
import org.exoplatform.social.core.activity.ActivityLifeCycleEvent;
import org.exoplatform.social.core.activity.ActivityListenerPlugin;
import org.exoplatform.social.core.activity.model.ActivityStream;
import org.exoplatform.social.core.activity.model.ExoSocialActivity;
import org.exoplatform.social.core.identity.model.Identity;
import org.exoplatform.social.core.manager.ActivityManager;
import org.exoplatform.social.core.manager.IdentityManager;
import org.exoplatform.social.core.service.LinkProvider;
import org.exoplatform.social.core.space.model.Space;
import org.exoplatform.social.core.space.spi.SpaceService;
import org.exoplatform.task.domain.Project;
import org.exoplatform.task.domain.Task;
import org.exoplatform.task.service.ParserContext;
import org.exoplatform.task.service.ProjectService;
import org.exoplatform.task.service.StatusService;
import org.exoplatform.task.service.TaskParser;
import org.exoplatform.task.service.TaskService;
import org.exoplatform.task.service.UserService;
import org.exoplatform.task.util.ProjectUtil;
import org.exoplatform.task.util.StringUtil;

import java.util.List;

public class ActivityTaskCreationListener extends ActivityListenerPlugin {

  private TaskService taskService;
  private ProjectService projectService;
  private StatusService statusService;
  
  private TaskParser parser;

  private final IdentityManager identityManager;
  private final ActivityManager activityManager;
  private final SpaceService spaceService;
  private UserService userService;
  
  public static final String PREFIX = "++";

  public ActivityTaskCreationListener(TaskService taskServ, ProjectService projectService, StatusService statusService, TaskParser parser, IdentityManager identityManager, ActivityManager activityManager, SpaceService spaceService, UserService userService) {
    this.taskService = taskServ;
    this.projectService = projectService;
    this.statusService = statusService;
    this.parser = parser;
    this.identityManager = identityManager;
    this.activityManager = activityManager;
    this.spaceService = spaceService;
    this.userService = userService;
  }

  @Override
  public void saveActivity(ActivityLifeCycleEvent event) {
    createTask(event, false);
  }

  @Override
  public void updateActivity(ActivityLifeCycleEvent event) {
  }

  @Override
  public void saveComment(ActivityLifeCycleEvent event) {
    createTask(event, true);
  }

  @Override
  public void likeActivity(ActivityLifeCycleEvent event) {
  }

  private void createTask(ActivityLifeCycleEvent event, boolean isComment) {
    ExoSocialActivity activity = event.getActivity();
    ExoSocialActivity rootActivity = isComment ? activityManager.getActivity(activity.getParentId()) : activity;
    Identity identity = identityManager.getIdentity(activity.getPosterId(), false);    
    ParserContext context = new ParserContext(userService.getUserTimezone(identity.getRemoteId()));
    String comment = activity.getTitle();
    //
    if (comment != null && !comment.isEmpty()) {
      int idx = comment.indexOf(PREFIX);
      //
      if (idx >=0 && idx + 2 < comment.length() - 1) {
        comment = ActivityTaskProcessor.decode(comment);
        comment = StringEscapeUtils.unescapeHtml(comment);
        String text = comment.substring(idx + 2);
        text = text.replaceFirst("<br(\\s*\\/?)>", "\n");

        String title, description;
        int index = text.indexOf("\n");
        if (index > 1) {
          title = text.substring(0, index);
          description = text.substring(index).trim();
        } else {
          title = text.trim();
          description = "";
        }
        Task task = parser.parse(title, context);
        //we need to remove malicious code here in case user inject request using curl TA-387
        task.setDescription(StringUtil.encodeInjectedHtmlTag(description));
        task.setContext(LinkProvider.getSingleActivityUrl(activity.getId()));

        
        task.setCreatedBy(identity.getRemoteId());

        task.setActivityId(activity.getId());

        // If task is created in space, it will belong to the top project
        if(rootActivity.getActivityStream().getType() == ActivityStream.Type.SPACE) {
          String spaceName = rootActivity.getActivityStream().getPrettyId();
          Space space = spaceService.getSpaceByPrettyName(spaceName);
          List<Project> projects = ProjectUtil.getProjectTree(space.getGroupId(), projectService);
          if (projects != null && projects.size() > 0) {
            task.setStatus(statusService.getDefaultStatus(projects.get(0).getId()));
          }
        }
        
        taskService.createTask(task);

        //TODO: This is workaround: update to rebuild cache of this activity
        activity.setTitle(comment);
        activityManager.updateActivity(activity);
      }
    }
  }
}

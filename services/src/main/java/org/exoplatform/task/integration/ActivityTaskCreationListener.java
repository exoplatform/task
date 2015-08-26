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

import org.exoplatform.social.core.activity.ActivityLifeCycleEvent;
import org.exoplatform.social.core.activity.ActivityListenerPlugin;
import org.exoplatform.social.core.activity.model.ActivityStream;
import org.exoplatform.social.core.activity.model.ExoSocialActivity;
import org.exoplatform.social.core.identity.model.Identity;
import org.exoplatform.social.core.manager.ActivityManager;
import org.exoplatform.social.core.manager.IdentityManager;
import org.exoplatform.social.core.service.LinkProvider;
import org.exoplatform.task.domain.Task;
import org.exoplatform.task.service.DAOHandler;
import org.exoplatform.task.service.TaskParser;

public class ActivityTaskCreationListener extends ActivityListenerPlugin {

  private DAOHandler DAOHandler;
  
  private TaskParser parser;

  private final IdentityManager identityManager;
  private final ActivityManager activityManager;
  
  public static final String PREFIX = "++";
  
  public ActivityTaskCreationListener(DAOHandler DAOHandler, TaskParser parser, IdentityManager identityManager, ActivityManager activityManager) {
    this.DAOHandler = DAOHandler;
    this.parser = parser;
    this.identityManager = identityManager;
    this.activityManager = activityManager;
  }

  @Override
  public void saveActivity(ActivityLifeCycleEvent event) {
    createTask(event);
  }

  @Override
  public void updateActivity(ActivityLifeCycleEvent event) {
  }

  @Override
  public void saveComment(ActivityLifeCycleEvent event) {
    createTask(event);
  }

  @Override
  public void likeActivity(ActivityLifeCycleEvent event) {
  }

  private void createTask(ActivityLifeCycleEvent event) {
    ExoSocialActivity activity = event.getActivity();
    String comment = activity.getTitle();
    //
    if (comment != null && !comment.isEmpty()) {
      int idx = comment.indexOf(PREFIX);
      //
      if (idx >=0 && idx + 2 < comment.length() - 1) {
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
        Task task = parser.parse(title);
        task.setDescription(description);
        task.setContext(LinkProvider.getSingleActivityUrl(activity.getId()));

        Identity identity = identityManager.getIdentity(activity.getPosterId(), false);
        task.setCreatedBy(identity.getRemoteId());

        task.setActivityId(activity.getId());

        //TODO: process if this activity is in space
        //Now, it's impossible to find project of space, be cause space (group) can have many project
        // which project will contains this task?
        //String spaceGroup = getSpaceGroup(activity);

        DAOHandler.getTaskHandler().create(task);

        //TODO: This is workaround: update to rebuild cache of this activity
        activityManager.updateActivity(activity);
      }
    }
  }

  private String getSpaceGroup(ExoSocialActivity activity) {
    ExoSocialActivity parent = activity;
    if (activity.isComment()) {
      parent = activityManager.getActivity(activity.getParentId());
    }
    ActivityStream stream = parent.getActivityStream();
    if (stream.getType() == ActivityStream.Type.SPACE) {
      return "/spaces/" + stream.getPrettyId();
    }
    return null;
  }
}

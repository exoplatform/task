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

import org.apache.shindig.social.opensocial.model.Activity;
import org.exoplatform.social.core.activity.ActivityLifeCycleEvent;
import org.exoplatform.social.core.activity.ActivityListenerPlugin;
import org.exoplatform.social.core.service.LinkProvider;
import org.exoplatform.task.domain.Task;
import org.exoplatform.task.service.TaskParser;
import org.exoplatform.task.service.DAOHandler;

public class ActivityTaskCreationListener extends ActivityListenerPlugin {

  private DAOHandler DAOHandler;
  
  private TaskParser parser;
  
  public static final String PREFIX = "++";
  
  public ActivityTaskCreationListener(DAOHandler DAOHandler, TaskParser parser) {
    this.DAOHandler = DAOHandler;
    this.parser = parser;
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
    Activity activity = event.getActivity();
    String comment = activity.getTitle();
    //
    if (comment != null && !comment.isEmpty()) {
      int idx = comment.indexOf(PREFIX);
      //
      if (idx >=0 && idx + 2 < comment.length() - 1) {
        Task task = parser.parse(comment.substring(idx + 2, comment.length() - 4));
        task.setDescription(LinkProvider.getSingleActivityUrl(activity.getId()));
        DAOHandler.getTaskHandler().create(task);
      }
    }
  }
}

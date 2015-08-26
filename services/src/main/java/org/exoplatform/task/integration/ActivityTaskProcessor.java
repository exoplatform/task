/*
 * Copyright (C) 2003-2010 eXo Platform SAS.
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
 */
  
package org.exoplatform.task.integration;

import org.exoplatform.commons.persistence.impl.EntityManagerService;
import org.exoplatform.container.ExoContainerContext;
import org.exoplatform.container.component.RequestLifeCycle;
import org.exoplatform.container.xml.InitParams;
import org.exoplatform.portal.mop.SiteKey;
import org.exoplatform.social.core.BaseActivityProcessorPlugin;
import org.exoplatform.social.core.activity.model.ExoSocialActivity;
import org.exoplatform.task.domain.Task;
import org.exoplatform.task.service.DAOHandler;
import org.exoplatform.task.utils.TaskUtil;
import org.exoplatform.web.WebAppController;
import org.exoplatform.web.controller.router.Router;

public class ActivityTaskProcessor extends BaseActivityProcessorPlugin {

  private final DAOHandler daoHandler;
  private final EntityManagerService entityManagerService;
  private final WebAppController webAppController;
  
  public ActivityTaskProcessor(InitParams params, WebAppController controller, DAOHandler daoHandler, EntityManagerService entityManagerService) {
    super(params);
    this.entityManagerService = entityManagerService;
    this.daoHandler = daoHandler;
    this.webAppController = controller;
  }

  public void processActivity(ExoSocialActivity activity) {
    if (activity != null) {
      activity.setTitle(substituteTask(activity.getTitle(), activity));
    }
  }

  private String substituteTask(String message, ExoSocialActivity activity) {
    if (message == null) {
      return null;
    }

    int idx = message.indexOf(ActivityTaskCreationListener.PREFIX);
    if (idx >=0) {
      try {
        RequestLifeCycle.begin(entityManagerService);
        Task task = daoHandler.getTaskHandler().findTaskByActivityId(activity.getId());
        if (task != null) {
          return substituteTask(task, message, webAppController.getRouter());
        }
      } finally {
        RequestLifeCycle.end();
      }
    }
    return message;
  }

  static String substituteTask(Task task, final String title, Router router) {
    if (task == null || title == null) {
      return title;
    }

    int idx = title.indexOf(ActivityTaskCreationListener.PREFIX);
    if (idx == -1) {
      return title;
    }
    int breakIdx = title.indexOf("<br", idx);

    StringBuilder builder = new StringBuilder(title);
    String taskURL = TaskUtil.buildTaskURL(task, SiteKey.portal("intranet"), ExoContainerContext.getCurrentContainer(), router);
    String url = " <a href='"+taskURL+"'>";
    builder.insert(idx, url);
    if (breakIdx > 0) {
      breakIdx = breakIdx + url.length();
      builder.insert(breakIdx, "</a>");
    } else {
      builder.append("</a>");
    }

    return builder.toString();
  }
}

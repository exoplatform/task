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
import org.exoplatform.portal.application.PortalRequestContext;
import org.exoplatform.portal.mop.SiteKey;
import org.exoplatform.social.core.BaseActivityProcessorPlugin;
import org.exoplatform.social.core.activity.model.ExoSocialActivity;
import org.exoplatform.task.dto.TaskDto;
import org.exoplatform.task.service.TaskService;
import org.exoplatform.task.util.TaskUtil;
import org.exoplatform.web.WebAppController;
import org.exoplatform.web.application.RequestContext;

public class ActivityTaskProcessor extends BaseActivityProcessorPlugin {

  private final TaskService taskService;
  private final EntityManagerService entityManagerService;
  private final WebAppController webAppController;
  
  public ActivityTaskProcessor(InitParams params, WebAppController controller, TaskService taskServ, EntityManagerService entityManagerService) {
    super(params);
    this.entityManagerService = entityManagerService;
    this.taskService = taskServ;
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
        TaskDto task = taskService.findTaskByActivityId(activity.getId());
        if (task != null) {
          RequestContext request = RequestContext.getCurrentInstance();
          String portalName = "intranet";
          if (request != null) {
            PortalRequestContext pContext = (PortalRequestContext) RequestContext.getCurrentInstance().getParentAppRequestContext();
            portalName = pContext.getPortalOwner();
          }
          String taskURL = TaskUtil.buildTaskURL(task, SiteKey.portal(portalName), ExoContainerContext.getCurrentContainer(), webAppController.getRouter());
          return substituteTask(taskURL, message);
        } else {
          //encode to disable MentionsProcessor
          return encode(message);
        }
      } finally {
        RequestLifeCycle.end();
      }
    }
    return message;
  }

  public static String encode(String message) {
    if (message != null && !message.isEmpty()) {
      message = message.replaceAll("@", "{@}");
    }
    return message;
  }
  
  public static String decode(String message) {
    if (message != null && !message.isEmpty()) {
      message = message.replaceAll("\\{@\\}", "@");
    }
    return message;
  }

  String substituteTask(String taskURL, final String html) {
    int idx = html.indexOf(ActivityTaskCreationListener.PREFIX);
    if (idx == -1) {
      return html;
    }

    StringBuilder builder = new StringBuilder(html);
    String url = "<a href=\""+taskURL+"\">";
    builder.insert(idx, url);

    int breakIdx = html.indexOf("<", idx);
    if (breakIdx > 0) {
      breakIdx = breakIdx + url.length();
      builder.insert(breakIdx, "</a>");
    } else {
      builder.append("</a>");
    }

    return builder.toString();
  }
}

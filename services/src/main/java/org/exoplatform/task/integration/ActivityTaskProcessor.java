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

import org.exoplatform.container.xml.InitParams;
import org.exoplatform.social.core.BaseActivityProcessorPlugin;
import org.exoplatform.social.core.activity.model.ExoSocialActivity;
import org.exoplatform.web.WebAppController;

public class ActivityTaskProcessor extends BaseActivityProcessorPlugin {
  
  private final String taskUrl = " <a href='/portal/intranet/tasks'>";
  
  public ActivityTaskProcessor(InitParams params, WebAppController controller) {
    super(params);    
  }

  public void processActivity(ExoSocialActivity activity) {
    if (activity != null) {
      activity.setTitle(substituteTask(activity.getTitle()));
    }
  }

  private String substituteTask(String message) {
    if (message == null) {
      return null;
    }
    
    StringBuilder builder = new StringBuilder(message);
    int idx = message.indexOf(ActivityTaskCreationListener.PREFIX);
    if (idx >=0) {
      builder.insert(idx, taskUrl).append("</a>");
    }
    return builder.toString();
  }
}

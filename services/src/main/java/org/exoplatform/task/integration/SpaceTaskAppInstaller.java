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

import java.util.List;

import org.exoplatform.container.ExoContainerContext;
import org.exoplatform.container.xml.InitParams;
import org.exoplatform.container.xml.ValueParam;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.social.core.space.SpaceApplicationConfigPlugin;
import org.exoplatform.social.core.space.SpaceApplicationConfigPlugin.SpaceApplication;
import org.exoplatform.social.core.space.spi.SpaceService;
import org.picocontainer.Startable;

public class SpaceTaskAppInstaller implements Startable {

  private String prevApp = "SpaceActivityStreamPortlet";
  
  private  String taskPortletName = "TaskManagementApplication";
  
  private  String taskAppName = "task-management";
  
  private String taskUri = "tasks";
  
  private static final Log log = ExoLogger.getExoLogger(SpaceTaskAppInstaller.class);

  public SpaceTaskAppInstaller(InitParams params) {
    if (params != null) {
      config(params, "prevApp");
      config(params, "taskPortletName");
      config(params, "taskAppName");
      config(params, "taskUri");
    }
  }

  @Override
  public void start() {
    SpaceService spaceService = (SpaceService) ExoContainerContext.getCurrentContainer().getComponentInstanceOfType(SpaceService.class);
    SpaceApplicationConfigPlugin plugin = spaceService.getSpaceApplicationConfigPlugin();
    List<SpaceApplication> apps =  plugin.getSpaceApplicationList();
    //
    if (!isTaskExists(apps)) {
      int idx = getTaskIndex(apps);
      
      SpaceApplication taskApp = new SpaceApplication();
      taskApp.setPortletApp(taskAppName);
      taskApp.setPortletName(taskPortletName);
      taskApp.setAppTitle("abcd");
      taskApp.setUri(taskUri);
      //
      apps.add(idx, taskApp);
      plugin.setSpaceApplicationList(apps);
    }
  }
  
  private void config(InitParams params, String name) {
    ValueParam p = params.getValueParam(name);
    if (p != null) {
      try {
        this.getClass().getDeclaredField(name).set(this, p.getValue());
      } catch (Exception e) {
        log.error(e);
      }      
    }
  }

  private int getTaskIndex(List<SpaceApplication> apps) {  
    int idx = 0;
    for (SpaceApplication app : apps) {
      idx++;
      if (app.getPortletName().equals(prevApp)) {
        return idx;
      }
    }
    return 0;
  }

  private boolean isTaskExists(List<SpaceApplication> apps) {
    for (SpaceApplication app : apps) {
      if (taskAppName.equals(app.getPortletApp()) && taskPortletName.equals(app.getPortletName())) {
        return true;
      }
    }
    return false;
  }

  @Override
  public void stop() {    
  }

}

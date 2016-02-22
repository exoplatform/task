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

package org.exoplatform.task.management.service;

import org.exoplatform.commons.api.settings.SettingService;
import org.exoplatform.commons.api.settings.SettingValue;
import org.exoplatform.commons.api.settings.data.Context;
import org.exoplatform.commons.api.settings.data.Scope;
import org.exoplatform.task.management.model.ViewType;

import javax.inject.Inject;

public class ViewStateService {
  public static final Scope TASK_APP_SCOPE = Scope.APPLICATION.id("taskManagement");

  private final SettingService settingService;

  @Inject
  public ViewStateService(SettingService settingService) {
    this.settingService = settingService;
  }

  public ViewType getViewType(String username, long projectId) {
    if (projectId > 0) {
      SettingValue<String> value = (SettingValue<String>) settingService.get(Context.USER.id(username), TASK_APP_SCOPE, buildViewTypeKey(projectId));
      if (value != null) {
        return ViewType.getViewType(value.getValue());
      }
    }
    return ViewType.LIST;
  }

  public void saveViewType(String username, long projectId, ViewType viewType) {
    if (projectId > 0) {
      //Don't need to save if projectId <= 0, in that case the viewType is always LIST
      SettingValue<String> value = new SettingValue<>(viewType.name());
      settingService.set(Context.USER.id(username), TASK_APP_SCOPE, buildViewTypeKey(projectId), value);
    }
  }

  private String buildViewTypeKey(long projectId) {
    return new StringBuilder("project.")
            .append(projectId)
            .append(".view_type")
            .toString();
  }
}

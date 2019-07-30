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

import juzu.View;
import org.exoplatform.commons.api.settings.SettingService;
import org.exoplatform.commons.api.settings.SettingValue;
import org.exoplatform.commons.api.settings.data.Context;
import org.exoplatform.commons.api.settings.data.Scope;
import org.exoplatform.task.domain.Priority;
import org.exoplatform.task.management.model.ViewState;
import org.exoplatform.task.management.model.ViewType;
import org.exoplatform.task.util.TaskUtil;

import javax.inject.Inject;

public class ViewStateService {
  public static final Scope TASK_APP_SCOPE = Scope.APPLICATION.id("taskManagement");

  private final SettingService settingService;

  @Inject
  public ViewStateService(SettingService settingService) {
    this.settingService = settingService;
  }

  public ViewState getViewState(String listId) {
    ViewState viewState = new ViewState(listId);
    SettingValue<String> value = (SettingValue<String>) settingService.get(Context.USER, TASK_APP_SCOPE, listId + ".orderBy");
    if (value != null) {
      viewState.setOrderBy(value.getValue());
    }

    value = (SettingValue<String>) settingService.get(Context.USER, TASK_APP_SCOPE, listId + ".groupBy");
    if (value != null) {
      viewState.setGroupBy(value.getValue());
    }

    value = (SettingValue<String>) settingService.get(Context.USER, TASK_APP_SCOPE, listId + ".viewType");
    if (value != null) {
      viewState.setViewType(ViewType.valueOf(value.getValue()));
    } else {
      viewState.setViewType(ViewType.LIST);
    }

    return viewState;
  }

  public void saveViewState(ViewState viewState) {
    ViewState oldViewState = getViewState(viewState.getId());
    if (!viewState.isEmpty() && !viewState.equals(oldViewState)) {
      String prefixId = viewState.getId();
      if (viewState.getOrderBy() != null) {
        settingService.set(Context.USER, TASK_APP_SCOPE, prefixId + ".orderBy", SettingValue.create(viewState.getOrderBy()));
      } else {
        settingService.remove(Context.USER, TASK_APP_SCOPE, prefixId + ".orderBy");
      }

      if (viewState.getGroupBy() != null) {
        settingService.set(Context.USER, TASK_APP_SCOPE, prefixId + ".groupBy", SettingValue.create(viewState.getGroupBy()));
      } else {
        settingService.remove(Context.USER, TASK_APP_SCOPE, prefixId + ".groupBy");
      }

      settingService.set(Context.USER, TASK_APP_SCOPE, prefixId + ".viewType", SettingValue.create(viewState.getViewType().toString()));

    }
  }

  public ViewState.Filter getFilter(String listId) {
    ViewState.Filter filter = new ViewState.Filter(listId);

    SettingValue<String> value = (SettingValue<String>) settingService.get(Context.USER, TASK_APP_SCOPE, listId + ".filter.enabled");
    if (value != null) {
      filter.setEnabled(Boolean.valueOf(value.getValue()));
    }

    value = (SettingValue<String>) settingService.get(Context.USER, TASK_APP_SCOPE, listId + ".filter.keyword");
    if (value != null) {
      filter.setKeyword(value.getValue());
    }

    value = (SettingValue<String>) settingService.get(Context.USER, TASK_APP_SCOPE, listId + ".filter.status");
    if (value != null) {
      filter.setStatus(Long.valueOf(value.getValue()));
    }

    value = (SettingValue<String>) settingService.get(Context.USER, TASK_APP_SCOPE, listId + ".filter.showCompleted");
    if (value != null) {
      filter.setShowCompleted(Boolean.valueOf(value.getValue()));
    }

    value = (SettingValue<String>) settingService.get(Context.USER, TASK_APP_SCOPE, listId + ".filter.due");
    if (value != null) {
      filter.setDue(TaskUtil.DUE.valueOf(value.getValue()));
    }

    value = (SettingValue<String>) settingService.get(Context.USER, TASK_APP_SCOPE, listId + ".filter.priority");
    if (value != null) {
      filter.setPriority(Priority.valueOf(value.getValue()));
    }

    value = (SettingValue<String>) settingService.get(Context.USER, TASK_APP_SCOPE, listId + ".filter.labels");
    if (value != null) {
      filter.setLabels(value.getValue());
    }

    value = (SettingValue<String>) settingService.get(Context.USER, TASK_APP_SCOPE, listId + ".filter.assignees");
    if (value != null) {
      filter.setAssignees(value.getValue());
    }

    return filter;
  }

  public void saveFilter(ViewState.Filter filter) {
    ViewState.Filter originalFilter = getFilter(filter.getId());
    if (!filter.equals(originalFilter)) {
      String prefixId = filter.getId();
      settingService.set(Context.USER, TASK_APP_SCOPE, prefixId + ".filter.enabled", SettingValue.create(String.valueOf(filter.isEnabled())));
      settingService.set(Context.USER, TASK_APP_SCOPE, prefixId + ".filter.keyword", SettingValue.create(filter.getKeyword()));

      if (filter.getStatus() != null) {
        settingService.set(Context.USER, TASK_APP_SCOPE, prefixId + ".filter.status", SettingValue.create(String.valueOf(filter.getStatus())));
      } else {
        settingService.remove(Context.USER, TASK_APP_SCOPE, prefixId + ".filter.status");
      }

      if (filter.getDue() != null) {
        settingService.set(Context.USER, TASK_APP_SCOPE, prefixId + ".filter.due", SettingValue.create(filter.getDue().toString()));
      } else {
        settingService.remove(Context.USER, TASK_APP_SCOPE, prefixId + ".filter.due");
      }

      if (filter.getPriority() != null) {
        settingService.set(Context.USER, TASK_APP_SCOPE, prefixId + ".filter.priority", SettingValue.create(filter.getPriority().toString()));
      } else {
        settingService.remove(Context.USER, TASK_APP_SCOPE, prefixId + ".filter.priority");
      }

      settingService.set(Context.USER, TASK_APP_SCOPE, prefixId + ".filter.labels", SettingValue.create(filter.getLabelsInString()));
      settingService.set(Context.USER, TASK_APP_SCOPE, prefixId + ".filter.assignees", SettingValue.create(filter.getAssigneesInString()));
      settingService.set(Context.USER, TASK_APP_SCOPE, prefixId + ".filter.showCompleted", SettingValue.create(String.valueOf(filter.isShowCompleted())));
    }
  }
}

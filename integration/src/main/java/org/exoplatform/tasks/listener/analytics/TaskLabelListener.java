/**
 * This file is part of the Meeds project (https://meeds.io/).
 * 
 * Copyright (C) 2022 Meeds Association contact@meeds.io
 * 
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */
package org.exoplatform.tasks.listener.analytics;

import org.exoplatform.analytics.model.StatisticData;
import org.exoplatform.analytics.utils.AnalyticsUtils;
import org.exoplatform.commons.api.persistence.ExoTransactional;
import org.exoplatform.container.ExoContainerContext;
import org.exoplatform.container.PortalContainer;
import org.exoplatform.container.component.RequestLifeCycle;
import org.exoplatform.container.xml.InitParams;
import org.exoplatform.services.listener.Asynchronous;
import org.exoplatform.services.listener.Event;
import org.exoplatform.services.listener.Listener;
import org.exoplatform.task.domain.LabelTaskMapping;

@Asynchronous
public class TaskLabelListener extends Listener<String, LabelTaskMapping> {

  private PortalContainer container;

  private final String    operation;

  public TaskLabelListener(InitParams params) {
    this.container = PortalContainer.getInstance();
    this.operation = params.getValueParam("operation").getValue();
  }

  @Override
  @ExoTransactional
  public void onEvent(Event<String, LabelTaskMapping> event) {
    RequestLifeCycle.begin(container);
    ExoContainerContext.setCurrentContainer(container);
    try {
      LabelTaskMapping labelTaskMapping = event.getData();
      String userName = event.getSource();
      createLabelStatistic(labelTaskMapping, userName);
    } finally {
      RequestLifeCycle.end();
    }
  }

  private void createLabelStatistic(LabelTaskMapping labelTaskMapping, String userName) {
    StatisticData statisticData = new StatisticData();
    statisticData.setModule("tasks");
    statisticData.setSubModule("task");
    statisticData.setUserId(AnalyticsUtils.getUserIdentityId(userName));
    statisticData.setOperation(operation);
    if (labelTaskMapping.getTask() != null) {
      statisticData.addParameter("taskId", labelTaskMapping.getTask().getId());
    }
    if (labelTaskMapping.getLabel() != null) {
      statisticData.addParameter("labelId", labelTaskMapping.getLabel().getId());
      if (labelTaskMapping.getLabel().getProject() != null) {
        statisticData.addParameter("projectId", labelTaskMapping.getLabel().getProject().getId());
      }
    }
    AnalyticsUtils.addStatisticData(statisticData);
  }
}

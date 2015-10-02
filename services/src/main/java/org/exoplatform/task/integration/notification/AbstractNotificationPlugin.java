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
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package org.exoplatform.task.integration.notification;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.Set;

import org.exoplatform.commons.api.notification.NotificationContext;
import org.exoplatform.commons.api.notification.model.NotificationInfo;
import org.exoplatform.commons.api.notification.plugin.BaseNotificationPlugin;
import org.exoplatform.commons.utils.CommonsUtils;
import org.exoplatform.container.ExoContainer;
import org.exoplatform.container.PortalContainer;
import org.exoplatform.container.RootContainer;
import org.exoplatform.container.component.RequestLifeCycle;
import org.exoplatform.container.xml.InitParams;
import org.exoplatform.portal.mop.SiteKey;
import org.exoplatform.task.domain.Project;
import org.exoplatform.task.domain.Task;
import org.exoplatform.task.util.ProjectUtil;
import org.exoplatform.task.util.TaskUtil;
import org.exoplatform.web.WebAppController;

public abstract class AbstractNotificationPlugin extends BaseNotificationPlugin {
  
  public AbstractNotificationPlugin(InitParams initParams) {
    super(initParams);
  }

  @Override
  public NotificationInfo makeNotification(NotificationContext ctx) {
    Task task = ctx.value(NotificationUtils.TASK);
    String creator = ctx.value(NotificationUtils.CREATOR);
    
    //
    String projectName = "";
    String projectId = "";
    Project project = null;
    if (task.getStatus() != null) {
      project = task.getStatus().getProject();
      projectName = project.getName();
      projectId = String.valueOf(project.getId());
    }
    StringBuilder activityId = new StringBuilder(projectId);
    activityId.append("-").append(creator);
    
    ExoContainer container = getContainer();
    WebAppController controller = container.getComponentInstanceOfType(WebAppController.class);
    RequestLifeCycle.begin(container);
    String taskUrl = buildTaskUrl(task, container, controller);
    String projectUrl = buildProjectUrl(project, container, controller);
    Set<String> receivers = getReceiver(task, ctx);    
    RequestLifeCycle.end();
    
    return NotificationInfo.instance()
                               .to(new LinkedList<String>(receivers))
                               .with(NotificationUtils.TASK_TITLE, task.getTitle())
                               .with(NotificationUtils.TASK_DESCRIPTION, task.getDescription())
                               .with(NotificationUtils.CREATOR.getKey(), creator)
                               .with(NotificationUtils.PROJECT_NAME, projectName)
                               .with(NotificationUtils.ACTIVITY_ID, activityId.toString())
                               .with(NotificationUtils.TASK_URL, taskUrl)
                               .with(NotificationUtils.PROJECT_URL, projectUrl)
                               .key(getKey()).end();
  }

  private ExoContainer getContainer() {
    String containerName = PortalContainer.getCurrentPortalContainerName();
    ExoContainer container = RootContainer.getInstance().getPortalContainer(containerName);
    return container;
  }

  private String buildProjectUrl(Project project, ExoContainer container, WebAppController controller) {
    if (project == null) return "#";    
    return CommonsUtils.getCurrentDomain() + ProjectUtil.buildProjectURL(project, SiteKey.portal("intranet"), container, controller.getRouter());
  }

  protected Set<String> getReceiver(Task task, NotificationContext ctx) {
    Set<String> receivers = new HashSet<String>();
    if (task.getAssignee() != null && !task.getAssignee().isEmpty()) {
      receivers.add(task.getAssignee());
    }
    if (task.getCoworker() != null && task.getCoworker().size() > 0) {
      receivers.addAll(task.getCoworker());
    }
    return receivers;
  }
  
  private String buildTaskUrl(Task t, ExoContainer container, WebAppController controller) {
    return CommonsUtils.getCurrentDomain() + TaskUtil.buildTaskURL(t, SiteKey.portal("intranet"), container, controller.getRouter());
  }
}

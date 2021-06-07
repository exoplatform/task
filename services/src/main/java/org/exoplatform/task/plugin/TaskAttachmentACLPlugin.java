/*
 * Copyright (C) 2021 eXo Platform SAS.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Affero General Public License
 * as published by the Free Software Foundation; either version 3
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, see<http://www.gnu.org/licenses/>.
 */
package org.exoplatform.task.plugin;

import org.apache.commons.lang.StringUtils;
import org.exoplatform.services.attachments.plugin.AttachmentACLPlugin;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.services.security.Identity;
import org.exoplatform.services.security.IdentityRegistry;
import org.exoplatform.social.core.manager.IdentityManager;
import org.exoplatform.task.dto.ProjectDto;
import org.exoplatform.task.dto.TaskDto;
import org.exoplatform.task.exception.EntityNotFoundException;
import org.exoplatform.task.service.TaskService;

public class TaskAttachmentACLPlugin extends AttachmentACLPlugin {

  private static final Log    LOG                  = ExoLogger.getLogger(TaskAttachmentACLPlugin.class.getName());

  private static final String TASK_ATTACHMENT_TYPE = "task";

  private TaskService         taskService;

  private IdentityManager     identityManager;

  private IdentityRegistry    identityRegistry;

  public TaskAttachmentACLPlugin(TaskService taskService, IdentityManager identityManager, IdentityRegistry identityRegistry) {
    this.taskService = taskService;
    this.identityManager = identityManager;
    this.identityRegistry = identityRegistry;
  }

  @Override
  public String getEntityType() {
    return TASK_ATTACHMENT_TYPE;
  }

  @Override
  public boolean canView(long userIdentityId, String entityType, String entityId) {
    if (!entityType.equals(TASK_ATTACHMENT_TYPE)) {
      throw new IllegalArgumentException("Entity type must be" + TASK_ATTACHMENT_TYPE);
    }

    if (StringUtils.isEmpty(entityId)) {
      throw new IllegalArgumentException("Entity id must not be Empty");
    }

    if (userIdentityId < 0) {
      throw new IllegalArgumentException("User identity must not be null");
    }
    org.exoplatform.social.core.identity.model.Identity userIdentity = identityManager.getIdentity(String.valueOf(userIdentityId));
    String username = userIdentity.getRemoteId();
    Identity identity = identityRegistry.getIdentity(username);
    boolean canView = false;
    try {
      TaskDto task = taskService.getTask(Long.parseLong(entityId));
      if (task.getStatus() != null) {
        ProjectDto project = task.getStatus().getProject();
        canView = project.canView(identity);
      }
    } catch (EntityNotFoundException e) {
      LOG.error("Can not find task with ID: " + entityId);
    }

    return canView;
  }

  @Override
  public boolean canDelete(long userIdentityId, String entityType, String entityId) {
    if (!entityType.equals(TASK_ATTACHMENT_TYPE)) {
      throw new IllegalArgumentException("Entity type must be" + TASK_ATTACHMENT_TYPE);
    }

    if (StringUtils.isEmpty(entityId)) {
      throw new IllegalArgumentException("Entity id must not be Empty");
    }

    if (userIdentityId <= 0) {
      throw new IllegalArgumentException("User identity must be positive");
    }
    org.exoplatform.social.core.identity.model.Identity userIdentity = identityManager.getIdentity(String.valueOf(userIdentityId));
    String username = userIdentity.getRemoteId();
    Identity identity = identityRegistry.getIdentity(username);

    boolean canDelete = false;
    try {
      TaskDto task = taskService.getTask(Long.parseLong(entityId));
      if (task.getStatus() != null) {
        ProjectDto project = task.getStatus().getProject();
        canDelete = project.canView(identity);
      }
    } catch (EntityNotFoundException e) {
      LOG.error("Can not find task with ID: " + entityId);
    }

    return canDelete;
  }

  private org.exoplatform.social.core.identity.model.Identity getIdentityById(String identityId) {
    return identityManager.getIdentity(identityId);
  }
}

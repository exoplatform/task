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
import org.exoplatform.social.core.manager.IdentityManager;
import org.exoplatform.task.dto.ProjectDto;
import org.exoplatform.task.dto.TaskDto;
import org.exoplatform.task.exception.EntityNotFoundException;
import org.exoplatform.task.service.TaskService;

public class TaskAttachmentACLPlugin extends AttachmentACLPlugin {

  private static final Log    LOG        = ExoLogger.getLogger(TaskAttachmentACLPlugin.class.getName());

  private static final String entityType = "task";

  private TaskService         taskService;

  private IdentityManager     identityManager;

  public TaskAttachmentACLPlugin(TaskService taskService, IdentityManager identityManager) {
    this.taskService = taskService;
    this.identityManager = identityManager;
  }

  @Override
  public String getEntityType() {
    return entityType;
  }

  @Override
  public boolean canView(Identity identity, String entityType, String entityId) {
    if (!entityType.equals(TaskAttachmentACLPlugin.entityType)) {
      throw new IllegalArgumentException("Entity type must be" + TaskAttachmentACLPlugin.entityType);
    }

    if (StringUtils.isEmpty(entityId)) {
      throw new IllegalArgumentException("Entity id must not be Empty");
    }

    if (identity == null) {
      throw new IllegalArgumentException("User identity must not be null");
    }
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
  public boolean canDelete(Identity identity, String entityType, String entityId) {
    if (!entityType.equals(TaskAttachmentACLPlugin.entityType)) {
      throw new IllegalArgumentException("Entity type must be" + TaskAttachmentACLPlugin.entityType);
    }

    if (StringUtils.isEmpty(entityId)) {
      throw new IllegalArgumentException("Entity id must not be Empty");
    }

    if (identity == null) {
      throw new IllegalArgumentException("User identity must not be null");
    }
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

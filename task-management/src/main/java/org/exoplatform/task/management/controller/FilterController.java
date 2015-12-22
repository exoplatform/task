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

package org.exoplatform.task.management.controller;

import javax.inject.Inject;

import java.util.Collections;
import java.util.List;
import java.util.ResourceBundle;

import juzu.MimeType;
import juzu.Path;
import juzu.Resource;
import juzu.Response;
import juzu.impl.common.Tools;
import juzu.request.SecurityContext;

import org.exoplatform.commons.juzu.ajax.Ajax;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.services.security.ConversationState;
import org.exoplatform.task.domain.Project;
import org.exoplatform.task.domain.Status;
import org.exoplatform.task.exception.EntityNotFoundException;
import org.exoplatform.task.management.model.TaskFilterData;
import org.exoplatform.task.management.model.TaskFilterData.Filter;
import org.exoplatform.task.management.model.TaskFilterData.FilterKey;
import org.exoplatform.task.service.ProjectService;
import org.exoplatform.task.service.StatusService;
import org.exoplatform.task.service.TaskService;
import org.exoplatform.task.service.UserService;
import org.exoplatform.task.util.ProjectUtil;
import org.exoplatform.task.util.TaskUtil.DUE;
import org.json.JSONException;

public class FilterController {

  private static final Log LOG = ExoLogger.getExoLogger(FilterController.class);

  @Inject
  TaskService taskService;
  
  @Inject
  UserService userService;
  
  @Inject
  ProjectService projectService;
  
  @Inject
  StatusService statusService;
  
  @Inject
  ResourceBundle bundle;
  
  @Inject
  TaskFilterData filterData;
  
  @Inject
  @Path("taskFilter.gtmpl")
  org.exoplatform.task.management.templates.taskFilter taskFilter;

  @Resource
  @Ajax
  @MimeType.HTML
  public Response toggleFilter(Long projectId, Long labelId, String filter, SecurityContext securityContext) throws JSONException, EntityNotFoundException {

    FilterKey filterKey = FilterKey.withProject(projectId, filter == null || filter.isEmpty() ? null : DUE.valueOf(filter.toUpperCase()));
    if (labelId != null && labelId != -1L) {
      filterKey = FilterKey.withLabel(labelId);
    }
    Filter fd = filterData.getFilter(filterKey);

    //
    fd.setEnabled(!fd.isEnabled());

    //
    if (fd.isEnabled()) {
      
      //don't allow to filter label when user already select specific label
      boolean filterLabel = labelId == null || labelId <= 0;
      
      //only allow to filter status with concrete project 
      boolean filterStatus = projectId != null && projectId > 0;
      Project project = filterStatus ? projectService.getProject(projectId) : null;
      if (project != null && !project.canView(ConversationState.getCurrent().getIdentity())) {
        project = null;
      }
      
      List<Status> status = Collections.emptyList();
      if (filterStatus && project != null) {      
        status = statusService.getStatuses(project.getId());
      }
      
      boolean filterAssignee = projectId == null || projectId != ProjectUtil.INCOMING_PROJECT_ID;
      //user already filter by dueDate, we don't need to show the dueDate field anymore
      boolean filterDueDate = filter == null || filter.isEmpty();
      
      return taskFilter.with().filterData(fd).taskService(taskService).userService(userService).filterLabel(filterLabel).status(status).bundle(bundle)
          .filterStatus(filterStatus).filterAssignee(filterAssignee).filterDueDate(filterDueDate).ok().withCharset(Tools.UTF_8);      
    } else {
      return Response.ok();
    }
  }
}
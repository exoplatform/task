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

import java.util.Arrays;
import java.util.List;
import java.util.ResourceBundle;

import juzu.MimeType;
import juzu.Path;
import juzu.Resource;
import juzu.Response;
import juzu.impl.common.Tools;
import juzu.request.SecurityContext;

import org.exoplatform.commons.juzu.ajax.Ajax;
import org.exoplatform.commons.utils.ListAccess;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.task.domain.Label;
import org.exoplatform.task.domain.UserSetting;
import org.exoplatform.task.exception.EntityNotFoundException;
import org.exoplatform.task.exception.UnAuthorizedOperationException;
import org.exoplatform.task.service.TaskService;
import org.exoplatform.task.service.UserService;
import org.exoplatform.task.util.ListUtil;
import org.exoplatform.task.util.TaskUtil;
import org.json.JSONException;
import org.json.JSONObject;

public class LabelController extends AbstractController {

  private static final Log LOG = ExoLogger.getExoLogger(LabelController.class);

  @Inject
  TaskService taskService;
  
  @Inject
  UserService userService;
  
  @Inject
  ResourceBundle bundle;
  
  @Inject
  @Path("listLabels.gtmpl")
  org.exoplatform.task.management.templates.listLabels listLabels;
  
  @Inject
  @Path("editLabelDialog.gtmpl")
  org.exoplatform.task.management.templates.editLabelDialog editLabelDialog;
  
  @Inject
  @Path("confirmDeleteLabel.gtmpl")
  org.exoplatform.task.management.templates.confirmDeleteLabel confirmDeleteLabel;

  @Resource
  @Ajax
  @MimeType.HTML
  public Response getAllLabels(SecurityContext securityContext) throws JSONException {
    String username = securityContext.getRemoteUser();
    ListAccess<Label> tmp = taskService.findLabelsByUser(username);
    List<Label> roots = Arrays.asList(ListUtil.load(tmp, 0, -1));
    List<Label> labels = TaskUtil.buildRootLabels(roots);

    return listLabels
        .with()
        .labels(labels)
        .currentLabelId(-1)
        .ok().withCharset(Tools.UTF_8);
  }

  @Resource
  @Ajax
  @MimeType.JSON
  public Response createLabel(String name, Long parentId, SecurityContext securityContext) throws UnAuthorizedOperationException {
    String username = securityContext.getRemoteUser();
    if (name != null && !(name = name.trim()).isEmpty()) {
      Label label = new Label(name, username);
      if (parentId != null && parentId > 0) {
        Label parent = taskService.getLabel(parentId);
        if (!parent.getUsername().equals(username)) {
          throw new UnAuthorizedOperationException(parentId, Label.class, getNoPermissionMsg());
        }
        label.setParent(parent);
      }
      taskService.createLabel(label);

      try {
        JSONObject json = new JSONObject();
        json.put("id", label.getId());
        return Response.ok(json.toString()).withCharset(Tools.UTF_8);      
      } catch (JSONException ex) {
        LOG.error(ex);
      }
    }
    return Response.status(503);
  }
  
  @Resource
  @Ajax
  @MimeType.HTML
  public Response openConfirmDeleteLabelDialog(Long labelId, SecurityContext context) throws UnAuthorizedOperationException {
    Label label = taskService.getLabel(labelId);
    if (label != null) {
      String username = label.getUsername();
      if (username.equals(context.getRemoteUser())) {
        return confirmDeleteLabel.with().label(label).bundle(bundle).ok().withCharset(Tools.UTF_8);
      } else {
        throw new UnAuthorizedOperationException(labelId, Label.class, getNoPermissionMsg());
      }
    }
    return Response.status(404);
  }

  @Resource
  @Ajax
  public Response deleteLabel(Long labelId, SecurityContext securityContext) throws UnAuthorizedOperationException {
    String username = securityContext.getRemoteUser();
    Label label = taskService.getLabel(labelId);
    if (label != null) {
      if (label.getUsername().equals(username)) {
        taskService.removeLabel(labelId);
        return Response.ok();        
      } else {
        throw new UnAuthorizedOperationException(labelId, Label.class, getNoPermissionMsg());
      }
    }
    
    return Response.status(404);
  }

  @Resource
  @Ajax
  @MimeType.HTML
  public Response openEditLabelDialog(Long labelId, SecurityContext context) throws UnAuthorizedOperationException {
    Label label = taskService.getLabel(labelId);
    if (label != null) {
      String username = label.getUsername();
      if (username.equals(context.getRemoteUser())) {
        ListAccess<Label> tmp = taskService.findLabelsByUser(username);
        List<Label> allLabels = Arrays.asList(ListUtil.load(tmp, 0, -1));
        allLabels = TaskUtil.filterLabelTree(allLabels, label);
        return editLabelDialog.with().lblTree(TaskUtil.buildRootLabels(allLabels)).lbl(label).ok().withCharset(Tools.UTF_8);
      } else {
        throw new UnAuthorizedOperationException(labelId, Label.class, getNoPermissionMsg());
      }
    }
    return Response.status(404);
  }
  
  @Resource
  @Ajax
  public Response changeColor(Long labelId, String color, SecurityContext securityContext) throws EntityNotFoundException, UnAuthorizedOperationException {
    String username = securityContext.getRemoteUser();
    Label label = taskService.getLabel(labelId);
    if (label != null) {
      if (label.getUsername().equals(username)) {
        label.setColor(color);
        taskService.updateLabel(label, Arrays.asList(Label.FIELDS.COLOR));
        return Response.ok();        
      } else {
        throw new UnAuthorizedOperationException(labelId, Label.class, getNoPermissionMsg());
      }
    } else {
      return Response.status(404);
    }
  }
  
  @Resource
  @Ajax
  public Response toggleHidden(Long labelId, SecurityContext securityContext) throws EntityNotFoundException, UnAuthorizedOperationException {
    String username = securityContext.getRemoteUser();
    Label label = taskService.getLabel(labelId);
    if (label != null) {
      if (label.getUsername().equals(username)) {
        label.setHidden(!label.isHidden());
        taskService.updateLabel(label, Arrays.asList(Label.FIELDS.HIDDEN));
        return Response.ok();        
      } else {
        throw new UnAuthorizedOperationException(labelId, Label.class, getNoPermissionMsg());
      }
    } else {
      return Response.status(404);
    }
  }
  
  @Resource
  @Ajax
  public Response toggleShowHiddenLabel(SecurityContext securityContext) throws EntityNotFoundException {
    String username = securityContext.getRemoteUser();
    UserSetting userSetting = userService.getUserSetting(username);
    userService.showHiddenLabel(username, !userSetting.isShowHiddenLabel());
    return Response.ok();
  }
  
  @Resource
  @Ajax
  public Response updateLabel(Long labelId, String lblName, Long parentId, SecurityContext securityContext) throws EntityNotFoundException, UnAuthorizedOperationException {
    String username = securityContext.getRemoteUser();
    Label label = taskService.getLabel(labelId);
    if (label != null) {
      if (label.getUsername().equals(username)) {
        if (lblName != null && !(lblName = lblName.trim()).isEmpty()) {
          label.setParent(taskService.getLabel(parentId));
          label.setName(lblName);
          taskService.updateLabel(label, Arrays.asList(Label.FIELDS.NAME, Label.FIELDS.PARENT));
          return Response.ok();
        } else {
          return Response.status(503);
        }        
      } else {
        throw new UnAuthorizedOperationException(labelId, Label.class, getNoPermissionMsg());
      }
    } else {
      return Response.status(404);
    }
  }
}
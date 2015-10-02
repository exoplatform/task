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

import java.util.List;

import javax.inject.Inject;

import juzu.MimeType;
import juzu.Resource;
import juzu.Response;
import juzu.impl.common.Tools;
import juzu.request.SecurityContext;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import org.exoplatform.commons.juzu.ajax.Ajax;
import org.exoplatform.commons.utils.ListAccess;
import org.exoplatform.services.organization.OrganizationService;
import org.exoplatform.services.organization.Query;
import org.exoplatform.services.organization.User;
import org.exoplatform.services.organization.UserHandler;
import org.exoplatform.services.security.ConversationState;
import org.exoplatform.services.security.Identity;
import org.exoplatform.task.domain.Label;
import org.exoplatform.task.exception.EntityNotFoundException;
import org.exoplatform.task.exception.NotAllowedOperationOnEntityException;
import org.exoplatform.task.service.TaskService;
import org.exoplatform.task.service.UserService;
import org.exoplatform.task.util.UserUtil;

/**
 * @author <a href="mailto:tuyennt@exoplatform.com">Tuyen Nguyen The</a>.
 */
public class UserController extends AbstractController {
    @Inject
    OrganizationService orgService;

    @Inject
    private UserService userService;

    @Inject
    private TaskService taskService;

    @Resource
    @Ajax
    @MimeType.JSON
    public Response findUser(String query) throws Exception { // NOSONAR
      UserHandler uHandler = orgService.getUserHandler();
      Query uQuery = new Query();
      uQuery.setUserName("*" + query + "*");
      ListAccess<User> users = uHandler.findUsersByQuery(uQuery);
      JSONArray array = new JSONArray();
      for(User u : users.load(0, users.getSize())) {
        org.exoplatform.task.model.User user = userService.loadUser(u.getUserName());
        JSONObject json = new JSONObject();
        json.put("id", u.getUserName());
        String displayName = UserUtil.getDisplayName(u);
        json.put("text", displayName);
        json.put("avatar", user.getAvatar());
        array.put(json);
      }
      return Response.ok(array.toString());
    }

    @Resource
    @Ajax
    @MimeType.JSON
    public Response findUsersToMention(String query) throws Exception { // NOSONAR
      UserHandler uHandler = orgService.getUserHandler();
      Query uQuery = new Query();
      uQuery.setUserName("*" + query + "*");
      ListAccess<User> users = uHandler.findUsersByQuery(uQuery);
      JSONArray array = new JSONArray();
      for(User u : users.load(0, users.getSize())) {
        JSONObject json = new JSONObject();
        org.exoplatform.task.model.User user = userService.loadUser(u.getUserName());
        json.put("id", "@" + u.getUserName());
        json.put("name", user.getDisplayName());
        json.put("avatar", user.getAvatar());
        json.put("type", "contact");
        array.put(json);
      }
      return Response.ok(array.toString());
    }

    @Resource
    @Ajax
    @MimeType.JSON
    public Response getDisplayNameOfUser(String usernames) throws JSONException {
      if(usernames != null) {
        JSONArray array = new JSONArray();
        for(String username : usernames.split(",")) {
          org.exoplatform.task.model.User user = userService.loadUser(username);
          JSONObject json = new JSONObject();
          json.put("id", user.getUsername());
          json.put("text", user.getDisplayName());
          json.put("avatar", user.getAvatar());
          array.put(json);
        }
        return Response.ok(array.toString()).withCharset(Tools.UTF_8);

      } else {
        return Response.ok("[]");
      }
    }

    @Resource
    @Ajax
    @MimeType("text/plain")
    public Response showHiddenProject(Boolean show, SecurityContext securityContext) {
        userService.showHiddenProject(securityContext.getRemoteUser(), show);
        return Response.ok("Update successfully");
    }

    @Resource
    @Ajax
    @MimeType("text/plain")
    public Response hideProject(Long projectId, Boolean hide) throws EntityNotFoundException, NotAllowedOperationOnEntityException {
      Identity identity = ConversationState.getCurrent().getIdentity();
      userService.hideProject(identity, projectId, hide);
      return Response.ok("Hide project successfully");
    }

    @Resource
    @Ajax
    @MimeType.JSON
    public Response findLabel(SecurityContext securityContext) throws JSONException {
      String username = securityContext.getRemoteUser();
      List<Label> labels = taskService.findLabelsByUser(username);

      //TODO: this block code is mock data
      if (labels == null || labels.isEmpty()) {
        Label label1 = new Label();
        label1.setUsername(username);
        label1.setName("Next");
        label1.setColor("sky_blue");

        Label label2 = new Label();
        label2.setUsername(username);
        label2.setName("Writing");
        label2.setColor("purple");

        Label label3 = new Label();
        label3.setUsername(username);
        label3.setName("Develop");
        label3.setColor("yellow");

        Label label4 = new Label();
        label4.setUsername(username);
        label4.setName("Next week");
        label4.setColor("yellow");

        taskService.createLabel(label1);
        taskService.createLabel(label2);
        taskService.createLabel(label3);
        taskService.createLabel(label4);

        labels = taskService.findLabelsByUser(username);
      }

      JSONArray array = new JSONArray();
      if (labels != null) {
        for (Label label : labels) {
          JSONObject json = new JSONObject();
          json.put("id", label.getId());
          json.put("text", label.getName());
          json.put("name", label.getName());
          json.put("color", label.getColor());

          array.put(json);
        }
      }
      return Response.ok(array.toString()).withCharset(Tools.UTF_8);
    }
}

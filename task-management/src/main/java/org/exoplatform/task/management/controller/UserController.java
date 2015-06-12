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

import juzu.MimeType;
import juzu.Resource;
import juzu.Response;
import juzu.impl.common.Tools;
import org.exoplatform.commons.juzu.ajax.Ajax;
import org.exoplatform.commons.utils.ListAccess;
import org.exoplatform.services.organization.OrganizationService;
import org.exoplatform.services.organization.Query;
import org.exoplatform.services.organization.User;
import org.exoplatform.services.organization.UserHandler;
import org.exoplatform.task.utils.UserUtils;
import org.exoplatform.task.service.UserService;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import javax.inject.Inject;

/**
 * @author <a href="mailto:tuyennt@exoplatform.com">Tuyen Nguyen The</a>.
 */
public class UserController {
    @Inject
    OrganizationService orgService;

    @Inject
    private UserService userService;

    @Resource
    @Ajax
    @MimeType.JSON
    public Response findUser(String query) {
        try {
            UserHandler uHandler = orgService.getUserHandler();
            Query uQuery = new Query();
            uQuery.setUserName("*" + query + "*");
            ListAccess<User> users = uHandler.findUsersByQuery(uQuery);
            JSONArray array = new JSONArray();
            for(User u : users.load(0, users.getSize())) {
                JSONObject json = new JSONObject();
                json.put("id", u.getUserName());
                String displayName = UserUtils.getDisplayName(u);
                json.put("text", displayName);
                array.put(json);
            }
            return Response.ok(array.toString());
        } catch (JSONException ex) {
            return Response.error(ex);
        } catch (Exception ex) {// NOSONAR
            return Response.error(ex);
        }
    }

    @Resource
    @Ajax
    @MimeType.JSON
    public Response findUsersToMention(String query) {
        try {
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
        } catch (JSONException ex) {
            return Response.error(ex);
        } catch (Exception ex) {// NOSONAR
            return Response.error(ex);
        }
    }

    @Resource
    @Ajax
    @MimeType.JSON
    public Response getDisplayNameOfUser(String usernames) {
      if(usernames != null) {
        try {
          JSONArray array = new JSONArray();
          for(String username : usernames.split(",")) {
            org.exoplatform.task.model.User user = userService.loadUser(username);
            JSONObject json = new JSONObject();
            json.put("id", user.getUsername());
            json.put("text", user.getDisplayName());
            array.put(json);
          }
          return Response.ok(array.toString()).withCharset(Tools.UTF_8);
        } catch (JSONException ex) {
          return Response.status(500);
        } catch (Exception ex) {// NOSONAR
          return Response.status(500);
        }
      } else {
        return Response.ok("[]");
      }
    }
}

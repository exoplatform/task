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

package org.exoplatform.task.management.util;

import org.exoplatform.container.ExoContainerContext;
import org.exoplatform.services.security.ConversationState;
import org.exoplatform.task.domain.Project;
import org.exoplatform.task.domain.Status;
import org.exoplatform.task.domain.Task;
import org.exoplatform.task.service.UserService;
import org.exoplatform.task.util.DateUtil;
import org.exoplatform.task.util.ResourceUtil;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Calendar;
import java.util.ResourceBundle;
import java.util.TimeZone;

/**
 * @author <a href="mailto:tuyennt@exoplatform.com">Tuyen Nguyen The</a>.
 */
public class JsonUtil {
  public static JSONObject buildTaskJSon(Task task, ResourceBundle bundle) {
    if (task == null) {
      throw new IllegalArgumentException("Task must not be null");
    }

    try {
      JSONObject json = new JSONObject();

      json.put("id", task.getId());
      json.put("title", task.getTitle());
      json.put("completed", task.isCompleted());
      json.put("priority", task.getPriority());

      if (task.getStatus() != null) {
        JSONObject status = buildStatusJSON(task.getStatus(), bundle);
        json.put("status", status);
      } else {
        json.put("status", false);
      }

      //. Due date class
      String dueDateCssClass = "";
      String dueDateString = "";
      if (task.getDueDate() != null) {
        UserService userService = ExoContainerContext.getCurrentContainer().getComponentInstanceOfType(UserService.class);
        TimeZone utz = userService.getUserTimezone(ConversationState.getCurrent().getIdentity().getUserId());
        if (utz == null) {
          utz = TimeZone.getDefault();
        }
        Calendar c = Calendar.getInstance(utz);
        c.setTime(task.getDueDate());

        dueDateString = DateUtil.formatDueDate(c, bundle);

        if (DateUtil.isOverdue(c)) {
          dueDateCssClass = "uiIconColorWarningInRed";
        } else if (DateUtil.isToday(c)) {
          dueDateCssClass = "uiIconColorWarningInBlue";
        }
      }
      json.put("dueDateCssClass", dueDateCssClass);
      json.put("dueDateString", dueDateString);
      json.put("dueDate", task.getDueDate() != null ? task.getDueDate().getTime() : false);

      return json;
    } catch (JSONException ex) {
      return null;
    }
  }
  public static JSONObject buildStatusJSON(Status status, ResourceBundle bundle) {
    if (status == null) {
      throw new IllegalArgumentException("Status can not be null");
    }
    try {
      JSONObject json = new JSONObject();
      json.put("id", status.getId());
      json.put("name", status.getName());
      json.put("localizedName", ResourceUtil.resolveStatus(bundle, status.getName()));
      json.put("rank", status.getRank());

      JSONObject project = buildProjectJSON(status.getProject());
      json.put("project", project);

      return json;
    } catch (JSONException ex) {
      return null;
    }
  }
  public static JSONObject buildProjectJSON(Project project) {
    if (project == null) {
      throw new IllegalArgumentException("Project can not be null");
    }

    try {
      JSONObject json = new JSONObject();

      json.put("id", project.getId());
      json.put("name", project.getName());
      json.put("color", project.getColor());

      return json;
    } catch (JSONException ex) {
      return null;
    }
  }
}

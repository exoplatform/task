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
import org.exoplatform.commons.juzu.ajax.Ajax;
import org.exoplatform.task.domain.Project;
import org.exoplatform.task.domain.Status;
import org.exoplatform.task.service.ProjectService;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import javax.inject.Inject;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;

/**
 * @author <a href="mailto:tuyennt@exoplatform.com">Tuyen Nguyen The</a>.
 */
public class StatusController {

  @Inject
  ProjectService projectService;

  @Resource
  @Ajax
  @MimeType.JSON
  public Response getAllStatus(Long projectId) {
    Project project = projectService.getProjectById(projectId);
    if(project == null) {
      return Response.notFound("Project does not exist with ID: " + projectId);
    }
    try {
      JSONArray array = new JSONArray();
      List<Status> statuses = new LinkedList<Status>(project.getStatus());
      Collections.sort(statuses, new Comparator<Status>() {
        @Override
        public int compare(Status o1, Status o2) {
          if(o1.getRank() == null) {
            return o2.getRank() == null ? 0 : -1;
          } else if(o2.getRank() == null) {
            return 1;
          }

          return o1.getRank().compareTo(o2.getRank());
        }
      });
      for (Status status : statuses) {
        JSONObject json = new JSONObject();
        json.put("value", status.getId());
        json.put("text", status.getName());
        array.put(json);
      }
      return Response.ok(array.toString());
    } catch (JSONException ex) {
      return Response.status(500).body("JSONException while create reporting");
    }
  }
}

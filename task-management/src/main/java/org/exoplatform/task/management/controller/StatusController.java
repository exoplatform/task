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
import org.exoplatform.task.domain.Status;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * @author <a href="mailto:tuyennt@exoplatform.com">Tuyen Nguyen The</a>.
 */
public class StatusController {
  @Resource
  @Ajax
  @MimeType.JSON
  public Response getAllStatus() {
    try {
      JSONArray array = new JSONArray();
      for (Status status : Status.STATUS) {
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

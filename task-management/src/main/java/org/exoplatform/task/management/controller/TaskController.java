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

import juzu.*;
import juzu.plugin.ajax.Ajax;
import juzu.plugin.asset.Assets;
import org.exoplatform.task.domain.Status;
import org.exoplatform.task.domain.Task;
import org.exoplatform.task.service.TaskBuilder;
import org.exoplatform.task.service.TaskService;
import org.json.JSONException;
import org.json.JSONObject;

import javax.inject.Inject;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

/**
 * @author <a href="mailto:tuyennt@exoplatform.com">Tuyen Nguyen The</a>.
 */
public class TaskController {

    @Inject
    TaskService taskService;

    @Inject
    @Path("detail.gtmpl")
    org.exoplatform.task.management.templates.detail detail;

    @Resource
    @Ajax
    @MimeType.HTML
    public Response detail(Long id) {
        return detail.with()
                .task(taskService.findTaskById(id))
                .ok();
    }

    @Resource
    @Ajax
    @MimeType.JSON
    public Response clone(Long id) {
        Task task = taskService.findTaskById(id);
        if(task == null) {
            return Response.notFound("Can not find task with ID: " + id);
        }
        Task newTask = new TaskBuilder()
                .withTitle("[Clone] " + task.getTitle())
                .withAssignee(task.getAssignee())
                .withContext(task.getContext())
                .withCreatedBy(task.getCreatedBy())
                .withDescription(task.getDescription())
                .withDueDate(task.getDueDate())
                .withPriority(task.getPriority())
                .withStartDate(task.getStartDate())
                .withDuration(task.getDuration())
                .withStatus(task.getStatus())
                .build();
        newTask.setProject(task.getProject());
        newTask.setTags(task.getTags());
        taskService.save(newTask);
        try {
            JSONObject json = new JSONObject();
            json.put("id", newTask.getId());
            return Response.ok(json.toString());
        } catch (JSONException ex) {
            return Response.status(500).body(ex.getMessage());
        }
    }

    @Resource
    @Ajax
    @MimeType.JSON
    public Response delete(Long id) {
        Task task = taskService.findTaskById(id);
        if(task == null) {
            return Response.notFound("Can not find task with ID: " + id);
        }
        taskService.remove(task);
        try {
            JSONObject json = new JSONObject();
            json.put("id", task.getId());
            return Response.ok(json.toString());
        } catch (JSONException ex) {
            return Response.status(500).body(ex.getMessage());
        }
    }

    @Resource
    @Ajax
    @MimeType("text/plain")
    public Response saveTaskInfo(Long taskId, String name, String value) {
        Task task = taskService.findTaskById(taskId);
        if(task == null) {
            return Response.notFound("Task not found with ID: " + taskId);
        }
        if("title".equalsIgnoreCase(name)) {
            task.setTitle(value);
        } else if("dueDate".equalsIgnoreCase(name)) {
            if(value == null || value.trim().isEmpty()) {
                task.setDueDate(null);
            } else {
                DateFormat df = new SimpleDateFormat("YYYY-MM-dd");
                try {
                    Date date = df.parse(value);
                    task.setDueDate(date);
                } catch (ParseException ex) {
                    return Response.status(406).body("Can not parse date time value: " + value);
                }
            }
        } else if("status".equalsIgnoreCase(name)) {
            //TODO: load status from ID
            try {
                int status = Integer.parseInt(value);
                for(Status s : Status.STATUS) {
                    if(s.getId() == status) {
                        task.setStatus(s);
                        break;
                    }
                }
            } catch (NumberFormatException ex) {
                return Response.status(406).body("Status is unacceptable: " + value);
            }
        } else if("description".equalsIgnoreCase(name)) {
            task.setDescription(value);
        } else {
            return Response.status(406).body("Field name: " + name + " is not supported");
        }

        taskService.save(task);
        return Response.ok("Update successfully");
    }

    @Resource
    @Ajax
    @MimeType("text/plain")
    public Response updateTaskStatus(Long taskId, int status) {
        Task task = taskService.findTaskById(taskId);
        if(task == null) {
            return Response.notFound("Task not found with ID: " + taskId);
        }
        for(Status s : Status.STATUS) {
            if(s.getId() == status) {
                task.setStatus(s);
                break;
            }
        }
        taskService.save(task);
        return Response.ok("Update successfully");
    }
}

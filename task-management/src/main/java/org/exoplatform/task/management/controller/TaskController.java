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
import juzu.Path;
import juzu.Resource;
import juzu.Response;
import juzu.plugin.ajax.Ajax;
import org.exoplatform.services.organization.OrganizationService;
import org.exoplatform.services.organization.User;
import org.exoplatform.task.domain.Status;
import org.exoplatform.task.domain.Task;
import org.exoplatform.task.management.util.UserUtils;
import org.exoplatform.task.service.TaskBuilder;
import org.exoplatform.task.service.TaskService;
import org.json.JSONException;
import org.json.JSONObject;

import javax.inject.Inject;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Collections;
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
  OrganizationService orgService;

  @Inject
  @Path("detail.gtmpl")
  org.exoplatform.task.management.templates.detail detail;

  @Resource
  @Ajax
  @MimeType.HTML
  public Response detail(Long id) {
    Task task = taskService.findTaskById(id);
    StringBuilder coWorkerDisplayName = new StringBuilder();
    if(task.getCoworker() != null && task.getCoworker().size() > 0) {
      for(String userName : task.getCoworker()) {
        try {
          User user = orgService.getUserHandler().findUserByName(userName);
          if(user != null) {
            if(coWorkerDisplayName.length() > 0) {
              coWorkerDisplayName.append(", ");
            }
            coWorkerDisplayName.append(UserUtils.getDisplayName(user));
          }
        } catch (Exception ex) {
        }
      }
    }

    String assignee = "";
    if(task.getAssignee() != null) {
      try {
        User user = orgService.getUserHandler().findUserByName(task.getAssignee());
        if(user != null) {
          assignee = UserUtils.getDisplayName(user);
        }
      } catch (Exception ex) {
        return Response.status(500).body(ex.getMessage());
      }
    }
    return detail.with()
        .task(task)
        .assigneeName(assignee)
        .coWokerDisplayName(coWorkerDisplayName.toString())
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
    newTask.setCoworker(task.getCoworker());
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
  public Response saveTaskInfo(Long taskId, String name, String[] value) {
    String val = value != null && value.length > 0 ? value[0] : null;

    Task task = taskService.findTaskById(taskId);
    if(task == null) {
      return Response.notFound("Task not found with ID: " + taskId);
    }
    if("title".equalsIgnoreCase(name)) {
      task.setTitle(val);
    } else if("dueDate".equalsIgnoreCase(name)) {
      if(value == null || val.trim().isEmpty()) {
        task.setDueDate(null);
      } else {
        DateFormat df = new SimpleDateFormat("YYYY-MM-dd");
        try {
          Date date = df.parse(val);
          task.setDueDate(date);
        } catch (ParseException ex) {
          return Response.status(406).body("Can not parse date time value: " + val);
        }
      }
    } else if("status".equalsIgnoreCase(name)) {
      //TODO: load status from ID
      try {
        int status = Integer.parseInt(val);
        for(Status s : Status.STATUS) {
          if(s.getId() == status) {
            task.setStatus(s);
            break;
          }
        }
      } catch (NumberFormatException ex) {
        return Response.status(406).body("Status is unacceptable: " + val);
      }
    } else if("description".equalsIgnoreCase(name)) {
      task.setDescription(val);
    } else if("assignee".equalsIgnoreCase(name)) {
      task.setAssignee(val);
    } else if("coworker".equalsIgnoreCase(name)) {
      Set<String> coworker = new HashSet<String>();
      for(String v : value) {
        coworker.add(v);
      }
      task.setCoworker(coworker);
    } else if("tags".equalsIgnoreCase(name)) {
      Set<String> tags = new HashSet<String>();
      for(String t : value) {
        tags.add(t);
      }
      task.setTags(tags);
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

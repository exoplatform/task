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

import juzu.Action;
import juzu.Path;
import juzu.Response;
import juzu.View;
import juzu.impl.common.Tools;
import juzu.request.SecurityContext;
import org.exoplatform.task.domain.Project;
import org.exoplatform.task.domain.Task;
import org.exoplatform.task.service.TaskParser;
import org.exoplatform.task.service.TaskService;

import javax.inject.Inject;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;

/**
 * @author <a href="mailto:tuyennt@exoplatform.com">Tuyen Nguyen The</a>.
 */
public class TaskManagement {

  @Inject
  TaskService taskService;

  @Inject
  TaskParser taskParser;

  @Inject
  @Path("index.gtmpl")
  org.exoplatform.task.management.templates.index index;

  @Inject
  ResourceBundle bundle;
  
  @Inject
  ProjectController projectController;

  @View
  public Response.Content index(SecurityContext securityContext) {
    //TODO: should check if username is null?
    String username = securityContext.getRemoteUser();
    List<Task> tasks = taskService.getIncomingTasksByUser(username, null);

    Map<String, List<Task>> groupTasks = new HashMap<String, List<Task>>();
    groupTasks.put("", tasks);

    List<Project> projects = projectController.getProjectTree();

    return index.with()
        .currentProjectId(-1)
        .project(null)
        .tasks(tasks)
        .groupTasks(groupTasks)
        .keyword("")
        .groupBy("")
        .orderBy("")
        .projects(projects)
        .ok().withCharset(Tools.UTF_8);
  }

  @Action
  public Response changeViewState(String groupBy, String orderBy) {
    return TaskManagement_.index();
  }

  @Action
  public Response createTask(String taskInput, String groupBy, String orderBy) {
    if(taskInput != null && !taskInput.isEmpty()) {
      taskService.createTask(taskParser.parse(taskInput));
    } else {

    }
    return TaskManagement_.index();
  }
}

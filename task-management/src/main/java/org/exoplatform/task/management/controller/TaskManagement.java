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
import org.exoplatform.task.service.GroupByService;
import org.exoplatform.task.service.OrderBy;
import org.exoplatform.task.service.TaskParser;
import org.exoplatform.task.service.TaskService;
import javax.inject.Inject;
import java.util.*;

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

  @View
  public Response.Content index(String groupBy, String orderBy) {
    List<GroupByService> groupByServices = taskService.getGroupByServices();
    GroupByService current = null;
    List<Map.Entry<String, String>> groupByNames = new ArrayList<Map.Entry<String, String>>(groupByServices.size());
    for(GroupByService g : groupByServices) {
      if(g.getName().equalsIgnoreCase(groupBy)) {
        current = g;
      }
      try {
        groupByNames.add(
            new AbstractMap.SimpleEntry<String, String>(g.getName(),bundle.getString("label." + g.getName()))
        );
      } catch (MissingResourceException ex) {
        groupByNames.add(new AbstractMap.SimpleEntry<String, String>(g.getName(), g.getName()));
      }
    }
    if(current == null) {
      current = groupByServices.get(0);
    }

    List<OrderBy> orders = new ArrayList<OrderBy>();
    if(orderBy != null) {
      orders.add(new OrderBy(orderBy, false));
    }

    List<Map.Entry<String, String>> orderByNames = new ArrayList<Map.Entry<String, String>>();
    for(String name : new String[] {"title", "duedate", "priority"}) {
      try {
        orderByNames.add(
            new AbstractMap.SimpleEntry<String, String>(name, bundle.getString("label." + name))
        );
      } catch (MissingResourceException ex) {
        orderByNames.add(new AbstractMap.SimpleEntry<String, String>(name, name));
      }
    }

    return index.with()
        .groupByNames(groupByNames)
        .orderByNames(orderByNames)
        .currentGroupBy(groupBy != null ? groupBy : "")
        .currentOrderBy(orderBy != null ? orderBy : "")
        .groupTasks(current.getGroupTasks(orders))
        .ok().withCharset(Tools.UTF_8);
  }

  @Action
  public Response changeViewState(String groupBy, String orderBy) {
    return TaskManagement_.index(groupBy, orderBy);
  }

  @Action
  public Response createTask(String taskInput, String groupBy, String orderBy) {
    if(taskInput != null && !taskInput.isEmpty()) {
      this.taskService.getTaskHandler().create(taskParser.parse(taskInput));
    } else {

    }
    return TaskManagement_.index(groupBy, orderBy);
  }
}

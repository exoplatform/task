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

package org.exoplatform.task.service.impl;

import org.exoplatform.task.domain.Task;
import org.exoplatform.task.model.GroupTask;
import org.exoplatform.task.service.GroupByService;
import org.exoplatform.task.service.OrderBy;
import org.exoplatform.task.service.TaskService;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * @author <a href="mailto:tuyennt@exoplatform.com">Tuyen Nguyen The</a>.
 */
public abstract class AbstractGroupBy<T> implements GroupByService {

  protected final TaskService taskService;

  protected AbstractGroupBy(TaskService taskService) {
    this.taskService = taskService;
  }

  @Override
  public List<GroupTask> getGroupTasks(List<OrderBy> orders) {
    Map<T, List<Task>> maps = this.getMaps();
    List<GroupTask> groupTasks = new ArrayList<GroupTask>();
    for(Map.Entry<T, List<Task>> entry : maps.entrySet()) {
      String name = this.getTitle(entry.getKey());
      List<Task> tasks = entry.getValue();
      Collections.sort(tasks, new TaskComparator(orders));
      groupTasks.add(new GroupTask(name, tasks));
    }
    return groupTasks;
  }

  protected abstract String getTitle(T key);
  protected abstract Map<T, List<Task>> getMaps();
}

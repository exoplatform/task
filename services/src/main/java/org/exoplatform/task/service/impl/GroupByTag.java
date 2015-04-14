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
import org.exoplatform.task.service.TaskService;

import java.util.*;

/**
 * @author <a href="mailto:tuyennt@exoplatform.com">Tuyen Nguyen The</a>.
 */
public class GroupByTag extends AbstractGroupBy<String> {
  public GroupByTag(TaskService taskService) {
    super(taskService);
  }

  @Override
  protected String getTitle(String key) {
    return key == null ? "not tagged" : key;
  }

  @Override
  protected Map<String, List<Task>> getMaps() {
    Map<String, List<Task>> maps = new HashMap<String, List<Task>>();
    for(Task task : taskService.findAllTask()) {
      Set<String> tags = task.getTags();
      if(tags == null || tags.isEmpty()) {
        this.put(maps, null, task);
      } else {
        for(String tag : tags) {
          this.put(maps, tag, task);
        }
      }
    }
    return maps;
  }

  protected void put(Map<String, List<Task>> maps, String tag, Task task) {
    List<Task> list = maps.get(tag);
    if(list == null) {
      list = new ArrayList<Task>();
      maps.put(tag, list);
    }
    list.add(task);
  }

  @Override
  public String getName() {
    return "tag";
  }
}

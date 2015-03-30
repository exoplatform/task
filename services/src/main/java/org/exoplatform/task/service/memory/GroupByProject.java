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

package org.exoplatform.task.service.memory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.exoplatform.task.domain.Project;
import org.exoplatform.task.domain.Task;
import org.exoplatform.task.service.TaskService;

/**
 * @author <a href="mailto:tuyennt@exoplatform.com">Tuyen Nguyen The</a>.
 */
public class GroupByProject extends AbstractGroupBy<Project> {

    public GroupByProject(TaskService taskService) {
        super(taskService);
    }

    @Override
    protected String getTitle(Project key) {
        if(key == null) {
            return "Personal task";
        }
        return key.getName();
    }

    @Override
    protected Map<Project, List<Task>> getMaps() {
        Map<Project, List<Task>> maps = new HashMap<Project, List<Task>>();
        for(Task task : taskService.findAllTask()) {
            Project project = task.getProject();
            this.put(maps, project, task);
        }
        return maps;
    }

    protected void put(Map<Project, List<Task>> maps, Project project, Task task) {
        List<Task> list = maps.get(project);
        if(list == null) {
            list = new ArrayList<Task>();
            maps.put(project, list);
        }
        list.add(task);
    }

    @Override
    public String getName() {
        return "project";
    }
}

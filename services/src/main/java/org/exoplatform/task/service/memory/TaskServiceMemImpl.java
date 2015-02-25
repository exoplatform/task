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

import org.exoplatform.task.domain.Project;
import org.exoplatform.task.domain.Task;
import org.exoplatform.task.service.TaskParser;
import org.exoplatform.task.service.TaskService;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

/**
 * @author <a href="mailto:tuyennt@exoplatform.com">Tuyen Nguyen The</a>.
 */
public class TaskServiceMemImpl implements TaskService {
    private final List<Task> tasks = new LinkedList<Task>();

    @Inject
    private TaskParser parser;

    public TaskServiceMemImpl() {

    }

    @PostConstruct
    public void initData() {
        this.save(parser.parse("Task 1"));
        this.save(parser.parse("Task 2"));
        this.save(parser.parse("Task 3"));
        this.save(parser.parse("Task 4"));
        this.save(parser.parse("Task 5"));
    }

    @Override
    public void save(Task task) {
        int index = tasks.indexOf(task);
        if(index != -1) {
            tasks.remove(index);
        }
        tasks.add(task);
    }

    @Override
    public Task findTaskById(long id) {
        for(Task t : tasks) {
            if(id == t.getId()) {
                return t;
            }
        }
        return null;
    }

    @Override
    public List<Task> findAllTask() {
        return Collections.unmodifiableList(tasks);
    }

    @Override
    public List<Task> findTaskByProject(Project project) {
        List<Task> ts = new LinkedList<Task>();
        for(Task t : tasks) {
            for(Project p : t.getProjects()) {
                if(p.getId() == project.getId()) {
                    ts.add(t);
                    break;
                }
            }
        }
        return ts;
    }

    @Override
    public List<Task> findTaskByTag(String tag) {
        List<Task> ts = new LinkedList<Task>();
        for(Task t : tasks) {
            if(t.getTags().contains(tag)) {
                ts.add(t);
            }
        }
        return ts;
    }
}

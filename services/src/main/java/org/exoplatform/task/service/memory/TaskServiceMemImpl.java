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

import org.exoplatform.task.domain.Priority;
import org.exoplatform.task.domain.Project;
import org.exoplatform.task.domain.Status;
import org.exoplatform.task.domain.Task;
import org.exoplatform.task.model.GroupTask;
import org.exoplatform.task.service.*;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.*;

/**
 * @author <a href="mailto:tuyennt@exoplatform.com">Tuyen Nguyen The</a>.
 */
@Singleton
public class TaskServiceMemImpl implements TaskService {
    private final List<Task> tasks = new LinkedList<Task>();

    @Inject
    private TaskParser parser;

    private final List<GroupByService> groupByServices;

    public TaskServiceMemImpl() {
        this.groupByServices = new ArrayList<GroupByService>();
        this.groupByServices.add(new GroupByStatus(this));
        this.groupByServices.add(new GroupByProject(this));
        this.groupByServices.add(new GroupByTag(this));
    }

    @PostConstruct
    public void initData() {
        //. Init some demo task for display in list
        TaskBuilder builder = new TaskBuilder();

        //. status TODO
        builder.withStatus(Status.TODO);
        builder.withTitle("choose audio track");
        builder.addTag("tag1");
        builder.addTag("tag2");
        builder.addTag("tag3");
        builder.withDueDate(new Date());
        this.save(builder.build());
        builder = new TaskBuilder();

        Project project = new Project(1, "Management project");
        builder.withTitle("choose speaker voice");
        builder.withProject(project)
                .withPriority(Priority.LOW);
        this.save(builder.build());
        builder = new TaskBuilder();

        builder.withTitle("choose speaker place");
        builder.withProject(project).withPriority(Priority.HIGH);
        this.save(builder.build());
        builder = new TaskBuilder();

        builder.withTitle("record demo");
        builder.withProject(project).withPriority(Priority.MEDIUM);
        this.save(builder.build());
        builder = new TaskBuilder();

        //. Status: In Progress
        builder.withStatus(Status.IN_PROGRESS);
        builder.withTitle("draft script");
        this.save(builder.build());
        builder.withTitle("record quick and dirty screencast");
        this.save(builder.build());

        //. Status: Waiting on
        builder.withStatus(Status.WAITING_ON);
        builder.withTitle("check competitor");
        this.save(builder.build());
        builder.withTitle("submit script to agency");
        this.save(builder.build());

        //. Status: DONE
        builder.withStatus(Status.DONE);
        builder.withTitle("negotiation and contract");
        this.save(builder.build());
        builder.withTitle("contract agencies");
        this.save(builder.build());
    }

    @Override
    public void save(Task task) {
        int index = tasks.indexOf(task);
        if(index != -1) {
            tasks.remove(index);
        }
        if(task.getId() == 0) {
            task.setId(tasks.size() + 1);
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
    public List<GroupByService> getGroupByServices() {
        return Collections.unmodifiableList(this.groupByServices);
    }

    public void remove(Task task) {
        this.tasks.remove(task);
    }
}

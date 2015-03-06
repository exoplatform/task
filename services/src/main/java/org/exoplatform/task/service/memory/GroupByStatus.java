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

import org.exoplatform.task.domain.Status;
import org.exoplatform.task.domain.Task;
import org.exoplatform.task.model.GroupTask;
import org.exoplatform.task.service.OrderBy;

import java.util.*;

/**
 * @author <a href="mailto:tuyennt@exoplatform.com">Tuyen Nguyen The</a>.
 */
public class GroupByStatus extends AbstractGroupBy<Status> {
    public GroupByStatus(TaskServiceMemImpl serviceMem) {
        super(serviceMem);
    }

    @Override
    public String getName() {
        return "status";
    }

    @Override
    protected String getTitle(Status key) {
        return key.getName();
    }

    @Override
    protected Map<Status, List<Task>> getMaps() {
        TreeMap<Status, List<Task>> maps = new TreeMap<Status, List<Task>>(new Comparator<Status>() {
            @Override
            public int compare(Status o1, Status o2) {
                if(o1 == null) {
                    if(o2 == null) {
                        return 0;
                    } else {
                        return -1;
                    }
                } else if (o2 == null) {
                    return 1;
                }

                return Long.compare(o1.getId(), o2.getId());
            }
        });
        for(Task task : taskService.findAllTask()) {
            Status status = task.getStatus();
            if(status == null) {
                status = Status.TODO;
            }

            List<Task> list = maps.get(status);
            if(list == null) {
                list = new ArrayList<Task>();
                maps.put(status, list);
            }
            list.add(task);
        }
        return maps;
    }
}

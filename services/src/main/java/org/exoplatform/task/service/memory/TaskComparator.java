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
import org.exoplatform.task.domain.Task;
import org.exoplatform.task.service.OrderBy;

import java.util.Comparator;
import java.util.Date;
import java.util.List;

/**
 * @author <a href="mailto:tuyennt@exoplatform.com">Tuyen Nguyen The</a>.
 */
public class TaskComparator implements Comparator<Task> {
    private final List<OrderBy> orderBy;

    public TaskComparator(List<OrderBy> orderBy) {
        this.orderBy = orderBy;
    }

    @Override
    public int compare(Task o1, Task o2) {
        if(orderBy == null || orderBy.size() == 0) {
            return 0;
        }

        if(o1 == null) {
            if(o2 == null) {
                return 0;
            } else {
                // o1 < o2
                return -1;
            }
        } else if(o2 == null) {
            // o1 > o2
            return 1;
        }

        for(OrderBy order : orderBy) {
            int compare = this.compare(order, o1, o2);
            if(compare != 0) {
                return compare;
            }
        }

        return 0;
    }

    private int compare(OrderBy order, Task t1, Task t2) {
        if(order == null) {
            return 0;
        }
        if("title".equalsIgnoreCase(order.field)) {
            return t1.getTitle().compareTo(t2.getTitle());

        } else if("duedate".equalsIgnoreCase(order.field)) {
            Date dueDate1 = t1.getDueDate();
            Date dueDate2 = t2.getDueDate();
            if(dueDate1 == null) {
                if(dueDate2 == null) {
                    return 0;
                }
                return order.isDesc ? -1 : 1;
            }
            if(dueDate2 == null) {
                return order.isDesc ? 1 : -1;
            }

            return order.isDesc ? dueDate2.compareTo(dueDate1) : dueDate1.compareTo(dueDate2);

        } else if("priority".equalsIgnoreCase(order.field)) {
            Priority p1 = t1.getPriority();
            Priority p2 = t2.getPriority();
            if(p1 == null) {
                if(p2 == null) {
                    return 0;
                }
                return order.isDesc ? -1 : 1;
            }
            if(p2 == null) {
                return order.isDesc ? 1 : -1;
            }

            return order.isDesc ? p2.compareTo(p1) : p1.compareTo(p2);
        }
        return 0;
    }
}

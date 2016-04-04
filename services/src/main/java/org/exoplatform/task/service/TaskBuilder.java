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

package org.exoplatform.task.service;

import org.exoplatform.task.domain.Priority;
import org.exoplatform.task.domain.Status;
import org.exoplatform.task.domain.Task;

import java.util.Date;
import java.util.HashSet;
import java.util.Set;

/**
 * @author <a href="mailto:tuyennt@exoplatform.com">Tuyen Nguyen The</a>.
 */
public class TaskBuilder {
  private String title;
  private String description;

  private Priority priority = Priority.NORMAL;
  private String context;
  private String assignee;
  private Set<String> coworker;

  private Status status;

  private String createdBy;
  private Date createdTime = new Date();

  private Date endDate;
  private Date startDate;
  private Date dueDate;

  public Task build() {
    Task task = new Task();

    task.setTitle(title);
    task.setDescription(description);
    task.setPriority(priority);
    task.setContext(context);
    task.setAssignee(assignee);
    task.setCoworker(coworker);
    task.setStatus(status);
    task.setCreatedBy(createdBy);
    task.setCreatedTime(createdTime);
    task.setEndDate(endDate);
    task.setStartDate(startDate);
    task.setDueDate(dueDate);

    return task;
  }

  public TaskBuilder withTitle(String title) {
    this.title = title;
    return this;
  }
  public TaskBuilder withDescription(String description) {
    this.description = description;
    return this;
  }
  public TaskBuilder withPriority(Priority priority) {
    this.priority = priority;
    return this;
  }

  public TaskBuilder withAssignee(String assignee) {
    this.assignee = assignee;
    return this;
  }

  public TaskBuilder addCoworker(String coworker) {
    if (this.coworker == null) {
      this.coworker = new HashSet<String>();
    }
    this.coworker.add(coworker);
    return this;
  }

  public TaskBuilder withContext(String context) {
    this.context = context;
    return this;
  }

  public TaskBuilder withDueDate(Date date) {
    this.dueDate = date;
    return this;
  }

  public TaskBuilder withStatus(Status status) {
    this.status = status;
    return this;
  }

  public TaskBuilder withCreatedBy(String username) {
    this.createdBy = username;
    return this;
  }

  public TaskBuilder withEndDate(Date endDate) {
    this.endDate = endDate;
    return this;
  }

  public TaskBuilder withStartDate(Date date) {
    this.startDate = date;
    return this;
  }
}

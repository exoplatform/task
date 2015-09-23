/**
 * Copyright (C) 2015 eXo Platform SAS.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Affero General Public License
 * as published by the Free Software Foundation; either version 3
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, see<http://www.gnu.org/licenses/>.
 **/
  
package org.exoplatform.task.service.impl;

import org.exoplatform.task.domain.Task;
import org.exoplatform.task.service.TaskService;

public class TaskEvent {
  public static enum Type {
    CREATED,
    EDIT_TITLE,
    EDIT_DESCRIPTION,
    EDIT_DUEDATE,
    EDIT_PROJECT,
    EDIT_ASSIGNEE,
    EDIT_COWORKER,
    EDIT_STATUS,
    ADD_LABEL,
    EDIT_WORKPLAN,
    ADD_SUBTASK,
    MARK_DONE
  }
  private TaskService context;
  private Type type;
  private Task task;
  private Object oldVal;
  private Object newVal;

  private TaskEvent(TaskService context, Type type, Task task, Object oldVal, Object newVal) {
    this.type = type;
    this.task = task;
    this.oldVal = oldVal;
    this.newVal = newVal;
    this.context = context;
  }

  public Type getType() {
    return type;
  }

  public Task getTask() {
    return task;
  }

  public Object getOldVal() {
    return oldVal;
  }

  public Object getNewVal() {
    return newVal;
  }
  
  public TaskService getContext() {
    return context;
  }

  public static class EventBuilder {
    private Type type;
    private Task task;
    private Object oldVal;
    private Object newVal;
    private TaskService context;
    
    public EventBuilder(TaskService context) {
      this.context = context;
    }

    public EventBuilder withType(Type type) {
      this.type = type;
      return this;
    }
    
    public EventBuilder withTask(Task task) {
      this.task = task;
      return this;
    }    
    
    public EventBuilder withOldVal(Object oldVal) {
      this.oldVal = oldVal;
      return this;
    }
    
    public EventBuilder withNewVal(Object newVal) {
      this.newVal = newVal;
      return this;
    }
    
    public TaskEvent build() {   
      return new TaskEvent(context, type, task, oldVal, newVal);
    }
  }
}

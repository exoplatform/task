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

package org.exoplatform.task.dao;

import org.exoplatform.task.dao.condition.Conditions;

public class LabelQuery extends Query {
  public LabelQuery setUserName(String username) {
    if (username != null) {
      this.add(Conditions.eq(Conditions.USERNAME, username));
    }
    return this;
  }
  
  public LabelQuery setTaskId(Long taskId) {
    if (taskId != null) {
      this.add(Conditions.eq(Conditions.LABEL_TASK_ID, taskId));
    }
    return this;
  }

  public LabelQuery setProjectId(Long projectId) {
    if (projectId != null) {
      this.add(Conditions.eq(Conditions.LABEL_PROJECT_ID, projectId));
    }
    return this;
  }

}

/* 
* Copyright (C) 2003-2015 eXo Platform SAS.
*
* This program is free software: you can redistribute it and/or modify
* it under the terms of the GNU Affero General Public License as published by
* the Free Software Foundation, either version 3 of the License, or
* (at your option) any later version.
*
* This program is distributed in the hope that it will be useful,
* but WITHOUT ANY WARRANTY; without even the implied warranty of
* MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
* GNU Affero General Public License for more details.
*
* You should have received a copy of the GNU Affero General Public License
* along with this program. If not, see http://www.gnu.org/licenses/ .
*/
package org.exoplatform.task.dao;

import java.util.List;
import java.util.Set;

import org.exoplatform.commons.api.persistence.GenericDAO;
import org.exoplatform.commons.utils.ListAccess;
import org.exoplatform.task.domain.Status;
import org.exoplatform.task.domain.Task;

/**
 * @author <a href="trongtt@exoplatform.com">Trong Tran</a>
 * @version $Revision$
 */
public interface TaskHandler extends GenericDAO<Task, Long> {

  ListAccess<Task> findTasksByLabel(long labelId, List<Long> projectIds, String username, OrderBy orderBy);
 
  List<Task> findByUser(String user);

  ListAccess<Task> findTasks(TaskQuery query);

  public <T> List<T> selectTaskField(TaskQuery query, String fieldName);

  Task findTaskByActivityId(String activityId);
  
  void updateStatus(Status stOld, Status stNew);

  void updateTaskOrder(long currentTaskId, Status newStatus, long[] orders);
  
  Set<String> getCoworker(long taskid);
}


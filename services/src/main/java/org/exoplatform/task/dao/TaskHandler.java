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

import java.util.Date;
import java.util.List;

import org.exoplatform.commons.api.persistence.GenericDAO;
import org.exoplatform.task.domain.Status;
import org.exoplatform.task.domain.Task;

/**
 * @author <a href="trongtt@exoplatform.com">Trong Tran</a>
 * @version $Revision$
 */
public interface TaskHandler extends GenericDAO<Task, Long> {

  List<Task> findByProject(Long projectId);

  List<Task> findByUser(String user);

  List<Task> findAllByMembership(String user, List<String> memberships);

  List<Task> findByTag(String tag);

  List<Task> findByTags(List<String> tags);

  List<Task> findTasksByLabel(long labelId, OrderBy orderBy);
 
  List<Task> findTaskByQuery(TaskQuery query);

  List<Task> getIncomingTask(String username, OrderBy orderBy);

  List<Task> getToDoTask(String username, List<Long> projectIds, OrderBy orderBy, Date fromDueDate, Date toDueDate);

  Task findTaskByActivityId(String activityId);

  long getTaskNum(String username, List<Long> projectIds);

  void updateTaskOrder(long currentTaskId, Status newStatus, long[] orders);
  
}


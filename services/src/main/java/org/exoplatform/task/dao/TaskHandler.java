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

import org.exoplatform.commons.api.persistence.GenericDAO;
import org.exoplatform.commons.utils.ListAccess;
import org.exoplatform.task.domain.Status;
import org.exoplatform.task.domain.Task;

import java.util.List;
import java.util.Set;

/**
 * @author <a href="trongtt@exoplatform.com">Trong Tran</a>
 * @version $Revision$
 */
public interface TaskHandler extends GenericDAO<Task, Long> {

  ListAccess<Task> findTasksByLabel(long labelId, List<Long> projectIds, String username, OrderBy orderBy);
 
  List<Task> findByUser(String user);

  ListAccess<Task> findTasks(TaskQuery query);

  public List<Task> findAllByMembership(String user, List<String> memberships);

  public <T> List<T> selectTaskField(TaskQuery query, String fieldName);

  Task findTaskByActivityId(String activityId);

  void updateStatus(Status stOld, Status stNew);

  void updateTaskOrder(long currentTaskId, Status newStatus, long[] orders);

  Set<String> getCoworker(long taskid);

  Task getTaskWithCoworkers(long id);

  List<Task> getUncompletedTasks(String user, int limit);

  List<Task> getWatchedTasks(String user, int limit);

  Long countWatchedTasks(String user);

  List<Task> getCollaboratedTasks(String user, int limit);

  Long countCollaboratedTasks(String user);

    List<Task> getAssignedTasks(String user, int limit);

  Long countAssignedTasks(String user);

  List<Task> getByStatus(long statusid);

  Long countUncompletedTasks(String user);

  ListAccess<Task> getIncomingTasks(String user);

  List<Task> getOverdueTasks(String user, int limit);

  Long countOverdueTasks(String user);

  void addWatcherToTask(String username, Task task) throws Exception;

  void deleteWatcherOfTask(String username,Task task) throws Exception;

  Set<String> getWatchersOfTask(Task task);

  /**
   * Find tasks assigned to a user using a term to find in title or description
   * of the task
   * 
   * @param user username
   * @param memberships memberships
   * @param query term to search in title or description
   * @param limit term to limit results.
   * @return {@link List} of {@link Task}
   */
  List<Task> findTasks(String user, List<String> memberships, String query, int limit);

  /**
   * Count tasks assigned to a user using a search term to find in title or
   * description of the task
   * 
   * @param user username
   * @param query term to search in title or description
   * @return tasks count
   */
  long countTasks(String user, String query);

    List<Object[]> countTaskStatusByProject(long projectId);
}


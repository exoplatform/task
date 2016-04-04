/* 
* Copyright (C) 2003-2015 eXo Platform SAS.
*
* This program is free software: you can redistribute it and/or modify
* it under the terms of the GNU Lesser General Public License as published by
* the Free Software Foundation, either version 3 of the License, or
* (at your option) any later version.
*
* This program is distributed in the hope that it will be useful,
* but WITHOUT ANY WARRANTY; without even the implied warranty of
* MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
* GNU Lesser General Public License for more details.
*
* You should have received a copy of the GNU Lesser General Public License
* along with this program. If not, see http://www.gnu.org/licenses/ .
*/
package org.exoplatform.task.dao.jpa;

import static org.exoplatform.task.dao.condition.Conditions.TASK_COWORKER;
import static org.exoplatform.task.dao.condition.Conditions.TASK_MANAGER;
import static org.exoplatform.task.dao.condition.Conditions.TASK_PARTICIPATOR;
import static org.exoplatform.task.dao.condition.Conditions.TASK_PROJECT;

import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;
import javax.persistence.PersistenceException;
import javax.persistence.Query;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Join;
import javax.persistence.criteria.JoinType;
import javax.persistence.criteria.Order;
import javax.persistence.criteria.Path;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.exoplatform.commons.utils.ListAccess;
import org.exoplatform.task.dao.OrderBy;
import org.exoplatform.task.dao.TaskHandler;
import org.exoplatform.task.dao.TaskQuery;
import org.exoplatform.task.dao.condition.SingleCondition;
import org.exoplatform.task.domain.Status;
import org.exoplatform.task.domain.Task;

/**
 * Created by The eXo Platform SAS
 * Author : Thibault Clement
 * tclement@exoplatform.com
 * 4/8/15
 */
public class TaskDAOImpl extends CommonJPADAO<Task, Long> implements TaskHandler {

  public TaskDAOImpl() {
  }

  /*@Override
  public void delete(Task entity) {
    EntityManager em = getEntityManager();
    Task task = em.find(Task.class, entity.getId());

    // Delete all task log relate to this task
    Query query = em.createNamedQuery("TaskChangeLog.removeChangeLogByTaskId");
    query.setParameter("taskId", entity.getId());
    query.executeUpdate();

    // Delete all comments of task
    query = em.createNamedQuery("Comment.deleteCommentOfTask");
    query.setParameter("taskId", entity.getId());
    query.executeUpdate();

    em.remove(task);
  }*/

  @Override
  public void updateStatus(Status stOld, Status stNew) {
    Query query = getEntityManager().createNamedQuery("Task.updateStatus");
    query.setParameter("status_old", stOld);
    query.setParameter("status_new", stNew);
    query.executeUpdate();
  }

  @Override
  public List<Task> findByUser(String user) {

    List<String> memberships = new ArrayList<String>();
    memberships.add(user);

    return  findAllByMembership(user, memberships);
  }

  public List<Task> findAllByMembership(String user, List<String> memberships) {

    Query query = getEntityManager().createNamedQuery("Task.findByMemberships", Task.class);
    query.setParameter("userName", user);
    query.setParameter("memberships", memberships);

    return cloneEntities(query.getResultList());
  }

  @Override
  public ListAccess<Task> findTasks(TaskQuery query) {
    return findEntities(query, Task.class);
  }

  @Override
  public <T> List<T> selectTaskField(TaskQuery query, String fieldName) {
    EntityManager em = getEntityManager();
    CriteriaBuilder cb = em.getCriteriaBuilder();
    CriteriaQuery q = cb.createQuery();

    Root<Task> task = q.from(Task.class);

    //List<Predicate> predicates = this.buildPredicate(query, task, cb);
    Predicate predicate = this.buildQuery(query.getCondition(), task, cb, q);

    if(predicate != null) {
      q.where(predicate);
    }

    //
    Path path = null;
    if (fieldName.indexOf('.') != -1) {
      String[] strs = fieldName.split("\\.");
      Join join = null;
      for (int i = 0; i < strs.length - 1; i++) {
        String s = strs[i];
        if (join == null) {
          join = task.join(s);
        } else {
          join = join.join(s);
        }
      }
      path = join.get(strs[strs.length - 1]);
    } else {
      path = task.get(fieldName);
    }
    if ("coworker".equals(fieldName)) {
      path = task.join(fieldName);
    }
    q.select(path).distinct(true);

    if(query.getOrderBy() != null && !query.getOrderBy().isEmpty()) {
      List<OrderBy> orderBies = query.getOrderBy();
      List<Order> orders = new ArrayList<Order>();
      for(OrderBy orderBy : orderBies) {
        if (!orderBy.getFieldName().equals(fieldName)) {
          continue;
        }
        Path p = task.get(orderBy.getFieldName());
        orders.add(orderBy.isAscending() ? cb.asc(p) : cb.desc(p));
      }
      if (!orders.isEmpty()) {
        q.orderBy(orders);
      }
    }

    final TypedQuery<T> selectQuery = em.createQuery(q);
    return cloneEntities(selectQuery.getResultList());
  }

  @Override
  public Task findTaskByActivityId(String activityId) {
    if (activityId == null || activityId.isEmpty()) {
      return null;
    }
    EntityManager em = getEntityManager();
    Query query = em.createNamedQuery("Task.findTaskByActivityId", Task.class);
    query.setParameter("activityId", activityId);
    try {
      return cloneEntity((Task) query.getSingleResult());
    } catch (PersistenceException e) {
      return null;
    }
  }

  @Override
  public void updateTaskOrder(long currentTaskId, Status newStatus, long[] orders) {
      int currentTaskIndex = -1;
      for (int i = 0; i < orders.length; i++) {
          if (orders[i] == currentTaskId) {
              currentTaskIndex = i;
              break;
          }
      }
      if (currentTaskIndex == -1) {
          return;
      }

      Task currentTask = find(currentTaskId);
      // Load coworker to avoid they will be deleted when save task
      currentTask.getCoworker();

      Task prevTask = null;
      Task nextTask = null;
      if (currentTaskIndex < orders.length - 1) {
          prevTask = find(orders[currentTaskIndex + 1]);
      }
      if (currentTaskIndex > 0) {
          nextTask = find(orders[currentTaskIndex - 1]);
      }

      int oldRank = currentTask.getRank();
      int prevRank = prevTask != null ? prevTask.getRank() : 0;
      int nextRank = nextTask != null ? nextTask.getRank() : 0;
      int newRank = prevRank + 1;
      if (newStatus != null && currentTask.getStatus().getId() != newStatus.getId()) {
          oldRank = 0;
          currentTask.setStatus(newStatus);
      }

      EntityManager em = getEntityManager();
      StringBuilder sql = null;

      if (newRank == 1 || oldRank == 0) {
          int increment = 1;
          StringBuilder exclude = new StringBuilder();
          if (nextRank == 0) {
              for (int i = currentTaskIndex - 1; i >= 0; i--) {
                  Task task = find(orders[i]);
                  if (task.getRank() > 0) {
                    break;
                  }
                  // Load coworker to avoid they will be deleted when save task
                  task.getCoworker();

                  task.setRank(newRank + currentTaskIndex - i);
                  update(task);
                  if (exclude.length() > 0) {
                      exclude.append(',');
                  }
                  exclude.append(task.getId());
                  increment++;
              }
          }
          //Update rank of tasks have rank >= newRank with rank := rank + increment
          sql = new StringBuilder("UPDATE TaskTask as ta SET ta.rank = ta.rank + ").append(increment)
                                .append(" WHERE ta.rank >= ").append(newRank);
          if (exclude.length() > 0) {
              sql.append(" AND ta.id NOT IN (").append(exclude.toString()).append(")");
          }

      } else if (oldRank < newRank) {
          //Update all task where oldRank < rank < newRank: rank = rank - 1
          sql = new StringBuilder("UPDATE TaskTask as ta SET ta.rank = ta.rank - 1")
                                .append(" WHERE ta.rank > ").append(oldRank)
                                .append(" AND ta.rank < ").append(newRank);
          newRank --;
      } else if (oldRank > newRank) {
          //Update all task where newRank <= rank < oldRank: rank = rank + 1
          sql = new StringBuilder("UPDATE TaskTask as ta SET ta.rank = ta.rank + 1")
                  .append(" WHERE ta.rank >= ").append(newRank)
                  .append(" AND ta.rank < ").append(oldRank);
          newRank ++;
      }

      if (sql != null && sql.length() > 0) {
          // Add common condition
          sql.append(" AND ta.completed = FALSE AND ta.status.id = ").append(currentTask.getStatus().getId());

          //TODO: This block code is temporary workaround because the update is require transaction
          EntityTransaction trans = em.getTransaction();
          boolean active = false;
          if (!trans.isActive()) {
            trans.begin();
            active = true;
          }

          em.createQuery(sql.toString()).executeUpdate();

          if (active) {
            trans.commit();
          }
      }
      currentTask.setRank(newRank);
      update(currentTask);
  }

  @Override
  public ListAccess<Task> findTasksByLabel(long labelId, List<Long> projectIds, String username, OrderBy orderBy) {
    TaskQuery query = new TaskQuery();
    if (projectIds != null) {
      query.setProjectIds(projectIds);      
    }
    if (orderBy != null) {
      query.setOrderBy(Arrays.asList(orderBy));      
    }
    if (labelId > 0) {
      query.setLabelIds(Arrays.asList(labelId));
    } else {
      query.setIsLabelOf(username);
    }
    return findTasks(query);
  }

  @Override
  public Set<String> getCoworker(long taskid) {
    TypedQuery<String> query = getEntityManager().createNamedQuery("Task.getCoworker", String.class);
    query.setParameter("taskid", taskid);
    List<String> tags = query.getResultList();
    return new HashSet<String>(tags);
  }

  protected Path buildPath(SingleCondition condition, Root<Task> root) {
    String field = condition.getField();
    
    Join join = null;
    Path path = null;
    if (TASK_PROJECT.equals(condition.getField())) {
      path = root.join("status", JoinType.LEFT).get("project");
    } else if (TASK_MANAGER.equals(condition.getField())) {
        path = root.join("status", JoinType.LEFT).join("project", JoinType.LEFT).join("manager", JoinType.LEFT);
    } else if (TASK_PARTICIPATOR.equals(condition.getField())) {
        path = root.join("status", JoinType.LEFT).join("project", JoinType.LEFT).join("participator", JoinType.LEFT);
    } else {
      if (field.indexOf('.') > 0) {
        String[] arr = field.split("\\.");
        for (int i = 0; i < arr.length - 1; i++) {
          String s = arr[i];
          if (join == null) {
            join = root.join(s, JoinType.INNER);
          } else {
            join = join.join(s, JoinType.INNER);
          }
        }
        field = arr[arr.length - 1];
      }    
      
      path = join == null ? root.get(field) : join.get(field);      
    }
    
    if (TASK_COWORKER.equals(field)) {
      path = root.join(field, JoinType.LEFT);
    }
    
    return path;
  }

  private static final ListAccess<Task> EMPTY = new ListAccess<Task>() {
    @Override
    public Task[] load(int index, int length) throws Exception, IllegalArgumentException {
      return new Task[0];
    }

    @Override
    public int getSize() throws Exception {
      return 0;
    }
  };
}


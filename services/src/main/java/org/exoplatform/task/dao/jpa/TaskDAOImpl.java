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

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;
import javax.persistence.PersistenceException;
import javax.persistence.Query;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Join;
import javax.persistence.criteria.JoinType;
import javax.persistence.criteria.Order;
import javax.persistence.criteria.Path;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import javax.persistence.criteria.Subquery;

import org.exoplatform.commons.persistence.impl.EntityManagerService;
import org.exoplatform.commons.persistence.impl.GenericDAOJPAImpl;
import org.exoplatform.task.dao.OrderBy;
import org.exoplatform.task.dao.TaskHandler;
import org.exoplatform.task.dao.TaskQuery;
import org.exoplatform.task.domain.Status;
import org.exoplatform.task.domain.Task;
import org.exoplatform.task.utils.TaskUtil;

/**
 * Created by The eXo Platform SAS
 * Author : Thibault Clement
 * tclement@exoplatform.com
 * 4/8/15
 */
public class TaskDAOImpl extends GenericDAOJPAImpl<Task, Long> implements TaskHandler {

  private EntityManagerService entityService;

  public TaskDAOImpl(EntityManagerService entityService) {
    this.entityService = entityService;
  }

  @Override
  public EntityManager getEntityManager() {
    return entityService.getEntityManager();
  }

  @Override
  public List<Task> findByProject(Long projectId) {
    EntityManager em = getEntityManager();
    Query query = em.createNamedQuery("Task.findTaskByProject", Task.class);
    query.setParameter("projectId", projectId);
    return query.getResultList();
  }

  @Override
  public List<Task> findByUser(String user) {

    List<String> memberships = new ArrayList<String>();
    memberships.add(user);

    return  findAllByMembership(user, memberships);
  }

  @Override
  public List<Task> findAllByMembership(String user, List<String> memberships) {

    Query query = getEntityManager().createNamedQuery("Task.findByMemberships", Task.class);
    query.setParameter("userName", user);
    query.setParameter("memberships", memberships);

    return query.getResultList();
  }

  @Override
  public List<Task> findByTag(String tag) {
    return null;
  }

  @Override
  public List<Task> findByTags(List<String> tags) {
    return null;
  }

  @Override
  public List<Task> findTaskByQuery(TaskQuery query) {
    EntityManager em = getEntityManager();
    CriteriaBuilder cb = em.getCriteriaBuilder();
    CriteriaQuery<Task> q = cb.createQuery(Task.class);
    
    Root<Task> task = q.from(Task.class);
    q.select(task);

    List<Predicate> predicates = new ArrayList<Predicate>();

    if(query.getTaskId() > 0) {
      predicates.add(cb.equal(task.get("id"), query.getTaskId()));
    }

    if (query.getTitle() != null && !query.getTitle().isEmpty()) {
      predicates.add(cb.like(task.<String>get("title"), "%" + query.getTitle() + "%"));
    }

    if (query.getDescription() != null && !query.getDescription().isEmpty()) {
      predicates.add(cb.like(task.<String>get("description"), '%' + query.getDescription() + '%'));
    }

    Predicate assignPred = null;
    if (query.getAssignee() != null && !query.getAssignee().isEmpty()) {
      assignPred = cb.like(task.<String>get("assignee"), '%' + query.getAssignee() + '%');
    }
    
    Predicate msPred = null;
    if (query.getMemberships() != null) {
      Subquery<Long> subm = q.subquery(Long.class);
      Root<Status> m = subm.from(Status.class);
      subm.select(m.<Long>get("id")).where(m.join("project").join("manager").in(query.getMemberships()));

      Subquery<Long> subp = q.subquery(Long.class);
      Root<Status> p = subp.from(Status.class);
      subp.select(p.<Long>get("id")).where(p.join("project").join("participator").in(query.getMemberships()));      
      
      msPred = cb.or(cb.in(task.get("status").get("id")).value(subm), cb.in(task.get("status").get("id")).value(subp));
    }

    Predicate projectPred = null;
    if (query.getProjectIds() != null) {
      if (query.getProjectIds().size() == 1 && query.getProjectIds().get(0) == 0) {
        projectPred = cb.isNotNull(task.get("status"));
      } else if (query.getProjectIds().isEmpty()) {
        return Collections.emptyList();
      } else {
        Subquery<Long> subp = q.subquery(Long.class);
        Root<Status> p = subp.from(Status.class);
        subp.select(p.<Long>get("id")).where(p.join("project").get("id").in(query.getProjectIds()));
        
        projectPred = cb.in(task.get("status").get("id")).value(subp);
      }             
    }

    List<Predicate> tmp = new LinkedList<Predicate>();
    for (String or : query.getOrFields()) {
      if (or.equals(TaskUtil.ASSIGNEE)) {
        tmp.add(assignPred);
      } 
      if (or.equals(TaskUtil.MEMBERSHIP)) {
        tmp.add(msPred);
      }
      if (or.equals(TaskUtil.PROJECT)) {
        tmp.add(projectPred);
      }
    }

    if (!tmp.isEmpty()) {
      predicates.add(cb.or(tmp.toArray(new Predicate[tmp.size()])));
    }
    
    if (!query.getOrFields().contains(TaskUtil.ASSIGNEE) && assignPred != null) {
      predicates.add(assignPred);
    }
    if (!query.getOrFields().contains(TaskUtil.MEMBERSHIP) && msPred != null) {
      predicates.add(msPred);      
    }
    if (!query.getOrFields().contains(TaskUtil.PROJECT) && projectPred != null) {
      predicates.add(projectPred);      
    }

    if(query.getKeyword() != null && !query.getKeyword().isEmpty()) {      
      List<Predicate> keyConditions = new LinkedList<Predicate>();
      Join<Task, String> tagJoin = task.<Task, String>join("tag", JoinType.LEFT);
      
      for (String k : query.getKeyword().split(" ")) {
        if (!(k = k.trim()).isEmpty()) {
          k = "%" + k.toLowerCase() + "%";
          keyConditions.add(cb.or(
                                  cb.like(cb.lower(task.<String>get("title")), k),
                                  cb.like(cb.lower(task.<String>get("description")), k),
                                  cb.like(cb.lower(task.<String>get("assignee")), k),
                                  cb.like(cb.lower(tagJoin), k)
                              ));
        }
      }
      predicates.add(cb.or(keyConditions.toArray(new Predicate[keyConditions.size()])));
    }

    if (query.getCompleted() != null) {
      if (query.getCompleted()) {
        predicates.add(cb.equal(task.get("completed"), query.getCompleted()));
      } else {
        predicates.add(cb.notEqual(task.get("completed"), !query.getCompleted()));
      }
    }
    
    if (query.getCalendarIntegrated() != null) {
      if (query.getCalendarIntegrated()) {
        predicates.add(cb.equal(task.get("calendarIntegrated"), query.getCalendarIntegrated()));
      } else {
        predicates.add(cb.notEqual(task.get("calendarIntegrated"), !query.getCalendarIntegrated()));
      }
    }
    
    if (query.getStartDate() != null) {
      predicates.add(cb.greaterThanOrEqualTo(task.<Date>get("endDate"), query.getStartDate()));
    }
    if (query.getEndDate() != null) {
      predicates.add(cb.lessThanOrEqualTo(task.<Date>get("startDate"), query.getEndDate()));
    }

    if(predicates.size() > 0) {
      Iterator<Predicate> it = predicates.iterator();
      Predicate p = it.next();
      while(it.hasNext()) {
        p = cb.and(p, it.next());
      }      
      q.where(p);
    }

    if(query.getOrderBy() != null && !query.getOrderBy().isEmpty()) {
      List<OrderBy> orderBies = query.getOrderBy();
      Order[] orders = new Order[orderBies.size()];
      for(int i = 0; i < orders.length; i++) {
        OrderBy orderBy = orderBies.get(i);
        Path p = task.get(orderBy.getFieldName());
        orders[i] = orderBy.isAscending() ? cb.asc(p) : cb.desc(p);
      }
      q.orderBy(orders);
    }

    return em.createQuery(q).getResultList();
  }

  @Override
  public List<Task> getIncomingTask(String username, OrderBy orderBy) {
    StringBuilder jql = new StringBuilder();
    jql.append("SELECT ta FROM Task ta LEFT JOIN ta.coworker cowoker ")
        .append("WHERE ta.status.id is null ")
        .append("AND (ta.assignee = :userName OR ta.createdBy = :userName OR cowoker = :userName)")
        .append(" AND ta.completed = FALSE");

    if(orderBy != null && !orderBy.getFieldName().isEmpty()) {
      jql.append(" ORDER BY ta.").append(orderBy.getFieldName()).append(" ").append(orderBy.isAscending() ?
          "ASC"
          : " DESC");
    }

    return getEntityManager().createQuery(jql.toString(), Task.class)
                             .setParameter("userName", username)
                             .getResultList();
  }

  @Override
  public List<Task> getToDoTask(String username, List<Long> projectIds, OrderBy orderBy, Date fromDueDate, Date toDueDate) {
    StringBuilder jql = new StringBuilder();
    jql.append("SELECT ta FROM Task ta LEFT JOIN ta.status st ")
        .append("WHERE ta.assignee = :userName ")
        .append("AND ta.completed = FALSE ");

    if (fromDueDate != null || toDueDate != null) {
      jql.append("AND ta.dueDate IS NOT NULL ");
    }

    if (projectIds != null && !projectIds.isEmpty()) {
      jql.append("AND ta.status.project.id IN (:projectIds) ");
    }
    
    if (fromDueDate != null) {
      jql.append("AND ta.dueDate >= :fromDueDate ");
    }
    if (toDueDate != null) {
      jql.append("AND ta.dueDate <= :toDueDate ");
    }

    if(orderBy != null && !orderBy.getFieldName().isEmpty()) {
      String fieldName = orderBy.getFieldName();
      if (fieldName.startsWith("status.")) {
        fieldName = fieldName.replace("status.", "st.");
      } else {
        fieldName = "ta." + fieldName;
      }
      jql.append(" ORDER BY ").append(fieldName).append(" ").append(orderBy.isAscending() ?
          "ASC"
          : " DESC");
    }

    Query query = getEntityManager()
        .createQuery(jql.toString(), Task.class);

    query.setParameter("userName", username);
    if (projectIds != null && !projectIds.isEmpty()) {
      query.setParameter("projectIds", projectIds);
    }
    if (fromDueDate != null) {
      query.setParameter("fromDueDate", fromDueDate);
    }
    if (toDueDate != null) {
      query.setParameter("toDueDate", toDueDate);
    }

    return query.getResultList();
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
      return (Task) query.getSingleResult();
    } catch (PersistenceException e) {
      return null;
    }
  }
  
  @Override
  public long getTaskNum(String userName, List<Long> projectIds) {
    if (userName == null && (projectIds == null || projectIds.isEmpty())) {
      return 0L;
    }
    
    StringBuilder jql = new StringBuilder();
    jql.append("SELECT count(*) FROM Task ta");
    if (userName != null) {
      jql.append(" LEFT JOIN ta.coworker cowoker ");      
    }
    jql.append(" WHERE ");
    if (userName != null) {
      jql.append("(ta.assignee = :userName OR ta.createdBy = :userName OR cowoker = :userName)");
      if (projectIds != null && !projectIds.isEmpty() && projectIds.get(0) != -2) {
        jql.append(" AND");
      }
    }
    boolean needParam = false;
    if (projectIds != null && !projectIds.isEmpty()) {
      if (projectIds.size() == 1 && projectIds.get(0) <= 0) {
        if (projectIds.get(0) == 0) {
          jql.append(" NOT ta.status is null");          
        } else if (projectIds.get(0) == -1) {
          jql.append(" ta.status is null");
        }
      } else {
        needParam = true;
        jql.append(" ta.status.project.id IN (:projectIds)");
      }
    }    

    Query query = getEntityManager()
        .createQuery(jql.toString());
    
    if (userName != null) {
      query.setParameter("userName", userName);
    }
    if (needParam) {
      query.setParameter("projectIds", projectIds);    
    }
    return (Long)query.getSingleResult();    
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
          sql = new StringBuilder("UPDATE Task as ta SET ta.rank = ta.rank + ").append(increment)
                                .append(" WHERE ta.rank >= ").append(newRank);
          if (exclude.length() > 0) {
              sql.append(" AND ta.id NOT IN (").append(exclude.toString()).append(")");
          }

      } else if (oldRank < newRank) {
          //Update all task where oldRank < rank < newRank: rank = rank - 1
          sql = new StringBuilder("UPDATE Task as ta SET ta.rank = ta.rank - 1")
                                .append(" WHERE ta.rank > ").append(oldRank)
                                .append(" AND ta.rank < ").append(newRank);
          newRank --;
      } else if (oldRank > newRank) {
          //Update all task where newRank <= rank < oldRank: rank = rank + 1
          sql = new StringBuilder("UPDATE Task as ta SET ta.rank = ta.rank + 1")
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
}


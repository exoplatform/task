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

import org.exoplatform.task.dao.OrderBy;
import org.exoplatform.task.dao.TaskHandler;
import org.exoplatform.task.dao.TaskQuery;
import org.exoplatform.task.domain.Task;
import org.exoplatform.task.service.jpa.DAOHandlerJPAImpl;

import javax.persistence.EntityManager;
import javax.persistence.Query;
import javax.persistence.criteria.*;

import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

/**
 * Created by The eXo Platform SAS
 * Author : Thibault Clement
 * tclement@exoplatform.com
 * 4/8/15
 */
public class TaskDAOImpl extends GenericDAOImpl<Task, Long> implements TaskHandler {

  public TaskDAOImpl(DAOHandlerJPAImpl taskServiceJPAImpl) {
    super(taskServiceJPAImpl);
  }

  @Override
  public List<Task> findByProject(Long projectId) {
    EntityManager em = daoHandler.getEntityManager();
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

    Query query = daoHandler.getEntityManager().createNamedQuery("Task.findByMemberships", Task.class);
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
    EntityManager em = daoHandler.getEntityManager();
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

    if (query.getAssignee() != null && !query.getAssignee().isEmpty()) {
      predicates.add(cb.like(task.<String>get("assignee"), '%' + query.getAssignee() + '%'));
    }

    if (query.getProjectId() != null) {
      if(query.getProjectId() > 0) {
        predicates.add(cb.equal(task.get("status").get("project").get("id"), query.getProjectId()));
      } else if (query.getProjectId() == 0) {
        predicates.add(cb.isNotNull(task.get("status")));
      }      
    }

    if(query.getKeyword() != null && !query.getKeyword().isEmpty()) {
      String keyword = "%" + query.getKeyword() + "%";
      predicates.add(
          cb.or(
              cb.like(task.<String>get("title"), keyword),
              cb.like(task.<String>get("description"), keyword),
              cb.like(task.<String>get("assignee"), keyword)
          )
      );
    }

    if (query.getCompleted() != null) {
      if (query.getCompleted()) {
        predicates.add(cb.equal(task.get("completed"), query.getCompleted()));
      } else {
        predicates.add(cb.notEqual(task.get("completed"), !query.getCompleted()));
      }
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
        .append("WHERE (ta.status.id is null or ta.status.id = 0) ")
        .append("AND (ta.assignee = :userName OR ta.createdBy = :userName OR cowoker = :userName)")
        .append(" AND ta.completed != TRUE");

    if(orderBy != null && !orderBy.getFieldName().isEmpty()) {
      jql.append(" ORDER BY ta.").append(orderBy.getFieldName()).append(" ").append(orderBy.isAscending() ?
          "ASC"
          : " DESC");
    }

    return daoHandler
        .getEntityManager()
        .createQuery(jql.toString(), Task.class)
        .setParameter("userName", username)
        .getResultList();
  }

  @Override
  public List<Task> getToDoTask(String username, OrderBy orderBy, Date fromDueDate, Date toDueDate) {
    StringBuilder jql = new StringBuilder();
    jql.append("SELECT ta FROM Task ta ")
        .append("WHERE ta.assignee = :userName ")
        .append("AND ta.completed != TRUE ")
        .append("AND ta.dueDate != NULL ");

    if (fromDueDate != null) {
      jql.append("AND ta.dueDate >= :fromDueDate ");
    }
    if (toDueDate != null) {
      jql.append("AND ta.dueDate <= :toDueDate ");
    }

    if(orderBy != null && !orderBy.getFieldName().isEmpty()) {
      jql.append(" ORDER BY ta.").append(orderBy.getFieldName()).append(" ").append(orderBy.isAscending() ?
          "ASC"
          : " DESC");
    }

    Query query = daoHandler
        .getEntityManager()
        .createQuery(jql.toString(), Task.class);

    query.setParameter("userName", username);
    if (fromDueDate != null) {
      query.setParameter("fromDueDate", fromDueDate);
    }
    if (toDueDate != null) {
      query.setParameter("toDueDate", toDueDate);
    }

    return query.getResultList();
  }
  
  @Override
  public long getTaskNum(String userName, Long projectId) {
    if (userName == null && projectId == null) {
      return -1L;
    }
    
    StringBuilder jql = new StringBuilder();
    jql.append("SELECT count(*) FROM Task ta");
    if (userName != null) {
      jql.append(" LEFT JOIN ta.coworker cowoker ");      
    }
    jql.append(" WHERE ");
    if (userName != null) {
      jql.append("(ta.assignee = :userName OR ta.createdBy = :userName OR cowoker = :userName)");
      if (projectId != null) {
        jql.append(" AND");
      }
    }
    if (projectId != null) {
      if (projectId != 0) {
        jql.append(" ta.status.project.id = :projectId");
      } else {
        jql.append(" NOT ta.status is null");
      }
    }    

    Query query = daoHandler.getEntityManager()
        .createQuery(jql.toString());
    
    if (userName != null) {
      query.setParameter("userName", userName);
    }
    if (projectId != null && projectId != 0) {
      query.setParameter("projectId", projectId);    
    }
    return (Long)query.getSingleResult();    
  }
}


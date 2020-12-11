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

import java.util.*;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import javax.persistence.EntityManager;
import javax.persistence.Query;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.JoinType;
import javax.persistence.criteria.Path;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import org.apache.commons.lang.StringUtils;

import org.exoplatform.commons.utils.ListAccess;
import org.exoplatform.social.core.jpa.storage.entity.IdentityEntity;
import org.exoplatform.task.dao.OrderBy;
import org.exoplatform.task.dao.ProjectHandler;
import org.exoplatform.task.dao.ProjectQuery;
import org.exoplatform.task.dao.condition.Conditions;
import org.exoplatform.task.dao.condition.SingleCondition;
import org.exoplatform.task.domain.Project;
import org.exoplatform.task.domain.UserSetting;

/**
 * Created by The eXo Platform SAS
 * Author : Thibault Clement
 * tclement@exoplatform.com
 * 4/10/15
 */
public class ProjectDAOImpl extends CommonJPADAO<Project, Long> implements ProjectHandler {

  private static final Logger LOG = Logger.getLogger("ProjectDAOImpl");

  public ProjectDAOImpl() {
  }

  @Override
  public Project update(Project entity) {
    return cloneEntity(super.update(entity));
  }

  @Override
  public void delete(Project entity) {
    Project p = getEntityManager().find(Project.class, entity.getId());
    if (p != null) {           
      super.delete(p);
    }
  }

  @Override
  public Project removeProject(long projectId, boolean deleteChild) {
    Project p = getEntityManager().find(Project.class, projectId);
    if (p == null) {
      return null;
    }
    if (!deleteChild && p.getChildren() != null) {
      for(Project pj : p.getChildren()) {
        pj.setParent(p.getParent());
        getEntityManager().persist(pj);
      }
      p.getChildren().clear();
    }

    if (p.getHiddenOn() != null) {
      for(UserSetting u : p.getHiddenOn()) {
        u.getHiddenProjects().remove(p);
        getEntityManager().persist(u);
      }
    }

    super.delete(p);
    return p;
  }

  @Override
  public ListAccess<Project> findSubProjects(Project project) {
    ProjectQuery query = new ProjectQuery();
    if (project != null) {
      query.setParent(project.getId());
    } else {
      query.setParent(null);
    }
    return findProjects(query);
  }

  @Override
  public ListAccess<Project> findAllByMembershipsAndKeyword(List<String> memberships, String keyword, OrderBy order) {
    if(order == null && (StringUtils.isBlank(keyword) || !keyword.trim().contains(" "))) {
        // order == null && keyword is Empty
        TypedQuery<Project> selectQuery = null;
        TypedQuery<Long> countQuery = null;
        if((memberships == null || memberships.isEmpty()) && StringUtils.isBlank(keyword)) {
          // memberships == null, keywork == null, order == null
          // => return all projects
          selectQuery = getEntityManager().createNamedQuery("TaskProject.findAllProjects", Project.class);
          countQuery = getEntityManager().createNamedQuery("TaskProject.countAllProjects", Long.class);
        } else if(memberships == null || memberships.isEmpty()) {
          // memberships == null, keywork != null, order == null
          // => search projects by keyword
          selectQuery = getEntityManager().createNamedQuery("TaskProject.findProjectsByKeyword", Project.class);
          selectQuery.setParameter("name", keyword);

          countQuery = getEntityManager().createNamedQuery("TaskProject.countProjectsByKeyword", Long.class);
          countQuery.setParameter("name", keyword);
        } else if(StringUtils.isBlank(keyword)) {
          HashSet<String> membershipsSet = new HashSet<String>(memberships);

          selectQuery = getEntityManager().createNamedQuery("TaskProject.findProjectsByMemberships", Project.class);
          selectQuery.setParameter("memberships", membershipsSet);

          countQuery = getEntityManager().createNamedQuery("TaskProject.countProjectsByMemberships", Long.class);
          countQuery.setParameter("memberships", membershipsSet);
        } else {
          HashSet<String> membershipsSet = new HashSet<String>(memberships);

          selectQuery = getEntityManager().createNamedQuery("TaskProject.findProjectsByMembershipsByKeyword", Project.class);
          selectQuery.setParameter("memberships", membershipsSet);
          selectQuery.setParameter("name", keyword);

          countQuery = getEntityManager().createNamedQuery("TaskProject.countProjectsByMembershipsByKeyword", Long.class);
          countQuery.setParameter("memberships", membershipsSet);
          countQuery.setParameter("name", keyword);
        }

        return new JPAQueryListAccess<Project>(Project.class, countQuery, selectQuery);
    }
    ProjectQuery query = new ProjectQuery();
    query.setMembership(memberships);
    query.setKeyword(keyword);
    if (order != null) {
      query.setOrderBy(Arrays.asList(order));      
    }
    return findProjects(query);
  }

  @Override
  public ListAccess<Project> findProjects(ProjectQuery query) {
    return findEntities(query, Project.class);
  }

  protected Path buildPath(SingleCondition condition, Root<Project> root) {
    String field = condition.getField();
    
    if (Conditions.MANAGER.equals(field) || Conditions.PARTICIPATOR.equals(field)) {
      return root.join(field, JoinType.LEFT);     
    }
    return super.buildPath(condition, root);
  }

 @Override
  public List<Project> findCollaboratedProjects(String userName, String keyword,int offset ,int limit) {
   Query q = getEntityManager().createNativeQuery(
           "SELECT DISTINCT(p.PROJECT_ID) FROM TASK_PROJECTS p \n" +
                   "where \n" +
                   "lower(p.NAME) LIKE lower(concat('%', :keyword, '%'))\n" +
                   "AND\n" +
                   "(p.PROJECT_ID in (\n" +
                   "SELECT distinct(ts.PROJECT_ID)\n" +
                   "FROM TASK_STATUS AS ts\n" +
                   "WHERE ts.STATUS_ID IN\n" +
                   "   ( \n" +
                   "SELECT task.STATUS_ID\n" +
                   "FROM TASK_TASKS AS task\n" +
                   "WHERE task.COMPLETED = false\n" +
                   "AND (task.ASSIGNEE = :userName\n" +
                   "  OR task.TASK_ID IN (\n" +
                   "SELECT com.TASK_ID\n" +
                   "FROM TASK_COMMENTS AS com\n" +
                   "WHERE com.TASK_ID = task.TASK_ID\n" +
                   "AND (com.AUTHOR = :userName\n" +
                   " OR com.COMMENT_ID IN (\n" +
                   "SELECT cmention.COMMENT_ID\n" +
                   "FROM TASK_COMMENT_MENTIONED_USERS AS cmention\n" +
                   "WHERE com.COMMENT_ID = cmention.COMMENT_ID\n" +
                   "AND cmention.MENTIONED_USERS = :userName\n" +
                   ")\n" +
                   ")\n" +
                   " )\n" +
                   "         OR task.TASK_ID IN (\n" +
                   "SELECT coworker.TASK_ID\n" +
                   "            FROM TASK_TASK_COWORKERS AS coworker\n" +
                   "            WHERE coworker.TASK_ID = task.TASK_ID\n" +
                   " AND coworker.COWORKER = :userName\n" +
                   "         )\n" +
                   "    )\n" +
                   " )\n" +
                   "))");
     q.setParameter("keyword", keyword);
     q.setParameter("userName", userName);
     if (limit > 0) {
         q.setFirstResult(offset).setMaxResults(limit);
     }
   List<Object> ids = q.getResultList();
   if(ids.isEmpty()) {
     return new ArrayList<Project>();
   }
   List<Long> idsLong = ids.stream().map(i -> Long.parseLong(i.toString())).collect(Collectors.toList());
   return findIdentitiesByIDs(idsLong);
 }

    @Override
    public List<Project> findNotEmptyProjects(List<String> memberships, String keyword,int offset ,int limit) {
   Query q = getEntityManager().createNativeQuery(
           "SELECT DISTINCT(p.PROJECT_ID) FROM TASK_PROJECTS p \n" +
                   "where \n" +
                   "lower(p.NAME) LIKE lower(concat('%', :keyword , '%'))\n" +
                   "AND\n" +
                   "(p.PROJECT_ID in (\n" +
                   "SELECT man.PROJECT_ID\n" +
                   "            FROM TASK_PROJECT_MANAGERS AS man\n" +
                   "            WHERE man.MANAGER IN (:memberships) \t\t    \n" +
                   ") or \n" +
                   "p.PROJECT_ID in (\n" +
                   "SELECT part.PROJECT_ID\n" +
                   "            FROM TASK_PROJECT_PARTICIPATORS AS part\n" +
                   "            WHERE part.PARTICIPATOR IN (:memberships) \n" +
                   ") ) AND p.PROJECT_ID in (\n" +
                   "SELECT distinct(ts.PROJECT_ID)\n" +
                   "FROM TASK_STATUS AS ts\n" +
                   "WHERE ts.STATUS_ID IN(\n" +
                   "SELECT tcol.STATUS_ID FROM  \n" +
                   "(SELECT task.STATUS_ID AS STATUS_ID , COUNT(*) \n" +
                   "FROM TASK_TASKS AS task \n" +
                   "WHERE task.COMPLETED = false\n" +
                   "GROUP BY task.STATUS_ID\n" +
                   "HAVING COUNT(*)> 0) AS tcol\n" +
                   ") \n" +
                   ")\n");
   q.setParameter("keyword", keyword);
   q.setParameter("memberships", memberships);
   if (limit > 0) {
       q.setFirstResult(offset).setMaxResults(limit);
   }
   List<Object> ids = q.getResultList();
   if(ids.isEmpty()) {
     return new ArrayList<Project>();
   }
   List<Long> idsLong = ids.stream().map(i -> Long.parseLong(i.toString())).collect(Collectors.toList());
   return findIdentitiesByIDs(idsLong);
  }

 @Override
  public int countCollaboratedProjects(String userName, String keyword) {
   Query q = getEntityManager().createNativeQuery(
           "SELECT DISTINCT(p.PROJECT_ID) FROM TASK_PROJECTS p \n" +
                   "where \n" +
                   "lower(p.NAME) LIKE lower(concat('%', :keyword, '%'))\n" +
                   "AND\n" +
                   "(p.PROJECT_ID in (\n" +
                   "SELECT distinct(ts.PROJECT_ID)\n" +
                   "FROM TASK_STATUS AS ts\n" +
                   "WHERE ts.STATUS_ID IN\n" +
                   "   ( \n" +
                   "SELECT task.STATUS_ID\n" +
                   "FROM TASK_TASKS AS task\n" +
                   "WHERE task.COMPLETED = false\n" +
                   "AND (task.ASSIGNEE = :userName\n" +
                   "  OR task.TASK_ID IN (\n" +
                   "SELECT com.TASK_ID\n" +
                   "FROM TASK_COMMENTS AS com\n" +
                   "WHERE com.TASK_ID = task.TASK_ID\n" +
                   "AND (com.AUTHOR = :userName\n" +
                   " OR com.COMMENT_ID IN (\n" +
                   "SELECT cmention.COMMENT_ID\n" +
                   "FROM TASK_COMMENT_MENTIONED_USERS AS cmention\n" +
                   "WHERE com.COMMENT_ID = cmention.COMMENT_ID\n" +
                   "AND cmention.MENTIONED_USERS = :userName\n" +
                   ")\n" +
                   ")\n" +
                   " )\n" +
                   "         OR task.TASK_ID IN (\n" +
                   "SELECT coworker.TASK_ID\n" +
                   "            FROM TASK_TASK_COWORKERS AS coworker\n" +
                   "            WHERE coworker.TASK_ID = task.TASK_ID\n" +
                   " AND coworker.COWORKER = :userName\n" +
                   "         )\n" +
                   "    )\n" +
                   " )\n" +
                   "))");
     q.setParameter("keyword", keyword);
     q.setParameter("userName", userName);
     int result = Integer.parseInt(q.getSingleResult().toString());
      return result ;
 }

    @Override
    public int countNotEmptyProjects(List<String> memberships, String keyword) {
   Query q = getEntityManager().createNativeQuery(
           "SELECT DISTINCT(p.PROJECT_ID) FROM TASK_PROJECTS p \n" +
                   "where \n" +
                   "lower(p.NAME) LIKE lower(concat('%', :keyword , '%'))\n" +
                   "AND\n" +
                   "(p.PROJECT_ID in (\n" +
                   "SELECT man.PROJECT_ID\n" +
                   "            FROM TASK_PROJECT_MANAGERS AS man\n" +
                   "            WHERE man.MANAGER IN (:memberships) \t\t    \n" +
                   ") or \n" +
                   "p.PROJECT_ID in (\n" +
                   "SELECT part.PROJECT_ID\n" +
                   "            FROM TASK_PROJECT_PARTICIPATORS AS part\n" +
                   "            WHERE part.PARTICIPATOR IN (:memberships) \n" +
                   ") ) AND p.PROJECT_ID in (\n" +
                   "SELECT distinct(ts.PROJECT_ID)\n" +
                   "FROM TASK_STATUS AS ts\n" +
                   "WHERE ts.STATUS_ID IN(\n" +
                   "SELECT tcol.STATUS_ID FROM  \n" +
                   "(SELECT task.STATUS_ID AS STATUS_ID , COUNT(*) \n" +
                   "FROM TASK_TASKS AS task \n" +
                   "WHERE task.COMPLETED = false\n" +
                   "GROUP BY task.STATUS_ID\n" +
                   "HAVING COUNT(*)> 0) AS tcol\n" +
                   ") \n" +
                   ")\n");
   q.setParameter("keyword", keyword);
   q.setParameter("memberships", memberships);
        int result = Integer.parseInt(q.getSingleResult().toString());
        return result ;
  }

  protected List<Project> findIdentitiesByIDs(List<Long> ids) {
    TypedQuery<Project> query = getEntityManager().createNamedQuery("TaskProject.findProjectsByIDs", Project.class);
      query.setParameter("ids", ids);
      return query.getResultList();
  }

  public <T> List<T> selectProjectField(ProjectQuery query, String fieldName) {
    EntityManager em = getEntityManager();
    CriteriaBuilder cb = em.getCriteriaBuilder();
    CriteriaQuery q = cb.createQuery();

    Root<Project> project = q.from(Project.class);
    Predicate predicate = this.buildQuery(query.getCondition(), project, cb, q);

    if(predicate != null) {
      q.where(predicate);
    }
    q.select(project.join(fieldName)).distinct(true);    

    final TypedQuery<T> selectQuery = em.createQuery(q);
    return cloneEntities(selectQuery.getResultList());
  }
}

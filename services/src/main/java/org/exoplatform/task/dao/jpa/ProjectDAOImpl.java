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
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Logger;

import javax.persistence.EntityManager;
import javax.persistence.Query;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.JoinType;
import javax.persistence.criteria.Path;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import org.exoplatform.task.dao.OrderBy;
import org.exoplatform.task.dao.ProjectHandler;
import org.exoplatform.task.domain.Project;

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
    Project p = getEntityManager().find(Project.class, entity.getId());
    //TODO: this is workaround to keep status when update project,
    // we should remove cascade ALL on the field "status" of Project
    entity.setStatus(p.getStatus());
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

    super.delete(p);
    return p;
  }

  @Override
  public List<Project> findSubProjects(Project project) {
    EntityManager em = getEntityManager();
    Query query = em.createNamedQuery(project != null ? "Project.findSubProjects" : "Project.getRootProjects");
    if(project != null) {
      query.setParameter("projectId", project.getId());
    }
    return cloneEntities(query.getResultList());
  }

  @Override
  public List<Project> findSubProjectsByMemberships(Project project, List<String> memberships) {
    EntityManager em = getEntityManager();
    Query query = em.createNamedQuery(project != null ?
        "Project.findSubProjectsByMemberships"
        : "Project.findRootProjectsByMemberships");
    if(project != null) {
      query.setParameter("projectId", project.getId());
    }
    query.setParameter("memberships", memberships);
    return cloneEntities(query.getResultList());
  }

  @Override
  public List<Project> findAllByMemberships(List<String> memberships) {
    Query query = getEntityManager().createNamedQuery("Project.findAllByMembership", Project.class);
    query.setParameter("memberships", memberships);

    return cloneEntities(query.getResultList());
  }

  @Override
  public List<Project> findAllByMembershipsAndKeyword(List<String> memberships, String keyword, OrderBy order) {
    EntityManager em = getEntityManager();
    CriteriaBuilder cb = em.getCriteriaBuilder();
    CriteriaQuery<Project> q = cb.createQuery(Project.class);

    Root<Project> project = q.from(Project.class);
    q.select(project);

    List<Predicate> predicates = new ArrayList<Predicate>();
    predicates.add(cb.or(project.join("manager", JoinType.LEFT).in(memberships), project.join("participator", JoinType.LEFT).in(memberships)));
    //
    if(keyword != null && !keyword.isEmpty()) {
      List<Predicate> keyConditions = new LinkedList<Predicate>();
      for (String k : keyword.split(" ")) {
        if (!(k = k.trim()).isEmpty()) {
          k = "%" + k.toLowerCase() + "%";
          keyConditions.add(cb.like(cb.lower(project.<String>get("name")), k));          
        }
      }
      predicates.add(cb.or(keyConditions.toArray(new Predicate[keyConditions.size()])));        
    }

    if(predicates.size() > 0) {
      Iterator<Predicate> it = predicates.iterator();
      Predicate p = it.next();
      while(it.hasNext()) {
        p = cb.and(p, it.next());
      }
      q.where(p);
    }
    
    if(order != null) {
      Path p = project.get(order.getFieldName());      
      q.orderBy(order.isAscending() ? cb.asc(p) : cb.desc(p));
    }
    
    return cloneEntities(em.createQuery(q).getResultList());
  }
}


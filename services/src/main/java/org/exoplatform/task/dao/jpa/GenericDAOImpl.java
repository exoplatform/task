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

import org.exoplatform.task.dao.GenericDAO;
import org.exoplatform.task.domain.Status;
import org.exoplatform.task.service.jpa.DAOHandlerJPAImpl;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Query;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;

import java.io.Serializable;
import java.lang.reflect.ParameterizedType;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

/**
 * Created by The eXo Platform SAS
 * Author : Thibault Clement
 * tclement@exoplatform.com
 * 4/6/15
 */
public class GenericDAOImpl<E, ID extends Serializable> implements GenericDAO<E, ID> {

  private static final Logger LOG = Logger.getLogger("GenericDAOImpl");

  protected Class entityClass;

  protected EntityManagerFactory entityManagerFactory;

  protected DAOHandlerJPAImpl daoHandler;

  public GenericDAOImpl(DAOHandlerJPAImpl daoHandlerJPA) {
    daoHandler = daoHandlerJPA;
    ParameterizedType genericSuperclass = (ParameterizedType) getClass().getGenericSuperclass();
    this.entityClass = (Class) genericSuperclass.getActualTypeArguments()[0];
  }

  @Override
  public Long count() {
    CriteriaBuilder cb = daoHandler.getEntityManager().getCriteriaBuilder();
    CriteriaQuery<Long> query = cb.createQuery(Long.class);

    Root<E> entity = query.from(entityClass);

    //Selecting the count
    query.select(cb.count(entity));

    return daoHandler.getEntityManager().createQuery(query).getSingleResult();
  }

  @Override
  public E find(ID id) {
    return (E) daoHandler.getEntityManager().find(entityClass, id);
  }

  @Override
  public List<E> findAll() {
    ParameterizedType genericSuperclass = (ParameterizedType) getClass().getGenericSuperclass();
    this.entityClass = (Class) genericSuperclass.getActualTypeArguments()[0];

    CriteriaBuilder cb = daoHandler.getEntityManager().getCriteriaBuilder();
    CriteriaQuery<E> query = cb.createQuery(entityClass);

    Root<E> entity = query.from(entityClass);

    //Selecting the entity
    query.select(entity);

    return daoHandler.getEntityManager().createQuery(query).getResultList();
  }
  
  public List<E> findByNamedQuery(String query, Map<String, Object> params) {
    EntityManager em = daoHandler.getEntityManager();
    TypedQuery<E> q = em.createNamedQuery(query, entityClass);
    if (params != null) {
      for (Map.Entry<String, Object> p : params.entrySet()) {
        q.setParameter(p.getKey(), p.getValue());      
      }      
    }
    return q.getResultList();
  }

  @Override
  public E create(E entity) {
    daoHandler.getEntityManager().persist(entity);
    return entity;
  }

  @Override
  public void createAll(List<E> entities) {
    for (E entity : entities) {
      daoHandler.getEntityManager().persist(entity);
    }
  }

  @Override
  public E update(E entity) {
    daoHandler.getEntityManager().merge(entity);
    return entity;
  }

  @Override
  public void updateAll(List<E> entities) {
    for (E entity : entities) {
      daoHandler.getEntityManager().merge(entity);
    }
  }

  @Override
  public void delete(E entity) {
    daoHandler.getEntityManager().remove(entity);
  }

  @Override
  public void deleteAll(List<E> entities) {
    for (E entity : entities) {
      daoHandler.getEntityManager().remove(entity);
    }
  }

  @Override
  public void deleteAll() {
    List<E> entities = findAll();
    for (E entity : entities) {
      daoHandler.getEntityManager().remove(entity);
    }
  }
}


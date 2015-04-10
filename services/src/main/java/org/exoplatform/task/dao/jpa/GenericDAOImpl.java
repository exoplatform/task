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
import org.exoplatform.task.factory.ExoEntityManagerFactory;

import javax.annotation.PostConstruct;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;
import java.io.Serializable;
import java.lang.reflect.ParameterizedType;
import java.util.List;
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
  private EntityManager entityManager;

  @PostConstruct
  public void initDAO() {
    //In case of no EE container (Tomcat) we cannot rely on @PersistenceContext to initialize the EntityManager
    //We will use our own EntityManagerFactory
    entityManagerFactory = ExoEntityManagerFactory.getEntityManagerFactory();
    ParameterizedType genericSuperclass = (ParameterizedType) getClass().getGenericSuperclass();
    this.entityClass = (Class) genericSuperclass.getActualTypeArguments()[0];
  }

  // *****************************
  //
  // Transaction management
  //
  // *****************************

  @Override
  public void createEntityManager() {
    entityManager = entityManagerFactory.createEntityManager();
  }

  @Override
  public void beginTransaction() {
    createEntityManager();
    entityManager.getTransaction().begin();
  }

  @Override
  public void commit() {
    entityManager.getTransaction().commit();
  }

  @Override
  public void rollback() {
    entityManager.getTransaction().rollback();
  }

  @Override
  public void closeTransaction() {
    entityManager.close();
  }

  @Override
  public void commitAndCloseTransaction() {
    commit();
    closeTransaction();
  }

  @Override
  public void flush() {
    entityManager.flush();
  }

  @Override
  public void joinTransaction() {
    createEntityManager();
    entityManager.joinTransaction();
  }


  // *****************************
  //
  // Access Object Methods
  //
  // *****************************

  @Override
  public Long count() {
    CriteriaBuilder cb = entityManager.getCriteriaBuilder();
    CriteriaQuery<Long> query = cb.createQuery(Long.class);

    Root<E> entity = query.from(entityClass);

    //Selecting the count
    query.select(cb.count(entity));

    return entityManager.createQuery(query).getSingleResult();
  }

  @Override
  public E find(ID id) {
    return (E) entityManager.find(entityClass, id);
  }

  @Override
  public List<E> findAll() {
    CriteriaBuilder cb = entityManager.getCriteriaBuilder();
    CriteriaQuery<E> query = cb.createQuery(entityClass);

    Root<E> entity = query.from(entityClass);

    //Selecting the entity
    query.select(entity);

    return entityManager.createQuery(query).getResultList();
  }

  @Override
  public E create(E entity) {
    entityManager.persist(entity);
    return entity;
  }

  @Override
  public void createAll(List<E> entities) {
    for (E entity : entities) {
      entityManager.persist(entity);
    }
  }

  @Override
  public E update(E entity) {
    entityManager.merge(entity);
    return entity;
  }

  @Override
  public void updateAll(List<E> entities) {
    for (E entity : entities) {
      entityManager.merge(entity);
    }
  }

  @Override
  public void delete(E entity) {
    entityManager.remove(entity);
  }

  @Override
  public void deleteAll(List<E> entities) {
    for (E entity : entities) {
      entityManager.remove(entity);
    }
  }
}


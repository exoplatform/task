package org.exoplatform.task.dao;

import java.io.Serializable;
import java.util.List;

/**
 * Created by The eXo Platform SAS
 * Author : Thibault Clement
 * tclement@exoplatform.com
 * 4/6/15
 */
public interface GenericDAO<E, ID extends Serializable> {

  void createEntityManager();

  void beginTransaction();

  void commit();

  void rollback();

  void closeTransaction();

  void commitAndCloseTransaction();

  void flush();

  void joinTransaction();

  Long count();

  E find(ID id);

  List<E> findAll();

  E create(E entity);

  void createAll(List<E> entities);

  E update(E entity);

  void updateAll(List<E> entities);

  void delete(E entity);

  void deleteAll(List<E> entities);

}

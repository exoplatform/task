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

  /**
   * Start a transaction.
   */
  void beginTransaction();

  /**
   * Commit the modification done in the transaction.
   */
  void commit();

  /**
   * Rollback the modification done in the transaction.
   */
  void rollback();

  /**
   * Close the transaction.
   */
  void closeTransaction();

  /**
   * Commit the modification and close the transaction.
   */
  void commitAndCloseTransaction();

  /**
   * Flush changes in the persistence context to the datasource.
   */
  void flush();

  /**
   * Join an existing transaction
   */
  void joinTransaction();

  /**
   * Get the number of entities with the specified type and id from the datasource.
   *
   * @return the entity, null if none is found
   */
  Long count();

  /**
   * Get the entity with the specified type and id from the database.
   *
   * @return the entity, null if none is found
   */
  E find(ID id);

  /**
   * Get a list of all object of the specified type from the datasource.
   *
   * @return a list of entities
   */
  List<E> findAll();

  /**
   * Insert a new entity.
   * If the entity already exist, use update(E entity) instead
   *
   * @return the new entity
   */
  E create(E entity);

  /**
   * Insert a list of new entities in the persistence context.
   * If the entities already exist, use update(E entity) instead
   */
  void createAll(List<E> entities);

  /**
   * Update the entity in the persistence context.
   * If the entity does not already exist, use create(E entity) instead
   *
   * @return the just created entity
   */
  E update(E entity);

  /**
   * Update the entity in the persistence context.
   * If the entity does not already exist, use create(E entity) instead
   *
   * @return the just created entity
   */
  void updateAll(List<E> entities);

  /**
   * Delete the specified entity from the persistence context.
   */
  void delete(E entity);

  /**
   * Remove all of the specified entities from the persistence context.
   */
  void deleteAll(List<E> entities);

  /**
   * Remove all of the entities from the persistence context.
   */
  void deleteAll();

}

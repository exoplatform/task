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
package org.exoplatform.task.service.jpa;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.logging.Logger;

import javax.inject.Singleton;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;

import org.exoplatform.container.ExoContainer;
import org.exoplatform.container.component.ComponentRequestLifecycle;
import org.exoplatform.task.dao.jpa.CommentDAOImpl;
import org.exoplatform.task.dao.jpa.ProjectDAOImpl;
import org.exoplatform.task.dao.jpa.StatusDAOImpl;
import org.exoplatform.task.dao.jpa.TaskDAOImpl;
import org.exoplatform.task.domain.Task;
import org.exoplatform.task.factory.ExoEntityManagerFactory;
import org.exoplatform.task.service.GroupByService;
import org.exoplatform.task.service.TaskService;
import org.exoplatform.task.service.impl.AbstractTaskService;
import org.exoplatform.task.service.impl.GroupByProject;
import org.exoplatform.task.service.impl.GroupByStatus;
import org.exoplatform.task.service.impl.GroupByTag;

/**
 * Created by The eXo Platform SAS
 * Author : Thibault Clement
 * tclement@exoplatform.com
 * 4/8/15
 */
@Singleton
public class TaskServiceJPAImpl extends AbstractTaskService implements TaskService, ComponentRequestLifecycle {

  private static final Logger LOG = Logger.getLogger("TaskServiceJPATestImpl");
  private final List<GroupByService> groupByServices;

  private ThreadLocal<EntityManager> entityManager = new ThreadLocal<EntityManager>();
  
  public TaskServiceJPAImpl() {
    pHandler = new ProjectDAOImpl(this);
    tHandler = new TaskDAOImpl(this);
    cHandler = new CommentDAOImpl(this);
    sHandler = new StatusDAOImpl(this);
    
    this.groupByServices = new ArrayList<GroupByService>();
    this.groupByServices.add(new GroupByStatus(this));
    this.groupByServices.add(new GroupByProject(this));
    this.groupByServices.add(new GroupByTag(this));
  }

  public EntityManager getEntityManager() {
    return entityManager.get();
  }
  
  @Override
  public void save(Task task) {
    tHandler.update(task);
  }

  //To add in interface
  public void update(Task task) {
    Task persistedTask = tHandler.find(task.getId());
    persistedTask.setTitle(task.getTitle());
    //TO DO: add all set methods...
    tHandler.update(persistedTask);
  }

  @Override
  public Task findTaskById(long id) {
    Task task = tHandler.find(id);
    return task;
  }

  @Override
  public List<Task> findAllTask() {
    List<Task> tasks = tHandler.findAll();
    return tasks;
  }

  @Override
  public void remove(Task task) {
    tHandler.delete(task);
  }

  @Override
  public List<GroupByService> getGroupByServices() {
    return Collections.unmodifiableList(this.groupByServices);
  }

  @Override
  public void startRequest(ExoContainer container) {
    EntityManagerFactory entityManagerFactory = ExoEntityManagerFactory.getEntityManagerFactory();
    entityManager.set(entityManagerFactory.createEntityManager());
    if (!getEntityManager().getTransaction().isActive()) {
      getEntityManager().getTransaction().begin();
    }
  }

  @Override
  public void endRequest(ExoContainer container) {
    if (getEntityManager() != null && getEntityManager().getTransaction().isActive()) {
      getEntityManager().getTransaction().commit();
      getEntityManager().close();
      entityManager.set(null);
    }      
  }
}


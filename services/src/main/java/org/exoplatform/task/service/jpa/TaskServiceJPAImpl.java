/*
 * Copyright (C) 2015 eXo Platform SAS.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
  
package org.exoplatform.task.service.jpa;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;

import java.util.ArrayList;
import java.util.List;

import org.exoplatform.task.domain.Task;
import org.exoplatform.task.service.GroupByService;
import org.exoplatform.task.service.TaskService;
import org.exoplatform.task.service.memory.GroupByProject;
import org.exoplatform.task.service.memory.GroupByStatus;
import org.exoplatform.task.service.memory.GroupByTag;

public class TaskServiceJPAImpl implements TaskService {

  private EntityManagerFactory factory = Persistence.createEntityManagerFactory("org.exoplatform.task");
  
  private final List<GroupByService> groupByServices;

  public TaskServiceJPAImpl() {
      this.groupByServices = new ArrayList<GroupByService>();
      this.groupByServices.add(new GroupByStatus(this));
      this.groupByServices.add(new GroupByProject(this));
      this.groupByServices.add(new GroupByTag(this));
  }
  
  @Override
  public void save(Task task) {
    EntityManager entityManager = begin();
    entityManager.persist(task);
    end(entityManager);
  }

  void end(EntityManager entityManager) {
    entityManager.getTransaction().commit();
    entityManager.close();
  }

  EntityManager begin() {
    EntityManager entityManager = factory.createEntityManager();
    entityManager.getTransaction().begin();
    return entityManager;
  }

  @Override
  public Task findTaskById(long id) {
    EntityManager entityManager = begin();
    Task task = entityManager.find(Task.class, id);
    end(entityManager);
    return task;
  }

  @Override
  public List<Task> findAllTask() {
    EntityManager entityManager = begin();
    List<Task> tasks = entityManager.createQuery("from Task", Task.class).getResultList();
    end(entityManager);
    return tasks;
  }

  @Override
  public List<GroupByService> getGroupByServices() {
    // TODO Auto-generated method stub
    return null;
  }
  
}

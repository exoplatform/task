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
package org.exoplatform.task.test;

import java.sql.SQLException;
import java.util.Date;
import java.util.HashSet;
import java.util.List;

import javax.persistence.Persistence;

import liquibase.exception.LiquibaseException;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import org.exoplatform.task.dao.ProjectHandler;
import org.exoplatform.task.dao.StatusHandler;
import org.exoplatform.task.dao.TaskHandler;
import org.exoplatform.task.domain.Task;
import org.exoplatform.task.factory.ExoEntityManagerFactory;
import org.exoplatform.task.service.jpa.DAOHandlerJPAImpl;

/**
 * Created by The eXo Platform SAS
 * Author : Thibault Clement
 * tclement@exoplatform.com
 * 4/13/15
 */
public class DAOTest {

  private TaskHandler taskDAO;
  private ProjectHandler projectDAO;
  private StatusHandler statusDAO;
  private DAOHandlerJPAImpl taskService;

  @BeforeClass
  public static void createTable() throws SQLException,
      ClassNotFoundException, LiquibaseException {
    TestUtils.initH2DB();
    ExoEntityManagerFactory.setEntityManagerFactory(Persistence.createEntityManagerFactory("org.exoplatform.task"));
  }

  @Before
  public void initDAOs() {
    taskService = new DAOHandlerJPAImpl();

    taskDAO = taskService.getTaskHandler();
    projectDAO = taskService.getProjectHandler();
    statusDAO = taskService.getStatusHandler();

    //
    taskService.startRequest(null);
  }

  @After
  public void cleanTables() {
    taskDAO.deleteAll();
    statusDAO.deleteAll();
    projectDAO.deleteAll();

    //
    taskService.endRequest(null);
  }

  @AfterClass
  public static void removeTables() throws LiquibaseException, SQLException {
    TestUtils.closeDB();
  }

  @Test
  public void testDAONotNull() {
    Assert.assertNotNull(taskDAO);
  }

  @Test
  public void testAddNewTask() {
    List<Task> tasks = taskDAO.findAll();
    Assert.assertEquals(0, tasks.size());

    Task task = newDefaultSimpleTask();
    HashSet<String> tags = new HashSet<String>();
    tags.add("my label");
    task.setTags(tags);
    taskDAO.create(task);

    tasks = taskDAO.findAll();
    Assert.assertEquals(1, tasks.size());
  }

  private Task newDefaultSimpleTask() {
    Task task = new Task();
    task.setTitle("Default task");
    task.setAssignee("root");
    task.setCreatedBy("root");
    task.setCreatedTime(new Date());
    return task;
  }
}


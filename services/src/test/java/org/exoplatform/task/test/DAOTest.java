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

import liquibase.exception.LiquibaseException;
import org.exoplatform.task.dao.jpa.ProjectDAOImpl;
import org.exoplatform.task.dao.jpa.StatusDAOImpl;
import org.exoplatform.task.dao.jpa.TaskDAOImpl;
import org.exoplatform.task.domain.Task;
import org.exoplatform.task.factory.ExoEntityManagerFactory;
import org.junit.*;

import javax.persistence.Persistence;
import java.sql.SQLException;
import java.util.Date;
import java.util.HashSet;
import java.util.List;

/**
 * Created by The eXo Platform SAS
 * Author : Thibault Clement
 * tclement@exoplatform.com
 * 4/13/15
 */
public class DAOTest {

  private TaskDAOImpl taskDAO;
  private ProjectDAOImpl projectDAO;
  private StatusDAOImpl statusDAO;

  @BeforeClass
  public static void createTable() throws SQLException,
      ClassNotFoundException, LiquibaseException {
    TestUtils.initH2DB();
    ExoEntityManagerFactory.setEntityManagerFactory(Persistence.createEntityManagerFactory("org.exoplatform.task"));
  }

  @Before
  public void initDAOs() {
    taskDAO = new TaskDAOImpl();
    projectDAO = new ProjectDAOImpl();
    statusDAO = new StatusDAOImpl();
    taskDAO.initDAO();
    projectDAO.initDAO();
    statusDAO.initDAO();
  }

  @After
  public void cleanTables() {
    taskDAO.beginTransaction();
    projectDAO.joinTransaction();
    statusDAO.joinTransaction();
    taskDAO.deleteAll();
    statusDAO.deleteAll();
    projectDAO.deleteAll();
    taskDAO.commitAndCloseTransaction();
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
    taskDAO.beginTransaction();
    List<Task> tasks = taskDAO.findAll();
    Assert.assertEquals(0, tasks.size());
    taskDAO.closeTransaction();
    //Add a new task
    taskDAO.beginTransaction();
    Task task = newDefaultSimpleTask();
    HashSet<String> tags = new HashSet<String>();
    tags.add("my label");
    task.setTags(tags);
    taskDAO.create(task);
    taskDAO.commitAndCloseTransaction();
    //Get all tasks
    taskDAO.beginTransaction();
    tasks = taskDAO.findAll();
    Assert.assertEquals(1, tasks.size());
    taskDAO.closeTransaction();
  }

  private Task newDefaultSimpleTask() {
    Task task = new Task();
    task.setTitle("Default task");
    task.setAssignee("root");
    task.setCreatedBy("root");
    task.setCreatedTime(new Date());
    return task;
  }

  private Task newSpecificSimpleTask(String title, String assignee, String creator) {
    Task task = new Task();
    task.setTitle(title);
    task.setAssignee(assignee);
    task.setCreatedBy(creator);
    task.setCreatedTime(new Date());
    return task;
  }

}


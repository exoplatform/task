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
import java.util.List;

import javax.persistence.Persistence;

import liquibase.exception.LiquibaseException;

import org.exoplatform.task.dao.TaskQuery;
import org.exoplatform.task.domain.Project;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import org.exoplatform.task.dao.TaskHandler;
import org.exoplatform.task.domain.Priority;
import org.exoplatform.task.domain.Task;
import org.exoplatform.task.factory.ExoEntityManagerFactory;
import org.exoplatform.task.service.TaskParser;
import org.exoplatform.task.service.impl.TaskParserImpl;
import org.exoplatform.task.service.jpa.TaskServiceJPAImpl;

/**
 * @author <a href="trongtt@exoplatform.com">Trong Tran</a>
 * @version $Revision$
 */
public class TestTaskDAO {

  private TaskHandler tDAO;
  private TaskServiceJPAImpl taskService;
  private TaskParser parser = new TaskParserImpl();

  private final String username = "root";

  @BeforeClass
  public static void init() throws SQLException,
      ClassNotFoundException, LiquibaseException {
    TestUtils.initH2DB();
    ExoEntityManagerFactory.setEntityManagerFactory(Persistence.createEntityManagerFactory("org.exoplatform.task"));
  }
  
  @AfterClass
  public static void destroy() throws LiquibaseException, SQLException {
    TestUtils.closeDB();
  }

  @Before
  public void setup() {
    taskService = new TaskServiceJPAImpl();
    tDAO = taskService.getTaskHandler();

    //
    taskService.startRequest(null);
  }

  @After
  public void tearDown() {
    //
    taskService.endRequest(null);
  }

  @Test
  public void testTaskCreation() {
    Task task = parser.parse("Testing task creation");
    tDAO.create(task);

    List<Task> list = tDAO.findAll();
    Assert.assertEquals(1, list.size());

    //
    task = parser.parse("There is an important meeting tomorrow !high");
    tDAO.create(task);
    list = tDAO.findAll();
    Assert.assertEquals(2, list.size());

    //
    task = tDAO.find(task.getId());
    Assert.assertNotNull(task);
    Assert.assertEquals("There is an important meeting tomorrow", task.getTitle());
    Assert.assertEquals(Priority.HIGH, task.getPriority());
  }

  @Test
  public void testFindTaskByQuery() {
    Task task = newTaskInstance("Test find task by query", "description of find task by query", "root");
    tDAO.create(task);

    TaskQuery query = new TaskQuery();
    query.setTitle("task");
    List<Task> tasks = tDAO.findTaskByQuery(query);
    Assert.assertTrue(tasks.size() > 0);

    query = new TaskQuery();
    query.setTitle("testFindTaskByQuery0123456789");
    tasks = tDAO.findTaskByQuery(query);
    Assert.assertEquals(0, tasks.size());

    query = new TaskQuery();
    query.setDescription("description of find task by query");
    tasks = tDAO.findTaskByQuery(query);
    Assert.assertTrue(tasks.size() > 0);

    query = new TaskQuery();
    query.setDescription("testFindTaskByQuery0123456789");
    tasks = tDAO.findTaskByQuery(query);
    Assert.assertEquals(0, tasks.size());

    query = new TaskQuery();
    query.setAssignee("root");
    tasks = tDAO.findTaskByQuery(query);
    Assert.assertTrue(tasks.size() > 0);

    query = new TaskQuery();
    query.setAssignee("testFindTaskByQuery0123456789");
    tasks = tDAO.findTaskByQuery(query);
    Assert.assertEquals(0, tasks.size());

    query = new TaskQuery();
    query.setKeyword("find task by query");
    tasks = tDAO.findTaskByQuery(query);
    Assert.assertTrue(tasks.size() > 0);

    query = new TaskQuery();
    query.setKeyword("testFindTaskByQuery0123456789");
    tasks = tDAO.findTaskByQuery(query);
    Assert.assertEquals(0, tasks.size());
  }

  @Test
  public void testGetIncomingTask() {
    Project project = new Project();
    project.setName("Project1");
    taskService.getProjectHandler().create(project);

    Task task1 = newTaskInstance("Task 1", "", username);
    tDAO.create(task1);

    Task task2 = newTaskInstance("Task 2", "", username);
    task2.setProject(project);
    tDAO.create(task2);

    List<Task> tasks = tDAO.getIncomingTask(username, null);
    assertContain(tasks, task1.getId());
    assertNotContain(tasks, task2.getId());

  }

  private Task newTaskInstance(String taskTitle, String description, String assignee) {
    Task task = new Task();
    task.setTitle(taskTitle);
    task.setDescription(description);
    task.setAssignee(assignee);
    return task;
  }

  private void assertContain(List<Task> tasks, Long taskId) {
    for(Task t : tasks) {
      if(t.getId() == taskId) {
        return;
      }
    }
    Assert.fail("Task with ID " + taskId  + " should exist on the list");
  }

  private void assertNotContain(List<Task> tasks, Long taskId) {
    for(Task t : tasks) {
      if(t.getId() == taskId) {
        Assert.fail("Task with ID " + taskId  + " should not exist on the list");
      }
    }
  }
}


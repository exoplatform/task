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
package org.exoplatform.task.test.dao;

import junit.framework.Assert;
import liquibase.exception.LiquibaseException;
import org.exoplatform.task.dao.ProjectHandler;
import org.exoplatform.task.domain.Project;
import org.exoplatform.task.domain.Status;
import org.exoplatform.task.domain.Task;
import org.exoplatform.task.factory.ExoEntityManagerFactory;
import org.exoplatform.task.service.jpa.DAOHandlerJPAImpl;
import org.exoplatform.task.test.TestUtils;
import org.junit.*;

import javax.persistence.Persistence;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * @author <a href="trongtt@exoplatform.com">Trong Tran</a>
 * @version $Revision$
 */
public class TestProjectDAO {

  private ProjectHandler pDAO;
  private DAOHandlerJPAImpl taskService;

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
    taskService = new DAOHandlerJPAImpl();
    pDAO = taskService.getProjectHandler();

    //
    taskService.startRequest(null);
  }

  @After
  public void tearDown() {
    pDAO.deleteAll();

    //
    taskService.endRequest(null);
  }

  @Test
  public void testProjectCreation() {
    List<Project> all;

    Project p1 = new Project("Test project 1", null, null, null, null);
    pDAO.create(p1);
    all = pDAO.findAll();
    Assert.assertEquals(1, all.size());
    Assert.assertEquals("Test project 1", p1.getName());

    all = pDAO.findAll();
    Assert.assertEquals(1, all.size());
    Assert.assertEquals("Test project 1", p1.getName());
    Project p2 = new Project("Test project 2", null, null, null, null);
    pDAO.create(p2);

    all = pDAO.findAll();
    Assert.assertEquals(2, all.size());
    Assert.assertEquals(p1.getId() + 1, p2.getId());
  }

  @Test
  public void testFindSubProject() {
    Project parent = new Project("Parent project", null, null, null, null);
    pDAO.create(parent);

    Project child = new Project("Child project", null, null, null, null);
    child.setParent(parent);
    pDAO.create(child);

    List<Project> projects = pDAO.findSubProjects(parent);
    Assert.assertEquals(1, projects.size());

    projects = pDAO.findSubProjects(null);
    Assert.assertTrue(projects.size() >= 1);
  }
  
  private Project createProject(String name, Status ...status) {
    Set<String> managers = new HashSet<String>();
    managers.add("root");

    Set<String> participators = new HashSet<String>();
    participators.add("demo");

    Set<Status> tmp = new HashSet<Status>();
    Project project = new Project(name, "des", tmp, managers, participators);    
    if (status != null) {
      for (Status st : status) {
        st.setProject(project);
        tmp.add(st);
      }
    }

    return project;
  }

  private Status createStatus(String name, Task ...tasks) {
    Status st = new Status();
    st.setName(name);
    st.setRank(1);
    if (tasks != null) {
      Set<Task> set = new HashSet<Task>();
      for (Task t : tasks) {
        t.setStatus(st);
        set.add(t);
      }
      st.setTasks(set);      
    }
    return st;
  }
  
  private Task createTask(String title) {
    Task t = new Task();
    t.setTitle("my task");
    return t;
  }
}


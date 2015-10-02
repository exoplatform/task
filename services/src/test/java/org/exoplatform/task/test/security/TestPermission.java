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
package org.exoplatform.task.test.security;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.TimeZone;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.exoplatform.container.PortalContainer;
import org.exoplatform.task.dao.DAOHandler;
import org.exoplatform.task.dao.ProjectHandler;
import org.exoplatform.task.dao.TaskHandler;
import org.exoplatform.task.domain.Project;
import org.exoplatform.task.domain.Status;
import org.exoplatform.task.domain.Task;
import org.exoplatform.task.service.ParserContext;
import org.exoplatform.task.service.TaskParser;
import org.exoplatform.task.service.impl.TaskParserImpl;
import org.exoplatform.task.test.AbstractTest;

/**
 * Created by The eXo Platform SAS
 * Author : Thibault Clement
 * tclement@exoplatform.com
 * 4/24/15
 */
public class TestPermission extends AbstractTest {

  private TaskHandler tDAO;
  private ProjectHandler pDAO;
  private DAOHandler daoHandler;
  private TaskParser parser = new TaskParserImpl();
  private ParserContext context = new ParserContext(TimeZone.getDefault());

  @Before
  public void setup() {
    PortalContainer container = PortalContainer.getInstance();
    
    daoHandler = (DAOHandler) container.getComponentInstanceOfType(DAOHandler.class);
    pDAO = daoHandler.getProjectHandler();
    tDAO = daoHandler.getTaskHandler();
    pDAO = daoHandler.getProjectHandler();
  }

  @After
  public void tearDown() {
    tDAO.deleteAll();
    pDAO.deleteAll();
  }

  @Test
  public void testTaskAssignPermission() {

    //User
    String user1 = "user1";
    String user2 = "user2";

    //Task
    List<Task> tasks = new ArrayList<Task>();
    Task task1 = parser.parse("Task of User 1", context);
    task1.setCreatedBy(user1);
    task1.setAssignee(user1);
    tasks.add(task1);
    Task task2 = parser.parse("Task of User 1 assign to user 2", context);
    task2.setCreatedBy(user1);
    task2.setAssignee(user2);
    tasks.add(task2);

    //Creation
    tDAO.createAll(tasks);

    //Test
    Assert.assertEquals(2, tDAO.findByUser(user1).size());
    Assert.assertEquals(1, tDAO.findByUser(user2).size());
    Assert.assertEquals(0, tDAO.findByUser("John Doe").size());

  }

  @Test
  public void testTaskCoworkerPermission() {
    //User
    String user1 = "user1";
    String user2 = "user2";
    Set<String> coworkers = new HashSet<String>();
    coworkers.add(user2);

    //Task
    List<Task> tasks = new ArrayList<Task>();
    Task task1 = parser.parse("Task of User 1", context);
    task1.setCreatedBy(user1);
    task1.setAssignee(user1);
    tasks.add(task1);
    Task task2 = parser.parse("Task of User 1 with user2 as coworker", context);
    task2.setCreatedBy(user1);
    task2.setAssignee(user1);
    task2.setCoworker(coworkers);
    tasks.add(task2);

    //Creation
    tDAO.createAll(tasks);

    //Test
    Assert.assertEquals(2, tDAO.findByUser(user1).size());
    Assert.assertEquals(1, tDAO.findByUser(user2).size());
    Assert.assertEquals(0, tDAO.findByUser("John Doe").size());
  }

  @Test
  public void testProjectManagerPermission() {

    //User
    String user1 = "user1";
    String user2 = "user2";
    Set<String> managers = new HashSet<String>();
    managers.add(user2);

    //Project
    Project project = new Project();
    project.setName("Project of user 2 manager");
    project.setManager(managers);

    //Status
    Status status = new Status();
    status.setName("TODO");
    status.setRank(1);
    status.setProject(project);
    project.getStatus().add(status);

    //Task
    Task task1 = parser.parse("Task of User 1", context);
    task1.setCreatedBy(user1);
    task1.setAssignee(user1);

    //Task attached to project
    Task task2 = parser.parse("Task of User 1 in project user 2 manage", context);
    task2.setCreatedBy(user1);
    task2.setAssignee(user1);
    task2.setStatus(status);
    status.getTasks().add(task2);

    //Creation
    pDAO.create(project);
    tDAO.create(task1);

    //Test
    Assert.assertEquals(2, tDAO.findByUser(user1).size());
    Assert.assertEquals(1, tDAO.findByUser(user2).size());
    Assert.assertEquals(0, tDAO.findByUser("John Doe").size());
    //Only user2 is manager of a project
    Assert.assertEquals(0, pDAO.findAllByMemberships(getMembershipsUser(user1)).size());
    Assert.assertEquals(1, pDAO.findAllByMemberships(getMembershipsUser(user2)).size());
  }

  @Test
  public void testProjectMemberPermission() {

    //User
    String user1 = "user1";
    String user2 = "user2";
    Set<String> members = new HashSet<String>();
    members.add(user2);

    //Project
    Project project = new Project();
    project.setName("Project of user 2 manager");
    project.setManager(members);

    //Status
    Status status = new Status();
    status.setName("TODO");
    status.setRank(1);
    status.setProject(project);
    project.getStatus().add(status);

    //Task
    Task task1 = parser.parse("Task of User 1", context);
    task1.setCreatedBy(user1);
    task1.setAssignee(user1);

    //Task attached to project
    Task task2 = parser.parse("Task of User 1 in project user 2 member", context);
    task2.setCreatedBy(user1);
    task2.setAssignee(user1);
    task2.setStatus(status);
    status.getTasks().add(task2);

    //Creation
    pDAO.create(project);
    tDAO.create(task1);

    //Test
    Assert.assertEquals(2, tDAO.findByUser(user1).size());
    Assert.assertEquals(1, tDAO.findByUser(user2).size());
    Assert.assertEquals(0, tDAO.findByUser("John Doe").size());
    //Only user2 is member of a project
    Assert.assertEquals(0, pDAO.findAllByMemberships(getMembershipsUser(user1)).size());
    Assert.assertEquals(1, pDAO.findAllByMemberships(getMembershipsUser(user2)).size());
  }

  private List<String> getMembershipsUser(String user) {
    List<String> memberships = new ArrayList<String>();
    memberships.add(user);
    return memberships;
  }

}


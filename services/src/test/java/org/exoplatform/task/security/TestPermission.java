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
package org.exoplatform.task.security;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.TimeZone;

import org.exoplatform.services.security.Identity;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.exoplatform.container.PortalContainer;
import org.exoplatform.task.dao.DAOHandler;
import org.exoplatform.task.dao.ProjectHandler;
import org.exoplatform.task.dao.ProjectQuery;
import org.exoplatform.task.dao.TaskHandler;
import org.exoplatform.task.domain.Project;
import org.exoplatform.task.domain.Status;
import org.exoplatform.task.domain.Task;
import org.exoplatform.task.service.ParserContext;
import org.exoplatform.task.service.TaskParser;
import org.exoplatform.task.service.impl.TaskParserImpl;
import org.exoplatform.task.AbstractTest;
import org.exoplatform.task.util.TaskUtil;

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
    daoHandler.getStatusHandler().deleteAll();
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
  public void testTaskEditPermission() {

    //Users and Identities creation
    String user1 = "user1";
    String user2 = "user2";

    Identity userId1= new Identity("user1");
    Identity userId2= new Identity("user2");

    //Project creation
    Project project = new Project();
    project.setName("Project of user 1");

    Set<String> managers = new HashSet<String>();
    managers.add(user1);
    project.setManager(managers);
    pDAO.create(project);

    // Status creation
    Status status = new Status();
    status.setName("TODO");
    status.setRank(1);
    status.setProject(project);
    daoHandler.getStatusHandler().create(status);

    //Tasks Creation
    List<Task> tasks = new ArrayList<Task>();
    Task task1 = parser.parse("Task of User 1", context);
    task1.setCreatedBy(user1);
    task1.setStatus(status);
    tasks.add(task1);
    Task task2 = parser.parse("Task of User 2", context);
    task2.setCreatedBy(user2);
    task2.setStatus(status);
    tasks.add(task2);

    //Creation
    tDAO.createAll(tasks);

    //Test
    //User1 is the manager of the project so he can edit all tasks in the project
    Assert.assertEquals(true, TaskUtil.canEditTaskProject(task1,userId1));
    Assert.assertEquals(true, TaskUtil.canEditTaskProject(task2,userId1));
    //User2 is not the manager of the project so he can only edit the task that he created
    Assert.assertEquals(false, TaskUtil.canEditTaskProject(task1,userId2));
    Assert.assertEquals(true, TaskUtil.canEditTaskProject(task2,userId2));
  }

  @Test
  public void testProjectManagerPermission() throws Exception {

    //User
    String user1 = "user1";
    String user2 = "user2";
    Set<String> managers = new HashSet<String>();
    managers.add(user2);

    //Project
    Project project = new Project();
    project.setName("Project of user 2 manager");
    project.setManager(managers);

    pDAO.create(project);

    //Status
    Status status = new Status();
    status.setName("TODO");
    status.setRank(1);
    status.setProject(project);
    daoHandler.getStatusHandler().create(status);

    //Task
    Task task1 = parser.parse("Task of User 1", context);
    task1.setCreatedBy(user1);
    task1.setAssignee(user1);

    //Task attached to project
    Task task2 = parser.parse("Task of User 1 in project user 2 manage", context);
    task2.setCreatedBy(user1);
    task2.setAssignee(user1);
    task2.setStatus(status);

    tDAO.create(task1);
    tDAO.create(task2);

    //Test
    Assert.assertEquals(2, tDAO.findByUser(user1).size());
    Assert.assertEquals(1, tDAO.findByUser(user2).size());
    Assert.assertEquals(0, tDAO.findByUser("John Doe").size());
    //Only user2 is manager of a project
    Assert.assertEquals(0, pDAO.findAllByMembershipsAndKeyword(getMembershipsUser(user1), null, null).getSize());
    Assert.assertEquals(1, pDAO.findAllByMembershipsAndKeyword(getMembershipsUser(user2), null, null).getSize());
  }

  @Test
  public void testProjectMemberPermission() throws Exception {

    //User
    String user1 = "user1";
    String user2 = "user2";
    Set<String> members = new HashSet<String>();
    members.add(user2);

    //Project
    Project project = new Project();
    project.setName("Project of user 2 manager");
    project.setManager(members);

    //
    pDAO.create(project);

    //Status
    Status status = new Status();
    status.setName("TODO");
    status.setRank(1);
    status.setProject(project);
    daoHandler.getStatusHandler().create(status);

    //Task
    Task task1 = parser.parse("Task of User 1", context);
    task1.setCreatedBy(user1);
    task1.setAssignee(user1);

    //Task attached to project
    Task task2 = parser.parse("Task of User 1 in project user 2 member", context);
    task2.setCreatedBy(user1);
    task2.setAssignee(user1);
    task2.setStatus(status);

    tDAO.create(task1);
    tDAO.create(task2);

    //Test
    Assert.assertEquals(2, tDAO.findByUser(user1).size());
    Assert.assertEquals(1, tDAO.findByUser(user2).size());
    Assert.assertEquals(0, tDAO.findByUser("John Doe").size());
    //Only user2 is member of a project
    Assert.assertEquals(0, pDAO.findAllByMembershipsAndKeyword(getMembershipsUser(user1), null, null).getSize());
    Assert.assertEquals(1, pDAO.findAllByMembershipsAndKeyword(getMembershipsUser(user2), null, null).getSize());
  }

  private List<String> getMembershipsUser(String user) {
    List<String> memberships = new ArrayList<String>();
    memberships.add(user);
    return memberships;
  }

}


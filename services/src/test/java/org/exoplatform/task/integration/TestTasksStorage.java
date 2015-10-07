/**
 * Copyright (C) 2015 eXo Platform SAS.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Affero General Public License
 * as published by the Free Software Foundation; either version 3
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, see<http://www.gnu.org/licenses/>.
 **/
  
package org.exoplatform.task.integration;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import org.exoplatform.calendar.model.Calendar;
import org.exoplatform.calendar.model.query.CalendarQuery;
import org.exoplatform.calendar.storage.CalendarDAO;
import org.exoplatform.container.PortalContainer;
import org.exoplatform.services.security.Identity;
import org.exoplatform.services.security.MembershipEntry;
import org.exoplatform.task.domain.Project;
import org.exoplatform.task.exception.EntityNotFoundException;
import org.exoplatform.task.integration.calendar.TasksStorage;
import org.exoplatform.task.service.ProjectService;
import org.exoplatform.task.service.TaskService;
import org.exoplatform.task.AbstractTest;
import org.exoplatform.task.util.ProjectUtil;

public class TestTasksStorage extends AbstractTest {

  private TasksStorage storage;
  private CalendarDAO calDAO;
  
  private Project p1;
  private Project p2;
  private Project p3;
  
  @Before
  public void setup() {
    PortalContainer container = PortalContainer.getInstance();
    
    ProjectService projectService = container.getComponentInstanceOfType(ProjectService.class);
    TaskService taskService = container.getComponentInstanceOfType(TaskService.class);
    storage = new TasksStorage(projectService, taskService);
    calDAO = storage.getCalendarDAO();
    
    Set<String> users = new HashSet<String>();
    users.add("root");
    p1 = new Project("Test project 1", null, null, users, null);
    p1.setCalendarIntegrated(true);
    projectService.createProject(p1);
    p2 = new Project("Test project 2", null, null, null, users);
    p2.setCalendarIntegrated(true);    
    projectService.createProject(p2);
    Set<String> memberships = new HashSet<String>();
    memberships.add("*:/platform/administrators");
    p3 = new Project("Test project 3", null, null, memberships, null);
    p3.setCalendarIntegrated(true);
    projectService.createProject(p3);
  }

  @After
  public void tearDown() throws EntityNotFoundException {
    PortalContainer container = PortalContainer.getInstance();

    ProjectService projectService = container.getComponentInstanceOfType(ProjectService.class);
    projectService.removeProject(p1.getId(), true);
    projectService.removeProject(p2.getId(), true);
    projectService.removeProject(p3.getId(), true);
  }
  
  @Test
  public void testGetCalendarById() {    
    Calendar cal = calDAO.getById(String.valueOf(p1.getId()));
    Assert.assertNotNull(cal);
    Assert.assertNull(calDAO.getById("-1"));
    Assert.assertNotNull(calDAO.getById(String.valueOf(ProjectUtil.TODO_PROJECT_ID)));
  }
  
  @Test
  public void testFindCalendars() {
    CalendarQuery query = new CalendarQuery();
    query.setDS(TasksStorage.TASKS_STORAGE);
    Identity identity = new Identity("root");
    query.setIdentity(identity);
    
    List<Calendar> cals = calDAO.findCalendars(query);
    Assert.assertEquals(3, cals.size());
    Assert.assertEquals(String.valueOf(ProjectUtil.TODO_PROJECT_ID), cals.get(0).getId());
    Assert.assertEquals(String.valueOf(p1.getId()), cals.get(1).getId());
    Assert.assertEquals(String.valueOf(p2.getId()), cals.get(2).getId());
    
    List<MembershipEntry> memberships = new LinkedList<MembershipEntry>();
    memberships.add(new MembershipEntry("/platform/administrators", "*"));
    identity = new Identity("root", memberships);
    query.setIdentity(identity);
    cals = calDAO.findCalendars(query);
    Assert.assertEquals(4, cals.size());
    Assert.assertEquals(String.valueOf(p3.getId()), cals.get(3).getId());
    
    query.setExclusions(String.valueOf(ProjectUtil.TODO_PROJECT_ID));
    cals = calDAO.findCalendars(query);
    Assert.assertEquals(3, cals.size());
  }
}

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

import static org.junit.Assert.fail;
import static org.mockito.Matchers.anyList;
import static org.mockito.Matchers.isNull;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.exoplatform.calendar.model.Calendar;
import org.exoplatform.calendar.model.query.CalendarQuery;
import org.exoplatform.calendar.storage.CalendarDAO;
import org.exoplatform.commons.utils.ListAccess;
import org.exoplatform.services.security.Identity;
import org.exoplatform.services.security.MembershipEntry;
import org.exoplatform.task.dao.OrderBy;
import org.exoplatform.task.domain.Project;
import org.exoplatform.task.exception.EntityNotFoundException;
import org.exoplatform.task.integration.calendar.TasksStorage;
import org.exoplatform.task.service.ProjectService;
import org.exoplatform.task.service.TaskService;
import org.exoplatform.task.util.ProjectUtil;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

public class TestTasksStorage {

  private TasksStorage storage;
  private CalendarDAO calDAO;
  
  private Project p1;
  private Project p2;
  private Project p3;

  @Mock
  private ProjectService projectService;

  @Mock
  private TaskService taskService;
  
  @Before
  public void setup() {
    MockitoAnnotations.initMocks(this);

    Set<String> users = new HashSet<String>();
    users.add("root");
    p1 = new Project("Test project 1", null, null, users, Collections.<String>emptySet());
    p1.setId(1);
    p1.setCalendarIntegrated(true);

    p2 = new Project("Test project 2", null, null, Collections.<String>emptySet(), users);
    p2.setId(2);
    p2.setCalendarIntegrated(true);
    Set<String> memberships = new HashSet<String>();
    memberships.add("*:/platform/administrators");

    p3 = new Project("Test project 3", null, null, memberships, Collections.<String>emptySet());
    p3.setId(3);
    p3.setCalendarIntegrated(true);

    storage = new TasksStorage(projectService, taskService);
    calDAO = storage.getCalendarDAO();
  }

  @After
  public void tearDown() throws EntityNotFoundException {

  }
  
  @Test
  public void testGetCalendarById() {
    try {
      when(projectService.getProject(p1.getId())).thenReturn(p1);
    } catch (EntityNotFoundException ex) {
      ex.printStackTrace();
      fail(ex.getMessage());
    }

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

    when(projectService.findProjects(anyList(), isNull(String.class), isNull(OrderBy.class)))
            .thenReturn(new ListAccess<Project>() {
              
              @Override
              public Project[] load(int arg0, int arg1) throws Exception, IllegalArgumentException {
                return new Project[] {p1, p2};
              }
              
              @Override
              public int getSize() throws Exception {
                return 2;
              }
            })
            .thenReturn(new ListAccess<Project>() {

              @Override
              public int getSize() throws Exception {
                return 3;
              }

              @Override
              public Project[] load(int arg0, int arg1) throws Exception, IllegalArgumentException {
                return new Project[] {p1, p2, p3};
              }
            });
    
    List<Calendar> cals = calDAO.findCalendars(query);
    Assert.assertEquals(3, cals.size());
    Assert.assertEquals(String.valueOf(ProjectUtil.TODO_PROJECT_ID), cals.get(0).getId());
    Assert.assertEquals(String.valueOf(p1.getId()), cals.get(1).getId());
    Assert.assertEquals(String.valueOf(p2.getId()), cals.get(2).getId());
    
    List<MembershipEntry> memberships = new LinkedList<MembershipEntry>();
    memberships.add(new MembershipEntry("/platform/administrators", "*"));
    identity = new Identity("root", memberships);
    query.setIdentity(identity);

    //when(projectService.findProjects(any(List.class), null, null)).thenReturn(Arrays.asList(p1, p2, p3));

    cals = calDAO.findCalendars(query);
    Assert.assertEquals(4, cals.size());
    Assert.assertEquals(String.valueOf(p3.getId()), cals.get(3).getId());
    
    query.setExclusions(String.valueOf(ProjectUtil.TODO_PROJECT_ID));
    cals = calDAO.findCalendars(query);
    Assert.assertEquals(3, cals.size());
  }
}

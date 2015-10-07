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
package org.exoplatform.task.service;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.exoplatform.commons.utils.ListAccessImpl;
import org.exoplatform.task.dao.TaskQuery;
import org.exoplatform.task.exception.EntityNotFoundException;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.runners.MockitoJUnitRunner;

import org.exoplatform.task.dao.DAOHandler;
import org.exoplatform.task.dao.StatusHandler;
import org.exoplatform.task.dao.TaskHandler;
import org.exoplatform.task.domain.Project;
import org.exoplatform.task.domain.Status;
import org.exoplatform.task.domain.Task;
import org.exoplatform.task.exception.NotAllowedOperationOnEntityException;
import org.exoplatform.task.service.impl.StatusServiceImpl;
import org.exoplatform.task.TestUtils;

import java.util.Arrays;

/**
 * Created by The eXo Platform SAS
 * Author : Thibault Clement
 * tclement@exoplatform.com
 * 6/9/15
 */
@RunWith(MockitoJUnitRunner.class)
public class StatusServiceTest {

  StatusService statusService;

  @Mock
  TaskHandler taskHandler;
  @Mock
  StatusHandler statusHandler;
  @Mock
  DAOHandler daoHandler;

  @Captor
  ArgumentCaptor<Status> statusCaptor;
  
  @Captor
  ArgumentCaptor<Task> taskCaptor;
  
  @Before
  public void setUp() {
    MockitoAnnotations.initMocks(this);
    statusService = new StatusServiceImpl(daoHandler);
    
    //Mock DAO handler to return Mocked DAO
    when(daoHandler.getTaskHandler()).thenReturn(taskHandler);
    when(daoHandler.getStatusHandler()).thenReturn(statusHandler);

    //Mock some DAO methods
    //Mock taskHandler.find(id) to return default task for id = TestUtils.EXISTING_TASK_ID (find(id) return null otherwise)
    when(taskHandler.find(TestUtils.EXISTING_TASK_ID)).thenReturn(TestUtils.getDefaultTask());
    when(statusHandler.find(TestUtils.EXISTING_STATUS_ID)).thenReturn(TestUtils.getDefaultStatus());

  }

  @After
  public void tearDown() {
    statusService = null;
  }

  @Test
  public void testCreateDefaultStatusesProject() {
    Project project = TestUtils.getDefaultProject();
    statusService.createDefaultStatuses(project);
    //Default project contains 4 default status so create(status) must be called 4 times
    verify(statusHandler, times(4)).create(any(Status.class));
  }

  @Test 
  public void testCreateStatus() {    
    Project project = TestUtils.getDefaultProject();
    Status currStatus = new Status(1L, "oldStatus");
    project.getStatus().add(currStatus);
    when(statusHandler.findHighestRankStatusByProject(project.getId())).thenReturn(currStatus);
    
    statusService.createStatus(project, "newStatus");
    //
    verify(statusHandler, times(1)).create(statusCaptor.capture());
    Status result = statusCaptor.getValue();
    int currRank = currStatus.getRank() != null ? currStatus.getRank() : -1;
    assertEquals(currRank + 1, result.getRank().intValue());
    assertEquals("newStatus", result.getName());
    assertEquals(project, result.getProject());    
  }
  
  @Test 
  public void testDeleteLastStatus() throws EntityNotFoundException, NotAllowedOperationOnEntityException {
    Project project = TestUtils.getDefaultProject();

    Status s1 = TestUtils.getDefaultStatus();
    s1.setProject(project);

    Status s2 = TestUtils.getDefaultStatus();
    s2.setId(2L);
    s2.setProject(project);

    Task t = TestUtils.getDefaultTask();
    t.setStatus(s2);
    
    when(statusHandler.find(s2.getId())).thenReturn(s2);
    when(statusHandler.getStatuses(project.getId())).thenReturn(Arrays.asList(s1, s2)).thenReturn(Arrays.asList(s1));
    when(taskHandler.findTasks(any(TaskQuery.class))).thenReturn(new ListAccessImpl<Task>(Task.class, Arrays.asList(t)));

    statusService.removeStatus(s2.getId());

    verify(taskHandler, times(1)).update(taskCaptor.capture());

    assertEquals(t.getId(), taskCaptor.getValue().getId());
    assertEquals(s1, taskCaptor.getValue().getStatus());
    verify(statusHandler, times(1)).delete(statusCaptor.capture());
    assertEquals(0, statusCaptor.getValue().getTasks().size());
  }
  
  @Test 
  public void testUpdateStatus() throws EntityNotFoundException, NotAllowedOperationOnEntityException {
    Project project = TestUtils.getDefaultProject();
    Status s1 = TestUtils.getDefaultStatus();
    Status s2 = TestUtils.getDefaultStatus();
    s2.setId(2L);
    s2.setName("s2");
    s2.setProject(project);
    
    when(statusHandler.find(s2.getId())).thenReturn(s2);
    when(statusHandler.findByName(s1.getName(), project.getId())).thenReturn(s1);
    try {
      statusService.updateStatus(s2.getId(), s1.getName());
      Assert.fail("should raise exception for duplicating name");
    } catch (NotAllowedOperationOnEntityException ex) {      
    }
    
    statusService.updateStatus(s2.getId(), "s3");
    verify(statusHandler, times(1)).update(statusCaptor.capture());
    assertEquals(s2.getId(), statusCaptor.getValue().getId());
    assertEquals("s3", statusCaptor.getValue().getName());
  }
  
  /*@Test
  public void testSwapPosition() throws NotAllowedOperationOnEntityException {
    Status s1 = TestUtils.getDefaultStatus();
    s1.setRank(0);
    Status s2 = TestUtils.getDefaultStatus();
    s2.setId(2L);
    s2.setRank(1);
    //
    when(statusHandler.find(s1.getId())).thenReturn(s1);
    when(statusHandler.find(s2.getId())).thenReturn(s2);
    
    statusService.swapPosition(s1.getId(), s2.getId());
    assertEquals(1, s1.getRank().intValue());
    assertEquals(0, s2.getRank().intValue());
  }*/
  
}
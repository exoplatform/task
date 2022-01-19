/*
 * Copyright (C) 2003-2020 eXo Platform SAS.
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
import static org.junit.Assert.assertFalse;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.*;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import org.exoplatform.task.storage.ProjectStorage;
import org.exoplatform.task.storage.StatusStorage;
import org.exoplatform.task.storage.impl.ProjectStorageImpl;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import org.exoplatform.container.ExoContainer;
import org.exoplatform.container.ExoContainerContext;
import org.exoplatform.container.PortalContainer;
import org.exoplatform.services.listener.ListenerService;
import org.exoplatform.services.security.ConversationState;
import org.exoplatform.services.security.Identity;
import org.exoplatform.task.TestDtoUtils;
import org.exoplatform.task.TestUtils;
import org.exoplatform.task.dao.CommentHandler;
import org.exoplatform.task.dao.DAOHandler;
import org.exoplatform.task.dao.TaskHandler;
import org.exoplatform.task.domain.Task;
import org.exoplatform.task.dto.TaskDto;
import org.exoplatform.task.exception.EntityNotFoundException;
import org.exoplatform.task.exception.ParameterEntityException;
import org.exoplatform.task.service.impl.TaskServiceImpl;
import org.exoplatform.task.storage.TaskStorage;
import org.exoplatform.task.storage.impl.TaskStorageImpl;

@RunWith(MockitoJUnitRunner.class)
public class TaskServiceTest {

  TaskService                taskService;

  TaskStorage                taskStorage;

  StatusStorage                statusStorage;

  ListenerService            listenerService;

  @Mock
  ExoContainer               container;

  @Mock
  TaskHandler                taskHandler;

  @Mock
  CommentHandler             commentHandler;

  @Mock
  StatusService              statusService;

  @Mock
  ProjectStorage             projectStorage;

  @Mock
  DAOHandler                 daoHandler;

  @Mock
  UserService userService;

  // ArgumentCaptors are how you can retrieve objects that were passed into a
  // method call
  @Captor
  ArgumentCaptor<Task>       taskCaptor;

  @Before
  public void setUp() throws Exception {
    // Make sure the container is started to prevent the ExoTransactional annotation
    // to fail
    PortalContainer.getInstance();

    listenerService = new ListenerService(new ExoContainerContext(container));
    projectStorage = new ProjectStorageImpl(daoHandler);
    taskStorage = new TaskStorageImpl(daoHandler,userService,projectStorage);
    taskService = new TaskServiceImpl(taskStorage, daoHandler, listenerService);

    // Mock DAO handler to return Mocked DAO
    when(daoHandler.getTaskHandler()).thenReturn(taskHandler);
    // Mock some DAO methods
    when(taskHandler.create(any(Task.class))).thenReturn(TestUtils.getDefaultTask());
    when(taskHandler.update((any(Task.class)))).thenReturn(TestUtils.getDefaultTask());
    // Mock taskHandler.find(id) to return default task for id =
    // TestUtils.EXISTING_TASK_ID (find(id) return null otherwise)
    when(taskHandler.find(TestUtils.EXISTING_TASK_ID)).thenReturn(TestUtils.getDefaultTask());
    when(statusService.getStatus(TestUtils.EXISTING_STATUS_ID)).thenReturn(TestUtils.getDefaultStatusDto());
    when(daoHandler.getTaskHandler().getTaskWithCoworkers(1)).thenReturn(TestUtils.getDefaultTask());

    Identity root = new Identity("root");
    ConversationState.setCurrent(new ConversationState(root));
  }

  @After
  public void tearDown() {
    taskService = null;
    ConversationState.setCurrent(null);
  }

  @Test
  public void testUpdateTaskTitle() throws Exception {

    String newTitle = "newTitle";

    TaskDto task = taskService.getTask(TestUtils.EXISTING_TASK_ID);
    task.setTitle(newTitle);
    taskService.updateTask(task);

    // capture the object that was passed into the TaskHandler.updateTask(task)
    // method
    // times(1) verify that the method update has been invoked only one time
    verify(taskHandler, times(1)).update(taskCaptor.capture());

    assertEquals(newTitle, taskCaptor.getValue().getTitle());
  }

  @Test
  public void testUpdateTaskDescription() throws EntityNotFoundException {

    String newDescription = "This is a new description";

    TaskDto task = taskService.getTask(TestUtils.EXISTING_TASK_ID);
    task.setDescription(newDescription);
    taskService.updateTask(task);

    verify(taskHandler, times(1)).update(taskCaptor.capture());

    assertEquals(task.getDescription(), taskCaptor.getValue().getDescription());

  }

  @Test
  public void testUpdateTaskCompleted() throws EntityNotFoundException {

    Boolean newCompleted = true;

    TaskDto task = taskService.getTask(TestUtils.EXISTING_TASK_ID);
    task.setCompleted(newCompleted);
    taskService.updateTask(task);

    verify(taskHandler, times(1)).update(taskCaptor.capture());

    assertEquals(newCompleted, taskCaptor.getValue().isCompleted());

  }

  @Test
  public void testUpdateTaskAssignee() throws EntityNotFoundException {

    String newAssignee = "Tib";

    TaskDto task = taskService.getTask(TestUtils.EXISTING_TASK_ID);
    task.setAssignee(newAssignee);
    taskService.updateTask(task);

    verify(taskHandler, times(1)).update(taskCaptor.capture());

    assertEquals(newAssignee, taskCaptor.getValue().getAssignee());

    taskService.findTasks(newAssignee, newAssignee, 1);
    verify(taskHandler, times(1)).findTasks(newAssignee,new ArrayList<>(),newAssignee,1);

  }

  @Test
  public void testUpdateTaskCoworker() throws EntityNotFoundException {

    Set<String> newCoworkers = new HashSet<String>();
    newCoworkers.add("Tib");
    newCoworkers.add("Trong");
    newCoworkers.add("Phuong");
    newCoworkers.add("TuyenTrong");
    newCoworkers.add("TuyenPhuong");
    newCoworkers.add("PhuongTuyen");
    newCoworkers.add("Tuyen");

    TaskDto task = taskService.getTask(TestUtils.EXISTING_TASK_ID);
    task.setCoworker(newCoworkers);
    taskService.updateTask(task);

    verify(taskHandler, times(1)).update(taskCaptor.capture());

    Set<String> coworker = new HashSet<String>();
    for (String v : newCoworkers) {
      coworker.add(v);
    }
    assertEquals(coworker, taskCaptor.getValue().getCoworker());

  }

  @Test
  public void testUpdateTaskWatcher() throws EntityNotFoundException {

    Set<String> newWatchers = new HashSet<String>();
    newWatchers.add("Tib");
    newWatchers.add("Trong");
    newWatchers.add("Phuong");
    newWatchers.add("Tuyen");

    TaskDto task = taskService.getTask(TestUtils.EXISTING_TASK_ID);
    task.setWatcher(newWatchers);
    when(daoHandler.getTaskHandler().getWatchersOfTask(any())).thenReturn(newWatchers);
    taskService.getWatchersOfTask(task);

    verify(taskHandler, times(1)).getWatchersOfTask(taskCaptor.capture());

    Set<String> newWatcher = new HashSet<String>();
    for (String v : newWatchers) {
      newWatcher.add(v);
    }
    assertEquals(newWatcher, taskCaptor.getValue().getWatcher());

  }

  @Test
  public void testWatcherToTask() throws Exception {

    Set<String> newWatchers = new HashSet<String>();
    newWatchers.add("Tib");
    TaskDto task = taskService.getTask(TestUtils.EXISTING_TASK_ID);
    task.setWatcher(newWatchers);
    taskService.addWatcherToTask("Tib",task);
    verify(taskHandler, times(1)).getWatchersOfTask(taskCaptor.capture());
    Set<String> newWatcher = new HashSet<String>();
    for (String v : newWatchers) {
      newWatcher.add(v);
    }
    assertEquals(newWatcher, taskCaptor.getValue().getWatcher());

  }


  @Test
  public void testUpdateTaskStatus() throws ParameterEntityException, EntityNotFoundException {

    TaskDto task = taskService.getTask(TestUtils.EXISTING_TASK_ID);
    task.setStatus(statusService.getStatus(TestUtils.EXISTING_STATUS_ID));
    taskService.updateTask(task);
    verify(taskHandler, times(1)).update(taskCaptor.capture());
    //assertEquals(TestUtils.getDefaultStatus(), taskCaptor.getValue().getStatus());

  }

  @Test
  public void testUpdateTaskDueDate() throws ParameterEntityException, EntityNotFoundException, ParseException {

    String dueDate = "1989-01-19";
    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
    Date date = sdf.parse(dueDate);

    TaskDto task = taskService.getTask(TestUtils.EXISTING_TASK_ID);
    task.setDueDate(date);
    taskService.updateTask(task);

    verify(taskHandler, times(1)).update(taskCaptor.capture());

    assertEquals(date, taskCaptor.getValue().getDueDate());

  }

  @Test
  public void testFindTasks() throws  EntityNotFoundException {

    String newAssignee = "Tib";

    TaskDto task = taskService.getTask(TestUtils.EXISTING_TASK_ID);
    task.setAssignee(newAssignee);
    taskService.updateTask(task);

    verify(taskHandler, times(1)).update(taskCaptor.capture());

    assertEquals(newAssignee, taskCaptor.getValue().getAssignee());

    taskService.findTasks(newAssignee, newAssignee, 1);
    verify(taskHandler, times(1)).findTasks(newAssignee,new ArrayList<>(),newAssignee,1);

    assertEquals(task.getId(), taskCaptor.getValue().getId());

  }



  @Test
  public void testDeleteTaskById() throws EntityNotFoundException {
    taskService.removeTask(TestUtils.EXISTING_TASK_ID);
    verify(taskHandler, times(1)).delete(taskCaptor.capture());

    assertEquals(TestUtils.EXISTING_TASK_ID, taskCaptor.getValue().getId());
  }

  @Test
  public void testCloneTaskById() throws EntityNotFoundException {

    TaskDto defaultTask = TestDtoUtils.getDefaultTask();

    taskService.cloneTask(TestDtoUtils.EXISTING_TASK_ID);
    ArgumentCaptor<Task> taskCaptor = ArgumentCaptor.forClass(Task.class);
    verify(taskHandler, times(1)).create(taskCaptor.capture());

    assertEquals("Copy of " + defaultTask.getTitle(), taskCaptor.getValue().getTitle());
    assertEquals(defaultTask.getDescription(), taskCaptor.getValue().getDescription());
    assertEquals(defaultTask.getPriority(), taskCaptor.getValue().getPriority());
    assertEquals(defaultTask.getContext(), taskCaptor.getValue().getContext());
    assertEquals(defaultTask.getAssignee(), taskCaptor.getValue().getAssignee());
    assertEquals(defaultTask.getCoworker(), taskCaptor.getValue().getCoworker());
    assertEquals(defaultTask.getStatus(), taskCaptor.getValue().getStatus());
    assertEquals(defaultTask.getCreatedBy(), taskCaptor.getValue().getCreatedBy());
    // Only the createdTime must be different for the cloned task
    assertFalse(defaultTask.getCreatedTime() == taskCaptor.getValue().getCreatedTime());
    assertEquals(defaultTask.getEndDate(), taskCaptor.getValue().getEndDate());
    assertEquals(defaultTask.getStartDate(), taskCaptor.getValue().getStartDate());
    assertEquals(defaultTask.getDueDate(), taskCaptor.getValue().getDueDate());
  }


}

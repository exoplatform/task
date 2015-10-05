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
package org.exoplatform.task.test.service;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.runners.MockitoJUnitRunner;

import org.exoplatform.task.dao.OrderBy;
import org.exoplatform.task.dao.ProjectHandler;
import org.exoplatform.task.dao.StatusHandler;
import org.exoplatform.task.dao.TaskHandler;
import org.exoplatform.task.dao.TaskQuery;
import org.exoplatform.task.domain.Project;
import org.exoplatform.task.domain.Status;
import org.exoplatform.task.domain.Task;
import org.exoplatform.task.exception.NotAllowedOperationOnEntityException;
import org.exoplatform.task.exception.ParameterEntityException;
import org.exoplatform.task.exception.ProjectNotFoundException;
import org.exoplatform.task.service.ProjectService;
import org.exoplatform.task.service.StatusService;
import org.exoplatform.task.service.TaskService;
import org.exoplatform.task.service.impl.ProjectServiceImpl;
import org.exoplatform.task.service.jpa.DAOHandlerJPAImpl;
import org.exoplatform.task.test.TestUtils;

/**
 * Created by The eXo Platform SAS
 * Author : Thibault Clement
 * tclement@exoplatform.com
 * 6/9/15
 */
@RunWith(MockitoJUnitRunner.class)
public class ProjectServiceTest {

  ProjectService projectService;

  @Mock
  StatusService statusService;
  @Mock
  TaskService taskService;
  @Mock
  TaskHandler taskHandler;
  @Mock
  ProjectHandler projectHandler;
  @Mock
  StatusHandler statusHandler;
  @Mock
  DAOHandlerJPAImpl daoHandler;

  //ArgumentCaptors are how you can retrieve objects that were passed into a method call
  @Captor
  ArgumentCaptor<Project> projectCaptor;
  @Captor
  ArgumentCaptor<Task> taskCaptor;
  @Captor
  ArgumentCaptor<TaskQuery> taskQueryCaptor;


  @Before
  public void setUp() {
    MockitoAnnotations.initMocks(this);
    projectService = new ProjectServiceImpl(statusService, taskService, daoHandler);
    
    when(statusService.getDefaultStatus()).thenReturn(Arrays.asList("1", "2", "3", "4"));
    
    //Mock DAO handler to return Mocked DAO

    when(daoHandler.getTaskHandler()).thenReturn(taskHandler);
    when(daoHandler.getProjectHandler()).thenReturn(projectHandler);
    when(daoHandler.getStatusHandler()).thenReturn(statusHandler);

    //Mock some DAO methods

    //when(taskHandler.create(any(Task.class))).thenReturn(defaultTask);
    //when(taskHandler.update(any(Task.class))).thenReturn(defaultTask);
    when(projectHandler.create(any(Project.class))).thenReturn(TestUtils.getDefaultProject());
    when(projectHandler.update(any(Project.class))).thenReturn(TestUtils.getDefaultProject());
    //Mock taskHandler.find(id) to return default task for id = TestUtils.EXISTING_TASK_ID (find(id) return null otherwise)
    when(taskHandler.find(TestUtils.EXISTING_TASK_ID)).thenReturn(TestUtils.getDefaultTask());
    when(statusHandler.find(TestUtils.EXISTING_STATUS_ID)).thenReturn(TestUtils.getDefaultStatus());
    when(projectHandler.find(TestUtils.EXISTING_PROJECT_ID)).thenReturn(TestUtils.getDefaultProject());

  }

  @After
  public void tearDown() {
    projectService = null;
  }

  @Test
  public void testCreateDefaultStatusProjectWithManager() throws ProjectNotFoundException {

    Project defaultProject = TestUtils.getDefaultProject();
    Long projectParent = TestUtils.EXISTING_PROJECT_ID;

    projectService.createDefaultStatusProjectWithManager(defaultProject.getName(), defaultProject.getDescription(), false, projectParent, defaultProject.getManager().iterator().next());
    verify(projectHandler, times(1)).create(projectCaptor.capture());

    assertEquals(defaultProject.getName(), projectCaptor.getValue().getName());
    assertEquals(defaultProject.getDescription(), projectCaptor.getValue().getDescription());
    assertEquals(projectParent, new Long(projectCaptor.getValue().getParent().getId()));
    assertEquals(defaultProject.getManager(), projectCaptor.getValue().getManager());
  }

  @Test
  public void testCreateDefaultStatusProjectWithAttributes() throws ProjectNotFoundException {

    Project defaultProject = TestUtils.getDefaultProject();
    Long projectParent = TestUtils.EXISTING_PROJECT_ID;

    projectService.createDefaultStatusProjectWithAttributes(projectParent, defaultProject.getName(), defaultProject.getDescription(), false, defaultProject.getManager(), defaultProject.getParticipator());
    verify(projectHandler, times(1)).create(projectCaptor.capture());

    assertEquals(defaultProject.getName(), projectCaptor.getValue().getName());
    assertEquals(defaultProject.getDescription(), projectCaptor.getValue().getDescription());
    assertEquals(projectParent, new Long(projectCaptor.getValue().getParent().getId()));
    assertEquals(defaultProject.getManager(), projectCaptor.getValue().getManager());
    assertEquals(defaultProject.getParticipator(), projectCaptor.getValue().getParticipator());
  }

  @Test
  public void testCreateDefaultStatusProject() {
    projectService.createDefaultStatusProject(TestUtils.getDefaultProject());
    verify(projectHandler, times(1)).create(any(Project.class));
    //Default project contains 4 default status so create(status) must be called 4 times
    verify(statusService, times(4)).createStatus(any(Project.class), any(String.class));
  }
  
  @Test
  public void testCreateProjectWithParent() throws ProjectNotFoundException {
    Project parent = new Project();
    parent.setId(1L);
    parent.getStatus().add(new Status(1L, "testStatus"));
    when(projectHandler.find(1L)).thenReturn(parent);
    
    projectService.createDefaultStatusProjectWithAttributes(1L, "test", null, false, null, null);
    
    verify(projectHandler, times(1)).create(any(Project.class));
    //the new created project must inherits parent's workflow
    verify(statusService, times(1)).createStatus(any(Project.class), eq("testStatus"));
  }

  @Test
  public void testUpdateProjectName() throws ParameterEntityException, ProjectNotFoundException {

    String name = "Project Name";

    projectService.updateProjectInfo(TestUtils.EXISTING_PROJECT_ID, "name", new String[]{name});
    verify(projectHandler, times(1)).update(projectCaptor.capture());

    assertEquals(name, projectCaptor.getValue().getName());
  }

  @Test
  public void testUpdateProjectDescription() throws ParameterEntityException, ProjectNotFoundException {

    String description = "Bla bla bla";

    projectService.updateProjectInfo(TestUtils.EXISTING_PROJECT_ID, "description", new String[]{description});
    verify(projectHandler, times(1)).update(projectCaptor.capture());

    assertEquals(description, projectCaptor.getValue().getDescription());
  }

  @Test
  public void testUpdateProjectManagers() throws ParameterEntityException, ProjectNotFoundException {

    String[] newManagers = {"Tib","Trong","Phuong","Tuyen"};

    projectService.updateProjectInfo(TestUtils.EXISTING_PROJECT_ID, "manager", newManagers);
    verify(projectHandler, times(1)).update(projectCaptor.capture());

    Set<String> managers = new HashSet<String>();
    for(String v : newManagers) {
      managers.add(v);
    }

    assertEquals(managers, projectCaptor.getValue().getManager());
  }

  @Test
  public void testUpdateProjectMembers() throws ParameterEntityException, ProjectNotFoundException {

    String[] newMembers = {"Tib","Trong","Phuong","Tuyen"};

    projectService.updateProjectInfo(TestUtils.EXISTING_PROJECT_ID, "participator", newMembers);
    verify(projectHandler, times(1)).update(projectCaptor.capture());

    Set<String> members = new HashSet<String>();
    for(String v : newMembers) {
      members.add(v);
    }

    assertEquals(members, projectCaptor.getValue().getParticipator());
  }

  @Test
  public void testUpdateProjectDueDate() throws ParameterEntityException, ProjectNotFoundException, ParseException {

    String dueDate = "1989-01-19";
    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
    Date date = sdf.parse(dueDate);

    projectService.updateProjectInfo(TestUtils.EXISTING_PROJECT_ID, "dueDate", new String[]{dueDate});
    verify(projectHandler, times(1)).update(projectCaptor.capture());

    assertEquals(date, projectCaptor.getValue().getDueDate());
  }

  @Test
  public void testUpdateProjectColor() throws ParameterEntityException, ProjectNotFoundException {

    String color = "#000000";

    projectService.updateProjectInfo(TestUtils.EXISTING_PROJECT_ID, "color", new String[]{color});
    verify(projectHandler, times(1)).update(projectCaptor.capture());

    assertEquals(color, projectCaptor.getValue().getColor());
  }

  @Test
  public void testDeleteProjectById() throws ProjectNotFoundException {

    Project projectParent = TestUtils.getDefaultProject();
    projectParent.setId(3L);
    Project projectChild = TestUtils.getDefaultProject();
    projectChild.setId(4L);
    projectChild.setParent(projectParent);
    List<Project> projectChilds = new ArrayList<Project>();
    projectChilds.add(projectChild);
    projectParent.setChildren(projectChilds);

    when(projectHandler.find(3L)).thenReturn(projectParent);

    projectService.deleteProjectById(3L, false);
    verify(projectHandler, times(1)).delete(projectCaptor.capture());

    assertEquals(3L, projectCaptor.getValue().getId());
    //Be sure the children is not link to Parent Project so it will not be deleted on cascade
    assertEquals(0, projectCaptor.getValue().getChildren().size());
  }

  @Test
  public void testDeleteProjectByIdWithSubproject() throws ProjectNotFoundException {

    Project projectParent = TestUtils.getDefaultProject();
    projectParent.setId(3L);
    Project projectChild = TestUtils.getDefaultProject();
    projectChild.setId(4L);
    projectChild.setParent(projectParent);
    List<Project> projectChilds = new ArrayList<Project>();
    projectChilds.add(projectChild);
    projectParent.setChildren(projectChilds);

    when(projectHandler.find(3L)).thenReturn(projectParent);

    projectService.deleteProjectById(3L, true);
    verify(projectHandler, times(1)).delete(projectCaptor.capture());

    assertEquals(3L, projectCaptor.getValue().getId());
    //Be sure the children is still link to Parent Project so it will be deleted on cascade
    assertEquals(1, projectCaptor.getValue().getChildren().size());
  }

  @Test
  public void testCloneProjectById() throws ProjectNotFoundException {

    Project project = TestUtils.getDefaultProject();
    project.setName("Tib Project");
    project.setId(3L);

    Set<Task> tasks1 = new HashSet<Task>();
    tasks1.add(TestUtils.getDefaultTask());
    tasks1.add(TestUtils.getDefaultTask());

    Set<Task> tasks2 = new HashSet<Task>();
    tasks2.add(TestUtils.getDefaultTask());
    tasks2.add(TestUtils.getDefaultTask());

    Status status1 = new Status(3, "ToDo", 1, tasks1, project);
    Status status2 = new Status(4, "ToDo", 2, tasks2, project);

    Set<Status> statuses = new HashSet<Status>();
    statuses.add(status1);
    statuses.add(status2);

    project.setStatus(statuses);

    when(projectHandler.find(3L)).thenReturn(project);

    projectService.cloneProjectById(3L, false);
    verify(projectHandler, times(1)).create(projectCaptor.capture());

    assertEquals("Copy of "+project.getName(), projectCaptor.getValue().getName());
    assertEquals(project.getStatus().size(), projectCaptor.getValue().getStatus().size());
    for (Status status : projectCaptor.getValue().getStatus()) {
      assertEquals(0, status.getTasks().size());
    }
  }

  @Test
  public void testCloneProjectByIdWithCopyOfTask() throws ProjectNotFoundException {

    Project project = TestUtils.getDefaultProject();
    project.setId(3L);

    Set<Task> tasks1 = new HashSet<Task>();
    tasks1.add(TestUtils.getDefaultTask());
    tasks1.add(TestUtils.getDefaultTask());

    Set<Task> tasks2 = new HashSet<Task>();
    tasks2.add(TestUtils.getDefaultTask());
    tasks2.add(TestUtils.getDefaultTask());
    tasks2.add(TestUtils.getDefaultTask());

    Status status1 = new Status(3, "ToDo", 1, tasks1, project);
    Status status2 = new Status(4, "On Progress", 2, tasks2, project);

    Set<Status> statuses = new HashSet<Status>();
    statuses.add(status1);
    statuses.add(status2);

    project.setStatus(statuses);

    when(projectHandler.find(3L)).thenReturn(project);

    projectService.cloneProjectById(3L, true);
    verify(projectHandler, times(1)).create(projectCaptor.capture());

    assertEquals(project.getStatus().size(), projectCaptor.getValue().getStatus().size());
    Iterator<Status> statusIterator = projectCaptor.getValue().getStatus().iterator();
    for (Status status : projectCaptor.getValue().getStatus()) {
      //status1 with id = 3 contains 2 tasks
      if (status.getId() == 3) assertEquals(2, statusIterator.next().getTasks().size());
      //status2 with id = 4 contains 3 tasks
      else if (status.getId() == 4) assertEquals(3, statusIterator.next().getTasks().size());
    }
  }

  @Test
  public void testCreateTaskToProjectId() throws ProjectNotFoundException {

    Status defaultStatus = TestUtils.getDefaultStatus();
    Task defaultTask = TestUtils.getDefaultTask();

    when(statusHandler.findLowestRankStatusByProject(TestUtils.EXISTING_PROJECT_ID)).thenReturn(defaultStatus);

    projectService.createTaskToProjectId(TestUtils.EXISTING_PROJECT_ID, defaultTask);
    verify(taskService, times(1)).createTask(taskCaptor.capture());

    assertEquals(defaultStatus.getId(), taskCaptor.getValue().getStatus().getId());
  }

  @Test
  public void testGetTasksWithKeywordByProjectId() throws ProjectNotFoundException {

    String fieldName = "tag";
    boolean ascending = true;
    String keyword = "MyTag";

    projectService.getTasksWithKeywordByProjectId(Arrays.asList(TestUtils.EXISTING_PROJECT_ID), new OrderBy(fieldName, ascending), keyword);
    verify(taskHandler, times(1)).findTaskByQuery(taskQueryCaptor.capture());

    assertEquals(fieldName, taskQueryCaptor.getValue().getOrderBy().iterator().next().getFieldName());
    assertEquals(ascending, taskQueryCaptor.getValue().getOrderBy().iterator().next().isAscending());
    assertEquals(keyword, taskQueryCaptor.getValue().getKeyword());
  }

  @Test
  public void testRemoveManagerFromProjectId() throws NotAllowedOperationOnEntityException, ProjectNotFoundException {

    //Create a project with 2 managers
    Project project = TestUtils.getDefaultProject();
    String newManager = "bobby";
    project.getManager().add(newManager);

    when(projectHandler.find(TestUtils.EXISTING_PROJECT_ID)).thenReturn(project);

    assertEquals(2, projectService.getProjectById(TestUtils.EXISTING_PROJECT_ID).getManager().size());

    projectService.removePermissionFromProjectId(TestUtils.EXISTING_PROJECT_ID, newManager, "manager");
    verify(projectHandler, times(1)).update(projectCaptor.capture());

    assertEquals(1, projectCaptor.getValue().getManager().size());
  }

  @Test
  public void testRemoveMemberFromProjectId() throws NotAllowedOperationOnEntityException, ProjectNotFoundException {

    //Create a project with 1 member
    Project project = TestUtils.getDefaultProject();
    String newMember = "bobby";
    project.getParticipator().add(newMember);

    when(projectHandler.find(TestUtils.EXISTING_PROJECT_ID)).thenReturn(project);

    assertEquals(1, projectService.getProjectById(TestUtils.EXISTING_PROJECT_ID).getParticipator().size());

    projectService.removePermissionFromProjectId(TestUtils.EXISTING_PROJECT_ID, newMember, "participator");
    verify(projectHandler, times(1)).update(projectCaptor.capture());

    assertEquals(0, projectCaptor.getValue().getParticipator().size());
  }


  @Test
  public void testAddManagerFromProjectId() throws ProjectNotFoundException, NotAllowedOperationOnEntityException {

    String newManager = "bobby";

    assertEquals(1, projectService.getProjectById(TestUtils.EXISTING_PROJECT_ID).getManager().size());

    projectService.addPermissionsFromProjectId(TestUtils.EXISTING_PROJECT_ID, newManager, "manager");
    verify(projectHandler, times(1)).update(projectCaptor.capture());

    assertEquals(2, projectCaptor.getValue().getManager().size());
  }

  @Test
  public void testAddMemberFromProjectId() throws ProjectNotFoundException, NotAllowedOperationOnEntityException {

    String newMember = "bobby";

    assertEquals(0, projectService.getProjectById(TestUtils.EXISTING_PROJECT_ID).getParticipator().size());

    projectService.addPermissionsFromProjectId(TestUtils.EXISTING_PROJECT_ID, newMember, "participator");
    verify(projectHandler, times(1)).update(projectCaptor.capture());

    assertEquals(1, projectCaptor.getValue().getParticipator().size());

  }

  @Test
  public void testGetProjectTreeByIdentity() {

  }

  @Test(expected = NotAllowedOperationOnEntityException.class)
  public void testNotAllowedToRemoveLastManagerOfProject() throws NotAllowedOperationOnEntityException, ProjectNotFoundException {
    Project defaultProject = TestUtils.getDefaultProject();
    assertEquals(1, defaultProject.getManager().size());
    projectService.removePermissionFromProjectId(TestUtils.EXISTING_PROJECT_ID, defaultProject.getManager().iterator().next(), "manager");
  }

}


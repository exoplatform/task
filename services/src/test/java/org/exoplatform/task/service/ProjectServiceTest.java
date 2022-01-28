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

import org.exoplatform.commons.utils.ListAccess;
import org.exoplatform.container.ExoContainer;
import org.exoplatform.container.ExoContainerContext;
import org.exoplatform.container.PortalContainer;
import org.exoplatform.services.listener.ListenerService;
import org.exoplatform.social.core.storage.impl.StorageUtils;
import org.exoplatform.task.TestDtoUtils;
import org.exoplatform.task.TestUtils;
import org.exoplatform.task.dao.*;
import org.exoplatform.task.dao.jpa.DAOHandlerJPAImpl;
import org.exoplatform.task.domain.Project;
import org.exoplatform.task.domain.Status;
import org.exoplatform.task.domain.Task;
import org.exoplatform.task.dto.ProjectDto;
import org.exoplatform.task.dto.StatusDto;
import org.exoplatform.task.dto.TaskDto;
import org.exoplatform.task.exception.EntityNotFoundException;
import org.exoplatform.task.exception.ParameterEntityException;
import org.exoplatform.task.service.impl.ProjectServiceImpl;
import org.exoplatform.task.storage.ProjectStorage;
import org.exoplatform.task.storage.StatusStorage;
import org.exoplatform.task.storage.TaskStorage;
import org.exoplatform.task.storage.impl.ProjectStorageImpl;
import org.exoplatform.task.storage.impl.StatusStorageImpl;
import org.exoplatform.task.util.ProjectUtil;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.*;
import org.exoplatform.task.util.StorageUtil;

@RunWith(MockitoJUnitRunner.class)
public class ProjectServiceTest {

    ProjectService projectService;
    ProjectStorage projectStorage;
    StatusStorage statusStorage;
    TaskStorage taskStorage;

    @Mock
    ListenerService listenerService;

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
    ArgumentCaptor<TaskDto> taskCaptor;
    @Captor
    ArgumentCaptor<TaskQuery> taskQueryCaptor;


    @Before
    public void setUp() {
        // Make sure the container is started to prevent the ExoTransactional annotation to fail
        PortalContainer.getInstance();
        projectStorage = new ProjectStorageImpl(daoHandler);
        statusStorage = new StatusStorageImpl(daoHandler, projectStorage);
        projectService = new ProjectServiceImpl(statusService, taskService, daoHandler, projectStorage, listenerService);
        //Mock DAO handler to return Mocked DAO
        when(daoHandler.getProjectHandler()).thenReturn(projectHandler);

        //Mock some DAO methods
        when(projectHandler.create(any(Project.class))).thenReturn(TestUtils.getDefaultProject());
        when(projectHandler.update(any(Project.class))).thenReturn(TestUtils.getDefaultProject());
        //Mock taskHandler.find(id) to return default task for id = TestUtils.EXISTING_TASK_ID (find(id) return null otherwise)
        when(projectHandler.find(TestUtils.EXISTING_PROJECT_ID)).thenReturn(TestUtils.getDefaultProject());

    }

    @After
    public void tearDown() {
        projectService = null;
    }



    @Test
    public void testCreateProjectWithParent() throws EntityNotFoundException {
        Project parent = new Project();
        parent.setId(1L);
        Status status = new Status(1L, "testStatus");

        when(projectHandler.find(1L)).thenReturn(parent);
        when(statusService.getStatuses(1L)).thenReturn(Arrays.asList(StorageUtil.statusToDTO(status,projectStorage)));

        //projectService.createDefaultStatusProjectWithAttributes(1L, "test", null, null, null);
        Project child = ProjectUtil.newProjectInstance("test", "", "root");
        projectService.createProject(StorageUtil.projectToDto(child,projectStorage), parent.getId());

        verify(projectHandler, times(1)).create(any(Project.class));
        //the new created project must inherits parent's workflow
        verify(statusService, times(1)).createStatus(any(), eq("testStatus"));
    }



    @Test
    public void testCreateDefaultProject() throws EntityNotFoundException {

        ProjectDto defaultProject = TestDtoUtils.getDefaultProject();
        Long projectParent = TestUtils.EXISTING_PROJECT_ID;
        //when(projectStorage.getProject(projectParent).getManager()).thenReturn(TestDtoUtils.getParentProject().getManager());
        projectService.createProject(defaultProject, projectParent);
        verify(projectHandler, times(1)).create(projectCaptor.capture());

        assertEquals(projectParent, new Long(projectCaptor.getValue().getParent().getId()));

    }

    @Test
    public void testUpdateProjectName() throws ParameterEntityException, EntityNotFoundException {

        String name = "Project Name";

        Map<String, String[]> fields = new HashMap<String, String[]>();
        fields.put("name", new String[]{name});
        ProjectDto project = ProjectUtil.saveProjectField(projectService, TestUtils.EXISTING_PROJECT_ID, fields);
        projectService.updateProject(project);
        verify(projectHandler, times(1)).update(projectCaptor.capture());

        assertEquals(name, projectCaptor.getValue().getName());
    }

    @Test
    public void testUpdateProjectDescription() throws ParameterEntityException, EntityNotFoundException {

        String description = "Bla bla bla";

        Map<String, String[]> fields = new HashMap<String, String[]>();
        fields.put("description", new String[]{description});
        ProjectDto project = ProjectUtil.saveProjectField(projectService, TestUtils.EXISTING_PROJECT_ID, fields);
        projectService.updateProject(project);
        verify(projectHandler, times(1)).update(projectCaptor.capture());

        assertEquals(description, projectCaptor.getValue().getDescription());
    }

    @Test
    public void testUpdateProjectManagers() throws ParameterEntityException, EntityNotFoundException {

        String[] newManagers = {"Tib", "Trong", "Phuong", "Tuyen"};

        Map<String, String[]> fields = new HashMap<String, String[]>();
        fields.put("manager", newManagers);
        ProjectDto project = ProjectUtil.saveProjectField(projectService, TestUtils.EXISTING_PROJECT_ID, fields);
        projectService.updateProject(project);
        verify(projectHandler, times(1)).update(projectCaptor.capture());

        Set<String> managers = new HashSet<String>();
        for (String v : newManagers) {
            managers.add(v);
        }

        assertEquals(managers, projectCaptor.getValue().getManager());
    }

    @Test
    public void testUpdateProjectMembers() throws ParameterEntityException, EntityNotFoundException {

        String[] newMembers = {"Tib", "Trong", "Phuong", "Tuyen"};

        Map<String, String[]> fields = new HashMap<String, String[]>();
        fields.put("participator", newMembers);
        ProjectDto project = ProjectUtil.saveProjectField(projectService, TestUtils.EXISTING_PROJECT_ID, fields);
        projectService.updateProject(project);
        verify(projectHandler, times(1)).update(projectCaptor.capture());

        Set<String> members = new HashSet<String>();
        for (String v : newMembers) {
            members.add(v);
        }

        assertEquals(members, projectCaptor.getValue().getParticipator());
    }

    @Test
    public void testUpdateProjectDueDate() throws ParameterEntityException, EntityNotFoundException, ParseException {

        String dueDate = "1989-01-19";
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        Date date = sdf.parse(dueDate);

        Map<String, String[]> fields = new HashMap<String, String[]>();
        fields.put("dueDate", new String[]{dueDate});
        ProjectDto project = ProjectUtil.saveProjectField(projectService, TestUtils.EXISTING_PROJECT_ID, fields);
        projectService.updateProject(project);
        verify(projectHandler, times(1)).update(projectCaptor.capture());

        assertEquals(date, projectCaptor.getValue().getDueDate());
    }

    @Test
    public void testCloneProjectById() throws Exception {

        ProjectDto project = TestUtils.getDefaultProjectDto();
        project.setName("Tib Project");
        project.setId(3L);

        Project project_ = TestUtils.getDefaultProject();
        project.setName("Tib Project");
        project.setId(3L);

        final Set<TaskDto> tasks1 = new HashSet<TaskDto>();
        tasks1.add(TestUtils.getDefaultTaskDto());
        tasks1.add(TestUtils.getDefaultTaskDto());

        final Set<TaskDto> tasks2 = new HashSet<TaskDto>();
        tasks2.add(TestUtils.getDefaultTaskDto());
        tasks2.add(TestUtils.getDefaultTaskDto());

        StatusDto status1 = new StatusDto(3, "ToDo", 1, project);
        StatusDto status2 = new StatusDto(4, "ToDo", 2, project);

        Set<StatusDto> statuses = new HashSet<StatusDto>();
        statuses.add(status1);
        statuses.add(status2);
        project.setStatus(statuses);
        List<StatusDto> list = new ArrayList<StatusDto>(statuses);
        when(projectHandler.find(3L)).thenReturn(project_);

        when(statusService.getStatuses(3L)).thenReturn(list);

        projectService.cloneProject(3L, false);
        verify(projectHandler, times(1)).create(projectCaptor.capture());


        // Verify 2 status were created
        ArgumentCaptor<String> statusNameCaptor = ArgumentCaptor.forClass(String.class);


        // Verify task is not created
        verify(taskService, times(0)).createTask(taskCaptor.capture());
    }

    @Test
    public void testUpdateProjectColor() throws ParameterEntityException, EntityNotFoundException {

        String color = "#000000";

        Map<String, String[]> fields = new HashMap<String, String[]>();
        fields.put("color", new String[]{color});
        ProjectDto project = ProjectUtil.saveProjectField(projectService, TestUtils.EXISTING_PROJECT_ID, fields);
        projectService.updateProject(project);
        verify(projectHandler, times(1)).update(projectCaptor.capture());

        assertEquals(color, projectCaptor.getValue().getColor());
    }

    @Test
    public void testDeleteProjectById() throws EntityNotFoundException {

        Project projectParent = TestUtils.getDefaultProject();
        projectParent.setId(3L);

        final Project projectChild = TestUtils.getDefaultProject();
        projectChild.setId(4L);
        projectChild.setParent(projectParent);

        ListAccess<Project> projectChilds = new ListAccess<Project>() {
            @Override
            public int getSize() throws Exception {
                return 1;
            }

            @Override
            public Project[] load(int arg0, int arg1) throws Exception, IllegalArgumentException {
                return new Project[]{projectChild};
            }
        };

        when(projectHandler.find(3L)).thenReturn(projectParent);

        projectService.removeProject(3L, false);
        verify(projectHandler, times(1)).removeProject(eq(3L), eq(false));
    }

    @Test
    public void testDeleteProjectByIdWithSubproject() throws EntityNotFoundException {

        Project projectParent = TestUtils.getDefaultProject();
        projectParent.setId(3L);
        Project projectChild = TestUtils.getDefaultProject();
        projectChild.setId(4L);
        projectChild.setParent(projectParent);
        List<Project> projectChilds = new ArrayList<Project>();
        projectChilds.add(projectChild);
        projectParent.setChildren(projectChilds);

        when(projectHandler.find(3L)).thenReturn(projectParent);

        projectService.removeProject(3L, true);
        verify(projectHandler, times(1)).removeProject(eq(3L), eq(true));
    }

    @Test
    public void testGetProjectTreeByIdentity() {

    }


}


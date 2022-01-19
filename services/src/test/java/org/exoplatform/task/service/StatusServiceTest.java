/* * Copyright (C) 2003-2020 eXo Platform SAS.
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

import org.exoplatform.container.ExoContainer;
import org.exoplatform.container.ExoContainerContext;
import org.exoplatform.container.PortalContainer;
import org.exoplatform.services.listener.ListenerService;
import org.exoplatform.task.TestDtoUtils;
import org.exoplatform.task.TestUtils;
import org.exoplatform.task.dao.*;
import org.exoplatform.task.domain.Project;
import org.exoplatform.task.domain.Status;
import org.exoplatform.task.domain.Task;
import org.exoplatform.task.dao.ProjectQuery;
import org.exoplatform.task.dto.ProjectDto;
import org.exoplatform.task.exception.EntityNotFoundException;
import org.exoplatform.task.exception.NotAllowedOperationOnEntityException;
import org.exoplatform.task.service.impl.StatusServiceImpl;
import org.exoplatform.task.storage.ProjectStorage;
import org.exoplatform.task.storage.StatusStorage;
import org.exoplatform.task.storage.TaskStorage;
import org.exoplatform.task.storage.impl.ProjectStorageImpl;
import org.exoplatform.task.storage.impl.StatusStorageImpl;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.*;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.*;


@RunWith(MockitoJUnitRunner.class)
public class StatusServiceTest {

    StatusService statusService;

    StatusStorage statusStorage;

    ProjectStorage projectStorage;

    TaskStorage taskStorage;

    @Mock
    ListenerService listenerService;

    @Mock
    TaskHandler taskHandler;
    @Mock
    StatusHandler statusHandler;
    @Mock
    ProjectHandler projectHandler;
    @Mock
    DAOHandler daoHandler;

    @Captor
    ArgumentCaptor<Status> statusCaptor;

    @Captor
    ArgumentCaptor<Task> taskCaptor;

    @Before
    public void setUp() {
        // Make sure the container is started to prevent the ExoTransactional annotation to fail
        PortalContainer.getInstance();
        projectStorage = new ProjectStorageImpl(daoHandler);
        statusStorage = new StatusStorageImpl(daoHandler, projectStorage);
        statusService = new StatusServiceImpl(daoHandler, statusStorage, projectStorage, listenerService);

        //Mock DAO handler to return Mocked DAO
        when(daoHandler.getTaskHandler()).thenReturn(taskHandler);
        when(daoHandler.getStatusHandler()).thenReturn(statusHandler);
        when(daoHandler.getProjectHandler()).thenReturn(projectHandler);

        //Mock some DAO methods
        when(daoHandler.getStatusHandler().findHighestRankStatusByProject(TestUtils.EXISTING_TASK_ID)).thenReturn(TestUtils.getDefaultStatus());

    }

    @After
    public void tearDown() {
        statusService = null;
    }

    @Test
    public void testCreateDefaultStatusesProject() {
        ProjectDto project = TestDtoUtils.getDefaultProject();
        statusService.createInitialStatuses(project);

        //Default project contains 4 default status so create(status) must be called 4 times
        verify(statusHandler, times(4)).create(any(Status.class));
    }

    @Test
    public void testCreateStatus() {
        ProjectDto project = TestDtoUtils.getDefaultProject();
        Status currStatus = new Status(1L, "oldStatus");
        //project.getStatus().add(currStatus);
        when(statusHandler.findHighestRankStatusByProject(project.getId())).thenReturn(currStatus);

        statusService.createStatus(project, "newStatus");
        //
        verify(statusHandler, times(1)).create(statusCaptor.capture());
        Status result = statusCaptor.getValue();
        int currRank = currStatus.getRank() != null ? currStatus.getRank() : -1;
        assertEquals(currRank + 1, result.getRank().intValue());
        assertEquals("newStatus", result.getName());
        //assertEquals(project, result.getProject());
    }

    @Test
    public void testDeleteLastStatus() throws EntityNotFoundException, NotAllowedOperationOnEntityException, Exception {
        Project project = TestUtils.getDefaultProject();

        Status s1 = TestUtils.getDefaultStatus();
        s1.setProject(project);

        Status s2 = TestUtils.getDefaultStatus();
        s2.setId(2L);
        s2.setProject(project);

        Task t = TestUtils.getDefaultTask();
        t.setStatus(s2);

        Set<String> participator = new HashSet<String>();
        participator.add("Tib");
        ProjectQuery query = new ProjectQuery();
        query.setId(project.getId());
        when(statusHandler.find(s2.getId())).thenReturn(s2);
        when(statusHandler.getStatuses(project.getId())).thenReturn(Arrays.asList(s1, s2)).thenReturn(Arrays.asList(s1));

        statusService.removeStatus(s2.getId());

        verify(statusHandler, times(1)).delete(statusCaptor.capture());
    }

    @Test
    public void testUpdateStatus() throws EntityNotFoundException, NotAllowedOperationOnEntityException {
        Project project = TestUtils.getDefaultProject();
        Status s1 = TestUtils.getDefaultStatus();
        Status s2 = TestUtils.getDefaultStatus();
        Status s3 = TestUtils.getDefaultStatus();
        s2.setId(2L);
        s2.setName("s3");
        s2.setProject(project);
        s3.setId(2L);
        s3.setName("s3");
        s3.setProject(project);
        List<Status> sts=new ArrayList<>();
        sts.add(s1);
        sts.add(s2);
        when(statusHandler.find(s2.getId())).thenReturn(s2);
        when(statusHandler.getStatuses(project.getId())).thenReturn(sts);
        try {
            statusService.updateStatus(s2.getId(), s1.getName());
            Assert.fail("should raise exception for duplicating name");
        } catch (NotAllowedOperationOnEntityException ex) {
        }
        when(daoHandler.getStatusHandler().update(s2)).thenReturn(s2);
        statusService.updateStatus(s2.getId(), "s3");
        verify(statusHandler, times(1)).update(statusCaptor.capture());
        assertEquals(s2.getId(), statusCaptor.getValue().getId());
        assertEquals("s3", statusCaptor.getValue().getName());
    }

}

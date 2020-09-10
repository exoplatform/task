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

import org.exoplatform.container.PortalContainer;
import org.exoplatform.task.TestUtils;
import org.exoplatform.task.dao.DAOHandler;
import org.exoplatform.task.dao.LabelHandler;
import org.exoplatform.task.dao.StatusHandler;
import org.exoplatform.task.dao.TaskHandler;
import org.exoplatform.task.domain.Label;
import org.exoplatform.task.domain.LabelTaskMapping;
import org.exoplatform.task.domain.Status;
import org.exoplatform.task.domain.Task;
import org.exoplatform.task.dto.LabelDto;
import org.exoplatform.task.exception.EntityNotFoundException;
import org.exoplatform.task.service.impl.LabelServiceImpl;
import org.exoplatform.task.service.impl.StatusServiceImpl;
import org.exoplatform.task.storage.LabelStorage;
import org.exoplatform.task.storage.ProjectStorage;
import org.exoplatform.task.storage.StatusStorage;
import org.exoplatform.task.storage.TaskStorage;
import org.exoplatform.task.storage.impl.LabelStorageImpl;
import org.exoplatform.task.storage.impl.ProjectStorageImpl;
import org.exoplatform.task.storage.impl.StatusStorageImpl;
import org.exoplatform.task.storage.impl.TaskStorageImpl;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class LabelServiceTest {

    StatusService statusService;

    LabelService labelService;

    StatusStorage statusStorage;

    TaskStorage taskStorage;

    LabelStorage labelStorage;

    ProjectStorage projectStorage;

    @Mock
    TaskHandler taskHandler;

    @Mock
    StatusHandler statusHandler;

    @Mock
    LabelHandler labelHandler;

    @Mock
    DAOHandler daoHandler;

    @Captor
    ArgumentCaptor<Status> statusCaptor;

    @Captor
    ArgumentCaptor<Label> labelCaptor;

    @Captor
    ArgumentCaptor<Task> taskCaptor;

    @Before
    public void setUp() {
        // Make sure the container is started to prevent the ExoTransactional annotation
        // to fail
        PortalContainer.getInstance();
        projectStorage = new ProjectStorageImpl(daoHandler);
        taskStorage = new TaskStorageImpl(daoHandler);
        statusStorage = new StatusStorageImpl(daoHandler, projectStorage);
        statusService = new StatusServiceImpl(daoHandler, statusStorage);
        labelStorage = new LabelStorageImpl(daoHandler);
        labelService = new LabelServiceImpl(labelStorage, taskStorage, daoHandler);

        // Mock DAO handler to return Mocked DAO
        when(daoHandler.getTaskHandler()).thenReturn(taskHandler);
        when(daoHandler.getStatusHandler()).thenReturn(statusHandler);
        when(daoHandler.getLabelHandler()).thenReturn(labelHandler);

        // Mock some DAO methods
        when(taskHandler.find(TestUtils.EXISTING_TASK_ID)).thenReturn(TestUtils.getDefaultTask());
        when(statusHandler.find(TestUtils.EXISTING_STATUS_ID)).thenReturn(TestUtils.getDefaultStatus());
        when(labelHandler.find(TestUtils.EXISTING_LABEL_ID)).thenReturn(labelStorage.labelToEntity(TestUtils.getDefaultLabel()));
        when(daoHandler.getStatusHandler()
                .findHighestRankStatusByProject(TestUtils.EXISTING_TASK_ID)).thenReturn(TestUtils.getDefaultStatus());
    }

    @After
    public void tearDown() {
        labelService = null;
    }

    @Test
    public void testCreateDefaultLabelTask() {
        LabelDto label = TestUtils.getDefaultLabel();
        when(daoHandler.getLabelHandler().create(any())).thenReturn(labelStorage.labelToEntity(label));
        labelService.createLabel(label);
        verify(labelHandler, times(2)).create(labelCaptor.capture());
        Label result = labelCaptor.getValue();
        assertEquals("TODO", result.getName());
        assertEquals("label", result.getUsername());

    }

    @Test
    public void testRemoveLabel() {
        labelService.removeLabel(TestUtils.EXISTING_TASK_ID);
        verify(labelHandler, times(1)).delete(labelCaptor.capture());

        assertEquals(TestUtils.EXISTING_TASK_ID, labelCaptor.getValue().getId());

    }

    @Test
    public void testRemoveTaskFromLabel() throws EntityNotFoundException {

        Task task = TestUtils.getDefaultTask();
        Label label = labelStorage.labelToEntity(TestUtils.getDefaultLabel());
        LabelTaskMapping labelTaskMappingDto = new LabelTaskMapping(label, task);
        //when(daoHandler.getLabelTaskMappingHandler().deleteAll()).thenReturn(labelTaskMappingDto);
        // labelService.removeTaskFromLabel(taskStorage.toDto(task), TestUtils.EXISTING_LABEL_ID);
        //verify(labelHandler, times(1)).delete(labelCaptor.capture());

        //assertEquals(TestUtils.EXISTING_LABEL_ID, labelCaptor.getValue().getId());
    }

    @Test
    public void testAddTaskToLabel() throws EntityNotFoundException {

        Task task = TestUtils.getDefaultTask();
        Label label = labelStorage.labelToEntity(TestUtils.getDefaultLabel());
        LabelTaskMapping labelTaskMappingDto = new LabelTaskMapping(label, task);
    /*labelTaskMappingDto.setLabel(label);
    labelTaskMappingDto.setTask(task);
    when(daoHandler.getLabelTaskMappingHandler().create(any())).thenReturn(labelTaskMappingDto);
    labelService.addTaskToLabel(taskStorage.toDto(task), TestUtils.EXISTING_LABEL_ID);
    verify(labelHandler, times(1)).create(labelCaptor.capture());

   assertEquals(TestUtils.EXISTING_LABEL_ID, labelCaptor.getValue().getId());*/
    }


}

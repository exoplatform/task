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
import org.exoplatform.services.listener.ListenerService;
import org.exoplatform.task.TestUtils;
import org.exoplatform.task.dao.DAOHandler;
import org.exoplatform.task.dao.LabelHandler;
import org.exoplatform.task.dao.StatusHandler;
import org.exoplatform.task.dao.TaskHandler;
import org.exoplatform.task.domain.Label;
import org.exoplatform.task.dto.LabelDto;
import org.exoplatform.task.exception.EntityNotFoundException;
import org.exoplatform.task.service.impl.LabelServiceImpl;
import org.exoplatform.task.service.impl.StatusServiceImpl;
import org.exoplatform.task.service.impl.TaskServiceImpl;
import org.exoplatform.task.storage.LabelStorage;
import org.exoplatform.task.storage.ProjectStorage;
import org.exoplatform.task.storage.StatusStorage;
import org.exoplatform.task.storage.TaskStorage;
import org.exoplatform.task.storage.impl.LabelStorageImpl;
import org.exoplatform.task.storage.impl.ProjectStorageImpl;
import org.exoplatform.task.storage.impl.StatusStorageImpl;
import org.exoplatform.task.storage.impl.TaskStorageImpl;
import org.exoplatform.task.util.StorageUtil;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.Arrays;
import java.util.Collections;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class LabelServiceTest {

    StatusService statusService;

    LabelService labelService;

    TaskService taskService;

    StatusStorage statusStorage;

    TaskStorage taskStorage;

    LabelStorage labelStorage;

    ProjectStorage projectStorage;

    ListenerService listenerService;

    @Mock
    TaskHandler taskHandler;

    @Mock
    StatusHandler statusHandler;

    @Mock
    LabelHandler labelHandler;

    @Mock
    DAOHandler daoHandler;

    @Mock
    UserService userService;

    @Captor
    ArgumentCaptor<Label> labelCaptor;

    @Before
    public void setUp() {
        // Make sure the container is started to prevent the ExoTransactional annotation
        // to fail
        PortalContainer.getInstance();
        projectStorage = new ProjectStorageImpl(daoHandler);
        taskStorage = new TaskStorageImpl(daoHandler,userService,projectStorage);
        statusStorage = new StatusStorageImpl(daoHandler, projectStorage);
        statusService = new StatusServiceImpl(daoHandler, statusStorage, projectStorage, listenerService);
        labelStorage = new LabelStorageImpl(daoHandler);
        labelService = new LabelServiceImpl(labelStorage, daoHandler, projectStorage, listenerService);
        taskService =new TaskServiceImpl(taskStorage, daoHandler, listenerService);
        // Mock DAO handler to return Mocked DAO
        when(daoHandler.getLabelHandler()).thenReturn(labelHandler);

        // Mock some DAO methods
        when(labelHandler.find(TestUtils.EXISTING_LABEL_ID)).thenReturn(StorageUtil.labelToEntity(TestUtils.getDefaultLabel()));
    }

    @After
    public void tearDown() {
        labelService = null;
    }

    @Test
    public void testCreateDefaultLabelTask() {
        LabelDto label = TestUtils.getDefaultLabel();
        when(daoHandler.getLabelHandler().create(any())).thenReturn(StorageUtil.labelToEntity(label));
        labelService.createLabel(label);
        verify(labelHandler, times(1)).create(labelCaptor.capture());
        Label result = labelCaptor.getValue();
        assertEquals("TODO", result.getName());
        assertEquals("label", result.getUsername());
    }

    @Test
    public void testRemoveLabel() {
        labelService.removeLabel(TestUtils.EXISTING_TASK_ID);
        verify(labelHandler, times(1)).delete(labelCaptor.capture());
        Label result = labelCaptor.getValue();
        result.setId(TestUtils.EXISTING_LABEL_ID);
        assertEquals(TestUtils.EXISTING_TASK_ID, result.getId());

    }

    @Test
    public void testUpdateLabelName() throws EntityNotFoundException {

        LabelDto label = labelService.getLabel(TestUtils.EXISTING_LABEL_ID);
        label.setId(TestUtils.EXISTING_LABEL_ID);
        label.setName("exo");
        when(daoHandler.getLabelHandler().update(any())).thenReturn(StorageUtil.labelToEntity(label));
        labelService.updateLabel(label, Arrays.asList(Label.FIELDS.NAME));
        verify(labelHandler, times(1)).update(labelCaptor.capture());

        assertEquals("exo", labelCaptor.getValue().getName());
    }

    @Test
    public void testUpdateLabelColor() throws EntityNotFoundException {

        LabelDto label = labelService.getLabel(TestUtils.EXISTING_LABEL_ID);
        label.setId(TestUtils.EXISTING_LABEL_ID);
        label.setColor("white");
        when(daoHandler.getLabelHandler().update(any())).thenReturn(StorageUtil.labelToEntity(label));
        labelService.updateLabel(label, Arrays.asList(Label.FIELDS.COLOR));
        verify(labelHandler, times(1)).update(labelCaptor.capture());

        assertEquals("white", labelCaptor.getValue().getColor());
    }

    @Test
    public void testUpdateLabelPARENT() throws EntityNotFoundException {
        LabelDto parentLabel = labelService.getLabel(TestUtils.EXISTING_LABEL_ID);
        LabelDto label = new LabelDto();
        long labelId = 5;
        label.setId(labelId);
        label.setUsername("root");
        label.setName("testLabel");
        label.setParent(parentLabel);
        when(daoHandler.getLabelHandler().find(labelId)).thenReturn(StorageUtil.labelToEntity(label));
        when(daoHandler.getLabelHandler().update(any())).thenReturn(StorageUtil.labelToEntity(label));
        labelService.updateLabel(label, Collections.singletonList(Label.FIELDS.PARENT));
        verify(labelHandler, times(1)).update(labelCaptor.capture());

        assertEquals(label.getParent(), StorageUtil.labelToDto(labelCaptor.getValue().getParent()));
    }

    @Test
    public void testUpdateLabelHIDDEN() throws EntityNotFoundException {

        LabelDto label = labelService.getLabel(TestUtils.EXISTING_LABEL_ID);
        label.setId(TestUtils.EXISTING_LABEL_ID);
        label.setHidden(true);
        when(daoHandler.getLabelHandler().update(any())).thenReturn(StorageUtil.labelToEntity(label));
        labelService.updateLabel(label, Arrays.asList(Label.FIELDS.HIDDEN));
        verify(labelHandler, times(1)).update(labelCaptor.capture());

        assertEquals(true, labelCaptor.getValue().isHidden());
    }


}

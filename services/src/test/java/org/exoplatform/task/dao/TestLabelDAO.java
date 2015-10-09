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
package org.exoplatform.task.dao;

import java.util.Arrays;
import java.util.List;

import org.exoplatform.commons.utils.ListAccess;
import org.exoplatform.container.PortalContainer;
import org.exoplatform.task.AbstractTest;
import org.exoplatform.task.domain.Label;
import org.exoplatform.task.domain.LabelTaskMapping;
import org.exoplatform.task.domain.Task;
import org.exoplatform.task.util.ListUtil;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class TestLabelDAO extends AbstractTest {

  private LabelHandler lblDAO;
  private DAOHandler taskService;
  private final String username = "root";

  @Before
  public void setup() {
    PortalContainer container = PortalContainer.getInstance();
    
    taskService = (DAOHandler) container.getComponentInstanceOfType(DAOHandler.class);
    lblDAO = taskService.getLabelHandler();
  }

  @After
  public void tearDown() {
    taskService.getLabelTaskMappingHandler().deleteAll();
    lblDAO.deleteAll();
  }
  
  @Test
  public void testAddTask() throws Exception {
    Label label = new Label("test label", username);
    label = lblDAO.create(label);
    Task task = new Task();
    task.setTitle("task1");
    taskService.getTaskHandler().create(task);
    //
    LabelTaskMapping map = new LabelTaskMapping(label, task);
    taskService.getLabelTaskMappingHandler().create(map);
    //find tasks that have label
    ListAccess<Task> tasks = taskService.getTaskHandler().findTasksByLabel(0, null, username, null);
    Assert.assertEquals(1, tasks.getSize());
  }
  
  @Test
  public void testRemoveTask() throws Exception {
    Label label = new Label("test label", username);
    label = lblDAO.create(label);
    Task task = new Task();
    task.setTitle("test");
    taskService.getTaskHandler().create(task);
    //
    LabelTaskMapping map = new LabelTaskMapping(label, task);
    taskService.getLabelTaskMappingHandler().create(map);
    
    ListAccess<Task> tasks = taskService.getTaskHandler().findTasksByLabel(label.getId(), null, username, null);
    Assert.assertEquals(1, tasks.getSize());
    //
    endRequestLifecycle();
    initializeContainerAndStartRequestLifecycle();
    lblDAO.delete(lblDAO.find(label.getId()));
    tasks = taskService.getTaskHandler().findTasksByLabel(label.getId(), null, username, null);
    Assert.assertEquals(0, tasks.getSize());
  }
  
  @Test
  public void testUpdate() {
    Label label = new Label("test label", username);
    label = lblDAO.create(label);
    Assert.assertNotNull(label);
    
    label = lblDAO.find(label.getId());
    Assert.assertNotNull(label);
    
    label.setColor("white");
    lblDAO.update(label);
    label = lblDAO.find(label.getId());
    Assert.assertEquals("white", label.getColor());
    
    label.setName("label2");
    lblDAO.update(label);
    label = lblDAO.find(label.getId());
    Assert.assertEquals("label2", label.getName());    
    
    Label parent = new Label("parent label", "root");
    lblDAO.create(parent);
    label.setParent(parent);
    lblDAO.update(label);
    label = lblDAO.find(label.getId());
    Assert.assertEquals(parent.getId(), label.getParent().getId());    
  }
  
  @Test
  public void testQuery() throws Exception {
    Label label1 = new Label("test label1", "root");
    Label label2 = new Label("test label2", "demo");
    lblDAO.create(label1);    
    lblDAO.create(label2);
    
    //
    ListAccess<Label> labels = lblDAO.findLabelsByUser("root");
    List<Label> list = Arrays.asList(ListUtil.load(labels, 0, -1));
    Assert.assertEquals(1, labels.getSize());
    Assert.assertEquals(label1.getName(), list.get(0).getName());
    labels = lblDAO.findLabelsByUser("demo");
    list = Arrays.asList(ListUtil.load(labels, 0, -1));
    Assert.assertEquals(1, labels.getSize());
    Assert.assertEquals(label2.getName(), list.get(0).getName());
    
    Task task = new Task();
    task.setTitle("task1");
    taskService.getTaskHandler().create(task);
    LabelTaskMapping mapping = new LabelTaskMapping(label1, task);
    taskService.getLabelTaskMappingHandler().create(mapping);
    //
    labels = lblDAO.findLabelsByTask(task.getId(), "root");
    Assert.assertEquals(1, labels.getSize());
  }
}

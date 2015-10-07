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

import java.util.List;

import org.exoplatform.container.PortalContainer;
import org.exoplatform.task.domain.Label;
import org.exoplatform.task.domain.Task;
import org.exoplatform.task.AbstractTest;

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
    lblDAO.deleteAll();
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
    
    Task task = new Task();
    task.setTitle("task1");
    taskService.getTaskHandler().create(task);
    label.getTasks().add(task);
    lblDAO.update(label);
    label = lblDAO.find(label.getId());
    Assert.assertEquals(1, label.getTasks().size());
    
    Label parent = new Label("parent label", "root");
    lblDAO.create(parent);
    label.setParent(parent);
    lblDAO.update(label);
    label = lblDAO.find(label.getId());
    Assert.assertEquals(parent.getId(), label.getParent().getId());    
  }
  
  @Test
  public void testQuery() {
    Label label1 = new Label("test label1", "root");
    Label label2 = new Label("test label2", "demo");
    lblDAO.create(label1);    
    lblDAO.create(label2);
    
    //
    List<Label> labels = lblDAO.findLabelsByUser("root");
    Assert.assertEquals(1, labels.size());
    Assert.assertEquals(label1.getName(), labels.get(0).getName());
    labels = lblDAO.findLabelsByUser("demo");
    Assert.assertEquals(1, labels.size());
    Assert.assertEquals(label2.getName(), labels.get(0).getName());
    
    Task task = new Task();
    task.setTitle("task1");
    taskService.getTaskHandler().create(task);
    label1.getTasks().add(task);
    lblDAO.update(label1);
    labels = lblDAO.findLabelsByTask(task.getId(), "root");
    Assert.assertEquals(1, labels.size());
  }
}

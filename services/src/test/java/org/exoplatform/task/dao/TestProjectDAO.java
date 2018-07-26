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
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.exoplatform.commons.utils.ListAccess;
import org.exoplatform.container.PortalContainer;
import org.exoplatform.task.domain.Project;
import org.exoplatform.task.AbstractTest;

/**
 * @author <a href="trongtt@exoplatform.com">Trong Tran</a>
 * @version $Revision$
 */
public class TestProjectDAO extends AbstractTest {

  private ProjectHandler pDAO;
  private DAOHandler daoHandler;

  @Before
  public void setup() {
    PortalContainer container = PortalContainer.getInstance();
    
    daoHandler = (DAOHandler) container.getComponentInstanceOfType(DAOHandler.class);
    pDAO = daoHandler.getProjectHandler();
  }

  @After
  public void tearDown() {
    pDAO.deleteAll();
  }

  @Test
  public void testProjectCreation() {
    List<Project> all;

    Project p1 = new Project("Test project 1", null, null, null, null);
    pDAO.create(p1);
    all = pDAO.findAll();
    Assert.assertEquals(1, all.size());
    Assert.assertEquals("Test project 1", p1.getName());

    all = pDAO.findAll();
    Assert.assertEquals(1, all.size());
    Assert.assertEquals("Test project 1", p1.getName());
    Project p2 = new Project("Test project 2", null, null, null, null);
    pDAO.create(p2);

    all = pDAO.findAll();
    Assert.assertEquals(2, all.size());
    Assert.assertEquals(p1.getId() + 1, p2.getId());
  }

  @Test
  public void testFindSubProject() throws Exception {
    Project parent = new Project("Parent project", null, null, null, null);
    pDAO.create(parent);

    Project child = new Project("Child project", null, null, null, null);
    child.setParent(parent);
    pDAO.create(child);

    ListAccess<Project> projects = pDAO.findSubProjects(parent);
    Assert.assertEquals(1, projects.getSize());

    projects = pDAO.findSubProjects(null);
    Assert.assertTrue(projects.getSize() >= 1);
  }
  
  @Test
  public void testFindProjectByKeywordWithDifferentCase() throws Exception {
    Set<String> manager = new HashSet<String>();
    manager.add("root");

    ListAccess<Project> projects = pDAO.findAllByMembershipsAndKeyword(Arrays.asList("root"), "test parenT ProJecT", null);
    int initialSize = projects.getSize();

    Project parent = new Project("Test Parent project", null, null, manager, null);
    pDAO.create(parent);

    projects = pDAO.findAllByMembershipsAndKeyword(Arrays.asList("root"), "test parenT ProJecT", null);
    Assert.assertEquals(initialSize + 1, projects.getSize());
  }

  @Test
  public void testFindProjectByKeywordPartiallyMatching() throws Exception {
    Set<String> manager = new HashSet<String>();
    manager.add("root");
    Project parent = new Project("Marketing", null, null, manager, null);
    pDAO.create(parent);

    ListAccess<Project> projects = pDAO.findAllByMembershipsAndKeyword(Arrays.asList("root"), "Mark", null);
    Assert.assertEquals(1, projects.getSize());

    projects = pDAO.findAllByMembershipsAndKeyword(Arrays.asList("root"), "arKet", null);
    Assert.assertEquals(1, projects.getSize());

    projects = pDAO.findAllByMembershipsAndKeyword(Arrays.asList("root"), "keTING", null);
    Assert.assertEquals(1, projects.getSize());
  }
}


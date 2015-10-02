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
package org.exoplatform.task.test.dao;

import java.util.HashSet;
import java.util.Set;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import org.exoplatform.container.PortalContainer;
import org.exoplatform.task.dao.DAOHandler;
import org.exoplatform.task.dao.ProjectHandler;
import org.exoplatform.task.dao.StatusHandler;
import org.exoplatform.task.domain.Project;
import org.exoplatform.task.domain.Status;
import org.exoplatform.task.domain.Task;
import org.exoplatform.task.test.AbstractTest;

public class TestStatusDAO extends AbstractTest {

  private StatusHandler sDAO;
  private ProjectHandler pDAO;
  private DAOHandler daoHandler;

  @Before
  public void setup() {
    PortalContainer container = PortalContainer.getInstance();
    
    daoHandler = (DAOHandler) container.getComponentInstanceOfType(DAOHandler.class);
    sDAO = daoHandler.getStatusHandler();
    pDAO = daoHandler.getProjectHandler();
  }

  @After
  public void tearDown() {
    sDAO.deleteAll();
  }

  @Test
  public void testFindHighestRank() {
    Status s1 = createStatus("s1");
    s1.setRank(0);
    Status s2 = createStatus("s2");
    s2.setRank(1);
    Project p = createProject("p", s1, s2);
    pDAO.create(p);
    
    Status s = sDAO.findHighestRankStatusByProject(p.getId());
    Assert.assertEquals(s2.getId(), s.getId());
  }

  @Test
  public void testFindByName() {
    Status s1 = createStatus("s1");
    Project p = createProject("p", s1);
    pDAO.create(p);
    
    Status s = sDAO.findByName(s1.getName(), p.getId());
    Assert.assertEquals(s1.getId(), s.getId());
  }
  
  private Project createProject(String name, Status ...status) {
    Set<String> managers = new HashSet<String>();
    managers.add("root");

    Set<String> participators = new HashSet<String>();
    participators.add("demo");

    Set<Status> tmp = new HashSet<Status>();
    Project project = new Project(name, "des", tmp, managers, participators);    
    if (status != null) {
      for (Status st : status) {
        st.setProject(project);
        tmp.add(st);
      }
    }

    return project;
  }

  private Status createStatus(String name, Task ...tasks) {
    Status st = new Status();
    st.setName(name);
    st.setRank(1);
    if (tasks != null) {
      Set<Task> set = new HashSet<Task>();
      for (Task t : tasks) {
        t.setStatus(st);
        set.add(t);
      }
      st.setTasks(set);      
    }
    return st;
  }
}


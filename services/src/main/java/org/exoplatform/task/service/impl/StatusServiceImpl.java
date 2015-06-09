/*
 * Copyright (C) 2015 eXo Platform SAS.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */

package org.exoplatform.task.service.impl;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.exoplatform.commons.api.persistence.Transactional;
import org.exoplatform.commons.utils.PropertyManager;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.task.dao.StatusHandler;
import org.exoplatform.task.domain.Project;
import org.exoplatform.task.domain.Status;
import org.exoplatform.task.domain.Task;
import org.exoplatform.task.exception.NotAllowedOperationOnEntityException;
import org.exoplatform.task.exception.StatusNotFoundException;
import org.exoplatform.task.service.DAOHandler;
import org.exoplatform.task.service.StatusService;

@Singleton
public class StatusServiceImpl implements StatusService {

  @Inject
  private DAOHandler daoHandler;
  
  private String[] DEFAULT_STATUS = {"To Do", "In Progress", "Waiting On", "Done"};
  
  private static Log LOG = ExoLogger.getExoLogger(StatusServiceImpl.class);  

  public StatusServiceImpl(DAOHandler daoHandler) {
    String status = PropertyManager.getProperty("exo.tasks.default.status");
    if (status != null) {
      List<String> stList = new LinkedList<String>();
      for (String s : status.split(",")) {
        s = s.trim();
        if (!s.isEmpty()) {
          stList.add(s);
        }
      }

      if (!stList.isEmpty()) {
        DEFAULT_STATUS = stList.toArray(new String[stList.size()]);
      }
    }
    this.daoHandler = daoHandler;
  }

  @Override
  public List<String> getDefaultStatus() {
    return Arrays.asList(DEFAULT_STATUS);
  }

  @Override
  @Transactional
  public Status createStatus(Project project, String name) {
    if (name == null || (name = name.trim()).isEmpty() || project == null) {
      throw new IllegalArgumentException("project must be not null and status must not be null or empty");
    }

    //
    if (project.getStatus() != null) {
      for (Status st : project.getStatus()) {
        if (st.getName().equalsIgnoreCase(name)) {
          LOG.warn("Status {} has already exists", name);
          return st;
        }
      }      
    } else {
      project.setStatus(new HashSet<Status>());
    }

    Status max = daoHandler.getStatusHandler().findHighestRankStatusByProject(project.getId());
    int maxRank = max != null && max.getRank() != null ? max.getRank() : -1;
    
    StatusHandler handler = daoHandler.getStatusHandler();
    Status st = new Status(name, ++maxRank, new HashSet<Task>(), project);    
    project.getStatus().add(st);
    handler.create(st);
    return st;
  }

  @Override
  @Transactional
  public Status deleteStatus(long statusID) throws StatusNotFoundException, NotAllowedOperationOnEntityException {
    StatusHandler handler = daoHandler.getStatusHandler();
    Status st = handler.find(statusID);
    if (st == null) {
      throw new StatusNotFoundException(statusID);
    }
    
    Project project = st.getProject();
    Status altStatus = findAltStatus(st, project);
    if (altStatus == null) {
      throw new NotAllowedOperationOnEntityException(statusID, "status", "Delete last status");
    }
    
    Set<Task> tasks = st.getTasks();
    altStatus.getTasks().addAll(tasks);
    for (Task t : tasks) {
      t.setStatus(altStatus);
      daoHandler.getTaskHandler().update(t);
    }
    //
    st.getTasks().clear();
    handler.delete(st);
    return st;
  }

  @Override
  @Transactional
  public Status updateStatus(long id, String name) throws StatusNotFoundException, NotAllowedOperationOnEntityException {
    if (name == null || (name = name.trim()).isEmpty()) {
      throw new IllegalArgumentException("status name can't be null or empty");
    }
    
    StatusHandler handler = daoHandler.getStatusHandler();
    Status status = handler.find(id);
    if (status == null) {
      throw new StatusNotFoundException(id);
    }
    Status curr = handler.findByName(name, status.getProject().getId());
    if (curr != null && !status.equals(curr)) {
      throw new NotAllowedOperationOnEntityException(status.getId(), "status", "duplicate status name");
    }
    
    status.setName(name);
    return daoHandler.getStatusHandler().update(status);
  }

  @Override
  public Status swapPosition(long statusID, long otherID) throws NotAllowedOperationOnEntityException {
    StatusHandler handler = daoHandler.getStatusHandler();
    Status status = handler.find(statusID);
    Status other = handler.find(otherID);
    //
    if (status == null || other == null) {
      throw new NotAllowedOperationOnEntityException(statusID, "status", "swap with null status");
    }
    
    Integer rank = status.getRank();
    status.setRank(other.getRank());
    other.setRank(rank);
    return status;
  }

  private Status findAltStatus(Status st, Project project) {
    List<Status> allSt = new LinkedList<Status>(project.getStatus());
    Collections.sort(allSt);
    
    Status other = null;
    for (int i = 0; i < allSt.size(); i++) {
      if (allSt.get(i).equals(st)) {
        if (i > 0) {
          other = allSt.get(i - 1);
        } else if (i + 1 < allSt.size()) {
          other = allSt.get(i + 1);
        }
        break;
      }
    }
    return other;
  }
}

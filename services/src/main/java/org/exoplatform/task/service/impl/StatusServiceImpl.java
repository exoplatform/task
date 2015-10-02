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

import javax.inject.Inject;
import javax.inject.Singleton;

import org.exoplatform.commons.api.persistence.ExoTransactional;
import org.exoplatform.commons.utils.ListAccess;
import org.exoplatform.commons.utils.PropertyManager;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.task.dao.DAOHandler;
import org.exoplatform.task.dao.StatusHandler;
import org.exoplatform.task.dao.TaskQuery;
import org.exoplatform.task.domain.Project;
import org.exoplatform.task.domain.Status;
import org.exoplatform.task.domain.Task;
import org.exoplatform.task.exception.EntityNotFoundException;
import org.exoplatform.task.exception.NotAllowedOperationOnEntityException;
import org.exoplatform.task.service.StatusService;
import org.exoplatform.task.util.ListUtil;

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

  private List<String> getDefaultStatus() {
    return Arrays.asList(DEFAULT_STATUS);
  }

  @Override
  public void createDefaultStatuses(Project proj) {
    for (String s : getDefaultStatus()) {
      createStatus(proj, s);
    }
  }

  @Override
  public Status getStatus(long statusId) {
    return daoHandler.getStatusHandler().find(statusId);
  }

  @Override
  public Status getDefaultStatus(long projectId) {
    return daoHandler.getStatusHandler().findLowestRankStatusByProject(projectId);
  }

  @Override
  public List<Status> getStatuses(long projectId) {
    return daoHandler.getStatusHandler().getStatuses(projectId);
  }

  @Override
  @ExoTransactional
  public Status createStatus(Project project, String name) {
    if (name == null || (name = name.trim()).isEmpty() || project == null) {
      throw new IllegalArgumentException("project must be not null and status must not be null or empty");
    }

    //
    List<Status> statuses = getStatuses(project.getId());
    if (statuses != null) {
      for (Status st : statuses) {
        if (st.getName().equalsIgnoreCase(name)) {
          LOG.warn("Status {} has already exists", name);
          return st;
        }
      }      
    }

    Status max = daoHandler.getStatusHandler().findHighestRankStatusByProject(project.getId());
    int maxRank = max != null && max.getRank() != null ? max.getRank() : -1;
    
    StatusHandler handler = daoHandler.getStatusHandler();
    Status st = new Status(name, ++maxRank, new HashSet<Task>(), project);
    handler.create(st);
    return st;
  }

  @Override
  @ExoTransactional
  public Status removeStatus(long statusID) throws EntityNotFoundException, NotAllowedOperationOnEntityException {
    StatusHandler handler = daoHandler.getStatusHandler();
    Status st = handler.find(statusID);
    if (st == null) {
      throw new EntityNotFoundException(statusID, Status.class);
    }
    
    Project project = st.getProject();
    Status altStatus = findAltStatus(st, project);
    if (altStatus == null) {
      throw new NotAllowedOperationOnEntityException(statusID, Status.class, "Delete last status");
    }

    TaskQuery query = new TaskQuery();
    query.setStatus(st);
    ListAccess<Task> tasks = daoHandler.getTaskHandler().findTasks(query);
    for (Task t : ListUtil.load(tasks, 0, -1)) {
      t.setStatus(altStatus);
      daoHandler.getTaskHandler().update(t);
    }

    //
    st.setProject(null);

    handler.delete(st);
    return st;
  }

  @Override
  @ExoTransactional
  public Status updateStatus(long id, String name) throws EntityNotFoundException, NotAllowedOperationOnEntityException {
    if (name == null || (name = name.trim()).isEmpty()) {
      throw new IllegalArgumentException("status name can't be null or empty");
    }
    
    StatusHandler handler = daoHandler.getStatusHandler();
    Status status = handler.find(id);
    if (status == null) {
      throw new EntityNotFoundException(id, Status.class);
    }
    Status curr = handler.findByName(name, status.getProject().getId());
    if (curr != null && !status.equals(curr)) {
      throw new NotAllowedOperationOnEntityException(status.getId(), Status.class, "duplicate status name");
    }
    
    status.setName(name);
    return daoHandler.getStatusHandler().update(status);
  }

  private Status findAltStatus(Status st, Project project) {
    List<Status> allSt = new LinkedList<Status>(getStatuses(project.getId()));
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

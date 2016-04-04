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
package org.exoplatform.task.service.impl;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.exoplatform.commons.api.persistence.ExoTransactional;
import org.exoplatform.commons.utils.ListAccess;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.task.dao.DAOHandler;
import org.exoplatform.task.dao.OrderBy;
import org.exoplatform.task.dao.ProjectQuery;
import org.exoplatform.task.dao.TaskQuery;
import org.exoplatform.task.domain.Project;
import org.exoplatform.task.domain.Status;
import org.exoplatform.task.domain.Task;
import org.exoplatform.task.exception.EntityNotFoundException;
import org.exoplatform.task.service.ProjectService;
import org.exoplatform.task.service.StatusService;
import org.exoplatform.task.service.TaskService;
import org.exoplatform.task.util.ListUtil;

/**
 * Created by The eXo Platform SAS
 * Author : Thibault Clement
 * tclement@exoplatform.com
 * 6/3/15
 */
@Singleton
public class ProjectServiceImpl implements ProjectService {

  private static final Log LOG = ExoLogger.getExoLogger(ProjectServiceImpl.class);

  @Inject
  StatusService statusService;
  
  @Inject
  TaskService taskService;
  
  @Inject
  DAOHandler daoHandler;

  public ProjectServiceImpl() {
  }

  //For testing purpose only
  public ProjectServiceImpl(StatusService statusService, TaskService taskService, DAOHandler daoHandler) {
    this.daoHandler = daoHandler;
    this.statusService = statusService;
    this.taskService = taskService;
  }

  @Override
  @ExoTransactional
  public Project createProject(Project project) {
    Project proj = daoHandler.getProjectHandler().create(project);
    return proj;
  }

  @Override
  @ExoTransactional
  public Project createProject(Project project, long parentId) throws EntityNotFoundException {
    Project parentProject = daoHandler.getProjectHandler().find(parentId);
    if (parentProject != null) {
      project.setParent(parentProject);
      //If parent, list of members/participators of parents override the list of members/participators in parameter
      project.setParticipator(new HashSet<String>(parentProject.getParticipator()));
      //If parent, list of manager of parents override the list of managers in parameter
      project.setManager(new HashSet<String>(parentProject.getManager()));

      //persist project
      project = createProject(project);

      //inherit status from parent
      List<Status> prSt = statusService.getStatuses(parentProject.getId());
      Collections.sort(prSt);
      for (Status st : prSt) {
        statusService.createStatus(project, st.getName());
      }
      return project;
    } else {
      LOG.info("Can not find project for parent with ID: " + parentId);
      throw new EntityNotFoundException(parentId, Project.class);
    }
  }

  @Override
  @ExoTransactional
  public Project updateProject(Project proj) {
    Project obj = daoHandler.getProjectHandler().update(proj);
    return obj;
  }

  @Override
  @ExoTransactional
  public void removeProject(long id, boolean deleteChild) throws EntityNotFoundException {
    Project project = getProject(id);
    if (project == null) throw new EntityNotFoundException(id, Project.class);
    daoHandler.getProjectHandler().removeProject(id, deleteChild);
  }

  @Override
  @ExoTransactional
  public Project cloneProject(long id, boolean cloneTask) throws EntityNotFoundException {

    Project project = getProject(id); //Can throw ProjectNotFoundException

    Project newProject = project.clone(cloneTask);
    Set<String> managers = getManager(id);
    if (managers != null && !managers.isEmpty()) {
      newProject.setManager(managers);
    }
    Set<String> participators = getParticipator(id);
    if (participators != null && !participators.isEmpty()) {
      newProject.setParticipator(participators);
    }
    newProject.setId(0);
    newProject.setName(Project.PREFIX_CLONE + newProject.getName());
    newProject = createProject(newProject);

    //. Get all Status of project
    List<Status> statuses = statusService.getStatuses(id);
    ListAccess<Task> tasks;
    TaskQuery taskQuery;
    if (statuses != null) {
      for(Status st : statuses) {
        Status s = statusService.createStatus(newProject, st.getName());
        if (cloneTask) {
          taskQuery = new TaskQuery();
          taskQuery.setStatus(st);
          tasks = taskService.findTasks(taskQuery);
          if (tasks != null) {
            for (Task t : ListUtil.load(tasks, 0, -1)) {
              Task newTask = t.clone();
              newTask.setId(0);
              newTask.setStatus(s);
              newTask.setCoworker(taskService.getCoworker(t.getId()));
              newTask.setTitle(Task.PREFIX_CLONE + newTask.getTitle());
              taskService.createTask(newTask);
            }
          }
        }
      }
    }

    return newProject;

  }

  @Override
  public Project getProject(Long id) throws EntityNotFoundException {

    Project project = daoHandler.getProjectHandler().find(id);
    if (project == null) throw new EntityNotFoundException(id, Project.class);

    return project;

  }

  @Override
  public ListAccess<Project> getSubProjects(long parentId) {
    try {
      Project parent = getProject(parentId);
      return daoHandler.getProjectHandler().findSubProjects(parent);
    } catch (EntityNotFoundException ex) {
      return new ListAccess<Project>() {
        @Override
        public int getSize() throws Exception {
          return 0;
        }

        @Override
        public Project[] load(int arg0, int arg1) throws Exception, IllegalArgumentException {
          return new Project[0];
        }
      };
    }
  }

  @Override
  public ListAccess<Project> findProjects(ProjectQuery query) {
    return daoHandler.getProjectHandler().findProjects(query);
  }

  @Override
  public ListAccess<Project> findProjects(List<String> memberships, String keyword, OrderBy order) {
    return daoHandler.getProjectHandler().findAllByMembershipsAndKeyword(memberships, keyword, order);
  }

  @Override
  public Set<String> getManager(long projectId) {
    ProjectQuery query = new ProjectQuery();
    query.setId(projectId);
    List<String> manager = daoHandler.getProjectHandler().selectProjectField(query, "manager"); 
    return new HashSet<String>(manager);
  }

  @Override
  public Set<String> getParticipator(long projectId) {
    ProjectQuery query = new ProjectQuery();
    query.setId(projectId);
    List<String> manager = daoHandler.getProjectHandler().selectProjectField(query, "participator"); 
    return new HashSet<String>(manager);
  }
  
}


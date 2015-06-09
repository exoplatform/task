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

import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.services.security.Identity;
import org.exoplatform.services.security.MembershipEntry;
import org.exoplatform.task.dao.OrderBy;
import org.exoplatform.task.dao.StatusHandler;
import org.exoplatform.task.dao.TaskQuery;
import org.exoplatform.task.domain.Project;
import org.exoplatform.task.domain.Status;
import org.exoplatform.task.domain.Task;
import org.exoplatform.task.exception.ParameterEntityException;
import org.exoplatform.task.exception.ProjectNotFoundException;
import org.exoplatform.task.exception.NotAllowedOperationOnEntityException;
import org.exoplatform.task.service.DAOHandler;
import org.exoplatform.task.service.ProjectService;
import org.exoplatform.task.utils.ProjectUtil;
import org.exoplatform.task.utils.UserUtils;

import javax.inject.Inject;
import javax.inject.Singleton;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

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
  DAOHandler daoHandler;

  @Override
  public Project createDefaultStatusProjectWithManager(String name, String description, Long parentId, String username) throws ProjectNotFoundException {

    Set<String> managers = new HashSet<String>();
    managers.add(username);

    return createDefaultStatusProjectWithAttributes(parentId, name, description, managers, Collections.<String>emptySet());

  }

  @Override
  public Project createDefaultStatusProjectWithAttributes(Long parentId, String name, String description, Set<String> managers, Set<String> participators) throws ProjectNotFoundException {

    Project project = new Project(name, description, null, managers, participators);

    if (parentId != null && parentId != 0) {
      Project parentProject = daoHandler.getProjectHandler().find(parentId);
      if (parentProject != null) {
        project.setParent(parentProject);
        //If parent, list of members/participators of parents override the list of members/participators in parameter
        project.setParticipator(new HashSet<String>(parentProject.getParticipator()));
        //If parent, list of manager of parents override the list of managers in parameter
        project.setManager(new HashSet<String>(parentProject.getManager()));
      }
      else {
        LOG.info("Can not find project for parent with ID: " + parentId);
        throw new ProjectNotFoundException(parentId);
        //return Response.content(406, "Can not find project id for parentID = " + parentId);
      }
    }

    return createDefaultStatusProject(project);
  }

  @Override
  public Project createDefaultStatusProject(Project project) {
    Project newProject = daoHandler.getProjectHandler().create(project);
    createDefaultStatusToProject(newProject);
    return newProject;
  }

  @Override
  public Project createProject(Project project) {
    return daoHandler.getProjectHandler().create(project);
  }

  @Override
  public Project updateProjectInfo(long id, String param, String[] values) throws ProjectNotFoundException, ParameterEntityException {

    String val = values != null && values.length > 0 ? values[0] : null;

    Project project = getProjectById(id); //Can throw ProjectNotFoundException

    if("name".equalsIgnoreCase(param)) {
      if(val == null || val.isEmpty()) {
        LOG.info("Name of project must not empty");
        throw new ParameterEntityException(id, "Project", param, val, "must not be empty");
      }
      project.setName(val);
    } else if("manager".equalsIgnoreCase(param)) {
      Set<String> manager = new HashSet<String>();
      if(values != null) {
        for (String v : values) {
          manager.add(v);
        }
      }
      project.setManager(manager);
    } else if("participator".equalsIgnoreCase(param)) {
      Set<String> participator = new HashSet<String>();
      if(values != null || true) {
        for (String v : values) {
          participator.add(v);
        }
      }
      project.setParticipator(participator);
    } else if("dueDate".equalsIgnoreCase(param)) {
      if(val == null || val.isEmpty()) {
        project.setDueDate(null);
      } else {
        DateFormat df = new SimpleDateFormat("yyyy-MM-dd");
        try {
          Date date = df.parse(val);
          project.setDueDate(date);
        } catch (ParseException e) {
          LOG.info("can not parse date string: " + val);
          throw new ParameterEntityException(id, "Project", param, val, "cannot be parse to date");
        }
      }
    } else if("description".equalsIgnoreCase(param)) {
      project.setDescription(val);
    } else if ("color".equalsIgnoreCase(param)) {
      project.setColor(val);
    } else {
      LOG.info("Field name: " + param + " is not supported for entity Project");
      throw new ParameterEntityException(id, "Project", param, val, "is not supported for the entity Project");
    }

    return daoHandler.getProjectHandler().update(project);
  }

  @Override
  public void deleteProjectById(long id, boolean deleteChild) throws ProjectNotFoundException {
    Project project = getProjectById(id); //Can throw ProjectNotFoundException

    deleteProject(project, deleteChild);
  }

  @Override
  public void deleteProject(Project project, boolean deleteChild) {    
    if (!deleteChild && project.getChildren() != null) {
      Project parent = project.getParent();
      for (Project child : project.getChildren()) {
        child.setParent(parent);
      }
      project.getChildren().clear();
    }
    daoHandler.getProjectHandler().delete(project);
  }

  @Override
  public Project cloneProjectById(long id, boolean cloneTask) throws ProjectNotFoundException {

    Project project = getProjectById(id); //Can throw ProjectNotFoundException

    Project newProject = project.clone(cloneTask);
    createProject(newProject);

    return newProject;

  }

  @Override
  public Project getProjectById(Long id) throws ProjectNotFoundException {

    Project project = daoHandler.getProjectHandler().find(id);
    if (project == null) throw new ProjectNotFoundException(id);

    return project;

  }

  @Override
  public Task createTaskToProjectId(long id, Task task) throws ProjectNotFoundException {

    Project project = getProjectById(id); //Can throw ProjectNotFoundException

    Status status = daoHandler.getStatusHandler().findLowestRankStatusByProject(id);
    task.setStatus(status);

    return daoHandler.getTaskHandler().create(task);

  }

  @Override
  public List<Task> getTasksByProjectId(long id, OrderBy orderBy) throws ProjectNotFoundException {
    return getTasksWithKeywordByProjectId(id, orderBy, null); //Can throw ProjectNotFoundException
  }

  @Override
  public List<Task> getTasksWithKeywordByProjectId(long id, OrderBy orderBy, String keyword) throws ProjectNotFoundException {

    Project project = getProjectById(id); //Can throw ProjectNotFoundException

    TaskQuery taskQuery = new TaskQuery();
    taskQuery.setProjectId(project.getId());
    taskQuery.setKeyword(keyword);
    taskQuery.setOrderBy(orderBy == null ? null : Arrays.asList(orderBy));

    return daoHandler.getTaskHandler().findTaskByQuery(taskQuery);
  }

  @Override
  public Project removePermissionFromProjectId(Long id, String permission, String type) throws ProjectNotFoundException, NotAllowedOperationOnEntityException {

    Project project = daoHandler.getProjectHandler().find(id);

    if (project != null) {
      if ("manager".equals(type)) {
        if (project.getManager().size() > 1) {
          project.getManager().remove(permission);
        } else {
          LOG.info("Not allow to remove last manager for project with ID: " + id);
          throw new NotAllowedOperationOnEntityException(id, "Project", "Remove last manager");
        }
      } else {
        project.getParticipator().remove(permission);
      }
      return daoHandler.getProjectHandler().update(project);
    } else {
      LOG.info("Can not find project with ID: " + id);
      throw new ProjectNotFoundException(id);
    }
  }

  @Override
  public Project addPermissionsFromProjectId(Long id, String permissions, String type) throws ProjectNotFoundException, NotAllowedOperationOnEntityException {

    Project project = getProjectById(id);

    if (permissions != null) {
      Set<String> per = new HashSet<String>();
      MembershipEntry entry = MembershipEntry.parse(permissions);
      if (entry != null) {
        per.add(entry.toString());
      } else {
        String[] users = permissions.split(",");
        for (int i = 0; i < users.length; i++) {
          per.add(users[i]);
        }
      }

      if ("manager".equals(type)) {
        project.getManager().addAll(per);
      } else {
        project.getParticipator().addAll(per);
      }

      return daoHandler.getProjectHandler().update(project);

    } else {
      LOG.info("Add permissions equal to null (not allow) to Project with ID: "+id);
      throw new NotAllowedOperationOnEntityException(id, "Project", "Add permission equal to null");
    }

  }

  @Override
  public List<Project> getProjectTreeByIdentity(Identity identity) {
    List<String> memberships = UserUtils.getMemberships(identity);
    List<Project> projects = daoHandler.getProjectHandler().findAllByMemberships(memberships);

    return ProjectUtil.buildRootProjects(projects);
  }
  
  

  private void createDefaultStatusToProject(Project project) {
    StatusHandler statusHandler = daoHandler.getStatusHandler();

    //Todo get default status from property file
    //. Create status for project
    Status status = new Status();
    status.setName("Todo");
    status.setRank(1);
    status.setProject(project);
    statusHandler.create(status);

    status = new Status();
    status.setName("In Progress");
    status.setRank(2);
    status.setProject(project);
    statusHandler.create(status);

    status = new Status();
    status.setName("Done");
    status.setRank(3);
    status.setProject(project);
    statusHandler.create(status);
  }

}


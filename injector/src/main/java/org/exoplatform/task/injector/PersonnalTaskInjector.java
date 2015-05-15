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
package org.exoplatform.task.injector;

import org.exoplatform.container.PortalContainer;
import org.exoplatform.services.bench.DataInjector;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.services.security.ConversationState;
import org.exoplatform.services.security.Identity;
import org.exoplatform.task.domain.Project;
import org.exoplatform.task.domain.Status;
import org.exoplatform.task.domain.Task;
import org.exoplatform.task.service.TaskService;

import java.util.*;

/**
 * Created by The eXo Platform SAS
 * Author : Thibault Clement
 * tclement@exoplatform.com
 * 5/14/15
 */
public class PersonnalTaskInjector extends DataInjector {

  private static final Log LOG = ExoLogger.getLogger(PersonnalTaskInjector.class);

  //Parameters

  private static final String NUMBER_PROJECTS = "nbProject";
  private static final int DEFAULT_NUMBER_PROJECTS = 15;


  private static final String NUMBER_TASKS = "nbTaskPerProject";
  private static final int DEFAULT_NUMBER_TASKS = 42;

  private static final String NUMBER_INCOMING_TASKS = "nbIncmingTask";
  private static final int DEFAULT_NUMBER_INCOMING_TASKS = 10;

  private static final String NUMBER_TAGS = "nbTagPerTask";
  private static final int DEFAULT_NUMBER_TAGS = 2;

  //Service

  private final TaskService taskService;

  //Attribute

  private List<String> statusName = Arrays.asList(new String[]{"To Do", "On Going", "Done"});

  private Random random = new Random();

  private String currentUser;

  private int nbProject;

  private int nbTasks;

  private int nbIncomingTasks;

  private int nbTags;

  public PersonnalTaskInjector() {
    PortalContainer container = PortalContainer.getInstance();
    taskService = (TaskService)container.getComponentInstanceOfType(TaskService.class);
  }

  private void initData() {
    Identity identity = ConversationState.getCurrent().getIdentity();
    currentUser = identity.getUserId();

  }

  private void initParam(HashMap<String, String> params) {
    nbProject = getIntegerParam(params, NUMBER_PROJECTS, DEFAULT_NUMBER_PROJECTS);
    nbTasks = getIntegerParam(params, NUMBER_TASKS, DEFAULT_NUMBER_TASKS);
    nbTags = getIntegerParam(params,NUMBER_TAGS, DEFAULT_NUMBER_TAGS);
    nbIncomingTasks = getIntegerParam(params, NUMBER_INCOMING_TASKS, DEFAULT_NUMBER_INCOMING_TASKS);
  }

  @Override
  public void inject(HashMap<String, String> hashMap) throws Exception {
    LOG.info("Start Inject Data for Task...");
    initParam(hashMap);
    initData();
    LOG.info("Adding "+nbProject+" projects with "+nbTasks+" tasks for user: "+currentUser);
    addTasks();
    LOG.info("... Finish Inject Data for Task");
  }

  @Override
  public void reject(HashMap<String, String> hashMap) throws Exception {

  }

  @Override
  public Log getLog() {
    return LOG;
  }

  @Override
  public Object execute(HashMap<String, String> hashMap) throws Exception {
    return null;
  }

  private void addTasks() {

    //Create tasks associated to personal project of the user
    //Loop on number of projects
    for (int i = 0; i < nbProject; i++) {
      //Create project
      Project project = createProject();
      //Add default status to the project
      List<Status> statuses = createStatus(project);
      Status randomStatus;
      project.setStatus(new HashSet<Status>(statuses));

      //Loop on number of tasks per project
      for (int j = 0; j < nbTasks; j++) {
        Task task = createTask();
        //Add a random status to task
        randomStatus = statuses.get(random.nextInt(3));
        task.setStatus(randomStatus);
        randomStatus.getTasks().add(task);
      }
      taskService.getProjectHandler().create(project);
    }

    //Create Incoming Task (not attached to project) of the user
    List<Task> tasks = new ArrayList<Task>();
    for (int j = 0; j < nbIncomingTasks; j++) {
      Task task = createTask();
      task.setTitle(randomWords(1));
      task.setDescription(randomWords(20));
      task.setCreatedBy(currentUser);
      task.setAssignee(currentUser);
      task.setCompleted(false);
      task.setCreatedTime(new Date());
      //Add tags to Task
      Set<String> tags = new HashSet<String>();
      for (int k = 0; k < nbTags; k++) {
        String tag = randomWords(1);
        tags.add(tag);
      }
      task.setTags(tags);
      tasks.add(task);
    }
    taskService.getTaskHandler().createAll(tasks);
  }

  private Task createTask() {
    Task task = new Task();
    task.setTitle(randomWords(2));
    task.setDescription(randomWords(20));
    task.setCreatedBy(currentUser);
    task.setAssignee(currentUser);
    task.setCompleted(false);
    task.setCreatedTime(new Date());
    //Add tags to Task
    Set<String> tags = new HashSet<String>();
    for (int k = 0; k < nbTags; k++) {
      String tag = randomWords(1);
      tags.add(tag);
    }
    task.setTags(tags);
    return task;
  }

  private Project createProject() {
    Project project = new Project();
    project.setName(randomWords(3));
    project.setDescription(randomWords(20));

    //Add current user as manager of the project
    Set<String> managers = new HashSet<String>();
    managers.add(currentUser);
    project.setManager(managers);

    return project;
  }

  private List<Status> createStatus(Project project) {
    List<Status> statusList = new ArrayList<Status>();
    int rank = 1;

    for (String name : statusName) {
      Status status = new Status();
      status.setName(name);
      status.setRank(rank);

      //Attach status to project
      status.setProject(project);
      project.getStatus().add(status);

      statusList.add(status);

      rank++;
    }
    return statusList;
  }

  protected int getIntegerParam(HashMap<String, String> params, String name, int defaultNb) {

    if (params == null) {
      return defaultNb;
    }

    if (name == null) {
      return defaultNb;
    }

    try {
      String value = params.get(name);
      if (value != null) {
        return Integer.valueOf(value);
      }
    } catch (NumberFormatException e) {
      LOG.warn("Integer number expected for property " + name +" ! Default value will be use instead");
    }
    return defaultNb;

  }

}


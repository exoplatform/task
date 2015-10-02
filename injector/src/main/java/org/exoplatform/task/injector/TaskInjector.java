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
import org.exoplatform.task.domain.Comment;
import org.exoplatform.task.domain.Project;
import org.exoplatform.task.domain.Status;
import org.exoplatform.task.domain.Task;
import org.exoplatform.task.service.ProjectService;
import org.exoplatform.task.service.StatusService;
import org.exoplatform.task.service.TaskService;

import java.util.*;

/**
 * Created by The eXo Platform SAS
 * Author : Thibault Clement
 * tclement@exoplatform.com
 * 5/14/15
 */
public class TaskInjector extends DataInjector {

  private static final Log LOG = ExoLogger.getLogger(TaskInjector.class);

  //Parameters

  private static final String NUMBER_PROJECTS = "nbProject";
  private static final int DEFAULT_NUMBER_PROJECTS = 15;

  private static final String NUMBER_TASKS = "nbTaskPerProject";
  private static final int DEFAULT_NUMBER_TASKS = 42;

  private static final String NUMBER_INCOMING_TASKS = "nbIncomingTask";
  private static final int DEFAULT_NUMBER_INCOMING_TASKS = 10;

  private static final String NUMBER_TAGS = "nbTagPerTask";
  private static final int DEFAULT_NUMBER_TAGS = 2;

  private static final String NUMBER_COMMENTS = "nbComPerTask";
  private static final int DEFAULT_NUMBER_COMMENTS = 2;

  private static final String PERCENTAGE_COMPLETED = "perCompleted";
  private static final int DEFAULT_PERCENTAGE_COMPLETED = 70;

  private static final String TASK_TYPE = "type"; // [user,space]
  private static final String USER_TASK_TYPE = "user";
  private static final String SPACE_TASK_TYPE = "space";
  private static final String DEFAULT_TASK_TYPE = USER_TASK_TYPE;

  private static final String NAME_PREFIX = "prefix";
  private static final String DEFAULT_USER_PREFIX = "bench.user";
  private static final String DEFAULT_SPACE_PREFIX = "bench.space";

  private static final String TASK_PREFIX = "taskPrefix";
  private static final String DEFAULT_TASK_PREFIX = "Task";

  private static final String PROJECT_PREFIX = "projectPrefix";
  private static final String DEFAULT_PROJECT_PREFIX = "Project";

  private static final String NAME_SUFFIX = "suffix";
  private static final int DEFAULT_SUFFIX = 4;

  private static final String USER_FROM = "from";
  private static final int DEFAULT_USER_FROM = 0;

  private static final String USER_TO = "to";
  private static final int DEFAULT_USER_TO = 10;

  //Service

  private final TaskService taskService;
  private final ProjectService projectService;

  //

  private List<String> statusName = Arrays.asList(new String[]{"To Do", "On Going", "Done"});

  private Random random = new Random();

  private int nbProject;

  private int nbTasks;
  private int nbIncomingTasks;

  private int nbTags;
  private int nbComments;
  private int perCompleted;

  private String taskType;

  private String namePrefix;
  private int nameSuffix;
  private String suffixPattern;

  private String taskPrefix;
  private String projectPrefix;

  private int userFrom;
  private int userTo;

  public TaskInjector() {
    PortalContainer container = PortalContainer.getInstance();
    taskService = (TaskService)container.getComponentInstanceOfType(TaskService.class);
    projectService = (ProjectService)container.getComponentInstanceOfType(ProjectService.class);
  }

  private void init() {
    suffixPattern = "%s%0" + nameSuffix + "d";
  }

  private void initParam(HashMap<String, String> params) {
    nbProject = getIntegerParam(params, NUMBER_PROJECTS, DEFAULT_NUMBER_PROJECTS);
    nbTasks = getIntegerParam(params, NUMBER_TASKS, DEFAULT_NUMBER_TASKS);
    nbTags = getIntegerParam(params,NUMBER_TAGS, DEFAULT_NUMBER_TAGS);
    nbComments = getIntegerParam(params, NUMBER_COMMENTS, DEFAULT_NUMBER_COMMENTS);
    perCompleted = getIntegerParam(params, PERCENTAGE_COMPLETED, DEFAULT_PERCENTAGE_COMPLETED);
    nbIncomingTasks = getIntegerParam(params, NUMBER_INCOMING_TASKS, DEFAULT_NUMBER_INCOMING_TASKS);
    taskType = getStringParam(params, TASK_TYPE, DEFAULT_TASK_TYPE);
    if (!USER_TASK_TYPE.equals(taskType) && !SPACE_TASK_TYPE.equals(taskType)) {
      LOG.warn(TASK_TYPE+" parameter ["+taskType+"] is invalid, the default one will be used."+TASK_TYPE
          +" parameter must be equal to "+USER_TASK_TYPE+" (default) or "+SPACE_TASK_TYPE);
      taskType = USER_TASK_TYPE;
    }
    namePrefix = (USER_TASK_TYPE.equals(taskType)) ? getStringParam(params, NAME_PREFIX, DEFAULT_USER_PREFIX) :
        getStringParam(params, NAME_PREFIX, DEFAULT_SPACE_PREFIX);
    nameSuffix = getIntegerParam(params, NAME_SUFFIX, DEFAULT_SUFFIX);
    taskPrefix = getStringParam(params, TASK_PREFIX, DEFAULT_TASK_PREFIX);
    projectPrefix = getStringParam(params, PROJECT_PREFIX, DEFAULT_PROJECT_PREFIX);
    userFrom = getIntegerParam(params, USER_FROM, DEFAULT_USER_FROM);
    userTo = getIntegerParam(params, USER_TO, DEFAULT_USER_TO);
  }

  @Override
  public void inject(HashMap<String, String> hashMap) throws Exception {
    LOG.info("Start Inject Data for Task...");
    initParam(hashMap);
    init();
    //Create personal tasks
    if (USER_TASK_TYPE.equals(taskType)) {
      addPersonnalTasks();
    }
    //Create space tasks
    else {
      addSpaceTasks();
    }
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

  private void addPersonnalTasks() {

    LOG.info("Adding " + nbProject + " personal projects with " + nbTasks + " tasks for "+(userTo-userFrom)
        +" user(s)" );

    for (int i = userFrom; i < userTo; i++) {

      //Get username
      String userName = getUsername(i);

      LOG.info("Add personnal tasks to user: "+userName);

      //Create tasks
      //Create tasks associated to personal project of the user
      //Loop on number of projects
      for (int j = 0; j < nbProject; j++) {
        //Create project
        Project project = createProject(userName, j);
        //Add default status to the project
        List<Status> statuses = createStatus(project);
        Status randomStatus;
        project.setStatus(new HashSet<Status>(statuses));

        //Loop on number of tasks per project
        for (int k = 0; k < nbTasks; k++) {
          Task task = createTask(userName, project.getName(), k);
          //Add a random status to task
          randomStatus = statuses.get(random.nextInt(3));
          task.setStatus(randomStatus);
          randomStatus.getTasks().add(task);
        }
        projectService.createProject(project);
        //DAOHandler.getProjectHandler().create(project);
      }

      //Create Incoming Task (not attached to project) of the user
      for (int j = 0; j < nbIncomingTasks; j++) {
        Task task = createTask(userName, "Incoming-"+userName, j);
        task.setCompleted(false);
        taskService.createTask(task);
      }

      //DAOHandler.getTaskHandler().createAll(tasks);

    }

  }

  private void addSpaceTasks() {

    LOG.info("Adding " + nbProject + " space projects with " + nbTasks + " tasks for "+(userTo-userFrom)+" space(s)" );

    for (int i = userFrom; i < userTo; i++) {

      //Get space name
      String spaceName = getSpaceName(i);
      String manager = "manager:/spaces/" + spaceName;
      String member = "member:/spaces/" + spaceName;

      LOG.info("Add tasks to space: " + spaceName);

      //Create tasks
      //Create tasks associated to personal project of the user
      //Loop on number of projects
      for (int j = 0; j < nbProject; j++) {
        //Create project
        Project project = createProject(manager, j);
        //Add space member, member of the project
        Set<String> members = new HashSet<String>();
        members.add(member);
        project.setParticipator(members);
        //Add default status to the project
        List<Status> statuses = createStatus(project);
        Status randomStatus;
        project.setStatus(new HashSet<Status>(statuses));

        //Loop on number of tasks per project
        for (int k = 0; k < nbTasks; k++) {
          Task task = createTask(manager, project.getName(), k);
          //Add a random status to task
          randomStatus = statuses.get(random.nextInt(3));
          task.setStatus(randomStatus);
          randomStatus.getTasks().add(task);
        }
        projectService.createProject(project);
        //DAOHandler.getProjectHandler().create(project);
      }

    }

  }

  private Task createTask(String username, String project, int numtask) {
    Task task = new Task();
    task.setTitle(taskPrefix+"-"+numtask);
    task.setDescription(randomWords(20));
    task.setCreatedBy(username);
    task.setAssignee(username);
    task.setCreatedTime(new Date());
    //Add tags to Task
    Set<String> tags = new HashSet<String>();
    for (int k = 0; k < nbTags; k++) {
      String tag = randomWords(1)+k;
      tags.add(tag);
    }
    task.setTag(tags);
    //Add comments to Task
    Set<Comment> comments = new HashSet<Comment>();
    for (int i = 0; i < nbComments; i++) {
      Comment comment = new Comment();
      comment.setAuthor(username);
      comment.setComment(randomWords(20));
      comment.setCreatedTime(new Date());
      comment.setTask(task);
      comments.add(comment);
    }
    task.setComments(comments);
    //Set tasks as completed
    if (random.nextInt(100) < perCompleted) {
      task.setCompleted(true);
    }
    return task;
  }

  private Project createProject(String username, int numProject) {
    Project project = new Project();
    project.setName(projectPrefix+"-"+username+"-"+numProject);
    project.setDescription(randomWords(20));

    //Add current user as manager of the project
    Set<String> managers = new HashSet<String>();
    managers.add(username);
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

  private String getUsername(int userNumber) {
      return String.format(suffixPattern, namePrefix, userNumber);
  }

  private String getSpaceName(int spaceNumber) {
    return String.format(suffixPattern, namePrefix.replace(".", ""), spaceNumber);
  }

  private int getIntegerParam(HashMap<String, String> params, String name, int defaultNb) {

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

  protected String getStringParam(HashMap<String, String> params, String name, String defaultParam) {

    if (params == null) {
      return defaultParam;
    }

    if (name == null) {
      return defaultParam;
    }

    String value = params.get(name);
    if (value != null) {
      return value;
    }

    return defaultParam;

  }

}


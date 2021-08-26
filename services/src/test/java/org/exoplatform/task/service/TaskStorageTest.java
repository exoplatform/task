package org.exoplatform.task.service;

import static org.junit.Assert.assertEquals;

import java.util.Arrays;
import java.util.Date;
import java.util.HashSet;
import java.util.List;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import org.exoplatform.container.PortalContainer;
import org.exoplatform.services.listener.ListenerService;
import org.exoplatform.task.AbstractTest;
import org.exoplatform.task.dao.CommentHandler;
import org.exoplatform.task.dao.DAOHandler;
import org.exoplatform.task.dao.LabelHandler;
import org.exoplatform.task.dao.TaskHandler;
import org.exoplatform.task.domain.Project;
import org.exoplatform.task.domain.Status;
import org.exoplatform.task.domain.Task;
import org.exoplatform.task.dto.TaskDto;
import org.exoplatform.task.service.impl.TaskServiceImpl;
import org.exoplatform.task.storage.ProjectStorage;
import org.exoplatform.task.storage.TaskStorage;
import org.exoplatform.task.storage.impl.ProjectStorageImpl;
import org.exoplatform.task.storage.impl.TaskStorageImpl;

public class TaskStorageTest extends AbstractTest {

  private TaskHandler     tDAO;

  private CommentHandler  cDAO;

  private DAOHandler      daoHandler;

  private final String    username = "root";

  private LabelHandler    labelHandler;

  private ListenerService listenerService;

  private ProjectStorage  projectStorage;

  private UserService     userService;

  private TaskStorage     taskStorage;

  private TaskService     taskService;

  @Before
  public void setup() {
    PortalContainer container = PortalContainer.getInstance();

    daoHandler = (DAOHandler) container.getComponentInstanceOfType(DAOHandler.class);
    tDAO = daoHandler.getTaskHandler();
    cDAO = daoHandler.getCommentHandler();
    projectStorage = new ProjectStorageImpl(daoHandler);
    labelHandler = daoHandler.getLabelHandler();
    taskStorage = new TaskStorageImpl(daoHandler, userService, projectStorage);
    taskService = new TaskServiceImpl(taskStorage, daoHandler, listenerService);
  }

  @After
  public void tearDown() {
    for (Task t : tDAO.findAll()) {
      t.setStatus(null);
    }
    tDAO.updateAll(tDAO.findAll());
    daoHandler.getLabelTaskMappingHandler().deleteAll();
    daoHandler.getTaskLogHandler().deleteAll();
    cDAO.deleteAll();
    tDAO.deleteAll();
    labelHandler.deleteAll();
  }

  @Test
  public void findTaskWithMembership() throws Exception {
    // Given
    Project project = new Project();
    project.setName("Project1");

    project.setParticipator(new HashSet<String>(Arrays.asList("jhon")));

    project = daoHandler.getProjectHandler().create(project);
    Status status = newStatusInstance("TO DO", 1);
    status.setProject(project);
    daoHandler.getStatusHandler().create(status);
    Task task1 = newTaskInstance("Task 1", "", "root");
    task1.setStatus(status);
    tDAO.create(task1);
    Project project2 = new Project();
    project2.setName("Project2");
    project2 = daoHandler.getProjectHandler().create(project2);
    Status status1 = newStatusInstance("TO DO", 1);
    status1.setProject(project2);
    daoHandler.getStatusHandler().create(status1);
    Task task2 = newTaskInstance("Task 2", "", "root");
    task2.setStatus(status1);
    tDAO.create(task2);
    List<String> memberships = Arrays.asList("jhon");

    // When
    List<TaskDto> tasks = taskStorage.findTasks("jhon", memberships, "Task", 10);

    // Then
    assertEquals(1, tasks.size());

  }

  private Status newStatusInstance(String name, int rank) {
    Status status = new Status();
    status.setName(name);
    status.setRank(rank);
    return status;
  }

  private Task newTaskInstance(String taskTitle, String description, String assignee) {
    Task task = new Task();
    task.setTitle(taskTitle);
    task.setDescription(description);
    task.setAssignee(assignee);
    task.setCreatedBy(username);
    task.setCreatedTime(new Date());
    return task;
  }

}

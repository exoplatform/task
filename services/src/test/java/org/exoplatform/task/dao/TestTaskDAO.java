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

import org.exoplatform.commons.utils.ListAccess;
import org.exoplatform.container.PortalContainer;
import org.exoplatform.services.listener.ListenerService;
import org.exoplatform.services.security.ConversationState;
import org.exoplatform.services.security.Identity;
import org.exoplatform.task.AbstractTest;
import org.exoplatform.task.dao.condition.Conditions;
import org.exoplatform.task.domain.*;
import org.exoplatform.task.exception.EntityNotFoundException;
import org.exoplatform.task.service.TaskService;
import org.exoplatform.task.service.UserService;
import org.exoplatform.task.service.impl.TaskServiceImpl;
import org.exoplatform.task.storage.ProjectStorage;
import org.exoplatform.task.storage.StatusStorage;
import org.exoplatform.task.storage.TaskStorage;
import org.exoplatform.task.storage.impl.TaskStorageImpl;
import org.exoplatform.task.util.ListUtil;
import org.exoplatform.task.util.TaskUtil;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;


import java.util.*;

/**
 * @author <a href="trongtt@exoplatform.com">Trong Tran</a>
 * @version $Revision$
 */
public class TestTaskDAO extends AbstractTest {

  private TaskHandler tDAO;
  private CommentHandler cDAO;
  private DAOHandler daoHandler;
  private final String username = "root";
  private LabelHandler labelHandler;
  private ListenerService listenerService;
  private ProjectStorage projectStorage;
  private UserService userService;
  private TaskStorage taskStorage;
  private TaskService taskService;

  @Before
  public void setup() {
    PortalContainer container = PortalContainer.getInstance();

    daoHandler = (DAOHandler) container.getComponentInstanceOfType(DAOHandler.class);
    tDAO = daoHandler.getTaskHandler();
    cDAO = daoHandler.getCommentHandler();
    labelHandler = daoHandler.getLabelHandler();
    taskStorage =new TaskStorageImpl(daoHandler, userService, projectStorage);
    taskService =new TaskServiceImpl(taskStorage, daoHandler, listenerService);
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
  public void testTaskCreation() {
    Task task = new Task();
    task.setTitle("Testing task creation");
    tDAO.create(task);

    List<Task> list = tDAO.findAll();
    Assert.assertEquals(1, list.size());

    //
    task = new Task();
    task.setTitle("There is an important meeting tomorrow !high");
    tDAO.create(task);
    list = tDAO.findAll();
    Assert.assertEquals(2, list.size());

    //
    task = tDAO.find(task.getId());
    Assert.assertNotNull(task);
    Assert.assertEquals("There is an important meeting tomorrow !high", task.getTitle());
    Assert.assertEquals(Priority.NORMAL, task.getPriority());
  }

  @Test
  public void testFindTaskByQuery() throws Exception {
    Task task = newTaskInstance("Test find task by query", "description of find task by query", "root");
    tDAO.create(task);

    TaskQuery query = new TaskQuery();
    query.setTitle("task");
    ListAccess<Task> tasks = tDAO.findTasks(query);
    Assert.assertTrue(tasks.getSize() > 0);

    query = new TaskQuery();
    query.setTitle("testFindTaskByQuery0123456789");
    tasks = tDAO.findTasks(query);
    Assert.assertEquals(0, tasks.getSize());
    Assert.assertEquals(0, ListUtil.load(tasks, 0, -1).length);

    query = new TaskQuery();
    query.setDescription("description of find task by query");
    tasks = tDAO.findTasks(query);
    Assert.assertTrue(tasks.getSize() > 0);
    Assert.assertTrue(ListUtil.load(tasks, 0, -1).length > 0);

    query = new TaskQuery();
    query.setDescription("testFindTaskByQuery0123456789");
    tasks = tDAO.findTasks(query);
    Assert.assertEquals(0, tasks.getSize());
    Assert.assertEquals(0, ListUtil.load(tasks, 0, -1).length);

    query = new TaskQuery();
    query.setAssignee(Arrays.asList("root"));
    tasks = tDAO.findTasks(query);
    Assert.assertTrue(tasks.getSize() > 0);
    Assert.assertTrue(ListUtil.load(tasks, 0, -1).length > 0);

    query = new TaskQuery();
    query.setAssignee(Arrays.asList("testFindTaskByQuery0123456789"));
    tasks = tDAO.findTasks(query);
    Assert.assertEquals(0, tasks.getSize());
    Assert.assertEquals(0, ListUtil.load(tasks, 0, -1).length);

    query = new TaskQuery();
    query.setKeyword("find task by query");
    tasks = tDAO.findTasks(query);
    Assert.assertTrue(tasks.getSize() > 0);
    Assert.assertTrue(ListUtil.load(tasks, 0, -1).length > 0);


    query = new TaskQuery();
    query.setKeyword("testFindTaskByQuery0123456789");
    tasks = tDAO.findTasks(query);
    Assert.assertEquals(0, tasks.getSize());
    Assert.assertEquals(0, ListUtil.load(tasks, 0, -1).length);
    
    query = new TaskQuery();
    query.setKeyword(" Find  QUERY");
    tasks = tDAO.findTasks(query);
    Assert.assertEquals(1, tasks.getSize());
    Assert.assertEquals(1, ListUtil.load(tasks, 0, -1).length);
  }  

  @Test
  public void testFindTaskOrderByDueDate() throws Exception {
    Calendar calendar = Calendar.getInstance();

    Task task = newTaskInstance("task 1", "description of task 1", "root");
    task.setDueDate(calendar.getTime());
    tDAO.create(task);

    task = newTaskInstance("task 2", "description of task 2", "root");
    calendar.add(Calendar.DATE, 1);
    task.setDueDate(calendar.getTime());
    tDAO.create(task);

    task = newTaskInstance("task 3", "description of task 3", "root");
    calendar.add(Calendar.DATE, -2);
    task.setDueDate(calendar.getTime());
    tDAO.create(task);


    task = newTaskInstance("task 4", "description of task 4", "root");
    calendar.add(Calendar.DATE, 3);
    task.setDueDate(calendar.getTime());
    tDAO.create(task);


    task = newTaskInstance("task 5", "description of task 5", "root");
    tDAO.create(task);


    TaskQuery taskQuery = new TaskQuery();
    taskQuery.setAssignee(Arrays.asList("root"));
    OrderBy order = new OrderBy.ASC(Conditions.TASK_DUEDATE);
    taskQuery.setOrderBy(Arrays.asList(order));

    ListAccess<Task> list = tDAO.findTasks(taskQuery);

    Assert.assertEquals(5, list.getSize());

    Task[] tasks = list.load(0, -1);
    Assert.assertEquals("task 3", tasks[0].getTitle());
    Assert.assertEquals("task 1", tasks[1].getTitle());
    Assert.assertEquals("task 2", tasks[2].getTitle());
    Assert.assertEquals("task 4", tasks[3].getTitle());
    Assert.assertEquals("task 5", tasks[4].getTitle());
  }

  @Test
  public void testFindTaskByQueryAdvance() throws Exception {
    Task task = newTaskInstance("testTask", "task with label", username);
    tDAO.create(task);

    TaskQuery query = new TaskQuery();
    query.setEmptyField("lblMapping");
    ListAccess<Task> tasks = tDAO.findTasks(query);
    Assert.assertEquals(1, tasks.getSize());
    
    //Find by label
    Label label = new Label("testLabel", username);    
    labelHandler.create(label);
    LabelTaskMapping mapping = new LabelTaskMapping(label, task);
    daoHandler.getLabelTaskMappingHandler().create(mapping);
    
    //
    query = new TaskQuery();
    query.setLabelIds(Arrays.asList(label.getId()));
    tasks = tDAO.findTasks(query);
    Assert.assertEquals(1, tasks.getSize());
    
    //Find by status
    Project project = new Project();
    project.setName("Project1");
    project.setParticipator(new HashSet<String>(Arrays.asList("root")));
    project = daoHandler.getProjectHandler().create(project);
    Status status = newStatusInstance("TO DO", 1);
    status.setProject(project);
    status = daoHandler.getStatusHandler().create(status);
    task.setStatus(status);
    tDAO.update(task);
    //
    query = new TaskQuery();
    query.setStatus(status);
    tasks = tDAO.findTasks(query);
    Assert.assertEquals(1, tasks.getSize());
    
    //Find by assigneeOrProjectId
    query = new TaskQuery();
    query.setAssigneeOrCoworkerOrInProject(username, Arrays.asList(-100L));
    tasks = tDAO.findTasks(query);
    Assert.assertEquals(1, tasks.getSize());
    //
    query = new TaskQuery();
    query.setAssigneeOrCoworkerOrInProject("test", Arrays.asList(project.getId()));
    tasks = tDAO.findTasks(query);
    Assert.assertEquals(1, tasks.getSize());
    
    //Find by duedate
    Date date = new Date();
    task.setDueDate(date);
    tDAO.update(task);
    //
    query = new TaskQuery();
    query.setDueDateFrom(date);
    tasks = tDAO.findTasks(query);
    Assert.assertEquals(1, tasks.getSize());

    //Find by priority
    task.setPriority(Priority.HIGH);
    tDAO.update(task);
    //
    query = new TaskQuery();
    query.setPriority(Priority.HIGH);
    tasks = tDAO.findTasks(query);
    Assert.assertEquals(1, tasks.getSize());
    
    //Find by assignee
    query = new TaskQuery();
    query.setAssignee(Arrays.asList(username));
    tasks = tDAO.findTasks(query);
    Assert.assertEquals(1, tasks.getSize());

    //Find completed
    task.setCompleted(true);
    tDAO.update(task);
    //
    query = new TaskQuery();
    query.setCompleted(true);
    tasks = tDAO.findTasks(query);
    Assert.assertEquals(1, tasks.getSize());    
  }

  @Test
  public void testFindTaskByMentionedUser() throws Exception {
    Task task = newTaskInstance("testTask", "task with label", username);
    tDAO.create(task);

    Comment comment = new Comment();
    comment.setAuthor(username);
    comment.setComment("test comment @mary test");
    comment.setCreatedTime(new Date());
    comment.setTask(task);
    cDAO.create(comment);

    TaskQuery query = new TaskQuery();
    query.setIsTodoOf("mary");
    ListAccess<Task> tasks = tDAO.findTasks(query);
    Assert.assertEquals(1, tasks.getSize());
    //
    query = new TaskQuery();
    query.setIsIncomingOf("mary");
    tasks = tDAO.findTasks(query);
    Assert.assertEquals(1, tasks.getSize());

    Task task2 = newTaskInstance("testTask2", "task with label", "mary");
    tDAO.create(task2);
    //
    query = new TaskQuery();
    query.setIsTodoOf("mary");
    tasks = tDAO.findTasks(query);
    Assert.assertEquals(2, tasks.getSize());
    //
    query = new TaskQuery();
    query.setIsIncomingOf("mary");
    tasks = tDAO.findTasks(query);
    Assert.assertEquals(2, tasks.getSize());

    query = new TaskQuery();
    query.setIsTodoOf(username);
    tasks = tDAO.findTasks(query);
    Assert.assertEquals(1, tasks.getSize());


  }

  @Test
  public void testFindTaskByMembership() {
    Project project = new Project();
    project.setName("Project1");
    project.setParticipator(new HashSet<String>(Arrays.asList("root")));
    project = daoHandler.getProjectHandler().create(project);

    Status status = newStatusInstance("TO DO", 1);
    status.setProject(project);
    daoHandler.getStatusHandler().create(status);
    
    Task task1 = newTaskInstance("Task 1", "", username);
    task1.setStatus(status);
    tDAO.create(task1);
    
    TaskQuery query = new TaskQuery();
    query.setMemberships(Arrays.asList("root"));
    ListAccess<Task> listTasks = tDAO.findTasks(query);
    Assert.assertEquals(1, ListUtil.getSize(listTasks));
    
    //
    query = new TaskQuery();
    Identity user = new Identity(username);
    query.setAccessible(user);
    query.setKeyword("Task");
    listTasks = tDAO.findTasks(query);
    Assert.assertEquals(1, ListUtil.getSize(listTasks));
    
    //The only thing related to "root" of task2 is he is creator
    //This is for the case user create task in label then search by unified search
    Task task2 = newTaskInstance("task2", "", null);
    tDAO.create(task2);
    Label label = new Label("label1", username);
    labelHandler.create(label);
    LabelTaskMapping mapping = new LabelTaskMapping(label, task2);
    daoHandler.getLabelTaskMappingHandler().create(mapping);
    //
    
    query = new TaskQuery();
    query.setAccessible(user);
    query.setKeyword("Task");
    listTasks = tDAO.findTasks(query);
    Assert.assertEquals(2, ListUtil.getSize(listTasks));
  }
  
  @Test
  public void testFindTasksByLabel() throws Exception {
    Project project = new Project();
    project.setName("Project1");
    daoHandler.getProjectHandler().create(project);

    Status status = newStatusInstance("TO DO", 1);
    status.setProject(project);
    daoHandler.getStatusHandler().create(status);
    
    Task task = newTaskInstance("task1", "", username);
    task.setStatus(status);
    tDAO.create(task);
    Label label = new Label("label1", username);
    labelHandler.create(label);
    //
    LabelTaskMapping mapping = new LabelTaskMapping(label, task);
    daoHandler.getLabelTaskMappingHandler().create(mapping);

    ListAccess<Task> tasks = tDAO.findTasksByLabel(label.getId(), Arrays.asList(project.getId()), username, null);
    Assert.assertEquals(1, tasks.getSize());
    Assert.assertEquals(task.getId(), tasks.load(0, -1)[0].getId());   
    
    daoHandler.getLabelTaskMappingHandler().delete(mapping);
    tasks = tDAO.findTasksByLabel(label.getId(), Arrays.asList(project.getId()), username, null);
    Assert.assertEquals(0, tasks.getSize());
  }
  
  @Test
  public void testRemoveTaskLabel() throws Exception {
    Task task = newTaskInstance("task1", "", username);
    tDAO.create(task);
    Label label = new Label("label1", username);
    labelHandler.create(label);
    //
    LabelTaskMapping mapping = new LabelTaskMapping(label, task);
    daoHandler.getLabelTaskMappingHandler().create(mapping);
    
    ListAccess<Label> labels = labelHandler.findLabelsByTask(task.getId(), username);
    Assert.assertEquals(1, labels.getSize());
    //
    endRequestLifecycle();
    initializeContainerAndStartRequestLifecycle();
    tDAO.delete(tDAO.find(task.getId()));
    labels = labelHandler.findLabelsByTask(task.getId(), username);
    Assert.assertEquals(0, labels.getSize());
  }

  @Test
  public void testGetIncomingTask() {
    Project project = new Project();
    project.setName("Project1");

    project = daoHandler.getProjectHandler().create(project);

    Status status = newStatusInstance("TO DO", 1);
    status.setProject(project);
    daoHandler.getStatusHandler().create(status);

    Task task1 = newTaskInstance("Task 1", "", username);
    tDAO.create(task1);

    Task task2 = newTaskInstance("Task 2", "", username);
    task2.setStatus(status);
    tDAO.create(task2);

    TaskQuery query = new TaskQuery();
    //query.setIsIncoming(Boolean.TRUE);
    //query.setUsername(username);
    query.setIsIncomingOf(username);
    ListAccess<Task> listTasks = tDAO.findTasks(query); //tDAO.getIncomingTasks(username, null);
    List<Task> tasks = Arrays.asList(ListUtil.load(listTasks, 0, -1)); //tDAO.getIncomingTask(username, null);
    assertContain(tasks, task1.getId());
    assertNotContain(tasks, task2.getId());

  }

  @Test
  public void testGetTodoTask() {
    Project project = new Project();
    project.setName("Project1");
    daoHandler.getProjectHandler().create(project);

    Status status = newStatusInstance("TO DO", 1);
    status.setProject(project);
    daoHandler.getStatusHandler().create(status);

    Task task1 = newTaskInstance("Task 1", "", null);
    tDAO.create(task1);

    Task task2 = newTaskInstance("Task 2", "", null);
    task2.setStatus(status);
    tDAO.create(task2);

    Task task3 = newTaskInstance("Task 3", "", username);
    task3.setDueDate(new Date());
    tDAO.create(task3);

    Task task4 = newTaskInstance("Task 4", "", username);
    task4.setDueDate(new Date());
    task4.setStatus(status);
    tDAO.create(task4);

    Task task5 = newTaskInstance("Task 5", "", username);
    task5.setStatus(status);
    task5.setCompleted(true);
    tDAO.create(task5);

    TaskQuery query = new TaskQuery();
    //query.setIsTodo(Boolean.TRUE);
    //query.setUsername(username);
    query.setIsTodoOf(username);
    query.setCompleted(false);
    ListAccess<Task> listTasks = tDAO.findTasks(query); //tDAO.getToDoTasks(username, null, null, null, null);
    List<Task> tasks = Arrays.asList(ListUtil.load(listTasks, 0, -1)); //tDAO.getToDoTask(username, null, null, null, null);

    assertContain(tasks, task3.getId());
    assertContain(tasks, task4.getId());
    assertNotContain(tasks, task1.getId());
    assertNotContain(tasks, task2.getId());
    assertNotContain(tasks, task5.getId());
  }
  
  @Test
  public void testTaskLog() throws EntityNotFoundException {
    Task task = newTaskInstance("Task 1", "", null);
    tDAO.create(task);

    TaskLogHandler logHandler = daoHandler.getTaskLogHandler();

    ListAccess<ChangeLog> logs = logHandler.findTaskLogs(task.getId());

    Assert.assertEquals(0, ListUtil.getSize(logs));
    
    ChangeLog log = new ChangeLog();
    log.setTask(task);
    log.setAuthor("root");
    log.setActionName("has created task");

    logHandler.create(log);
    
    //
    logs = logHandler.findTaskLogs(task.getId());
    Assert.assertEquals(1,  ListUtil.getSize(logs));
  }
  
  @Test
  public void testTaskField() throws Exception {
    Task task = newTaskInstance("Task 1", "", null);

    Set<String> coworkers = new HashSet<String>();
    coworkers.add("worker1");
    task.setCoworker(coworkers);
    //
    tDAO.create(task);

    Assert.assertEquals(1, tDAO.getCoworker(task.getId()).size());    
  }

  @Test
  public void testGetTaskWithCoworkers() throws Exception {
    Task task = newTaskInstance("Task 1", "", null);

    Set<String> coworkers = new HashSet<String>();
    coworkers.add("worker1");
    task.setCoworker(coworkers);
    tDAO.create(task);
    Assert.assertEquals(1, tDAO.getTaskWithCoworkers(task.getId()).getCoworker().size());

    coworkers.add("worker2");
    coworkers.add("worker3");
    task.setCoworker(coworkers);
    Assert.assertEquals(3, tDAO.getTaskWithCoworkers(task.getId()).getCoworker().size());

    coworkers.remove("worker3");
    task.setCoworker(coworkers);
    Assert.assertEquals(2, tDAO.getTaskWithCoworkers(task.getId()).getCoworker().size());
  }

  @Test
  public void testTaskPermission() throws Exception {
    Task task = newTaskInstance("Task 1", "", "worker");
    Identity worker = new Identity("worker");
    Identity worker1 = new Identity("worker1");
    Identity worker2 = new Identity("worker2");
    Identity worker3 = new Identity("worker3");

    Set<String> coworkers = new HashSet<String>();
    coworkers.add("worker1");
    task.setCoworker(coworkers);
    task.setCreatedBy("worker3");
    tDAO.create(task);
    //worker1 is a coworker, so he has permission
    ConversationState.setCurrent(new ConversationState(worker1));
    Assert.assertEquals(true, TaskUtil.hasEditPermission(taskService,task));

    //worker is the assignee, so he has permission
    ConversationState.setCurrent(new ConversationState(worker));
    Assert.assertEquals(true, TaskUtil.hasEditPermission(taskService,task));

    //worker2 is not the assignee, neither coworker nor the creator, so he has not permission
    ConversationState.setCurrent(new ConversationState(worker2));
    Assert.assertEquals(false, TaskUtil.hasEditPermission(taskService,task));

    //worker3 is the creator, so he has permission
    ConversationState.setCurrent(new ConversationState(worker3));
    Assert.assertEquals(true, TaskUtil.hasEditPermission(taskService,task));

    coworkers.add("worker2");
    task.setCoworker(coworkers);
    //worker2 is now a coworker, so he has permission
    ConversationState.setCurrent(new ConversationState(worker2));
    Assert.assertEquals(true, TaskUtil.hasEditPermission(taskService,task));
  }

  @Test
  public void testSelectFieldInTask() throws Exception {
    Project project = new Project();
    project.setName("Project1");

    project = daoHandler.getProjectHandler().create(project);

    Status status = newStatusInstance("TO DO", 1);
    status.setProject(project);
    Status status1 = newStatusInstance("IN Progress", 2);
    status1.setProject(project);

    daoHandler.getStatusHandler().create(status);
    daoHandler.getStatusHandler().create(status1);

    Task task1 = newTaskInstance("Task 1", "", null);
    tDAO.create(task1);

    Task task2 = newTaskInstance("Task 2", "", null);
    task2.setStatus(status);
    tDAO.create(task2);

    Task task3 = newTaskInstance("Task 3", "", username);
    task3.setDueDate(new Date());
    tDAO.create(task3);

    Task task4 = newTaskInstance("Task 4", "", username);
    task4.setDueDate(new Date());
    task4.setStatus(status1);
    tDAO.create(task4);

    Task task5 = newTaskInstance("Task 4", "", username);
    task5.setStatus(status1);
    task5.setCompleted(true);
    tDAO.create(task5);

    TaskQuery taskQuery = new TaskQuery();
    taskQuery.setProjectIds(Arrays.asList(project.getId()));
    
    ListAccess<Task> tasks = tDAO.findTasks(taskQuery);
    System.out.println("****************************" + tasks.getSize());

//    List<Status> statuses = tDAO.selectTaskField(taskQuery, "status");
//    Assert.assertEquals(2, statuses.size());
//
//    taskQuery = new TaskQuery();
//    taskQuery.setAssignee(username);
//
//    List<Project> projects = tDAO.selectTaskField(taskQuery, "status.project");
//    Assert.assertEquals(1, projects.size());
  }

  @Test
  public void testGetUncompletedTasks() {
    Task task1 = newTaskInstance("Task 1", "", username);
    task1.setCompleted(false);
    tDAO.create(task1);

    Task task2 = newTaskInstance("Task 2", "", username);
    task2.setCompleted(false);
    tDAO.create(task2);

    Task task3 = newTaskInstance("Task 3", "", null);
    Set<String> coworker = new HashSet<>();
    coworker.add(username);
    task3.setCoworker(coworker);
    task3.setCompleted(false);
    tDAO.create(task3);

    Task task4 = newTaskInstance("Task 4", "", username);
    task4.setCompleted(true);
    tDAO.create(task4);

    Task task5 = newTaskInstance("Task 5", "", username);
    task5.setCompleted(true);
    tDAO.create(task5);

    assertNotNull(tDAO.getUncompletedTasks(username, -1));
    // test countUncompletedTasks
    assertEquals("should be 3 uncompleted tasks", Long.valueOf(3L), tDAO.countUncompletedTasks(username));
  }

  @Test
  public void testGetOverdueTasks() {

    Calendar calendar = Calendar.getInstance();

    Task task1 = newTaskInstance("task 1", "description of task 1", username);
    calendar = Calendar.getInstance();
    calendar.add(Calendar.DAY_OF_MONTH, -5);
    task1.setDueDate(calendar.getTime());
    tDAO.create(task1);

    Task task2 = newTaskInstance("task 2", "description of task 2", username);
    calendar = Calendar.getInstance();
    calendar.add(Calendar.DAY_OF_MONTH, +2);
    task2.setDueDate(calendar.getTime());
    tDAO.create(task2);

    Task task5 = newTaskInstance("task 5", "description of task 5", null);
    Set<String> coworker = new HashSet<>();
    coworker.add(username);
    task5.setCoworker(coworker);
    calendar = Calendar.getInstance();
    calendar.add(Calendar.DAY_OF_MONTH, -2);
    task5.setDueDate(calendar.getTime());
    tDAO.create(task5);

    assertNotNull(tDAO.getOverdueTasks(username, -1));
    // test countOverdueTasks
    assertEquals("should be 2 overdue tasks", Long.valueOf(2L), tDAO.countOverdueTasks(username));
  }

  @Test
  public void testAddWatchersToTasks() throws Exception {
    // Test Add watchers to task
    String user="john";
    Task task = newTaskInstance("task ", "description of task ", username);
    tDAO.create(task);
    Set<String> watchers = new HashSet<>();
    tDAO.addWatcherToTask(user,task);
    watchers=tDAO.getWatchersOfTask(task);
    assertEquals(1, watchers.size());
    assertTrue(watchers.contains(user));
  }

  @Test
  public void testDeleteWatchersToTasks() throws Exception {
    // Test Delete watcher From task
    String user="john";
    Task task = newTaskInstance("task ", "description of task", username);
    tDAO.create(task);
    //Add watcher to task
    tDAO.addWatcherToTask(user,task);
    Set<String> watchers=tDAO.getWatchersOfTask(task);
    assertEquals(1, watchers.size());
    // Delete watcher from task
    tDAO.deleteWatcherOfTask(user,task);
    watchers=tDAO.getWatchersOfTask(task);
    assertEquals(0, watchers.size());
  }

  @Test
  public void testGetWatchersOfTask() throws Exception{
    // Test GetWatchers of task
    Task task = newTaskInstance("Task ", "", null);
    Set<String> watchers = new HashSet<String>();
    tDAO.addWatcherToTask("john",task);
    tDAO.create(task);
    //Add watcher to task
    tDAO.addWatcherToTask("marry",task);
    tDAO.update(task);

    //Then Test getWatchersOfTask after adding a watcher
    Assert.assertEquals(2, tDAO.getWatchersOfTask(task).size());
    assertTrue(tDAO.getWatchersOfTask(task).contains("marry"));

    //Delete watcher from task
    tDAO.deleteWatcherOfTask("marry",task);
    tDAO.update(task);
    //Then Test getWatchersOfTask after deleting a watcher
    Assert.assertEquals(1, tDAO.getWatchersOfTask(task).size());
    assertFalse(tDAO.getWatchersOfTask(task).contains("marry"));
  }

  @Test
  public void testFindTasks() throws Exception {
    String user="john";
    Task task = newTaskInstance("What is Lorem Ipsum?", "Lorem Ipsum is simply dummy text of the printing", user);
    tDAO.create(task);

    List<Task> tasks = tDAO.findTasks(user, "Lorem", 10);
    assertEquals(1, tasks.size());

    tasks = tDAO.findTasks(user, "lorem", 10);
    assertEquals(1, tasks.size());

    tasks = tDAO.findTasks(user, "simply", 10);
    assertEquals(1, tasks.size());

    tasks = tDAO.findTasks("root", "simply", 10);
    assertEquals(1, tasks.size());

    tasks = tDAO.findTasks("mary", "simply", 10);
    assertEquals(0, tasks.size());

    tasks = tDAO.findTasks(user, "example", 10);
    assertEquals(0, tasks.size());
  }

  @Test
  public void testCountTasks() throws Exception {
    String user="john";
    Task task = newTaskInstance("What is Lorem Ipsum?", "Lorem Ipsum is simply dummy text of the printing", user);
    tDAO.create(task);

    assertEquals(1, tDAO.countTasks(user, "Lorem"));
    assertEquals(1, tDAO.countTasks(user, "lorem"));
    assertEquals(1, tDAO.countTasks(user, "simply"));
    assertEquals(1, tDAO.countTasks("root", "simply"));
    assertEquals(0, tDAO.countTasks("mary", "simply"));
    assertEquals(0, tDAO.countTasks(user, "example"));
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

  private Status newStatusInstance(String name, int rank) {
    Status status = new Status();
    status.setName(name);
    status.setRank(rank);
    return status;
  }

  private void assertContain(List<Task> tasks, Long taskId) {
    for(Task t : tasks) {
      if(t.getId() == taskId) {
        return;
      }
    }
    Assert.fail("Task with ID " + taskId  + " should exist on the list");
  }

  private void assertNotContain(List<Task> tasks, Long taskId) {
    for(Task t : tasks) {
      if(t.getId() == taskId) {
        Assert.fail("Task with ID " + taskId  + " should not exist on the list");
      }
    }
  }
}

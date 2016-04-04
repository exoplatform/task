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
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.TimeZone;

import org.exoplatform.commons.utils.ListAccess;
import org.exoplatform.container.PortalContainer;
import org.exoplatform.services.security.Identity;
import org.exoplatform.task.AbstractTest;
import org.exoplatform.task.domain.Label;
import org.exoplatform.task.domain.LabelTaskMapping;
import org.exoplatform.task.domain.Priority;
import org.exoplatform.task.domain.Project;
import org.exoplatform.task.domain.Status;
import org.exoplatform.task.domain.Task;
import org.exoplatform.task.domain.ChangeLog;
import org.exoplatform.task.exception.EntityNotFoundException;
import org.exoplatform.task.service.ParserContext;
import org.exoplatform.task.service.TaskParser;
import org.exoplatform.task.service.impl.TaskParserImpl;
import org.exoplatform.task.util.ListUtil;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * @author <a href="trongtt@exoplatform.com">Trong Tran</a>
 * @version $Revision$
 */
public class TestTaskDAO extends AbstractTest {

  private TaskHandler tDAO;
  private DAOHandler daoHandler;
  private TaskParser parser = new TaskParserImpl();
  private ParserContext context = new ParserContext(TimeZone.getDefault());

  private final String username = "root";
  private LabelHandler labelHandler;

  @Before
  public void setup() {
    PortalContainer container = PortalContainer.getInstance();
    
    daoHandler = (DAOHandler) container.getComponentInstanceOfType(DAOHandler.class);
    tDAO = daoHandler.getTaskHandler();
    labelHandler = daoHandler.getLabelHandler(); 
  }

  @After
  public void tearDown() {
    for (Task t : tDAO.findAll()) {
      t.setStatus(null);
    }    
    tDAO.updateAll(tDAO.findAll());
    daoHandler.getLabelTaskMappingHandler().deleteAll();
    daoHandler.getTaskLogHandler().deleteAll();
    tDAO.deleteAll();
    labelHandler.deleteAll();
  }

  @Test
  public void testTaskCreation() {
    Task task = parser.parse("Testing task creation", context);
    tDAO.create(task);

    List<Task> list = tDAO.findAll();
    Assert.assertEquals(1, list.size());

    //
    task = parser.parse("There is an important meeting tomorrow !high", context);
    tDAO.create(task);
    list = tDAO.findAll();
    Assert.assertEquals(2, list.size());

    //
    task = tDAO.find(task.getId());
    Assert.assertNotNull(task);
    Assert.assertEquals("There is an important meeting tomorrow", task.getTitle());
    Assert.assertEquals(Priority.HIGH, task.getPriority());
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

  private Task newTaskInstance(String taskTitle, String description, String assignee) {
    Task task = new Task();
    task.setTitle(taskTitle);
    task.setDescription(description);
    task.setAssignee(assignee);
    task.setCreatedBy(username);
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

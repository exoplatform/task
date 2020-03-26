package org.exoplatform.task.rest;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.ws.rs.core.Response;
import javax.ws.rs.ext.RuntimeDelegate;

import org.json.JSONArray;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import org.exoplatform.commons.utils.ListAccess;
import org.exoplatform.services.rest.impl.RuntimeDelegateImpl;
import org.exoplatform.services.security.ConversationState;
import org.exoplatform.services.security.Identity;
import org.exoplatform.task.domain.Comment;
import org.exoplatform.task.domain.Label;
import org.exoplatform.task.domain.Project;
import org.exoplatform.task.domain.Status;
import org.exoplatform.task.domain.Task;
import org.exoplatform.task.model.CommentModel;
import org.exoplatform.task.service.ProjectService;
import org.exoplatform.task.service.StatusService;
import org.exoplatform.task.service.TaskService;
import org.exoplatform.task.service.UserService;
import org.exoplatform.task.util.ProjectUtil;

@RunWith(MockitoJUnitRunner.class)
public class TestTaskRestService {
  @Mock
  TaskService    taskService;

  @Mock
  ProjectService projectService;

  @Mock
  StatusService  statusService;

  @Mock
  UserService    userService;

  @Before
  public void setup() {
    RuntimeDelegate.setInstance(new RuntimeDelegateImpl());
  }

  @Test
  public void testGetTasks() throws Exception {
    // Given
    TaskRestService taskRestService = new TaskRestService(taskService, projectService, statusService, userService);
    Identity root = new Identity("root");
    ConversationState.setCurrent(new ConversationState(root));
    Task task1 = new Task();
    Task task2 = new Task();
    Task task3 = new Task();
    Task task4 = new Task();
    List<Task> uncompletedTasks = new ArrayList<Task>();
    task1.setCompleted(true);
    uncompletedTasks.add(task2);
    uncompletedTasks.add(task3);
    uncompletedTasks.add(task4);
    List<Task> overdueTasks = new ArrayList<Task>();
    overdueTasks.add(task1);
    overdueTasks.add(task2);
    List<Task> incomingTasks = new ArrayList<Task>();
    incomingTasks.add(task1);
    incomingTasks.add(task2);

    ListAccess<Task> incomingTasksListAccess = new ListAccess<Task>() {
      @Override
      public Task[] load(int i, int i1) {
        return new Task[] { incomingTasks.get(0), incomingTasks.get(1) };
      }

      @Override
      public int getSize() throws Exception {
        return incomingTasks.size();
      }
    };

    when(taskService.getUncompletedTasks("root")).thenReturn(uncompletedTasks);
    when(taskService.countUncompletedTasks("root")).thenReturn(Long.valueOf(uncompletedTasks.size()));
    when(taskService.getOverdueTasks("root")).thenReturn(overdueTasks);
    when(taskService.countOverdueTasks("root")).thenReturn(Long.valueOf(overdueTasks.size()));
    when(taskService.getIncomingTasks("root")).thenReturn(incomingTasksListAccess);
    when(taskService.countOverdueTasks("root")).thenReturn(Long.valueOf(incomingTasksListAccess.getSize()));

    // When
    Response response = taskRestService.getTasks("overdue", 0, 20, false);
    Response response1 = taskRestService.getTasks("incoming", 0, 20, false);
    Response response2 = taskRestService.getTasks("", 0, 20, false);

    // Then
    assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
    List<Task> tasks = (List<Task>) response.getEntity();
    assertNotNull(tasks);
    assertEquals(2, tasks.size());

    assertEquals(Response.Status.OK.getStatusCode(), response1.getStatus());
    List<Task> tasks1 = (List<Task>) response1.getEntity();
    assertNotNull(tasks1);
    assertEquals(2, tasks1.size());

    assertEquals(Response.Status.OK.getStatusCode(), response2.getStatus());
    List<Task> tasks2 = (List<Task>) response2.getEntity();
    assertNotNull(tasks2);
    assertEquals(3, tasks2.size());

  }

  @Test
  public void testUpdateTaskById() throws Exception {
    // Given
    TaskRestService taskRestService = new TaskRestService(taskService, projectService, statusService, userService);
    Identity john = new Identity("john");
    ConversationState.setCurrent(new ConversationState(john));
    Task task1 = new Task();
    Task task2 = new Task();
    task1.setId(1);
    task1.setTitle("oldTask");
    task1.setCreatedBy("john");
    task1.setAssignee("john");
    task2.setId(2);
    task2.setTitle("updatedTask");
    taskService.createTask(task1);
    taskService.createTask(task2);

    when(taskService.getTask(1)).thenReturn(task1);
    when(taskService.updateTask(task2)).thenReturn(task2);

    // When
    Response response = taskRestService.updateTaskById(1, task2);

    // Then
    assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
    Task task = (Task) response.getEntity();
    assertNotNull(task);
    assertEquals("updatedTask", task.getTitle());
  }

  @Test
  public void testGetProjects() throws Exception {
    // Given
    TaskRestService taskRestService = new TaskRestService(taskService, projectService, statusService, userService);
    Identity root = new Identity("root");
    ConversationState.setCurrent(new ConversationState(root));

    Project project1 = new Project();
    project1.setName("project1");
    projectService.createProject(project1);
    Project project2 = new Project();
    project2.setName("project2");
    projectService.createProject(project2);
    Project project3 = new Project();
    project3.setName("project3");
    projectService.createProject(project3);
    
    // When
    Response response = taskRestService.getProjects();

    // Then
    assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
    assertNotNull(response.getEntity());
  }

  @Test
  public void testGetDefaultStatusByProjectId() throws Exception {
    // Given
    TaskRestService taskRestService = new TaskRestService(taskService, projectService, statusService, userService);
    Identity john = new Identity("john");
    ConversationState.setCurrent(new ConversationState(john));
    Project project = new Project();
    project.setId(1);
    Set<String> manager = new HashSet<String>();
    manager.add("john");
    project.setManager(manager);
    Status status = new Status();
    status.setId(1);
    status.setName("status 1");

    when(projectService.getProject(1L)).thenReturn(project);
    when(statusService.getDefaultStatus(1L)).thenReturn(status);

    // When
    Response response = taskRestService.getDefaultStatusByProjectId(1);

    // Then
    assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
    Status status1 = (Status) response.getEntity();
    assertNotNull(status1);
    assertEquals("status 1", status1.getName());
  }

  @Test
  public void testGetLabels() throws Exception {
    // Given
    TaskRestService taskRestService = new TaskRestService(taskService, projectService, statusService, userService);
    Identity root = new Identity("root");
    ConversationState.setCurrent(new ConversationState(root));

    Label label1 = new Label();
    label1.setId(1);
    label1.setName("label1");

    Label label2 = new Label();
    label2.setId(2);
    label1.setName("label2");

    Label label3 = new Label();
    label3.setId(3);
    label1.setName("label3");

    List<Label> labels = new ArrayList<Label>();
    labels.add(label1);
    labels.add(label2);
    labels.add(label3);

    ListAccess<Label> labelsListAccess = new ListAccess<Label>() {
      @Override
      public Label[] load(int offset, int limit) {
        return new Label[] { labels.get(0), labels.get(1), labels.get(2) };
      }

      @Override
      public int getSize() throws Exception {
        return labels.size();
      }
    };
    
    when(taskService.findLabelsByUser(root.getUserId())).thenReturn(labelsListAccess);
    // When
    Response response = taskRestService.getLabels();

    // Then
    assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
    List<Label> labelList = (List<Label>) response.getEntity();
    assertNotNull(labelList);
    assertEquals(3, labelList.size());
  }

  @Test
  public void getLabelsByTaskId() throws Exception {
    // Given
    TaskRestService taskRestService = new TaskRestService(taskService, projectService, statusService, userService);
    Identity root = new Identity("root");
    ConversationState.setCurrent(new ConversationState(root));

    Label label1 = new Label();
    label1.setId(1);
    label1.setName("label1");

    Label label2 = new Label();
    label2.setId(2);
    label1.setName("label2");

    Label label3 = new Label();
    label3.setId(3);
    label1.setName("label3");

    List<Label> labels = new ArrayList<Label>();
    labels.add(label1);
    labels.add(label2);
    labels.add(label3);

    ListAccess<Label> labelsListAccess = new ListAccess<Label>() {
      @Override
      public Label[] load(int offset, int limit) {
        return new Label[] { labels.get(0), labels.get(1), labels.get(2) };
      }

      @Override
      public int getSize() throws Exception {
        return labels.size();
      }
    };

    when(taskService.findLabelsByTask(1, root.getUserId())).thenReturn(labelsListAccess);
    // When
    Response response = taskRestService.getLabelsByTaskId(1);

    // Then
    assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
    List<Label> labelList = (List<Label>) response.getEntity();
    assertNotNull(labelList);
    assertEquals(3, labelList.size());
  }

  @Test
  public void testAddTaskToLabel() throws Exception {
    // Given
    TaskRestService taskRestService = new TaskRestService(taskService, projectService, statusService, userService);
    Identity root = new Identity("root");
    ConversationState.setCurrent(new ConversationState(root));

    Task task = new Task();
    task.setId(1);
    task.setCreatedBy("root");
    task.setAssignee("root");
    taskService.createTask(task);

    Label label1 = new Label();
    label1.setId(1);
    label1.setName("label1");

    Label label2 = new Label();
    label2.setId(2);;
    label2.setName("label1");

    when(taskService.createLabel(label1)).thenReturn(label1);
    when(taskService.getTask(1)).thenReturn(task);

    // When
    Response response = taskRestService.addTaskToLabel(label1, 1);

    // Then
    assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
    Label addedLabel = (Label) response.getEntity();
    assertNotNull(addedLabel);
  }

  @Test
  public void testAddTaskComment() throws Exception {
    // Given
    TaskRestService taskRestService = new TaskRestService(taskService, projectService, statusService, userService);
    Identity john = new Identity("john");
    ConversationState.setCurrent(new ConversationState(john));

    Task task = new Task();
    task.setId(1);
    task.setCreatedBy("john");
    task.setAssignee("john");
    taskService.createTask(task);

    Comment comment = new Comment();
    comment.setId(1);

    when(taskService.addComment(1, john.getUserId(), "commentText")).thenReturn(comment);
    when(taskService.getTask(1)).thenReturn(task);
    // When
    Response response = taskRestService.addTaskComment("commentText", 1);

    // Then
    assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
    CommentModel commentModel = (CommentModel) response.getEntity();
    assertNotNull(commentModel);
  }

  @Test
  public void testAddTaskSubComment() throws Exception {
    // Given
    TaskRestService taskRestService = new TaskRestService(taskService, projectService, statusService, userService);
    Identity john = new Identity("john");
    ConversationState.setCurrent(new ConversationState(john));

    Task task = new Task();
    task.setId(1);
    task.setCreatedBy("john");
    task.setAssignee("john");
    taskService.createTask(task);

    Comment comment = new Comment();
    comment.setId(1);
    comment.setComment("commentText");

    when(taskService.addComment(1, 1, john.getUserId(), "commentText")).thenReturn(comment);
    when(taskService.getTask(1)).thenReturn(task);
    // When
    Response response = taskRestService.addTaskSubComment("commentText", 1, 1);

    // Then
    assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
    CommentModel commentModel = (CommentModel) response.getEntity();
    assertNotNull(commentModel);
  }
}

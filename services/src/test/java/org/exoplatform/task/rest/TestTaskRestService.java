package org.exoplatform.task.rest;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import java.util.*;

import javax.ws.rs.core.Response;
import javax.ws.rs.ext.RuntimeDelegate;

import org.exoplatform.social.core.space.spi.SpaceService;

import org.exoplatform.task.dto.*;
import org.exoplatform.task.rest.model.CommentEntity;
import org.exoplatform.task.service.*;
import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import org.exoplatform.commons.utils.ListAccess;
import org.exoplatform.services.rest.impl.RuntimeDelegateImpl;
import org.exoplatform.services.security.ConversationState;
import org.exoplatform.services.security.Identity;
import org.exoplatform.task.legacy.service.UserService;

@RunWith(MockitoJUnitRunner.class)
public class TestTaskRestService {
  @Mock
  TaskService taskService;

  @Mock
  ProjectService projectService;

  @Mock
  StatusService statusService;

  @Mock
  UserService    userService;

  @Mock
  SpaceService spaceService;

  @Mock
  CommentService commentService;

  @Mock
  LabelService labelService;

  @Before
  public void setup() {
    RuntimeDelegate.setInstance(new RuntimeDelegateImpl());
  }

  @Test
  public void testGetTasks() throws Exception {
    // Given
    TaskRestService taskRestService = new TaskRestService(taskService, commentService, projectService, statusService, userService, spaceService, labelService);
    Identity root = new Identity("root");
    ConversationState.setCurrent(new ConversationState(root));
    TaskDto task1 = new TaskDto();
    TaskDto task2 = new TaskDto();
    TaskDto task3 = new TaskDto();
    TaskDto task4 = new TaskDto();
    List<TaskDto> uncompletedTasks = new ArrayList<TaskDto>();
    task1.setCompleted(true);
    uncompletedTasks.add(task2);
    uncompletedTasks.add(task3);
    uncompletedTasks.add(task4);
    List<TaskDto> overdueTasks = new ArrayList<TaskDto>();
    overdueTasks.add(task1);
    overdueTasks.add(task2);
    List<TaskDto> incomingTasks = new ArrayList<TaskDto>();
    incomingTasks.add(task1);
    incomingTasks.add(task2);


    when(taskService.getUncompletedTasks("root", 20)).thenReturn(uncompletedTasks);
    when(taskService.countUncompletedTasks("root")).thenReturn(Long.valueOf(uncompletedTasks.size()));
    when(taskService.getOverdueTasks("root", 20)).thenReturn(overdueTasks);
    when(taskService.countOverdueTasks("root")).thenReturn(Long.valueOf(overdueTasks.size()));
    when(taskService.getIncomingTasks("root",0,20)).thenReturn(incomingTasks);
    when(taskService.countIncomingTasks("root")).thenReturn(incomingTasks.size());
    when(taskService.findTasks(eq("root"), eq("searchTerm"), anyInt())).thenReturn(Collections.singletonList(task4));
    when(taskService.countTasks(eq("root"), eq("searchTerm"))).thenReturn(1L);

    // When
    Response response = taskRestService.getTasks("overdue", null, 0, 20, false, false);
    Response response1 = taskRestService.getTasks("incoming", null, 0, 20, false, false);
    Response response2 = taskRestService.getTasks("", null, 0, 20, false, false);
    Response response3 = taskRestService.getTasks("whatever", "searchTerm", 0, 20, true, false);

    // Then
    assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
    List<TaskDto> tasks = (List<TaskDto>) response.getEntity();
    assertNotNull(tasks);
    assertEquals(2, tasks.size());

    assertEquals(Response.Status.OK.getStatusCode(), response1.getStatus());
    List<TaskDto> tasks1 = (List<TaskDto>) response1.getEntity();
    assertNotNull(tasks1);
    assertEquals(2, tasks1.size());

    assertEquals(Response.Status.OK.getStatusCode(), response2.getStatus());
    List<TaskDto> tasks2 = (List<TaskDto>) response2.getEntity();
    assertNotNull(tasks2);
    assertEquals(3, tasks2.size());

    assertEquals(Response.Status.OK.getStatusCode(), response3.getStatus());
    JSONObject tasks3JsonObject = (JSONObject) response3.getEntity();
    assertNotNull(tasks3JsonObject);
    assertTrue(tasks3JsonObject.has("size"));
    assertTrue(tasks3JsonObject.has("tasks"));
    JSONArray tasks3 = (JSONArray) tasks3JsonObject.get("tasks");
    assertNotNull(tasks3);
    assertEquals(1, tasks3.length());
    Long tasks3Size = (Long) tasks3JsonObject.get("size");
    assertEquals(1L, tasks3Size.longValue());
  }

  @Test
  public void testUpdateTaskById() throws Exception {
    // Given
    TaskRestService taskRestService = new TaskRestService(taskService, commentService, projectService, statusService, userService, spaceService, labelService);
    Identity john = new Identity("john");
    ConversationState.setCurrent(new ConversationState(john));
    TaskDto task1 = new TaskDto();
    TaskDto task2 = new TaskDto();
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
    TaskDto task = (TaskDto) response.getEntity();
    assertNotNull(task);
    assertEquals("updatedTask", task.getTitle());
  }

  @Test
  public void testGetProjects() throws Exception {
    // Given
    TaskRestService taskRestService = new TaskRestService(taskService, commentService, projectService, statusService, userService, spaceService, labelService);
    Identity root = new Identity("root");
    ConversationState.setCurrent(new ConversationState(root));

    ProjectDto project1 = new ProjectDto();
    project1.setName("project1");
    projectService.createProject(project1);
    ProjectDto project2 = new ProjectDto();
    project2.setName("project2");
    projectService.createProject(project2);
    ProjectDto project3 = new ProjectDto();
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
    TaskRestService taskRestService = new TaskRestService(taskService, commentService, projectService, statusService, userService, spaceService, labelService);
    Identity john = new Identity("john");
    ConversationState.setCurrent(new ConversationState(john));
    ProjectDto project = new ProjectDto();
    project.setId(1);
    Set<String> manager = new HashSet<String>();
    manager.add("john");
    project.setManager(manager);
    StatusDto status = new StatusDto();
    status.setId(1);
    status.setName("status 1");

    when(projectService.getProject(1L)).thenReturn(project);
    when(statusService.getDefaultStatus(1L)).thenReturn(status);

    // When
   /* Response response = taskRestService.getDefaultStatusByProjectId(1);

    // Then
    assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
    Status status1 = (Status) response.getEntity();
    assertNotNull(status1);
    assertEquals("status 1", status1.getName());*/
  }

  @Test
  public void testGetLabels() throws Exception {
    // Given
    TaskRestService taskRestService = new TaskRestService(taskService, commentService, projectService, statusService, userService, spaceService, labelService);
    Identity root = new Identity("root");
    ConversationState.setCurrent(new ConversationState(root));

    LabelDto label1 = new LabelDto();
    label1.setId(1);
    label1.setName("label1");

    LabelDto label2 = new LabelDto();
    label2.setId(2);
    label1.setName("label2");

    LabelDto label3 = new LabelDto();
    label3.setId(3);
    label1.setName("label3");

    List<LabelDto> labels = new ArrayList<LabelDto>();
    labels.add(label1);
    labels.add(label2);
    labels.add(label3);

    ListAccess<LabelDto> labelsListAccess = new ListAccess<LabelDto>() {
      @Override
      public LabelDto[] load(int offset, int limit) {
        return new LabelDto[] { labels.get(0), labels.get(1), labels.get(2) };
      }

      @Override
      public int getSize() throws Exception {
        return labels.size();
      }
    };
    
    when(labelService.findLabelsByUser(root.getUserId())).thenReturn(labelsListAccess);
    // When
    Response response = taskRestService.getLabels();

    // Then
    assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
    List<LabelDto> labelList = (List<LabelDto>) response.getEntity();
    assertNotNull(labelList);
    assertEquals(3, labelList.size());
  }

  @Test
  public void getLabelsByTaskId() throws Exception {
    // Given
    TaskRestService taskRestService = new TaskRestService(taskService, commentService, projectService, statusService, userService, spaceService, labelService);
    Identity root = new Identity("root");
    ConversationState.setCurrent(new ConversationState(root));

    LabelDto label1 = new LabelDto();
    label1.setId(1);
    label1.setName("label1");

    LabelDto label2 = new LabelDto();
    label2.setId(2);
    label1.setName("label2");

    LabelDto label3 = new LabelDto();
    label3.setId(3);
    label1.setName("label3");

    List<LabelDto> labels = new ArrayList<LabelDto>();
    labels.add(label1);
    labels.add(label2);
    labels.add(label3);

    ListAccess<LabelDto> labelsListAccess = new ListAccess<LabelDto>() {
      @Override
      public LabelDto[] load(int offset, int limit) {
        return new LabelDto[] { labels.get(0), labels.get(1), labels.get(2) };
      }

      @Override
      public int getSize() throws Exception {
        return labels.size();
      }
    };

    when(labelService.findLabelsByTask(1, root.getUserId())).thenReturn(labelsListAccess);
    // When
    Response response = taskRestService.getLabelsByTaskId(1);

    // Then
    assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
    List<LabelDto> labelList = (List<LabelDto>) response.getEntity();
    assertNotNull(labelList);
    assertEquals(3, labelList.size());
  }

  @Test
  public void testAddTaskToLabel() throws Exception {
    // Given
    TaskRestService taskRestService = new TaskRestService(taskService, commentService, projectService, statusService, userService, spaceService, labelService);
    Identity root = new Identity("root");
    ConversationState.setCurrent(new ConversationState(root));

    TaskDto task = new TaskDto();
    task.setId(1);
    task.setCreatedBy("root");
    task.setAssignee("root");
    taskService.createTask(task);

    LabelDto label1 = new LabelDto();
    label1.setId(1);
    label1.setName("label1");

    LabelDto label2 = new LabelDto();
    label2.setId(2);;
    label2.setName("label1");

    when(labelService.createLabel(label1)).thenReturn(label1);
    when(taskService.getTask(1)).thenReturn(task);

    // When
    Response response = taskRestService.addTaskToLabel(label1, 1);

    // Then
    assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
    LabelDto addedLabel = (LabelDto) response.getEntity();
    assertNotNull(addedLabel);
  }

  @Test
  public void testAddTaskComment() throws Exception {
    // Given
    TaskRestService taskRestService = new TaskRestService(taskService, commentService, projectService, statusService, userService, spaceService, labelService);
    Identity john = new Identity("john");
    ConversationState.setCurrent(new ConversationState(john));

    TaskDto task = new TaskDto();
    task.setId(1);
    task.setCreatedBy("john");
    task.setAssignee("john");
    taskService.createTask(task);

    CommentDto comment = new CommentDto();
    comment.setId(1);
    // Sending non empty comment
    when(commentService.addComment(task, john.getUserId(), "commentText")).thenReturn(comment);
    when(taskService.getTask(1)).thenReturn(task);
    Response response = taskRestService.addTaskComment("commentText", 1);
    assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
    CommentEntity commentModel = (CommentEntity) response.getEntity();
    assertNotNull(commentModel);
    assertEquals(commentModel.getFormattedComment(),"commentText");

    // Sending an empty comment
    response = taskRestService.addTaskComment("", 1);
    assertEquals(Response.Status.BAD_REQUEST.getStatusCode(), response.getStatus());

    // Sending an encoded comment
    response = taskRestService.addTaskComment("x%20%3C%3D%202", 1);
    assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
    commentModel = (CommentEntity) response.getEntity();
    assertNotNull(commentModel);
    assertEquals(commentModel.getFormattedComment(),"x <= 2");

  }

  @Test
  public void testAddTaskSubComment() throws Exception {
    // Given
    TaskRestService taskRestService = new TaskRestService(taskService, commentService, projectService, statusService, userService, spaceService, labelService);
    Identity john = new Identity("john");
    ConversationState.setCurrent(new ConversationState(john));

    TaskDto task = new TaskDto();
    task.setId(1);
    task.setCreatedBy("john");
    task.setAssignee("john");
    taskService.createTask(task);

    CommentDto comment = new CommentDto();
    comment.setId(1);
    comment.setComment("commentText");

    when(commentService.addComment(task, 1, john.getUserId(), "commentText")).thenReturn(comment);
    when(taskService.getTask(1)).thenReturn(task);

    // Sending non empty subcomment
    Response response = taskRestService.addTaskSubComment("commentText", 1, 1);
    assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
    CommentEntity commentModel = (CommentEntity) response.getEntity();
    assertNotNull(commentModel);
    assertEquals(commentModel.getFormattedComment(),"commentText");

    // Sending an empty subcomment
    response = taskRestService.addTaskSubComment("", 1,1);
    assertEquals(Response.Status.BAD_REQUEST.getStatusCode(), response.getStatus());

    // Sending an encoded subcomment
    response = taskRestService.addTaskSubComment("x%20%3C%3D%202", 1,1);
    assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
    commentModel = (CommentEntity) response.getEntity();
    assertNotNull(commentModel);
    assertEquals(commentModel.getFormattedComment(),"x <= 2");

  }
}

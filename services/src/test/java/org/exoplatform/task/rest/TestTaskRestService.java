package org.exoplatform.task.rest;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import java.util.*;

import javax.ws.rs.core.Response;
import javax.ws.rs.ext.RuntimeDelegate;

import org.exoplatform.task.TestUtils;
import org.exoplatform.task.dao.TaskQuery;
import org.exoplatform.task.domain.Priority;
import org.exoplatform.task.model.User;
import org.exoplatform.task.rest.model.PaginatedTaskList;
import org.exoplatform.task.rest.model.ViewState;
import org.exoplatform.task.util.CommentUtil;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import org.exoplatform.commons.utils.ListAccess;
import org.exoplatform.services.rest.impl.RuntimeDelegateImpl;
import org.exoplatform.services.security.ConversationState;
import org.exoplatform.services.security.Identity;
import org.exoplatform.social.core.space.spi.SpaceService;
import org.exoplatform.task.dto.*;
import org.exoplatform.task.service.UserService;
import org.exoplatform.task.rest.model.CommentEntity;
import org.exoplatform.task.service.*;

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

  @Mock
  SpaceService   spaceService;

  @Mock
  CommentService commentService;

  @Mock
  LabelService   labelService;



  @Before
  public void setup() {
    RuntimeDelegate.setInstance(new RuntimeDelegateImpl());
  }

  @Test
  public void testGetTasks() throws Exception {
    // Given
    TaskRestService taskRestService = new TaskRestService(taskService,
            commentService,
            projectService,
            statusService,
            userService,
            spaceService,
            labelService);
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
    when(taskService.getIncomingTasks("root", 0, 20)).thenReturn(incomingTasks);
    when(taskService.countIncomingTasks("root")).thenReturn(incomingTasks.size());
    when(taskService.countTasks(eq("root"), eq("searchTerm"))).thenReturn(1L);

    // When
    Response response = taskRestService.getTasks("overdue", null, 0, 20, false, false);
    Response response1 = taskRestService.getTasks("incoming", null, 0, 20, false, false);
    Response response2 = taskRestService.getTasks("", null, 0, 20, false, false);
    Response response3 = taskRestService.getTasks("whatever", "searchTerm", 0, 20, true, true);

    // Then
    assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
    PaginatedTaskList tasks = (PaginatedTaskList) response.getEntity();
    assertNotNull(tasks);
    assertEquals(2, tasks.getTasksNumber());

    assertEquals(Response.Status.OK.getStatusCode(), response1.getStatus());
    PaginatedTaskList tasks1 = (PaginatedTaskList) response1.getEntity();
    assertNotNull(tasks1);
    assertEquals(2, tasks1.getTasksNumber());

    assertEquals(Response.Status.OK.getStatusCode(), response2.getStatus());
    PaginatedTaskList tasks2 = (PaginatedTaskList) response2.getEntity();
    assertNotNull(tasks2);
    assertEquals(3, tasks2.getTasksNumber());

    assertEquals(Response.Status.OK.getStatusCode(), response3.getStatus());
    PaginatedTaskList tasks3 = (PaginatedTaskList) response3.getEntity();
    assertNotNull(tasks3);
    assertNotNull(tasks3.getTasks());
    assertNotNull(tasks3.getTasksNumber());
    assertEquals(1,tasks3.getTasksNumber());
  }

  @Test
  public void testGetTasksByProjectId() throws Exception {
    // Given
    TaskRestService taskRestService = new TaskRestService(taskService,
            commentService,
            projectService,
            statusService,
            userService,
            spaceService,
            labelService);
    Identity root = new Identity("root");
    ConversationState.setCurrent(new ConversationState(root));
    TaskDto task1 = new TaskDto();
    TaskDto task2 = new TaskDto();
    TaskDto task3 = new TaskDto();
    TaskDto task4 = new TaskDto();
    List<TaskDto>tasks = new ArrayList<TaskDto>();
    tasks.add(task1);
    tasks.add(task2);
    tasks.add(task3);
    tasks.add(task4);
    TaskQuery taskQuery = new TaskQuery();
    List<Long> allProjectIds = new ArrayList<Long>();
    allProjectIds.add((long) 1);
    ProjectDto project1 = new ProjectDto();
    project1.setName("project1");
    when(projectService.getProject(project1.getId())).thenReturn(project1);
    // When
    Response response = taskRestService.getTasksByProjectId(project1.getId(),0,-1,false,false, false);
    // Then
    assertEquals(Response.Status.UNAUTHORIZED.getStatusCode(), response.getStatus());

    Set<String> manager = new HashSet<String>();
    manager.add("root");
    project1.setManager(manager);

    // When
    response = taskRestService.getTasksByProjectId(project1.getId(),0,-1,false,false, false);
    // Then
    assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
  }
  @Test
  public void testGetTaskById() throws Exception {
    // Given
    TaskRestService taskRestService = new TaskRestService(taskService,
            commentService,
            projectService,
            statusService,
            userService,
            spaceService,
            labelService);
    Identity root = new Identity("root");
    ConversationState.setCurrent(new ConversationState(root));
    TaskDto task1 = new TaskDto();
    task1.setAssignee("root");
    taskService.createTask(task1);
    when(taskService.getTask(1)).thenReturn(task1);
    // When
    Response response = taskRestService.getTaskById((long)1);
    // Then
    assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
  }

  @Test
  public void testAddTask() throws Exception {
    // Given
    TaskRestService taskRestService = new TaskRestService(taskService,
            commentService,
            projectService,
            statusService,
            userService,
            spaceService,
            labelService);
    Identity john = new Identity("john");
    ConversationState.setCurrent(new ConversationState(john));
    TaskDto task1 = new TaskDto();
    task1.setTitle("task");
    task1.setCreatedBy("john");
    task1.setAssignee("john");

    // When
    Response response = taskRestService.addTask(task1);

    // Then
    assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());

    ProjectDto project = new ProjectDto();
    project.setId(1);
    StatusDto status = new StatusDto();
    status.setProject(project);
    task1.setStatus(status);

    when(projectService.getProject(1L)).thenReturn(project);
    when(statusService.getDefaultStatus(1L)).thenReturn(status);

    // When
    response = taskRestService.addTask(task1);
    // Then
    assertEquals(Response.Status.UNAUTHORIZED.getStatusCode(), response.getStatus());

    Set<String> participators = new HashSet<String>();
    participators.add("john");
    project.setParticipator(participators);

    // When
    response = taskRestService.addTask(task1);
    // Then
    assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
  }

  @Test
  public void testUpdateTaskById() throws Exception {
    // Given
    TaskRestService taskRestService = new TaskRestService(taskService,
            commentService,
            projectService,
            statusService,
            userService,
            spaceService,
            labelService);
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

    // When
    Response response = taskRestService.updateTaskById(1, null);

    // Then
    assertEquals(Response.Status.BAD_REQUEST.getStatusCode(), response.getStatus());

    // When
    Response response1 = taskRestService.updateTaskById(3, task2);

    // Then
    assertEquals(Response.Status.NOT_FOUND.getStatusCode(), response1.getStatus());

    when(taskService.getTask(1)).thenReturn(task1);
    when(taskService.updateTask(task2)).thenReturn(task2);

    // When
    Response response2 = taskRestService.updateTaskById(1, task2);

    // Then
    assertEquals(Response.Status.OK.getStatusCode(), response2.getStatus());
    TaskDto task = (TaskDto) response2.getEntity();
    assertNotNull(task);
    assertEquals("updatedTask", task.getTitle());
  }


  @Test
  public void deleteTaskById() throws Exception {
      // Given
      TaskRestService taskRestService = new TaskRestService(taskService,
              commentService,
              projectService,
              statusService,
              userService,
              spaceService,
              labelService);
      Identity john = new Identity("john");
      ConversationState.setCurrent(new ConversationState(john));
      TaskDto task = new TaskDto();
      task.setId(1);
      task.setCreatedBy("john");
      task.setAssignee("john");
      taskService.createTask(task);
      when(taskService.getTask(1)).thenReturn(task);
      Response response = taskRestService.deleteTaskById( 1);
      assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
    }


    @Test
  public void testGetLabels() throws Exception {
    // Given
    TaskRestService taskRestService = new TaskRestService(taskService,
            commentService,
            projectService,
            statusService,
            userService,
            spaceService,
            labelService);
    Identity root = new Identity("root");
    ConversationState.setCurrent(new ConversationState(root));

    ProjectDto project = new ProjectDto();
    project.setName("project1");

    StatusDto status = new StatusDto();
    status.setName("status1");
    status.setRank(1);
    status.setProject(project);

    TaskDto task = new TaskDto();
    task.setId(1);
    task.setStatus(status);

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

    when(labelService.findLabelsByUser(root.getUserId(), 0, -1)).thenReturn(labels);
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
    TaskRestService taskRestService = new TaskRestService(taskService,
            commentService,
            projectService,
            statusService,
            userService,
            spaceService,
            labelService);
    Identity root = new Identity("root");
    ConversationState.setCurrent(new ConversationState(root));


    ProjectDto project = new ProjectDto();
    project.setName("project1");

    StatusDto status = new StatusDto();
    status.setName("status1");
    status.setRank(1);
    status.setProject(project);

    TaskDto task = new TaskDto();
    task.setId(1);
    task.setStatus(status);

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

    when(taskService.getTask(1)).thenReturn(task);
    when(labelService.findLabelsByTask(task, 0, root, 0,-1)).thenReturn(labels);
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
    TaskRestService taskRestService = new TaskRestService(taskService,
            commentService,
            projectService,
            statusService,
            userService,
            spaceService,
            labelService);
    Identity root = new Identity("root");
    ConversationState.setCurrent(new ConversationState(root));

    ProjectDto project = new ProjectDto();
    project.setId(1);
    Set<String> manager = new HashSet<String>();
    manager.add("root");
    project.setManager(manager);
    StatusDto status = new StatusDto();
    status.setId(Long.valueOf(1));
    status.setName("status 1");
    status.setProject(project);

    TaskDto task = new TaskDto();
    task.setId(1);
    task.setCreatedBy("root");
    task.setAssignee("root");
    task.setStatus(status);
    taskService.createTask(task);

    LabelDto label1 = new LabelDto();
    label1.setId(1);
    label1.setName("label1");
    label1.setProject(project);

    LabelDto label2 = new LabelDto();
    label2.setId(2);
    label2.setName("label1");
    label2.setProject(project);

    LabelDto label3 = new LabelDto();
    label3.setId(0);
    label3.setProject(project);

    when(projectService.getProject(project.getId())).thenReturn(project);
    when(taskService.getTask(1)).thenReturn(task);

    // When
    Response response = taskRestService.addTaskToLabel(label1, 1);

    // Then
    assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
    LabelDto addedLabel = (LabelDto) response.getEntity();
    assertNotNull(addedLabel);


    // When
    Response response1 = taskRestService.addTaskToLabel(null, 1);

    // Then
    assertEquals(Response.Status.BAD_REQUEST.getStatusCode(), response1.getStatus());


    // When
    Response response2 = taskRestService.addTaskToLabel(label1, 3);

    // Then
    assertEquals(Response.Status.NOT_FOUND.getStatusCode(), response2.getStatus());

    // When
    when(labelService.createLabel(label3)).thenReturn(label1);
    Response response3 = taskRestService.addTaskToLabel(label3, 1);

    // Then
    assertEquals(Response.Status.OK.getStatusCode(), response3.getStatus());

  }

  @Test
  public void testGetTaskLogs() throws Exception {
    // Given
    TaskRestService taskRestService = new TaskRestService(taskService,
            commentService,
            projectService,
            statusService,
            userService,
            spaceService,
            labelService);
    Identity root = new Identity("root");
    ConversationState.setCurrent(new ConversationState(root));

    ProjectDto project = new ProjectDto();
    project.setId(1);
    StatusDto status = new StatusDto();
    status.setId(Long.valueOf(1));
    status.setName("status 1");
    status.setProject(project);

    TaskDto task = new TaskDto();
    task.setId(1);
    task.setCreatedBy("root");
    task.setAssignee("root");
    taskService.createTask(task);
   //When
    Response response = taskRestService.getTaskLogs(task.getId(), 0,1);

    // Then
    assertEquals(Response.Status.NOT_FOUND.getStatusCode(), response.getStatus());

    //When
    when(taskService.getTask(1)).thenReturn(task);

    Response response1 = taskRestService.getTaskLogs(task.getId(), 0,1);

    // Then
    assertEquals(Response.Status.OK.getStatusCode(), response1.getStatus());
   }

  @Test
  public void testRemoveTaskFromLabel() throws Exception {
    // Given
    TaskRestService taskRestService = new TaskRestService(taskService,
            commentService,
            projectService,
            statusService,
            userService,
            spaceService,
            labelService);
    Identity root = new Identity("root");
    ConversationState.setCurrent(new ConversationState(root));
    ProjectDto project = new ProjectDto();
    project.setId(1);
    Set<String> manager = new HashSet<String>();
    manager.add("root");
    project.setManager(manager);
    StatusDto status = new StatusDto();
    status.setId(Long.valueOf(1));
    status.setName("status 1");
    status.setProject(project);

    TaskDto task = new TaskDto();
    task.setId(1);
    task.setCreatedBy("root");
    task.setAssignee("root");
    task.setStatus(status);
    taskService.createTask(task);

    LabelDto label1 = new LabelDto();
    label1.setId(1);
    label1.setName("label1");
    label1.setProject(project);

    LabelDto label2 = new LabelDto();
    label2.setId(2);
    label2.setName("label2");
    label2.setProject(project);


    when(projectService.getProject(project.getId())).thenReturn(project);
    when(taskService.getTask(1)).thenReturn(task);

    // When
    Response response = taskRestService.addTaskToLabel(label1, 1);

    // Then
    assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
    LabelDto addedLabel = (LabelDto) response.getEntity();
    assertNotNull(addedLabel);

    Response response1 = taskRestService.removeTaskFromLabel(1,1);
    assertEquals(Response.Status.NOT_FOUND.getStatusCode(), response1.getStatus());

    when(labelService.getLabel(1)).thenReturn(label1);
    when(taskService.getTask(1)).thenReturn(task);

    Response response2 = taskRestService.removeTaskFromLabel(1,1);
    assertEquals(Response.Status.OK.getStatusCode(), response2.getStatus());
    TaskDto addedLabel1 = (TaskDto) response1.getEntity();
    assertNull(addedLabel1);
  }

  @Test
  public void testAddLabel() throws Exception {
    // Given
    TaskRestService taskRestService = new TaskRestService(taskService,
            commentService,
            projectService,
            statusService,
            userService,
            spaceService,
            labelService);
    Identity root = new Identity("root");
    ConversationState.setCurrent(new ConversationState(root));
    ProjectDto project = new ProjectDto();
    project.setId(1);
    Set<String> manager = new HashSet<String>();
    manager.add("root");
    project.setManager(manager);

    LabelDto label1 = new LabelDto();
    label1.setId(1);
    label1.setName("label1");
    label1.setProject(project);

    when(projectService.getProject(project.getId())).thenReturn(project);

    // When
    Response response = taskRestService.addLabel(label1);

    // Then
    assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
    LabelDto addedLabel = (LabelDto) response.getEntity();
    assertNotNull(addedLabel);
  }

  @Test
  public void testRemoveLabel() throws Exception {
    // Given
    TaskRestService taskRestService = new TaskRestService(taskService,
            commentService,
            projectService,
            statusService,
            userService,
            spaceService,
            labelService);
    Identity root = new Identity("root");
    ConversationState.setCurrent(new ConversationState(root));
    ProjectDto project = new ProjectDto();
    project.setId(1);
    Set<String> manager = new HashSet<String>();
    manager.add("root");
    project.setManager(manager);

    LabelDto label1 = new LabelDto();
    label1.setId(1);
    label1.setName("label1");
    label1.setProject(project);

    when(projectService.getProject(project.getId())).thenReturn(project);
    when(labelService.getLabel(1)).thenReturn(label1);

    // When
    Response response = taskRestService.addLabel(label1);

    // Then
    assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
    LabelDto addedLabel = (LabelDto) response.getEntity();
    assertNotNull(addedLabel);
    Response response2 = taskRestService.removeLabel(1);
    assertEquals(Response.Status.OK.getStatusCode(), response2.getStatus());

  }

  @Test
  public void testAddTaskComment() throws Exception {
    // Given
    TaskRestService taskRestService = new TaskRestService(taskService,
            commentService,
            projectService,
            statusService,
            userService,
            spaceService,
            labelService);
    Identity john = new Identity("john");
    ConversationState.setCurrent(new ConversationState(john));

    TaskDto task = new TaskDto();
    task.setId(1);
    task.setCreatedBy("john");
    task.setAssignee("john");
    taskService.createTask(task);

    CommentDto comment = new CommentDto();
    comment.setId(1);
    List<CommentDto> comments = new ArrayList<CommentDto>();
    comments.add(comment);
    // Sending non empty comment
    when(commentService.addComment(task, john.getUserId(), "commentText")).thenReturn(comment);
    when(taskService.getTask(1)).thenReturn(task);
    Response response = taskRestService.addTaskComment("commentText", 1);
    assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
    CommentEntity commentModel = (CommentEntity) response.getEntity();
    assertNotNull(commentModel);
    assertEquals(commentModel.getFormattedComment(), CommentUtil.formatMention("commentText", Locale.ENGLISH.getLanguage(), userService));

    // Sending an empty comment
    response = taskRestService.addTaskComment("", 1);
    assertEquals(Response.Status.BAD_REQUEST.getStatusCode(), response.getStatus());

    // Sending an encoded comment
    response = taskRestService.addTaskComment("x%20%3C%3D%202", 1);
    assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
    commentModel = (CommentEntity) response.getEntity();
    assertNotNull(commentModel);
    assertEquals(commentModel.getFormattedComment(), CommentUtil.formatMention("x <= 2", Locale.ENGLISH.getLanguage(), userService));

    response = taskRestService.getTaskComments(3, 0, 1);
    assertEquals(Response.Status.NOT_FOUND.getStatusCode(), response.getStatus());

    response = taskRestService.getTaskComments(1, 0, 1);
    assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
  }

  @Test
  public void testfindUsersToMention() throws Exception {
    // Given
    TaskRestService taskRestService = new TaskRestService(taskService,
            commentService,
            projectService,
            statusService,
            userService,
            spaceService,
            labelService);

    Identity root = new Identity("root");
    ConversationState.setCurrent(new ConversationState(root));

    ProjectDto project1 = new ProjectDto();
    project1.setName("project1");


    final User user = TestUtils.getUser();
    ListAccess<User> lists = new ListAccess<User>() {
      @Override
      public User[] load(int i, int i1) throws Exception, IllegalArgumentException {
        return new User[]{user};
      }

      @Override
      public int getSize() throws Exception {
        return 1;
      }
    };

    when(userService.findUserByName("root")).thenReturn(lists);
    when(taskService.isExternal("root")).thenReturn(false);
    Response response = taskRestService.findUsersToMention("root");
    assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
  }

  @Test
  public void testDeleteComment() throws Exception {
    // Given
    TaskRestService taskRestService = new TaskRestService(taskService,
            commentService,
            projectService,
            statusService,
            userService,
            spaceService,
            labelService);
    Identity john = new Identity("john");
    ConversationState.setCurrent(new ConversationState(john));

    TaskDto task = new TaskDto();
    task.setId(1);
    task.setCreatedBy("john");
    task.setAssignee("john");
    taskService.createTask(task);

    CommentDto comment = new CommentDto();
    comment.setId(1);
    comment.setAuthor(john.getUserId());

    // Sending non empty comment
    when(commentService.addComment(task, john.getUserId(), "commentText")).thenReturn(comment);
    when(taskService.getTask(1)).thenReturn(task);
    Response response = taskRestService.addTaskComment("commentText", 1);
    assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
    CommentEntity commentModel = (CommentEntity) response.getEntity();
    assertNotNull(commentModel);
    assertEquals(commentModel.getFormattedComment(), CommentUtil.formatMention("commentText", Locale.ENGLISH.getLanguage(), userService));

    // Sending an encoded comment
    response = taskRestService.addTaskComment("x%20%3C%3D%202", 1);
    assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
    commentModel = (CommentEntity) response.getEntity();
    assertNotNull(commentModel);
    assertEquals(commentModel.getFormattedComment(), CommentUtil.formatMention("x <= 2", Locale.ENGLISH.getLanguage(), userService));

    when(commentService.getComment(1)).thenReturn(null);
    response = taskRestService.deleteComment( 1);
    assertEquals(Response.Status.NOT_FOUND.getStatusCode(), response.getStatus());

    comment.setAuthor(john.getUserId());
    when(commentService.getComment(1)).thenReturn(comment);
    response = taskRestService.deleteComment( 1);
    assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());

    when(commentService.getComment(1)).thenReturn(comment);
    ConversationState.setCurrent(new ConversationState(null));
    response = taskRestService.deleteComment( 1);
    assertEquals(Response.Status.FORBIDDEN.getStatusCode(), response.getStatus());

  }

  @Test
  public void testAddTaskSubComment() throws Exception {
    // Given
    TaskRestService taskRestService = new TaskRestService(taskService,
            commentService,
            projectService,
            statusService,
            userService,
            spaceService,
            labelService);
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
    assertEquals(commentModel.getFormattedComment(), CommentUtil.formatMention("commentText", Locale.ENGLISH.getLanguage(), userService));

    // Sending an empty subcomment
    response = taskRestService.addTaskSubComment("", 1, 1);
    assertEquals(Response.Status.BAD_REQUEST.getStatusCode(), response.getStatus());

    // Sending an encoded subcomment
    response = taskRestService.addTaskSubComment("x%20%3C%3D%202", 1, 1);
    assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
    commentModel = (CommentEntity) response.getEntity();
    assertNotNull(commentModel);
    assertEquals(commentModel.getFormattedComment(), CommentUtil.formatMention("x <= 2",Locale.ENGLISH.getLanguage(), userService));

  }

  @Test
  public void testFilterTasks() throws Exception {
    // Given
    TaskRestService taskRestService = new TaskRestService(taskService,
            commentService,
            projectService,
            statusService,
            userService,
            spaceService,
            labelService);
    Identity root = new Identity("root");
    ConversationState.setCurrent(new ConversationState(root));
    TaskDto task1 = new TaskDto();
    TaskDto task2 = new TaskDto();
    TaskDto task3 = new TaskDto();
    task1.setAssignee("root");
    task2.setTitle("exo");
    task3.setPriority(Priority.NORMAL);
    String Id="due@null";
    ViewState viewState=new ViewState(Id);
    viewState.setGroupBy(null);
    viewState.setOrderBy(null);

    ViewState.Filter filter=new ViewState.Filter(Id);
    filter.setAssignees(null);
    filter.setDue(null);
    filter.setKeyword("exo");
    List<Long> labelIDs=new ArrayList<>();
    List<String> assignee=new ArrayList<>();
    List<String> coworkers=new ArrayList<>();
    List<String> watchers=new ArrayList<>();


    TasksList tasksList = new TasksList(Collections.singletonList(task2),1);
    when(taskService.filterTasks("exo",-2,"exo",labelIDs,null,null,assignee,coworkers, watchers,null,null,root,null,null,null,false,true,false,false,null,null,0,0)).thenReturn(tasksList);
    // When
    Response response = taskRestService.filterTasks(null, -2, "exo",null, null, null, null, null, false,null,null,null,null,null,null,null,0,0,false,false);

    // Then
    assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
    PaginatedTaskList tasks = (PaginatedTaskList) response.getEntity();
    assertNotNull(tasks.getTasks());
    assertEquals(1, tasks.getTasksNumber());

    when(taskService.filterTasks("exo",-2,"exo",labelIDs,null,null,assignee,coworkers, watchers,null,null,root,null,null,null,false,true,false,false,"priority","project",0,0)).thenReturn(tasksList);

    Response response1 = taskRestService.filterTasks(null, -2, "exo",null, null, null, null, null, false,null,null,"project","priority",null,null,null,0,0,false,false);

    assertEquals(Response.Status.OK.getStatusCode(), response1.getStatus());


    Identity exo = new Identity(null);
    ConversationState.setCurrent(new ConversationState(exo));
    Response response2 = taskRestService.filterTasks(null, -2, "exo",null, null, null, null, null, false,null,null,"project","priority",null,null,null,0,0,false,false);

    // Then
    assertEquals(Response.Status.NOT_FOUND.getStatusCode(), response2.getStatus());


  }

  @Test
  public void testUpdateCompleted() throws Exception {
    // Given
    TaskRestService taskRestService = new TaskRestService(taskService,
            commentService,
            projectService,
            statusService,
            userService,
            spaceService,
            labelService);
    Identity root = new Identity("root");
    ConversationState.setCurrent(new ConversationState(root));
    TaskDto task = new TaskDto();
    task.setId(1);
    task.setCreatedBy("root");
    task.setAssignee("root");

    when(taskService.getTask(1)).thenReturn(task);

    // When
    Response response = taskRestService.updateCompleted(1, true);

    // Then
    assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
  }
}

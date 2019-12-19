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
package org.exoplatform.task.service;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;
import static org.mockito.Matchers.*;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.mock;

import java.util.*;

import org.exoplatform.services.idgenerator.IDGeneratorService;
import org.exoplatform.services.jcr.util.IdGenerator;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.runners.MockitoJUnitRunner;
import org.mockito.stubbing.Answer;

import org.exoplatform.commons.utils.ListAccess;
import org.exoplatform.container.ExoContainer;
import org.exoplatform.container.ExoContainerContext;
import org.exoplatform.container.PortalContainer;
import org.exoplatform.services.listener.ListenerService;
import org.exoplatform.services.security.ConversationState;
import org.exoplatform.services.security.Identity;
import org.exoplatform.task.TestUtils;
import org.exoplatform.task.dao.CommentHandler;
import org.exoplatform.task.dao.DAOHandler;
import org.exoplatform.task.dao.LabelHandler;
import org.exoplatform.task.dao.StatusHandler;
import org.exoplatform.task.dao.TaskHandler;
import org.exoplatform.task.domain.Comment;
import org.exoplatform.task.domain.Task;
import org.exoplatform.task.exception.EntityNotFoundException;
import org.exoplatform.task.model.CommentModel;
import org.exoplatform.task.model.TaskModel;
import org.exoplatform.task.model.User;
import org.exoplatform.task.service.impl.TaskServiceImpl;
import org.exoplatform.task.util.TaskUtil;

@RunWith(MockitoJUnitRunner.class)
public class TaskUtilsTest {

  private TaskService taskService;

  private TaskService originTaskService;

  private ListenerService listenerService;

  private ResourceBundle rb_en;

  @Mock
  private ExoContainer container;
  @Mock
  private TaskHandler taskHandler;
  @Mock
  private CommentHandler commentHandler;
  @Mock
  private StatusHandler statusHandler;
  @Mock
  private LabelHandler labelHandler;
  @Mock
  private DAOHandler daoHandler;
  @Mock
  private UserService userService;
  @Mock
  IdGenerator idGenerator;
  @Mock
  private IDGeneratorService idGeneratorService;

  //ArgumentCaptors are how you can retrieve objects that were passed into a method call
  @Captor
  private ArgumentCaptor<Comment> commentCaptor;

  @Before
  public void setUp() throws Exception {
    // Make sure the container is started to prevent the ExoTransactional annotation to fail
    PortalContainer container = PortalContainer.getInstance();

    listenerService = new ListenerService(new ExoContainerContext(container));

    //Mock DAO handler to return Mocked DAO
    when(daoHandler.getTaskHandler()).thenReturn(taskHandler);
    when(daoHandler.getCommentHandler()).thenReturn(commentHandler);
    when(daoHandler.getStatusHandler()).thenReturn(statusHandler);
    when(daoHandler.getLabelHandler()).thenReturn(labelHandler);

    taskService = new TaskServiceImpl(daoHandler, listenerService);

    originTaskService = container.getComponentInstanceOfType(TaskService.class);
    container.unregisterComponent(TaskService.class);
    container.registerComponentInstance(TaskService.class, taskService);

    //Mock some DAO methods
    when(taskHandler.create(any(Task.class))).thenReturn(TestUtils.getDefaultTask());
    when(taskHandler.update(any(Task.class))).thenReturn(TestUtils.getDefaultTask());
    //Mock taskHandler.find(id) to return default task for id = TestUtils.EXISTING_TASK_ID (find(id) return null otherwise)
    when(taskHandler.find(TestUtils.EXISTING_TASK_ID)).thenReturn(TestUtils.getDefaultTask());
    when(taskHandler.getCoworker(anyLong())).thenReturn(Collections.emptySet());
    when(labelHandler.findLabelsByTask(anyLong(), any())).thenReturn(null);
    when(statusHandler.find(TestUtils.EXISTING_STATUS_ID)).thenReturn(TestUtils.getDefaultStatus());
    when(commentHandler.find(TestUtils.EXISTING_COMMENT_ID)).thenReturn(TestUtils.getDefaultComment());
    when(commentHandler.findMentionedUsersOfTask(anyLong())).thenReturn(Collections.emptySet());
    IdGenerator idGenerator = new IdGenerator(idGeneratorService);
    when(idGeneratorService.generateStringID(any(String.class))).thenReturn(Long.toString(System.currentTimeMillis()));
            when(userService.loadUser(any())).thenAnswer(new Answer<User>() {
              @Override
              public User answer(InvocationOnMock invocation) throws Throwable {
                return new User(invocation.getArguments()[0].toString(), null, null, null, null, null, null);
              }
            });

    Identity root = new Identity("root");
    ConversationState.setCurrent(new ConversationState(root));
    rb_en = ResourceBundle.getBundle("locale.portlet.taskManagement", Locale.ENGLISH);
  }

  @After
  public void tearDown() {
    PortalContainer container = PortalContainer.getInstance();
    container.unregisterComponent(TaskService.class);
    container.registerComponentInstance(TaskService.class, originTaskService);
    taskService = null;
    ConversationState.setCurrent(null);
  }

  @Test
  public void testGetTaskModel() throws EntityNotFoundException {
    String username = "Tib";
    String comment = "Bla bla bla bla bla";
    String authorSubComment = "Tib2";
    String subCommentContent = "Bla bla bla bla bla sub comment";

    taskService.addComment(TestUtils.EXISTING_TASK_ID, username, comment);
    verify(commentHandler, times(1)).create(commentCaptor.capture());

    Comment parentComment = commentCaptor.getValue();
    assertEquals(TestUtils.EXISTING_TASK_ID, parentComment.getTask().getId());
    assertEquals(username, parentComment.getAuthor());
    assertEquals(comment, parentComment.getComment());

    long parentCommentId = parentComment.getId();
    taskService.addComment(TestUtils.EXISTING_TASK_ID, parentCommentId ,authorSubComment, subCommentContent);
    verify(commentHandler, times(2)).create(commentCaptor.capture());

    Comment subComment = commentCaptor.getValue();
    assertEquals(TestUtils.EXISTING_TASK_ID, subComment.getTask().getId());
    assertEquals(authorSubComment, subComment.getAuthor());
    assertEquals(subCommentContent, subComment.getComment());

    subComment.setParentComment(parentComment);

    @SuppressWarnings("unchecked")
    ListAccess<Comment> loadedComments = mock(ListAccess.class);
    try {
      when(loadedComments.getSize()).thenReturn(1);
      when(loadedComments.load(anyInt(), anyInt())).thenReturn(new Comment[]{parentComment});
      when(commentHandler.findComments(TestUtils.EXISTING_TASK_ID)).thenReturn(loadedComments);
      when(commentHandler.getSubComments(any())).thenReturn(Collections.singletonList(subComment));
    } catch (Exception e) {
      fail(e.getMessage());
    }

    TaskModel taskModel = TaskUtil.getTaskModel(TestUtils.EXISTING_TASK_ID, true, rb_en, username, taskService, null, userService, null);
    assertNotNull(taskModel); 
    List<CommentModel> comments = taskModel.getComments();
    assertNotNull(comments);
    assertEquals(1, comments.size());
    CommentModel parentCommentModel = comments.get(0);
    assertNotNull(parentCommentModel);
    assertEquals(parentComment.getId(), parentCommentModel.getId());
    assertEquals(username, parentCommentModel.getAuthor().getUsername());
    assertEquals(comment, parentCommentModel.getComment());
    List<CommentModel> subCommentModels = parentCommentModel.getSubComments();
    assertNotNull(subCommentModels);
    assertEquals(1, subCommentModels.size());
    CommentModel subCommentModel = subCommentModels.get(0);
    assertEquals(subComment.getId(), subCommentModel.getId());
    assertEquals(authorSubComment, subCommentModel.getAuthor().getUsername());
    assertEquals(subCommentContent, subCommentModel.getComment());
  }

}
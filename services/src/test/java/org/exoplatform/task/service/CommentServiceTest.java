/* * Copyright (C) 2003-2015 eXo Platform SAS.
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

import org.exoplatform.container.ExoContainer;
import org.exoplatform.container.ExoContainerContext;
import org.exoplatform.container.PortalContainer;
import org.exoplatform.services.listener.ListenerService;
import org.exoplatform.task.TestUtils;
import org.exoplatform.task.dao.*;
import org.exoplatform.task.domain.Comment;
import org.exoplatform.task.dto.CommentDto;
import org.exoplatform.task.dto.UserSettingDto;
import org.exoplatform.task.exception.EntityNotFoundException;
import org.exoplatform.task.legacy.service.UserService;
import org.exoplatform.task.model.User;
import org.exoplatform.task.service.impl.CommentServiceImpl;
import org.exoplatform.task.service.impl.StatusServiceImpl;
import org.exoplatform.task.storage.CommentStorage;
import org.exoplatform.task.storage.ProjectStorage;
import org.exoplatform.task.storage.StatusStorage;
import org.exoplatform.task.storage.TaskStorage;
import org.exoplatform.task.storage.impl.CommentStorageImpl;
import org.exoplatform.task.storage.impl.TaskStorageImpl;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.Date;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class CommentServiceTest {

  StatusService           statusService;

  CommentService          commentService;

  StatusStorage           statusStorage;

  ProjectStorage projectStorage;

  CommentStorage          commentStorage;

  TaskStorage             taskStorage;

  ListenerService         listenerService;

  @Mock
  ExoContainer container;

  @Mock
  TaskHandler             taskHandler;

  @Mock
  StatusHandler           statusHandler;

  @Mock
  LabelHandler            labelHandler;

  @Mock
  UserService             userService;

  @Mock
  CommentHandler          commentHandler;

  @Mock
  DAOHandler              daoHandler;

  @Captor
  ArgumentCaptor<Comment> commentCaptor;

  @Before
  public void setUp() {
    // Make sure the container is started to prevent the ExoTransactional annotation
    // to fail
    PortalContainer.getInstance();
    taskStorage = new TaskStorageImpl(daoHandler,userService);
    commentStorage = new CommentStorageImpl(daoHandler);
    statusService = new StatusServiceImpl(daoHandler, statusStorage, projectStorage, listenerService);
    commentService = new CommentServiceImpl(taskStorage, commentStorage, daoHandler, listenerService);
    // Mock DAO handler to return Mocked DAO
    when(daoHandler.getTaskHandler()).thenReturn(taskHandler);
    when(daoHandler.getStatusHandler()).thenReturn(statusHandler);
    when(daoHandler.getLabelHandler()).thenReturn(labelHandler);
    when(daoHandler.getCommentHandler()).thenReturn(commentHandler);

    // Mock some DAO methods
    when(taskHandler.find(TestUtils.EXISTING_TASK_ID)).thenReturn(TestUtils.getDefaultTask());
    when(statusHandler.find(TestUtils.EXISTING_STATUS_ID)).thenReturn(TestUtils.getDefaultStatus());
    when(commentHandler.find(TestUtils.EXISTING_COMMENT_ID)).thenReturn(TestUtils.getDefaultComment());
  }

  @After
  public void tearDown() {
    commentService = null;
  }

  @Test public void testAddComment() throws EntityNotFoundException { Comment
          comment = TestUtils.getDefaultComment();
    when(daoHandler.getCommentHandler().create(any())).thenReturn(comment);
    commentService.addComment(taskStorage.toDto(comment.getTask()),comment.
            getAuthor(),comment.getComment());
    verify(commentHandler, times(1)).create(commentCaptor.capture());
    Comment result = commentCaptor.getValue();
    assertEquals("Bla bla", result.getComment());
  }

  @Test
  public void testCommentWithMention() throws EntityNotFoundException {
    Comment comment = TestUtils.getDefaultCommentWithMention();
    when(daoHandler.getCommentHandler().create(any())).thenReturn(comment);
    commentService.addComment(StorageUtil.taskToDto(comment.getTask(), projectStorage), comment.
            getAuthor(), comment.getComment());
    verify(commentHandler, times(1)).create(commentCaptor.capture());
    Comment result = commentCaptor.getValue();
    assertEquals(true, result.getMentionedUsers().contains("testa"));
  }

  @Test
  public void testRemoveComment() throws EntityNotFoundException {
    Comment comment = TestUtils.getDefaultComment();

    when(daoHandler.getCommentHandler().find(TestUtils.EXISTING_COMMENT_ID)).thenReturn(comment);
    commentService.removeComment(TestUtils.EXISTING_COMMENT_ID);
    verify(commentHandler, times(1)).delete(commentCaptor.capture());

    assertEquals(TestUtils.EXISTING_COMMENT_ID, commentCaptor.getValue().getId());

  }

  @Test
  public void testGetComment() {
    Comment comment = TestUtils.getDefaultComment();
    commentService.getComment(TestUtils.EXISTING_COMMENT_ID);

  }

  @Test public void testAddCommentsByTask() throws EntityNotFoundException {
    String username = "Tib"; String comment = "Bla bla bla bla bla";
    CommentDto newComment = new CommentDto();
    newComment.setTask(TestUtils.getDefaultTask());
    newComment.setAuthor(username);
    newComment.setComment(comment);
    newComment.setCreatedTime(new Date());
    when(daoHandler.getCommentHandler().create(any())).thenReturn(commentStorage.commentToEntity(newComment));
    commentService.addComment(taskStorage.toDto(TestUtils.getDefaultTask()), username, comment);
    commentService.getComments(TestUtils.EXISTING_TASK_ID,0,1);
    commentService.countComments(TestUtils.EXISTING_TASK_ID);
    verify(commentHandler, times(1)).create(commentCaptor.capture());
    assertEquals(TestUtils.EXISTING_TASK_ID, commentCaptor.getValue().getTask().getId());
    assertEquals(username, commentCaptor.getValue().getAuthor());
    assertEquals(comment, commentCaptor.getValue().getComment());

  }

  @Test public void testAddSubComments() throws EntityNotFoundException {
    String username = "Tib"; String comment = "Bla bla bla bla bla";
    String authorSubComment = "Tib2";
    String subCommentContent = "Bla bla bla bla bla sub comment";
    CommentDto newComment = new CommentDto();
    newComment.setTask(TestUtils.getDefaultTask());
    newComment.setAuthor(username);
    newComment.setComment(comment);
    newComment.setCreatedTime(new Date());
    when(daoHandler.getCommentHandler().create(any())).thenReturn(commentStorage. commentToEntity(newComment));
    when(userService.loadUser(eq(username))).thenReturn(new User(username, null, null, null, null, null, null));
    when(userService.loadUser(eq(authorSubComment))).thenReturn(new User(authorSubComment, null, null, null, null, null, null));
    UserSettingDto userSettingDto= TestUtils.getDefaultUserSettingDto();
    assertEquals ("user",userSettingDto.getUsername());
    assertEquals (true,userSettingDto.isShowHiddenLabel());
    assertEquals (true,userSettingDto.isShowHiddenProject());
    commentService.addComment(taskStorage.toDto(TestUtils.getDefaultTask()), username, comment);
    verify(commentHandler, times(1)).create(commentCaptor.capture());
    Comment parentComment = commentCaptor.getValue();
    assertEquals(TestUtils.EXISTING_TASK_ID, parentComment.getTask().getId());
    assertEquals(username, parentComment.getAuthor());
    assertEquals(comment, parentComment.getComment());
    long parentCommentId = parentComment.getId();
    commentService.addComment(taskStorage.toDto(TestUtils.getDefaultTask()), parentCommentId ,authorSubComment, subCommentContent);
    verify(commentHandler, times(2)).create(commentCaptor.capture());
    Comment subComment = commentCaptor.getValue(); assertEquals(TestUtils.EXISTING_TASK_ID, subComment.getTask().getId());
    assertEquals(authorSubComment, subComment.getAuthor());
    assertEquals(subCommentContent, subComment.getComment());
  }

}

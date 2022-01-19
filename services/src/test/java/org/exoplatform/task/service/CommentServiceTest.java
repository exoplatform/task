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

import org.exoplatform.container.PortalContainer;
import org.exoplatform.services.listener.ListenerService;
import org.exoplatform.task.TestUtils;
import org.exoplatform.task.dao.*;
import org.exoplatform.task.domain.Comment;
import org.exoplatform.task.dto.CommentDto;
import org.exoplatform.task.dto.TaskDto;
import org.exoplatform.task.dto.UserSettingDto;
import org.exoplatform.task.exception.EntityNotFoundException;
import org.exoplatform.task.model.User;
import org.exoplatform.task.service.impl.CommentServiceImpl;
import org.exoplatform.task.service.impl.StatusServiceImpl;
import org.exoplatform.task.storage.CommentStorage;
import org.exoplatform.task.storage.ProjectStorage;
import org.exoplatform.task.storage.StatusStorage;
import org.exoplatform.task.storage.TaskStorage;
import org.exoplatform.task.storage.impl.CommentStorageImpl;
import org.exoplatform.task.storage.impl.TaskStorageImpl;
import org.exoplatform.task.util.StorageUtil;
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

  ProjectStorage          projectStorage;

  @Mock
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

  @Mock
  StatusStorage           statusStorage;

  @Captor
  ArgumentCaptor<Comment> commentCaptor;

  @Before
  public void setUp() throws EntityNotFoundException {
    // Make sure the container is started to prevent the ExoTransactional annotation
    // to fail
    PortalContainer.getInstance();
    PortalContainer.getInstance();
    taskStorage = new TaskStorageImpl(daoHandler,userService,projectStorage);
    commentStorage = new CommentStorageImpl(daoHandler,projectStorage);
    statusService = new StatusServiceImpl(daoHandler, statusStorage, projectStorage, listenerService);
    commentService = new CommentServiceImpl(taskStorage, commentStorage, daoHandler, listenerService);
    // Mock DAO handler to return Mocked DAO
    when(daoHandler.getCommentHandler()).thenReturn(commentHandler);

    // Mock some DAO methods
    when(commentHandler.find(TestUtils.EXISTING_COMMENT_ID)).thenReturn(TestUtils.getDefaultComment());
  }

  @After
  public void tearDown() {
    commentService = null;
  }

  @Test public void testAddComment() throws EntityNotFoundException { Comment
          comment = TestUtils.getDefaultComment();
    when(daoHandler.getCommentHandler().create(any())).thenReturn(comment);
    commentService.addComment(StorageUtil.taskToDto(comment.getTask(),projectStorage),comment.
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
   CommentDto newComment = TestUtils.getDefaultCommentDto();
    commentService.addComment(TestUtils.getDefaultTaskDto(), newComment.getAuthor(), newComment.getComment());
    commentService.getComments(TestUtils.EXISTING_TASK_ID,0,1);
    commentService.countComments(TestUtils.EXISTING_TASK_ID);
    verify(commentHandler, times(1)).create(commentCaptor.capture());
    assertEquals(TestUtils.EXISTING_TASK_ID, commentCaptor.getValue().getTask().getId());
    assertEquals(username, commentCaptor.getValue().getAuthor());
    assertEquals(newComment.getComment(), commentCaptor.getValue().getComment());

  }

  @Test public void testAddSubComments() throws EntityNotFoundException {
    String username = "Tib"; String comment = "Bla bla bla bla bla";
    String authorSubComment = "Tib2";
    String subCommentContent = "Bla bla bla bla bla sub comment";
    CommentDto newComment = new CommentDto();
    newComment.setTask(TestUtils.getDefaultTaskDto());
    newComment.setAuthor(username);
    newComment.setComment(comment);
    newComment.setCreatedTime(new Date());
    UserSettingDto userSettingDto= TestUtils.getDefaultUserSettingDto();
    assertEquals ("user",userSettingDto.getUsername());
    assertEquals (true,userSettingDto.isShowHiddenLabel());
    assertEquals (true,userSettingDto.isShowHiddenProject());
    commentService.addComment(StorageUtil.taskToDto(TestUtils.getDefaultTask(),projectStorage), username, comment);
    verify(commentHandler, times(1)).create(commentCaptor.capture());
    Comment parentComment = commentCaptor.getValue();
    assertEquals(TestUtils.EXISTING_TASK_ID, parentComment.getTask().getId());
    assertEquals(username, parentComment.getAuthor());
    assertEquals(comment, parentComment.getComment());
    long parentCommentId = parentComment.getId();
    commentService.addComment(StorageUtil.taskToDto(TestUtils.getDefaultTask(),projectStorage), parentCommentId ,authorSubComment, subCommentContent);
    verify(commentHandler, times(2)).create(commentCaptor.capture());
    Comment subComment = commentCaptor.getValue(); assertEquals(TestUtils.EXISTING_TASK_ID, subComment.getTask().getId());
    assertEquals(authorSubComment, subComment.getAuthor());
    assertEquals(subCommentContent, subComment.getComment());
  }

}

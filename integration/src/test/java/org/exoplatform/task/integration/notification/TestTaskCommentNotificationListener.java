/*
 * Copyright (C) 2015 eXo Platform SAS.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */

package org.exoplatform.task.integration.notification;

import org.exoplatform.commons.api.notification.NotificationContext;
import org.exoplatform.commons.api.notification.channel.ChannelManager;
import org.exoplatform.commons.api.notification.model.PluginKey;
import org.exoplatform.commons.api.notification.service.NotificationCompletionService;
import org.exoplatform.commons.api.notification.service.setting.PluginSettingService;
import org.exoplatform.commons.api.notification.service.storage.NotificationService;
import org.exoplatform.commons.api.settings.ExoFeatureService;
import org.exoplatform.commons.notification.impl.setting.NotificationPluginContainer;
import org.exoplatform.commons.persistence.impl.EntityManagerService;
import org.exoplatform.commons.utils.ListAccessImpl;
import org.exoplatform.container.ExoContainerContext;
import org.exoplatform.container.PortalContainer;
import org.exoplatform.container.xml.InitParams;
import org.exoplatform.services.listener.ListenerService;
import org.exoplatform.services.security.ConversationState;
import org.exoplatform.services.security.Identity;
import org.exoplatform.task.dao.CommentHandler;
import org.exoplatform.task.dao.DAOHandler;
import org.exoplatform.task.dao.TaskHandler;
import org.exoplatform.task.domain.Comment;
import org.exoplatform.task.domain.Task;
import org.exoplatform.task.exception.EntityNotFoundException;
import org.exoplatform.task.integration.TaskCommentNotificationListener;
import org.exoplatform.task.integration.notification.mock.MockNotificationCompletionService;
import org.exoplatform.task.service.TaskService;
import org.exoplatform.task.service.impl.TaskServiceImpl;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.runners.MockitoJUnitRunner;
import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.junit.Assert.*;

import java.lang.reflect.Array;
import java.util.Arrays;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

/**
 * @author <a href="mailto:tuyennt@exoplatform.com">Tuyen Nguyen The</a>.
 */
@RunWith(MockitoJUnitRunner.class)
public class TestTaskCommentNotificationListener {

  @Mock
  private PortalContainer container;

  // Some service in social need to be mock
  @Mock
  private NotificationPluginContainer notificationPluginContainer;
  @Mock
  private PluginSettingService pluginSettingService;
  @Mock
  private ChannelManager channelManager;
  @Mock
  private NotificationService notificationService;
  @Mock
  private ExoFeatureService exoFeatureService;
  @Mock
  private AbstractNotificationPlugin notificationPlugin;


  // Mock EntityManager service
  @Mock
  private EntityManagerService entityManagerService;
  @Mock
  private EntityManager entityManager;

  // Mock task services and handlers
  @Mock
  private DAOHandler daoHandler;
  @Mock
  private TaskHandler taskHandler;
  @Mock
  private CommentHandler commentHandler;

  private ListenerService listenerService;
  private TaskService taskService;

  @Captor
  ArgumentCaptor<NotificationContext> notificationCapture;

  @Before
  public void setUp() throws Exception {
    MockitoAnnotations.initMocks(this);

    // Enable notification feature
    when(exoFeatureService.isActiveFeature(org.exoplatform.commons.notification.NotificationUtils.FEATURE_NAME)).thenReturn(true);

    // Mock notification plugin
    when(notificationPluginContainer.getPlugin(PluginKey.key(TaskCommentPlugin.ID))).thenReturn(notificationPlugin);
    when(notificationPluginContainer.getPlugin(PluginKey.key(TaskMentionPlugin.ID))).thenReturn(notificationPlugin);

    // Mock entity manager session
    when(entityManager.getTransaction()).thenReturn(new EntityTransaction() {
      @Override
      public void begin() {

      }

      @Override
      public void commit() {

      }

      @Override
      public void rollback() {

      }

      @Override
      public void setRollbackOnly() {

      }

      @Override
      public boolean getRollbackOnly() {
        return false;
      }

      @Override
      public boolean isActive() {
        return true;
      }
    });
    when(entityManagerService.getEntityManager()).thenReturn(entityManager);

    when(container.getComponentInstanceOfType(EntityManagerService.class)).thenReturn(entityManagerService);
    when(container.getComponentInstanceOfType(NotificationPluginContainer.class)).thenReturn(notificationPluginContainer);
    when(container.getComponentInstanceOfType(PluginSettingService.class)).thenReturn(pluginSettingService);
    when(container.getComponentInstanceOfType(ChannelManager.class)).thenReturn(channelManager);
    when(container.getComponentInstanceOfType(NotificationService.class)).thenReturn(notificationService);
    when(container.getComponentInstanceOfType(NotificationCompletionService.class)).thenReturn(new MockNotificationCompletionService(new InitParams()));
    when(container.getComponentInstanceOfType(ExoFeatureService.class)).thenReturn(exoFeatureService);

    when(daoHandler.getTaskHandler()).thenReturn(taskHandler);
    when(daoHandler.getCommentHandler()).thenReturn(commentHandler);

    ExoContainerContext.setCurrentContainer(container);

    listenerService = new ListenerService(new ExoContainerContext(container));
    taskService = new TaskServiceImpl(daoHandler, listenerService);
    listenerService.addListener(TaskService.TASK_COMMENT_CREATION, new TaskCommentNotificationListener());
  }

  private void setCurrentUser(String username) {
    ConversationState.setCurrent(new ConversationState(new Identity(username)));
  }

  @Test
  public void testSendNotificationToCreator() throws EntityNotFoundException {
    setCurrentUser("root");
    Task task = makeTask("usera");
    Comment comment = makeComment(task, "root");

    when(taskHandler.find(task.getId())).thenReturn(task);
    when(commentHandler.create(any(Comment.class))).thenReturn(comment);

    taskService.addComment(task.getId(), "root", "This is comment text");

    verify(notificationPlugin, times(2)).isValid(notificationCapture.capture());

    NotificationContext ctx = notificationCapture.getValue();
    assertEquals(task.getId(), ctx.value(NotificationUtils.TASK).getId());

    Set<String> receiver = ctx.value(NotificationUtils.RECEIVERS);
    assertEquals(1, receiver.size());
    assertEquals("usera", receiver.iterator().next());
    assertFalse(receiver.contains("root"));
  }

  @Test
  public void testSendNotificationToNoOne() throws EntityNotFoundException {
    setCurrentUser("usera");
    Task task = makeTask("usera");
    Comment comment = makeComment(task, "usera");

    when(taskHandler.find(task.getId())).thenReturn(task);
    when(taskHandler.getCoworker(task.getId())).thenReturn(new HashSet<String>());

    when(commentHandler.create(any(Comment.class))).thenReturn(comment);

    taskService.addComment(task.getId(), "root", "This is comment text");

    verify(notificationPlugin, times(2)).isValid(notificationCapture.capture());

    NotificationContext ctx = notificationCapture.getValue();
    assertEquals(task.getId(), ctx.value(NotificationUtils.TASK).getId());

    Set<String> receiver = ctx.value(NotificationUtils.RECEIVERS);
    assertEquals(0, receiver.size());
  }

  @Test
  public void testSendNotificationToAssigneeAndCoworker() throws EntityNotFoundException {
    setCurrentUser("root");
    Task task = makeTask("usera");
    task.setAssignee("userb");
    Set<String> coworker = new HashSet<>();
    coworker.add("userc");
    coworker.add("userd");
    coworker.add("root");
    task.setCoworker(coworker);

    Comment comment = makeComment(task, "root");

    when(taskHandler.find(task.getId())).thenReturn(task);
    when(taskHandler.getCoworker(task.getId())).thenReturn(coworker);

    when(commentHandler.create(any(Comment.class))).thenReturn(comment);

    taskService.addComment(task.getId(), "root", any(String.class));

    verify(notificationPlugin, times(2)).isValid(notificationCapture.capture());
    NotificationContext ctx = notificationCapture.getValue();
    assertEquals(task.getId(), ctx.value(NotificationUtils.TASK).getId());

    Set<String> receiver = ctx.value(NotificationUtils.RECEIVERS);
    assertEquals(4, receiver.size());
    assertTrue(receiver.contains("usera"));
    assertTrue(receiver.contains("userb"));
    assertTrue(receiver.contains("userc"));
    assertTrue(receiver.contains("userd"));
    assertFalse(receiver.contains("root"));
  }

  public void testSendNotificationToCommenter() throws EntityNotFoundException {
    setCurrentUser("root");
    Task task = makeTask("usera");
    Comment comment = makeComment(task, "root");

    Comment c1 = makeComment(task, "userb");
    Comment c2 = makeComment(task, "userc");
    Comment c3 = makeComment(task, "root");

    when(taskHandler.find(task.getId())).thenReturn(task);

    when(commentHandler.create(any(Comment.class))).thenReturn(comment);
    when(commentHandler.findComments(task.getId())).thenReturn(new ListAccessImpl<Comment>(Comment.class, Arrays.asList(c1, c2, c3)));

    taskService.addComment(task.getId(), "root", any(String.class));

    verify(notificationPlugin, times(2)).isValid(notificationCapture.capture());
    NotificationContext ctx = notificationCapture.getValue();
    assertEquals(task.getId(), ctx.value(NotificationUtils.TASK).getId());

    Set<String> receiver = ctx.value(NotificationUtils.RECEIVERS);
    assertEquals(3, receiver.size());
    assertTrue(receiver.contains("usera"));
    assertTrue(receiver.contains("userb"));
    assertTrue(receiver.contains("userc"));
    assertFalse(receiver.contains("root"));
  }

  public void testSendNotificationToMentioned() throws EntityNotFoundException {
    setCurrentUser("root");
    Task task = makeTask("usera");
    Comment comment = makeComment(task, "root");
    comment.setComment("This is comment with mention @usera @root @userb @userc");

    when(taskHandler.find(task.getId())).thenReturn(task);
    when(commentHandler.create(any(Comment.class))).thenReturn(comment);

    taskService.addComment(task.getId(), "root", any(String.class));

    verify(notificationPlugin, times(2)).isValid(notificationCapture.capture());

    // Comment notification
    NotificationContext commentCtx = notificationCapture.getAllValues().get(0);
    assertEquals(task.getId(), commentCtx.value(NotificationUtils.TASK).getId());

    Set<String> receiver = commentCtx.value(NotificationUtils.RECEIVERS);
    assertEquals(1, receiver.size());
    assertTrue(receiver.contains("usera"));

    // Mention notification
    notificationCapture.getAllValues().get(1);
    NotificationContext mentionCtx = notificationCapture.getAllValues().get(0);
    assertEquals(task.getId(), mentionCtx.value(NotificationUtils.TASK).getId());

    receiver = mentionCtx.value(NotificationUtils.RECEIVERS);
    assertEquals(3, receiver.size());
    assertTrue(receiver.contains("usera"));
    assertTrue(receiver.contains("userb"));
    assertTrue(receiver.contains("userc"));
    assertFalse(receiver.contains("root"));
  }

  private Comment makeComment(Task task, String author) {
    Comment comment = new Comment();
    comment.setTask(task);
    comment.setId(1);
    comment.setAuthor(author);
    comment.setComment("This is test comment");
    comment.setCreatedTime(new Date());

    return comment;
  }

  private Task makeTask(String creator) {
    Task task = new Task();
    task.setId(1);
    task.setTitle("Test task");
    task.setCreatedBy(creator);

    return task;
  }
}

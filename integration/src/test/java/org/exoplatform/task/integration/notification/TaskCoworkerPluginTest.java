package org.exoplatform.task.integration.notification;

import org.exoplatform.commons.api.notification.NotificationContext;
import org.exoplatform.commons.notification.impl.NotificationContextImpl;
import org.exoplatform.container.xml.InitParams;
import org.exoplatform.task.domain.Task;
import org.junit.Test;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import static org.junit.Assert.*;

/**
 *
 */
public class TaskCoworkerPluginTest {

  @Test
  public void shouldReturnAssigneeInReceivers() throws Exception {
    // Given
    Task task = new Task();
    task.setCoworker(new HashSet<>(Arrays.asList("user1", "user2")));

    NotificationContext ctx = NotificationContextImpl.cloneInstance();
    ctx.append(NotificationUtils.COWORKER, task.getCoworker());

    AbstractNotificationPlugin notificationPlugin = new TaskCoworkerPlugin(new InitParams());

    // When
    Set<String> receivers = notificationPlugin.getReceiver(task, ctx);

    // Then
    assertNotNull(receivers);
    assertEquals(2, receivers.size());
    assertTrue(receivers.contains("user1"));
    assertTrue(receivers.contains("user2"));
  }

  @Test
  public void shouldNotReturnAssigneeInReceiversWhenAssigneeIsCreator() throws Exception {
    // Given
    Task task = new Task();
    task.setCoworker(new HashSet<>(Arrays.asList("user1", "user2")));

    NotificationContext ctx = NotificationContextImpl.cloneInstance();
    ctx.append(NotificationUtils.CREATOR, "user1");
    ctx.append(NotificationUtils.COWORKER, task.getCoworker());

    AbstractNotificationPlugin notificationPlugin = new TaskCoworkerPlugin(new InitParams());

    // When
    Set<String> receivers = notificationPlugin.getReceiver(task, ctx);

    // Then
    assertNotNull(receivers);
    assertEquals(1, receivers.size());
    assertTrue(receivers.contains("user2"));
  }
}
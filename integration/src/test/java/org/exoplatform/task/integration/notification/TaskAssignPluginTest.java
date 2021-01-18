package org.exoplatform.task.integration.notification;

import org.exoplatform.commons.api.notification.NotificationContext;
import org.exoplatform.commons.notification.impl.NotificationContextImpl;
import org.exoplatform.container.xml.InitParams;
import org.exoplatform.task.domain.Task;
import org.exoplatform.task.dto.TaskDto;
import org.junit.Test;

import java.util.Set;

import static org.junit.Assert.*;

/**
 *
 */
public class TaskAssignPluginTest {

  @Test
  public void shouldReturnAssigneeInReceivers() throws Exception {
    // Given
    TaskDto task = new TaskDto();
    task.setAssignee("user1");

    AbstractNotificationPlugin notificationPlugin = new TaskAssignPlugin(new InitParams());

    // When
    Set<String> receivers = notificationPlugin.getReceiver(task, null);

    // Then
    assertNotNull(receivers);
    assertEquals(1, receivers.size());
    assertEquals("user1", receivers.iterator().next());
  }

  @Test
  public void shouldNotReturnAssigneeInReceiversWhenAssigneeIsCreator() throws Exception {
    // Given
    TaskDto task = new TaskDto();
    task.setAssignee("user1");

    NotificationContext ctx = NotificationContextImpl.cloneInstance();
    ctx.append(NotificationUtils.CREATOR, "user1");

    AbstractNotificationPlugin notificationPlugin = new TaskAssignPlugin(new InitParams());

    // When
    Set<String> receivers = notificationPlugin.getReceiver(task, ctx);

    // Then
    assertNotNull(receivers);
    assertEquals(0, receivers.size());
  }
}
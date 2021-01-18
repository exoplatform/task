package org.exoplatform.task.integration.notification;

import org.exoplatform.commons.api.notification.NotificationContext;
import org.exoplatform.commons.notification.impl.NotificationContextImpl;
import org.exoplatform.container.xml.InitParams;
import org.exoplatform.task.domain.Task;
import org.exoplatform.task.dto.TaskDto;
import org.junit.Test;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import static org.junit.Assert.*;

/**
 *
 */
public class AbstractNotificationPluginTest {

  public class DummyNotificationPlugin extends AbstractNotificationPlugin {

    public DummyNotificationPlugin(InitParams initParams) {
      super(initParams);
    }

    @Override
    public String getId() {
      return "DummyNotificationPlugin";
    }

    @Override
    public boolean isValid(NotificationContext notificationContext) {
      return true;
    }
  }

  @Test
  public void shouldReturnAssigneeInReceivers() throws Exception {
    // Given
    TaskDto task = new TaskDto();
    task.setAssignee("user1");

    AbstractNotificationPlugin notificationPlugin = new DummyNotificationPlugin(new InitParams());

    // When
    Set<String> receivers = notificationPlugin.getReceiver(task, null);

    // Then
    assertNotNull(receivers);
    assertEquals(1, receivers.size());
    assertEquals("user1", receivers.iterator().next());
  }

  @Test
  public void shouldReturnCoworkersInReceivers() throws Exception {
    // Given
    TaskDto task = new TaskDto();
    task.setCoworker(new HashSet<>(Arrays.asList("user1", "user2")));

    AbstractNotificationPlugin notificationPlugin = new DummyNotificationPlugin(new InitParams());

    // When
    Set<String> receivers = notificationPlugin.getReceiver(task, null);

    // Then
    assertNotNull(receivers);
    assertEquals(2, receivers.size());
    assertTrue(receivers.contains("user1"));
    assertTrue(receivers.contains("user2"));
  }

  @Test
  public void shouldReturnWatchersInReceivers() throws Exception {
    // Given
    TaskDto task = new TaskDto();
    task.setWatcher(new HashSet<>(Arrays.asList("user1", "user2")));

    AbstractNotificationPlugin notificationPlugin = new DummyNotificationPlugin(new InitParams());

    // When
    Set<String> receivers = notificationPlugin.getReceiver(task, null);

    // Then
    assertNotNull(receivers);
    assertEquals(2, receivers.size());
    assertTrue(receivers.contains("user1"));
    assertTrue(receivers.contains("user2"));
  }

  @Test
  public void shouldReturnCreatorInReceivers() throws Exception {
    // Given
    TaskDto task = new TaskDto();
    task.setAssignee("user1");
    task.setCreatedBy("user2");
    task.setCoworker(new HashSet<>(Arrays.asList("user2", "user3")));

    NotificationContext ctx = NotificationContextImpl.cloneInstance();

    AbstractNotificationPlugin notificationPlugin = new DummyNotificationPlugin(new InitParams());

    // When
    Set<String> receivers = notificationPlugin.getReceiver(task, ctx);

    // Then
    assertNotNull(receivers);
    assertEquals(3, receivers.size());
    assertTrue(receivers.contains("user1"));
    assertTrue(receivers.contains("user2"));
    assertTrue(receivers.contains("user3"));
  }
}

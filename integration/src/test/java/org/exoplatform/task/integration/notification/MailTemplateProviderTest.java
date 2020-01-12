package org.exoplatform.task.integration.notification;

import java.io.Writer;
import java.util.*;

import org.mockito.Mockito;

import org.exoplatform.commons.api.notification.NotificationContext;
import org.exoplatform.commons.api.notification.model.NotificationInfo;
import org.exoplatform.commons.api.notification.model.PluginKey;
import org.exoplatform.commons.notification.impl.NotificationContextImpl;
import org.exoplatform.container.xml.InitParams;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.task.service.UserService;

import junit.framework.TestCase;

public class MailTemplateProviderTest extends TestCase {

  private static final Log LOG = ExoLogger.getLogger(MailTemplateProviderTest.class);
  
  public void testMakeDigest() {
    NotificationInfo notificationInfo1 = createNotification();
    NotificationInfo notificationInfo2 = createNotification();
    NotificationInfo notificationInfo3 = createNotification();

    assertNotNull(notificationInfo1);
    assertNotNull(notificationInfo2);
    assertNotNull(notificationInfo3);

    UserService userService = Mockito.mock(UserService.class);
    InitParams initParams = new InitParams();
    Writer writer = null;
    NotificationContext context = NotificationContextImpl.cloneInstance();
    List<NotificationInfo> list = new ArrayList<>();
    NotificationContext newCtx = NotificationContextImpl.cloneInstance();
    Map<String, String> ownerParameter = new HashMap<>();
    MailTemplateProvider mailTemplateProvider = new MailTemplateProvider(initParams, userService);
    NotificationContext ctx = NotificationContextImpl.cloneInstance();
    notificationInfo1.setTo("root");
    notificationInfo1.setId(TaskCoworkerPlugin.ID);
    notificationInfo1.key(TaskCoworkerPlugin.ID);
    ownerParameter.put(NotificationUtils.CREATOR.getKey(), "user1");
    ownerParameter.put(NotificationUtils.TASK_ASSIGNEE, "user2");
    ownerParameter.put(NotificationUtils.ADDED_COWORKER, "user3");
    notificationInfo1.setOwnerParameter(ownerParameter);
    list.add(notificationInfo1);
    context.setNotificationInfos(list);
    assertFalse(mailTemplateProvider.getTemplateBuilder().get(PluginKey.key(TaskCoworkerPlugin.ID)).buildDigest(context, writer));
    list.remove(notificationInfo1);
    notificationInfo2.setTo("root");
    notificationInfo2.setId(TaskAssignPlugin.ID);
    notificationInfo2.key(TaskAssignPlugin.ID);
    ownerParameter.put(NotificationUtils.CREATOR.getKey(), "user1");
    ownerParameter.put(NotificationUtils.TASK_ASSIGNEE, "user2");
    ownerParameter.put(NotificationUtils.ADDED_COWORKER, "user3");
    notificationInfo2.setOwnerParameter(ownerParameter);
    list.add(notificationInfo2);
    ctx.setNotificationInfos(list);
    assertFalse(mailTemplateProvider.getTemplateBuilder().get(PluginKey.key(TaskAssignPlugin.ID)).buildDigest(ctx, writer));
    list.remove(notificationInfo2);
    notificationInfo3.setTo("root");
    notificationInfo3.setId(TaskCompletedPlugin.ID);
    notificationInfo3.key(TaskCompletedPlugin.ID);
    ownerParameter.put(NotificationUtils.CREATOR.getKey(), "user1");
    ownerParameter.put(NotificationUtils.TASK_COWORKERS, "user2");
    ownerParameter.put(NotificationUtils.TASK_ASSIGNEE, "user3");
    ownerParameter.put(NotificationUtils.ADDED_COWORKER, "user4");
    notificationInfo3.setOwnerParameter(ownerParameter);
    list.add(notificationInfo3);
    newCtx.setNotificationInfos(list);
    assertFalse(mailTemplateProvider.getTemplateBuilder().get(PluginKey.key(TaskCompletedPlugin.ID)).buildDigest(newCtx, writer));
  }

  private NotificationInfo createNotification() {
    try {
      return NotificationInfo.instance();
    } catch (Exception e) {
      LOG.error("Error getting notification", e);
      fail("Error getting notification instance: " + e.getMessage());
    }
    return null;
  }
}

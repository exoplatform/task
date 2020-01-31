package org.exoplatform.task.integration.notification;

import org.exoplatform.commons.api.notification.NotificationContext;
import org.exoplatform.commons.api.notification.model.NotificationInfo;
import org.exoplatform.commons.api.notification.model.PluginKey;
import org.exoplatform.commons.notification.impl.NotificationContextImpl;
import org.exoplatform.container.PortalContainer;
import org.exoplatform.container.xml.InitParams;
import org.exoplatform.services.idgenerator.IDGeneratorService;
import org.exoplatform.task.service.UserService;
import org.mockito.Mockito;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.when;
import static org.junit.Assert.*;


import org.exoplatform.services.jcr.util.IdGenerator;
import org.junit.Test;
import java.io.Writer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MailTemplateProviderTest {

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
    ownerParameter.put(NotificationUtils.TASK_CREATOR, "user1");
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
    ownerParameter.put(NotificationUtils.TASK_CREATOR, "user1");
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
    ownerParameter.put(NotificationUtils.TASK_CREATOR, "user1");
    ownerParameter.put(NotificationUtils.TASK_COWORKERS, "user2");
    ownerParameter.put(NotificationUtils.TASK_ASSIGNEE, "user3");
    ownerParameter.put(NotificationUtils.ADDED_COWORKER, "user4");
    notificationInfo3.setOwnerParameter(ownerParameter);
    list.add(notificationInfo3);
    newCtx.setNotificationInfos(list);
    assertFalse(mailTemplateProvider.getTemplateBuilder().get(PluginKey.key(TaskCompletedPlugin.ID)).buildDigest(newCtx, writer));
  } 

    @Test
    public void testMakeDigest() {
        IDGeneratorService idGeneratorService = Mockito.mock(IDGeneratorService.class);
        UserService userService = Mockito.mock(UserService.class);
        when(idGeneratorService.generateStringID(any())).thenReturn("test");
        PortalContainer.getInstance().registerComponentInstance(IDGeneratorService.class, idGeneratorService);
        IdGenerator idGenerator = new IdGenerator(idGeneratorService);
        PortalContainer.getInstance().registerComponentInstance(IdGenerator.class, idGenerator);
        InitParams initParams = new InitParams();
        Writer writer = null;
        NotificationContext context = NotificationContextImpl.cloneInstance();
        NotificationInfo notificationInfo1 = NotificationInfo.instance();
        NotificationInfo notificationInfo2 = NotificationInfo.instance();
        NotificationInfo notificationInfo3 = NotificationInfo.instance();
        List<NotificationInfo> list = new ArrayList<NotificationInfo>();
        NotificationContext newCtx = NotificationContextImpl.cloneInstance();
        Map<String, String> ownerParameter = new HashMap<String, String>();
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
}

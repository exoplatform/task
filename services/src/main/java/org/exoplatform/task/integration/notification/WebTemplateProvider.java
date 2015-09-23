/*
 * Copyright (C) 2003-2015 eXo Platform SAS.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package org.exoplatform.task.integration.notification;

import java.io.Writer;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

import org.exoplatform.commons.api.notification.NotificationContext;
import org.exoplatform.commons.api.notification.NotificationMessageUtils;
import org.exoplatform.commons.api.notification.annotation.TemplateConfig;
import org.exoplatform.commons.api.notification.annotation.TemplateConfigs;
import org.exoplatform.commons.api.notification.channel.template.AbstractTemplateBuilder;
import org.exoplatform.commons.api.notification.channel.template.TemplateProvider;
import org.exoplatform.commons.api.notification.model.MessageInfo;
import org.exoplatform.commons.api.notification.model.NotificationInfo;
import org.exoplatform.commons.api.notification.model.PluginKey;
import org.exoplatform.commons.api.notification.service.storage.WebNotificationStorage;
import org.exoplatform.commons.api.notification.service.template.TemplateContext;
import org.exoplatform.commons.notification.template.TemplateUtils;
import org.exoplatform.commons.utils.CommonsUtils;
import org.exoplatform.container.xml.InitParams;
import org.exoplatform.social.core.identity.model.Identity;
import org.exoplatform.social.core.identity.model.Profile;
import org.exoplatform.social.core.identity.provider.OrganizationIdentityProvider;
import org.exoplatform.social.core.manager.IdentityManager;
import org.exoplatform.social.core.service.LinkProvider;
import org.exoplatform.social.notification.LinkProviderUtils;
import org.exoplatform.task.service.UserService;
import org.exoplatform.webui.utils.TimeConvertUtils;

@TemplateConfigs (
   templates = {
       @TemplateConfig( pluginId=TaskAssignPlugin.ID, template="war:/notification/templates/web/TaskAssignPlugin.gtmpl"),
       @TemplateConfig( pluginId=TaskCoworkerPlugin.ID, template="war:/notification/templates/web/TaskCoworkerPlugin.gtmpl"),
       @TemplateConfig( pluginId=TaskDueDatePlugin.ID, template="war:/notification/templates/web/TaskDueDatePlugin.gtmpl"),
       @TemplateConfig( pluginId=TaskCompletedPlugin.ID, template="war:/notification/templates/web/TaskCompletedPlugin.gtmpl")
   }
)
public class WebTemplateProvider extends TemplateProvider { 
  
  public WebTemplateProvider(InitParams initParams) {
    super(initParams);
    this.templateBuilders.put(PluginKey.key(TaskAssignPlugin.ID), new TemplateBuilder());
    this.templateBuilders.put(PluginKey.key(TaskCoworkerPlugin.ID), new TemplateBuilder());
    this.templateBuilders.put(PluginKey.key(TaskDueDatePlugin.ID), new TemplateBuilder());
    this.templateBuilders.put(PluginKey.key(TaskCompletedPlugin.ID), new TemplateBuilder());
  }

  private class TemplateBuilder extends AbstractTemplateBuilder {
    @Override
    protected MessageInfo makeMessage(NotificationContext ctx) {
      NotificationInfo notification = ctx.getNotificationInfo();      
      String pluginId = notification.getKey().getId();      
      //This is actually contain projectId of task
      //workaround to use it in getUnreadNotification method (the notification storage get by filter has not been implemented)
      String activityId = notification.getValueOwnerParameter(NotificationUtils.ACTIVITY_ID);      
      
      if (ctx.isWritingProcess()) {
        WebNotificationStorage storage = CommonsUtils.getService(WebNotificationStorage.class);
        NotificationInfo prevNotif = storage.getUnreadNotification(pluginId, activityId, notification.getTo());
        if (prevNotif != null) {
          prevNotif.with(NotificationUtils.TASK_TITLE, "");
          String count = prevNotif.getValueOwnerParameter(NotificationUtils.COUNT);
          if (count != null) {
            prevNotif.with(NotificationUtils.COUNT, String.valueOf(Integer.parseInt(count) + 1));
          } else {
            prevNotif.with(NotificationUtils.COUNT, String.valueOf(2));
          }
          prevNotif.setUpdate(true);
          prevNotif.setLastModifiedDate(System.currentTimeMillis());
          prevNotif.setDateCreated(Calendar.getInstance());
          ctx.setNotificationInfo(prevNotif);
          notification = prevNotif;
        }
      }
      
      String language = getLanguage(notification);
      TemplateContext templateContext = TemplateContext.newChannelInstance(getChannelKey(), pluginId, language);
      
      String creator = notification.getValueOwnerParameter(NotificationUtils.CREATOR.getKey());
      String projectName = notification.getValueOwnerParameter(NotificationUtils.PROJECT_NAME);
      String taskTitle = notification.getValueOwnerParameter(NotificationUtils.TASK_TITLE);
      String taskUrl = notification.getValueOwnerParameter(NotificationUtils.TASK_URL);
      String projectUrl = notification.getValueOwnerParameter(NotificationUtils.PROJECT_URL);
      
      Date dueDate = null;
      String tmpD = notification.getValueOwnerParameter(NotificationUtils.DUE_DATE);
      if (tmpD != null) {
        dueDate = new Date(Long.parseLong(tmpD));
      }
      
      Identity identity = CommonsUtils.getService(IdentityManager.class).getOrCreateIdentity(OrganizationIdentityProvider.NAME, creator, true);
      Profile profile = identity.getProfile();
      templateContext.put("USER", profile.getFullName());
      templateContext.put("AVATAR", profile.getAvatarUrl() != null ? profile.getAvatarUrl() : LinkProvider.PROFILE_DEFAULT_AVATAR_URL);
      templateContext.put("PROFILE_URL", LinkProviderUtils.getRedirectUrl("user", identity.getRemoteId()));
      //
      templateContext.put("PROJECT_NAME", projectName);
      templateContext.put("TASK_URL", taskUrl);
      templateContext.put("PROJECT_URL", projectUrl);
      templateContext.put("TASK_TITLE", taskTitle);
      UserService userService = CommonsUtils.getService(UserService.class);
      templateContext.put("DUE_DATE", org.exoplatform.task.integration.notification.TemplateUtils.format(dueDate, userService.getUserTimezone(notification.getTo())));        
      //
      templateContext.put("COUNT", notification.getValueOwnerParameter(NotificationUtils.COUNT));
      templateContext.put("READ", Boolean.valueOf(notification.getValueOwnerParameter(NotificationMessageUtils.READ_PORPERTY.getKey())) ? "read" : "unread");
      templateContext.put("NOTIFICATION_ID", notification.getId());      
      templateContext.put("LAST_UPDATED_TIME", TimeConvertUtils.convertXTimeAgoByTimeServer(notification.getDateCreated().getTime(), 
                                                                                            "EE, dd yyyy", new Locale(language), TimeConvertUtils.YEAR));

      //
      String body = TemplateUtils.processGroovy(templateContext);
      //binding the exception throws by processing template
      ctx.setException(templateContext.getException());
      MessageInfo messageInfo = new MessageInfo();
      return messageInfo.body(body).end();
    }

    @Override
    protected boolean makeDigest(NotificationContext ctx, Writer writer) {
      return false;
    }

  };

}

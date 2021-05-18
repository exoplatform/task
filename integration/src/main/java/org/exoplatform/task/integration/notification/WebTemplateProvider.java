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
import org.exoplatform.commons.utils.HTMLEntityEncoder;
import org.exoplatform.container.xml.InitParams;
import org.exoplatform.social.core.identity.model.Identity;
import org.exoplatform.social.core.identity.model.Profile;
import org.exoplatform.social.core.identity.provider.OrganizationIdentityProvider;
import org.exoplatform.social.core.manager.IdentityManager;
import org.exoplatform.social.core.service.LinkProvider;
import org.exoplatform.social.notification.LinkProviderUtils;
import org.exoplatform.task.service.UserService;
import org.exoplatform.task.util.CommentUtil;
import org.exoplatform.task.util.TaskUtil;
import org.exoplatform.webui.utils.TimeConvertUtils;
import org.gatein.common.text.EntityEncoder;

import java.io.Writer;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;

@TemplateConfigs (
   templates = {
       @TemplateConfig( pluginId=TaskAssignPlugin.ID, template="war:/notification/templates/web/TaskAssignPlugin.gtmpl"),
       @TemplateConfig( pluginId=TaskCoworkerPlugin.ID, template="war:/notification/templates/web/TaskCoworkerPlugin.gtmpl"),
       @TemplateConfig( pluginId=TaskDueDatePlugin.ID, template="war:/notification/templates/web/TaskDueDatePlugin.gtmpl"),
       @TemplateConfig( pluginId=TaskCompletedPlugin.ID, template="war:/notification/templates/web/TaskCompletedPlugin.gtmpl"),
       @TemplateConfig( pluginId=TaskCommentPlugin.ID, template="war:/notification/templates/web/TaskCommentPlugin.gtmpl"),
       @TemplateConfig( pluginId=TaskMentionPlugin.ID, template="war:/notification/templates/web/TaskMentionPlugin.gtmpl"),
       @TemplateConfig( pluginId=TaskEditionPlugin.ID, template="war:/notification/templates/web/TaskEditionPlugin.gtmpl")
   }
)
public class WebTemplateProvider extends TemplateProvider { 
  
  public WebTemplateProvider(InitParams initParams) {
    super(initParams);
    this.templateBuilders.put(PluginKey.key(TaskAssignPlugin.ID), new TemplateBuilder());
    this.templateBuilders.put(PluginKey.key(TaskCoworkerPlugin.ID), new TemplateBuilder());
    this.templateBuilders.put(PluginKey.key(TaskDueDatePlugin.ID), new TemplateBuilder());
    this.templateBuilders.put(PluginKey.key(TaskCompletedPlugin.ID), new TemplateBuilder());
    this.templateBuilders.put(PluginKey.key(TaskCommentPlugin.ID), new CommentTemplateBuilder());
    this.templateBuilders.put(PluginKey.key(TaskMentionPlugin.ID), new TemplateBuilder());
    this.templateBuilders.put(PluginKey.key(TaskEditionPlugin.ID), new TemplateBuilder());
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
          ctx.setNotificationInfo(prevNotif);
          notification = prevNotif;
        }
      }
      
      String language = getLanguage(notification);
      TemplateContext templateContext = TemplateContext.newChannelInstance(getChannelKey(), pluginId, language);
      
      String creator = notification.getValueOwnerParameter(NotificationUtils.CREATOR.getKey());
      String projectName = notification.getValueOwnerParameter(NotificationUtils.PROJECT_NAME);
      String actionName = notification.getValueOwnerParameter(NotificationUtils.ACTION_NAME.getKey());

      String taskTitle = notification.getValueOwnerParameter(NotificationUtils.TASK_TITLE);
      String taskUrl = notification.getValueOwnerParameter(NotificationUtils.TASK_URL);
      String projectUrl = notification.getValueOwnerParameter(NotificationUtils.PROJECT_URL);

      Date dueDate = null;
      String tmpD = notification.getValueOwnerParameter(NotificationUtils.DUE_DATE);
      if (tmpD != null) {
        dueDate = new Date(Long.parseLong(tmpD));
      }
      
      EntityEncoder encoder = HTMLEntityEncoder.getInstance();
      Identity identity = CommonsUtils.getService(IdentityManager.class).getOrCreateIdentity(OrganizationIdentityProvider.NAME, creator, true);
      Profile profile = identity.getProfile();
      String fullName = profile.getFullName();
      if(CommentUtil.isExternal(identity.getRemoteId())) {
        fullName += " " + "(" + TaskUtil.getResourceBundleLabel(new Locale(TaskUtil.getUserLanguage(identity.getRemoteId())), "external.label.tag") + ")";
      }
      templateContext.put("USER", encoder.encode(fullName));
      templateContext.put("AVATAR", profile.getAvatarUrl() != null ? profile.getAvatarUrl() : LinkProvider.PROFILE_DEFAULT_AVATAR_URL);
      templateContext.put("PROFILE_URL", LinkProviderUtils.getRedirectUrl("user", identity.getRemoteId()));
      //
      templateContext.put("PROJECT_NAME", encoder.encode(projectName));
      templateContext.put("TASK_URL", taskUrl);
      templateContext.put("PROJECT_URL", projectUrl);
      templateContext.put("TASK_TITLE", encoder.encode(taskTitle));
      UserService userService = CommonsUtils.getService(UserService.class);
      templateContext.put("DUE_DATE", org.exoplatform.task.integration.notification.TemplateUtils.format(dueDate, userService.getUserTimezone(notification.getTo())));        
      //
      templateContext.put("COUNT", notification.getValueOwnerParameter(NotificationUtils.COUNT));
      templateContext.put("READ", Boolean.valueOf(notification.getValueOwnerParameter(NotificationMessageUtils.READ_PORPERTY.getKey())) ? "read" : "unread");
      templateContext.put("NOTIFICATION_ID", notification.getId());      
      Calendar lastModified = Calendar.getInstance();
      lastModified.setTimeInMillis(notification.getLastModifiedDate());
      templateContext.put("LAST_UPDATED_TIME", TimeConvertUtils.convertXTimeAgoByTimeServer(lastModified.getTime(),
                                                                                            "EE, dd yyyy", new Locale(language), TimeConvertUtils.YEAR));

      templateContext.put("ACTION_NAME", encoder.encode(actionName.toString()));
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

  private class CommentTemplateBuilder extends AbstractTemplateBuilder {
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
          // Count tasks
          List<Long> taskIds = parseListTaskId(prevNotif.getValueOwnerParameter(NotificationUtils.TASKS));
          Long newId = Long.parseLong(notification.getValueOwnerParameter(NotificationUtils.TASKS));
          taskIds.remove(newId);
          taskIds.add(newId);
          if (taskIds.size() > 1) {
            prevNotif.with(NotificationUtils.TASK_TITLE, "");
            prevNotif.with(NotificationUtils.COUNT, String.valueOf(taskIds.size()));
          }
          prevNotif.with(NotificationUtils.TASKS, mergeListTaskId(taskIds));

          // Count user
          List<String> users = parseListUser(prevNotif.getValueOwnerParameter(NotificationUtils.CREATOR.getKey()));
          String newUser = notification.getValueOwnerParameter(NotificationUtils.CREATOR.getKey());
          users.remove(newUser);
          users.add(newUser);

          prevNotif.with(NotificationUtils.CREATOR.getKey(), mergeUsers(users));

          prevNotif.setUpdate(true);
          prevNotif.setLastModifiedDate(System.currentTimeMillis());
          prevNotif.setDateCreated(Calendar.getInstance());
          ctx.setNotificationInfo(prevNotif);
          notification = prevNotif;
        }
      }

      String language = getLanguage(notification);
      TemplateContext templateContext = TemplateContext.newChannelInstance(getChannelKey(), pluginId, language);

      List<String> creator = parseListUser(notification.getValueOwnerParameter(NotificationUtils.CREATOR.getKey()));
      String projectName = notification.getValueOwnerParameter(NotificationUtils.PROJECT_NAME);
      String taskTitle = notification.getValueOwnerParameter(NotificationUtils.TASK_TITLE);
      String taskUrl = notification.getValueOwnerParameter(NotificationUtils.TASK_URL);
      String projectUrl = notification.getValueOwnerParameter(NotificationUtils.PROJECT_URL);

      Date dueDate = null;
      String tmpD = notification.getValueOwnerParameter(NotificationUtils.DUE_DATE);
      if (tmpD != null) {
        dueDate = new Date(Long.parseLong(tmpD));
      }

      EntityEncoder encoder = HTMLEntityEncoder.getInstance();
      IdentityManager identityManager = CommonsUtils.getService(IdentityManager.class);

      Collections.reverse(creator);
      templateContext.put("TOTAL_USER", creator.size());
      Identity identity = identityManager.getOrCreateIdentity(OrganizationIdentityProvider.NAME, creator.get(0), true);
      Profile lastUser = identity.getProfile();
      String fullName = lastUser.getFullName();
      if(CommentUtil.isExternal(identity.getRemoteId())) {
        fullName += " " + "(" + TaskUtil.getResourceBundleLabel(new Locale(TaskUtil.getUserLanguage(identity.getRemoteId())), "external.label.tag") + ")";
      }
      templateContext.put("AVATAR", lastUser.getAvatarUrl() != null ? lastUser.getAvatarUrl() : LinkProvider.PROFILE_DEFAULT_AVATAR_URL);
      templateContext.put("USER", encoder.encode(fullName));
      templateContext.put("PROFILE_URL", LinkProviderUtils.getRedirectUrl("user", (String)lastUser.getProperty(Profile.USERNAME)));

      templateContext.put("COUNT_USER", creator.size() > 2 ? creator.size() - 2 : 0);
      if (creator.size() > 1) {
        Profile lastUser2 = identity.getProfile();
        String fullName2 = lastUser2.getFullName();
         if(CommentUtil.isExternal(identity.getRemoteId())) {
           fullName2 += " " + "(" + TaskUtil.getResourceBundleLabel(new Locale(TaskUtil.getUserLanguage(identity.getRemoteId())), "external.label.tag") + ")";
         }
        templateContext.put("USER2", encoder.encode(fullName2));
        templateContext.put("PROFILE_URL2", LinkProviderUtils.getRedirectUrl("user", (String)lastUser2.getProperty(Profile.USERNAME)));
      }

      //
      templateContext.put("PROJECT_NAME", encoder.encode(projectName));
      templateContext.put("TASK_URL", taskUrl);
      templateContext.put("PROJECT_URL", projectUrl);
      templateContext.put("TASK_TITLE", encoder.encode(taskTitle));
      UserService userService = CommonsUtils.getService(UserService.class);
      templateContext.put("DUE_DATE", org.exoplatform.task.integration.notification.TemplateUtils.format(dueDate, userService.getUserTimezone(notification.getTo())));
      //
      templateContext.put("COUNT", notification.getValueOwnerParameter(NotificationUtils.COUNT));
      templateContext.put("READ", Boolean.valueOf(notification.getValueOwnerParameter(NotificationMessageUtils.READ_PORPERTY.getKey())) ? "read" : "unread");
      templateContext.put("NOTIFICATION_ID", notification.getId());
      Calendar lastModified = Calendar.getInstance();
      lastModified.setTimeInMillis(notification.getLastModifiedDate());
      templateContext.put("LAST_UPDATED_TIME", TimeConvertUtils.convertXTimeAgoByTimeServer(lastModified.getTime(),
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
  }

  private List<Long> parseListTaskId(String ids) {
    List<Long> set = new ArrayList<>();
    if (ids != null && !ids.trim().isEmpty()) {
      for (String id : ids.trim().split(",")) {
        set.add(Long.parseLong(id));
      }
    }
    return set;
  }
  private String mergeListTaskId(List<Long> ids) {
    StringBuilder sb = new StringBuilder();
    if (ids != null && ids.size() > 0) {
      for (Long id : ids) {
        sb.append(id).append(",");
      }
      sb.deleteCharAt(sb.length() - 1);
    }
    return sb.toString();
  }

  private List<String> parseListUser(String users) {
    List<String> set = new ArrayList<>();
    if (users != null && !users.isEmpty()) {
      for (String s : users.split(",")) {
        if (s.trim().length() > 0) {
          set.add(s.trim());
        }
      }
    }
    return set;
  }
  private String mergeUsers(List<String> users) {
    StringBuilder sb = new StringBuilder();
    if (users != null && users.size() > 0) {
      for (String u : users) {
        sb.append(u).append(",");
      }
      sb.deleteCharAt(sb.length() - 1);
    }
    return sb.toString();
  }
}

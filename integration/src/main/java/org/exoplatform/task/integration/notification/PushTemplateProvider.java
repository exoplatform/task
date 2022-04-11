/*
 * Copyright (C) 2003-2018 eXo Platform SAS.
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
import org.exoplatform.commons.api.notification.annotation.TemplateConfig;
import org.exoplatform.commons.api.notification.annotation.TemplateConfigs;
import org.exoplatform.commons.api.notification.channel.template.AbstractTemplateBuilder;
import org.exoplatform.commons.api.notification.model.MessageInfo;
import org.exoplatform.commons.api.notification.model.NotificationInfo;
import org.exoplatform.commons.api.notification.model.PluginKey;
import org.exoplatform.container.xml.InitParams;

import java.io.Writer;
import java.util.*;

@TemplateConfigs (
   templates = {
       @TemplateConfig(pluginId=TaskAssignPlugin.ID, template="war:/notification/templates/push/TaskAssignPlugin.gtmpl"),
       @TemplateConfig(pluginId=TaskCoworkerPlugin.ID, template="war:/notification/templates/push/TaskCoworkerPlugin.gtmpl"),
       @TemplateConfig(pluginId=TaskDueDatePlugin.ID, template="war:/notification/templates/push/TaskDueDatePlugin.gtmpl"),
       @TemplateConfig(pluginId=TaskCompletedPlugin.ID, template="war:/notification/templates/push/TaskCompletedPlugin.gtmpl"),
       @TemplateConfig(pluginId=TaskCommentPlugin.ID, template="war:/notification/templates/push/TaskCommentPlugin.gtmpl"),
       @TemplateConfig(pluginId=TaskMentionPlugin.ID, template="war:/notification/templates/push/TaskMentionPlugin.gtmpl"),
       @TemplateConfig(pluginId=TaskEditionPlugin.ID, template="war:/notification/templates/push/TaskEditionPlugin.gtmpl")
   }
)
public class PushTemplateProvider extends WebTemplateProvider {

  private final Map<PluginKey, AbstractTemplateBuilder> webTemplateBuilders = new HashMap<>();

  private class TemplateBuilder extends AbstractTemplateBuilder {
    @Override
    protected MessageInfo makeMessage(NotificationContext ctx) {
      MessageInfo messageInfo = webTemplateBuilders.get(new PluginKey(TaskAssignPlugin.ID)).buildMessage(ctx);

      NotificationInfo notification = ctx.getNotificationInfo();

      String taskUrl = notification.getValueOwnerParameter(NotificationUtils.TASK_URL);

      return messageInfo.subject(taskUrl).end();
    }

    @Override
    protected boolean makeDigest(NotificationContext ctx, Writer writer) {
      return false;
    }

  }

  private class CommentTemplateBuilder extends AbstractTemplateBuilder {
    @Override
    protected MessageInfo makeMessage(NotificationContext ctx) {
      MessageInfo messageInfo = webTemplateBuilders.get(new PluginKey(TaskCommentPlugin.ID)).buildMessage(ctx);

      NotificationInfo notification = ctx.getNotificationInfo();

      String taskUrl = notification.getValueOwnerParameter(NotificationUtils.TASK_URL);

      return messageInfo.subject(taskUrl).end();
    }

    @Override
    protected boolean makeDigest(NotificationContext ctx, Writer writer) {
      return false;
    }
  }

  public PushTemplateProvider(InitParams initParams) {
    super(initParams);
    this.webTemplateBuilders.putAll(this.templateBuilders);
    this.templateBuilders.put(PluginKey.key(TaskAssignPlugin.ID), new TemplateBuilder());
    this.templateBuilders.put(PluginKey.key(TaskCoworkerPlugin.ID), new TemplateBuilder());
    this.templateBuilders.put(PluginKey.key(TaskDueDatePlugin.ID), new TemplateBuilder());
    this.templateBuilders.put(PluginKey.key(TaskCompletedPlugin.ID), new TemplateBuilder());
    this.templateBuilders.put(PluginKey.key(TaskCommentPlugin.ID), new CommentTemplateBuilder());
    this.templateBuilders.put(PluginKey.key(TaskMentionPlugin.ID), new TemplateBuilder());
  }
}

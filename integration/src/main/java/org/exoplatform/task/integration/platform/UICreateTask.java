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

package org.exoplatform.task.integration.platform;

import org.apache.commons.lang.StringEscapeUtils;
import org.exoplatform.container.ExoContainerContext;
import org.exoplatform.portal.mop.SiteKey;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.task.domain.Task;
import org.exoplatform.task.service.ParserContext;
import org.exoplatform.task.service.TaskParser;
import org.exoplatform.task.service.TaskService;
import org.exoplatform.task.service.UserService;
import org.exoplatform.task.util.TaskUtil;
import org.exoplatform.web.WebAppController;
import org.exoplatform.webui.application.WebuiRequestContext;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.core.UIComponent;
import org.exoplatform.webui.core.lifecycle.UIFormLifecycle;
import org.exoplatform.webui.event.Event;
import org.exoplatform.webui.event.EventListener;
import org.exoplatform.webui.form.UIForm;
import org.exoplatform.webui.form.UIFormStringInput;

import java.util.Date;
import java.util.MissingResourceException;
import java.util.ResourceBundle;
import java.util.TimeZone;

/**
 * @author <a href="mailto:tuyennt@exoplatform.com">Tuyen Nguyen The</a>.
 */
@ComponentConfig(
    lifecycle = UIFormLifecycle.class,
    template = "classpath:groovy/webui/create/UICreateTask.gtmpl",
    events = {
      @EventConfig(
        listeners = UICreateTask.AddActionListener.class,
        phase = Event.Phase.DECODE
      ),
      @EventConfig(
        listeners = UICreateTask.CancelActionListener.class,
        phase = Event.Phase.DECODE
      )
    }
)
public class UICreateTask extends UIForm {
  private static Log log = ExoLogger.getLogger(UICreateTask.class);

  public static String TITLE = "title";

  private final TaskParser taskParser;
  private final TaskService taskService;
  private final UserService userService;

  private final WebAppController webAppController;

  protected String errorMessage = null;

  public UICreateTask() {
    UIFormStringInput input = new UIFormStringInput(TITLE, TITLE, null);
    input.setHTMLAttribute("class", "span3");
    addUIFormInput(input);

    taskParser = getApplicationComponent(TaskParser.class);
    taskService = getApplicationComponent(TaskService.class);
    userService = getApplicationComponent(UserService.class);
    webAppController = getApplicationComponent(WebAppController.class);
  }

  public String getError() {
    return this.errorMessage;
  }

  public static String getResourceBundle(String key, String defaultValue) {
    WebuiRequestContext context = WebuiRequestContext.getCurrentInstance();
    ResourceBundle res = context.getApplicationResourceBundle();
    try {
      return res.getString(key);
    } catch (MissingResourceException e) {
      log.warn("Can not find the resource for key: " + key);
      return defaultValue;
    }
  }

  static public class AddActionListener extends EventListener<UICreateTask> {
    @Override
    public void execute(Event<UICreateTask> event) throws Exception {
      UICreateTask ui = event.getSource();
      ui.errorMessage = null;

      UIFormStringInput input = ui.getUIStringInput(TITLE);
      String title = input.getValue();

      if (title == null || title.trim().isEmpty()) {
        ui.errorMessage = UICreateTask.getResourceBundle(ui.getId() + ".msg.titleRequired", "Task title is required");
        event.getRequestContext().addUIComponentToUpdateByAjax(ui);
        return;
      }

      //. Get user timezone
      String username = event.getRequestContext().getRemoteUser();
      TimeZone tz = ui.userService.getUserTimezone(username);
      Task task = ui.taskParser.parse(title, new ParserContext(tz));
      task.setCreatedBy(username);
      task.setCreatedTime(new Date());
      task = ui.taskService.createTask(task);

      // Close the form after create task
      Event<UIComponent> cancelEvent = ui.getParent().createEvent("Cancel", Event.Phase.PROCESS, event.getRequestContext());
      if (cancelEvent != null) {
        cancelEvent.broadcast();
      }

      event.getRequestContext().getUserPortal();
      String taskURL = TaskUtil.buildTaskURL(task, SiteKey.portal("intranet"), ExoContainerContext.getCurrentContainer(), ui.webAppController.getRouter());

      String defaultMsg = "The task has been added";
      String message =  UICreateTask.getResourceBundle(ui.getId()+".msg.add-successfully", defaultMsg);

      event.getRequestContext().getJavascriptManager().require("SHARED/navigation-toolbar", "toolbarnav")
              .addScripts("toolbarnav.UIPortalNavigation.cancelNextClick('UICreateList','UICreatePlatformToolBarPortlet','" + StringEscapeUtils.escapeJavaScript(message) + "', '"+ taskURL +"');");
    }
  }

  static public class CancelActionListener extends EventListener<UICreateTask> {
    public void execute(Event<UICreateTask> event) throws Exception {
      UICreateTask uisource = event.getSource();
      WebuiRequestContext ctx = event.getRequestContext();
      Event<UIComponent> cancelEvent = uisource.getParent().createEvent("Cancel", Event.Phase.DECODE, ctx);
      if (cancelEvent != null) {
        cancelEvent.broadcast();
      }
    }
  }
}

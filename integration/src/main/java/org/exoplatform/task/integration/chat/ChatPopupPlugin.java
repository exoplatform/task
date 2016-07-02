/*
 * Copyright (C) 2012 eXo Platform SAS.
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

package org.exoplatform.task.integration.chat;

import javax.ws.rs.core.MediaType;

import java.nio.charset.Charset;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.output.StringBuilderWriter;
import org.apache.commons.lang.StringUtils;
import org.exoplatform.commons.api.ui.ActionContext;
import org.exoplatform.commons.api.ui.BaseUIPlugin;
import org.exoplatform.commons.api.ui.RenderContext;
import org.exoplatform.commons.api.ui.Response;
import org.exoplatform.container.xml.InitParams;
import org.exoplatform.container.xml.ValueParam;
import org.exoplatform.groovyscript.text.BindingContext;
import org.exoplatform.groovyscript.text.TemplateService;
import org.exoplatform.resolver.ClasspathResourceResolver;
import org.exoplatform.resolver.ResourceResolver;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.services.security.ConversationState;
import org.exoplatform.social.core.space.model.Space;
import org.exoplatform.social.core.space.spi.SpaceService;
import org.exoplatform.task.domain.Project;
import org.exoplatform.task.domain.Task;
import org.exoplatform.task.service.ParserContext;
import org.exoplatform.task.service.ProjectService;
import org.exoplatform.task.service.StatusService;
import org.exoplatform.task.service.TaskParser;
import org.exoplatform.task.service.TaskService;
import org.exoplatform.task.service.UserService;
import org.exoplatform.task.util.ProjectUtil;

public class ChatPopupPlugin extends BaseUIPlugin {

  private static Log          log                       =
                                  ExoLogger.getExoLogger(ChatPopupPlugin.class);

  private static final String POPUP                     = "classpath:/groovy/TaskPopup.gtmpl";

  private static final String TYPE                      = "type";

  private static final String CREATE_TASK_ACTION        = "createTask";

  private static final String CREATE_TASK_INLINE_ACTION = "createTaskInline";

  private TemplateService     templateService;

  private ProjectService      projectService;

  private StatusService       statusService;

  private SpaceService        spaceService;

  private TaskService         taskService;

  private UserService         userService;

  private TaskParser          taskParser;

  private String              pluginType                = "chat_extension_popup";

  public ChatPopupPlugin(InitParams params,
                         TemplateService templateService,
                         ProjectService projectService,
                         StatusService statusService,
                         SpaceService spaceService,
                         TaskService taskService,
                         UserService userService,
                         TaskParser taskParser) {
    this.templateService = templateService;
    this.projectService = projectService;
    this.statusService = statusService;
    this.taskService = taskService;
    this.spaceService = spaceService;
    this.userService = userService;
    this.taskParser = taskParser;

    ValueParam param = params.getValueParam(TYPE);
    if (param != null) {
      pluginType = param.getValue();
    }
  }

  @Override
  public Response render(RenderContext renderContext) {
    ResourceResolver resolver = new ClasspathResourceResolver();
    StringBuilderWriter writer = new StringBuilderWriter();

    DateFormat df = new SimpleDateFormat("MM/dd/yyyy");
    Date today = Calendar.getInstance().getTime();
    String todayDate = df.format(today);

    BindingContext bindingContext = new BindingContext(resolver, writer);
    bindingContext.put("rs", renderContext.getRsBundle());

    try {
      bindingContext.put("today", todayDate);
      bindingContext.put("actionUrl", renderContext.getActionUrl());
      templateService.merge(POPUP, bindingContext);
    } catch (Exception ex) {
      log.error(ex.getMessage(), ex);
    }
    String result = writer.getBuilder().toString();
    return new Response(result.getBytes(Charset.forName("UTF-8")), MediaType.TEXT_HTML);
  }

  @Override
  public void processAction(ActionContext context) {
    Map<String, List<String>> params = context.getParams();
    String actionName = context.getName();

    String creator = ConversationState.getCurrent().getIdentity().getUserId();
    if (CREATE_TASK_ACTION.equals(actionName)) {
      String username = getParam("username", params);
      username = StringUtils.isEmpty(username) ? creator : username;

      SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy HH:mm");
      Date today = new Date();
      today.setHours(0);
      today.setMinutes(0);
      Date dueDate = new Date();
      try {
        dueDate = sdf.parse(getParam("dueDate", params) + " 23:59");
      } catch (Exception ex) {
        log.error(ex.getMessage(), ex);
      }

      String taskTitle = getParam("task", params);
      String roomName = getParam("roomName", params);
      String isSpace = getParam("isSpace", params);

      for (String name : username.split(",")) {
        Task task = new Task();
        task.setAssignee(name);
        task.setTitle(taskTitle);
        task.setDueDate(dueDate);
        task.setCreatedBy(creator);

        // find default project of space
        if ("true".equals(isSpace)) {
          if (StringUtils.isNotEmpty(roomName)) {
            Space space = spaceService.getSpaceByPrettyName(roomName);
            List<Project> projects = ProjectUtil.getProjectTree(space.getGroupId(), projectService);
            if (projects != null && projects.size() > 0) {
              task.setStatus(statusService.getDefaultStatus(projects.get(0).getId()));
            }
          }
        }
        taskService.createTask(task);
      }
    } else if (CREATE_TASK_INLINE_ACTION.equals(actionName)) {
      String msg = getParam("msg", params);
      ParserContext parserCtx = new ParserContext(userService.getUserTimezone(creator));
      taskService.createTask(taskParser.parse(msg, parserCtx));
    }
  }

  private String getParam(String name, Map<String, List<String>> params) {
    if (params.get(name) != null && params.get(name).size() > 0) {
      return params.get(name).get(0);
    }
    return null;
  }

  @Override
  public String getType() {
    return pluginType;
  }

}

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
import java.text.SimpleDateFormat;
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
import org.exoplatform.task.service.ProjectService;
import org.exoplatform.task.service.StatusService;
import org.exoplatform.task.service.TaskService;
import org.exoplatform.task.util.ProjectUtil;

public class ChatTaskPlugin extends BaseUIPlugin<RenderContext, ActionContext> {

  private static Log          log        = ExoLogger.getExoLogger(ChatTaskPlugin.class);

  private static final String MENU_ITEM  = "classpath:/groovy/TaskMenuItem.gtmpl";

  private static final String POPUP      = "classpath:/groovy/TaskPopup.gtmpl";

  private static final String TYPE       = "type";

  private TemplateService     templateService;

  private ProjectService      projectService;

  private StatusService       statusService;

  private SpaceService        spaceService;

  private TaskService         taskService;

  private String              pluginType = "task";

  public ChatTaskPlugin(InitParams params,
                        TemplateService templateService,
                        ProjectService projectService,
                        StatusService statusService,
                        SpaceService spaceService,
                        TaskService taskService) {
    this.templateService = templateService;
    this.projectService = projectService;
    this.statusService = statusService;
    this.taskService = taskService;
    this.spaceService = spaceService;

    ValueParam param = params.getValueParam(TYPE);
    if (param != null) {
      pluginType = param.getValue();
    }
  }

  @Override
  public Response render(RenderContext renderContext) {
    ResourceResolver resolver = new ClasspathResourceResolver();
    StringBuilderWriter writer = new StringBuilderWriter();

    BindingContext bindingContext = new BindingContext(resolver, writer);
    bindingContext.put("rs", renderContext.getRsBundle());

    Map<String, ?> params = renderContext.getParams();
    try {
      Object renderPopup = params.get("renderPopup");
      if (renderPopup != null && (boolean) renderPopup) {
        bindingContext.put("today", params.get("today"));
        bindingContext.put("actionUrl", renderContext.getActionUrl());
        templateService.merge(POPUP, bindingContext);
      } else {
        templateService.merge(MENU_ITEM, bindingContext);
      }
    } catch (Exception ex) {
      log.error(ex.getMessage(), ex);
    }
    String result = writer.getBuilder().toString();
    return new Response(result.getBytes(Charset.forName("UTF-8")), MediaType.TEXT_HTML);
  }

  @Override
  public void processAction(ActionContext context) {
    Map<String, Object> params = context.getParams();
    String creator = ConversationState.getCurrent().getIdentity().getUserId();
    String username = (String) params.get("username");
    username = StringUtils.isEmpty(username) ? creator : username;

    SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy HH:mm");
    Date today = new Date();
    today.setHours(0);
    today.setMinutes(0);
    Date dueDate = new Date();
    try {
      dueDate = sdf.parse((String) params.get("dueDate") + " 23:59");
    } catch (Exception ex) {
      log.error(ex.getMessage(), ex);
    }

    String taskTitle = (String) params.get("task");
    String roomName = (String) params.get("roomName");
    String isSpace = (String) params.get("isSpace");

    for (String name : username.split(",")) {
      Task task = new Task();
      task.setAssignee(name);
      task.setTitle(taskTitle);
      task.setDueDate(dueDate);
      task.setCreatedBy(creator);
      
      //find default project of space
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
  }

  @Override
  public String getType() {
    return pluginType;
  }

}

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

import org.apache.commons.io.output.StringBuilderWriter;
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
import org.exoplatform.social.core.space.spi.SpaceService;
import org.exoplatform.task.service.ProjectService;
import org.exoplatform.task.service.StatusService;
import org.exoplatform.task.service.TaskService;

public class ChatMenuPlugin extends BaseUIPlugin {

  private static Log          log        = ExoLogger.getExoLogger(ChatMenuPlugin.class);

  private static final String MENU_ITEM  = "classpath:/groovy/TaskMenuItem.gtmpl";

  private static final String TYPE       = "type";

  private TemplateService     templateService;

  private String              pluginType = "chat_extension_menu";

  public ChatMenuPlugin(InitParams params,
                        TemplateService templateService,
                        ProjectService projectService,
                        StatusService statusService,
                        SpaceService spaceService,
                        TaskService taskService) {
    this.templateService = templateService;

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
    try {
      templateService.merge(MENU_ITEM, bindingContext);
    } catch (Exception ex) {
      log.error(ex.getMessage(), ex);
    }
    String result = writer.getBuilder().toString();
    return new Response(result.getBytes(Charset.forName("UTF-8")), MediaType.TEXT_HTML);
  }

  @Override
  public void processAction(ActionContext context) {
    
  }

  @Override
  public String getType() {
    return pluginType;
  }

}

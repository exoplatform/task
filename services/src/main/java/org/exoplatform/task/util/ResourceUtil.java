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

package org.exoplatform.task.util;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

import org.exoplatform.commons.utils.CommonsUtils;
import org.exoplatform.container.ExoContainer;
import org.exoplatform.container.ExoContainerContext;
import org.exoplatform.container.PortalContainer;
import org.exoplatform.portal.application.PortalRequestContext;
import org.exoplatform.portal.application.RequestNavigationData;
import org.exoplatform.portal.config.UserPortalConfigService;
import org.exoplatform.portal.mop.SiteKey;
import org.exoplatform.portal.mop.SiteType;
import org.exoplatform.portal.mop.navigation.NavigationContext;
import org.exoplatform.portal.mop.navigation.NavigationService;
import org.exoplatform.portal.mop.navigation.NodeContext;
import org.exoplatform.portal.mop.navigation.NodeModel;
import org.exoplatform.portal.mop.navigation.Scope;
import org.exoplatform.portal.webui.util.Util;
import org.exoplatform.social.common.router.ExoRouter;
import org.exoplatform.social.core.space.SpaceUtils;
import org.exoplatform.social.core.space.model.Space;
import org.exoplatform.social.core.space.spi.SpaceService;
import org.exoplatform.web.WebAppController;
import org.exoplatform.web.controller.QualifiedName;
import org.exoplatform.web.controller.router.Router;
import org.exoplatform.webui.application.WebuiRequestContext;

/**
 * @author <a href="mailto:tuyennt@exoplatform.com">Tuyen Nguyen The</a>.
 */
public class ResourceUtil {
  public static final String TASK_PAGE_NAME = "tasks";
  public static final String TASK_PORTLET_NAME = "TaskManagementApplication";
  public static final String STATUS_KEY_PREFIX = "exo.tasks.status.";
  
  public static String resolveMessage(ResourceBundle bundle, String key, Object... args) {
    if (bundle == null || key == null) {
      return key;
    }
    try {
      String msg = bundle.getString(key);

      if (msg != null && args != null) {
        for (int i = 0; i < args.length; i++) {
          if (args[i] != null) {
            final String messageArg = String.valueOf(args[i]);
            msg = msg.replace("{" + i + "}", messageArg);
          }
        }
      }

      return msg;
    } catch (MissingResourceException ex) {
      return key;
    }
  }

  public static String resolveStatus(ResourceBundle bundle, String name) {
    if (name == null || name.isEmpty()) {
      return "";
    }

    String key = STATUS_KEY_PREFIX + name.toLowerCase();
    try {
      return bundle.getString(key);
    } catch (MissingResourceException ex) {
      return name;
    }
  }
  
  public static <T> List<T> subList(List<T> it, int offset, int limit) {    
    if (it == null) {
      return it;
    }
    if (it.size() <= offset) {
      return Collections.emptyList();
    }
    if (limit < 0) {
      limit = it.size();
    }
    if (offset < 0) {
      offset = 0;
    }    
    limit = offset + limit > it.size() ? it.size() - offset : limit;    
    return (limit == it.size() && offset == 0) ? it : it.subList(offset, offset + limit);    
  }

  public static String buildBaseURL() {
    PortalContainer container = PortalContainer.getInstance();
    WebAppController webAppController = container.getComponentInstanceOfType(WebAppController.class);
    UserPortalConfigService portalConfigService = container.getComponentInstanceOfType(UserPortalConfigService.class);
    Router router = webAppController.getRouter();

    SiteKey siteKey = null;
    Space space = null;
    WebuiRequestContext context = WebuiRequestContext.getCurrentInstance();
    if (context == null) {
      siteKey = SiteKey.portal(portalConfigService.getDefaultPortal());
    } else {
      siteKey = CommonsUtils.getCurrentSite();
      space = SpaceUtils.getSpaceByContext();
    }

    return baseURL(siteKey, container, router, space);
  }

  private static String baseURL(SiteKey siteKey, ExoContainer container, Router router, Space space) {
    if (siteKey == null) {
      return "#";
    }

    // Check taskPage is exist or not
    NodeContext<?> page = null;
    NavigationService navService = container.getComponentInstanceOfType(NavigationService.class);
    NavigationContext ctx = navService.loadNavigation(siteKey);
    if (ctx == null || ctx.getData() == null || ctx.getData().getRootId() == null) {
      return "#";
    }
    Scope scope;
    if (siteKey.getType().equals(SiteType.GROUP)) {
      scope = Scope.GRANDCHILDREN;
    } else {
      scope = Scope.CHILDREN;
    }
    NodeContext<NodeContext<?>> nodeCtx = navService.loadNode(NodeModel.SELF_MODEL, ctx, scope, null);
    if (nodeCtx.getNodeSize() > 0) {
      Collection<NodeContext<?>> children = nodeCtx.getNodes();
      if (siteKey.getType() == SiteType.GROUP) {
        children = nodeCtx.get(0).getNodes();
      }
      Iterator<NodeContext<?>> it = children.iterator();
      NodeContext<?> child = null;
      while (it.hasNext()) {
        child = it.next();
        if (TASK_PAGE_NAME.equals(child.getName()) || child.getName().indexOf(TASK_PORTLET_NAME) >= 0) {
          page = child;
          break;
        }
      }
    }

    String portalName = container.getComponentInstanceOfType(ExoContainerContext.class).getPortalContainerName();

    if (page == null) {
      // Redirect to page tasks
      return new StringBuilder("/")
              .append(portalName)
              .append("/")
              .append(siteKey.getName())
              .append("/")
              .append(TASK_PAGE_NAME)
              .toString();
    }
    String path;
    if (space == null) {
      path = page.getName();
    } else {
      path = space.getPrettyName() + "/" + page.getName();
    }

    Map<QualifiedName, String> qualifiedName = new HashMap<QualifiedName, String>();
    qualifiedName.put(QualifiedName.create("gtn", "handler"), portalName);
    qualifiedName.put(QualifiedName.create("gtn", "sitetype"), siteKey.getTypeName());
    qualifiedName.put(QualifiedName.create("gtn", "sitename"), siteKey.getName());
    qualifiedName.put(QualifiedName.create("gtn", "path"), path);
    qualifiedName.put(QualifiedName.create("gtn", "lang"), "");

    try {
      return new StringBuilder("/")
              .append(portalName)
              .append(URLDecoder.decode(router.render(qualifiedName), "UTF-8"))
              .toString();

    } catch (UnsupportedEncodingException e) {
      return "";
    }
  }

  public static String buildBaseURL(SiteKey siteKey, ExoContainer container, Router router) {
    return baseURL(siteKey, container, router, null);
  }
}

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

import org.exoplatform.commons.utils.CommonsUtils;
import org.exoplatform.commons.utils.HTMLEntityEncoder;
import org.exoplatform.container.ExoContainerContext;
import org.exoplatform.portal.localization.LocaleContextInfoUtils;
import org.exoplatform.services.resources.LocaleContextInfo;
import org.exoplatform.services.resources.LocalePolicy;
import org.exoplatform.services.security.ConversationState;
import org.exoplatform.social.core.identity.model.Identity;
import org.exoplatform.social.core.identity.provider.OrganizationIdentityProvider;
import org.exoplatform.social.core.manager.IdentityManager;
import org.exoplatform.task.model.User;
import org.exoplatform.task.service.UserService;

import java.util.Locale;
import java.util.StringTokenizer;

/**
 * @author <a href="mailto:tuyennt@exoplatform.com">Tuyen Nguyen The</a>.
 */
public final class CommentUtil {

  private CommentUtil() {
  }

  public static String formatMention(String text,String lang, UserService userService) {
    if (text == null || text.isEmpty()) {
      return text;
    }
    HTMLEntityEncoder encoder = HTMLEntityEncoder.getInstance();
    StringBuilder sb = new StringBuilder();

    StringTokenizer tokenizer = new StringTokenizer(text);
    while (tokenizer.hasMoreElements()) {
      String next = (String) tokenizer.nextElement();
      if (next.length() == 0) {
        continue;
      } else if (next.charAt(0) == '@') {
        String username = next.substring(1);
        User user = userService.loadUser(username);
        if (user != null && !"guest".equals(user.getUsername())) {
          next = "<a href=\"" + CommonsUtils.getCurrentDomain() + user.getUrl() + "\">"
              + encoder.encodeHTML(user.getDisplayName());
          if(isExternal(username)){
            next += "<span class=\" externalTagClass\">" + " (" + TaskUtil.getResourceBundleLabel(new Locale(lang), "external.label.tag") + ")</span>";
          }
          next += "</a>";
        }
      } else if (next.startsWith("<p>@")) {
        String username = next.substring(4);
        User user = userService.loadUser(username);
        if (user != null && !"guest".equals(user.getUsername())) {
          next = "<p><a href=\"" + CommonsUtils.getCurrentDomain() + user.getUrl() + "\">"
              + encoder.encodeHTML(user.getDisplayName());
          if(isExternal(username)){
            next += "<span class=\" externalTagClass\">" + " (" + TaskUtil.getResourceBundleLabel(new Locale(lang), "external.label.tag") + ")</span>";
          }
          next += "</a>";
        }
      } else if (next.contains("@")) {
        String username = next.split("@")[1];
        User user = userService.loadUser(username);
        if (user != null && !"guest".equals(user.getUsername())) {
          next = next.split("@")[0] + "<a href=\"" + CommonsUtils.getCurrentDomain() + user.getUrl() + "\">"
              + encoder.encodeHTML(user.getDisplayName());
          if(isExternal(username)){
            next += "<span class=\" externalTagClass\">" + " (" + TaskUtil.getResourceBundleLabel(new Locale(lang), "external.label.tag") + ")</span>";
          }
          next += "</a>";
        }
      }
      sb.append(next);
      sb.append(' ');
    }

    return StringUtil.encodeInjectedHtmlTag(sb.toString());
  }

  /**
   * Check if is an external user.
   *
   * @param userName user name
   * @return true if is an external
   */
  public static boolean isExternal(String userName){
    IdentityManager identityManager = CommonsUtils.getService(IdentityManager.class);
    Identity identity = identityManager.getOrCreateIdentity(OrganizationIdentityProvider.NAME, userName);
    return identity.getProfile().getProperty("external") != null &&  identity.getProfile().getProperty("external").equals("true");
  }

}

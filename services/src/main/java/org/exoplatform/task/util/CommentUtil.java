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

import java.util.StringTokenizer;

import org.exoplatform.task.model.User;
import org.exoplatform.task.service.UserService;

/**
 * @author <a href="mailto:tuyennt@exoplatform.com">Tuyen Nguyen The</a>.
 */
public final class CommentUtil {

  private CommentUtil() {
  }

  public static String formatMention(String text, UserService userService) {
    if(text == null || text.isEmpty()) {
      return text;
    }
    StringBuilder sb = new StringBuilder();

    StringTokenizer tokenizer = new StringTokenizer(text);
    while(tokenizer.hasMoreElements()) {
      String next = (String)tokenizer.nextElement();
      if(next.length() == 0) {
        continue;
      } else if(next.charAt(0) == '@') {
        String username = next.substring(1);
        User user = userService.loadUser(username);
        if(user != null && !"guest".equals(user.getUsername())) {
          next = "<a href=\"/portal/intranet/profile/" + user.getUsername() + "\">"+user.getDisplayName()+"</a>";
        }
      }
      sb.append(next);
      sb.append(' ');
    }

    return sb.toString();
  }
}

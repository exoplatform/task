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

package org.exoplatform.task.utils;

import java.util.MissingResourceException;
import java.util.ResourceBundle;

/**
 * @author <a href="mailto:tuyennt@exoplatform.com">Tuyen Nguyen The</a>.
 */
public class ResourceUtil {
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
}

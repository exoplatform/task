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

import org.exoplatform.commons.utils.ListAccess;
import org.exoplatform.task.dao.jpa.JPAQueryListAccess;

/**
 * @author <a href="mailto:tuyennt@exoplatform.com">Tuyen Nguyen The</a>.
 */
public class ListUtil {
  public static <E> int getSize(ListAccess<E> list) {
    try {
      return list.getSize();
    } catch (Exception ex) {
      return 0;
    }
  }

  public static <E> E[] load(ListAccess<E> list, int start, int limit) {
    try {
      if (list instanceof JPAQueryListAccess) {
        return list.load(start, limit);
      } else {
        if (limit < 0) {
          start = 0;
          limit = list.getSize();
        }
        return list.load(start, limit);
      }
    } catch (Exception ex) {
      return (E[])(new Object[0]);
    }
  }
}

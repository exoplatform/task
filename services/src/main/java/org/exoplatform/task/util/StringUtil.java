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

import java.text.SimpleDateFormat;
import java.util.regex.Pattern;

/**
 * @author <a href="mailto:tuyennt@exoplatform.com">Tuyen Nguyen The</a>.
 */
public class StringUtil {
  public static final String  ORDERBY_DATE  = "date" ;
  public static final String  ORDERBY_TITLE  = "title" ;  
  public static final String ORDERBY_RELEVANCY = "relevancy";
  public static final String ASC = "asc";
  public static final String DESC = "desc";  
  
  public static final  Pattern FUZZY = Pattern.compile(".[~][0]([\\.][0-9])");
  
  public static String highlight(String text, String keyword, String before, String after) {
    if (text == null || text.isEmpty() || keyword == null || keyword.isEmpty()) {
      return text;
    }
    String lowerText = text.toLowerCase();
    String lowerKeyword = keyword.toLowerCase();
    StringBuilder result = new StringBuilder();

    int length = keyword.length();
    int fromIndex = 0;
    int index = lowerText.indexOf(lowerKeyword, fromIndex);
    while(index != -1) {
      result.append(text.substring(fromIndex, index));
      result.append(before);
      result.append(text.substring(index, index + length));
      result.append(after);
      fromIndex = index + length;
      index = lowerText.indexOf(lowerKeyword, fromIndex);
    }
    result.append(text.substring(fromIndex));
    return result.toString();
  }
}

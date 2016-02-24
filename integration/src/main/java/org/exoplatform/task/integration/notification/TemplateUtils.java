/**
 * Copyright (C) 2015 eXo Platform SAS.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Affero General Public License
 * as published by the Free Software Foundation; either version 3
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, see<http://www.gnu.org/licenses/>.
 **/
  
package org.exoplatform.task.integration.notification;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

public class TemplateUtils {
  public static String format(Date date, TimeZone timezone) { 
    if (date == null) {
      return null;
    }
    
    Calendar today = Calendar.getInstance(timezone);
    Calendar cal = Calendar.getInstance(timezone);
    cal.setTime(date);
    String format = "MMM dd yyyy";
    if (cal.get(Calendar.YEAR) == today.get(Calendar.YEAR)) {
      format = "MMM dd";
    }
    SimpleDateFormat df = new SimpleDateFormat(format);
    df.setTimeZone(timezone);
    return df.format(date);
  }
}

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

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.ResourceBundle;
import java.util.TimeZone;

/**
 * @author <a href="mailto:tuyennt@exoplatform.com">Tuyen Nguyen The</a>.
 */
public class DateUtil {

  public static final String DUE_DATE_FORMAT = "MMM dd";
  public static final String DUE_DATE_FORMAT_WITH_YEAR = "MMM dd, yyyy";

  public static boolean isOverdue(Calendar calendar) {
    TimeZone tz = calendar.getTimeZone();
    Calendar current = Calendar.getInstance(tz);
    current.set(Calendar.HOUR_OF_DAY, 0);
    current.set(Calendar.MINUTE, 0);
    current.set(Calendar.SECOND, 0);
    current.set(Calendar.MILLISECOND, 0);

    return (calendar.getTimeInMillis() < current.getTimeInMillis());
  }

  public static boolean isYesterday(Calendar calendar) {
    Calendar cal = newCalendarInstance(calendar.getTimeZone());
    cal.add(Calendar.DATE, -1);
    return (calendar.get(Calendar.DATE) == cal.get(Calendar.DATE)
            && calendar.get(Calendar.MONTH) == cal.get(Calendar.MONTH)
            && calendar.get(Calendar.YEAR) == cal.get(Calendar.YEAR));
  }

  public static boolean isToday(Calendar calendar) {
    TimeZone tz = calendar.getTimeZone();
    Calendar current = Calendar.getInstance(tz);
    return (calendar.get(Calendar.DATE) == current.get(Calendar.DATE)
            && calendar.get(Calendar.MONTH) == current.get(Calendar.MONTH)
            && calendar.get(Calendar.YEAR) == current.get(Calendar.YEAR));
  }

  public static boolean isTomorrow(Calendar calendar) {
    TimeZone tz = calendar.getTimeZone();
    Calendar current = Calendar.getInstance(tz);
    //. Switch current to tomorrow
    current.add(Calendar.DATE, 1);
    return (calendar.get(Calendar.DATE) == current.get(Calendar.DATE)
            && calendar.get(Calendar.MONTH) == current.get(Calendar.MONTH)
            && calendar.get(Calendar.YEAR) == current.get(Calendar.YEAR));
  }

  public static String getDueDateLabel(Calendar calendar, ResourceBundle bundle) {
    if (calendar == null) {
      return bundle.getString("label.noDueDate");
    }
    if (isOverdue(calendar)) {
      return bundle.getString("label.overdue");
    }

    if (isToday(calendar)) {
      return bundle.getString("label.today");
    }
    if (isTomorrow(calendar)) {
      return bundle.getString("label.tomorrow");
    }

    return bundle.getString("label.upcoming");
  }

  public static String formatDueDate(Calendar calendar, ResourceBundle bundle) {
    if (calendar == null) {
      return "";
    }

    /*if (isOverdue(calendar)) {
      return bundle.getString("label.overdue");
    }*/
    if (isYesterday(calendar)) {
      return bundle.getString("label.yesterday");
    }
    if (isToday(calendar)) {
      return bundle.getString("label.today");
    }
    if (isTomorrow(calendar)) {
      return bundle.getString("label.tomorrow");
    }
    Calendar current = Calendar.getInstance(calendar.getTimeZone());
    boolean isSameYear = current.get(Calendar.YEAR) == calendar.get(Calendar.YEAR);
    DateFormat df = new SimpleDateFormat(isSameYear ? DUE_DATE_FORMAT : DUE_DATE_FORMAT_WITH_YEAR);
    df.setCalendar(calendar);

    return df.format(calendar.getTime());
  }

  public static Calendar newCalendarInstance(TimeZone timeZone) {
    Calendar calendar = Calendar.getInstance(timeZone);
    return calendar;
  }
}

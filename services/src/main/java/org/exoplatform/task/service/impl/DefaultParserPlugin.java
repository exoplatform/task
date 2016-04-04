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

package org.exoplatform.task.service.impl;

import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.TimeZone;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.task.domain.Priority;
import org.exoplatform.task.service.ParserContext;
import org.exoplatform.task.service.TaskBuilder;
import org.exoplatform.task.service.TaskParserPlugin;
import org.exoplatform.task.util.DateUtil;

/**
 * @author <a href="trongtt@gmail.com">Trong Tran</a>
 * @version $Revision$
 */
public class DefaultParserPlugin implements TaskParserPlugin {

  private static final Log                  LOG          = ExoLogger.getExoLogger(DefaultParserPlugin.class);

  private static final Map<String, Integer> DAY_OF_WEEKS = new HashMap<String, Integer>();

  private static final Map<String, Integer> MONTHS       = new HashMap<String, Integer>();

  private Pattern ASSIGNEE_REGEX = Pattern.compile("(\\s)(@)([\\S]+)");

  private Pattern PRIORITY_REGEX = Pattern.compile("(\\s)(!)([a-zA-Z]+)");

  static {
    DAY_OF_WEEKS.put("monday", Calendar.MONDAY);
    DAY_OF_WEEKS.put("mon", Calendar.MONDAY);

    DAY_OF_WEEKS.put("tuesday", Calendar.TUESDAY);
    DAY_OF_WEEKS.put("tue", Calendar.TUESDAY);

    DAY_OF_WEEKS.put("wednesday", Calendar.WEDNESDAY);
    DAY_OF_WEEKS.put("wed", Calendar.WEDNESDAY);

    DAY_OF_WEEKS.put("thursday", Calendar.THURSDAY);
    DAY_OF_WEEKS.put("thu", Calendar.THURSDAY);

    DAY_OF_WEEKS.put("friday", Calendar.FRIDAY);
    DAY_OF_WEEKS.put("fri", Calendar.FRIDAY);

    DAY_OF_WEEKS.put("saturday", Calendar.SATURDAY);
    DAY_OF_WEEKS.put("sat", Calendar.SATURDAY);

    DAY_OF_WEEKS.put("sunday", Calendar.SUNDAY);
    DAY_OF_WEEKS.put("sun", Calendar.SUNDAY);

    // . Month list
    MONTHS.put("january", Calendar.JANUARY);
    MONTHS.put("jan", Calendar.JANUARY);

    MONTHS.put("january", Calendar.FEBRUARY);
    MONTHS.put("feb", Calendar.FEBRUARY);

    MONTHS.put("march", Calendar.MARCH);
    MONTHS.put("mar", Calendar.MARCH);

    MONTHS.put("april", Calendar.APRIL);
    MONTHS.put("apr", Calendar.APRIL);

    MONTHS.put("may", Calendar.MAY);

    MONTHS.put("june", Calendar.JUNE);
    MONTHS.put("jun", Calendar.JUNE);

    MONTHS.put("july", Calendar.JULY);
    MONTHS.put("jul", Calendar.JULY);

    MONTHS.put("august", Calendar.AUGUST);
    MONTHS.put("aug", Calendar.AUGUST);

    MONTHS.put("september", Calendar.SEPTEMBER);
    MONTHS.put("sep", Calendar.SEPTEMBER);

    MONTHS.put("october", Calendar.OCTOBER);
    MONTHS.put("oct", Calendar.OCTOBER);

    MONTHS.put("november", Calendar.NOVEMBER);
    MONTHS.put("nov", Calendar.NOVEMBER);

    MONTHS.put("december", Calendar.DECEMBER);
    MONTHS.put("dec", Calendar.DECEMBER);
  }

  @Override
  public String parse(String input, ParserContext context, TaskBuilder builder) {
    input = parseAssignee(input, builder);
    input = parsePriority(input, builder);
    input = parseDueDate(input, builder, context);
    return input;
  }

  //
  private String parseAssignee(String input, TaskBuilder builder) {
    Matcher m = ASSIGNEE_REGEX.matcher(input);
    while (m.find()) {
      String assignee = m.group(3);
      builder.withAssignee(assignee.trim());
    }

    String in = m.replaceAll(" ").trim();
    return in;
  }

  //
  private String parsePriority(String input, TaskBuilder builder) {
    Matcher m = PRIORITY_REGEX.matcher(input);
    while (m.find()) {
      String priority = m.group(3);
      try {
        builder.withPriority(Priority.valueOf(priority.toUpperCase()));
      } catch (IllegalArgumentException ex) {
        LOG.debug("Issue during parsing task priority: ", ex);
      }
    }

    String in = m.replaceAll(" ").trim();
    return in;
  }

  //
  private String parseDueDate(String input, TaskBuilder builder, ParserContext context) {
    StringTokenizer tokenizer = new StringTokenizer(input);
    StringBuilder retInput = new StringBuilder();
    while (tokenizer.hasMoreElements()) {
      String element = (String) tokenizer.nextElement();
      if (element.charAt(0) == '^') {
        Date dueDate = null;
        String param = element.substring(1).toLowerCase();
        if (param.startsWith("next") && tokenizer.hasMoreElements()) {
          dueDate = this.parseNextDateOf((String) tokenizer.nextElement(), context.getTimezone());
        } else {
          dueDate = this.parseDate(param, context.getTimezone());
        }
        if (dueDate != null) {
          builder.withDueDate(dueDate);
        } else {
          retInput.append(element).append(' ');
        }
      } else {
        retInput.append(element).append(' ');
      }
    }
    return retInput.toString().trim();
  }

  private Date parseNextDateOf(String token, TimeZone timezone) {
    Calendar calendar = this.getCalendar(timezone);
    if ("week".equalsIgnoreCase(token)) {
      calendar.add(Calendar.DATE, 7);
    } else if ("month".equalsIgnoreCase(token)) {
      calendar.add(Calendar.MONTH, 1);
    }
    return calendar.getTime();
  }

  private Date parseDate(String dateString, TimeZone timezone) {
    Calendar calendar = this.getCalendar(timezone);

    if (dateString.equalsIgnoreCase("today")) {
      return calendar.getTime();
    }

    if ("Tomorrow".equalsIgnoreCase(dateString)) {
      calendar.add(Calendar.DATE, 1);
      return calendar.getTime();
    }

    String key = dateString.toLowerCase();
    if (DAY_OF_WEEKS.containsKey(key)) {
      int dayOfWeek = DAY_OF_WEEKS.get(key);
      calendar.add(Calendar.DATE, 1);
      while (dayOfWeek != calendar.get(Calendar.DAY_OF_WEEK)) {
        calendar.add(Calendar.DATE, 1);
      }
      return calendar.getTime();
    }

    // Pattern dd-mon
    long timeInMillis = calendar.getTimeInMillis();
    StringTokenizer tokenizer = new StringTokenizer(dateString, "-");
    String day = (String) tokenizer.nextElement();
    try {
      calendar.set(Calendar.DATE, Integer.parseInt(day));
      // . If the day is in the past, we will set to next month
      if (timeInMillis > calendar.getTimeInMillis()) {
        calendar.add(Calendar.MONTH, 1);
      }
    } catch (NumberFormatException ex) {
      return null;
    }
    if (tokenizer.hasMoreElements()) {
      String month = ((String) tokenizer.nextElement()).toLowerCase();
      if (MONTHS.containsKey(month)) {
        calendar.set(Calendar.MONTH, MONTHS.get(month));
      }
      if (timeInMillis > calendar.getTimeInMillis()) {
        //. If the day is in the past, we will set to next year
        calendar.add(Calendar.YEAR, 1);
      }
    }

    return calendar.getTime();
  }

  private Calendar getCalendar(TimeZone timezone) {
    Calendar calendar = DateUtil.newCalendarInstance(timezone);
    calendar.set(Calendar.HOUR_OF_DAY, 23);
    calendar.set(Calendar.MINUTE, 59);
    calendar.set(Calendar.SECOND, 59);
    return calendar;
  }
}

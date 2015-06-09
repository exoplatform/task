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

import org.exoplatform.task.service.TaskBuilder;
import org.exoplatform.task.service.TaskParserPlugin;

/**
 * @author <a href="mailto:tuyennt@exoplatform.com">Tuyen Nguyen The</a>.
 */
public class TaskDueDateParserPlugin implements TaskParserPlugin {
    private static final Map<String, Integer> DAY_OF_WEEKS = new HashMap<String, Integer>();
    private static final Map<String, Integer> MONTHS = new HashMap<String, Integer>();
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

        //. Month list
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
    public String parse(String input, TaskBuilder builder) {
        StringTokenizer tokenizer = new StringTokenizer(input);
        StringBuilder retInput = new StringBuilder();
        while(tokenizer.hasMoreElements()) {
            String element = (String)tokenizer.nextElement();
            if(element.charAt(0) == '^') {
                Date dueDate = null;
                String param = element.substring(1).toLowerCase();
                if(param.startsWith("next") && tokenizer.hasMoreElements()) {
                    dueDate = this.parseNextDateOf((String)tokenizer.nextElement());
                } else {
                    dueDate = this.parseDate(param);
                }
                if(dueDate != null) {
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

    private Date parseNextDateOf(String token) {
        Calendar calendar = this.getCalendar();
        if("week".equalsIgnoreCase(token)) {
            calendar.add(Calendar.DATE, 7);
        } else if("month".equalsIgnoreCase(token)) {
            calendar.add(Calendar.MONTH, 1);
        }
        return calendar.getTime();
    }

    private Date parseDate(String dateString) {
        Calendar calendar = this.getCalendar();

        if(dateString.equalsIgnoreCase("today")) {
            return calendar.getTime();
        }

        if("Tomorrow".equalsIgnoreCase(dateString)) {
            calendar.add(Calendar.DATE, 1);
            return calendar.getTime();
        }

        String key = dateString.toLowerCase();
        if(DAY_OF_WEEKS.containsKey(key)) {
            int dayOfWeek = DAY_OF_WEEKS.get(key);
            calendar.add(Calendar.DATE, 1);
            while(dayOfWeek != calendar.get(Calendar.DAY_OF_WEEK)) {
                calendar.add(Calendar.DATE, 1);
            }
            return calendar.getTime();
        }

        //Pattern dd-mon
        StringTokenizer tokenizer = new StringTokenizer(dateString, "-");
        String day = (String)tokenizer.nextElement();
        try {
            long timeInMillis = calendar.getTimeInMillis();
            calendar.set(Calendar.DATE, Integer.parseInt(day));
            //. If the day is in the past, we will set to next month
            if(timeInMillis > calendar.getTimeInMillis()) {
                calendar.add(Calendar.MONTH, 1);
            }
        } catch (NumberFormatException ex) {
            return null;
        }
        if(tokenizer.hasMoreElements()) {
            String month = ((String) tokenizer.nextElement()).toLowerCase();
            if(MONTHS.containsKey(month)) {
                calendar.set(Calendar.MONTH, MONTHS.get(month));
            }
        }

        return calendar.getTime();
    }

    private Calendar getCalendar() {
        //TODO: how to process timezone
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, 23);
        calendar.set(Calendar.MINUTE, 59);
        calendar.set(Calendar.SECOND, 59);
        return calendar;
    }
}

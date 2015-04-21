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

package org.exoplatform.task.test;

import org.exoplatform.task.domain.Priority;
import org.exoplatform.task.domain.Task;
import org.exoplatform.task.service.TaskParser;
import org.exoplatform.task.service.impl.TaskParserImpl;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.*;

/**
 * @author <a href="mailto:tuyennt@exoplatform.com">Tuyen Nguyen The</a>.
 */
public class TestTaskParser {
  private TaskParser creator;

  @Before
  public void setup() {
    this.creator = new TaskParserImpl();
  }

  @Test
  public void testParserHighPriority() {
    Task task = creator.parse("Test task !High tomorrow");
    Assert.assertNotNull(task);
    Assert.assertEquals("Test task tomorrow", task.getTitle());
    Assert.assertEquals("Priority must be high", Priority.HIGH, task.getPriority());

    task = creator.parse("Test task !HIGH tomorrow");
    Assert.assertNotNull(task);
    Assert.assertEquals("Test task tomorrow", task.getTitle());
    Assert.assertEquals("Priority must be High", Priority.HIGH, task.getPriority());

    task = creator.parse("Test task !higH tomorrow");
    Assert.assertNotNull(task);
    Assert.assertEquals("", "Test task tomorrow", task.getTitle());
    Assert.assertEquals("Priority must be High", Priority.HIGH, task.getPriority());

    
    task = creator.parse("Test task !Low tomorrow !higH");
    Assert.assertNotNull(task);
    Assert.assertEquals("", "Test task tomorrow", task.getTitle());
    Assert.assertEquals("Priority must be High", Priority.HIGH, task.getPriority());
  }

  @Test
  public void testParserLowPriority() {
    Task task = creator.parse("Test task !Low tomorrow");
    Assert.assertNotNull(task);
    Assert.assertEquals("", "Test task tomorrow", task.getTitle());
    Assert.assertEquals("Priority must be low", Priority.LOW, task.getPriority());

    task = creator.parse("Test task !LOW tomorrow");
    Assert.assertNotNull(task);
    Assert.assertEquals("", "Test task tomorrow", task.getTitle());
    Assert.assertEquals("Priority must be low", Priority.LOW, task.getPriority());

    task = creator.parse("Test task !loW tomorrow");
    Assert.assertNotNull(task);
    Assert.assertEquals("", "Test task tomorrow", task.getTitle());
    Assert.assertEquals("Priority must be low", Priority.LOW, task.getPriority());
  }

  @Test
  public void testParserAssignee() {
    Task task = creator.parse("Test task @john tomorrow");
    Assert.assertNotNull(task);
    Assert.assertEquals("", "Test task tomorrow", task.getTitle());
    Assert.assertEquals("Assignee must be john", "john", task.getAssignee());

    task = creator.parse("Test task tomorrow @john");
    Assert.assertNotNull(task);
    Assert.assertEquals("", "Test task tomorrow", task.getTitle());
    Assert.assertEquals("Assignee must be john", "john", task.getAssignee());
  }

  @Test
  public void testParserDueDate() {
    Calendar current = Calendar.getInstance();
    Task task = creator.parse("Test task ^today tomorrow");
    Assert.assertNotNull(task);
    Assert.assertEquals("", "Test task tomorrow", task.getTitle());
    Date dueDate = task.getDueDate();
    Assert.assertNotNull(dueDate);
    Calendar dueCalendar = Calendar.getInstance();
    dueCalendar.setTime(dueDate);
    Assert.assertEquals(current.get(Calendar.DATE), dueCalendar.get(Calendar.DATE));
    Assert.assertEquals(current.get(Calendar.MONTH), dueCalendar.get(Calendar.MONTH));
    Assert.assertEquals(current.get(Calendar.YEAR), dueCalendar.get(Calendar.YEAR));
  }

  @Test
  public void testParserDueDateTomorrow() {
    Calendar tomorrow = Calendar.getInstance();
    tomorrow.add(Calendar.DATE, 1);
    Task task = creator.parse("Test task ^tomorrow need to do");
    Assert.assertNotNull(task);
    Assert.assertEquals("", "Test task need to do", task.getTitle());
    Date dueDate = task.getDueDate();
    Assert.assertNotNull(dueDate);
    Calendar dueCalendar = Calendar.getInstance();
    dueCalendar.setTime(dueDate);
    Assert.assertEquals(tomorrow.get(Calendar.DATE), dueCalendar.get(Calendar.DATE));
    Assert.assertEquals(tomorrow.get(Calendar.MONTH), dueCalendar.get(Calendar.MONTH));
    Assert.assertEquals(tomorrow.get(Calendar.YEAR), dueCalendar.get(Calendar.YEAR));
  }

  @Test
  public void testParserDueDateNextFriday() {
    Calendar nextFriday = Calendar.getInstance();
    nextFriday.add(Calendar.DATE, 1);
    while(nextFriday.get(Calendar.DAY_OF_WEEK) != Calendar.FRIDAY) {
      nextFriday.add(Calendar.DATE, 1);
    }

    Task task = creator.parse("Test task ^Friday need to do");
    Assert.assertNotNull(task);
    Assert.assertEquals("", "Test task need to do", task.getTitle());
    Date dueDate = task.getDueDate();
    Assert.assertNotNull(dueDate);
    Calendar dueCalendar = Calendar.getInstance();
    dueCalendar.setTime(dueDate);
    Assert.assertEquals(nextFriday.get(Calendar.DATE), dueCalendar.get(Calendar.DATE));
    Assert.assertEquals(nextFriday.get(Calendar.MONTH), dueCalendar.get(Calendar.MONTH));
    Assert.assertEquals(nextFriday.get(Calendar.YEAR), dueCalendar.get(Calendar.YEAR));
  }

  @Test
  public void testParserDueDateNextWeek() {
    Calendar nextFriday = Calendar.getInstance();
    nextFriday.add(Calendar.DATE, 7);

    Task task = creator.parse("Test task ^next week need to do");
    Assert.assertNotNull(task);
    Assert.assertEquals("", "Test task need to do", task.getTitle());
    Date dueDate = task.getDueDate();
    Assert.assertNotNull(dueDate);
    Calendar dueCalendar = Calendar.getInstance();
    dueCalendar.setTime(dueDate);
    Assert.assertEquals(nextFriday.get(Calendar.DATE), dueCalendar.get(Calendar.DATE));
    Assert.assertEquals(nextFriday.get(Calendar.MONTH), dueCalendar.get(Calendar.MONTH));
    Assert.assertEquals(nextFriday.get(Calendar.YEAR), dueCalendar.get(Calendar.YEAR));
  }

  @Test
  public void testParserDueDateNextMonth() {
    Calendar nextMonth = Calendar.getInstance();
    nextMonth.add(Calendar.MONTH, 1);

    Task task = creator.parse("Test task ^next month need to do");
    Assert.assertNotNull(task);
    Assert.assertEquals("", "Test task need to do", task.getTitle());
    Date dueDate = task.getDueDate();
    Assert.assertNotNull(dueDate);
    Calendar dueCalendar = Calendar.getInstance();
    dueCalendar.setTime(dueDate);
    Assert.assertEquals(nextMonth.get(Calendar.DATE), dueCalendar.get(Calendar.DATE));
    Assert.assertEquals(nextMonth.get(Calendar.MONTH), dueCalendar.get(Calendar.MONTH));
    Assert.assertEquals(nextMonth.get(Calendar.YEAR), dueCalendar.get(Calendar.YEAR));
  }

  @Test
  public void testParserDueDateAtSpecificDate() {
    Calendar calendar = Calendar.getInstance();
    calendar.add(Calendar.DATE, 2);

    Task task = creator.parse("Test task ^" + calendar.get(Calendar.DATE) + " need to do");
    Assert.assertNotNull(task);
    Assert.assertEquals("", "Test task need to do", task.getTitle());
    Date dueDate = task.getDueDate();
    Assert.assertNotNull(dueDate);
    Calendar dueCalendar = Calendar.getInstance();
    dueCalendar.setTime(dueDate);
    Assert.assertEquals(calendar.get(Calendar.DATE), dueCalendar.get(Calendar.DATE));
    Assert.assertEquals(calendar.get(Calendar.MONTH), dueCalendar.get(Calendar.MONTH));
    Assert.assertEquals(calendar.get(Calendar.YEAR), dueCalendar.get(Calendar.YEAR));
  }

  @Test
  public void testParserDueDateAtMar24() {
    Calendar nextMonth = Calendar.getInstance();
    nextMonth.set(Calendar.MONTH, Calendar.MARCH);
    nextMonth.set(Calendar.DATE, 24);

    Task task = creator.parse("Test task ^24-mar need to do");
    Assert.assertNotNull(task);
    Assert.assertEquals("", "Test task need to do", task.getTitle());
    Date dueDate = task.getDueDate();
    Assert.assertNotNull(dueDate);
    Calendar dueCalendar = Calendar.getInstance();
    dueCalendar.setTime(dueDate);
    Assert.assertEquals(nextMonth.get(Calendar.DATE), dueCalendar.get(Calendar.DATE));
    Assert.assertEquals(nextMonth.get(Calendar.MONTH), dueCalendar.get(Calendar.MONTH));
    Assert.assertEquals(nextMonth.get(Calendar.YEAR), dueCalendar.get(Calendar.YEAR));
  }

  @Test
  public void testParseTag() {
    Task task = creator.parse("Test task tomorrow #tag1 #tag2 #tag3");
    Assert.assertNotNull(task);
    Set<String> labels = task.getTags();
    Assert.assertEquals(3, labels.size());
    assertEqual(Arrays.asList("tag1", "tag2", "tag3"), labels);
  }

  private void assertEqual(Collection expected, Collection actual) {
    if(expected.size() != actual.size()) {
      Assert.fail("Size of 2 collection must be the same");
    }
    for(Object e1 : expected) {
      boolean existed = false;
      for(Object e2 : actual) {
        if(e1 == e2 || e1.equals(e2)) {
          existed = true;
          break;
        }
      }
      if(!existed) {
        Assert.fail("Collection is not the same, expect: " + expected + ", actual: " + actual);
      }
    }
  }
}

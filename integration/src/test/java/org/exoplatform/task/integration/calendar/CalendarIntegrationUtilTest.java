/* 
* Copyright (C) 2003-2019 eXo Platform SAS.
*
* This program is free software: you can redistribute it and/or modify
* it under the terms of the GNU Lesser General Public License as published by
* the Free Software Foundation, either version 3 of the License, or
* (at your option) any later version.
*
* This program is distributed in the hope that it will be useful,
* but WITHOUT ANY WARRANTY; without even the implied warranty of
* MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
* GNU Lesser General Public License for more details.
*
* You should have received a copy of the GNU Lesser General Public License
* along with this program. If not, see http://www.gnu.org/licenses/ .
*/
package org.exoplatform.task.integration.calendar;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.when;

import java.util.Date;

import org.junit.*;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import org.exoplatform.calendar.model.Event;
import org.exoplatform.services.idgenerator.IDGeneratorService;
import org.exoplatform.services.jcr.util.IdGenerator;
import org.exoplatform.task.TestUtils;
import org.exoplatform.task.domain.*;
import org.exoplatform.task.util.ProjectUtil;

@RunWith(MockitoJUnitRunner.class)
public class CalendarIntegrationUtilTest {
  @Mock
  IdGenerator                idGenerator;

  @Mock
  private IDGeneratorService idGeneratorService;

  @Before
  public void setUp() {
    new IdGenerator(idGeneratorService);
    when(idGeneratorService.generateStringID(any(String.class))).thenReturn(Long.toString(System.currentTimeMillis()));
  }

  @Test
  public void testTaskEvent() {

    Task task = TestUtils.getDefaultTask();
    task.setStartDate(new Date());
    task.setEndDate(new Date());
    Status status = TestUtils.getDefaultStatus();
    Project project = TestUtils.getDefaultProject();
    Event event = new Event();
    CalendarIntegrationUtil.buildEvent(event, task);

    Assert.assertEquals(String.valueOf(ProjectUtil.TODO_PROJECT_ID), event.getCalendarId());

    status.setProject(project);
    task.setStatus(status);
    CalendarIntegrationUtil.buildEvent(event, task);
    Assert.assertEquals(task.getStatus().getProject().getId(), Long.parseLong(event.getCalendarId()));
    Assert.assertEquals(task.getTitle(), event.getSummary());
    Assert.assertEquals(task.getDescription(), event.getDescription());
    status.setProject(project);
  }

}

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

package org.exoplatform.task;

import org.exoplatform.task.util.DateUtil;
import org.junit.Assert;
import org.junit.Test;

import java.net.URL;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;

public class TestDateUtil {

  @Test
  public void testFormatDueDate() {
    ResourceBundle rb_en = ResourceBundle.getBundle("locale.portlet.taskManagement", Locale.ENGLISH);
    ResourceBundle rb_fr = ResourceBundle.getBundle("locale.portlet.taskManagement", Locale.FRENCH);

    Calendar now = Calendar.getInstance();

    String enResult = DateUtil.formatDueDate(now,rb_en);
    String frResult =DateUtil.formatDueDate(now,rb_fr);

    Assert.assertEquals(enResult, rb_en.getString("label.today"));
    Assert.assertEquals(frResult, rb_fr.getString("label.today"));

    Calendar yesterday = (Calendar)now.clone();
    yesterday.add(Calendar.DATE,-1);
    enResult = DateUtil.formatDueDate(yesterday,rb_en);
    frResult =DateUtil.formatDueDate(yesterday,rb_fr);

    Assert.assertEquals(enResult, rb_en.getString("label.yesterday"));
    Assert.assertEquals(frResult, rb_fr.getString("label.yesterday"));

    Calendar tomorrow = (Calendar)now.clone();
    tomorrow.add(Calendar.DATE,1);
    enResult = DateUtil.formatDueDate(tomorrow,rb_en);
    frResult =DateUtil.formatDueDate(tomorrow,rb_fr);

    Assert.assertEquals(enResult, rb_en.getString("label.tomorrow"));
    Assert.assertEquals(frResult, rb_fr.getString("label.tomorrow"));

    //use feb for testing language (jan has no diff between french and english)
    Calendar firstFebLastYear = (Calendar)now.clone();
    firstFebLastYear.set(Calendar.DATE,1);
    firstFebLastYear.set(Calendar.MONTH,Calendar.FEBRUARY);
    firstFebLastYear.add(Calendar.YEAR,-1);

    enResult = DateUtil.formatDueDate(firstFebLastYear,rb_en);
    frResult =DateUtil.formatDueDate(firstFebLastYear,rb_fr);

    DateFormat dfEn = DateFormat.getDateInstance(DateFormat.MEDIUM, rb_en.getLocale());
    DateFormat dfFr = DateFormat.getDateInstance(DateFormat.MEDIUM, rb_fr.getLocale());

    Assert.assertEquals(enResult, dfEn.format(firstFebLastYear.getTime()));
    Assert.assertEquals(frResult, dfFr.format(firstFebLastYear.getTime()));

  }

}
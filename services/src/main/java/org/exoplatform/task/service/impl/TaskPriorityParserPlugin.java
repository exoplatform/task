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

import org.exoplatform.task.domain.Priority;
import org.exoplatform.task.service.TaskBuilder;
import org.exoplatform.task.service.TaskParserPlugin;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author <a href="mailto:tuyennt@exoplatform.com">Tuyen Nguyen The</a>.
 */
public class TaskPriorityParserPlugin implements TaskParserPlugin {
    @Override
    public String parse(String input, TaskBuilder builder) {
        Pattern p = Pattern.compile("(\\s)(!)([a-zA-Z]+)");
        Matcher m = p.matcher(input);
        while(m.find()) {
            String priority = m.group(3);
            try {
                builder.withPriority(Priority.valueOf(priority.toUpperCase()));
            } catch (IllegalArgumentException ex) {
                ex.printStackTrace();
            }
        }

        String in = input.replaceAll("\\s![a-zA-Z]+\\s*", " ").trim();
        return in;
    }
}

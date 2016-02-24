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

import java.util.ServiceLoader;

import org.exoplatform.task.domain.Task;
import org.exoplatform.task.service.ParserContext;
import org.exoplatform.task.service.TaskBuilder;
import org.exoplatform.task.service.TaskParser;
import org.exoplatform.task.service.TaskParserPlugin;

/**
 * @author <a href="mailto:tuyennt@exoplatform.com">Tuyen Nguyen The</a>.
 */
public class TaskParserImpl implements TaskParser {
  ServiceLoader<TaskParserPlugin> parsers = ServiceLoader.load(TaskParserPlugin.class,
                                                               this.getClass().getClassLoader());

  @Override
  public Task parse(String input, ParserContext context) {
    if(input == null) {
      return null;
    }

    TaskBuilder builder = new TaskBuilder();
    String in = input;
    for(TaskParserPlugin parser : parsers) {
      in = parser.parse(in, context, builder);
    }
    builder.withTitle(in);

    return builder.build();
  }
}

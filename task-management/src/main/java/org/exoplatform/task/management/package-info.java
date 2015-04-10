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

@Application(defaultController = TaskManagement.class)
@Portlet
@Bindings({
    @Binding(value = TaskParser.class, implementation = TaskParserImpl.class),
    @Binding(value = TaskService.class, implementation = TaskServiceJPAImpl.class),
    @Binding(value = TaskDAO.class, implementation = TaskDAOImpl.class),
    @Binding(value = ProjectDAO.class, implementation = ProjectDAOImpl.class),
    @Binding(value = StatusDAO.class, implementation = StatusDAOImpl.class)

})
@Scripts({
    @Script(id = "jquery", value = "libs/jquery-1.11.2.js"),
    @Script(id = "boostrap-tooltip", value = "libs/xeditable/js/bootstrap-tooltip.js", depends = {"jquery"}),
    @Script(id = "boostrap-popover", value = "libs/xeditable/js/bootstrap-popover.js", depends = {"jquery", "boostrap-tooltip"}),
    @Script(id = "boostrap-datepicker", value = "libs/xeditable/js/bootstrap-datepicker.js", depends = {"jquery"}),
    @Script(id = "edit-inline-js", value = "libs/xeditable/js/bootstrap-editable.js", depends = {"jquery", "boostrap-popover", "boostrap-datepicker"}),
    @Script(id = "task-managemen-js", value = "javascripts/task-management.js", depends = {"edit-inline-js"})
})
@Stylesheets({
    @Stylesheet(id = "style.css", value = "styles/style.css"),
    @Stylesheet(id = "edit-inline-css", value = "libs/xeditable/css/bootstrap-editable.css", depends = {})
})
@Assets({"*"})
package org.exoplatform.task.management;

import juzu.Application;
import juzu.plugin.asset.*;
import juzu.plugin.binding.Binding;
import juzu.plugin.binding.Bindings;
import juzu.plugin.portlet.Portlet;
import org.exoplatform.task.dao.ProjectDAO;
import org.exoplatform.task.dao.StatusDAO;
import org.exoplatform.task.dao.TaskDAO;
import org.exoplatform.task.dao.jpa.ProjectDAOImpl;
import org.exoplatform.task.dao.jpa.StatusDAOImpl;
import org.exoplatform.task.dao.jpa.TaskDAOImpl;
import org.exoplatform.task.service.TaskParser;
import org.exoplatform.task.service.TaskService;
import org.exoplatform.task.service.impl.TaskParserImpl;
import org.exoplatform.task.service.jpa.TaskServiceJPAImpl;

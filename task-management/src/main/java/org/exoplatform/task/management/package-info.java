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
    @Binding(value = TaskParser.class),
    @Binding(value = TaskService.class),
    @Binding(value = OrganizationService.class),
    @Binding(IdentityManager.class),
    @Binding(value = UserService.class, implementation = UserServiceImpl.class)
})
@WebJars({
    @WebJar("x-editable-bootstrap"),
    @WebJar("bootstrap-datepicker"),
    @WebJar("select2")
})
@Scripts({
        @Script(id = "task-management-js", value = "javascripts/task-management.js")
})
@Stylesheets({
    @Stylesheet(id = "style.css", value = "styles/style.css"),
    @Stylesheet(id = "bootstrap-datepicker.css", value = "bootstrap-datepicker/1.4.0/css/bootstrap-datepicker.css"),
    @Stylesheet(id = "select2-css", value = "select2/3.5.2/select2.css"),
    @Stylesheet(id = "edit-inline-css", value = "x-editable-bootstrap/1.4.6/css/bootstrap-editable.css")
})
@Assets({"*"})
package org.exoplatform.task.management;

import juzu.Application;
import juzu.plugin.asset.*;
import juzu.plugin.portlet.Portlet;
import juzu.plugin.binding.Bindings;
import juzu.plugin.binding.Binding;
import juzu.plugin.webjars.WebJar;
import juzu.plugin.webjars.WebJars;
import org.exoplatform.services.organization.OrganizationService;
import org.exoplatform.social.core.manager.IdentityManager;
import org.exoplatform.task.management.controller.TaskManagement;
import org.exoplatform.task.service.TaskParser;
import org.exoplatform.task.service.TaskService;
import org.exoplatform.task.service.UserService;
import org.exoplatform.task.service.impl.UserServiceImpl;
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
    @Binding(value = OrganizationService.class)
})
@WebJars({
    @WebJar("jquery"),
    @WebJar("x-editable-bootstrap"),
    @WebJar("bootstrap-datepicker"),
    @WebJar("select2")
})
@Scripts({
    @Script(id = "jquery", value = "jquery/1.11.2/jquery.js"),
    @Script(id = "bootstrap-tooltip", value = "javascripts/bootstrap/bootstrap-tooltip.js",
        depends = {"jquery"}),
    @Script(id = "bootstrap-popover", value = "javascripts/bootstrap/bootstrap-popover.js",
        depends = {"jquery", "bootstrap-tooltip"}),
    @Script(id = "bootstrap-datepicker", value = "bootstrap-datepicker/1.4.0/js/bootstrap-datepicker.js",
        depends = {"jquery"}),
    @Script(id = "select2", value = "select2/3.5.2/select2.js", depends = {"jquery"}),
    @Script(id = "edit-inline-js", value = "x-editable-bootstrap/1.4.6/js/bootstrap-editable.js",
        depends = {"jquery", "bootstrap-popover", "bootstrap-datepicker", "select2"}),
    @Script(id = "task-management-js", value = "javascripts/task-management.js",
        depends = {"edit-inline-js"})
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
import juzu.plugin.asset.Assets;
import juzu.plugin.asset.Script;
import juzu.plugin.asset.Scripts;
import juzu.plugin.asset.Stylesheet;
import juzu.plugin.asset.Stylesheets;
import juzu.plugin.binding.Binding;
import juzu.plugin.binding.Bindings;
import juzu.plugin.portlet.Portlet;
import juzu.plugin.webjars.WebJar;
import juzu.plugin.webjars.WebJars;

import org.exoplatform.task.service.TaskParser;
import org.exoplatform.task.service.TaskService;
import org.exoplatform.services.organization.OrganizationService;


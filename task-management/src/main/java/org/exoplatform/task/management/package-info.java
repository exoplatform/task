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
    @Binding(value = NavigationState.class),
    @Binding(value = TaskParser.class),
    @Binding(value = DAOHandler.class),
    @Binding(value = TaskService.class),
    @Binding(value = ProjectService.class),
    @Binding(value = StatusService.class),
    @Binding(value = OrganizationService.class),
    @Binding(value = UserService.class),
    @Binding(value = SettingService.class),
    @Binding(value = ViewStateService.class)
})
@WebJars({
    @WebJar("x-editable-bootstrap"),
    @WebJar("bootstrap-datepicker"),
    @WebJar("select2"),
    @WebJar("jquery-ui")
})
@Scripts({
    @Script(id = "taskManagementCommon", value = "javascripts/taskManagement.js"),
    @Script(id = "uicalendar", value = "javascripts/uicalendar.js"),
    @Script(id = "x-editable-selectize-field", value = "javascripts/x-editable-selectize.js"),
    @Script(id = "x-editable-customfield-js", value = "javascripts/x-editable-parentProject.js"),
    @Script(id = "x-editable-calendar", value = "javascripts/x-editable-calendar.js", depends = {"uicalendar"}),
    @Script(id = "x-editable-ckeditor", value = "javascripts/x-editable-ckeditor.js"),
    @Script(id = "task-edit-inline", value = "javascripts/editinline.js",
            depends = {"taskManagementCommon", "filter_js", "x-editable-selectize-field","x-editable-customfield-js", "x-editable-calendar", "x-editable-ckeditor"}),
    @Script(id = "label_js", value = "javascripts/label.js", depends = {"taskManagementCommon"}),
    @Script(id = "filter_js", value = "javascripts/filter.js"),
    @Script(id = "project-menu-js", value = "javascripts/project-menu.js", depends = {"taskManagementCommon", "task-edit-inline"}),
    @Script(id = "taskBoardView", value = "javascripts/taskBoardView.js", depends = {"taskManagementCommon"}),
    @Script(id = "taskListView", value = "javascripts/taskListView.js", depends = {"taskManagementCommon"}),
    @Script(id = "taskCenterView", value = "javascripts/taskCenterView.js", depends = {"taskManagementCommon"}),
    @Script(id = "taskDetailView", value = "javascripts/taskDetailView.js", depends = {"taskManagementCommon", "taskCenterView"}),

    @Script(id = "app-js", value = "javascripts/app.js",
            depends = {"taskManagementCommon", "project-menu-js", "filter_js", "task-edit-inline", "taskListView", "taskCenterView"})
})
@Less({
    @Stylesheet(id = "style-less", value = "less/style.less")
})
@Stylesheets({
    @Stylesheet(id = "style.css", value = "less/style.css"),
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
import juzu.plugin.less4j.Less;
import juzu.plugin.portlet.Portlet;
import juzu.plugin.webjars.WebJar;
import juzu.plugin.webjars.WebJars;

import org.exoplatform.commons.api.settings.SettingService;
import org.exoplatform.services.organization.OrganizationService;
import org.exoplatform.task.dao.DAOHandler;
import org.exoplatform.task.management.controller.NavigationState;
import org.exoplatform.task.management.controller.TaskManagement;
import org.exoplatform.task.management.service.ViewStateService;
import org.exoplatform.task.service.ProjectService;
import org.exoplatform.task.service.StatusService;
import org.exoplatform.task.service.TaskParser;
import org.exoplatform.task.service.TaskService;
import org.exoplatform.task.service.UserService;
/**
 * Copyright (C) 2015 eXo Platform SAS.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Affero General Public License
 * as published by the Free Software Foundation; either version 3
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, see<http://www.gnu.org/licenses/>.
 **/

package org.exoplatform.task.integration;

import java.util.Date;

import org.exoplatform.commons.utils.CommonsUtils;
import org.exoplatform.services.listener.Event;
import org.exoplatform.services.listener.Listener;
import org.exoplatform.task.domain.Project;
import org.exoplatform.task.dto.ProjectDto;
import org.exoplatform.task.service.ProjectService;
import org.exoplatform.task.storage.ProjectStorage;

public class ProjectModifiedListener extends Listener<ProjectService, Project> {

  @Override
  public void onEvent(Event<ProjectService, Project> event) throws Exception {
    ProjectStorage storage = CommonsUtils.getService(ProjectStorage.class);
    ProjectDto data = storage.projectToDto(event.getData());
    data.setLastModifiedDate(System.currentTimeMillis());
    storage.updateProject(data);
  }

}

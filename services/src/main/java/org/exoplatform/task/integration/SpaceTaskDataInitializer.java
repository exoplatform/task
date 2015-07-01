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

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.social.core.space.SpaceListenerPlugin;
import org.exoplatform.social.core.space.model.Space;
import org.exoplatform.social.core.space.spi.SpaceLifeCycleEvent;
import org.exoplatform.task.exception.ProjectNotFoundException;
import org.exoplatform.task.service.DAOHandler;
import org.exoplatform.task.service.ProjectService;
import org.exoplatform.task.service.StatusService;
import org.exoplatform.task.service.impl.ProjectServiceImpl;
import org.exoplatform.task.service.impl.StatusServiceImpl;
import org.exoplatform.task.utils.UserUtils;

public class SpaceTaskDataInitializer extends SpaceListenerPlugin {
  
  private static final Log log = ExoLogger.getExoLogger(SpaceTaskDataInitializer.class);
  
  private ProjectService projectService;

  public SpaceTaskDataInitializer(DAOHandler daoHandler) {
    //workaround for now, util the service is declared in configuration.xml
    StatusService statusService = new StatusServiceImpl(daoHandler);
    projectService = new ProjectServiceImpl(statusService, null, daoHandler);
  }

  @Override
  public void spaceAccessEdited(SpaceLifeCycleEvent event) {
  }
  
  @Override
  public void spaceCreated(SpaceLifeCycleEvent event) {
    Space space = event.getSpace();
    String space_group_id = space.getGroupId();
  
    List<String> memberships = UserUtils.getSpaceMemberships(space_group_id);
    Set<String> managers = new HashSet<String>(Arrays.asList(memberships.get(0)));
    Set<String> participators = new HashSet<String>(Arrays.asList(memberships.get(1)));
    //
    try {
      projectService.createDefaultStatusProjectWithAttributes(0L, space.getPrettyName(), "", managers, participators);
    } catch (ProjectNotFoundException e) {
      log.error(e);
    }
  }

  @Override
  public void spaceRemoved(SpaceLifeCycleEvent event) {    
  }

  @Override
  public void applicationActivated(SpaceLifeCycleEvent event) {    
  }

  @Override
  public void applicationAdded(SpaceLifeCycleEvent event) {    
  }

  @Override
  public void applicationDeactivated(SpaceLifeCycleEvent event) {
  }

  @Override
  public void applicationRemoved(SpaceLifeCycleEvent event) {
  }

  @Override
  public void grantedLead(SpaceLifeCycleEvent event) {
  }

  @Override
  public void joined(SpaceLifeCycleEvent event) {
  }

  @Override
  public void left(SpaceLifeCycleEvent event) {
  }

  @Override
  public void revokedLead(SpaceLifeCycleEvent event) {
  }

  @Override
  public void spaceRenamed(SpaceLifeCycleEvent event) {
  }

  @Override
  public void spaceDescriptionEdited(SpaceLifeCycleEvent event) {
  }

  @Override
  public void spaceAvatarEdited(SpaceLifeCycleEvent event) {
  }

  @Override
  public void addInvitedUser(SpaceLifeCycleEvent event) {
  }

  @Override
  public void addPendingUser(SpaceLifeCycleEvent event) {
  }
  
}

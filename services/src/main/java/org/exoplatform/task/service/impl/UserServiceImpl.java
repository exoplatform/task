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

import javax.inject.Inject;
import javax.inject.Singleton;

import org.exoplatform.calendar.service.CalendarService;
import org.exoplatform.calendar.service.CalendarSetting;
import org.exoplatform.commons.api.persistence.ExoTransactional;
import org.exoplatform.commons.utils.ListAccess;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.services.organization.OrganizationService;
import org.exoplatform.services.security.Identity;
import org.exoplatform.social.core.identity.model.Profile;
import org.exoplatform.social.core.identity.provider.OrganizationIdentityProvider;
import org.exoplatform.social.core.manager.IdentityManager;
import org.exoplatform.social.core.profile.ProfileFilter;
import org.exoplatform.social.core.service.LinkProvider;
import org.exoplatform.task.dao.DAOHandler;
import org.exoplatform.task.domain.Project;
import org.exoplatform.task.domain.UserSetting;
import org.exoplatform.task.exception.EntityNotFoundException;
import org.exoplatform.task.exception.NotAllowedOperationOnEntityException;
import org.exoplatform.task.model.User;
import org.exoplatform.task.service.UserService;

import java.util.ArrayList;
import java.util.TimeZone;

/**
 * @author <a href="mailto:tuyennt@exoplatform.com">Tuyen Nguyen The</a>.
 */
@Singleton
public class UserServiceImpl implements UserService {
  private static final User GUEST = new User("guest", null, "", "", "Guest", LinkProvider.PROFILE_DEFAULT_AVATAR_URL, "#");
  private static final User NULL = new User(null, null, "", "", "Guest", LinkProvider.PROFILE_DEFAULT_AVATAR_URL, "#");

  private static final Log LOG = ExoLogger.getExoLogger(UserServiceImpl.class);

  @Inject
  private OrganizationService orgService;

  @Inject
  private IdentityManager identityManager;

  @Inject
  private DAOHandler daoHandler;
  
  private CalendarService calService;

  public UserServiceImpl(OrganizationService orgService, CalendarService calService, IdentityManager idMgr, DAOHandler handler) {
    this.orgService = orgService;
    this.identityManager = idMgr;
    this.daoHandler = handler;
    this.calService = calService;
  }

  @Override
  public User loadUser(String username) {
    if (username == null || username.isEmpty()) {
      return GUEST;
    }
    org.exoplatform.social.core.identity.model.Identity identity = identityManager.getOrCreateIdentity(OrganizationIdentityProvider.NAME, username, true);

    if (identity == null) {
      return GUEST;
    } else {
      return convertToUser(identity);
    }
  }

  @Override
  public ListAccess<User> findUserByName(String keyword) {
    ProfileFilter filter = new ProfileFilter();
    filter.setName(keyword);
    filter.setCompany("");
    filter.setPosition("");
    filter.setSkills("");
    filter.setExcludedIdentityList(new ArrayList<org.exoplatform.social.core.identity.model.Identity>());

    final ListAccess<org.exoplatform.social.core.identity.model.Identity> list = identityManager.getIdentitiesByProfileFilter(OrganizationIdentityProvider.NAME, filter, true);
    return new ListAccess<User>() {
      @Override
      public User[] load(int index, int length) throws Exception, IllegalArgumentException {
        org.exoplatform.social.core.identity.model.Identity[] identities = list.load(index, length);
        User[] users = new User[identities.length];

        for (int i = 0; i < identities.length; i++) {
          org.exoplatform.social.core.identity.model.Identity id = identities[i];
          users[i] = convertToUser(id);
        }

        return users;
      }

      @Override
      public int getSize() throws Exception {
        return list.getSize();
      }
    };
  }

  private User convertToUser(org.exoplatform.social.core.identity.model.Identity identity) {
    Profile profile = identity.getProfile();
    String username = identity.getRemoteId();
    String email = profile.getEmail();
    String firstName = (String)profile.getProperty(Profile.FIRST_NAME);
    String lastName = (String)profile.getProperty(Profile.LAST_NAME);
    String displayName = profile.getFullName();
    String avatar = profile.getAvatarUrl();
    if (avatar == null) {
      avatar = LinkProvider.PROFILE_DEFAULT_AVATAR_URL;
    }
    String url = profile.getUrl();

    User u = new User(username, email, firstName, lastName, displayName, avatar, url);
    u.setDeleted(identity.isDeleted());
    u.setEnable(identity.isEnable());

    return u;
  }

  @Override
  @ExoTransactional
  public UserSetting getUserSetting(String username) {
    return daoHandler.getUserSettingHandler().getOrCreate(username);
  }

  @Override
  @ExoTransactional
  public void hideProject(Identity identity, Long projectId, boolean hide) throws EntityNotFoundException, NotAllowedOperationOnEntityException {
    Project project = daoHandler.getProjectHandler().find(projectId);
    if (project == null) {
      throw new EntityNotFoundException(projectId, Project.class);
    }

    if (!project.canView(identity)) {
      throw new NotAllowedOperationOnEntityException(projectId, Project.class, "hide");
    }

    UserSetting setting = daoHandler.getUserSettingHandler().getOrCreate(identity.getUserId());
    if (hide) {
      setting.getHiddenProjects().add(project);
    } else {
      setting.getHiddenProjects().remove(project);
    }

    daoHandler.getUserSettingHandler().update(setting);
  }

  @Override
  @ExoTransactional
  public void showHiddenProject(String username, boolean show) {
    UserSetting setting = daoHandler.getUserSettingHandler().getOrCreate(username);
    setting.setShowHiddenProject(show);
    daoHandler.getUserSettingHandler().update(setting);
  }
  
  @Override
  @ExoTransactional
  public void showHiddenLabel(String username, boolean show) {
    UserSetting setting = daoHandler.getUserSettingHandler().getOrCreate(username);
    setting.setShowHiddenLabel(show);
    daoHandler.getUserSettingHandler().update(setting);
  }

  @Override
  public TimeZone getUserTimezone(String username) {
    try {
      CalendarSetting setting = calService.getCalendarSetting(username);
      return TimeZone.getTimeZone(setting.getTimeZone());
    } catch (Exception e) {
      LOG.error("Can't retrieve timezone", e);
    }
    return null;
  }
}

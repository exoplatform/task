/* 
* Copyright (C) 2003-2015 eXo Platform SAS.
*
* This program is free software: you can redistribute it and/or modify
* it under the terms of the GNU Lesser General Public License as published by
* the Free Software Foundation, either version 3 of the License, or
* (at your option) any later version.
*
* This program is distributed in the hope that it will be useful,
* but WITHOUT ANY WARRANTY; without even the implied warranty of
* MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
* GNU Lesser General Public License for more details.
*
* You should have received a copy of the GNU Lesser General Public License
* along with this program. If not, see http://www.gnu.org/licenses/ .
*/
package org.exoplatform.task.service.jpa;

import javax.inject.Singleton;

import org.exoplatform.commons.persistence.impl.EntityManagerService;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.task.dao.jpa.CommentDAOImpl;
import org.exoplatform.task.dao.jpa.ProjectDAOImpl;
import org.exoplatform.task.dao.jpa.StatusDAOImpl;
import org.exoplatform.task.dao.jpa.TaskDAOImpl;
import org.exoplatform.task.dao.jpa.UserSettingDAO;
import org.exoplatform.task.service.DAOHandler;
import org.exoplatform.task.service.impl.AbstractDAOHandler;

/**
 * Created by The eXo Platform SAS
 * Author : Thibault Clement
 * tclement@exoplatform.com
 * 4/8/15
 */
@Singleton
public class DAOHandlerJPAImpl extends AbstractDAOHandler implements DAOHandler {

  private static final Log LOG = ExoLogger.getLogger("DAOHandlerJPAImpl");

  public DAOHandlerJPAImpl(EntityManagerService entityService) {
    LOG.info("DAOHandlerJPAImpl is creating...");
    pHandler = new ProjectDAOImpl(entityService);
    tHandler = new TaskDAOImpl(entityService);
    cHandler = new CommentDAOImpl(entityService);
    sHandler = new StatusDAOImpl(entityService);
    uHandler = new UserSettingDAO(entityService);
    LOG.info("DAOHandlerJPAImpl is created");
  }
}


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
package org.exoplatform.task.dao.jpa;

import javax.inject.Singleton;

import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.task.dao.DAOHandler;
import org.exoplatform.task.domain.Comment;
import org.exoplatform.task.domain.Project;
import org.exoplatform.task.domain.Status;
import org.exoplatform.task.domain.Task;
import org.exoplatform.task.domain.TaskLog;
import org.exoplatform.task.domain.UserSetting;
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

  public DAOHandlerJPAImpl() {
    LOG.info("DAOHandlerJPAImpl is creating...");
    pHandler = new ProjectDAOImpl();
    tHandler = new TaskDAOImpl();
    cHandler = new CommentDAOImpl();
    taskLogHandler = new TaskLogDAOImpl();
    sHandler = new StatusDAOImpl();
    uHandler = new UserSettingDAO();
    lHandler = new LabelDAOImpl();
    LOG.info("DAOHandlerJPAImpl is created");
  }
  static <E> E clone(E e) {
    if (e == null) return null;
    if (e instanceof Task) {
      return (E)((Task)e).clone();
    } else if (e instanceof Status) {
      return (E)((Status)e).clone();
    } else if (e instanceof Project) {
      return (E)((Project)e).clone(false);
    } else if (e instanceof Comment) {
      return (E)((Comment)e).clone();
    } else if (e instanceof TaskLog) {
      return (E)((TaskLog)e).clone();
    } else if (e instanceof UserSetting) {
      return (E)((UserSetting)e).clone();
    }

    return e;
  }
}


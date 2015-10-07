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
package org.exoplatform.task.service.impl;

import org.exoplatform.task.dao.CommentHandler;
import org.exoplatform.task.dao.LabelHandler;
import org.exoplatform.task.dao.DAOHandler;
import org.exoplatform.task.dao.ProjectHandler;
import org.exoplatform.task.dao.StatusHandler;
import org.exoplatform.task.dao.TaskHandler;
import org.exoplatform.task.dao.TaskLogHandler;
import org.exoplatform.task.dao.UserSettingHandler;

/**
 * @author <a href="trongtt@gmail.com">Trong Tran</a>
 * @version $Revision$
 */
abstract public class AbstractDAOHandler implements DAOHandler {

  protected ProjectHandler pHandler;

  protected TaskHandler tHandler;
  
  protected CommentHandler cHandler;

  protected TaskLogHandler taskLogHandler;

  protected StatusHandler sHandler;

  protected UserSettingHandler uHandler;

  protected LabelHandler lHandler;
  
  public ProjectHandler getProjectHandler() {
    return pHandler;
  }

  public TaskHandler getTaskHandler() {
    return tHandler;
  }
  
  public CommentHandler getCommentHandler() {
    return cHandler;
  }
  
  public StatusHandler getStatusHandler() {
    return sHandler;
  }

  @Override
  public UserSettingHandler getUserSettingHandler() {
    return uHandler;
  }

  @Override
  public LabelHandler getLabelHandler() {
    return lHandler;
  }

  @Override
  public TaskLogHandler getTaskLogHandler() {
    return taskLogHandler;
  }
}


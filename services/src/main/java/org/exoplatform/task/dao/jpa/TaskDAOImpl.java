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

import org.exoplatform.task.dao.TaskHandler;
import org.exoplatform.task.domain.Task;
import org.exoplatform.task.service.jpa.TaskServiceJPAImpl;

import java.util.List;

/**
 * Created by The eXo Platform SAS
 * Author : Thibault Clement
 * tclement@exoplatform.com
 * 4/8/15
 */
public class TaskDAOImpl extends GenericDAOImpl<Task, Long> implements TaskHandler {

  public TaskDAOImpl(TaskServiceJPAImpl taskServiceJPAImpl) {
    super(taskServiceJPAImpl);
  }

  @Override
  public List<Task> findByProject(Long projectId) {
    return null;
  }

  @Override
  public List<Task> findByUser(String user) {
    return null;
  }

  @Override
  public List<Task> findByTag(String tag) {
    return null;
  }

  @Override
  public List<Task> findByTags(List<String> tags) {
    return null;
  }
}


/* 
* Copyright (C) 2003-2015 eXo Platform SAS.
*
* This program is free software: you can redistribute it and/or modify
* it under the terms of the GNU Affero General Public License as published by
* the Free Software Foundation, either version 3 of the License, or
* (at your option) any later version.
*
* This program is distributed in the hope that it will be useful,
* but WITHOUT ANY WARRANTY; without even the implied warranty of
* MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
* GNU Affero General Public License for more details.
*
* You should have received a copy of the GNU Affero General Public License
* along with this program. If not, see http://www.gnu.org/licenses/ .
*/
package org.exoplatform.task.dao;

import org.exoplatform.commons.api.persistence.GenericDAO;
import org.exoplatform.commons.utils.ListAccess;
import org.exoplatform.task.domain.Label;

import java.util.List;

public interface LabelHandler extends GenericDAO<Label, Long> {
  /**
   * @param username user name
   * @return List of labels
   */
  ListAccess<Label> findLabelsByUser(String username);

  List<Label> findLabelsByUserAndProject(String username, long projectId);

  ListAccess<Label> findLabelsByProject(long projectId);

  ListAccess<Label> findLabelsByTask(long taskId, long projectId);

  List<Label> findOldLabels();

  ListAccess<Label> findLabels(LabelQuery query);
}


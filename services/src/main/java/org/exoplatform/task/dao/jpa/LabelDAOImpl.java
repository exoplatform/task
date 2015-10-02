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

import java.util.List;

import javax.persistence.TypedQuery;

import org.exoplatform.task.dao.LabelHandler;
import org.exoplatform.task.domain.Label;

public class LabelDAOImpl extends CommonJPADAO<Label, Long> implements LabelHandler {

  @Override
  public List<Label> findLabelsByUser(String username) {
    TypedQuery<Label> query = getEntityManager().createNamedQuery("Label.findLabelByUser", Label.class);
    query.setParameter("username", username);

    return query.getResultList();
  }

}


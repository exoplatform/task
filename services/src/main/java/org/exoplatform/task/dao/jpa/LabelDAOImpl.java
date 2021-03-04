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

import javax.persistence.Query;
import javax.persistence.TypedQuery;

import java.util.List;

import org.exoplatform.commons.api.persistence.ExoTransactional;
import org.exoplatform.commons.utils.ListAccess;
import org.exoplatform.task.dao.LabelHandler;
import org.exoplatform.task.dao.LabelQuery;
import org.exoplatform.task.domain.Label;

public class LabelDAOImpl extends CommonJPADAO<Label, Long> implements LabelHandler {

  @Override
  @ExoTransactional
  public void delete(Label entity) {
    Query query = getEntityManager().createNamedQuery("LabelTaskMapping.removeLabelTaskMapping");
    query.setParameter("labelId", entity.getId());
    query.executeUpdate();
    List<Label> children = entity.getChildren();
    if (children != null) {
      for (Label child : children) {
        delete(child);
      }
    }
    super.delete(entity);
  }

  @Override
  public void deleteAll(List<Label> entities) {
    for (Label lb : entities) {
      delete(lb);
    }
  }

  @Override
  public void deleteAll() {
    deleteAll(findAll());
  }

  @Override
  public ListAccess<Label> findLabelsByUser(String username) {
    LabelQuery query = new LabelQuery();
    query.setUserName(username);
    return findLabels(query);
  }

  @Override
  public ListAccess<Label> findLabelsByProject(long projectId) {
    TypedQuery<Label> query = getEntityManager().createNamedQuery("Label.findLabelsByProject", Label.class);
    query.setParameter("projectId", projectId);

    TypedQuery<Long> count = getEntityManager().createNamedQuery("Label.findLabelsByProjectCount", Long.class);
    count.setParameter("projectId", projectId);
    return new JPAQueryListAccess<Label>(Label.class, count, query);
  }

  @Override
  public ListAccess<Label> findLabelsByTask(long taskId, long projectId) {
    TypedQuery<Label> query = getEntityManager().createNamedQuery("Label.findLabelsByTask", Label.class);
    query.setParameter("projectId", projectId);
    query.setParameter("taskid", taskId);

    TypedQuery<Long> count = getEntityManager().createNamedQuery("Label.findLabelsByTaskCount", Long.class);
    count.setParameter("projectId", projectId);
    count.setParameter("taskid", taskId);
    return new JPAQueryListAccess<Label>(Label.class, count, query);
  }

  @Override
  public ListAccess<Label> findLabels(LabelQuery query) {
    return findEntities(query, Label.class);
  }
  
}


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

import java.io.Serializable;
import java.util.List;

import org.exoplatform.commons.utils.ListAccess;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.task.dao.LabelTaskMappingHandler;
import org.exoplatform.task.domain.Label;
import org.exoplatform.task.domain.LabelTaskMapping;
import org.exoplatform.task.domain.Task;

import javax.persistence.PersistenceException;
import javax.persistence.Query;
import javax.persistence.TypedQuery;

public class LabelTaskMappingDAOImpl extends CommonJPADAO<LabelTaskMapping, Serializable> implements LabelTaskMappingHandler {

    private static final Log log = ExoLogger.getExoLogger(LabelTaskMappingDAOImpl.class);

    @Override
    public LabelTaskMapping findLabelTaskMapping(long labelId, long taskId) {
        TypedQuery<LabelTaskMapping> query = getEntityManager().createNamedQuery("LabelTaskMapping.findLabelMapping", LabelTaskMapping.class);
        query.setParameter("labelId", labelId);
        query.setParameter("taskId", taskId);
        try {
            return cloneEntity((LabelTaskMapping)query.getSingleResult());
        } catch (PersistenceException e) {
            log.error("Error when fetching label mapping", e);
            return null;
        }
    }
    @Override
    public List<LabelTaskMapping> findLabelMappingByLabel(long labelId) {
        TypedQuery<LabelTaskMapping> query = getEntityManager().createNamedQuery("LabelTaskMapping.findLabelMappingByLabel", LabelTaskMapping.class);
        query.setParameter("labelId", labelId);
        try {
            return cloneEntities((List<LabelTaskMapping>)query.getResultList());
        } catch (PersistenceException e) {
            log.error("Error when fetching label mapping", e);
            return null;
        }
    }
}


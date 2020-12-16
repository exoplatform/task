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

import org.exoplatform.commons.api.persistence.ExoTransactional;
import org.exoplatform.commons.utils.PropertyManager;
import org.exoplatform.services.listener.ListenerService;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.task.dao.DAOHandler;
import org.exoplatform.task.dto.ProjectDto;
import org.exoplatform.task.dto.StatusDto;
import org.exoplatform.task.exception.EntityNotFoundException;
import org.exoplatform.task.exception.NotAllowedOperationOnEntityException;
import org.exoplatform.task.service.StatusService;
import org.exoplatform.task.storage.ProjectStorage;
import org.exoplatform.task.storage.StatusStorage;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;


@Singleton
public class StatusServiceImpl implements StatusService {

    @Inject
    private DAOHandler daoHandler;

    @Inject
    private StatusStorage statusStorage;

    @Inject
    private ProjectStorage projectStorage;

    private ListenerService listenerService;

    private String[] DEFAULT_STATUS = {"ToDo", "InProgress", "WaitingOn", "Done"};

    private static Log LOG = ExoLogger.getExoLogger(org.exoplatform.task.legacy.service.impl.StatusServiceImpl.class);

    public StatusServiceImpl(DAOHandler daoHandler, StatusStorage statusStorage, ProjectStorage projectStorage, ListenerService listenerService) {
        this.daoHandler = daoHandler;
        this.statusStorage = statusStorage;
        this.projectStorage = projectStorage;
        this.listenerService = listenerService;
    }

    public StatusServiceImpl(DAOHandler daoHandler) {
        String status = PropertyManager.getProperty("exo.tasks.default.workflow");
        if (status != null) {
            List<String> stList = new LinkedList<String>();
            for (String s : status.split(",")) {
                s = s.trim();
                if (!s.isEmpty()) {
                    stList.add(s);
                }
            }

            if (!stList.isEmpty()) {
                DEFAULT_STATUS = stList.toArray(new String[stList.size()]);
            }
        }
        this.daoHandler = daoHandler;
    }

    private List<String> getDefaultStatus() {
        return Arrays.asList(DEFAULT_STATUS);
    }

    @Override
    @ExoTransactional
    public void createInitialStatuses(ProjectDto proj) {
        for (String s : getDefaultStatus()) {
            createStatus(proj, s);
        }
    }

    @Override
    public StatusDto getStatus(long statusId) {
        return statusStorage.getStatus(statusId);
    }

    @Override
    public StatusDto getDefaultStatus(long projectId) {
        return statusStorage.getDefaultStatus(projectId);
    }

    @Override
    public List<StatusDto> getStatuses(long projectId) {
        return statusStorage.getStatuses(projectId);
    }

    @Override
    @ExoTransactional
    public StatusDto createStatus(ProjectDto project, String name) {
        if (name == null || (name = name.trim()).isEmpty() || project == null) {
            throw new IllegalArgumentException("project must be not null and status must not be null or empty");
        }
        try {
            listenerService.broadcast("exo.project.projectModified", null, projectStorage.projectToEntity(project));
        } catch (Exception e) {
            LOG.error("Error while broadcasting status creation event", e);
        }
        return statusStorage.createStatus(project, name);
    }

    @Override
    @ExoTransactional
    public StatusDto createStatus(ProjectDto project, String name, int rank) {
        if (name == null || (name = name.trim()).isEmpty() || project == null) {
            throw new IllegalArgumentException("project must be not null and status must not be null or empty");
        }
        try {
            listenerService.broadcast("exo.project.projectModified", this, projectStorage.projectToEntity(project));
        } catch (Exception e) {
            LOG.error("Error while broadcasting status creation event", e);
        }
        return statusStorage.createStatus(project, name, rank);
    }

    @Override
    @ExoTransactional
    public void removeStatus(long statusID) throws Exception {
        statusStorage.removeStatus(statusID);
    }

    @Override
    @ExoTransactional
    public StatusDto updateStatus(long id, String name) throws EntityNotFoundException, NotAllowedOperationOnEntityException {
        if (name == null || (name = name.trim()).isEmpty()) {
            throw new IllegalArgumentException("status name can't be null or empty");
        }
        StatusDto statusDto = statusStorage.updateStatus(id, name);
        try {
            listenerService.broadcast("exo.project.projectModified", this, statusDto);
        } catch (Exception e) {
            LOG.error("Error while broadcasting status update event", e);
        }
        return statusDto;
    }

    @Override
    @ExoTransactional
    public StatusDto updateStatus(StatusDto statusDto) throws EntityNotFoundException, NotAllowedOperationOnEntityException {
        String name = statusDto.getName();
        if (name == null || (name = name.trim()).isEmpty()) {
            throw new IllegalArgumentException("status name can't be null or empty");
        }
        statusDto = statusStorage.updateStatus(statusDto);
        try {
            listenerService.broadcast("exo.project.projectModified", this, statusDto.getProject());
        } catch (Exception e) {
            LOG.error("Error while broadcasting status update event", e);
        }
        return statusDto;
    }

}

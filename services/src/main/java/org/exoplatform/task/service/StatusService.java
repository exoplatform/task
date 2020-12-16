package org.exoplatform.task.service;


import org.exoplatform.task.dto.ProjectDto;
import org.exoplatform.task.dto.StatusDto;
import org.exoplatform.task.exception.EntityNotFoundException;
import org.exoplatform.task.exception.NotAllowedOperationOnEntityException;

import java.util.List;

public interface StatusService {

    /**
     * Create initial statuses for given <code>project</code>.
     * <p>
     * The initial statuses can be configured via a system exo property <code>exo.tasks.default.status</code>.
     * If it's not configured, the default <code>{"To Do", "In Progress", "Waiting On", "Done"}</code> will be used.
     *
     * @param project The given project.
     */
    void createInitialStatuses(ProjectDto project);

    /**
     * Return the <code>Status</code> with given <code>statusId</code>.
     *
     * @param statusId The status id.
     * @return The status of the given statusId.
     */
    StatusDto getStatus(long statusId);

    /**
     * Return the default status of the project which is ideally the first step in the project workflow.
     *
     * @param projectId The project id.
     * @return The default status of the given project.
     */
    StatusDto getDefaultStatus(long projectId);

    /**
     * Return the list of statuses from a project with given <code>projectId</code>.
     *
     * @param projectId The project id.
     * @return The status of the given project.
     */
    List<StatusDto> getStatuses(long projectId);

    StatusDto createStatus(ProjectDto project, String status);

    StatusDto createStatus(ProjectDto project, String status, int rank);

    void removeStatus(long statusId) throws EntityNotFoundException, NotAllowedOperationOnEntityException, Exception;

    StatusDto updateStatus(long statusId, String statusName) throws EntityNotFoundException, NotAllowedOperationOnEntityException;

    StatusDto updateStatus(StatusDto statusDto) throws EntityNotFoundException, NotAllowedOperationOnEntityException;


}

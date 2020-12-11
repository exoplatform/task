package org.exoplatform.task.service;

import org.exoplatform.commons.utils.ListAccess;
import org.exoplatform.task.dao.OrderBy;
import org.exoplatform.task.dao.ProjectQuery;
import org.exoplatform.task.dto.ProjectDto;
import org.exoplatform.task.exception.EntityNotFoundException;

import java.util.List;
import java.util.Set;


public interface ProjectService {

    /**
     * Return the project with given <code>projectId</code>.
     *
     * @param projectId the project id.
     * @return get the given project.
     * @throws EntityNotFoundException when user is not authorized to get project.
     */
    ProjectDto getProject(Long projectId) throws EntityNotFoundException;

    List<ProjectDto> findCollaboratedProjects(String userName, String keyword, int offset, int limit);

    List<ProjectDto> findNotEmptyProjects(List<String> memberships, String keyword, int offset, int limit);

    int countCollaboratedProjects(String userName, String keyword);

    int countNotEmptyProjects(List<String> memberships, String keyword);

    int countProjects(List<String> memberships, String keyword);

    Set<String> getManager(long projectId);

    Set<String> getParticipator(long projectId);

    /**
     * Create a project with given <code>project</code> model object.
     *
     * @param project the given project.
     * @return create the given project.
     */
    ProjectDto createProject(ProjectDto project);

    /**
     * Create a sub-project with given <code>project</code> model object and parent project ID.
     *
     * @param project  the project metadata to create.
     * @param parentId parent project ID
     * @return Create a sub-project.
     * @throws EntityNotFoundException the project associated with <code>parentId</code> doesn't exist.
     */
    ProjectDto createProject(ProjectDto project, long parentId) throws EntityNotFoundException;

    /**
     * Update the project.
     * <p>
     * It should throws EntityNotFoundException if the project has been removed OR not existed from database.
     *
     * @param project the given project.
     * @return update the project.
     */
    ProjectDto updateProject(ProjectDto project);

    /**
     * Remove the project with given <code>projectId</code>,
     * and also its descendants if <code>deleteChild</code> is true.
     *
     * @param projectId   the given project id.
     * @param deleteChild delete Child.
     * @throws EntityNotFoundException when user is not authorized to remove project.
     */
    void removeProject(long projectId, boolean deleteChild) throws EntityNotFoundException;

    /**
     * Clone a project with given <code>projectId</code>. If <code>cloneTask</code> is true,
     * it will also clone all non-completed tasks from the project.
     *
     * @param projectId The id of a project which it copies from.
     * @param cloneTask If false, it will clone only project metadata.
     *                  Otherwise, it also clones all non-completed tasks from the project.
     * @return The cloned project.
     * @throws EntityNotFoundException when user is not authorized to clone project.
     */
    ProjectDto cloneProject(long projectId, boolean cloneTask) throws EntityNotFoundException, Exception;

    /**
     * Return a list of children of a parent project with given <code>parentId</code>.
     *
     * @param parentId The parent id of a project.
     * @return The list of children of a parent project.
     */
    List<ProjectDto> getSubProjects(long parentId,int offset ,int limit);

    List<ProjectDto> findProjects(ProjectQuery query,int offset ,int limit);

    int countProjects(ProjectQuery query);

    List<ProjectDto> findProjects(List<String> memberships, String keyword, OrderBy order,int offset ,int limit);
}

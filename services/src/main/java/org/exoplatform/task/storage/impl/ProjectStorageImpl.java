package org.exoplatform.task.storage.impl;

import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.task.dao.DAOHandler;
import org.exoplatform.task.dao.OrderBy;
import org.exoplatform.task.dao.ProjectQuery;
import org.exoplatform.task.domain.Project;
import org.exoplatform.task.dto.ProjectDto;
import org.exoplatform.task.exception.EntityNotFoundException;
import org.exoplatform.task.storage.ProjectStorage;

import javax.inject.Inject;
import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class ProjectStorageImpl implements ProjectStorage {

    private static final Log LOG = ExoLogger.getExoLogger(ProjectStorageImpl.class);

    private static final Pattern pattern = Pattern.compile("@([^\\s]+)|@([^\\s]+)$");


    @Inject
    private final DAOHandler daoHandler;


    public ProjectStorageImpl(DAOHandler daoHandler) {
        this.daoHandler = daoHandler;
    }

    @Override
    public ProjectDto getProject(Long projectId) throws EntityNotFoundException {
        return projectToDto(daoHandler.getProjectHandler().find(projectId));
    }

    @Override
    public Set<String> getManager(long projectId) {
        ProjectQuery query = new ProjectQuery();
        query.setId(projectId);
        List<String> manager = daoHandler.getProjectHandler().selectProjectField(query, "manager");
        return new HashSet<String>(manager);
    }

    @Override
    public Set<String> getParticipator(long projectId) {
        ProjectQuery query = new ProjectQuery();
        query.setId(projectId);
        List<String> manager = daoHandler.getProjectHandler().selectProjectField(query, "participator");
        return new HashSet<String>(manager);
    }

    @Override
    public ProjectDto createProject(ProjectDto project) {
        return projectToDto(daoHandler.getProjectHandler().create(projectToEntity(project)));
    }

    @Override
    public ProjectDto createProject(ProjectDto project, long parentId) throws EntityNotFoundException {
        return null;
    }

    @Override
    public ProjectDto updateProject(ProjectDto project) {
        return projectToDto(daoHandler.getProjectHandler().update(projectToEntity(project)));
    }

    @Override
    public void removeProject(long projectId, boolean deleteChild) throws EntityNotFoundException {
        ProjectDto project = getProject(projectId);
        if (project == null) throw new EntityNotFoundException(projectId, Project.class);
        daoHandler.getProjectHandler().removeProject(projectId, deleteChild);
    }

    @Override
    public ProjectDto cloneProject(long projectId, boolean cloneTask) throws EntityNotFoundException {
        return null;
    }

    @Override
    public List<ProjectDto> getSubProjects(long parentId, int offset, int limit) throws Exception {
        ProjectDto parent = getProject(parentId);
        return Arrays.asList(daoHandler.getProjectHandler().findSubProjects(projectToEntity(parent)).load(offset, limit)).stream().map(this::projectToDto).collect(Collectors.toList());

    }

    @Override
    public List<ProjectDto> findProjects(ProjectQuery query, int offset, int limit) {
        try {
            return Arrays.asList(daoHandler.getProjectHandler().findProjects(query).load(offset, limit)).stream().map(this::projectToDto).collect(Collectors.toList());
        } catch (Exception e) {
            return new ArrayList<ProjectDto>();
        }

    }

    @Override
    public List<ProjectDto> findProjects(List<String> memberships, String keyword, OrderBy order, int offset, int limit) {
        try {
            return Arrays.asList(daoHandler.getProjectHandler().findAllByMembershipsAndKeyword(memberships, keyword, order).load(offset, limit)).stream().map(this::projectToDto).collect(Collectors.toList());
        } catch (Exception e) {
            return new ArrayList<ProjectDto>();
        }
    }

    @Override
    public Project projectToEntity(ProjectDto projectDto) {
        if(projectDto==null){
            return null;
        }
        Project project = new Project();
        project.setId(projectDto.getId());
        project.setName(projectDto.getName());
        project.setDescription(projectDto.getDescription());
        project.setParent(projectDto.getParent());
        project.setColor(projectDto.getColor());
        project.setDueDate(projectDto.getDueDate());
        project.setParticipator(projectDto.getParticipator());
        project.setManager(projectDto.getManager());
        project.setParent(projectDto.getParent());
        project.setStatus(projectDto.getStatus());
        project.setChildren(projectDto.getChildren());
        return project;

    }

    @Override
    public ProjectDto projectToDto(Project project) {
        if(project==null){
            return null;
        }
        ProjectDto projectDto = new ProjectDto();
        projectDto.setId(project.getId());
        projectDto.setName(project.getName());
        projectDto.setDescription(project.getDescription());
        projectDto.setParent(project.getParent());
        projectDto.setColor(project.getColor());
        projectDto.setDueDate(project.getDueDate());
        projectDto.setParticipator(project.getParticipator());
        projectDto.setManager(project.getManager());
        projectDto.setParent(project.getParent());
        projectDto.setStatus(project.getStatus());
        projectDto.setChildren(project.getChildren());
        return projectDto;
    }
}

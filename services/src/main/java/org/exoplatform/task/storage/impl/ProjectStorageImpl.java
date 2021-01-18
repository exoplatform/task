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
import org.exoplatform.task.util.StorageUtil;

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
        return StorageUtil.projectToDto(daoHandler.getProjectHandler().find(projectId),this);
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
        return StorageUtil.projectToDto(daoHandler.getProjectHandler().create(StorageUtil.projectToEntity(project)),this);
    }

    @Override
    public ProjectDto createProject(ProjectDto project, long parentId) throws EntityNotFoundException {
        return null;
    }

    @Override
    public ProjectDto updateProject(ProjectDto project) {
        return StorageUtil.projectToDto(daoHandler.getProjectHandler().update(StorageUtil.projectToEntity(project)),this);
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
        return Arrays.asList(daoHandler.getProjectHandler().findSubProjects(StorageUtil.projectToEntity(parent)).load(offset, limit)).stream().map((Project project) -> StorageUtil.projectToDto(project,this)).collect(Collectors.toList());

    }

    @Override
    public List<ProjectDto> findProjects(ProjectQuery query, int offset, int limit) {
        try {
            return Arrays.asList(daoHandler.getProjectHandler().findProjects(query).load(offset, limit)).stream().map((Project project) -> StorageUtil.projectToDto(project,this)).collect(Collectors.toList());
        } catch (Exception e) {
            return new ArrayList<ProjectDto>();
        }

    }

    @Override
    public int countProjects(ProjectQuery query) {
        try {
            return daoHandler.getProjectHandler().findProjects(query).getSize();
        } catch (Exception e) {
            return 0;
        }

    }

    @Override
    public List<ProjectDto> findProjects(List<String> memberships, String keyword, OrderBy order, int offset, int limit) {
        try {
            return Arrays.asList(daoHandler.getProjectHandler().findAllByMembershipsAndKeyword(memberships, keyword, order).load(offset, limit)).stream().map((Project project) -> StorageUtil.projectToDto(project,this)).collect(Collectors.toList());
        } catch (Exception e) {
            return new ArrayList<ProjectDto>();
        }
    }

    @Override
    public List<ProjectDto> findCollaboratedProjects(String userName, String keyword,int offset ,int limit){
        try {
            return daoHandler.getProjectHandler().findCollaboratedProjects(userName,keyword,offset, limit).stream().map((Project project) -> StorageUtil.projectToDto(project,this)).collect(Collectors.toList());
        } catch (Exception e) {
            return new ArrayList<ProjectDto>();
        }
    }

    @Override
    public List<ProjectDto> findNotEmptyProjects(List<String> memberships, String keyword,int offset ,int limit) {
        try {
            return daoHandler.getProjectHandler().findNotEmptyProjects(memberships,keyword,offset, limit).stream().map((Project project) -> StorageUtil.projectToDto(project,this)).collect(Collectors.toList());
        } catch (Exception e) {
            return new ArrayList<ProjectDto>();
        }
    }

    @Override
    public int countCollaboratedProjects(String userName, String keyword){
        try {
            return daoHandler.getProjectHandler().countCollaboratedProjects(userName,keyword);
        } catch (Exception e) {
            return 0;
        }
    }

    @Override
    public int countNotEmptyProjects(List<String> memberships, String keyword) {
        try {
            return daoHandler.getProjectHandler().countNotEmptyProjects(memberships,keyword);
        } catch (Exception e) {
            return 0;
        }
    }

    @Override
    public int countProjects(List<String> memberships, String keyword) {
        try {
            return daoHandler.getProjectHandler().findAllByMembershipsAndKeyword(memberships, keyword,null).getSize();
        } catch (Exception e) {
            return 0;
        }

    }

}

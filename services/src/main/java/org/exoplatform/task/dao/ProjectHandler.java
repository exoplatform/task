package org.exoplatform.task.dao;

import java.util.List;

import org.exoplatform.commons.api.persistence.GenericDAO;
import org.exoplatform.commons.utils.ListAccess;
import org.exoplatform.task.domain.Project;

/**
 * @author <a href="trongtt@exoplatform.com">Trong Tran</a>
 * @version $Revision$
 */
public interface ProjectHandler extends GenericDAO<Project, Long> {

  Project removeProject(long projectId, boolean deleteChild);

  ListAccess<Project> findSubProjects(Project project);

  ListAccess<Project> findAllByMembershipsAndKeyword(List<String> membership, String keyword, OrderBy order);

  ListAccess<Project> findProjects(ProjectQuery query);
}

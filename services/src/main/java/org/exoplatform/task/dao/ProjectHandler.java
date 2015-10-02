package org.exoplatform.task.dao;

import java.util.List;

import org.exoplatform.commons.api.persistence.GenericDAO;
import org.exoplatform.task.domain.Project;

/**
 * @author <a href="trongtt@exoplatform.com">Trong Tran</a>
 * @version $Revision$
 */
public interface ProjectHandler extends GenericDAO<Project, Long> {

  Project removeProject(long projectId, boolean deleteChild);

  List<Project> findSubProjects(Project project);

  List<Project> findSubProjectsByMemberships(Project project, List<String> memberships);

  List<Project> findAllByMemberships(List<String> memberships);

  List<Project> findAllByMembershipsAndKeyword(List<String> membership, String keyword, OrderBy order);
}

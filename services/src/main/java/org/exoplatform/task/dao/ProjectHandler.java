package org.exoplatform.task.dao;

import org.exoplatform.task.domain.Project;

import java.util.List;

/**
 * @author <a href="trongtt@exoplatform.com">Trong Tran</a>
 * @version $Revision$
 */
public interface ProjectHandler extends GenericDAO<Project, Long> {
  List<Project> findSubProjects(Project project);
  
  Project cloneProject(Long projectId, boolean cloneTask);
}

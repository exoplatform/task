package org.exoplatform.task.dao;

import org.exoplatform.commons.api.persistence.GenericDAO;
import org.exoplatform.task.domain.Status;

/**
 * @author <a href="trongtt@exoplatform.com">Trong Tran</a>
 * @version $Revision$
 */
public interface StatusHandler extends GenericDAO<Status, Long> {

  Status findLowestRankStatusByProject(Long projectId);

  Status findHighestRankStatusByProject(long projectId);

  Status findByName(String name, long projectID);
}

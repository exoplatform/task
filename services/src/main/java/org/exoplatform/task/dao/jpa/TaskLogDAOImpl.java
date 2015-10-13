/*
 * Copyright (C) 2015 eXo Platform SAS.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */

package org.exoplatform.task.dao.jpa;

import org.exoplatform.commons.utils.ListAccess;
import org.exoplatform.task.dao.TaskLogHandler;
import org.exoplatform.task.domain.ChangeLog;

import javax.persistence.TypedQuery;

/**
 * @author <a href="mailto:tuyennt@exoplatform.com">Tuyen Nguyen The</a>.
 */
public class TaskLogDAOImpl extends CommonJPADAO<ChangeLog, Long> implements TaskLogHandler {
  @Override
  public ListAccess<ChangeLog> findTaskLogs(Long taskId) {
    TypedQuery<ChangeLog> query = getEntityManager().createNamedQuery("TaskChangeLog.findChangeLogByTaskId", ChangeLog.class);
    TypedQuery<Long> count = getEntityManager().createNamedQuery("TaskChangeLog.countChangeLogByTaskId", Long.class);

    query.setParameter("taskId", taskId);
    count.setParameter("taskId", taskId);

    return new JPAQueryListAccess<ChangeLog>(ChangeLog.class, count, query);
  }
}

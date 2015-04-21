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

package org.exoplatform.task.dao;

import org.exoplatform.task.domain.Comment;
import org.exoplatform.task.domain.Task;

import java.util.List;

/**
 * @author <a href="mailto:tuyennt@exoplatform.com">Tuyen Nguyen The</a>.
 */
public interface CommentHandler extends GenericDAO<Comment, Long> {
  /**
   * Count number comments of Task
   * @param task
   * @return
   */
  long count(Task task);

  /**
   * Load list comments of a task. The List comments will be sorted by createdTime inverse.
   * (Last comment will be at first)
   * @param task - Task object
   * @param start - The first row to select.
   * @param limit - The limit row to select. If limit <= 0, the start param will be ignore and all comment will be loaded.
   * @return list of comments
   */
  List<Comment> findCommentsOfTask(Task task, int start, int limit);
}

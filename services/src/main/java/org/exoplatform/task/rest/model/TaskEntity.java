/**
 * Copyright (C) 2015 eXo Platform SAS.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Affero General Public License
 * as published by the Free Software Foundation; either version 3
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, see<http://www.gnu.org/licenses/>.
 **/
  
package org.exoplatform.task.rest.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import lombok.Data;
import org.exoplatform.social.core.space.model.Space;
import org.exoplatform.task.domain.Label;
import org.exoplatform.task.dto.LabelDto;
import org.exoplatform.task.dto.TaskDto;
import org.exoplatform.task.model.CommentModel;
import org.exoplatform.task.model.User;
import org.exoplatform.task.util.UserUtil;

@Data
public class TaskEntity {
  private TaskDto task;
  private boolean readOnly;
  private boolean isWatched;
  private String breadcumbs;
  private User assignee;
  private User createdBy;
  private List<User> coworker;
  private List<User> watcher;
  private int numberCoworkers;
  private long commentCount;
  private List<CommentModel> comments;
  private User currentUser;
  private Space space;
  private List<LabelDto> labels = new ArrayList<LabelDto>();

  public TaskEntity() {
  }

  public TaskEntity(TaskDto task, long commentCount) {
    this.task = task;
    this.commentCount = commentCount;
    this.assignee = UserUtil.getUser(task.getAssignee());
    this.createdBy = UserUtil.getUser(task.getCreatedBy());
    if(task.getCoworker()!=null) this.coworker = task.getCoworker().stream().map(user -> UserUtil.getUser(user)).collect(Collectors.toList());
    if(task.getWatcher()!=null) this.watcher = task.getWatcher().stream().map(user -> UserUtil.getUser(user)).collect(Collectors.toList());
  }

}
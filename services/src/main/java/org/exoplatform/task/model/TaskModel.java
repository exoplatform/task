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
  
package org.exoplatform.task.model;

import java.util.ArrayList;
import java.util.List;

import org.exoplatform.task.domain.Label;
import org.exoplatform.task.domain.Task;

public class TaskModel {
  private Task task;
  private boolean readOnly;
  private boolean isWatched;
  private String breadcumbs;
  private User assignee;
  private int numberCoworkers;
  private long commentCount;
  private List<CommentModel> comments;
  private User currentUser;
  private List<Label> labels = new ArrayList<Label>();

  public Task getTask() {
    return task;
  }
  public void setTask(Task task) {
    this.task = task;
  }
  public String getBreadcumbs() {
    return breadcumbs;
  }
  public void setBreadcumbs(String breadcumbs) {
    this.breadcumbs = breadcumbs;
  }
  public User getAssignee() {
    return assignee;
  }
  public void setAssignee(User assignee) {
    this.assignee = assignee;
  }
  public long getCommentCount() {
    return commentCount;
  }
  public void setCommentCount(long commentCount) {
    this.commentCount = commentCount;
  }
  public List<CommentModel> getComments() {
    return comments;
  }
  public void setComments(List<CommentModel> comments) {
    this.comments = comments;
  }
  public User getCurrentUser() {
    return currentUser;
  }
  public void setCurrentUser(User currentUser) {
    this.currentUser = currentUser;
  }

  public int getNumberCoworkers() {
    return numberCoworkers;
  }

  public void setNumberCoworkers(int numberCoworkers) {
    this.numberCoworkers = numberCoworkers;
  }

  public List<Label> getLabels() {
    return labels;
  }

  public void setLabels(List<Label> labels) {
    this.labels = labels;
  }

  public boolean isReadOnly() {
    return readOnly;
  }

  public void setReadOnly(boolean readOnly) {
    this.readOnly = readOnly;
  }

  public boolean isWatched() {
    return isWatched;
  }

  public void setWatched(boolean isWatched) {
    this.isWatched = isWatched;
  }
}

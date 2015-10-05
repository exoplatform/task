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

import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

import org.exoplatform.task.domain.Priority;
import org.exoplatform.task.utils.TaskUtil;

/**
 * @author <a href="mailto:tuyennt@exoplatform.com">Tuyen Nguyen The</a>.
 */
public class TaskQuery {
  private long taskId = 0;
  private String title = null;
  private String description = null;
  private List<Long> projectIds;
  private String assignee = null;
  private String createdBy = null;
  private String coworker = null;
  private String keyword = null;
  private Boolean calendarIntegrated;
  private Boolean completed;
  private Date startDate;
  private Date endDate;
  private List<Long> labelIds;
  private List<String> tags;
  private Long statusId = -1L;
  private Long dueDateFrom;
  private Long dueDateTo;
  private Priority priority;  
  private List<String> memberships;  
  
  private List<OrderBy> orderBy = new ArrayList<OrderBy>();
  private List<String> orFields = new LinkedList<String>();  

  public long getTaskId() {
    return taskId;
  }

  public void setTaskId(long taskId) {
    this.taskId = taskId;
  }

  public String getTitle() {
    return title;
  }

  public void setTitle(String title) {
    this.title = title;
  }

  public String getDescription() {
    return description;
  }

  public void setDescription(String description) {
    this.description = description;
  }

  public List<Long> getProjectIds() {
    return projectIds;
  }

  public void setProjectIds(List<Long> projectIds) {
    this.projectIds = projectIds;
  }

  public String getAssignee() {
    return assignee;
  }

  public void setAssignee(String assignee) {
    this.assignee = assignee;
  }

  public String getCreatedBy() {
    return createdBy;
  }

  public void setCreatedBy(String createdBy) {
    this.createdBy = createdBy;
  }

  public String getCoworker() {
    return coworker;
  }

  public void setCoworker(String coworker) {
    this.coworker = coworker;
  }

  public String getKeyword() {
    return keyword;
  }

  public void setKeyword(String keyword) {
    this.keyword = keyword;
  }

  public List<OrderBy> getOrderBy() {
    return orderBy;
  }

  public void setOrderBy(List<OrderBy> orderBy) {
    this.orderBy = orderBy;
  }

  public Boolean getCompleted() {
    return completed;
  }

  public void setCompleted(Boolean completed) {
    this.completed = completed;
  }

  public Date getStartDate() {
    return startDate;
  }

  public void setStartDate(Date startDate) {
    this.startDate = startDate;
  }

  public Date getEndDate() {
    return endDate;
  }

  public void setEndDate(Date endDate) {
    this.endDate = endDate;
  }

  public Boolean getCalendarIntegrated() {
    return calendarIntegrated;
  }

  public void setCalendarIntegrated(Boolean calendarIntegrated) {
    this.calendarIntegrated = calendarIntegrated;
  }

  public void setMemberships(List<String> permissions) {
    this.memberships =  permissions;
  }

  public List<String> getMemberships() {
    return memberships;
  }  

  public List<Long> getLabelIds() {
    return labelIds;
  }

  public void setLabelIds(List<Long> labelIds) {
    this.labelIds = labelIds;
  }

  public List<String> getTags() {
    return tags;
  }

  public void setTags(List<String> tags) {
    this.tags = tags;
  }

  public Long getStatusId() {
    return statusId;
  }

  public void setStatusId(Long statusId) {
    this.statusId = statusId;
  }

  public Long getDueDateFrom() {
    return dueDateFrom;
  }

  public void setDueDateFrom(Long dueDateFrom) {
    this.dueDateFrom = dueDateFrom;
  }

  public Long getDueDateTo() {
    return dueDateTo;
  }

  public void setDueDateTo(Long dueDateTo) {
    this.dueDateTo = dueDateTo;
  }

  public Priority getPriority() {
    return priority;
  }

  public void setPriority(Priority priority) {
    this.priority = priority;
  }

  public List<String> getOrFields() {
    return orFields;
  }

  public void setOrFields(List<String> orFields) {
    this.orFields = orFields;
  }
}

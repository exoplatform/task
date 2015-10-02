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

import org.exoplatform.task.dao.condition.AggregateCondition;
import org.exoplatform.task.dao.condition.Condition;
import org.exoplatform.task.dao.condition.Conditions;
import org.exoplatform.task.domain.Status;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import static org.exoplatform.task.dao.condition.Conditions.*;

import org.exoplatform.task.domain.Priority;

/**
 * @author <a href="mailto:tuyennt@exoplatform.com">Tuyen Nguyen The</a>.
 */
public class TaskQuery implements Cloneable {

  private AggregateCondition aggCondition = null;

  //TODO: how to remove these two field
  private List<Long> projectIds = null;
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

  public TaskQuery() {

  }

  TaskQuery(AggregateCondition condition, List<OrderBy> orderBies, List<Long> projectIds, String assignee) {
    this.aggCondition = condition;
    this.orderBy = orderBies;
    this.projectIds = projectIds;
    this.assignee = assignee;
  }

  public static TaskQuery or(TaskQuery... queries) {
    List<Condition> cond = new ArrayList<Condition>();
    for(TaskQuery q : queries) {
      if (q.getCondition() != null) {
        cond.add(q.getCondition());
      }
    }
    Condition c = Conditions.or(cond.toArray(new Condition[cond.size()]));
    TaskQuery q = new TaskQuery();
    q.add(c);
    return q;
  }

  public TaskQuery add(TaskQuery taskQuery) {
    this.add(taskQuery.getCondition());
    return this;
  }

  TaskQuery add(Condition condition) {
    if (condition == null) return this;

    if (aggCondition == null) {
      aggCondition = and(condition);
    } else {
      aggCondition.add(condition);
    }
    return this;
  }

  public Condition getCondition() {
    return this.aggCondition;
  }

  public void setTitle(String title) {
    this.add(like(TASK_TITLE, '%' + title + '%'));
  }

  public void setDescription(String description) {
    this.add(like(TASK_DES, '%' + description + '%'));
  }

  public List<Long> getProjectIds() {
    return projectIds;
  }

  public void setProjectIds(List<Long> projectIds) {
    this.projectIds = projectIds;
    this.add(in(TASK_PROJECT, projectIds));
  }

  public String getAssignee() {
    return assignee;
  }

  public void setAssignee(String assignee) {
    this.add(eq(TASK_ASSIGNEE, assignee));
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
    if (keyword == null || keyword.trim().isEmpty()) return;

    List<Condition> conditions = new ArrayList<Condition>();
    for(String k : keyword.split(" ")) {
      if (!k.trim().isEmpty()) {
        k = "%" + k.trim().toLowerCase() + "%";
        conditions.add(like(TASK_TITLE, k));
        conditions.add(like(TASK_DES, k));
        conditions.add(like(TASK_ASSIGNEE, k));
      }
    }
    add(Conditions.or(conditions.toArray(new Condition[conditions.size()])));
  }

  public List<OrderBy> getOrderBy() {
    return orderBy;
  }

  public void setOrderBy(List<OrderBy> orderBy) {
    this.orderBy = orderBy;
  }

  public void setCompleted(Boolean completed) {
    if (completed) {
      add(isTrue(TASK_COMPLETED));
    } else {
      add(isFalse(TASK_COMPLETED));
    }
  }

  public void setStartDate(Date startDate) {
    add(gte(TASK_END_DATE, startDate));
  }

  public void setEndDate(Date endDate) {
    add(lte(TASK_START_DATE, endDate));
  }

  public void setCalendarIntegrated(Boolean calendarIntegrated) {
    if (calendarIntegrated) {
      add(isTrue(TASK_CALENDAR_INTEGRATED));
    } else {
      add(isFalse(TASK_CALENDAR_INTEGRATED));
    }
  }

  public void setMemberships(List<String> permissions) {
    add(Conditions.or(in(TASK_PARTICIPATOR, permissions), in(TASK_MANAGER, permissions)));
  }

  public void setAssigneeOrMembership(String username, List<String> memberships) {
    this.assignee = username;
    this.add(Conditions.or(eq(TASK_ASSIGNEE, username), in(TASK_MANAGER, memberships), in(TASK_PARTICIPATOR, memberships)));
  }

  public void setAssigneeOrInProject(String username, List<Long> projectIds) {
    this.assignee = username;
    this.projectIds = projectIds;
    this.add(Conditions.or(eq(TASK_ASSIGNEE, username), in(TASK_PROJECT, projectIds)));
  }

  public void setStatus(Status status) {
    add(eq(TASK_STATUS, status));
  }

  public void setDueDateFrom(Date dueDateFrom) {
    if (dueDateFrom != null) {
      add(gte(TASK_DUEDATE, dueDateFrom));
    }
  }

  public void setDueDateTo(Date dueDateTo) {
    if (dueDateTo != null) {
      add(lte(TASK_DUEDATE, dueDateTo));
    }
  }

  public void setIsIncomingOf(String username) {
    add(and(Conditions.or(eq(TASK_ASSIGNEE, username), eq(TASK_COWORKER, username), eq(TASK_CREATOR, username)), isNull(TASK_STATUS)));
  }

  public void setIsTodoOf(String username) {
    setAssignee(username);
    //add(eq(TASK_ASSIGNEE, username));
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

  public void setNullField(String nullField) {
    add(isNull(nullField));
  }

  public TaskQuery clone() {
    return new TaskQuery(aggCondition.clone(), orderBy, projectIds != null ? new ArrayList<Long>(projectIds) : null, assignee);
  }
}

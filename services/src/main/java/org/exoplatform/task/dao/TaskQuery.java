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
import org.exoplatform.task.domain.Priority;
import org.exoplatform.task.domain.Status;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

import static org.exoplatform.task.dao.condition.Conditions.TASK_ASSIGNEE;
import static org.exoplatform.task.dao.condition.Conditions.TASK_CALENDAR_INTEGRATED;
import static org.exoplatform.task.dao.condition.Conditions.TASK_COMPLETED;
import static org.exoplatform.task.dao.condition.Conditions.TASK_COWORKER;
import static org.exoplatform.task.dao.condition.Conditions.TASK_CREATOR;
import static org.exoplatform.task.dao.condition.Conditions.TASK_DES;
import static org.exoplatform.task.dao.condition.Conditions.TASK_DUEDATE;
import static org.exoplatform.task.dao.condition.Conditions.TASK_END_DATE;
import static org.exoplatform.task.dao.condition.Conditions.TASK_LABEL_ID;
import static org.exoplatform.task.dao.condition.Conditions.TASK_LABEL_USERNAME;
import static org.exoplatform.task.dao.condition.Conditions.TASK_MANAGER;
import static org.exoplatform.task.dao.condition.Conditions.TASK_PARTICIPATOR;
import static org.exoplatform.task.dao.condition.Conditions.TASK_PRIORITY;
import static org.exoplatform.task.dao.condition.Conditions.TASK_PROJECT;
import static org.exoplatform.task.dao.condition.Conditions.TASK_START_DATE;
import static org.exoplatform.task.dao.condition.Conditions.TASK_STATUS;
import static org.exoplatform.task.dao.condition.Conditions.TASK_TAG;
import static org.exoplatform.task.dao.condition.Conditions.TASK_TITLE;
import static org.exoplatform.task.dao.condition.Conditions.and;
import static org.exoplatform.task.dao.condition.Conditions.eq;
import static org.exoplatform.task.dao.condition.Conditions.gte;
import static org.exoplatform.task.dao.condition.Conditions.in;
import static org.exoplatform.task.dao.condition.Conditions.isEmpty;
import static org.exoplatform.task.dao.condition.Conditions.isFalse;
import static org.exoplatform.task.dao.condition.Conditions.isNull;
import static org.exoplatform.task.dao.condition.Conditions.isTrue;
import static org.exoplatform.task.dao.condition.Conditions.like;
import static org.exoplatform.task.dao.condition.Conditions.lte;

/**
 * @author <a href="mailto:tuyennt@exoplatform.com">Tuyen Nguyen The</a>.
 */
public class TaskQuery implements Cloneable {

  private AggregateCondition aggCondition = null;

  //TODO: how to remove these two field
  private List<Long> projectIds = null;
  private List<String> assignee = null;
  
  private List<OrderBy> orderBy = new ArrayList<OrderBy>();

  public TaskQuery() {

  }

  TaskQuery(AggregateCondition condition, List<OrderBy> orderBies, List<Long> projectIds, List<String> assignee) {
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

  public List<String> getAssignee() {
    return assignee;
  }

  public void setAssignee(List<String> assignee) {
    if (assignee != null) {
      this.add(in(TASK_ASSIGNEE, assignee));      
    }
    this.assignee = assignee;
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
    this.assignee = Arrays.asList(username);
    this.add(Conditions.or(eq(TASK_ASSIGNEE, username), in(TASK_MANAGER, memberships), in(TASK_PARTICIPATOR, memberships)));
  }

  public void setAssigneeOrInProject(String username, List<Long> projectIds) {
    this.assignee = Arrays.asList(username);
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
    setAssignee(Arrays.asList(username));
    //add(eq(TASK_ASSIGNEE, username));
  }

  public void setLabelIds(List<Long> labelIds) {
    if (labelIds != null) {
      List<Condition> cond = new LinkedList<Condition>();
      for (Long id : labelIds) {
        cond.add(eq(TASK_LABEL_ID, id));
      }
      this.add(Conditions.and(cond.toArray(new Condition[cond.size()])));
    }
  }
  
  public void setIsLabelOf(String username) {
    this.add(eq(TASK_LABEL_USERNAME, username));
  }

  public void setTags(List<String> tags) {
    if (tags != null) {
      List<Condition> cond = new LinkedList<Condition>();
      for (String tag : tags) {
        cond.add(eq(TASK_TAG, tag));
      }
      this.add(Conditions.and(cond.toArray(new Condition[cond.size()])));
    }
  }

  public void setPriority(Priority priority) {
    this.add(eq(TASK_PRIORITY, priority));
  }

  public void setNullField(String nullField) {
    add(isNull(nullField));
  }

  public void setEmptyField(String emptyField) {
    add(isEmpty(emptyField));
  }

  public TaskQuery clone() {
    return new TaskQuery(aggCondition.clone(), orderBy, projectIds != null ? new ArrayList<Long>(projectIds) : null, assignee);
  }

}

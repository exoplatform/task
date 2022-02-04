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

import org.exoplatform.services.security.Identity;
import org.exoplatform.task.dao.condition.AggregateCondition;
import org.exoplatform.task.dao.condition.Condition;
import org.exoplatform.task.dao.condition.Conditions;
import org.exoplatform.task.domain.Priority;
import org.exoplatform.task.domain.Status;
import org.exoplatform.task.dto.StatusDto;
import org.exoplatform.task.util.UserUtil;

import java.util.*;

import static org.exoplatform.task.dao.condition.Conditions.*;

/**
 * @author <a href="mailto:tuyennt@exoplatform.com">Tuyen Nguyen The</a>.
 */
public class TaskQuery extends Query implements Cloneable {
  //TODO: how to remove these two field
  private List<Long> projectIds = null;
  private List<String> assignee = null;
  private List<String> coworker = null;
  private List<String> watchers = null;

  public TaskQuery() {

  }

  TaskQuery(AggregateCondition condition, List<OrderBy> orderBies, List<Long> projectIds, List<String> assignee, List<String> coworker, List<String> watchers) {
    super(condition, orderBies);
    this.projectIds = projectIds;
    this.assignee = assignee;
    this.coworker= coworker;
    this.watchers= watchers;
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

  public List<String> getWatchers() {
    return watchers;
  }

  public void setWatchers(List<String> watchers) {
    if (watchers != null) {
      this.add(in(TASK_WATCHER, watchers));
    }
    this.watchers = watchers;
  }

  public void setCoworker(List<String> coworker) {
    if (coworker != null) {
      this.add(in(TASK_COWORKER, coworker));
    }
    this.coworker = coworker;
  }

  public void setAssigneeOrCoworker(List<String> assignee) {
    if (assignee != null && assignee.size() > 0) {
      this.add(Conditions.or(in(TASK_ASSIGNEE, assignee), in(TASK_COWORKER, assignee)));
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

  public void setCreatedBy(String creator) {
    add(eq(TASK_CREATOR, creator));
  }

  public void setMemberships(List<String> permissions) {
    add(Conditions.or(in(TASK_PARTICIPATOR, permissions), in(TASK_MANAGER, permissions)));
  }

  @Deprecated
  public void setAssigneeOrMembership(String username, List<String> memberships) {
    this.assignee = Arrays.asList(username);
    this.add(Conditions.or(eq(TASK_ASSIGNEE, username), in(TASK_MANAGER, memberships), in(TASK_PARTICIPATOR, memberships)));
  }
  
  public void setAccessible(Identity user) {
    this.assignee = Arrays.asList(user.getUserId());
    List<String> memberships = UserUtil.getMemberships(user);
    this.add(Conditions.or(eq(TASK_ASSIGNEE, assignee), eq(TASK_COWORKER, assignee), eq(TASK_CREATOR, assignee),
                           in(TASK_MANAGER, memberships), in(TASK_PARTICIPATOR, memberships),
                           in(TASK_MENTIONED_USERS, Arrays.asList(user.getUserId()))));
  }

  public void setAssigneeOrCoworkerOrInProject(String username, List<Long> projectIds) {
    this.assignee = Arrays.asList(username);
    this.projectIds = projectIds;
    this.add(Conditions.or(eq(TASK_ASSIGNEE, username), eq(TASK_COWORKER, username), in(TASK_PROJECT, projectIds)));
  }

  public void setStatus(Status status) {
    add(eq(TASK_STATUS, status));
  }

  public void setStatus(StatusDto status) {
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
    add(and(Conditions.or(
            eq(TASK_ASSIGNEE, username), eq(TASK_COWORKER, username), eq(TASK_CREATOR, username),
            in(TASK_MENTIONED_USERS, Arrays.asList(username))),
            isNull(TASK_STATUS)));
  }

  public void setIsTodoOf(String username) {
    //setAssignee(Arrays.asList(username));
    //add(eq(TASK_ASSIGNEE, username));
    this.add(Conditions.or(eq(TASK_ASSIGNEE, username), eq(TASK_COWORKER, username), in(TASK_MENTIONED_USERS, Arrays.asList(username))));
    this.assignee = Arrays.asList(username);
  }

  public void setAssigneeIsTodoOf(String username) {
    this.add(Conditions.or(eq(TASK_ASSIGNEE, username), eq(TASK_COWORKER, username)));
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
    Condition condition = getCondition();
    return new TaskQuery(condition != null ? (AggregateCondition)condition.clone() : null, getOrderBy(), projectIds != null ? new ArrayList<Long>(projectIds) : null, assignee,coworker,watchers);
  }

}

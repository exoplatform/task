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

package org.exoplatform.task.domain;

import javax.persistence.CascadeType;
import javax.persistence.CollectionTable;
import javax.persistence.Column;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.OneToMany;
import javax.persistence.PreRemove;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.exoplatform.commons.api.persistence.ExoEntity;
import org.exoplatform.task.service.TaskBuilder;

/**
 * @author <a href="mailto:tuyennt@exoplatform.com">Tuyen Nguyen The</a>
 */
@Entity
@ExoEntity
@Table(name = "TASK_TASKS")
@NamedQueries({
    @NamedQuery(name = "Task.findByMemberships",
        query = "SELECT ta FROM Task ta LEFT JOIN ta.coworker coworkers " +
            "WHERE ta.assignee = :userName " +
            "OR ta.createdBy = :userName " +
            "OR coworkers = :userName " +
            "OR ta.status IN (SELECT st.id FROM Status st " +
            "WHERE project IN " +
            "(SELECT pr1.id FROM Project pr1 LEFT JOIN pr1.manager managers WHERE managers IN :memberships) " +
            "OR project IN " +
            "(SELECT pr2.id FROM Project pr2 LEFT JOIN pr2.participator participators " +
            "WHERE participators IN :memberships) " +
            ") "),
    @NamedQuery(name = "Task.findTaskByProject",
        query = "SELECT t FROM Task t WHERE t.status.project.id = :projectId"),
    @NamedQuery(name = "Task.findTaskByActivityId",
        query = "SELECT t FROM Task t WHERE t.activityId = :activityId"),
    @NamedQuery(name = "Task.findTasksHasLabel",
            query = "SELECT t FROM Task t INNER JOIN t.labels lbl WHERE lbl.username = :username"),
    @NamedQuery(name = "Task.findTasksByLabel",
    query = "SELECT t FROM Task t INNER JOIN t.labels lbl WHERE lbl.id = :labelId")
})
public class Task {

  public static final String PREFIX_CLONE = "Copy of ";

  @Id
  @SequenceGenerator(name="SEQ_TASK_TASKS_TASK_ID", sequenceName="SEQ_TASK_TASKS_TASK_ID")
  @GeneratedValue(strategy=GenerationType.AUTO, generator="SEQ_TASK_TASKS_TASK_ID")
  @Column(name = "TASK_ID")
  private long        id;

  private String      title;

  private String      description;

  @Enumerated(EnumType.STRING)
  private Priority    priority;

  private String      context;

  private String      assignee;

  @ManyToOne
  @JoinColumn(name = "STATUS_ID")
  private Status      status;

  private int         rank;

  private boolean completed = false;
  
  @Column(name = "CALENDAR_INTEGRATED")
  private boolean calendarIntegrated = true;

  @ElementCollection
  @CollectionTable(name = "TASK_TASK_COWORKERS",
      joinColumns = @JoinColumn(name = "TASK_ID"))
  private Set<String> coworker = new HashSet<String>();

  @ElementCollection
  @CollectionTable(name = "TASK_TAGS",
      joinColumns = @JoinColumn(name = "TASK_ID"))
  private Set<String> tag = new HashSet<String>();

  @Column(name = "CREATED_BY")
  private String      createdBy;

  @Temporal(TemporalType.TIMESTAMP)
  @Column(name = "CREATED_TIME")
  private Date        createdTime;

  @Temporal(TemporalType.TIMESTAMP)
  @Column(name = "START_DATE")
  private Date        startDate;
  
  @Temporal(TemporalType.TIMESTAMP)
  @Column(name = "END_DATE")
  private Date        endDate;

  @Temporal(TemporalType.TIMESTAMP)
  @Column(name = "DUE_DATE")
  private Date        dueDate;

  @OneToMany(mappedBy = "task", cascade = CascadeType.ALL, orphanRemoval = true)
  private Set<Comment> comments = new HashSet<Comment>();
  
  @ElementCollection(fetch=FetchType.LAZY)
  @CollectionTable(name = "TASK_LOGS",
      joinColumns = @JoinColumn(name = "TASK_ID"))
  private Set<TaskLog> taskLogs = new HashSet<TaskLog>();
  
  @ManyToMany(fetch = FetchType.LAZY, mappedBy="tasks")
  private Set<Label> labels = new HashSet<Label>();

  @Column(name = "ACTIVITY_ID")
  private String activityId;

  public Task() {
  }

  public long getId() {
    return id;
  }

  public void setId(long id) {
    this.id = id;
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

  public Priority getPriority() {
    return priority;
  }

  public void setPriority(Priority priority) {
    this.priority = priority;
  }

  public String getContext() {
    return context;
  }

  public void setContext(String context) {
    this.context = context;
  }

  public String getAssignee() {
    return assignee;
  }

  public void setAssignee(String assignee) {
    this.assignee = assignee;
  }

  public Status getStatus() {
    return status;
  }

  public void setStatus(Status status) {
    this.status = status;
  }

  public int getRank() {
    return rank;
  }

  public void setRank(int rank) {
    this.rank = rank;
  }

  public boolean isCompleted() {
    return completed;
  }

  public void setCompleted(boolean completed) {
    this.completed = completed;
  }

  public boolean isCalendarIntegrated() {
    return calendarIntegrated;
  }

  public void setCalendarIntegrated(boolean calendarIntegrated) {
    this.calendarIntegrated = calendarIntegrated;
  }

  public Set<String> getTag() {
    return tag != null ? Collections.unmodifiableSet(tag) : Collections.<String> emptySet();
  }

  public void setTag(Set<String> tag) {
    this.tag = tag;
  }

  public void addTag(String tag) {
    if (this.tag == null) {
      this.tag = new HashSet<String>();
    }
    this.tag.add(tag);
  }

  public String getCreatedBy() {
    return createdBy;
  }

  public Set<Label> getLabels() {
    return labels;
  }

  public void setLabels(Set<Label> labels) {
    this.labels = labels;
  }

  public void setCreatedBy(String createdBy) {
    this.createdBy = createdBy;
  }

  public Date getCreatedTime() {
    return createdTime;
  }

  public void setCreatedTime(Date createdTime) {
    this.createdTime = createdTime;
  }

  public Date getEndDate() {
    return endDate;
  }

  public void setEndDate(Date endDate) {
    this.endDate = endDate;
  }

  public Date getStartDate() {
    return startDate;
  }

  public void setStartDate(Date startDate) {
    this.startDate = startDate;
  }

  public Date getDueDate() {
    return dueDate;
  }

  public void setDueDate(Date dueDate) {
    this.dueDate = dueDate;
  }

  public Set<String> getCoworker() {
    return coworker;
  }

  public void setCoworker(Set<String> coworker) {
    this.coworker = coworker;
  }

  //TODO: get comments of task via TaskService
  @Deprecated
  public Set<Comment> getComments() {
    return comments;
  }

  @Deprecated
  public void setComments(Set<Comment> comments) {
    this.comments = comments;
  }

  //TODO: get TaskLogs via TaskService
  @Deprecated
  public Set<TaskLog> getTaskLogs() {
    return taskLogs;
  }

  @Deprecated
  public void setTaskLogs(Set<TaskLog> taskLogs) {
    this.taskLogs = taskLogs;
  }

  public String getActivityId() {
    return activityId;
  }

  public void setActivityId(String activityId) {
    this.activityId = activityId;
  }

  public Task clone() {
    Task newTask = new TaskBuilder().withTitle(this.getTitle())
        .withAssignee(this.getAssignee())
        .withContext(this.getContext())
        .withCreatedBy(this.getCreatedBy())
        .withDescription(this.getDescription())
        .withDueDate(this.getDueDate())
        .withPriority(this.getPriority())
        .withStartDate(this.getStartDate())
        .withEndDate(this.getEndDate())
        .withStatus(this.getStatus() != null ? this.getStatus().clone() : null)
        .build();

    //
    Set<String> coworker = new HashSet<String>();
    if (this.getCoworker() != null) {
      coworker.addAll(getCoworker());
    }
    newTask.setCoworker(coworker);

    //
    Set<String> tags = new HashSet<String>();
    if (getTag() != null) {
      tags.addAll(getTag());
    }
    newTask.setTag(tags);

    newTask.setId(getId());

    return newTask;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;

    Task task = (Task) o;

    if (completed != task.completed) return false;
    if (id != task.id) return false;
    if (assignee != null ? !assignee.equals(task.assignee) : task.assignee != null) return false;
    if (comments != null ? !comments.equals(task.comments) : task.comments != null) return false;
    if (context != null ? !context.equals(task.context) : task.context != null) return false;
    if (coworker != null ? !coworker.equals(task.coworker) : task.coworker != null) return false;
    if (createdBy != null ? !createdBy.equals(task.createdBy) : task.createdBy != null) return false;
    if (createdTime != null ? !createdTime.equals(task.createdTime) : task.createdTime != null) return false;
    if (description != null ? !description.equals(task.description) : task.description != null) return false;
    if (dueDate != null ? !dueDate.equals(task.dueDate) : task.dueDate != null) return false;
    if (priority != task.priority) return false;
    if (startDate != null ? !startDate.equals(task.startDate) : task.startDate != null) return false;
    if (endDate != null ? !endDate.equals(task.endDate) : task.endDate != null) return false;
    if (status != null ? !status.equals(task.status) : task.status != null) return false;
    if (tag != null ? !tag.equals(task.tag) : task.tag != null) return false;
    if (title != null ? !title.equals(task.title) : task.title != null) return false;

    return true;
  }

  @PreRemove
  private void removeLabel() {
    if (getLabels() != null) {
      for (Label lbl : getLabels()) {
        if (lbl.getTasks() != null) {
          lbl.getTasks().remove(this);
        }
      }      
    }
  }
}

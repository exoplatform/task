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

import java.util.*;

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
import javax.persistence.ManyToOne;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.OneToMany;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import org.exoplatform.commons.api.persistence.ExoEntity;
import org.exoplatform.task.dto.StatusDto;
import org.exoplatform.task.service.TaskBuilder;

/**
 * @author <a href="mailto:tuyennt@exoplatform.com">Tuyen Nguyen The</a>
 */
@Entity(name = "TaskTask")
@ExoEntity
@Table(name = "TASK_TASKS")
@NamedQueries({
    @NamedQuery(name = "Task.findByMemberships",
        query = "SELECT ta FROM TaskTask ta LEFT JOIN ta.coworker coworkers " +
            "WHERE ta.assignee = :userName " +
            "OR ta.createdBy = :userName " +
            "OR coworkers = :userName " +
            "OR ta.status IN (SELECT st.id FROM TaskStatus st " +
            "WHERE project IN " +
            "(SELECT pr1.id FROM TaskProject pr1 LEFT JOIN pr1.manager managers WHERE managers IN :memberships) " +
            "OR project IN " +
            "(SELECT pr2.id FROM TaskProject pr2 LEFT JOIN pr2.participator participators " +
            "WHERE participators IN :memberships) " +
            ") "),
    @NamedQuery(name = "Task.findTaskByProject",
        query = "SELECT t FROM TaskTask t WHERE t.status.project.id = :projectId"),
    @NamedQuery(name = "Task.findTaskByActivityId",
        query = "SELECT t FROM TaskTask t WHERE t.activityId = :activityId"),
    @NamedQuery(name = "Task.getCoworker",
        query = "SELECT c FROM TaskTask t inner join t.coworker c WHERE t.id = :taskid"),
    @NamedQuery(name = "Task.getWatcher",
        query = "SELECT w FROM TaskTask t inner join t.watcher w WHERE t.id = :taskid"),
    @NamedQuery(name = "Task.updateStatus",
        query = "UPDATE TaskTask t SET t.status = :status_new WHERE t.status = :status_old"),
    @NamedQuery(name = "Task.getTaskWithCoworkers",
        query = "SELECT t FROM TaskTask t LEFT JOIN FETCH t.coworker c WHERE t.id = :taskid"),
    @NamedQuery(name = "Task.getUncompletedTasks",
        query = "SELECT ta FROM TaskTask ta " +
                "WHERE ta.completed = false " +
                "AND (ta.assignee = :userName " +
                "OR :userName in (select co FROM ta.coworker co) " +
                ")"),
    @NamedQuery(name = "Task.countUncompletedTasks",
        query = "SELECT COUNT(ta) FROM TaskTask ta " +
                "WHERE ta.completed = false " +
                "AND (ta.assignee = :userName " +
                "OR :userName in (select co FROM ta.coworker co) " +
                ")"),
    @NamedQuery(name = "Task.getCollaboratedTasks",
        query = "SELECT ta FROM TaskTask ta " +
                "WHERE ta.completed = false " +
                "AND :userName in (select co FROM ta.coworker co) "),
    @NamedQuery(name = "Task.countCollaboratedTasks",
        query = "SELECT COUNT(ta) FROM TaskTask ta " +
                "WHERE ta.completed = false " +
                "AND :userName in (select co FROM ta.coworker co) "),

   @NamedQuery(name = "Task.getAssignedTasks",
        query = "SELECT ta FROM TaskTask ta " +
                "WHERE ta.completed = false " +
                "AND ta.assignee = :userName "),

   @NamedQuery(name = "Task.countAssignedTasks",
        query = "SELECT COUNT(ta)  FROM TaskTask ta " +
                "WHERE ta.completed = false " +
                "AND ta.assignee = :userName "),

   @NamedQuery(name = "Task.getWatchedTasks",
        query = "SELECT ta FROM TaskTask ta " +
                "WHERE ta.completed = false " +
                "AND :userName in (select wa FROM ta.watcher wa) "),

   @NamedQuery(name = "Task.countWatchedTasks",
        query = "SELECT COUNT(ta)  FROM TaskTask ta " +
                "WHERE ta.completed = false " +
                "AND :userName in (select wa FROM ta.watcher wa) "),

    @NamedQuery(name = "Task.getOverdueTasks",
        query = "SELECT ta FROM TaskTask ta " +
                "WHERE ta.completed = false " +
                "AND ta.dueDate < CURDATE() " +
                "AND (ta.assignee = :userName " +
                "OR :userName in (select co FROM ta.coworker co) " +
                ")"),
    @NamedQuery(name = "Task.countOverdueTasks",
        query = "SELECT COUNT(ta) FROM TaskTask ta " +
                "WHERE ta.completed = false " +
                "AND ta.dueDate < CURDATE() " +
                "AND (ta.assignee = :userName " +
                "OR :userName in (select co FROM ta.coworker co) " +
                ")"),
    @NamedQuery(
        name = "Task.findTasks",
        query = "SELECT ta FROM TaskTask ta " +
            "WHERE (ta.assignee = :userName OR ta.createdBy = :userName OR :userName in (select co FROM ta.coworker co) OR (SELECT participator FROM TaskProject p LEFT JOIN p.participator participator where p.id = ta.status.project.id) IN (:memberships) ) " +
            "AND (lower(ta.title) LIKE lower(:term)  OR lower(ta.description) LIKE :term) " +
            "ORDER BY ta.createdTime DESC"
    ),
    @NamedQuery(
        name = "Task.countTasks",
        query = "SELECT COUNT(ta) FROM TaskTask ta " +
            "WHERE (ta.assignee = :userName OR ta.createdBy = :userName OR :userName in (select co FROM ta.coworker co)) " +
            "AND (lower(ta.title) LIKE lower(:term)  OR lower(ta.description) LIKE :term) "
    ),
        @NamedQuery(name = "Task.countTaskStatusByProject",
                query = "SELECT m.status.name AS name, COUNT(m) AS total FROM TaskTask AS m where m.status.project.id = :projectId GROUP BY m.status.name ORDER BY m.status.name ASC"),

        @NamedQuery(name = "Task.getByStatus",
                query = "SELECT t FROM TaskTask t  WHERE t.status.id = :statusid")
})
public class Task {

  public static final String PREFIX_CLONE = "Copy of ";

  @Id
  @SequenceGenerator(name="SEQ_TASK_TASKS_TASK_ID", sequenceName="SEQ_TASK_TASKS_TASK_ID", allocationSize = 1)
  @GeneratedValue(strategy=GenerationType.AUTO, generator="SEQ_TASK_TASKS_TASK_ID")
  @Column(name = "TASK_ID")
  private long        id;

  private String      title;

  private String      description;

  @Enumerated(EnumType.ORDINAL)
  private Priority    priority;

  private String      context;

  private String      assignee;

  @ManyToOne(fetch=FetchType.LAZY)
  @JoinColumn(name = "STATUS_ID")
  private Status      status;

  @Column(name = "TASK_RANK")
  private int         rank;

  private boolean completed = false;

  @ElementCollection(fetch = FetchType.LAZY)
  @CollectionTable(name = "TASK_TASK_COWORKERS",
      joinColumns = @JoinColumn(name = "TASK_ID"))
  private Set<String> coworker = new HashSet<String>();

  @ElementCollection(fetch = FetchType.LAZY)
  @CollectionTable(name = "TASK_TASK_WATCHERS",
      joinColumns = @JoinColumn(name = "TASK_ID"))
  private Set<String> watcher =  new HashSet<String>();

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

  //This field is only used for remove cascade
  @OneToMany(mappedBy = "task", fetch = FetchType.LAZY, cascade = CascadeType.REMOVE, orphanRemoval = true)
  private List<Comment> comments = new ArrayList<Comment>();

  //This field is only used for remove cascade
  @OneToMany(mappedBy = "task", fetch = FetchType.LAZY, cascade = CascadeType.REMOVE, orphanRemoval = true)
  private List<ChangeLog> logs = new ArrayList<ChangeLog>();

  @OneToMany(mappedBy = "task", fetch = FetchType.LAZY, cascade = CascadeType.REMOVE, orphanRemoval = true)
  private Set<LabelTaskMapping> lblMapping = new HashSet<LabelTaskMapping>();

  @Column(name = "ACTIVITY_ID")
  private String activityId;

  public Task() {
    this.priority = Priority.NORMAL;
  }



  public Task(String title, String description, Priority priority, String context, String assignee, Set<String> coworker, Set<String> watcher, Status status, String createdBy , Date endDate, Date startDate, Date dueDate) {
    this.title = title;
    this.assignee = assignee;
    this.coworker = coworker;
    this.watcher = watcher;
    this.context = context;
    this.createdBy = createdBy;
    this.description = description;
    this.priority = priority;
    this.startDate = startDate;
    this.endDate = endDate;
    this.dueDate = dueDate;
    this.status = status;
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

  public String getCreatedBy() {
    return createdBy;
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

  public Set<String> getWatcher() {
    return watcher;
  }

  public void setWatcher(Set<String> watcher) {
    this.watcher = watcher;
  }

  public String getActivityId() {
    return activityId;
  }

  public void setActivityId(String activityId) {
    this.activityId = activityId;
  }

  public Task clone() {
    Set<String> coworkerClone = new HashSet<String>();
    Set<String> watcherClone = new HashSet<String>();
    if (getCoworker() != null) {
      coworkerClone = new HashSet<String>(getCoworker());
    }
    if (getWatcher() != null) {
      watcherClone = new HashSet<String>(getWatcher());
    }
    Task newTask = new Task(this.getTitle(), this.getDescription(), this.getPriority(), this.getContext(), this.getAssignee(), coworkerClone, watcherClone, this.getStatus() != null ? this.getStatus().clone() : null, this.getCreatedBy() , this.getEndDate(), this.getStartDate(), this.getDueDate());

    newTask.setCreatedTime(getCreatedTime());
    newTask.setActivityId(getActivityId());
    newTask.setCompleted(isCompleted());
    newTask.setRank(getRank());
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
    if (context != null ? !context.equals(task.context) : task.context != null) return false;
    if (coworker != null ? !coworker.equals(task.coworker) : task.coworker != null) return false;
    if (watcher != null ? !watcher.equals(task.watcher) : task.watcher != null) return false;
    if (createdBy != null ? !createdBy.equals(task.createdBy) : task.createdBy != null) return false;
    if (createdTime != null ? !createdTime.equals(task.createdTime) : task.createdTime != null) return false;
    if (description != null ? !description.equals(task.description) : task.description != null) return false;
    if (dueDate != null ? !dueDate.equals(task.dueDate) : task.dueDate != null) return false;
    if (priority != task.priority) return false;
    if (startDate != null ? !startDate.equals(task.startDate) : task.startDate != null) return false;
    if (endDate != null ? !endDate.equals(task.endDate) : task.endDate != null) return false;
    if (status != null ? !status.equals(task.status) : task.status != null) return false;
    if (title != null ? !title.equals(task.title) : task.title != null) return false;

    return true;
  }

  @Override
  public int hashCode() {
    return Objects.hash(id, title, description, priority, context, assignee, status, completed, coworker,watcher,
            createdBy, createdTime, startDate, endDate, dueDate);
  }
}

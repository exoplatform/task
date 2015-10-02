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

import org.exoplatform.commons.api.persistence.ExoEntity;

import javax.persistence.*;
import java.util.HashSet;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.OneToMany;
import javax.persistence.Table;

/**
 * @author <a href="mailto:tuyennt@exoplatform.com">Tuyen Nguyen The</a>
 */
@Entity
@ExoEntity
@Table(name = "TASK_STATUS")
@NamedQueries({
    @NamedQuery(name = "Status.findLowestRankStatusByProject",
        query = "SELECT s FROM Status s WHERE s.rank = (SELECT MIN(s2.rank) FROM Status s2 " +
            "WHERE s2.project.id = :projectId) AND s.project.id = :projectId)"),
    @NamedQuery(name = "Status.findHighestRankStatusByProject",
            query = "SELECT s FROM Status s WHERE s.rank = (SELECT MAX(s2.rank) FROM Status s2 " +
                "WHERE s2.project.id = :projectId) AND s.project.id = :projectId)"),
    @NamedQuery(name = "Status.findByName",
                query = "SELECT s FROM Status s WHERE s.name = :name AND s.project.id = :projectID"),
    @NamedQuery(name = "Status.findStatusByProject",
                query = "SELECT s FROM Status s WHERE s.project.id = :projectId ORDER BY s.rank ASC")
})
public class Status implements Comparable<Status>{
  @Id
  @SequenceGenerator(name="SEQ_TASK_STATUS_STATUS_ID", sequenceName="SEQ_TASK_STATUS_STATUS_ID")
  @GeneratedValue(strategy=GenerationType.AUTO, generator="SEQ_TASK_STATUS_STATUS_ID")
  @Column(name = "STATUS_ID")
  private long id;

  private String name;

  private Integer rank;

  @OneToMany(mappedBy = "status", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
  private Set<Task> tasks = new HashSet<Task>();

  @ManyToOne
  @JoinColumn(name = "PROJECT_ID")
  private Project project;

  public Status() {
  }

  //TO REMOVE after removing the TaskServiceMemImpl
  public Status(long id, String name) {
    this.id = id;
    this.name = name;
  }
  public Status(long id, String name, Integer rank, Set<Task> tasks, Project project) {
    this.id = id;
    this.name = name;
    this.rank = rank;
    this.tasks = tasks;
    this.project = project;
  }

  public Status(String name, Integer rank, Set<Task> tasks, Project project) {
    this.name = name;
    this.rank = rank;
    this.tasks = tasks;
    this.project = project;
  }

  public long getId() {
    return id;
  }

  public void setId(long id) {
    this.id = id;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  //TODO: Get Tasks of status via TaskService
  @Deprecated
  public Set<Task> getTasks() {
    return tasks;
  }

  @Deprecated
  public void setTasks(Set<Task> tasks) {
    this.tasks = tasks;
  }

  public Integer getRank() {
    return rank;
  }

  public void setRank(Integer rank) {
    this.rank = rank;
  }

  public Project getProject() {
    return project;
  }

  public void setProject(Project project) {
    this.project = project;
  }

  public Status clone() {
    Status status = new Status(getId(), getName(), getRank(), null, getProject().clone(false));

    return status;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;

    Status status = (Status) o;

    if (id != status.id) return false;
    if (name != null ? !name.equals(status.name) : status.name != null) return false;
    if (project != null ? !project.equals(status.project) : status.project != null) return false;
    if (rank != null ? !rank.equals(status.rank) : status.rank != null) return false;
    if (tasks != null ? !tasks.equals(status.tasks) : status.tasks != null) return false;

    return true;
  }

  @Override
  public int compareTo(Status o) {
    if(getRank() == null) {
      return o.getRank() == null ? 0 : -1;
    } else if(o.getRank() == null) {
      return 1;
    }

    return getRank().compareTo(o.getRank());
  }
}
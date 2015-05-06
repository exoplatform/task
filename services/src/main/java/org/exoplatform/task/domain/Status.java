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

import javax.persistence.*;

import java.util.HashSet;
import java.util.Set;

/**
 * @author <a href="mailto:tuyennt@exoplatform.com">Tuyen Nguyen The</a>
 */
@Entity
@Table(name = "TASK_STATUS")
public class Status {

  public static final Status   INCOMING    = new Status(0, "INCOMING");

  public static final Status   TODO        = new Status(1, "TODO");

  public static final Status   IN_PROGRESS = new Status(2, "In Progress");

  public static final Status   WAITING_ON  = new Status(3, "Waiting on");

  public static final Status   DONE        = new Status(4, "Done");

  public static final Status[] STATUS      = { TODO, IN_PROGRESS, WAITING_ON, DONE };


  @Id
  @GeneratedValue
  @Column(name = "STATUS_ID")
  private long id;

  private String name;

  private Integer rank;

  @OneToMany(mappedBy = "status", cascade = CascadeType.ALL, orphanRemoval = true)
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

  public Set<Task> getTasks() {
    return tasks;
  }

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
  
  public Status clone(boolean cloneTask) {
    Status status = new Status(this.getName(), this.getRank(), new HashSet<Task>(), null);
    
    if (cloneTask) {
      if (this.getTasks() != null) {
        for (Task t : this.getTasks()) {
          Task cloned = t.clone();
          status.getTasks().add(cloned);
          cloned.setStatus(status);
        }
      }
    }
    return status;
  }
}

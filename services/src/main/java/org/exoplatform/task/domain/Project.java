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
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * @author <a href="mailto:tuyennt@exoplatform.com">Tuyen Nguyen The</a>
 */
@Entity
@Table(name = "TASK_PROJECTS")
@NamedQueries({
    @NamedQuery(name = "getRootProjects", query = "SELECT p FROM Project p WHERE p.parent.id = 0 OR p.parent is null"),
    @NamedQuery(name = "findSubProjects", query = "SELECT p FROM Project p WHERE p.parent.id = :projectId")
})
public class Project {

  @Id
  @GeneratedValue
  @Column(name = "PROJECT_ID")
  private long      id;

  private String    name;

  private String    description;

  private String    color;

  @OneToMany(mappedBy = "project", cascade = CascadeType.ALL, orphanRemoval = true)
  private Set<Status> status = new HashSet<Status>();

  @ElementCollection
  @CollectionTable(name = "TASK_PROJECT_MANAGERS",
      joinColumns = @JoinColumn(name = "PROJECT_ID"))
  private Set<String> manager = new HashSet<String>();

  @ElementCollection
  @CollectionTable(name = "TASK_PROJECT_PARTICIPATORS",
      joinColumns = @JoinColumn(name = "PROJECT_ID"))
  private Set<String> participator = new HashSet<String>();

  @Temporal(TemporalType.DATE)
  @Column(name = "DUE_DATE")
  private Date dueDate;

  @ManyToOne(optional = true, fetch = FetchType.LAZY)
  @JoinColumn(name = "PARENT_PROJECT_ID", nullable = true)
  private Project parent;

  @OneToMany(mappedBy = "parent", fetch = FetchType.LAZY)
  private List<Project> children;

  public Project() {
  }

  public Project(String name, String description, Set<Status> status, Set<String> manager, Set<String> participator) {
    this.name = name;
    this.description = description;
    this.status = status;
    this.manager = manager;
    this.participator = participator;
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

  public Set<Status> getStatus() {
    return status;
  }

  public void setStatus(Set<Status> status) {
    this.status = status;
  }

  public Set<String> getParticipator() {
    return participator;
  }

  public void setParticipator(Set<String> participator) {
    this.participator = participator;
  }

  public Set<String> getManager() {
    return manager;
  }

  public void setManager(Set<String> manager) {
    this.manager = manager;
  }

  public String getDescription() {
    return description;
  }

  public void setDescription(String description) {
    this.description = description;
  }

  public Date getDueDate() {
    return dueDate;
  }

  public void setDueDate(Date dueDate) {
    this.dueDate = dueDate;
  }

  public String getColor() {
    return color;
  }

  public void setColor(String color) {
    this.color = color;
  }

  public Project getParent() {
    return parent;
  }

  public void setParent(Project parent) {
    this.parent = parent;
  }

  public List<Project> getChildren() {
    return children;
  }

  public void setChildren(List<Project> children) {
    this.children = children;
  }
}

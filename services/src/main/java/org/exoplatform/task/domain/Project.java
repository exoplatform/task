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

import java.util.Date;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.CollectionTable;
import javax.persistence.Column;
import javax.persistence.ElementCollection;
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
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import org.exoplatform.services.security.Identity;
import org.exoplatform.services.security.MembershipEntry;

/**
 * @author <a href="mailto:tuyennt@exoplatform.com">Tuyen Nguyen The</a>
 */
@Entity
@Table(name = "TASK_PROJECTS")
@NamedQueries({
    @NamedQuery(name = "Project.getRootProjects",
        query = "SELECT p FROM Project p WHERE p.parent.id = 0 OR p.parent is null"),
    @NamedQuery(name = "Project.findSubProjects",
        query = "SELECT p FROM Project p WHERE p.parent.id = :projectId"),
    @NamedQuery(name = "Project.findAllByMembership",
        query = "SELECT p FROM Project p " +
            "  LEFT JOIN p.manager managers " +
            "  LEFT JOIN p.participator participators " +
            "WHERE managers in (:memberships) OR participators in (:memberships)"),
    @NamedQuery(name = "Project.findRootProjectsByMemberships",
        query = "SELECT p FROM Project p " +
            "  LEFT JOIN p.manager managers " +
            "  LEFT JOIN p.participator participators " +
            "WHERE (managers in (:memberships) OR participators in (:memberships)) " +
            "AND (p.parent.id = 0 OR p.parent is null)"),
    @NamedQuery(name = "Project.findSubProjectsByMemberships",
        query = "SELECT p FROM Project p " +
            "  LEFT JOIN p.manager managers " +
            "  LEFT JOIN p.participator participators " +
            "WHERE (managers in (:memberships) OR participators in (:memberships)) " +
            "AND p.parent.id = :projectId")
})
public class Project {

  private static final String PREFIX_CLONE = "Copy of ";

  @Id
  @GeneratedValue
  @Column(name = "PROJECT_ID")
  private long      id;

  private String    name;

  private String    description;

  private String    color;
  
  @Column(name = "CALENDAR_INTEGRATED")
  private boolean calendarIntegrated = false;

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

  @OneToMany(mappedBy = "parent", fetch = FetchType.LAZY, cascade=CascadeType.REMOVE)
  private List<Project> children = new LinkedList<Project>();

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

  public boolean isCalendarIntegrated() {
    return calendarIntegrated;
  }

  public void setCalendarIntegrated(boolean calendarIntegrated) {
    this.calendarIntegrated = calendarIntegrated;
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

  public Project clone(boolean cloneTask) {
    Project project = new Project(PREFIX_CLONE + this.getName(), this.getDescription(), new HashSet<Status>(),
        new HashSet<String>(this.getManager()), new HashSet<String>(this.getParticipator()));

    project.setColor(this.getColor());
    project.setDueDate(this.getDueDate());
    project.setParent(this.getParent());

    if (this.getStatus() != null) {
      for (Status st : this.getStatus()) {
        Status cloned = st.clone(cloneTask);
        project.getStatus().add(cloned);
        cloned.setProject(project);
      }
    }

    if (this.getChildren() != null) {
      for (Project p : this.getChildren()) {
        Project cloned = p.clone(cloneTask);
        project.getChildren().add(cloned);
        cloned.setParent(project);
      }
    }

    return project;
  }

  public boolean canView(Identity user) {
    Set<String> permissions = new HashSet<String>(getParticipator());
    permissions.addAll(getManager());

    return hasPermission(user, permissions);
  }

  public boolean canEdit(Identity user) {
    return hasPermission(user, getManager());
  }

  private boolean hasPermission(Identity user, Set<String> permissions) {
    if (permissions.contains(user.getUserId())) {
      return true;
    } else {
      Set<MembershipEntry> memberships = new HashSet<MembershipEntry>();
      for (String per : permissions) {
        MembershipEntry entry = MembershipEntry.parse(per);
        if (entry != null) {
          memberships.add(entry);
        }
      }

      for (MembershipEntry entry :  user.getMemberships()) {
        if (memberships.contains(entry)) {
          return true;
        }
      }
    }

    return false;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + (int) (getId() ^ (getId() >>> 32));
    return result;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj)
      return true;
    if (obj == null)
      return false;
    if (!(obj instanceof Project))
      return false;
    Project other = (Project) obj;
    if (getId() != other.getId())
      return false;
    return true;
  }

}

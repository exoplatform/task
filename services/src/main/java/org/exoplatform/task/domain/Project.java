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
import org.exoplatform.services.security.Identity;
import org.exoplatform.services.security.MembershipEntry;

import javax.persistence.*;
import java.util.*;

/**
 * @author <a href="mailto:tuyennt@exoplatform.com">Tuyen Nguyen The</a>
 */
@Entity
@ExoEntity
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

  public static final String PREFIX_CLONE = "Copy of ";

  @Id
  @SequenceGenerator(name="SEQ_TASK_PROJECTS_PROJECT_ID", sequenceName="SEQ_TASK_PROJECTS_PROJECT_ID")
  @GeneratedValue(strategy=GenerationType.AUTO, generator="SEQ_TASK_PROJECTS_PROJECT_ID")
  @Column(name = "PROJECT_ID")
  private long      id;

  private String    name;

  private String    description;

  private String    color;
  
  @Column(name = "CALENDAR_INTEGRATED")
  private boolean calendarIntegrated = false;

  //TODO: should remove cascade ALL on this field
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

  //TODO: get list status of project via StatusService
  @Deprecated
  public Set<Status> getStatus() {
    return status;
  }

  @Deprecated
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

  @Deprecated
  public List<Project> getChildren() {
    return children;
  }

  @Deprecated
  public void setChildren(List<Project> children) {
    this.children = children;
  }

  public Project clone(boolean cloneTask) {
    Set<String> manager = new HashSet<String>();
    Set<String> participator = new HashSet<String>();

    if (getManager() != null) {
      manager.addAll(getManager());
    }
    if (getParticipator() != null) {
      participator.addAll(getParticipator());
    }

    Project project = new Project(this.getName(), this.getDescription(), new HashSet<Status>(),
        manager, participator);

    project.setId(getId());
    project.setColor(this.getColor());
    project.setDueDate(this.getDueDate());
    if (this.getParent() != null) {
      project.setParent(getParent().clone(false));
    }
    project.setCalendarIntegrated(isCalendarIntegrated());

    //
    project.status = new HashSet<Status>();
    project.children = new LinkedList<Project>();

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

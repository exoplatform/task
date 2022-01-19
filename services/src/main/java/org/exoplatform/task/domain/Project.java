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
import org.exoplatform.task.util.ProjectUtil;

import javax.persistence.*;

import java.util.*;

/**
 * @author <a href="mailto:tuyennt@exoplatform.com">Tuyen Nguyen The</a>
 */
@Entity(name = "TaskProject")
@ExoEntity
@Table(name = "TASK_PROJECTS")
@NamedQueries({
  @NamedQuery(
          name = "TaskProject.findAllProjects",
          query = "SELECT DISTINCT p FROM TaskProject p ORDER BY p.lastModifiedDate DESC"
  ),
  @NamedQuery(
              name = "TaskProject.countAllProjects",
              query = "SELECT count(p) FROM TaskProject p"
  ),
  @NamedQuery(
              name = "TaskProject.findProjectsByKeyword",
              query = "SELECT DISTINCT p FROM TaskProject p WHERE LOWER(p.name) LIKE LOWER(CONCAT('%', :name, '%')) order by p.lastModifiedDate desc"
  ),
  @NamedQuery(
              name = "TaskProject.findProjectsByIDs",
              query = "SELECT DISTINCT p FROM TaskProject p WHERE p.id in (:ids) order by p.lastModifiedDate desc"
  ),
  @NamedQuery(
              name = "TaskProject.countProjectsByKeyword",
              query = "SELECT count(p) FROM TaskProject p WHERE LOWER(p.name) LIKE LOWER(CONCAT('%', :name, '%'))"
  ),
  @NamedQuery(
              name = "TaskProject.findProjectsByMemberships",
              query = "SELECT DISTINCT p FROM TaskProject p"
                  + " LEFT JOIN p.manager manager "
                  + " LEFT JOIN p.participator participator "
                  + " WHERE manager IN (:memberships) OR participator IN (:memberships)"
                  + " order by p.lastModifiedDate desc"
  ),
  @NamedQuery(
              name = "TaskProject.countProjectsByMemberships",
              query = "SELECT count(p) FROM TaskProject p"
                  + " LEFT JOIN p.manager manager "
                  + " LEFT JOIN p.participator participator "
                  + " WHERE manager IN (:memberships) OR participator IN (:memberships)"
  ),
  @NamedQuery(
              name = "TaskProject.findProjectsByMembershipsByKeyword",
              query = "SELECT DISTINCT p FROM TaskProject p "
                  + " LEFT JOIN p.manager manager "
                  + " LEFT JOIN p.participator participator "
                  + " WHERE (manager IN (:memberships) OR participator IN (:memberships)) AND LOWER(p.name) LIKE LOWER(CONCAT('%', :name, '%'))"
                  + " ORDER BY p.lastModifiedDate DESC"
  ),
  @NamedQuery(
              name = "TaskProject.countProjectsByMembershipsByKeyword",
              query = "SELECT count(p) FROM TaskProject p "
                  + " LEFT JOIN p.manager manager "
                  + " LEFT JOIN p.participator participator "
                  + " WHERE (manager IN (:memberships) OR participator IN (:memberships)) AND LOWER(p.name) LIKE LOWER(CONCAT('%', :name, '%'))"
  )
})
public class Project {

  public static final String PREFIX_CLONE = "Copy of ";

  @Id
  @SequenceGenerator(name="SEQ_TASK_PROJECTS_PROJECT_ID", sequenceName="SEQ_TASK_PROJECTS_PROJECT_ID", allocationSize = 1)
  @GeneratedValue(strategy=GenerationType.AUTO, generator="SEQ_TASK_PROJECTS_PROJECT_ID")
  @Column(name = "PROJECT_ID")
  private long      id;

  private String    name;

  private String    description;

  private String    color;

  //TODO: should remove cascade ALL on this field
  @OneToMany(mappedBy = "project", cascade = CascadeType.REMOVE, orphanRemoval = true)
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

  // This field is used for remove cascade
  @ManyToMany(mappedBy = "hiddenProjects", fetch = FetchType.LAZY)
  private Set<UserSetting> hiddenOn = new HashSet<UserSetting>();


  // This field is used for remove cascade
  @ManyToMany(mappedBy = "project", fetch = FetchType.LAZY)
  private Set<Label> lebels = new HashSet<Label>();


  @Column(name = "LAST_MODIFIED_DATE")
  private Long lastModifiedDate;

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

  @Deprecated
  public Set<String> getParticipator() {
    if (participator == null) {
      participator = ProjectUtil.getParticipator(getId());
    }
    return participator;
  }

  public void setParticipator(Set<String> participator) {
    this.participator = participator;
  }

  @Deprecated
  public Set<String> getManager() {
    if (manager == null) {
      manager = ProjectUtil.getManager(getId());
    }
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

  public Long getLastModifiedDate() {
    return lastModifiedDate;
  }

  public void setLastModifiedDate(Long lastModifiedDate) {
    this.lastModifiedDate = lastModifiedDate;
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
//    Set<String> manager = new HashSet<String>();
//    Set<String> participator = new HashSet<String>();

    //This create performance issue, we need this to be loaded lazily
//    if (getManager() != null) {
//      manager.addAll(getManager());
//    }
//    if (getParticipator() != null) {
//      participator.addAll(getParticipator());
//    }

    Project project = new Project(this.getName(), this.getDescription(), new HashSet<Status>(),
        null, null);

    project.setId(getId());
    project.setColor(this.getColor());
    project.setDueDate(this.getDueDate());
    if (this.getParent() != null) {
      project.setParent(getParent().clone(false));
    }

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

  public Set<UserSetting> getHiddenOn() {
    return hiddenOn;
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

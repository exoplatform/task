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
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.exoplatform.commons.api.persistence.ExoEntity;

@Entity(name = "TaskLabel")
@ExoEntity
@Table(name = "TASK_LABELS")
@NamedQueries({  
  @NamedQuery(name = "Label.findLabelsByTask",
      query = "SELECT lbl FROM TaskLabel lbl inner join lbl.lblMapping m WHERE lbl.project.id = :projectId AND m.task.id = :taskid ORDER BY lbl.id"),
      @NamedQuery(name = "Label.findLabelsByTaskCount",
      query = "SELECT count(*) FROM TaskLabel lbl inner join lbl.lblMapping m WHERE lbl.project.id = :projectId AND m.task.id = :taskid"),
  @NamedQuery(name = "Label.findLabelsByProject",
      query = "SELECT lbl FROM TaskLabel lbl WHERE lbl.project.id = :projectId ORDER BY lbl.id"),
      @NamedQuery(name = "Label.findLabelsByProjectCount",
      query = "SELECT count(*) FROM TaskLabel lbl WHERE lbl.project.id = :projectId ORDER BY lbl.id")
})
public class Label {
  @Id
  @SequenceGenerator(name="SEQ_TASK_LABELS_LABEL_ID", sequenceName="SEQ_TASK_LABELS_LABEL_ID", allocationSize = 1)
  @GeneratedValue(strategy=GenerationType.AUTO, generator="SEQ_TASK_LABELS_LABEL_ID")
  @Column(name = "LABEL_ID")
  private long      id;

  @Column(name = "USERNAME", nullable=false)
  private String username;

  private String    name;

  private String    color;
  
  private boolean hidden;
  
  public static enum FIELDS {
    NAME, COLOR, PARENT, HIDDEN
  }

  @ManyToOne(optional = true, fetch = FetchType.LAZY)
  @JoinColumn(name = "PARENT_LABEL_ID", nullable = true)
  private Label parent;

  @OneToMany(mappedBy = "parent", fetch = FetchType.LAZY)
  private List<Label> children = new LinkedList<Label>();
  
  //Still use for named query
  @OneToMany(mappedBy = "label", fetch=FetchType.LAZY)
  private Set<LabelTaskMapping> lblMapping = new HashSet<LabelTaskMapping>();

  @ManyToOne
  @JoinColumn(name = "PROJECT_ID")
  private Project project;

  public Label() {
  }

  public Label(String name, String username, Project project) {
    this.name = name;
    this.username = username;
    this.username = username;
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

  public String getColor() {
    return color;
  }

  public void setColor(String color) {
    this.color = color;
  }

  public Label getParent() {
    return parent;
  }

  public void setParent(Label parent) {
    this.parent = parent;
  }

  public List<Label> getChildren() {
    return children;
  }

  public void setChildren(List<Label> children) {
    this.children = children;
  }

  public String getUsername() {
    return username;
  }

  public void setUsername(String username) {
    this.username = username;
  }

  public Project getProject() {
    return project;
  }

  public void setProject(Project project) {
    this.project = project;
  }

  public boolean isHidden() {
    return hidden;
  }

  public void setHidden(boolean hidden) {
    this.hidden = hidden;
  }


  
}

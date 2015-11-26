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
      query = "SELECT lbl FROM TaskLabel lbl inner join lbl.lblMapping m WHERE lbl.username = :username AND m.task.id = :taskid"),
      @NamedQuery(name = "Label.findLabelsByTaskCount",
      query = "SELECT count(*) FROM TaskLabel lbl inner join lbl.lblMapping m WHERE lbl.username = :username AND m.task.id = :taskid")
})
public class Label {
  @Id
  @GeneratedValue
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

  public Label() {
  }

  public Label(String name, String username) {
    this.name = name;
    this.username = username;
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

  public boolean isHidden() {
    return hidden;
  }

  public void setHidden(boolean hidden) {
    this.hidden = hidden;
  }  
  
}

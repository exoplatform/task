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

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Table;

import java.io.Serializable;

import org.exoplatform.commons.api.persistence.ExoEntity;

@Entity(name = "TaskLabelTaskMapping")
@ExoEntity
@Table(name = "TASK_LABEL_TASK")
@NamedQueries({  
  @NamedQuery(name = "LabelTaskMapping.removeLabelTaskMapping",
      query = "DELETE FROM TaskLabelTaskMapping m WHERE m.label.id = :labelId"),
  @NamedQuery(name = "LabelTaskMapping.findLabelMapping",
       query = "SELECT m FROM TaskLabelTaskMapping m  WHERE m.label.id = :labelId and  m.task.id = :taskId"),
  @NamedQuery(name = "LabelTaskMapping.findLabelMappingByLabel",
       query = "SELECT m FROM TaskLabelTaskMapping m  WHERE m.label.id = :labelId")
})
public class LabelTaskMapping implements Serializable {
  @Id
  @ManyToOne
  @JoinColumn(name = "LABEL_ID")
  private Label label;
  @Id
  @ManyToOne
  @JoinColumn(name = "TASK_ID")
  private Task task;
  
  public LabelTaskMapping() {    
  }
  
  public LabelTaskMapping(Label label, Task task) {
    super();
    this.label = label;
    this.task = task;
  }

  public Label getLabel() {
    return label;
  }
  
  public void setLabel(Label label) {
    this.label = label;
  }
  
  public Task getTask() {
    return task;
  }
  
  public void setTask(Task task) {
    this.task = task;
  }
  
}

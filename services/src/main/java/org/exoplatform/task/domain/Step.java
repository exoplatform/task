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
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import javax.persistence.*;

import lombok.Data;
import org.exoplatform.commons.api.persistence.ExoEntity;

/**
 * @author <a href="mailto:tuyennt@exoplatform.com">Tuyen Nguyen The</a>.
 */
@Entity(name = "TaskStep")
@ExoEntity
@Data
@Table(name = "TASK_STEP")
@NamedQueries({
    @NamedQuery(name = "Step.countStepOfTask",
        query = "SELECT count(c) FROM TaskStep c WHERE c.task.id = :taskId"),
    @NamedQuery(name = "Step.findStepsOfTask",
        query = "SELECT c FROM TaskStep c WHERE c.task.id = :taskId  ORDER BY c.createdTime DESC"),
    @NamedQuery(name = "Step.deleteStepOfTask",
        query = "DELETE FROM TaskStep c WHERE c.task.id = :taskId")
})
public class Step {

  private static final Pattern pattern = Pattern.compile("@([^\\s]+)|@([^\\s]+)$");

  @Id
  @SequenceGenerator(name="SEQ_TASK_Step_ID", sequenceName="SEQ_TASK_Step_ID")
  @GeneratedValue(strategy=GenerationType.AUTO, generator="SEQ_TASK_Step_ID")
  @Column(name = "STEP_ID")
  private long id;

  @Column(name = "AUTHOR")
  private String author;

  @Column(name = "NAME")
  private String name;

  @Column(name = "COMPLETED")
  private boolean completed = false;

  @Temporal(TemporalType.TIMESTAMP)
  @Column(name = "CREATED_TIME")
  private Date createdTime;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "TASK_ID")
  private Task task;

}

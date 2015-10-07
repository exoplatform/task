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
import java.util.Date;

/**
 * @author <a href="mailto:tuyennt@exoplatform.com">Tuyen Nguyen The</a>.
 */
@Entity
@ExoEntity
@Table(name = "TASK_COMMENTS")
@NamedQueries({
    @NamedQuery(name = "Comment.countCommentOfTask",
        query = "SELECT count(c) FROM Comment c WHERE c.task.id = :taskId"),
    @NamedQuery(name = "Comment.findCommentsOfTask",
        query = "SELECT c FROM Comment c WHERE c.task.id = :taskId ORDER BY c.createdTime DESC"),
    @NamedQuery(name = "Comment.deleteCommentOfTask",
        query = "DELETE FROM Comment c WHERE c.task.id = :taskId")
})
public class Comment {
  @Id
  @SequenceGenerator(name="SEQ_TASK_COMMENTS_COMMENT_ID", sequenceName="SEQ_TASK_COMMENTS_COMMENT_ID")
  @GeneratedValue(strategy=GenerationType.AUTO, generator="SEQ_TASK_COMMENTS_COMMENT_ID")
  @Column(name = "COMMENT_ID")
  private long id;

  private String author;

  @Column(name = "CMT")
  private String comment;

  @Temporal(TemporalType.TIMESTAMP)
  @Column(name = "CREATED_TIME")
  private Date createdTime;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "TASK_ID")
  private Task task;

  public Comment() {}

  public long getId() {
    return id;
  }

  public void setId(long id) {
    this.id = id;
  }

  public String getAuthor() {
    return author;
  }

  public void setAuthor(String author) {
    this.author = author;
  }

  public String getComment() {
    return comment;
  }

  public void setComment(String comment) {
    this.comment = comment;
  }

  public Date getCreatedTime() {
    return createdTime;
  }

  public void setCreatedTime(Date createdTime) {
    this.createdTime = createdTime;
  }

  public Task getTask() {
    return task;
  }

  public void setTask(Task task) {
    this.task = task;
  }

  @Override
  public Comment clone() {
    Comment c = new Comment();
    c.setId(getId());
    c.setAuthor(getAuthor());
    c.setComment(getComment());
    c.setCreatedTime(getCreatedTime());
    c.setTask(getTask().clone());
    return c;
  }
}

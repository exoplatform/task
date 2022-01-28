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

import org.exoplatform.commons.api.persistence.ExoEntity;

/**
 * @author <a href="mailto:tuyennt@exoplatform.com">Tuyen Nguyen The</a>.
 */
@Entity(name = "TaskComment")
@ExoEntity
@Table(name = "TASK_COMMENTS")
@NamedQueries({
    @NamedQuery(name = "Comment.countCommentOfTask",
        query = "SELECT count(c) FROM TaskComment c WHERE c.task.id = :taskId AND c.parentComment IS NULL"),
    @NamedQuery(name = "Comment.findCommentsOfTask",
        query = "SELECT c FROM TaskComment c WHERE c.task.id = :taskId AND c.parentComment IS NULL ORDER BY c.createdTime DESC"),
    @NamedQuery(name = "Comment.findSubCommentsOfComments",
      query = "SELECT c FROM TaskComment c WHERE c.parentComment IN (:comments) ORDER BY c.createdTime ASC"),
    @NamedQuery(name = "Comment.deleteCommentOfTask",
        query = "DELETE FROM TaskComment c WHERE c.task.id = :taskId"),
    @NamedQuery(name = "Comment.findMentionedUsersOfTask",
                query = "SELECT m FROM TaskComment c INNER JOIN c.mentionedUsers m WHERE c.task.id = :taskId")
})
public class Comment {

  private static final Pattern pattern = Pattern.compile("@([^\\s]+)|@([^\\s]+)$");

  @Id
  @SequenceGenerator(name="SEQ_TASK_COMMENTS_COMMENT_ID", sequenceName="SEQ_TASK_COMMENTS_COMMENT_ID", allocationSize = 1)
  @GeneratedValue(strategy=GenerationType.AUTO, generator="SEQ_TASK_COMMENTS_COMMENT_ID")
  @Column(name = "COMMENT_ID")
  private long id;

  @Column(name = "AUTHOR")
  private String author;

  @Column(name = "CMT")
  private String comment;

  @Temporal(TemporalType.TIMESTAMP)
  @Column(name = "CREATED_TIME")
  private Date createdTime;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "TASK_ID")
  private Task task;

  @ManyToOne(fetch = FetchType.EAGER)
  @JoinColumn(name = "PARENT_COMMENT_ID")
  private Comment       parentComment;

  @OneToMany(cascade = CascadeType.REMOVE, mappedBy = "parentComment", fetch = FetchType.LAZY)
  private List<Comment> subComments = new ArrayList<Comment>();

  @ElementCollection(fetch = FetchType.LAZY)
  @CollectionTable(name = "TASK_COMMENT_MENTIONED_USERS",
          joinColumns = @JoinColumn(name = "COMMENT_ID"))
  @Column(name = "MENTIONED_USERS")
  private Set<String> mentionedUsers = new HashSet<>();

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
    
    this.parseMentionedUsers(comment);
  }

  public Set<String> getMentionedUsers() {
    return mentionedUsers.stream().collect(Collectors.toSet());
  }

  private void parseMentionedUsers(String comment) {
    Set<String> ment = new HashSet<>();

    if (comment != null && !comment.isEmpty()) {
      Matcher matcher = pattern.matcher(comment);

      // Replace all occurrences of pattern in input
      StringBuffer buf = new StringBuffer();
      while (matcher.find()) {
        // Get the match result
        String username = matcher.group().substring(1);
        if (username == null || username.isEmpty()) {
          continue;
        }
        ment.add(username.trim());
      }
    }

    this.mentionedUsers = ment;
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

  public Comment getParentComment() {
    return parentComment;
  }

  public void setParentComment(Comment parentComment) {
    this.parentComment = parentComment;
    this.parentComment.addSubComment(this);
  }

  public List<Comment> getSubComments() {
    return subComments;
  }

  public void setSubComments(List<Comment> subComments) {
    this.subComments = subComments;
  }

  public void addSubComment(Comment subComment) {
    this.subComments.add(subComment);
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

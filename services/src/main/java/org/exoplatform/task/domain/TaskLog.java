/**
 * Copyright (C) 2015 eXo Platform SAS.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Affero General Public License
 * as published by the Free Software Foundation; either version 3
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, see<http://www.gnu.org/licenses/>.
 **/
  
package org.exoplatform.task.domain;

import org.exoplatform.commons.api.persistence.ExoEntity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;

@Entity
@ExoEntity
@Table(name = "TASK_LOGS")
@NamedQueries({
        @NamedQuery(name = "TaskChangeLog.findChangeLogByTaskId",
                    query = "SELECT log FROM TaskLog log WHERE log.task.id = :taskId ORDER BY log.createdTime DESC"),
        @NamedQuery(name = "TaskChangeLog.countChangeLogByTaskId",
                query = "SELECT count(log) FROM TaskLog log WHERE log.task.id = :taskId"),
        @NamedQuery(name = "TaskChangeLog.removeChangeLogByTaskId",
                query = "DELETE FROM TaskLog log WHERE log.task.id = :taskId")
})
//TODO Should be renamed to ChangeLog which is more meaning
public class TaskLog implements Comparable<TaskLog> {

  @Id
  @SequenceGenerator(name="SEQ_TASK_CHANGE_LOG_ID", sequenceName="SEQ_TASK_CHANGE_LOG_ID")
  @GeneratedValue(strategy= GenerationType.AUTO, generator="SEQ_TASK_CHANGE_LOG_ID")
  @Column(name = "CHANGE_LOG_ID")
  private long id;

  @ManyToOne
  @JoinColumn(name = "TASK_ID")
  private Task task;
  
  private String author;

  //TODO It could be a kind of LogType with the integer datatype
  private String msg;

  private String target;

  @Column(name="CREATED_TIME")
  private long createdTime = System.currentTimeMillis();

  public long getId() {
    return id;
  }

  public void setId(long id) {
    this.id = id;
  }

  public Task getTask() {
    return task;
  }

  public void setTask(Task task) {
    this.task = task;
  }

  public String getAuthor() {
    return author;
  }

  public void setAuthor(String author) {
    this.author = author;
  }

  public String getMsg() {
    return msg;
  }

  public void setMsg(String msg) {
    this.msg = msg;
  }

  public String getTarget() {
    return target;
  }

  public void setTarget(String target) {
    this.target = target;
  }

  public long getCreatedTime() {
    return createdTime;
  }

  public void setCreatedTime(long createdTime) {
    this.createdTime = createdTime;
  }

  @Override
  public int compareTo(TaskLog o) {
    return (int)(getCreatedTime() - o.getCreatedTime());
  }

  @Override
  public TaskLog clone() {
    TaskLog log = new TaskLog();
    log.setTask(getTask().clone());
    log.setAuthor(getAuthor());
    log.setMsg(getMsg());
    log.setCreatedTime(getCreatedTime());

    return log;
  }
}

/* 
* Copyright (C) 2003-2015 eXo Platform SAS.
*
* This program is free software: you can redistribute it and/or modify
* it under the terms of the GNU Lesser General Public License as published by
* the Free Software Foundation, either version 3 of the License, or
* (at your option) any later version.
*
* This program is distributed in the hope that it will be useful,
* but WITHOUT ANY WARRANTY; without even the implied warranty of
* MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
* GNU Lesser General Public License for more details.
*
* You should have received a copy of the GNU Lesser General Public License
* along with this program. If not, see http://www.gnu.org/licenses/ .
*/
package org.exoplatform.task.service.impl;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.persistence.TypedQuery;

import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.exoplatform.commons.api.persistence.ExoTransactional;
import org.exoplatform.commons.utils.ListAccess;
import org.exoplatform.services.listener.ListenerService;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.task.dao.DAOHandler;
import org.exoplatform.task.dao.OrderBy;
import org.exoplatform.task.dao.TaskQuery;
import org.exoplatform.task.domain.Comment;
import org.exoplatform.task.domain.Label;
import org.exoplatform.task.domain.LabelTaskMapping;
import org.exoplatform.task.domain.Status;
import org.exoplatform.task.domain.Task;
import org.exoplatform.task.domain.ChangeLog;
import org.exoplatform.task.exception.EntityNotFoundException;
import org.exoplatform.task.service.TaskPayload;
import org.exoplatform.task.service.TaskService;

/**
 * Created by The eXo Platform SAS
 * Author : Thibault Clement
 * tclement@exoplatform.com
 * 6/3/15
 */
@Singleton
public class TaskServiceImpl implements TaskService {

  private static final Log LOG = ExoLogger.getExoLogger(TaskServiceImpl.class);

  @Inject
  private DAOHandler daoHandler;

  private ListenerService listenerService;
  
  public TaskServiceImpl(DAOHandler daoHandler, ListenerService listenerService) {
    this.daoHandler = daoHandler;
    this.listenerService = listenerService;
  }

  @Override
  @ExoTransactional
  public Task createTask(Task task) {
    Task result = daoHandler.getTaskHandler().create(task);
    //
    TaskPayload event = new TaskPayload(null, result);
    try {
      listenerService.broadcast(TASK_CREATION, this, event);
    } catch (Exception e) {
      LOG.error("Error while broadcasting task creation event", e);
    }

    return result;
  }
  
  @Override
  @ExoTransactional
  public Task updateTask(Task task) {
    if (task == null) {
      throw new IllegalArgumentException("Task must not be NULL");
    }

    Task oldTask = daoHandler.getTaskHandler().find(task.getId());
    Task newTask = daoHandler.getTaskHandler().update(task);
    TaskPayload event = new TaskPayload(oldTask, newTask);
    try {
      listenerService.broadcast(TASK_UPDATE, this, event);
    } catch (Exception e) {
      LOG.error("Error while broadcasting task creation event", e);
    }
 
    return newTask;
  }

  @Override
  @ExoTransactional
  public void updateTaskOrder(long currentTaskId, Status newStatus, long[] orders) {
      daoHandler.getTaskHandler().updateTaskOrder(currentTaskId, newStatus, orders);
  }

  @Override
  @ExoTransactional
  public void removeTask(long id) throws EntityNotFoundException {
    Task task = getTask(id);// Can throw TaskNotFoundException

    daoHandler.getTaskHandler().delete(task);
  }

  @Override
  @ExoTransactional
  public Task cloneTask(long id) throws EntityNotFoundException {

    Task task = getTask(id);// Can throw TaskNotFoundException

    Task newTask = task.clone();
    newTask.setId(0L);
    newTask.setCoworker(getCoworker(id));
    newTask.setTag(getTag(id));
    newTask.setTitle(Task.PREFIX_CLONE + newTask.getTitle());

    return createTask(newTask);
  }

  @Override
  public Task getTask(long id) throws EntityNotFoundException {
    Task task = daoHandler.getTaskHandler().find(id);
    if (task == null) {
      LOG.info("Can not find task with ID: " + id);
      throw new org.exoplatform.task.exception.EntityNotFoundException(id, Task.class);
    }
    return task;
  }

  @Override
  public Comment getComment(long commentId) {
    return daoHandler.getCommentHandler().find(commentId);
  }

  @Override
  public ListAccess<Comment> getComments(long taskId) {
    return daoHandler.getCommentHandler().findComments(taskId);
  }

  @Override
  @ExoTransactional
  public Comment addComment(long id, String username, String comment) throws EntityNotFoundException {

    Task task = getTask(id); //Can throws TaskNotFoundException

    Comment newComment = new Comment();
    newComment.setTask(task);
    newComment.setAuthor(username);
    newComment.setComment(comment);
    newComment.setCreatedTime(new Date());
    Comment obj = daoHandler.getCommentHandler().create(newComment);

    try {
      listenerService.broadcast(TASK_COMMENT_CREATION, this, obj);
    } catch (Exception e) {
      LOG.error("Error while broadcasting task creation event", e);
    }

    return obj;
  }
  
  @Override
  @ExoTransactional
  public ChangeLog addTaskLog(long id, String username, String actionName, String target) throws EntityNotFoundException {
    ChangeLog log = new ChangeLog();
    log.setTask(getTask(id));
    log.setAuthor(username);
    log.setActionName(actionName);
    log.setTarget(target);
    return daoHandler.getTaskLogHandler().create(log);
  }

  @Override
  public ListAccess<ChangeLog> getTaskLogs(long taskId) {
    return daoHandler.getTaskLogHandler().findTaskLogs(taskId);
  }

  @Override
  @ExoTransactional
  public void removeComment(long commentId) throws EntityNotFoundException {

    Comment comment = daoHandler.getCommentHandler().find(commentId);

    if(comment == null) {
      LOG.info("Can not find comment with ID: " + commentId);
      throw new EntityNotFoundException(commentId, Comment.class);
    }

    daoHandler.getCommentHandler().delete(comment);
  }

  @Override
  public ListAccess<Task> findTasks(TaskQuery query) {
    return daoHandler.getTaskHandler().findTasks(query);
  }

  @Override
  public <T> List<T> selectTaskField(TaskQuery query, String fieldName) {
    return daoHandler.getTaskHandler().selectTaskField(query, fieldName);
  }

  @Override
  @ExoTransactional
  public void addTaskToLabel(Long taskId, Long labelId) throws EntityNotFoundException {
    LabelTaskMapping mapping = new LabelTaskMapping();
    mapping.setLabel(getLabel(labelId));
    mapping.setTask(getTask(taskId));
    daoHandler.getLabelTaskMappingHandler().create(mapping);
  }
  
  @Override
  @ExoTransactional
  public void removeTaskFromLabel(Long taskId, Long labelId) throws EntityNotFoundException {
    LabelTaskMapping mapping = new LabelTaskMapping();
    mapping.setLabel(getLabel(labelId));
    mapping.setTask(getTask(taskId));
    daoHandler.getLabelTaskMappingHandler().delete(mapping);
  }

  @Override
  public ListAccess<Task> findTasksByLabel(long labelId, List<Long> projectIds, String username, OrderBy orderBy) throws EntityNotFoundException {
    if (labelId > 0) {
      Label label = getLabel(labelId);
      if (label == null) {
        throw new EntityNotFoundException(labelId, Label.class);
      }
    }
    return daoHandler.getTaskHandler().findTasksByLabel(labelId, projectIds, username, orderBy);
  }

  @Override
  public ListAccess<Label> findLabelsByUser(String username) {
    return daoHandler.getLabelHandler().findLabelsByUser(username);
  }

  @Override
  public ListAccess<Label> findLabelsByTask(long taskId, String username) throws EntityNotFoundException {
    return daoHandler.getLabelHandler().findLabelsByTask(taskId, username);
  }

  @Override
  public Label getLabel(long labelId) {
    return daoHandler.getLabelHandler().find(labelId);
  }

  @Override
  @ExoTransactional
  public Label createLabel(Label label) {
    return daoHandler.getLabelHandler().create(label);
  }

  @Override
  @ExoTransactional
  public Label updateLabel(Label label, List<Label.FIELDS> fields) throws EntityNotFoundException {
    Label lb = getLabel(label.getId());
    if (lb == null) {
      throw new EntityNotFoundException(label.getId(), Label.class);
    }
    
    //Todo: validate input and throw exception if need
    for (Label.FIELDS field : fields) {
      switch (field) {
      case NAME:
        lb.setName(label.getName());
        break;
      case COLOR:
        lb.setColor(label.getColor());
        break;
      case PARENT:      
        lb.setParent(label.getParent());
        break;
      case HIDDEN:
        lb.setHidden(label.isHidden());
      }
    }
    return daoHandler.getLabelHandler().update(lb);
  }

  @Override
  @ExoTransactional
  public void removeLabel(long labelId) {
    daoHandler.getLabelHandler().delete(getLabel(labelId));
  }

  public Task findTaskByActivityId(String id) {
    return daoHandler.getTaskHandler().findTaskByActivityId(id);
  }

  @Override
  public Set<String> getTag(long taskId) {
    return daoHandler.getTaskHandler().getTag(taskId);
  }

  @Override
  public Set<String> getCoworker(long taskId) { 
    return daoHandler.getTaskHandler().getCoworker(taskId);
  }

  @Override
  public ListAccess<String> findTags(String keyword) {
    return daoHandler.getTaskHandler().findTags(keyword);
  }
}
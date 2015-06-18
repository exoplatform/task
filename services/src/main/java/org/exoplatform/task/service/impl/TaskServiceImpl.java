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

import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.task.dao.OrderBy;
import org.exoplatform.task.domain.Comment;
import org.exoplatform.task.domain.Priority;
import org.exoplatform.task.domain.Status;
import org.exoplatform.task.domain.Task;
import org.exoplatform.task.exception.CommentNotFoundException;
import org.exoplatform.task.exception.ParameterEntityException;
import org.exoplatform.task.exception.StatusNotFoundException;
import org.exoplatform.task.exception.TaskNotFoundException;
import org.exoplatform.task.service.DAOHandler;
import org.exoplatform.task.service.TaskService;

import javax.inject.Inject;
import javax.inject.Singleton;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

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

  public TaskServiceImpl() {
  }

  //For testing purpose only
  public TaskServiceImpl(DAOHandler daoHandler) {
    this.daoHandler = daoHandler;
  }

  @Override
  public Task createTask(Task task) {
    return daoHandler.getTaskHandler().create(task);
  }

  @Override
  public Task updateTask(Task task) {
    return daoHandler.getTaskHandler().update(task);
  }

  @Override
  public Task updateTaskInfo(long id, String param, String[] values)
      throws TaskNotFoundException, ParameterEntityException, StatusNotFoundException {

    Task task = getTaskById(id);

    if(task == null) {
      LOG.info("Can not find task with ID: " + id);
      throw new TaskNotFoundException(id);
    }

    if ("workPlan".equalsIgnoreCase(param)) {
      if (values == null) {
        task.setStartDate(null);
        task.setDuration(0);
      } else {
        if (values.length != 2) {
          LOG.error("workPlan updating lack of params");
        }

        try {
          Calendar dateFrom = Calendar.getInstance();
          dateFrom.setTimeInMillis(Long.parseLong(values[0]));
          Calendar dateTo = Calendar.getInstance();
          dateTo.setTimeInMillis(Long.parseLong(values[1]));

          task.setStartDate(dateFrom.getTime());
          task.setDuration(dateTo.getTimeInMillis() - dateFrom.getTimeInMillis());
        } catch (NumberFormatException ex) {
          LOG.info("Can parse date time value: "+values[0]+" or "+values[1]+" for Task with ID: "+id);
          throw new ParameterEntityException(id, "Task", param, values[0]+" or "+values[1],
              "cannot be parse to date", ex);
        }
      }
    } else {
      String value = values != null && values.length > 0 ? values[0] : null;

      if("title".equalsIgnoreCase(param)) {
        task.setTitle(value);
      } else if("dueDate".equalsIgnoreCase(param)) {
        if(value == null || value.trim().isEmpty()) {
          task.setDueDate(null);
        } else {
          DateFormat df = new SimpleDateFormat("yyyy-MM-dd");
          try {
            Date date = df.parse(value);
            task.setDueDate(date);
          } catch (ParseException ex) {
            LOG.info("Can parse date time value: "+value+" for Task with ID: "+id);
            throw new ParameterEntityException(id, "Task", param, value, "cannot be parse to date", ex);
          }
        }
      } else if("status".equalsIgnoreCase(param)) {
        try {
          Long statusId = Long.parseLong(value);
          Status status = daoHandler.getStatusHandler().find(statusId);
          if(status == null) {
            LOG.info("Status does not exist with ID: " + value);
            throw new StatusNotFoundException(id);
          }
          task.setStatus(status);

        } catch (NumberFormatException ex) {
          LOG.info("Status is unacceptable: "+value+" for Task with ID: "+id);
          throw new ParameterEntityException(id, "Task", param, value, "is unacceptable", ex);
        }
      } else if("description".equalsIgnoreCase(param)) {
        task.setDescription(value);
      } else if("completed".equalsIgnoreCase(param)) {
        task.setCompleted(Boolean.parseBoolean(value));
      } else if("assignee".equalsIgnoreCase(param)) {
        task.setAssignee(value);
      } else if("coworker".equalsIgnoreCase(param)) {
        Set<String> coworker = new HashSet<String>();
        for(String v : values) {
          coworker.add(v);
        }
        task.setCoworker(coworker);
      } else if("tags".equalsIgnoreCase(param)) {
        Set<String> tags = new HashSet<String>();
        for(String t : values) {
          tags.add(t);
        }
        task.setTags(tags);
      } else if ("priority".equalsIgnoreCase(param)) {
        Priority priority = Priority.valueOf(value);
        task.setPriority(priority);
      } else {
        LOG.info("Field name: " + param + " is not supported for entity Task");
        throw new ParameterEntityException(id, "Task", param, value, "is not supported for the entity Task", null);
      }
    }

    return updateTask(task);

  }

  @Override
  public Task updateTaskCompleted(long id, Boolean completed)
      throws TaskNotFoundException, ParameterEntityException, StatusNotFoundException {

    String[] values = new String[1];
    values[0] = completed.toString();
    return updateTaskInfo(id, "completed", values);
  }

  @Override
  public void deleteTask(Task task) {
    daoHandler.getTaskHandler().delete(task);
  }

  @Override
  public void deleteTaskById(long id) throws TaskNotFoundException {

    Task task = getTaskById(id);// Can throw TaskNotFoundException

    deleteTask(task);

  }

  @Override
  public Task cloneTaskById(long id) throws TaskNotFoundException {

    Task task = getTaskById(id);// Can throw TaskNotFoundException

    Task newTask = task.clone();

    return createTask(newTask);
  }

  @Override
  public Task getTaskById(long id) throws TaskNotFoundException {
    Task task = daoHandler.getTaskHandler().find(id);
    if (task == null) {
      LOG.info("Can not find task with ID: " + id);
      throw new TaskNotFoundException(id);
    }
    return task;
  }

  @Override
  public Long getNbOfCommentsByTask(Task task) {
    return daoHandler.getCommentHandler().count(task);
  }

  @Override
  public List<Comment> getCommentsByTaskId(long id, int start, int limit) throws TaskNotFoundException {

    Task task = getTaskById(id); //Can throws TaskNotFoundException

    return getCommentsByTask(task, start, limit);
  }

  @Override
  public List<Comment> getCommentsByTask(Task task, int start, int limit) {
    return daoHandler.getCommentHandler().findCommentsOfTask(task, start, limit);
  }

  @Override
  public Comment addCommentToTaskId(long id, String username, String comment) throws TaskNotFoundException {

    Task task = getTaskById(id); //Can throws TaskNotFoundException

    Comment newComment = new Comment();
    newComment.setTask(task);
    newComment.setAuthor(username);
    newComment.setComment(comment);
    newComment.setCreatedTime(new Date());

    return daoHandler.getCommentHandler().create(newComment);

  }

  @Override
  public void deleteCommentById(long commentId) throws CommentNotFoundException {

    Comment comment = daoHandler.getCommentHandler().find(commentId);

    if(comment == null) {
      LOG.info("Can not find comment with ID: " + commentId);
      throw new CommentNotFoundException(commentId);
    }

    daoHandler.getCommentHandler().delete(comment);

  }

  @Override
  public List<Task> getIncomingTasksByUser(String username, OrderBy orderBy) {
    return daoHandler.getTaskHandler().getIncomingTask(username, orderBy);
  }

  @Override
  public List<Task> getToDoTasksByUser(String username, OrderBy orderBy) {
    return daoHandler.getTaskHandler().getToDoTask(username, orderBy);
  }
  
  @Override
  public long getTaskNum(String username, Long projectId) {
    return daoHandler.getTaskHandler().getTaskNum(username, projectId);
  }
}
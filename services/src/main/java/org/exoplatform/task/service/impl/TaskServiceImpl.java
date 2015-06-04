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
import org.exoplatform.task.domain.Status;
import org.exoplatform.task.domain.Task;
import org.exoplatform.task.service.DAOHandler;
import org.exoplatform.task.service.TaskBuilder;
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

  @Override
  public Task createTask(Task task) {
    return daoHandler.getTaskHandler().create(task);
  }

  @Override
  public Task updateTask(Task task) {
    return daoHandler.getTaskHandler().update(task);
  }

  @Override
  public Task updateTaskInfo(long id, String param, String[] values) {

    String value = values != null && values.length > 0 ? values[0] : null;

    Task task = getTaskById(id);

    if(task == null) {
      LOG.info("Can not find task with ID: " + id);
      //TODO return exception instead of null
      //return Response.notFound("Task not found with ID: " + taskId);
      return null;
    }
    if("title".equalsIgnoreCase(param)) {
      task.setTitle(value);
    } else if("dueDate".equalsIgnoreCase(param)) {
      if(value == null || value.trim().isEmpty()) {
        task.setDueDate(null);
      } else {
        DateFormat df = new SimpleDateFormat("YYYY-MM-dd");
        try {
          Date date = df.parse(value);
          task.setDueDate(date);
        } catch (ParseException ex) {
          LOG.info("Can parse date time value: " + value);
          //TODO return exception instead of null
          //return Response.status(406).body("Can not parse date time value: " + val);
          return null;
        }
      }
    } else if("status".equalsIgnoreCase(param)) {
      try {
        Long statusId = Long.parseLong(value);
        Status status = daoHandler.getStatusHandler().find(statusId);
        if(status == null) {
          LOG.info("Status does not exist with ID: " + value);
          //TODO return exception instead of null
          //return Response.notFound("Status does not exist with ID: " + val);
          return null;
        }
        task.setStatus(status);

        // Task is completed if this status is last one
        boolean isLast = true;
        for(Status s : status.getProject().getStatus()) {
          if (s.getId() != status.getId() && s.getRank() > status.getRank()) {
            isLast = false;
            break;
          }
        }
        if(isLast) {
          task.setCompleted(true);
        } else {
          task.setCompleted(false);
        }

      } catch (NumberFormatException ex) {
        LOG.info("Status is unacceptable: " + value);
        //TODO return exception instead of null
        //return Response.status(406).body("Status is unacceptable: " + val);
        return null;
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
    } else {
      LOG.info("Field name: " + param + " is not supported");
      //TODO return exception instead of null
      //return Response.status(406).body("Field name: " + name + " is not supported");
      return null;
    }

    return updateTask(task);

  }

  @Override
  public Task updateTaskCompleted(long id, Boolean completed) {
    String[] values = new String[1];
    values[0] = completed.toString();
    return updateTaskInfo(id, "completed", values);
  }

  @Override
  public void deleteTask(Task task) {
    daoHandler.getTaskHandler().delete(task);
  }

  @Override
  public boolean deleteTaskById(long id) {
    //TODO return exception in case task does not exist instead of boolean (change method signature to void)

    Task task = getTaskById(id);

    if(task == null) {
      LOG.info("Can not find task with ID: " + id);
      return false;
    }

    deleteTask(task);

    return true;
  }

  @Override
  public Task cloneTaskById(long id) {

    Task task = getTaskById(id);

    if(task == null) {
      LOG.info("Can not find task with ID: " + id);
      //TODO return exception instead of null
      return null;
    }

    Task newTask = new TaskBuilder()
        .withTitle(task.getTitle())
        .withAssignee(task.getAssignee())
        .withContext(task.getContext())
        .withCreatedBy(task.getCreatedBy())
        .withDescription(task.getDescription())
        .withDueDate(task.getDueDate())
        .withPriority(task.getPriority())
        .withStartDate(task.getStartDate())
        .withDuration(task.getDuration())
        .withStatus(task.getStatus())
        .build();
    newTask.setCoworker(task.getCoworker());
    newTask.setTags(task.getTags());

    return createTask(newTask);
  }

  @Override
  public Task getTaskById(long id) {
    return daoHandler.getTaskHandler().find(id);
  }

  @Override
  public Long getNbOfCommentsByTaskId(long id) {

    Task task = getTaskById(id);

    if (task == null) {
      LOG.info("Can not find task with ID: " + id);
      //TODO return exception instead of null
      //return Response.notFound("Task does not exist with ID: " + taskId);
      return null;
    }

    return getNbOfCommentsByTask(task);
  }

  @Override
  public Long getNbOfCommentsByTask(Task task) {
    return daoHandler.getCommentHandler().count(task);
  }

  @Override
  public List<Comment> getCommentsByTaskId(long id, int start, int limit) {

    Task task = getTaskById(id);

    if (task == null) {
      LOG.info("Can not find task with ID: " + id);
      //TODO return exception instead of null
      //return Response.notFound("Task does not exist with ID: " + taskId);
      return null;
    }

    //TODO refactor after returning exception instead of null
    List<Comment> comments = getCommentsByTask(task, start, limit);
    if (comments == null) comments = new ArrayList<Comment>();

    return comments;
  }

  @Override
  public List<Comment> getCommentsByTask(Task task, int start, int limit) {
    return daoHandler.getCommentHandler().findCommentsOfTask(task, start, limit);
  }

  @Override
  public Comment addCommentToTaskId(long id, String username, String comment) {

    Task task = getTaskById(id);
    if (task == null) {
      LOG.info("Can not find task with ID: " + id);
      //TODO return exception instead of null
      //return Response.status(404).body("There is no task with ID: " + taskId);
      return null;
    }

    Comment newComment = new Comment();
    newComment.setTask(task);
    newComment.setAuthor(username);
    newComment.setComment(comment);
    newComment.setCreatedTime(new Date());

    return daoHandler.getCommentHandler().create(newComment);

  }

  @Override
  public boolean deleteCommentById(long commentId) {
    //TODO return exception in case comment does not exist instead of boolean (change method signature to void)

    Comment comment = daoHandler.getCommentHandler().find(commentId);

    if(comment == null) {
      LOG.info("Can not find comment with ID: " + commentId);
      return false;
    }

    daoHandler.getCommentHandler().delete(comment);

    return true;
  }

  @Override
  public List<Task> getIncomingTasksByUser(String username, OrderBy orderBy) {
    return daoHandler.getTaskHandler().getIncomingTask(username, orderBy);
  }

  @Override
  public List<Task> getToDoTasksByUser(String username, OrderBy orderBy) {
    return daoHandler.getTaskHandler().getToDoTask(username, orderBy);
  }
}


package org.exoplatform.task.service;

import org.exoplatform.task.dao.OrderBy;
import org.exoplatform.task.domain.Comment;
import org.exoplatform.task.domain.Task;
import org.exoplatform.task.exception.CommentNotFoundException;
import org.exoplatform.task.exception.ParameterEntityException;
import org.exoplatform.task.exception.StatusNotFoundException;
import org.exoplatform.task.exception.TaskNotFoundException;

import java.util.Date;
import java.util.List;

/**
 * Created by TClement on 6/3/15.
 */
public interface TaskService {

  Task createTask(Task task);

  Task updateTask(Task task);

  Task updateTaskInfo(long id, String param, String[] values)
      throws TaskNotFoundException, ParameterEntityException, StatusNotFoundException;

  Task updateTaskCompleted(long id, Boolean completed)
      throws TaskNotFoundException, ParameterEntityException, StatusNotFoundException;

  void deleteTask(Task task);

  void deleteTaskById(long id) throws TaskNotFoundException;

  Task cloneTaskById(long id) throws TaskNotFoundException;

  Task getTaskById(long id) throws TaskNotFoundException;

  Long getNbOfCommentsByTask(Task task);

  List<Comment> getCommentsByTaskId(long id, int start, int limit) throws TaskNotFoundException;

  List<Comment> getCommentsByTask(Task task, int start, int limit);

  Comment addCommentToTaskId(long id, String username, String comment) throws TaskNotFoundException;

  void deleteCommentById(long commentId) throws CommentNotFoundException;

  List<Task> getIncomingTasksByUser(String username, OrderBy orderBy);

  List<Task> getToDoTasksByUser(String username, OrderBy orderBy, Date fromDueDate, Date toDueDate);

  long getTaskNum(String username, Long projectId);
}

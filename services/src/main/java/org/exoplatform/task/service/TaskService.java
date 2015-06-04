package org.exoplatform.task.service;

import org.exoplatform.task.dao.OrderBy;
import org.exoplatform.task.domain.Comment;
import org.exoplatform.task.domain.Task;

import java.util.List;

/**
 * Created by TClement on 6/3/15.
 */
public interface TaskService {

  Task createTask(Task task);

  Task updateTask(Task task);

  Task updateTaskInfo(long id, String param, String[] values);

  Task updateTaskCompleted(long id, Boolean completed);

  void deleteTask(Task task);

  boolean deleteTaskById(long id);

  Task cloneTaskById(long id);

  Task getTaskById(long id);

  Long getNbOfCommentsByTaskId(long id);

  Long getNbOfCommentsByTask(Task task);

  List<Comment> getCommentsByTaskId(long id, int start, int limit);

  List<Comment> getCommentsByTask(Task task, int start, int limit);

  Comment addCommentToTaskId(long id, String username, String comment);

  boolean deleteCommentById(long commentId);

  List<Task> getIncomingTasksByUser(String username, OrderBy orderBy);

  List<Task> getToDoTasksByUser(String username, OrderBy orderBy);


}

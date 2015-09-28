package org.exoplatform.task.service;

import java.util.Date;
import java.util.List;
import java.util.TimeZone;

import org.exoplatform.task.dao.OrderBy;
import org.exoplatform.task.dao.TaskQuery;
import org.exoplatform.task.domain.Comment;
import org.exoplatform.task.domain.Label;
import org.exoplatform.task.domain.Status;
import org.exoplatform.task.domain.Task;
import org.exoplatform.task.domain.TaskLog;
import org.exoplatform.task.exception.CommentNotFoundException;
import org.exoplatform.task.exception.LabelNotFoundException;
import org.exoplatform.task.exception.ParameterEntityException;
import org.exoplatform.task.exception.StatusNotFoundException;
import org.exoplatform.task.exception.TaskNotFoundException;

import javax.persistence.criteria.Order;

/**
 * Created by TClement on 6/3/15.
 */
public interface TaskService {

  Task createTask(Task task);

  Task updateTaskInfo(long id, String param, String[] values, TimeZone timezone, String username)
      throws TaskNotFoundException, ParameterEntityException, StatusNotFoundException, LabelNotFoundException;

  void updateTaskOrder(long currentTaskId, Status newStatus, long[] orders);

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

  List<Task> getToDoTasksByUser(String username, List<Long> projectIds, OrderBy orderBy, Date fromDueDate, Date toDueDate);

  List<Task> findTasksByLabel(long labelId, String username, OrderBy orderBy) throws LabelNotFoundException;
  
  List<Task> findTaskByQuery(TaskQuery query);
  
  long getTaskNum(String username, List<Long> projectIds);

  TaskLog addTaskLog(long taskId, String username, String msg, String target) throws TaskNotFoundException;
  
  List<Label> findLabelsByUser(String username);
  
  List<Label> findLabelsByTask(long taskId, String username) throws TaskNotFoundException;
  
  Label getLabelById(long labelId);
  
  Label createLabel(Label label);
  
  Label updateLabel(Label label, List<Label.FIELDS> fields) throws LabelNotFoundException;

  void deleteLabel(long labelId);
}

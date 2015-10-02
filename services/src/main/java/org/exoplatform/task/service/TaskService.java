package org.exoplatform.task.service;

import java.util.List;

import org.exoplatform.commons.utils.ListAccess;
import org.exoplatform.task.dao.OrderBy;
import org.exoplatform.task.dao.TaskQuery;
import org.exoplatform.task.domain.Comment;
import org.exoplatform.task.domain.Label;
import org.exoplatform.task.domain.Status;
import org.exoplatform.task.domain.Task;
import org.exoplatform.task.domain.TaskLog;
import org.exoplatform.task.exception.EntityNotFoundException;

/**
 * Created by TClement on 6/3/15.
 */
public interface TaskService {

  String TASK_CREATION = "exo.task.taskCreation";

  String TASK_UPDATE = "exo.task.taskUpdate";

  Task createTask(Task task);

  Task updateTask(Task task);

  void updateTaskOrder(long currentTaskId, Status newStatus, long[] orders);

  void removeTask(long taskId) throws EntityNotFoundException;

  Task cloneTask(long taskId) throws EntityNotFoundException;

  Task getTask(long taskId) throws EntityNotFoundException;

  ListAccess<Comment> getComments(long taskId);

  Comment addComment(long taskId, String username, String commentText) throws EntityNotFoundException;

  void removeComment(long commentId) throws EntityNotFoundException;

  ListAccess<Task> findTasks(TaskQuery query);

  public <T> List<T> selectTaskField(TaskQuery query, String fieldName);

  List<Task> findTasksByLabel(long labelId, List<Long> projectIds, String username, OrderBy orderBy) throws EntityNotFoundException;  

  /**
   * Create a log associated with a task with given <code>taskId</code>.
   * 
   * @param taskId
   * @param username
   * @param msg
   * @param target
   * @return
   * @throws EntityNotFoundException
   */
  TaskLog addTaskLog(long taskId, String username, String msg, String target) throws EntityNotFoundException;
  
  List<Label> findLabelsByUser(String username);
  
  List<Label> findLabelsByTask(long taskId, String username) throws EntityNotFoundException;
  
  Label getLabelById(long labelId);
  
  Label createLabel(Label label);
  
  Label updateLabel(Label label, List<Label.FIELDS> fields) throws EntityNotFoundException;

  void removeLabel(long labelId);

  ListAccess<TaskLog> getTaskLogs(long taskId);

  //TODO: should use via #findTasks(TaskQuery)?
  Task findTaskByActivityId(String activityId);
}

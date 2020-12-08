package org.exoplatform.task.service;

import org.exoplatform.commons.utils.ListAccess;
import org.exoplatform.services.security.Identity;
import org.exoplatform.task.dao.OrderBy;
import org.exoplatform.task.dao.TaskQuery;
import org.exoplatform.task.domain.Priority;
import org.exoplatform.task.domain.Status;
import org.exoplatform.task.dto.ChangeLogEntry;
import org.exoplatform.task.dto.LabelDto;
import org.exoplatform.task.dto.TaskDto;
import org.exoplatform.task.dto.TasksList;
import org.exoplatform.task.exception.EntityNotFoundException;
import org.exoplatform.task.util.TaskUtil;

import java.util.List;
import java.util.Set;
import java.util.TimeZone;


public interface TaskService {

    String TASK_CREATION = "exo.task.taskCreation";

    String TASK_UPDATE = "exo.task.taskUpdate";


    /**
     * Create a new task.
     *
     * @param task the given task.
     * @return the created task.
     */
    TaskDto createTask(TaskDto task);

    /**
     * Update the task.
     *
     * @param task the given task.
     * @return the updated task.
     */
    TaskDto updateTask(TaskDto task);

    void updateTaskOrder(long currentTaskId, Status newStatus, long[] orders);

    /**
     * Remove the task with given <code>taskId</code>
     *
     * @param taskId the given task id.
     * @throws EntityNotFoundException when user is not authorized to remove task
     */
    void removeTask(long taskId) throws EntityNotFoundException;

    /**
     * Clone the task from a task with given <code>taskId</code>.
     */
    TaskDto cloneTask(long taskId) throws EntityNotFoundException;

    /**
     * Return the task with given <code>taskId</code>.
     *
     * @param taskId
     * @return the given task.
     * @throws EntityNotFoundException when user is not authorized to get task.
     */
    TaskDto getTask(long taskId) throws EntityNotFoundException;


    List<TaskDto> getWatchedTasks(String user, int limit);

    Long countWatchedTasks(String user);

    List<TaskDto> getAssignedTasks(String user, int limit);

    Long countAssignedTasks(String user);

    List<TaskDto> getCollaboratedTasks(String user, int limit);

    Long countCollaboratedTasks(String user);

    List<TaskDto> findTasks(String user, String query, int limit);


    int countTasks(TaskQuery query) throws Exception;

    public <T> List<T> selectTaskField(TaskQuery query, String fieldName);

    List<TaskDto> findTasksByLabel(LabelDto label, List<Long> projectIds, String username, OrderBy orderBy, int offset, int limit) throws Exception;

    int countTasksByLabel(LabelDto label, List<Long> projectIds, String username, OrderBy orderBy) throws Exception;

    /**
     * Create a log associated with a task with given <code>taskId</code>.
     *
     * @param taskId     the given task id.
     * @param username   the given username.
     * @param actionName the given action name.
     * @param target     the given target.
     * @return add task log.
     * @throws EntityNotFoundException when user is not authorized to add taskLog.
     */
    ChangeLogEntry addTaskLog(long taskId, String username, String actionName, String target) throws EntityNotFoundException;


    List<ChangeLogEntry> getTaskLogs(long taskId, int offset, int limit) throws Exception;

    //TODO: should use via #findTasks(TaskQuery)?
    TaskDto findTaskByActivityId(String activityId);

    Set<String> getCoworker(long taskId);


    Set<String> getMentionedUsers(long taskId);

    List<TaskDto> getUncompletedTasks(String user, int limit);

    Long countUncompletedTasks(String user);

    List<TaskDto> getIncomingTasks(String user, int offset, int limit) throws Exception;

    int countIncomingTasks(String user) throws Exception;

    List<TaskDto> getOverdueTasks(String user, int limit);

    Long countOverdueTasks(String user);

    Set<String> getWatchersOfTask(TaskDto task);

    void addWatcherToTask(String username, TaskDto task) throws Exception;

    void deleteWatcherOfTask(String username, TaskDto task) throws Exception;

    /**
     * Find tasks assigned to a user using a term to find in title or description
     * of the task
     *
     * @param query term to search in title or description
     * @param limit term to limit results.
     * @return {@link List} of {@link TaskDto}
     */
    List<TaskDto> findTasks(TaskQuery query, int offset, int limit) throws Exception;

    /**
     * Count tasks assigned to a user using a search term to find in title or
     * description of the task
     *
     * @param user  username
     * @param query term to search in title or description
     * @return tasks count
     */
    long countTasks(String user, String query);

    List<Object[]> countTaskStatusByProject(long projectId);

    public TasksList filterTasks(String query, long projectId, String keyword, List<Long> labels, TaskUtil.DUE dueDate, Priority priority, List<String> assignees, List<String> coworkers, List<String> watchers, Long labelId, Long statusId, Identity currIdentity, String dueCategory, String space_group_id , TimeZone userTimezone, boolean isShowCompleted, boolean advanceSearch, boolean noProjPermission, boolean noLblPermission, String orderBy, String groupBy, int offset, int limit) throws Exception;
    }

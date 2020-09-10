package org.exoplatform.task.storage;

import org.exoplatform.commons.utils.ListAccess;
import org.exoplatform.task.dao.OrderBy;
import org.exoplatform.task.dao.TaskQuery;
import org.exoplatform.task.domain.ChangeLog;
import org.exoplatform.task.domain.Status;
import org.exoplatform.task.domain.Task;
import org.exoplatform.task.dto.ChangeLogEntry;
import org.exoplatform.task.dto.LabelDto;
import org.exoplatform.task.dto.TaskDto;
import org.exoplatform.task.exception.EntityNotFoundException;

import java.util.List;
import java.util.Set;

public interface TaskStorage {

    /**
     * Find task by its id
     *
     * @param id the given id.
     * @return {@link TaskDto}
     */
    TaskDto getTaskById(long id);

    /**
     * Create a new task.
     *
     * @param task the given task
     * @return {@link TaskDto}
     */
    TaskDto createTask(TaskDto task);

    /**
     * Update the provided task.
     *
     * @param task the given task.
     * @return {@link TaskDto}
     */
    TaskDto update(TaskDto task);

    /**
     * Delete the provided task.
     *
     * @param task the provided task.
     */
    void delete(TaskDto task);

    List<TaskDto> findTasksByLabel(LabelDto label,
                                   List<Long> projectIds,
                                   String username,
                                   OrderBy orderBy,
                                   int offset,
                                   int limit) throws Exception;

    int countTasksByLabel(LabelDto label, List<Long> projectIds, String username, OrderBy orderBy) throws Exception;

    List<TaskDto> findByUser(String user);

    List<TaskDto> findTasks(TaskQuery query, int offset, int limit) throws Exception;

    ListAccess<TaskDto> findTasks(TaskQuery query);

    int countTasks(TaskQuery query) throws Exception;

    <T> List<T> selectTaskField(TaskQuery query, String fieldName);

    TaskDto findTaskByActivityId(String activityId);

    void updateStatus(Status stOld, Status stNew);

    void updateTaskOrder(long currentTaskId, Status newStatus, long[] orders);

    Set<String> getCoworker(long taskid);

    TaskDto getTaskWithCoworkers(long id);

    List<TaskDto> getUncompletedTasks(String user, int limit);

    Long countUncompletedTasks(String user);

    List<TaskDto> getIncomingTasks(String user, int offset, int limit) throws Exception;

    int countIncomingTasks(String user) throws Exception;

    List<TaskDto> getOverdueTasks(String user, int limit);

    Long countOverdueTasks(String user);

    void addWatcherToTask(String username, TaskDto task) throws Exception;

    void deleteWatcherOfTask(String username, TaskDto task) throws Exception;

    Set<String> getWatchersOfTask(TaskDto task);

    /**
     * Find tasks assigned to a user using a term to find in title or description of
     * the task
     *
     * @param user  username
     * @param query term to search in title or description
     * @param limit term to limit results.
     * @return {@link List} of {@link TaskDto}
     */
    List<TaskDto> findTasks(String user, String query, int limit);

    /**
     * Count tasks assigned to a user using a search term to find in title or
     * description of the task
     *
     * @param user  username
     * @param query term to search in title or description
     * @return tasks count
     */
    long countTasks(String user, String query);

    /**
     * Create a log associated with a task with given <code>changeLogEntry</code>.
     *
     * @param changeLogEntry changeLogEntry
     * @return Create a log associated
     * @throws EntityNotFoundException when user is not authorized to add taskLog.
     */
    ChangeLogEntry addTaskLog(ChangeLogEntry changeLogEntry) throws EntityNotFoundException;

    ListAccess<ChangeLogEntry> getTaskLogs(long taskId);

    ChangeLog changeLogToEntity(ChangeLogEntry changeLogEntry);

    ChangeLogEntry changeLogToDto(ChangeLog changeLog);

    Task toEntity(TaskDto taskDto);

    TaskDto toDto(Task taskEntity);
}

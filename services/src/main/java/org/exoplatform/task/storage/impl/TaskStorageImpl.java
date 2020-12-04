package org.exoplatform.task.storage.impl;

import org.exoplatform.commons.utils.ListAccess;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.task.dao.DAOHandler;
import org.exoplatform.task.dao.OrderBy;
import org.exoplatform.task.dao.TaskQuery;
import org.exoplatform.task.domain.ChangeLog;
import org.exoplatform.task.domain.Status;
import org.exoplatform.task.domain.Task;
import org.exoplatform.task.dto.ChangeLogEntry;
import org.exoplatform.task.dto.LabelDto;
import org.exoplatform.task.dto.TaskDto;
import org.exoplatform.task.exception.EntityNotFoundException;
import org.exoplatform.task.legacy.service.UserService;
import org.exoplatform.task.storage.TaskStorage;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class TaskStorageImpl implements TaskStorage {

    private static final Log LOG = ExoLogger.getExoLogger(TaskStorageImpl.class);

    private static final Pattern pattern = Pattern.compile("@([^\\s]+)|@([^\\s]+)$");


    @Inject
    private final DAOHandler daoHandler;

    @Inject
    private final UserService userService;


    public TaskStorageImpl(DAOHandler daoHandler, UserService userService) {
        this.daoHandler = daoHandler;
        this.userService = userService;
    }

    @Override
    public TaskDto getTaskById(long id) {
        return toDto(daoHandler.getTaskHandler().find(id));
    }

    @Override
    public TaskDto createTask(TaskDto task) {
        return toDto(daoHandler.getTaskHandler().create(toEntity(task)));
    }

    @Override
    public TaskDto update(TaskDto task) {
        return toDto(daoHandler.getTaskHandler().update(toEntity(task)));
    }

    @Override
    public void delete(TaskDto task) {
        daoHandler.getTaskHandler().delete(toEntity(task));
    }

    @Override
    public List<TaskDto> findTasksByLabel(LabelDto label, List<Long> projectIds, String username, OrderBy orderBy, int offset, int limit) throws Exception {
        List<Task> taskEntities = Arrays.asList(daoHandler.getTaskHandler().findTasksByLabel(label.getId(), projectIds, username, orderBy).load(offset, limit));
        return taskEntities.stream().map(this::toDto).collect(Collectors.toList());
    }

    @Override
    public int countTasksByLabel(LabelDto label, List<Long> projectIds, String username, OrderBy orderBy) throws Exception {
        return daoHandler.getTaskHandler().findTasksByLabel(label.getId(), projectIds, username, orderBy).getSize();
    }

    @Override
    public List<TaskDto> findByUser(String user) {
        List<String> memberships = new ArrayList<String>();
        memberships.add(user);
        List<Task> taskEntities = daoHandler.getTaskHandler().findAllByMembership(user, memberships);
        return taskEntities.stream().map(this::toDto).collect(Collectors.toList());
    }

    @Override
    public List<TaskDto> findTasks(TaskQuery query, int offset, int limit) throws Exception {
        List<Task> taskEntities = Arrays.asList(daoHandler.getTaskHandler().findTasks(query).load(offset, limit));
        return taskEntities.stream().map(this::toDto).collect(Collectors.toList());
    }

    public int countTasks(TaskQuery query) throws Exception {
        return daoHandler.getTaskHandler().findTasks(query).getSize();
    }

    @Override
    public <T> List<T> selectTaskField(TaskQuery query, String fieldName) {
        return daoHandler.getTaskHandler().selectTaskField(query, fieldName);
    }

    @Override
    public TaskDto findTaskByActivityId(String activityId) {
        return toDto(daoHandler.getTaskHandler().findTaskByActivityId(activityId));
    }

    @Override
    public void updateStatus(Status stOld, Status stNew) {
        daoHandler.getTaskHandler().updateStatus(stOld, stNew);
    }

    @Override
    public void updateTaskOrder(long currentTaskId, Status newStatus, long[] orders) {
        daoHandler.getTaskHandler().updateTaskOrder(currentTaskId, newStatus, orders);
    }

    @Override
    public Set<String> getCoworker(long taskid) {
        return daoHandler.getTaskHandler().getCoworker(taskid);
    }

    @Override
    public TaskDto getTaskWithCoworkers(long id) {
        return toDto(daoHandler.getTaskHandler().getTaskWithCoworkers(id));
    }

    @Override
    public List<TaskDto> getUncompletedTasks(String user, int limit) {
        List<Task> taskEntities = daoHandler.getTaskHandler().getUncompletedTasks(user, limit);
        return taskEntities.stream().map(this::toDto).collect(Collectors.toList());
    }

    @Override
    public Long countUncompletedTasks(String user) {
        return daoHandler.getTaskHandler().countUncompletedTasks(user);
    }

    @Override
    public List<TaskDto> getAssignedTasks(String user, int limit) {
        List<Task> taskEntities = daoHandler.getTaskHandler().getAssignedTasks(user, limit);
        return taskEntities.stream().map(this::toDto).collect(Collectors.toList());
    }

    @Override
    public Long countAssignedTasks(String user) {
        return daoHandler.getTaskHandler().countAssignedTasks(user);
    }


    @Override
    public List<TaskDto> getWatchedTasks(String user, int limit) {
        List<Task> taskEntities = daoHandler.getTaskHandler().getWatchedTasks(user, limit);
        return taskEntities.stream().map(this::toDto).collect(Collectors.toList());
    }

    @Override
    public Long countWatchedTasks(String user) {
        return daoHandler.getTaskHandler().countWatchedTasks(user);
    }


    @Override
    public List<TaskDto> getCollaboratedTasks(String user, int limit) {
        List<Task> taskEntities = daoHandler.getTaskHandler().getCollaboratedTasks(user, limit);
        return taskEntities.stream().map(this::toDto).collect(Collectors.toList());
    }


    @Override
    public Long countCollaboratedTasks(String user) {
        return daoHandler.getTaskHandler().countCollaboratedTasks(user);
    }


    @Override
    public List<TaskDto> getIncomingTasks(String user, int offset, int limit) throws Exception {
        List<Task> taskEntities = Arrays.asList(daoHandler.getTaskHandler().getIncomingTasks(user).load(offset, limit));
        return taskEntities.stream().map(this::toDto).collect(Collectors.toList());
    }

    @Override
    public int countIncomingTasks(String user) throws Exception {
        return daoHandler.getTaskHandler().getIncomingTasks(user).getSize();
    }

    @Override
    public List<TaskDto> getOverdueTasks(String user, int limit) {
        List<Task> taskEntities = daoHandler.getTaskHandler().getOverdueTasks(user, limit);
        return taskEntities.stream().map(this::toDto).collect(Collectors.toList());
    }

    @Override
    public Long countOverdueTasks(String user) {
        return daoHandler.getTaskHandler().countOverdueTasks(user);
    }

    @Override
    public void addWatcherToTask(String username, TaskDto task) {
        Set<String> watchers = getWatchersOfTask(task);
        if (watchers != null && !watchers.contains(username)) {
            watchers.add(username);
            task.setWatcher(watchers);
            daoHandler.getTaskHandler().update(toEntity(task));
        }
    }

    @Override
    public void deleteWatcherOfTask(String username, TaskDto task) throws Exception {
        Set<String> watchers = getWatchersOfTask(task);
        if (watchers != null && watchers.contains(username)) {
            watchers.remove(username);
            task.setWatcher(watchers);
            daoHandler.getTaskHandler().update(toEntity(task));
        } else {
            throw new Exception("Cannot remove watcher " + username + "of task because watcher does not exist.");
        }
    }

    @Override
    public Set<String> getWatchersOfTask(TaskDto task) {
        return daoHandler.getTaskHandler().getWatchersOfTask(toEntity(task));
    }

    /**
     * Find tasks assigned to a user using a term to find in title or description
     * of the task
     *
     * @param user  username
     * @param query term to search in title or description
     * @param limit
     * @return {@link List} of {@link Task}
     */
    @Override
    public List<TaskDto> findTasks(String user, String query, int limit) {
        List<Task> taskEntities = daoHandler.getTaskHandler().findTasks(user, query, limit);
        return taskEntities.stream().map(this::toDto).collect(Collectors.toList());
    }

    /**
     * Count tasks assigned to a user using a search term to find in title or
     * description of the task
     *
     * @param user  username
     * @param query term to search in title or description
     * @return tasks count
     */
    @Override
    public long countTasks(String user, String query) {
        return daoHandler.getTaskHandler().countTasks(user, query);
    }

    @Override
    public ChangeLogEntry addTaskLog(ChangeLogEntry changeLogEntry) throws EntityNotFoundException {
        return changeLogToDto(daoHandler.getTaskLogHandler().create(changeLogToEntity(changeLogEntry)));
    }

    @Override
    public List<ChangeLogEntry> getTaskLogs(long taskId, int offset, int limit) throws Exception {
        return Arrays.asList(daoHandler.getTaskLogHandler().findTaskLogs(taskId).load(offset, limit))
                .stream()
                .map(this::changeLogToDto)
                .collect(Collectors.toList());

    }

    @Override
    public List<Object[]> countTaskStatusByProject(long projectId) {
        return daoHandler.getTaskHandler().countTaskStatusByProject(projectId);
    }


    @Override
    public ChangeLog changeLogToEntity(ChangeLogEntry changeLogEntry) {
        ChangeLog changeLog = new ChangeLog();
        changeLog.setId(changeLogEntry.getId());
        changeLog.setTask(changeLogEntry.getTask());
        changeLog.setAuthor(changeLogEntry.getAuthor());
        changeLog.setActionName(changeLogEntry.getActionName());
        changeLog.setCreatedTime(changeLogEntry.getCreatedTime());
        changeLog.setTarget(changeLogEntry.getTarget());
        return changeLog;
    }

    @Override
    public ChangeLogEntry changeLogToDto(ChangeLog changeLog) {
        ChangeLogEntry changeLogEntry = new ChangeLogEntry();
        changeLogEntry.setId(changeLog.getId());
        changeLogEntry.setTask(changeLog.getTask());
        changeLogEntry.setAuthor(changeLog.getAuthor());
        changeLogEntry.setActionName(changeLog.getActionName());
        changeLogEntry.setCreatedTime(changeLog.getCreatedTime());
        changeLogEntry.setTarget(changeLog.getTarget());
        changeLogEntry.setAuthorFullName(userService.loadUser(changeLog.getAuthor()).getDisplayName());
        return changeLogEntry;
    }

    public Task toEntity(TaskDto taskDto) {
        if(taskDto==null){
            return null;
        }
        Task taskEntity = new Task();
        taskEntity.setId(taskDto.getId());
        taskEntity.setTitle(taskDto.getTitle());
        taskEntity.setDescription(taskDto.getDescription());
        taskEntity.setPriority(taskDto.getPriority());
        taskEntity.setContext(taskDto.getContext());
        taskEntity.setAssignee(taskDto.getAssignee());
        taskEntity.setCoworker(taskDto.getCoworker());
        taskEntity.setWatcher(taskDto.getWatcher());
        taskEntity.setStatus(taskDto.getStatus());
        taskEntity.setRank(taskDto.getRank());
        taskEntity.setActivityId(taskDto.getActivityId());
        taskEntity.setCompleted(taskDto.isCompleted());
        taskEntity.setCreatedBy(taskDto.getCreatedBy());
        taskEntity.setCreatedTime(taskDto.getCreatedTime());
        taskEntity.setEndDate(taskDto.getEndDate());
        taskEntity.setStartDate(taskDto.getStartDate());
        taskEntity.setDueDate(taskDto.getDueDate());
        return taskEntity;
    }

    public TaskDto toDto(Task taskEntity) {
        if(taskEntity==null){
            return null;
        }
        TaskDto task = new TaskDto();
        task.setId(taskEntity.getId());
        task.setTitle(taskEntity.getTitle());
        task.setDescription(taskEntity.getDescription());
        task.setPriority(taskEntity.getPriority());
        task.setContext(taskEntity.getContext());
        task.setAssignee(taskEntity.getAssignee());
        task.setCoworker(taskEntity.getCoworker());
        task.setWatcher(taskEntity.getWatcher());
        task.setStatus(taskEntity.getStatus());
        task.setRank(taskEntity.getRank());
        task.setActivityId(taskEntity.getActivityId());
        task.setCompleted(taskEntity.isCompleted());
        task.setCreatedBy(taskEntity.getCreatedBy());
        task.setCreatedTime(taskEntity.getCreatedTime());
        task.setEndDate(taskEntity.getEndDate());
        task.setStartDate(taskEntity.getStartDate());
        task.setDueDate(taskEntity.getDueDate());
        return task;
    }
}

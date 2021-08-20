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
import org.exoplatform.task.service.StatusService;
import org.exoplatform.task.service.UserService;
import org.exoplatform.task.storage.ProjectStorage;
import org.exoplatform.task.storage.StatusStorage;
import org.exoplatform.task.storage.TaskStorage;
import org.exoplatform.task.util.StorageUtil;

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

    @Inject
    private final ProjectStorage projectStorage;


    public TaskStorageImpl(DAOHandler daoHandler, UserService userService, ProjectStorage projectStorage) {
        this.daoHandler = daoHandler;
        this.userService = userService;
        this.projectStorage = projectStorage;
    }

    @Override
    public TaskDto getTaskById(long id) {
        return StorageUtil.taskToDto(daoHandler.getTaskHandler().find(id),projectStorage);
    }

    @Override
    public TaskDto createTask(TaskDto task) {
        return StorageUtil.taskToDto(daoHandler.getTaskHandler().create(StorageUtil.taskToEntity(task)),projectStorage);
    }

    @Override
    public TaskDto update(TaskDto task) {
        return StorageUtil.taskToDto(daoHandler.getTaskHandler().update(StorageUtil.taskToEntity(task)),projectStorage);
    }

    @Override
    public void delete(TaskDto task) {
        daoHandler.getTaskHandler().delete(StorageUtil.taskToEntity(task));
    }

    @Override
    public List<TaskDto> findTasksByLabel(LabelDto label, List<Long> projectIds, String username, OrderBy orderBy, int offset, int limit) throws Exception {
        List<Task> taskEntities = Arrays.asList(daoHandler.getTaskHandler().findTasksByLabel(label.getId(), projectIds, username, orderBy).load(offset, limit));
        return taskEntities.stream().map((Task taskEntity) -> StorageUtil.taskToDto(taskEntity,projectStorage)).collect(Collectors.toList());
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
        return taskEntities.stream().map((Task taskEntity) -> StorageUtil.taskToDto(taskEntity,projectStorage)).collect(Collectors.toList());
    }

    @Override
    public List<TaskDto> findTasks(TaskQuery query, int offset, int limit) throws Exception {
        List<Task> taskEntities = Arrays.asList(daoHandler.getTaskHandler().findTasks(query).load(offset, limit));
        return taskEntities.stream().map((Task taskEntity) -> StorageUtil.taskToDto(taskEntity,projectStorage)).collect(Collectors.toList());
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
        return StorageUtil.taskToDto(daoHandler.getTaskHandler().findTaskByActivityId(activityId),projectStorage);
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
        return StorageUtil.taskToDto(daoHandler.getTaskHandler().getTaskWithCoworkers(id),projectStorage);
    }

    @Override
    public List<TaskDto> getUncompletedTasks(String user, int limit) {
        List<Task> taskEntities = daoHandler.getTaskHandler().getUncompletedTasks(user, limit);
        return taskEntities.stream().map((Task taskEntity) -> StorageUtil.taskToDto(taskEntity,projectStorage)).collect(Collectors.toList());
    }

    @Override
    public Long countUncompletedTasks(String user) {
        return daoHandler.getTaskHandler().countUncompletedTasks(user);
    }

    @Override
    public List<TaskDto> getAssignedTasks(String user, int limit) {
        List<Task> taskEntities = daoHandler.getTaskHandler().getAssignedTasks(user, limit);
        return taskEntities.stream().map((Task taskEntity) -> StorageUtil.taskToDto(taskEntity,projectStorage)).collect(Collectors.toList());
    }

    @Override
    public Long countAssignedTasks(String user) {
        return daoHandler.getTaskHandler().countAssignedTasks(user);
    }


    @Override
    public List<TaskDto> getWatchedTasks(String user, int limit) {
        List<Task> taskEntities = daoHandler.getTaskHandler().getWatchedTasks(user, limit);
        return taskEntities.stream().map((Task taskEntity) -> StorageUtil.taskToDto(taskEntity,projectStorage)).collect(Collectors.toList());
    }

    @Override
    public Long countWatchedTasks(String user) {
        return daoHandler.getTaskHandler().countWatchedTasks(user);
    }


    @Override
    public List<TaskDto> getCollaboratedTasks(String user, int limit) {
        List<Task> taskEntities = daoHandler.getTaskHandler().getCollaboratedTasks(user, limit);
        return taskEntities.stream().map((Task taskEntity) -> StorageUtil.taskToDto(taskEntity,projectStorage)).collect(Collectors.toList());
    }


    @Override
    public Long countCollaboratedTasks(String user) {
        return daoHandler.getTaskHandler().countCollaboratedTasks(user);
    }


    @Override
    public List<TaskDto> getIncomingTasks(String user, int offset, int limit) throws Exception {
        List<Task> taskEntities = Arrays.asList(daoHandler.getTaskHandler().getIncomingTasks(user).load(offset, limit));
        return taskEntities.stream().map((Task taskEntity) -> StorageUtil.taskToDto(taskEntity,projectStorage)).collect(Collectors.toList());
    }

    @Override
    public int countIncomingTasks(String user) throws Exception {
        return daoHandler.getTaskHandler().getIncomingTasks(user).getSize();
    }

    @Override
    public List<TaskDto> getOverdueTasks(String user, int limit) {
        List<Task> taskEntities = daoHandler.getTaskHandler().getOverdueTasks(user, limit);
        return taskEntities.stream().map((Task taskEntity) -> StorageUtil.taskToDto(taskEntity,projectStorage)).collect(Collectors.toList());
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
            daoHandler.getTaskHandler().update(StorageUtil.taskToEntity(task));
        }
    }

    @Override
    public void deleteWatcherOfTask(String username, TaskDto task) throws Exception {
        Set<String> watchers = getWatchersOfTask(task);
        if (watchers != null && watchers.contains(username)) {
            watchers.remove(username);
            task.setWatcher(watchers);
            daoHandler.getTaskHandler().update(StorageUtil.taskToEntity(task));
        } else {
            throw new Exception("Cannot remove watcher " + username + "of task because watcher does not exist.");
        }
    }

    @Override
    public Set<String> getWatchersOfTask(TaskDto task) {
        return daoHandler.getTaskHandler().getWatchersOfTask(StorageUtil.taskToEntity(task));
    }

    /**
     * Find tasks assigned to a user using a term to find in title or description
     * of the task
     *
     * @param user  username
     * @param query term to search in title or description
     * @param limit term to limit results.
     * @return {@link List} of {@link Task}
     */
    @Override
    public List<TaskDto> findTasks(String user, List<String> memberships, String query, int limit) {
        List<Task> taskEntities = daoHandler.getTaskHandler().findTasks(user, memberships, query, limit);
        return taskEntities.stream().map((Task taskEntity) -> StorageUtil.taskToDto(taskEntity,projectStorage)).collect(Collectors.toList());
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
        return StorageUtil.changeLogToDto(daoHandler.getTaskLogHandler().create(StorageUtil.changeLogToEntity(changeLogEntry,userService)),userService);
    }

    @Override
    public List<ChangeLogEntry> getTaskLogs(long taskId, int offset, int limit) throws Exception {
        return Arrays.asList(daoHandler.getTaskLogHandler().findTaskLogs(taskId).load(offset, limit))
                .stream()
                .map((ChangeLog changeLog) -> StorageUtil.changeLogToDto(changeLog,userService))
                .collect(Collectors.toList());

    }

    @Override
    public List<Object[]> countTaskStatusByProject(long projectId) {
        return daoHandler.getTaskHandler().countTaskStatusByProject(projectId);
    }

}

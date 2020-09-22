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

import org.apache.commons.lang3.StringUtils;
import org.exoplatform.commons.api.persistence.ExoTransactional;
import org.exoplatform.commons.utils.ListAccess;
import org.exoplatform.services.listener.ListenerService;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.task.dao.DAOHandler;
import org.exoplatform.task.dao.OrderBy;
import org.exoplatform.task.dao.TaskQuery;
import org.exoplatform.task.domain.Status;
import org.exoplatform.task.domain.Task;
import org.exoplatform.task.dto.ChangeLogEntry;
import org.exoplatform.task.dto.LabelDto;
import org.exoplatform.task.dto.TaskDto;
import org.exoplatform.task.exception.EntityNotFoundException;
import org.exoplatform.task.legacy.service.TaskPayload;
import org.exoplatform.task.service.TaskService;
import org.exoplatform.task.storage.TaskStorage;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.List;
import java.util.Set;


@Singleton
public class TaskServiceImpl implements TaskService {

    private static final Log LOG = ExoLogger.getExoLogger(TaskServiceImpl.class);


    @Inject
    private TaskStorage taskStorage;

    @Inject
    private DAOHandler daoHandler;

    private ListenerService listenerService;

    public TaskServiceImpl(TaskStorage taskStorage, DAOHandler daoHandler, ListenerService listenerService) {
        this.taskStorage = taskStorage;
        this.daoHandler = daoHandler;
        this.listenerService = listenerService;
    }

    @Override
    @ExoTransactional
    public TaskDto createTask(TaskDto task) {
        TaskDto result = taskStorage.createTask(task);
        //
        TaskPayload event = new TaskPayload(null, taskStorage.toEntity(result));
        try {
            listenerService.broadcast(TASK_CREATION, this, event);
        } catch (Exception e) {
            LOG.error("Error while broadcasting task creation event", e);
        }

        return result;
    }

    @Override
    @ExoTransactional
    public TaskDto updateTask(TaskDto task) {
        if (task == null) {
            throw new IllegalArgumentException("TaskDto must not be NULL");
        }

        TaskDto oldTaskEntity = taskStorage.getTaskWithCoworkers(task.getId());
        TaskDto newTaskEntity = taskStorage.update(task);
        TaskPayload event = new TaskPayload(taskStorage.toEntity(oldTaskEntity), taskStorage.toEntity(newTaskEntity));
        try {
            listenerService.broadcast(TASK_UPDATE, this, event);
        } catch (Exception e) {
            LOG.error("Error while broadcasting task creation event", e);
        }

        return newTaskEntity;
    }

    @Override
    @ExoTransactional
    public void updateTaskOrder(long currentTaskId, Status newStatus, long[] orders) {
        taskStorage.updateTaskOrder(currentTaskId, newStatus, orders);
    }

    @Override
    @ExoTransactional
    public void removeTask(long id) throws EntityNotFoundException {
        TaskDto task = getTask(id);// Can throw TaskNotFoundException

        taskStorage.delete(task);
    }

    @Override
    @ExoTransactional
    public TaskDto cloneTask(long id) throws EntityNotFoundException {

        TaskDto task = getTask(id);// Can throw TaskNotFoundException

        TaskDto newTask = task.clone();
        newTask.setId(0L);
        newTask.setCoworker(getCoworker(id));
        newTask.setTitle(Task.PREFIX_CLONE + newTask.getTitle());

        return createTask(newTask);
    }

    @Override
    public TaskDto getTask(long id) throws EntityNotFoundException {
        TaskDto task = taskStorage.getTaskById(id);
        if (task == null) {
            LOG.info("Can not find task with ID: " + id);
            throw new org.exoplatform.task.exception.EntityNotFoundException(id, Task.class);
        }
        return task;
    }

    @Override
    public Set<String> getMentionedUsers(long taskId) {
        return daoHandler.getCommentHandler().findMentionedUsersOfTask(taskId);
    }


    @Override
    @ExoTransactional
    public ChangeLogEntry addTaskLog(long id, String username, String actionName, String target) throws EntityNotFoundException {
        ChangeLogEntry log = new ChangeLogEntry();
        log.setTask(taskStorage.toEntity(getTask(id)));
        log.setAuthor(username);
        log.setActionName(actionName);
        log.setTarget(target);
        return taskStorage.addTaskLog(log);
    }

    @Override
    public List<ChangeLogEntry> getTaskLogs(long taskId, int offset, int limit) throws Exception {
        return taskStorage.getTaskLogs(taskId,  offset,  limit);
    }


    @Override
    public List<TaskDto> findTasks(TaskQuery query, int offset, int limit) throws Exception {
        return taskStorage.findTasks(query, offset, limit);
    }

    @Override
    public int countTasks(TaskQuery query) throws Exception {
        return taskStorage.countTasks(query);
    }

    @Override
    public <T> List<T> selectTaskField(TaskQuery query, String fieldName) {
        return taskStorage.selectTaskField(query, fieldName);
    }

    @Override
    public List<TaskDto> findTasksByLabel(LabelDto label, List<Long> projectIds, String username, OrderBy orderBy, int offset, int limit) throws Exception {

        return taskStorage.findTasksByLabel(label, projectIds, username, orderBy, offset, limit);
    }

    @Override
    public int countTasksByLabel(LabelDto label, List<Long> projectIds, String username, OrderBy orderBy) throws Exception {
        return taskStorage.countTasksByLabel(label, projectIds, username, orderBy);
    }

    public TaskDto findTaskByActivityId(String id) {
        return taskStorage.findTaskByActivityId(id);
    }

    @Override
    public Set<String> getCoworker(long taskId) {
        return taskStorage.getCoworker(taskId);
    }

    @Override
    public List<TaskDto> getUncompletedTasks(String user, int limit) {
        return taskStorage.getUncompletedTasks(user, limit);
    }

    @Override
    public Long countUncompletedTasks(String user) {
        return taskStorage.countUncompletedTasks(user);
    }

    @Override
    public List<TaskDto> findTasks(String user, String query, int limit) {
        if (StringUtils.isBlank(user)) {
            throw new IllegalArgumentException("user parameter is mandatory");
        }
        if (StringUtils.isBlank(query)) {
            throw new IllegalArgumentException("query parameter is mandatory");
        }
        return taskStorage.findTasks(user, query, limit);
    }

    @Override
    public long countTasks(String user, String query) {
        if (StringUtils.isBlank(user)) {
            throw new IllegalArgumentException("user parameter is mandatory");
        }
        if (StringUtils.isBlank(query)) {
            throw new IllegalArgumentException("query parameter is mandatory");
        }
        return taskStorage.countTasks(user, query);
    }

    @Override
    public List<TaskDto> getIncomingTasks(String user, int offset, int limit) throws Exception {
        return taskStorage.getIncomingTasks(user, offset, limit);
    }

    @Override
    public int countIncomingTasks(String user) throws Exception {
        return taskStorage.countIncomingTasks(user);
    }

    @Override
    public List<TaskDto> getOverdueTasks(String user, int limit) {
        return taskStorage.getOverdueTasks(user, limit);
    }

    @Override
    public Long countOverdueTasks(String user) {
        return taskStorage.countOverdueTasks(user);
    }

    @Override
    public void addWatcherToTask(String user, TaskDto task) throws Exception {
        taskStorage.addWatcherToTask(user, task);
    }

    @Override
    public void deleteWatcherOfTask(String user, TaskDto task) throws Exception {
        taskStorage.deleteWatcherOfTask(user, task);
    }

    public Set<String> getWatchersOfTask(TaskDto task) {
        return taskStorage.getWatchersOfTask(task);
    }


    @Override
    public List<Object[]> countTaskStatusByProject(long projectId) {
        return taskStorage.countTaskStatusByProject(projectId);
    }



}

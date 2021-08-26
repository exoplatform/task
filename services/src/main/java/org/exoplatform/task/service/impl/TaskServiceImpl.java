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
import org.exoplatform.commons.utils.CommonsUtils;
import org.exoplatform.services.listener.ListenerService;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.services.security.Identity;
import org.exoplatform.social.core.identity.model.Profile;
import org.exoplatform.social.core.identity.provider.OrganizationIdentityProvider;
import org.exoplatform.social.core.manager.IdentityManager;
import org.exoplatform.task.dao.DAOHandler;
import org.exoplatform.task.dao.OrderBy;
import org.exoplatform.task.dao.TaskQuery;
import org.exoplatform.task.domain.Priority;
import org.exoplatform.task.domain.Status;
import org.exoplatform.task.domain.Task;
import org.exoplatform.task.dto.*;
import org.exoplatform.task.exception.EntityNotFoundException;
import org.exoplatform.task.service.TaskPayload;
import org.exoplatform.task.service.ProjectService;
import org.exoplatform.task.service.TaskService;
import org.exoplatform.task.storage.TaskStorage;
import org.exoplatform.task.util.ProjectUtil;
import org.exoplatform.task.util.StorageUtil;
import org.exoplatform.task.util.TaskUtil;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.*;


@Singleton
public class TaskServiceImpl implements TaskService {

    private static final Log LOG = ExoLogger.getExoLogger(TaskServiceImpl.class);


    @Inject
    private TaskStorage taskStorage;

    @Inject
    private DAOHandler daoHandler;

    private ListenerService listenerService;

    private ProjectService projectService;

    public TaskServiceImpl(TaskStorage taskStorage, DAOHandler daoHandler, ListenerService listenerService) {
        this.taskStorage = taskStorage;
        this.daoHandler = daoHandler;
        this.listenerService = listenerService;
        this.projectService = projectService;
    }

    @Override
    @ExoTransactional
    public TaskDto createTask(TaskDto task) {
        TaskDto result = taskStorage.createTask(task);
        //
        TaskPayload event = new TaskPayload(null, result);
        try {
            listenerService.broadcast(TASK_CREATION, null, event);
            if(result.getStatus()!=null){
                listenerService.broadcast("exo.project.projectModified", null,result.getStatus().getProject() );
            }
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
        TaskPayload event = new TaskPayload(oldTaskEntity, newTaskEntity);
        try {
            listenerService.broadcast(TASK_UPDATE, null, event);
            if(task.getStatus()!=null){
                listenerService.broadcast("exo.project.projectModified", null, task.getStatus().getProject() );
            }
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
        LOG.info("Task {} removed", id);
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
        log.setTask(StorageUtil.taskToEntity(getTask(id)));
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
    public List<TaskDto> getWatchedTasks(String user, int limit) {
        return taskStorage.getWatchedTasks(user, limit);
    }


    @Override
    public Long countWatchedTasks(String user) {
        return taskStorage.countWatchedTasks(user);
    }



    @Override
    public List<TaskDto> getAssignedTasks(String user, int limit) {
        return taskStorage.getAssignedTasks(user, limit);
    }


    @Override
    public Long countAssignedTasks(String user) {
        return taskStorage.countWatchedTasks(user);
    }


    @Override
    public List<TaskDto> getCollaboratedTasks(String user, int limit) {
        return taskStorage.getCollaboratedTasks(user, limit);
    }


    @Override
    public Long countCollaboratedTasks(String user) {
        return taskStorage.countCollaboratedTasks(user);
    }



    @Override
    public List<TaskDto> findTasksByMemberShips(String user, List<String> memberships, String query, int limit) {
        if (StringUtils.isBlank(user)) {
            throw new IllegalArgumentException("user parameter is mandatory");
        }
        if (StringUtils.isBlank(query)) {
            throw new IllegalArgumentException("query parameter is mandatory");
        }
        return taskStorage.findTasks(user, memberships, query, limit);
    }

    @Override
    public List<TaskDto> findTasks(String user, String query, int limit) {
        return findTasksByMemberShips(user,new ArrayList<>(), query, limit);
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

    @Override
    public TasksList filterTasks(String query, long projectId, String keyword, List<Long> labels, TaskUtil.DUE dueDate, Priority priority, List<String> assignees, List<String> coworker, List<String> watchers, Long labelId, Long statusId, Identity currIdentity, String dueCategory, String space_group_id , TimeZone userTimezone, boolean isShowCompleted, boolean advanceSearch, boolean noProjPermission, boolean noLblPermission, String orderBy, String groupBy, int offset, int limit) throws Exception {

        List<String> defOrders;
        List<String> defGroupBys;

        List<Long> spaceProjectIds = null;
        if (space_group_id != null) {
            spaceProjectIds = new LinkedList<Long>();
            List<ProjectDto> projects = ProjectUtil.flattenTree(ProjectUtil.getProjectTree(space_group_id, projectService,offset,limit), projectService,offset,limit);
            for (ProjectDto p : projects) {
                if (p.canView(currIdentity)) {
                    spaceProjectIds.add(p.getId());
                }
            }
        }
        List<Long> allProjectIds = null;
        if (projectId == 0) {
            allProjectIds = new LinkedList<Long>();
            List<ProjectDto> projects = ProjectUtil.flattenTree(ProjectUtil.getProjectTree(null, projectService,offset,limit), projectService,offset,limit);
            for (ProjectDto p : projects) {
                if (p.canView(currIdentity)) {
                    allProjectIds.add(p.getId());
                }
            }
        }

        OrderBy order = null;
        if(orderBy != null && !orderBy.trim().isEmpty()) {
            order = TaskUtil.TITLE.equals(orderBy) || TaskUtil.DUEDATE.equals(orderBy) ? new OrderBy.ASC(orderBy) : new OrderBy.DESC(orderBy);
        }

        TaskQuery taskQuery = new TaskQuery();
        if (advanceSearch) {
            Status status = statusId != null ? daoHandler.getStatusHandler().find(statusId) : null;
            TaskUtil.buildTaskQuery(taskQuery, keyword, labels, status, dueDate, priority, assignees, coworker, watchers, isShowCompleted, userTimezone);
        } else {
            taskQuery.setKeyword(query);
            taskQuery.setCompleted(false);
        }

        //Get Tasks in good order
        if(projectId == ProjectUtil.INCOMING_PROJECT_ID) {
            //. Default order by CreatedDate
            if (orderBy == null || orderBy.isEmpty()) {
                orderBy = TaskUtil.CREATED_TIME;
            }
            order = new OrderBy.DESC(orderBy);

            taskQuery.setIsIncomingOf(currIdentity.getUserId());
            taskQuery.setOrderBy(Arrays.asList(order));
        } else if (projectId == ProjectUtil.TODO_PROJECT_ID) {
            defGroupBys =  Arrays.asList(TaskUtil.NONE, TaskUtil.PROJECT, TaskUtil.LABEL, TaskUtil.DUEDATE,TaskUtil.ASSIGNEE);
            defOrders =  Arrays.asList(TaskUtil.TITLE, TaskUtil.STATUS, TaskUtil.DUEDATE, TaskUtil.PRIORITY, TaskUtil.RANK);
            if (dueDate != null || !dueCategory.equals("")){
                taskQuery.setAssigneeIsTodoOf(currIdentity.getUserId());
            } else {
                taskQuery.setIsTodoOf(currIdentity.getUserId());
            }

            //TODO: process fiter here
            if ("overDue".equalsIgnoreCase(dueCategory)) {
                defGroupBys = Arrays.asList(TaskUtil.NONE, TaskUtil.PROJECT, TaskUtil.LABEL);
                defOrders = Arrays.asList(TaskUtil.TITLE, TaskUtil.PRIORITY, TaskUtil.DUEDATE);
                groupBy = groupBy == null || !defGroupBys.contains(groupBy) ? TaskUtil.PROJECT : groupBy;
            } else if ("today".equalsIgnoreCase(dueCategory)) {
                defGroupBys =  Arrays.asList(TaskUtil.NONE, TaskUtil.PROJECT, TaskUtil.LABEL);
                defOrders =  Arrays.asList(TaskUtil.TITLE, TaskUtil.PRIORITY, TaskUtil.RANK);
                if (orderBy == null) {
                    order = new OrderBy.DESC(TaskUtil.PRIORITY);
                    orderBy = TaskUtil.PRIORITY;
                }
                groupBy = groupBy == null || !defGroupBys.contains(groupBy) ? TaskUtil.NONE : groupBy;
            } else if ("tomorrow".equalsIgnoreCase(dueCategory)) {
                defGroupBys = Arrays.asList(TaskUtil.NONE, TaskUtil.PROJECT, TaskUtil.LABEL);
                defOrders = Arrays.asList(TaskUtil.TITLE, TaskUtil.PRIORITY, TaskUtil.RANK);
                if (orderBy == null || !defOrders.contains(orderBy)) {
                    order = new OrderBy.DESC(TaskUtil.PRIORITY);
                    orderBy = TaskUtil.PRIORITY;
                }
                groupBy = groupBy == null || !defGroupBys.contains(groupBy) ? TaskUtil.NONE : groupBy;
            } else if ("upcoming".equalsIgnoreCase(dueCategory)) {
                defGroupBys = Arrays.asList(TaskUtil.NONE, TaskUtil.PROJECT, TaskUtil.LABEL);
                defOrders =  Arrays.asList(TaskUtil.TITLE, TaskUtil.PRIORITY, TaskUtil.DUEDATE, TaskUtil.RANK);
                groupBy = groupBy == null || !defGroupBys.contains(groupBy) ? TaskUtil.NONE : groupBy;
            }

            if (orderBy == null || !defOrders.contains(orderBy)) {
                orderBy = TaskUtil.DUEDATE;
                order = new OrderBy.ASC(TaskUtil.DUEDATE);
            }
            if (groupBy == null || !defGroupBys.contains(groupBy)) {
                groupBy = TaskUtil.DUEDATE;
            }

            Date[] filterDate = TaskUtil.convertDueDate(dueCategory, userTimezone);
            taskQuery.setDueDateFrom(filterDate[0]);
            taskQuery.setDueDateTo(filterDate[1]);
            taskQuery.setOrderBy(Arrays.asList(order));

        } else if (projectId >= 0) {
            if (projectId == 0) {
                defGroupBys = Arrays.asList(TaskUtil.NONE, TaskUtil.ASSIGNEE, TaskUtil.PROJECT, TaskUtil.LABEL, TaskUtil.DUEDATE, TaskUtil.STATUS);

                if (orderBy == null || orderBy.isEmpty()) {
                    orderBy = TaskUtil.DUEDATE;
                    order = new OrderBy.ASC(orderBy);
                }

                if (spaceProjectIds != null) {
                    taskQuery.setProjectIds(spaceProjectIds);
                } else {
                    taskQuery.setProjectIds(allProjectIds);
                }
            } else {
                //. Default order by CreatedDate
                if (orderBy == null || orderBy.isEmpty()) {
                    orderBy = TaskUtil.DUEDATE;
                    order = new OrderBy.ASC(orderBy);
                }
                if (!noProjPermission) {
                    taskQuery.setProjectIds(Arrays.asList(projectId));
                }
            }
            taskQuery.setOrderBy(Arrays.asList(order));

        } else if (labelId != null && labelId >= 0) {
            defOrders =  Arrays.asList(TaskUtil.TITLE, TaskUtil.CREATED_TIME, TaskUtil.DUEDATE, TaskUtil.PRIORITY);
            if (orderBy == null || orderBy.isEmpty() || !defOrders.contains(orderBy)) {
                orderBy = TaskUtil.DUEDATE;
                order = new OrderBy.ASC(TaskUtil.DUEDATE);
            }

            if (labelId > 0) {
                defGroupBys = Arrays.asList(TaskUtil.NONE, TaskUtil.PROJECT, TaskUtil.DUEDATE, TaskUtil.STATUS);
                if (groupBy == null || groupBy.isEmpty() || !defGroupBys.contains(groupBy)) {
                    groupBy = TaskUtil.NONE;
                }
                if (!noLblPermission) {
                    taskQuery.setLabelIds(Arrays.asList(labelId));
                }
            } else {
                defGroupBys = Arrays.asList(TaskUtil.NONE, TaskUtil.PROJECT, TaskUtil.LABEL, TaskUtil.DUEDATE, TaskUtil.STATUS);
                if (groupBy == null || groupBy.isEmpty() || !defGroupBys.contains(groupBy)) {
                    groupBy = TaskUtil.LABEL;
                }
                taskQuery.setIsLabelOf(currIdentity.getUserId());
            }

            if (spaceProjectIds != null) {
                taskQuery.setProjectIds(spaceProjectIds);
            }
            taskQuery.setOrderBy(Arrays.asList(order));
        }



        List<TaskDto> listTasks = null;
        long tasksSize = 0;
        if ((spaceProjectIds != null  && spaceProjectIds.isEmpty()) || ( projectId == 0 && allProjectIds.isEmpty()) ||
                (noLblPermission && labelId != null && labelId > 0) || (noProjPermission &&  projectId > 0)) {
            listTasks = new ArrayList<>();
        } else {
            listTasks = findTasks(taskQuery,offset,limit);
            tasksSize = countTasks(taskQuery);
        }
       return new TasksList(listTasks,tasksSize);
    }

    @Override
    public boolean isExternal(String userId) {
        IdentityManager identityManager = CommonsUtils.getService(IdentityManager.class);
        org.exoplatform.social.core.identity.model.Identity userIdentity =  identityManager.getOrCreateIdentity(
            OrganizationIdentityProvider.NAME, userId);
        return userIdentity.getProfile() != null && userIdentity.getProfile().getProperty(Profile.EXTERNAL) != null && userIdentity.getProfile().getProperty(Profile.EXTERNAL).equals("true");
    }

}

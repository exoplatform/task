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
package org.exoplatform.task.util;


import java.text.*;
import java.util.*;
import java.util.stream.Collectors;

import org.exoplatform.portal.localization.LocaleContextInfoUtils;
import org.exoplatform.services.resources.LocaleContextInfo;
import org.exoplatform.services.resources.LocalePolicy;
import org.exoplatform.services.resources.ResourceBundleService;
import org.exoplatform.task.dto.*;
import org.exoplatform.task.service.StatusService;
import org.exoplatform.task.service.TaskService;
import org.exoplatform.task.service.UserService;
import org.exoplatform.task.rest.model.TaskEntity;
import org.exoplatform.task.service.LabelService;
import org.exoplatform.commons.utils.ListAccess;
import org.exoplatform.container.*;
import org.exoplatform.portal.mop.SiteKey;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.services.security.ConversationState;
import org.exoplatform.services.security.Identity;
import org.exoplatform.task.dao.TaskQuery;
import org.exoplatform.task.domain.*;
import org.exoplatform.task.exception.EntityNotFoundException;
import org.exoplatform.task.exception.ParameterEntityException;
import org.exoplatform.task.model.*;
import org.exoplatform.web.controller.router.Router;

/**
 * Created by The eXo Platform SAS
 * Author : Thibault Clement
 * tclement@exoplatform.com
 * 6/3/15
 */
public final class TaskUtil {
  public static final String URL_TASK_DETAIL = "/taskDetail/";
  
  private static final Log LOG = ExoLogger.getExoLogger(TaskUtil.class.getName());

  public static final String TITLE = "title";  
  public static final String PRIORITY = "priority";
  public static final String DUEDATE = "dueDate";
  public static final String CREATED_TIME = "createdTime";
  public static final String NONE = "none";
  public static final String STATUS = "status";
  public static final String ASSIGNEE = "assignee";
  public static final String PROJECT = "project";
  public static final String LABEL = "label";
  public static final String COMPLETED = "completed";
  public static final String RANK = "rank";
  public static final String MEMBERSHIP = "membership";
  public static final String CREATED_BY = "createdBy";
  public static final String COWORKER = "coworker";

  public static final ListAccess<Task> EMPTY_TASK_LIST = new ListAccess<Task>() {

    @Override
    public Task[] load(int index, int length) throws Exception, IllegalArgumentException {
      return new Task[0];
    }

    @Override
    public int getSize() throws Exception {
      return 0;
    }
  };
  
  public static enum DUE {
    OVERDUE, TODAY, TOMORROW, UPCOMING
  }
  
  private TaskUtil() {
  }

  public static Map<String, String> getDefOrders(ResourceBundle bundle) {
    return resolve(Arrays.asList(TITLE, PRIORITY, DUEDATE, CREATED_TIME), bundle);
  }
  
  public static Map<String, String> getDefGroupBys(long currentProject, ResourceBundle bundle) {
    if (currentProject == -1) {
      return resolve(Arrays.asList(NONE, ASSIGNEE, LABEL, DUEDATE), bundle);
    } else {
      return resolve(Arrays.asList(NONE, ASSIGNEE, LABEL, DUEDATE, STATUS), bundle);
    }
  }
  
  public static Map<String, String> resolve(List<String> keys, ResourceBundle bundle) {    
    Map<String, String> labels = new LinkedHashMap<String, String>();
    for (String k : keys) {
      if (k.isEmpty()) {
        k = NONE;
      }
      String label;
      try {
        label = bundle.getString("label." + k);
      } catch (Exception e) {
        LOG.warn("Error while resolving label 'label." + k + "' with locale " + bundle.getLocale(), e);
        label = k;
      }
      labels.put(k, label);
    }
    return labels;
  }
  
  public static TaskQuery buildTaskQuery(TaskQuery query, String keyword,
                                   List<Long> searchLabelIds,
                                   Status status,
                                   DUE dueDate,
                                   Priority priority,
                                   List<String> searchAssignee,
                                   Boolean showCompleted, TimeZone timezone) {
    if (keyword != null && !keyword.trim().isEmpty()) {
      query.setKeyword(keyword);
    }
    if (searchLabelIds != null && !searchLabelIds.isEmpty()) {
      query.setLabelIds(searchLabelIds);
    }
    if (status != null) {
      query.setStatus(status);
    }
    if (dueDate != null) {
      Date[] due = TaskUtil.convertDueDate(dueDate.name(), timezone);
      query.setDueDateFrom(due[0]);
      query.setDueDateTo(due[1]);
    }
    if (priority != null) {
      query.setPriority(priority);      
    }
    if (searchAssignee != null && !searchAssignee.isEmpty()) {      
      query.setAssignee(searchAssignee);      
    }
    if (showCompleted != null && !showCompleted) {
      query.setCompleted(showCompleted);
    }
    
    return query;
  }

  public static TaskQuery buildTaskQuery(TaskQuery query, String keyword,
                                   List<Long> searchLabelIds,
                                   Status status,
                                   DUE dueDate,
                                   Priority priority,
                                   List<String> searchAssignee,
                                   List<String> searchCoworker,
                                   List<String> searchWatcher,
                                   Boolean showCompleted, TimeZone timezone) {
    if (keyword != null && !keyword.trim().isEmpty()) {
      query.setKeyword(keyword);
    }
    if (searchLabelIds != null && !searchLabelIds.isEmpty()) {
      query.setLabelIds(searchLabelIds);
    }
    if (status != null) {
      query.setStatus(status);
    }
    if (dueDate != null) {
      Date[] due = TaskUtil.convertDueDate(dueDate.name(), timezone);
      query.setDueDateFrom(due[0]);
      query.setDueDateTo(due[1]);
    }
    if (priority != null) {
      query.setPriority(priority);
    }
    if (searchAssignee != null && !searchAssignee.isEmpty()) {
      query.setAssignee(searchAssignee);
    }
    if (searchCoworker != null && !searchCoworker.isEmpty()) {
      query.setCoworker(searchCoworker);
    }
    if (searchWatcher != null && !searchWatcher.isEmpty()) {
      query.setWatchers(searchWatcher);
    }
    if (showCompleted != null && !showCompleted) {
      query.setCompleted(showCompleted);
    }

    return query;
  }

  public static TaskQuery buildTaskQuery(TaskQuery query, String keyword,
                                         List<Long> searchLabelIds,
                                         StatusDto status,
                                         DUE dueDate,
                                         Priority priority,
                                         List<String> searchAssignee,
                                         Boolean showCompleted, TimeZone timezone) {
    if (keyword != null && !keyword.trim().isEmpty()) {
      query.setKeyword(keyword);
    }
    if (searchLabelIds != null && !searchLabelIds.isEmpty()) {
      query.setLabelIds(searchLabelIds);
    }
    if (status != null) {
      query.setStatus(status);
    }
    if (dueDate != null) {
      Date[] due = TaskUtil.convertDueDate(dueDate.name(), timezone);
      query.setDueDateFrom(due[0]);
      query.setDueDateTo(due[1]);
    }
    if (priority != null) {
      query.setPriority(priority);
    }
    if (searchAssignee != null && !searchAssignee.isEmpty()) {
      query.setAssignee(searchAssignee);
    }
    if (showCompleted != null && !showCompleted) {
      query.setCompleted(showCompleted);
    }

    return query;
  }

  public static Date[] convertDueDate(String dueDate, TimeZone timezone) {
    Date[] due = new Date[] {null, null};

    if (dueDate != null && !dueDate.isEmpty()) {
      Calendar today = Calendar.getInstance(timezone);
      today.set(Calendar.HOUR_OF_DAY, 0);
      today.set(Calendar.MINUTE, 0);
      today.set(Calendar.SECOND, 0);
      today.set(Calendar.MILLISECOND, 0);

      switch (DUE.valueOf(dueDate.toUpperCase())) {
        case OVERDUE:
          today.add(Calendar.DATE, 0);
          due[1] = today.getTime();
          break;
        case TODAY:
          due[0] = today.getTime();
          today.add(Calendar.DATE, 1);
          due[1] = new Date(today.getTimeInMillis() - 1);
          break;
        case TOMORROW:
          today.add(Calendar.DATE, 1);
          due[0] = today.getTime();
          today.add(Calendar.DATE, 1);
          due[1] = new Date(today.getTimeInMillis() - 1);
          break;
        case UPCOMING:
          today.add(Calendar.DATE, 2);
          due[0] = today.getTime();
      }
    }
    return due;
  }


  public static Map<GroupKey, List<TaskEntity>> groupTasks(List<TaskEntity> tasks, String groupBy, Identity userId, TimeZone userTimezone, LabelService labelService, UserService userService) throws EntityNotFoundException {
    Map<GroupKey, List<TaskEntity>> maps = new TreeMap<GroupKey, List<TaskEntity>>();
    for(TaskEntity task : tasks) {
      for (GroupKey key : getGroupName(task, groupBy, userId, userTimezone, labelService, userService)) {
        List<TaskEntity> list = maps.entrySet().stream()
          .filter(group -> group.getKey().getName().equals(key.getName()))
          .map(Map.Entry::getValue)
          .findFirst()
          .orElse(null);
        if(list == null) {
          list = new LinkedList<TaskEntity>();
          maps.put(key, list);
        }
        list.add(task);
      }
    }

    Comparator<GroupKey> byKey = Comparator.comparing(GroupKey::getName);

    return maps.entrySet().stream()
            .sorted(Map.Entry.comparingByKey(byKey))
            .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (e1, e2) -> e1, LinkedHashMap::new));
  }

  public static String buildTaskURL(TaskDto task) {
    if (task == null) {
      return "#";
    }

    StringBuilder urlBuilder = new StringBuilder(ResourceUtil.buildBaseURL());
    if (urlBuilder.length() <= 1) {
      return urlBuilder.toString();
    } else {
      return urlBuilder.append(URL_TASK_DETAIL)
              .append(task.getId())
              .toString();
    }
  }

  public static String buildTaskURL(TaskDto task, SiteKey siteKey, ExoContainer container, Router router) {
    if (task == null) {
      return "#";
    }

    StringBuilder urlBuilder = new StringBuilder(ResourceUtil.buildBaseURL(siteKey, container, router));
    if (urlBuilder.length() <= 1) {
      return urlBuilder.toString();
    } else {
      return urlBuilder.append(URL_TASK_DETAIL)
          .append(task.getId())
          .toString();
    }
  }

  private static GroupKey[] getGroupName(TaskEntity task, String groupBy, Identity userId, TimeZone userTimezone, LabelService labelService, UserService userService) throws EntityNotFoundException {
    if("project".equalsIgnoreCase(groupBy)) {
      StatusDto s = task.getStatus();
      if(s == null) {
        return new GroupKey[] {new GroupKey("No project", null, Integer.MAX_VALUE)};
      } else {
        return new GroupKey[] {new GroupKey(s.getProject().getName(), s.getProject(), (int)s.getProject().getId())};
      }
    } else if("status".equalsIgnoreCase(groupBy)) {
      StatusDto s = task.getStatus();
      if(s == null) {
        return new GroupKey[] {new GroupKey("To do", null, Integer.MIN_VALUE)};
      }
    } else if ("assignee".equalsIgnoreCase(groupBy)) {
      User user = null;
      if (task.getAssignee()!=null){
        user = task.getAssignee();
      }
      else {
        user=userService.loadUser(null);
      }

      return ( user.getUsername() != "guest" && user.getUsername() != null) ? new GroupKey[]{new GroupKey(user.getUsername(), user, user.getUsername().hashCode())} : new GroupKey[]{new GroupKey("Unassigned", null, Integer.MAX_VALUE)};

    } else if ("dueDate".equalsIgnoreCase(groupBy)) {
      Date dueDate = task.getDueDate();
      Calendar calendar = null;
      if (dueDate != null) {
        calendar = Calendar.getInstance(userTimezone);
        calendar.setTime(dueDate);
        if (DateUtil.isOverdue(calendar)) {
          calendar.setTimeInMillis(System.currentTimeMillis());
          calendar.add(Calendar.DATE, -1);
        } else if (!DateUtil.isToday(calendar) && !DateUtil.isTomorrow(calendar)) {
          //. is upcoming task
          calendar.setTimeInMillis(System.currentTimeMillis());
          calendar.add(Calendar.DATE, 7);
        }
        calendar.set(Calendar.HOUR, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);

        //
        dueDate = calendar.getTime();
      }
      return new GroupKey[] {new GroupKey(DateUtil.getDueDateLabel(calendar), dueDate, calendar == null ? Integer.MAX_VALUE : (int)calendar.getTimeInMillis())};

    } else if (TaskUtil.LABEL.equalsIgnoreCase(groupBy)&&(task.getStatus()!=null && task.getStatus().getProject()!=null)) {
      List<LabelDto> labels = labelService.findLabelsByTask(task.getTask(), task.getStatus().getProject().getId(),userId,0,-1);
      if (labels.isEmpty()) {
        return new GroupKey[] {new GroupKey("No Label", null, Integer.MAX_VALUE)};
      } else {
        GroupKey[] keys = new GroupKey[labels.size()];
        for (int i = 0; i < keys.length; i++) {
          LabelDto label = labels.get(i);
          keys[i] = new GroupKey(label.getName(), label, (int)label.getId());
        }
        return keys;
      }
    }
    else if (TaskUtil.COMPLETED.equalsIgnoreCase(groupBy)) {

      if(task.isCompleted() == false) {
        return new GroupKey[] {new GroupKey("Uncompleted", null, Integer.MAX_VALUE)};
      } else {
        return new GroupKey[] {new GroupKey("Completed", null, Integer.MIN_VALUE)};
      }
    }
    return new GroupKey[0];
  }

  public static boolean canDeleteComment(Identity identity, Comment comment) {
    if (comment == null || identity == null) {
      return false;
    }

    // Owner can delete his comment
    if (identity.getUserId().equals(comment.getAuthor())) {
      return true;
    }

    // Project manager can delete comment
    Task task = comment.getTask();
    if (task.getStatus() != null) {
      Project pj = task.getStatus().getProject();
      return pj.canEdit(identity);
    }

    return false;
  }

  public static boolean canDeleteComment(Identity identity, CommentDto comment) {
    if (comment == null || identity == null) {
      return false;
    }

    // Owner can delete his comment
    if (identity.getUserId().equals(comment.getAuthor())) {
      return true;
    }

    // Project manager can delete comment
    TaskDto task = comment.getTask();
    if (task.getStatus() != null) {
      ProjectDto pj = task.getStatus().getProject();
      return pj.canEdit(identity);
    }

    return false;
  }
  
  public static TaskDto saveTaskField(TaskDto task, Identity userId, String param, String[] values, TimeZone timezone, TaskService taskService, LabelService labelService, StatusService statusService)
      throws EntityNotFoundException, ParameterEntityException {

    if (timezone == null) {
      timezone = TimeZone.getDefault();
    }
    
    // Load coworker to avoid they will be deleted when save task
    //This issue caused because we alway clone entity from DAO, but Coworker are loaded lazily
    //This is need for TA-421
    task.setCoworker(TaskUtil.getCoworker(taskService, task.getId()));
    task.setWatcher(TaskUtil.getWatcher(taskService, task));

    //
    if ("workPlan".equalsIgnoreCase(param)) {
      //
      if (values == null) {
        task.setStartDate(null);
        task.setEndDate(null);
      } else {
        if (values.length != 2) {
          LOG.error("workPlan updating lack of params");
        }

        try {
          SimpleDateFormat wpf = new SimpleDateFormat("yyyy-MM-dd HH:mm");
          wpf.setTimeZone(timezone);
          Date startDate = wpf.parse(values[0]);
          Date endDate = wpf.parse(values[1]);
          
          task.setStartDate(startDate);
          task.setEndDate(endDate);
        } catch (ParseException ex) {
          LOG.info("Can parse date time value: "+values[0]+" or "+values[1]+" for Task with ID: " + task.getId());
          throw new ParameterEntityException(task.getId(), Task.class, param, values[0]+" or "+values[1],
              "cannot be parse to date", ex);
        }
      }
    } else {
      String value = values != null && values.length > 0 ? values[0] : null;

      if("title".equalsIgnoreCase(param)) {
        task.setTitle(value);
      } else if("dueDate".equalsIgnoreCase(param)) {
        //
        if(value == null || value.trim().isEmpty()) {
          task.setDueDate(null);
        } else {
          DateFormat df = new SimpleDateFormat("yyyy-MM-dd");
          df.setTimeZone(timezone);
          try {
            Date date = df.parse(value);
            task.setDueDate(date);
          } catch (ParseException ex) {
            LOG.info("Can parse date time value: "+value+" for Task with ID: "+ task.getId());
            throw new ParameterEntityException(task.getId(), Task.class, param, value, "cannot be parse to date", ex);
          }
        }
      } else if("status".equalsIgnoreCase(param)) {
        //
        try {
          Long statusId = Long.parseLong(value.toUpperCase());
          StatusDto status = statusService.getStatus(statusId);
          if(status == null) {
            LOG.info("Status does not exist with ID: " + value);
            throw new EntityNotFoundException(task.getId(), Status.class);
          }
          task.setStatus(status);
        } catch (NumberFormatException ex) {
          LOG.info("Status is unacceptable: "+value+" for Task with ID: "+task.getId());
          throw new ParameterEntityException(task.getId(), Task.class, param, value, "is unacceptable", ex);
        }
      } else if("description".equalsIgnoreCase(param)) {
        //CKEditor encode user input already, but we still need to remove malicious code
        //here in case user inject request using curl TA-387
        value = StringUtil.encodeInjectedHtmlTag(value);
        task.setDescription(value);
      } else if("completed".equalsIgnoreCase(param)) {
        task.setCompleted(Boolean.parseBoolean(value));
      } else if("assignee".equalsIgnoreCase(param)) {
        task.setAssignee(value);
      } else if("coworker".equalsIgnoreCase(param)) {
        Set<String> coworker = new HashSet<String>();
        if (values != null) {
          for (String v : values) {
            if (v != null && !v.isEmpty()) {
              coworker.add(v);
            }
          }
        }
        task.setCoworker(coworker);
      } else if("watcher".equalsIgnoreCase(param)) {
        Set<String> watcher = new HashSet<String>();
        if (values != null) {
          for (String v : values) {
            if (v != null && !v.isEmpty()) {
              watcher.add(v);
            }
          }
        }
        task.setWatcher(watcher);
      } else if ("priority".equalsIgnoreCase(param)) {
        Priority priority = Priority.valueOf(value);
        task.setPriority(priority);
      } else if ("project".equalsIgnoreCase(param)) {
        try {
          Long projectId = Long.parseLong(value);
          if (projectId > 0) {
            StatusDto st = statusService.getDefaultStatus(projectId);
            if (st == null) {
              throw new ParameterEntityException(task.getId(), Task.class, param, value, "Status for project is not found", null);
            }
            task.setStatus(st);
          } else {
            task.setStatus(null);
          }
        } catch (NumberFormatException ex) {
          throw new ParameterEntityException(task.getId(), Task.class, param, value, "ProjectID must be long", ex);
        }
      } else if("labels".equalsIgnoreCase(param)) {
        List<Long> ids = new ArrayList<Long>(values.length);
        List<String> labelsToCreate = new ArrayList<String>();
        for (int i = 0; i < values.length; i++) {
          try {
            if (values[i] == null || values[i].isEmpty()) {
              continue;
            }
            Long id = Long.parseLong(values[i]);
            LabelDto label = labelService.getLabel(id);
            if (label != null) {
              ids.add(Long.parseLong(values[i]));
            } else {
              labelsToCreate.add(values[i]);
            }
          } catch (NumberFormatException ex) {
            //throw new ParameterEntityException(-1L, Label.class, param, values[i], "LabelID must be long", ex);
            labelsToCreate.add(values[i]);
          }
        }

        Set<Long> persisted = new HashSet<Long>();
      if (task.getStatus()!=null && task.getStatus().getProject()!=null) {
        List<LabelDto> labels = labelService.findLabelsByTask(task, task.getStatus().getProject().getId(),userId, 0, -1);
        for (LabelDto label : labels) {
          if (!ids.contains(label.getId())) {
            labelService.removeTaskFromLabel(task, label.getId());
          } else {
            persisted.add(label.getId());
          }
        }
      }
        //
        for (String label : labelsToCreate) {
          LabelDto l = new LabelDto();
          l.setName(label);
          l.setUsername(userId.getUserId());
          l = labelService.createLabel(l);
          ids.add(l.getId());
        }
        
        for (Long labelId : ids) {
          if (!persisted.contains(labelId)) {
            labelService.addTaskToLabel(task, labelId);
          }
        }
      } else {
        LOG.info("Field name: " + param + " is not supported for entity Task");
        throw new ParameterEntityException(task.getId(), Task.class, param, value, "is not supported for the entity Task", null);
      }
    }

    return taskService.updateTask(task);
  }

  public static boolean hasEditPermission(TaskService taskService,TaskDto task) {
    Identity identity = ConversationState.getCurrent().getIdentity();
    String userId = identity.getUserId();

    if ((task.getAssignee() != null && task.getAssignee().equals(identity.getUserId())) ||
        getCoworker(taskService,task.getId()).contains(userId) ||
        (task.getCreatedBy() != null && task.getCreatedBy().equals(userId))) {
      return true;
    }

    if (task.getStatus() != null) {
      ProjectDto project = task.getStatus().getProject();
      if (project.canView(identity)) {
        return true;
      }
    }

    return UserUtil.isPlatformAdmin(identity);
  }
  public static boolean hasEditPermission(TaskService taskService,Task task) {
    Identity identity = ConversationState.getCurrent().getIdentity();
    String userId = identity.getUserId();

    if ((task.getAssignee() != null && task.getAssignee().equals(identity.getUserId())) ||
        getCoworker(taskService, task.getId()).contains(userId) ||
        (task.getCreatedBy() != null && task.getCreatedBy().equals(userId))) {
      return true;
    }

    if (task.getStatus() != null) {
      Project project = task.getStatus().getProject();
      if (project.canView(identity)) {
        return true;
      }
    }

    return UserUtil.isPlatformAdmin(identity);
  }

  public static boolean hasViewPermission(TaskService taskService,TaskDto task) {
    Identity identity = ConversationState.getCurrent().getIdentity();
    String userId = identity.getUserId();

    return hasMentionedUser(taskService, task, userId) || hasEditPermission(taskService,task);
  }

  private static boolean hasMentionedUser(TaskService taskService, TaskDto task, String userId) {
    Set<String> mentionedUsers = taskService.getMentionedUsers(task.getId());
    return mentionedUsers.contains(userId);
  }

  public static boolean hasViewOnlyPermission(TaskService taskService,TaskDto task) {
    return hasViewPermission(taskService,task) && !hasEditPermission(taskService,task);
  }

  public static boolean hasDeletePermission(TaskDto task) {
    Identity identity = ConversationState.getCurrent().getIdentity();
    String userId = identity.getUserId();

    if (task.getCreatedBy() != null && task.getCreatedBy().equals(userId)) {
      return true;
    }

    if (task.getStatus() != null) {
      ProjectDto project = task.getStatus().getProject();
      if (project.canEdit(identity)) {
        return true;
      }
    }

    return UserUtil.isPlatformAdmin(identity);
  }

/*
  private static TaskService getTaskService() {
    ExoContainer container = ExoContainerContext.getCurrentContainer();
    if (container instanceof RootContainer) {
      container = ((RootContainer)container).getPortalContainer(PortalContainer.getCurrentPortalContainerName());
    }
    return container.getComponentInstanceOfType(TaskService.class);
  }
*/

  public static String getResourceBundleLabel(Locale locale, String label) {
    ResourceBundleService resourceBundleService =  ExoContainerContext.getService(ResourceBundleService.class);
    return resourceBundleService.getResourceBundle(resourceBundleService.getSharedResourceBundleNames(), locale).getString(label);
  }

  /**
   * Gets platform language of user. In case of any errors return null.
   *
   * @param userId user Id
   * @return the platform language
   */
  public static String getUserLanguage(String userId) {
    LocaleContextInfo localeCtx = LocaleContextInfoUtils.buildLocaleContextInfo(userId);
    LocalePolicy localePolicy = ExoContainerContext.getCurrentContainer().getComponentInstanceOfType(LocalePolicy.class);
    String lang = Locale.getDefault().getLanguage();
    if(localePolicy != null) {
      Locale locale = localePolicy.determineLocale(localeCtx);
      lang = locale.toString();
    }
    return lang;
  }

  /**
   * 
   * Added for tests using a specific instance of taskService
   * 
   * @param taskService TaskService instance
   * @param taskId Task Id
   * @return List of Coworkers
   */
  public static Set<String> getCoworker(TaskService taskService, long taskId) {
    return taskService.getCoworker(taskId);
  }

/*  public static Set<String> getCoworker(long taskId) {
    return getTaskService().getCoworker(taskId);
  }*/

  public static Set<String> getWatcher(TaskService taskService, TaskDto task) {
    return taskService.getWatchersOfTask(task);
  }
  public static boolean isEligibleWatcher(Task task,String username){
    if(!task.getCoworker().contains(username)&& !username.equals(task.getAssignee())&& !username.equals(task.getCreatedBy())) {
      return true;
    } else {
      return false;
    }
  }
}


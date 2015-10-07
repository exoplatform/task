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


import org.exoplatform.calendar.model.Event;
import org.exoplatform.calendar.service.impl.NewUserListener;
import org.exoplatform.commons.utils.ListAccess;
import org.exoplatform.container.ExoContainer;
import org.exoplatform.portal.mop.SiteKey;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.services.organization.OrganizationService;
import org.exoplatform.task.dao.TaskQuery;
import org.exoplatform.task.domain.Comment;
import org.exoplatform.task.domain.Label;
import org.exoplatform.task.domain.Priority;
import org.exoplatform.task.domain.Project;
import org.exoplatform.task.domain.Status;
import org.exoplatform.task.domain.Task;
import org.exoplatform.task.exception.EntityNotFoundException;
import org.exoplatform.task.exception.ParameterEntityException;
import org.exoplatform.task.model.CommentModel;
import org.exoplatform.task.model.GroupKey;
import org.exoplatform.task.model.TaskModel;
import org.exoplatform.task.service.ProjectService;
import org.exoplatform.task.service.StatusService;
import org.exoplatform.task.service.TaskService;
import org.exoplatform.task.service.UserService;
import org.exoplatform.web.controller.router.Router;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.TimeZone;
import java.util.TreeMap;

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
  public static final String RANK = "rank";
  public static final String MEMBERSHIP = "membership";
  public static final String CREATED_BY = "createdBy";
  public static final String COWORKER = "coworker";
  
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
      labels.put(k, bundle.getString("label." + k));
    }
    return labels;
  }

  public static TaskModel getTaskModel(Long id, boolean loadAllComment, ResourceBundle bundle, String username,
                                       TaskService taskService, OrganizationService orgService, UserService userService, ProjectService projectService) throws EntityNotFoundException {
    TaskModel taskModel = new TaskModel();
    
    Task task = taskService.getTask(id); //Can throw TaskNotFoundException
    taskModel.setTask(task);

    org.exoplatform.task.model.User assignee = null;
    int numberCoworkers = 0;
    if(task.getAssignee() != null && !task.getAssignee().isEmpty()) {
      assignee = userService.loadUser(task.getAssignee());
      numberCoworkers = task.getCoworker() != null ? task.getCoworker().size() : 0;
    } else if (task.getCoworker() != null && task.getCoworker().size() > 0) {
      Set<String> coworkers = task.getCoworker();
      for (String u : coworkers) {
        if (assignee == null && u != null && !u.isEmpty()) {
          assignee = userService.loadUser(coworkers.iterator().next());
        } else {
          numberCoworkers++;
        }
      }
    }
    taskModel.setAssignee(assignee);
    taskModel.setNumberCoworkers(numberCoworkers);

    ListAccess<Comment> listComments = taskService.getComments(task.getId());
    long commentCount = ListUtil.getSize(listComments);
    taskModel.setCommentCount(commentCount);

    int limitComment = loadAllComment ? -1 : 2;
    List<Comment> cmts = Arrays.asList(ListUtil.load(listComments, 0, limitComment));
    List<CommentModel> comments = new ArrayList<CommentModel>(cmts.size());
    for(Comment c : cmts) {
      org.exoplatform.task.model.User u = userService.loadUser(c.getAuthor());
      comments.add(new CommentModel(c, u, CommentUtil.formatMention(c.getComment(), userService)));
    }
    taskModel.setComments(comments);

    org.exoplatform.task.model.User currentUser = userService.loadUser(username);
    taskModel.setCurrentUser(currentUser);
    
    String breadcumbs = "<li class=\"muted\" >" + bundle.getString("label.noProject") + "</li>";
    if (task.getStatus() != null) {
      Project p = task.getStatus().getProject();
      //breadcumbs = ProjectUtil.buildBreadcumbs(p.getId(), projectService, bundle);
      breadcumbs = "<li><a class=\"project-name\" href=\"javascript:void(0)\">" + p.getName() + "</a></li>";
    }
    taskModel.setBreadcumbs(breadcumbs);

    List<Label> labels = taskService.findLabelsByTask(id, username);
    if (labels != null) {
      taskModel.setLabels(labels);
    }

    return taskModel;
  }

  public static Map<GroupKey, List<Task>> groupTasks(List<Task> tasks, String groupBy, String username, TimeZone userTimezone, ResourceBundle bundle, TaskService taskService) {
    Map<GroupKey, List<Task>> maps = new TreeMap<GroupKey, List<Task>>();
    for(Task task : tasks) {
      for (GroupKey key : getGroupName(task, groupBy, username, userTimezone, bundle, taskService)) {
        List<Task> list = maps.get(key);
        if(list == null) {
          list = new LinkedList<Task>();
          maps.put(key, list);
        }
        list.add(task);
      }
    }
    return maps;
  }

  public static int countTasks(TaskService taskService, TaskQuery taskQuery) {
    ListAccess<Task> list = taskService.findTasks(taskQuery);
    return ListUtil.getSize(list);
  }

  public static Map<GroupKey, ListAccess<Task>> findTasks(TaskService taskService, TaskQuery query, String groupBy,
                                                          TimeZone userTimezone, UserService userService) {
    Map<GroupKey, ListAccess<Task>> maps = new TreeMap<GroupKey, ListAccess<Task>>();
    TaskQuery selectFieldQuery = query.clone();
    if (groupBy == null || groupBy.trim().isEmpty() || NONE.equalsIgnoreCase(groupBy)) {

      ListAccess<Task> tasks = taskService.findTasks(query);
      GroupKey key = new GroupKey("", null, 0);
      maps.put(key, tasks);
      return maps;

    } else if (DUEDATE.equalsIgnoreCase(groupBy)) {
      Calendar c = Calendar.getInstance(userTimezone);
      c.set(Calendar.HOUR_OF_DAY, 0);
      c.set(Calendar.MINUTE, 0);
      c.set(Calendar.SECOND, 0);
      c.set(Calendar.MILLISECOND, 0);

      Date fromDueDate = null;
      Date toDueDate = null;
      TaskQuery q;

      GroupKey key;
      ListAccess<Task> tasks;

      // Overdue
      c.add(Calendar.MILLISECOND, -1);
      toDueDate = c.getTime();
      q = query.clone();
      q.setDueDateTo(toDueDate);
      key = new GroupKey("Overdue", toDueDate, 0);
      tasks = taskService.findTasks(q);
      if (ListUtil.getSize(tasks) > 0) {
          maps.put(key, tasks);
      }

      // Today
      c.add(Calendar.MILLISECOND, 1);
      fromDueDate = c.getTime();
      c.add(Calendar.HOUR_OF_DAY, 24);
      c.add(Calendar.MILLISECOND, -1);
      toDueDate = c.getTime();

      q = query.clone();
      q.setDueDateFrom(fromDueDate);
      q.setDueDateTo(toDueDate);

      key = new GroupKey("Today", fromDueDate, 1);
      tasks = taskService.findTasks(q);
      if (ListUtil.getSize(tasks) > 0) {
        maps.put(key, tasks);
      }

      // Tomorrow
      c.add(Calendar.MILLISECOND, 1);
      fromDueDate = c.getTime();
      c.add(Calendar.HOUR_OF_DAY, 24);
      c.add(Calendar.MILLISECOND, -1);
      toDueDate = c.getTime();

      q = query.clone();
      q.setDueDateFrom(fromDueDate);
      q.setDueDateTo(toDueDate);

      key = new GroupKey("Tomorrow", fromDueDate, 2);
      tasks = taskService.findTasks(q);
      if (ListUtil.getSize(tasks) > 0) {
        maps.put(key, tasks);
      }

      // Upcoming
      c.add(Calendar.MILLISECOND, 1);
      fromDueDate = c.getTime();
      toDueDate = null;

      q = query.clone();
      q.setDueDateFrom(fromDueDate);
      q.setDueDateTo(toDueDate);

      key = new GroupKey("Upcoming", fromDueDate, 3);
      tasks = taskService.findTasks(q);
      if (ListUtil.getSize(tasks) > 0) {
        maps.put(key, tasks);
      }


      // No Due date
      q = query.clone();
      q.setNullField(DUEDATE);
      key = new GroupKey("No Due date", null, 4);
      tasks = taskService.findTasks(q);
      if (ListUtil.getSize(tasks) > 0) {
        maps.put(key, tasks);
      }

    } else if (PROJECT.equalsIgnoreCase(groupBy)) {
      List<Project> projects = taskService.selectTaskField(selectFieldQuery, "status.project");
      for (int i = 0; i < projects.size(); i++) {
        Project p = projects.get(i);
        GroupKey key = new GroupKey(p.getName(), p, i);
        TaskQuery q = query.clone();
        q.setProjectIds(Arrays.asList(p.getId()));
        ListAccess<Task> tasks = taskService.findTasks(q);
        if (ListUtil.getSize(tasks) > 0) {
          maps.put(key, tasks);
        }
      }

      //
      if (query.getProjectIds() == null || query.getProjectIds().isEmpty()) {
        GroupKey key = new GroupKey("No Project", null, Integer.MAX_VALUE);
        TaskQuery q = query.clone();
        q.setNullField(STATUS);
        ListAccess<Task> tasks = taskService.findTasks(q);
        if (ListUtil.getSize(tasks) > 0) {
          maps.put(key, tasks);
        }
      }

    } else if (STATUS.equalsIgnoreCase(groupBy)) {
      List<Status> statuses = taskService.selectTaskField(selectFieldQuery, STATUS);
      TaskQuery q;
      GroupKey key;
      ListAccess<Task> tasks;

      for (Status st : statuses) {
        q = query.clone();
        q.setStatus(st);
        key = new GroupKey(st.getName(), st, st.getRank());
        tasks = taskService.findTasks(q);
        if (ListUtil.getSize(tasks) > 0) {
          maps.put(key, tasks);
        }
      }
      if (query.getProjectIds() == null || query.getProjectIds().isEmpty()) {
        q = query.clone();
        q.setNullField(STATUS);
        key = new GroupKey("No Status", null, Integer.MAX_VALUE);
        tasks = taskService.findTasks(q);
        if (ListUtil.getSize(tasks) > 0) {
          maps.put(key, tasks);
        }
      }

    } else if (ASSIGNEE.equalsIgnoreCase(groupBy)) {
      List<String> assignees = taskService.selectTaskField(selectFieldQuery, ASSIGNEE);
      GroupKey key;
      ListAccess<Task> tasks;
      TaskQuery q;

      for (int i = 0; i < assignees.size(); i++) {
        String assignee = assignees.get(i);
        if (assignee == null) continue;
        q = query.clone();
        q.setAssignee(Arrays.asList(assignee));
        org.exoplatform.task.model.User user = userService.loadUser(assignee);
        key = new GroupKey(user.getDisplayName(), user, i);
        tasks = taskService.findTasks(q);
        if (ListUtil.getSize(tasks) > 0) {
          maps.put(key, tasks);
        }
      }

      if (query.getAssignee() == null) {
        q = query.clone();
        q.setNullField(ASSIGNEE);
        key = new GroupKey("Unassigned", null, Integer.MAX_VALUE);
        tasks = taskService.findTasks(q);
        if (ListUtil.getSize(tasks) > 0) {
          maps.put(key, tasks);
        }
      }
    } else if (LABEL.equalsIgnoreCase(groupBy)) {
      List<Label> labels = taskService.selectTaskField(selectFieldQuery, "labels");
      GroupKey key;
      ListAccess<Task> tasks;
      TaskQuery q;

      for (int i = 0; i < labels.size(); i++) {
        Label label = labels.get(i);
        if (label == null) continue;
        q = query.clone();
        q.setLabelIds(Arrays.asList(label.getId()));
        key = new GroupKey(label.getName(), label, i);
        tasks = taskService.findTasks(q);
        if (ListUtil.getSize(tasks) > 0) {
          maps.put(key, tasks);
        }
      }

      q = query.clone();
      q.setEmptyField("labels");
      key = new GroupKey("No Label", null, Integer.MAX_VALUE);
      tasks = taskService.findTasks(q);
      if (ListUtil.getSize(tasks) > 0) {
        maps.put(key, tasks);
      }
    }
    return maps;
  }

 public static String getPeriod(long time, ResourceBundle bundle) {
   long duration = (System.currentTimeMillis() - time) / 1000;
   long value;
   if (duration < 60) {
     return bundle.getString("label.Less_Than_A_Minute");
   } else {
     if (duration < 120) {
       return bundle.getString("label.About_A_Minute");
     } else {
       if (duration < 3600) {
         value = Math.round(duration / 60);
         return bundle.getString("label.About_?_Minutes").
                 replaceFirst("\\{0\\}", String.valueOf(value));
       } else {
         if (duration < 7200) {
           return bundle.getString("label.About_An_Hour");
         } else {
           if (duration < 86400) {
             value = Math.round(duration / 3600);
             return bundle.getString("label.About_?_Hours").
                     replaceFirst("\\{0\\}", String.valueOf(value));
           } else {
             if (duration < 172800) {
               return bundle.getString("label.About_A_Day");
             } else {
               if (duration < 2592000) {
                 value = Math.round(duration / 86400);
                 return bundle.getString("label.About_?_Days").
                         replaceFirst("\\{0\\}", String.valueOf(value));
               } else {
                 if (duration < 5184000) {
                   return bundle.getString("label.About_A_Month");
                 } else {
                   value = Math.round(duration / 2592000);
                   return bundle.getString("label.About_?_Months").
                           replaceFirst("\\{0\\}", String.valueOf(value));
                 }
               }
             }
           }
         }
       }
     }
   }
 }

  public static String getWorkPlan(Calendar start, Calendar end, ResourceBundle bundle) {
    if (start == null || start == null) {
      return null;
    }    
    
    //
    long duration = normalizeDate(end).getTimeInMillis() - normalizeDate(start).getTimeInMillis();
    
    SimpleDateFormat df = new SimpleDateFormat("dd MMMM YYYY");
    df.setTimeZone(start.getTimeZone());

    StringBuilder workplan = new StringBuilder(bundle.getString("label.workPlaned")).append(" ");
    if (start.get(Calendar.MONTH) == end.get(Calendar.MONTH) && start.get(Calendar.YEAR) == end.get(Calendar.YEAR)) {
      if (start.get(Calendar.DATE) == end.get(Calendar.DATE)) {
        workplan.append(bundle.getString("label.for")).append(" ").append("<strong>").append(df.format(start.getTime())).append("</strong>");
      } else {
        workplan.append(bundle.getString("label.from")).append(" ").append("<strong>").append(start.get(Calendar.DATE)).append("</strong>");
        workplan.append(" ").append(bundle.getString("label.to")).append(" ").append("<strong>").append(df.format(end.getTime())).append("</strong>");
      }
    } else {
      workplan.append(bundle.getString("label.from")).append(" ").append("<strong>").append(df.format(start.getTime())).append("</strong>");
      workplan.append(" ").append(bundle.getString("label.to")).append(" ").append("<strong>").append(df.format(end.getTime())).append("</strong>");
    }
    buildHour(duration, workplan, bundle);
    return workplan.toString();
  }

  public static long getTaskIdFromURI(String requestPath) {
    long taskId = -1;
    int index = requestPath.indexOf(URL_TASK_DETAIL);
    if (index >= 0) {
      index = index + URL_TASK_DETAIL.length();
      int lastIndex = requestPath.indexOf('/', index);
      String id;
      if (lastIndex > 0) {
        id = requestPath.substring(index, lastIndex);
      } else {
        id = requestPath.substring(index);
      }
      try {
        taskId = Long.parseLong(id);
      } catch (NumberFormatException ex) {
        taskId = -1;
      }
    }
    return taskId;
  }

  public static String buildTaskURL(Task task, SiteKey siteKey, ExoContainer container, Router router) {
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

  private static void buildHour(long duration, StringBuilder workplan, ResourceBundle bundle) {
    workplan.append(" (");        
    int halfHour = 30 * 60 * 1000;
    if (duration == halfHour) {
      workplan.append("30 ").append(bundle.getString("label.minutes")).append(")");
    } else if (duration == halfHour * 2) {
      workplan.append("1 ").append(bundle.getString("label.hour")).append(")");
    } else if (duration == halfHour * 48) {
      workplan.append(bundle.getString("label.allday")).append(")");
    } else {
      long hour = duration / (halfHour*2);
      long odd = duration % (halfHour * 2);
      if (odd == 0) {
        workplan.append(hour).append(" ").append(bundle.getString("label.hours")).append(")");
      } else {
        workplan.append(hour).append(" ").append(bundle.getString("label.hours")).append(" 30 ").append(bundle.getString("label.minutes")).append(")");
      }
    }
  }

  private static Calendar normalizeDate(Calendar date) {
    Calendar result = Calendar.getInstance();
    result.setTimeInMillis(date.getTimeInMillis());
    if (result.get(Calendar.MINUTE) == 59) {
      result.add(Calendar.MINUTE, 1);
    }
    return result;
  }

  private static GroupKey[] getGroupName(Task task, String groupBy, String username, TimeZone userTimezone, ResourceBundle bundle, TaskService taskService) {
    if("project".equalsIgnoreCase(groupBy)) {
      Status s = task.getStatus();
      if(s == null) {
        return new GroupKey[] {new GroupKey("No Project", null, Integer.MAX_VALUE)};
      } else {
        return new GroupKey[] {new GroupKey(s.getProject().getName(), s.getProject(), s.hashCode())};
      }
    } else if("status".equalsIgnoreCase(groupBy)) {
      Status s = task.getStatus();
      if(s == null) {
        return new GroupKey[] {new GroupKey("TODO", null, Integer.MIN_VALUE)};
      } else {
        return new GroupKey[] {new GroupKey(s.getName(), s, s.getRank())};
      }
    } else if("tag".equalsIgnoreCase(groupBy)) {
      Set<String> tags = task.getTag();
      GroupKey[] keys = new GroupKey[tags != null && tags.size() > 0 ? tags.size() : 1];
      if (tags == null || tags.size() == 0) {
        keys[0] = new GroupKey("Un tagged", null, Integer.MAX_VALUE);
      } else {
        int index = 0;
        for (String tag : tags) {
          keys[index++] = new GroupKey(tag, tag, tag.hashCode());
        }
      }
      return keys;
    } else if ("assignee".equalsIgnoreCase(groupBy)) {
      String assignee = task.getAssignee();
      return assignee != null ? new GroupKey[]{new GroupKey(assignee, assignee, assignee.hashCode())} : new GroupKey[]{new GroupKey("Unassigned", "", Integer.MAX_VALUE)};
    } else if ("dueDate".equalsIgnoreCase(groupBy)) {
      Date dueDate = task.getDueDate();
      Calendar calendar = null;
      if (dueDate != null) {
        calendar = Calendar.getInstance(userTimezone);
        calendar.setTime(dueDate);
      }
      return new GroupKey[] {new GroupKey(DateUtil.getDueDateLabel(calendar, bundle), dueDate, calendar != null ? 0 : 1)};
    } else if (TaskUtil.LABEL.equalsIgnoreCase(groupBy)) {
      //TODO:
      List<Label> labels;
      try {
        labels = taskService.findLabelsByTask(task.getId(), username);
      } catch (EntityNotFoundException ex) {
        labels = new ArrayList<Label>();
      }
      if (labels.isEmpty()) {
        return new GroupKey[] {new GroupKey("No label", null, Integer.MAX_VALUE)};
      } else {
        GroupKey[] keys = new GroupKey[labels.size()];
        for (int i = 0; i < keys.length; i++) {
          Label label = labels.get(i);
          keys[i] = new GroupKey(label.getName(), label, (int)label.getId());
        }
        return keys;
      }

    }
    return new GroupKey[0];
  }

  public static Event buildEvent(Event event, Task task) {
    if (task.getStatus() != null) {
      event.setCalendarId(String.valueOf(task.getStatus().getProject().getId()));      
      event.setEventState(task.getStatus().getName());
    }
    event.setDescription(task.getDescription());
    event.setEventCategoryId(NewUserListener.DEFAULT_EVENTCATEGORY_ID_ALL);
    event.setEventCategoryName(NewUserListener.DEFAULT_EVENTCATEGORY_NAME_ALL);
    event.setEventType(Event.TYPE_TASK);
    if (task.getStartDate() != null) {
      event.setFromDateTime(task.getStartDate());
      event.setToDateTime(task.getEndDate());
    } else {
      throw new IllegalStateException("Can't build event with a task that doesn't have workplan");
    }
    event.setId(String.valueOf(task.getId()));
    if (task.getPriority() != null) {      
      switch (task.getPriority()) {
        case HIGH :
          event.setPriority(Event.PRIORITY_HIGH);
          break;
        case NORMAL:
          event.setPriority(Event.PRIORITY_NORMAL);
          break;
        case LOW:
          event.setPriority(Event.PRIORITY_LOW);
          break;
        default:
          event.setPriority(Event.PRIORITY_NONE);
      }   
    }
    event.setSummary(task.getTitle());
    event.setTaskDelegator(task.getAssignee());    
    return event;
  }
  
  public static List<Label> buildRootLabels(List<Label> labels) {
    if (labels == null) return labels;

    Set<Label> rootLBLs = new LinkedHashSet<Label>();
    Set<Label> childs = new LinkedHashSet<Label>();
    for (Label p : labels) {
      while(true) {
        Label parent = p.getParent();
        if (parent == null) {
          rootLBLs.add(p);
          break;
        } else {
          childs.add(p);
          p = parent;
        }
      }
    }

    List<Label> parents = new LinkedList<Label>(rootLBLs);
    do {
      List<Label> tmpParents = new LinkedList<Label>();
      for (Label p : parents) {
        List<Label> tmp = new LinkedList<Label>();
        for (Label c : childs) {
          if (c.getParent().equals(p)) {
            tmp.add(c);
          }
        }
        p.setChildren(tmp);
        tmpParents.addAll(tmp);
        childs.removeAll(tmp);
      }
      parents = tmpParents;
    } while (!parents.isEmpty() || !childs.isEmpty());

    return new LinkedList<Label>(rootLBLs);
  }
  
  public static List<Label> filterLabelTree(List<Label> allLabels, Label lbl) {
    List<Label> tmp = new LinkedList<Label>();
    for (Label l : allLabels) {
      if (!TaskUtil.isLabelIn(l, lbl)) {
        tmp.add(l);
      }
    }
    return tmp;
  }
  
  private static boolean isLabelIn(Label child, Label parent) {
    Label pr = child;
    while (pr != null) {
      if (pr.getId() == parent.getId()) {
        return true;
      }
      pr = pr.getParent();
    }
    return false;
  }

  public static Task saveTaskField(Task task, String username, String param, String[] values, TimeZone timezone, TaskService taskService, StatusService statusService)
      throws EntityNotFoundException, ParameterEntityException {

    if (timezone == null) {
      timezone = TimeZone.getDefault();
    }

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
          SimpleDateFormat wpf = new SimpleDateFormat("yyyy-MM-dd hh:mm");
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
          Status status = statusService.getStatus(statusId);
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
      } else if("tags".equalsIgnoreCase(param)) {
        Set<String> tags = new HashSet<String>();
        for(String t : values) {
          tags.add(t);
        }
        task.setTag(tags);
      } else if ("priority".equalsIgnoreCase(param)) {
        Priority priority = Priority.valueOf(value);
        task.setPriority(priority);
      } else if ("project".equalsIgnoreCase(param)) {
        try {
          Long projectId = Long.parseLong(value);
          if (projectId > 0) {
            Status st = statusService.getDefaultStatus(projectId);
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
        for (int i = 0; i < values.length; i++) {
          try {
            if (values[i] == null || values[i].isEmpty()) {
              continue;
            }
            ids.add(Long.parseLong(values[i]));
          } catch (NumberFormatException ex) {
            throw new ParameterEntityException(-1L, Label.class, param, values[i], "LabelID must be long", ex);
          }
        }
        
        taskService.updateTask(task);        
        for (Long labelId : ids) {
          Label label = taskService.getLabelById(labelId);
          if (label == null) {
            throw new EntityNotFoundException(labelId, Label.class);
          }
          label.getTasks().add(task);
          try {            
            taskService.updateLabel(label, Arrays.asList(Label.FIELDS.TASK));            
          } catch (Exception ex) {
            ex.printStackTrace();
          }
        }

        List<Label> remove = new ArrayList<Label>();
        for(Label label : taskService.findLabelsByTask(task.getId(), username)) {
          if (label.getUsername().equals(username) && !ids.contains(label.getId())) {
            remove.add(label);
          }
        }
        for (Label l : remove) {
          l.getTasks().remove(task);
          try {
            taskService.updateLabel(l, Arrays.asList(Label.FIELDS.TASK));            
          } catch (Exception ex) {
            ex.printStackTrace();
          }
        }        
      } else if ("calendarIntegrated".equalsIgnoreCase(param)) {
        task.setCalendarIntegrated(Boolean.parseBoolean(value));
      } else {
        LOG.info("Field name: " + param + " is not supported for entity Task");
        throw new ParameterEntityException(task.getId(), Task.class, param, value, "is not supported for the entity Task", null);
      }
    }

    return taskService.updateTask(task);
  }
}


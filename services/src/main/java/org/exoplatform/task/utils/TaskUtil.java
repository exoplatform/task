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
package org.exoplatform.task.utils;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.TimeZone;
import java.util.TreeMap;

import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.services.organization.OrganizationService;
import org.exoplatform.services.organization.User;
import org.exoplatform.task.domain.Comment;
import org.exoplatform.task.domain.Project;
import org.exoplatform.task.domain.Status;
import org.exoplatform.task.domain.Task;
import org.exoplatform.task.exception.TaskNotFoundException;
import org.exoplatform.task.model.CommentModel;
import org.exoplatform.task.model.GroupKey;
import org.exoplatform.task.model.TaskModel;
import org.exoplatform.task.service.ProjectService;
import org.exoplatform.task.service.TaskService;
import org.exoplatform.task.service.UserService;

/**
 * Created by The eXo Platform SAS
 * Author : Thibault Clement
 * tclement@exoplatform.com
 * 6/3/15
 */
public final class TaskUtil {

  private static final Log LOG = ExoLogger.getExoLogger(TaskUtil.class.getName());
  
  public static final String TITLE = "title";  
  public static final String PRIORITY = "priority";
  public static final String DUEDATE = "dueDate";
  public static final String CREATED_TIME = "createdTime";
  public static final String NONE = "none";
  public static final String STATUS = "status";
  public static final String ASSIGNEE = "assignee";
  public static final String PROJECT = "project";
  public static final String RANK = "rank";
  
  private TaskUtil() {
  }

  public static Map<String, String> getDefOrders(ResourceBundle bundle) {
    return resolve(Arrays.asList(TITLE, PRIORITY, DUEDATE, CREATED_TIME), bundle);
  }
  
  public static Map<String, String> getDefGroupBys(long currentProject, ResourceBundle bundle) {
    if (currentProject == -1) {
      return resolve(Arrays.asList(NONE, ASSIGNEE, DUEDATE), bundle);
    } else {
      return resolve(Arrays.asList(NONE, DUEDATE, STATUS, ASSIGNEE), bundle);
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
  
  public static long getTaskNum(String username, List<Long> spaceProjectIds, Long projectId, TaskService taskService) {
    long taskNum = 0;
    if (projectId > 0) {
      taskNum = taskService.getTaskNum(null, Arrays.asList(projectId));      
    } else {
      if (spaceProjectIds != null) {
        if (projectId == 0) {
          taskNum = taskService.getTaskNum(null, spaceProjectIds);
        } else {
          taskNum = taskService.getTaskNum(username, spaceProjectIds);
        }
      } else {
        if (projectId == 0) {
          taskNum = taskService.getTaskNum(null, Arrays.asList(projectId));
        } else {
          taskNum = taskService.getTaskNum(username, Arrays.asList(projectId));
        }
      }      
    }
    return taskNum;
  }

  public static TaskModel getTaskModel(Long id, boolean loadAllComment, ResourceBundle bundle, String username,
                                       TaskService taskService, OrganizationService orgService, UserService userService, ProjectService projectService) throws TaskNotFoundException {
    TaskModel taskModel = new TaskModel();
    
    Task task = taskService.getTaskById(id); //Can throw TaskNotFoundException
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

    long commentCount = taskService.getNbOfCommentsByTask(task);
    taskModel.setCommentCount(commentCount);

    int limitComment = loadAllComment ? -1 : 2;
    List<Comment> cmts = taskService.getCommentsByTask(task, 0, limitComment);
    List<CommentModel> comments = new ArrayList<CommentModel>(cmts.size());
    for(Comment c : cmts) {
      org.exoplatform.task.model.User u = userService.loadUser(c.getAuthor());
      comments.add(new CommentModel(c, u, CommentUtils.formatMention(c.getComment(), userService)));
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

    return taskModel;
  }

  private static User findUserByName(String userName, OrganizationService orgService) {
    try {
      return orgService.getUserHandler().findUserByName(userName);
    } catch (Exception e) {
      LOG.error(e);
      return null;
    }
  }

  public static Map<GroupKey, List<Task>> groupTasks(List<Task> tasks, String groupBy, TimeZone userTimezone, ResourceBundle bundle) {
    Map<GroupKey, List<Task>> maps = new TreeMap<GroupKey, List<Task>>();
    for(Task task : tasks) {
      for (GroupKey key : getGroupName(task, groupBy, userTimezone, bundle)) {
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

  public static String getWorkPlan(Date startDate, long duration, ResourceBundle bundle) {
    if (startDate == null || duration <= 0) {
      return null;
    }
    
    Calendar start = Calendar.getInstance();
    start.setTimeInMillis(startDate.getTime());
    Calendar end = Calendar.getInstance();
    end.setTimeInMillis(startDate.getTime() + duration);
    //
    duration = normalizeDate(end).getTimeInMillis() - normalizeDate(start).getTimeInMillis();
    
    SimpleDateFormat df = new SimpleDateFormat("dd MMMM YYYY");

    StringBuilder workplan = new StringBuilder(bundle.getString("label.workPlaned")).append(" ");
    if (start.get(Calendar.MONTH) == end.get(Calendar.MONTH) && start.get(Calendar.YEAR) == end.get(Calendar.YEAR)) {
      if (start.get(Calendar.DATE) == end.get(Calendar.DATE)) {
        workplan.append(bundle.getString("label.for")).append(" ").append("<strong>").append(df.format(start.getTime())).append("</strong>");
      } else {
        workplan.append(bundle.getString("label.from")).append(" ").append("<strong>").append(start.get(Calendar.DATE)).append("</strong>");
        workplan.append(" ").append(bundle.getString("label.to")).append(" ").append("<strong>").append(df.format(end.getTime())).append("</strong>");
      }
    } else {
      workplan.append(bundle.getString("label.from")).append(" ").append("<strong>").append(df.format(startDate)).append("</strong>");
      workplan.append(" ").append(bundle.getString("label.to")).append(" ").append("<strong>").append(df.format(end.getTime())).append("</strong>");
    }
    buildHour(duration, workplan, bundle);
    return workplan.toString();
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

  private static GroupKey[] getGroupName(Task task, String groupBy, TimeZone userTimezone, ResourceBundle bundle) {
    if("project".equalsIgnoreCase(groupBy)) {
      Status s = task.getStatus();
      if(s == null) {
        return new GroupKey[] {new GroupKey("No Project", null, 1)};
      } else {
        return new GroupKey[] {new GroupKey(s.getProject().getName(), s.getProject(), 0)};
      }
    } else if("status".equalsIgnoreCase(groupBy)) {
      Status s = task.getStatus();
      if(s == null) {
        return new GroupKey[] {new GroupKey("TODO", null, -1)};
      } else {
        return new GroupKey[] {new GroupKey(s.getName(), s, 0)};
      }
    } else if("tag".equalsIgnoreCase(groupBy)) {
      Set<String> tags = task.getTags();
      GroupKey[] keys = new GroupKey[tags != null && tags.size() > 0 ? tags.size() : 1];
      if (tags == null || tags.size() == 0) {
        keys[0] = new GroupKey("Un tagged", null, 1);
      } else {
        int index = 0;
        for (String tag : tags) {
          keys[index++] = new GroupKey(tag, tag, 0);
        }
      }
      return keys;
    } else if ("assignee".equalsIgnoreCase(groupBy)) {
      String assignee = task.getAssignee();
      return assignee != null ? new GroupKey[]{new GroupKey(assignee, assignee, 0)} : new GroupKey[]{new GroupKey("Unassigned", "", 1)};
    } else if ("dueDate".equalsIgnoreCase(groupBy)) {
      Date dueDate = task.getDueDate();
      Calendar calendar = null;
      if (dueDate != null) {
        calendar = Calendar.getInstance(userTimezone);
        calendar.setTime(dueDate);
      }
      return new GroupKey[] {new GroupKey(DateUtil.getDueDateLabel(calendar, bundle), dueDate, calendar != null ? 0 : 1)};
    }
    return new GroupKey[0];
  }

}


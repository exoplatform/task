/**
 * Copyright (C) 2015 eXo Platform SAS.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Affero General Public License
 * as published by the Free Software Foundation; either version 3
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, see<http://www.gnu.org/licenses/>.
 **/
  
package org.exoplatform.task.integration;

import org.exoplatform.commons.api.search.SearchServiceConnector;
import org.exoplatform.commons.api.search.data.SearchContext;
import org.exoplatform.commons.api.search.data.SearchResult;
import org.exoplatform.container.ExoContainer;
import org.exoplatform.container.ExoContainerContext;
import org.exoplatform.container.xml.InitParams;
import org.exoplatform.portal.mop.SiteKey;
import org.exoplatform.services.security.ConversationState;
import org.exoplatform.services.security.Identity;
import org.exoplatform.task.dao.OrderBy;
import org.exoplatform.task.dao.TaskQuery;
import org.exoplatform.task.domain.Task;
import org.exoplatform.task.service.TaskService;
import org.exoplatform.task.service.UserService;
import org.exoplatform.task.util.ListUtil;
import org.exoplatform.task.util.ResourceUtil;
import org.exoplatform.task.util.StringUtil;
import org.exoplatform.task.util.TaskUtil;
import org.exoplatform.task.util.UserUtil;
import org.exoplatform.web.WebAppController;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

public class TaskSearchConnector extends SearchServiceConnector {

  private static final int MAX_EXCERPT_LENGTH = 430;
  
  public static final String DUE_FOR = "Due for: ";

  private TaskService taskService;
  
  private WebAppController controller;
  
  private UserService userService;
  
  public TaskSearchConnector(InitParams initParams, TaskService taskService, WebAppController controller, UserService userService) {
    super(initParams);
    this.taskService = taskService;
    this.controller = controller;    
    this.userService = userService;
  }
  
  @Override
  public Collection<SearchResult> search(SearchContext context,
                                         String query,
                                         Collection<String> sites,
                                         int offset,
                                         int limit,
                                         String sort,
                                         String order) {
    List<SearchResult> result = new ArrayList<SearchResult>();
    if(query == null || query.trim().isEmpty()) {
      return result;
    }
    
    query = StringUtil.FUZZY.matcher(query.trim()).replaceAll("");
    
    Identity currentUser = ConversationState.getCurrent().getIdentity(); 
    List<String> permissions = UserUtil.getMemberships(currentUser);

    TaskQuery taskQuery = new TaskQuery();
    //taskQuery.setAssignee(currentUser.getUserId());
    taskQuery.setKeyword(query);
    //taskQuery.setMemberships(permissions);
    //taskQuery.setOrFields(Arrays.asList(TaskUtil.ASSIGNEE, TaskUtil.MEMBERSHIP));
    taskQuery.setAccessible(currentUser);
    OrderBy orderBy = buildOrderBy(sort, order);
    if (orderBy != null) {
      taskQuery.setOrderBy(Arrays.asList(orderBy));      
    }
    SimpleDateFormat  df = new SimpleDateFormat("EEEEE, MMMMMMMM d, yyyy");
    df.setTimeZone(userService.getUserTimezone(currentUser.getUserId()));
    Task[] tasks = ListUtil.load(taskService.findTasks(taskQuery), 0, -1);
    for (Task t : tasks) {
      result.add(buildResult(t, df));
    }

    return ResourceUtil.subList(result, offset, limit);
  }

  private SearchResult buildResult(Task t, SimpleDateFormat df) {    
    String url = buildUrl(t);
    String imageUrl = buildImageUrl(t);
    String excerpt = buildExcerpt(t);
    String projectName = t.getStatus() != null ? t.getStatus().getProject().getName() : null;
    String priority = t.getPriority() != null ? t.getPriority().name() : null;
    String dueDate = t.getDueDate() != null ? DUE_FOR + df.format(t.getDueDate()) : null;    
    return new TaskSearchResult(url, t.getTitle(), excerpt, imageUrl,  projectName, priority, dueDate, t.isCompleted(), t.getCreatedTime().getTime(), 0);
  }
  
  private String buildExcerpt(Task t) {
    String description = t.getDescription();
    if (description != null) {
      if (description.length() > MAX_EXCERPT_LENGTH) {
        return description.substring(0, MAX_EXCERPT_LENGTH) + "...";        
      } else {
        return description;
      }
    }

    return null;
  }

  private String buildImageUrl(Task t) {
    return null;
  }

  private String buildUrl(Task t) {
    ExoContainer container = ExoContainerContext.getCurrentContainer();
    return TaskUtil.buildTaskURL(t, SiteKey.portal("intranet"), container, controller.getRouter());
  }

  private OrderBy buildOrderBy(String sort, String order) {
    String orderBy = null;
    if (StringUtil.ORDERBY_DATE.equals(sort) || StringUtil.ORDERBY_RELEVANCY.equals(sort)) {
      orderBy = TaskUtil.CREATED_TIME;
    } else if (StringUtil.ORDERBY_TITLE.equals(sort)) {
      orderBy = TaskUtil.TITLE;
    }
    if (orderBy != null) {
      if (StringUtil.ASC.equals(order)) {
        return new OrderBy.ASC(orderBy);
      } else {
        return new OrderBy.DESC(orderBy);
      }
    }
    return null;
  }  
}

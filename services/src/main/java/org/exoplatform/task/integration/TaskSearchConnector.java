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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

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
import org.exoplatform.task.domain.Priority;
import org.exoplatform.task.domain.Task;
import org.exoplatform.task.service.TaskService;
import org.exoplatform.task.utils.ResourceUtil;
import org.exoplatform.task.utils.StringUtil;
import org.exoplatform.task.utils.TaskUtil;
import org.exoplatform.task.utils.UserUtils;
import org.exoplatform.web.WebAppController;

public class TaskSearchConnector extends SearchServiceConnector {

  private static final int MAX_EXCERPT_LENGTH = 430;
  
  public static final String DUE_FOR = "Due for: ";
  public static final String PRIORITY = "Priority: ";

  private TaskService taskService;
  
  private WebAppController controller;
  
  public TaskSearchConnector(InitParams initParams, TaskService taskService, WebAppController controller) {
    super(initParams);
    this.taskService = taskService;
    this.controller = controller;    
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
    List<String> permissions = UserUtils.getMemberships(currentUser);

    TaskQuery taskQuery = new TaskQuery();
    taskQuery.setAssignee(currentUser.getUserId());
    taskQuery.setKeyword(query);
    taskQuery.setMemberships(permissions);
    taskQuery.setOrFields(Arrays.asList(TaskUtil.ASSIGNEE, TaskUtil.MEMBERSHIP));
    OrderBy orderBy = buildOrderBy(sort, order);
    if (orderBy != null) {
      taskQuery.setOrderBy(Arrays.asList(orderBy));      
    }
    List<Task> tasks = taskService.findTaskByQuery(taskQuery);
    for (Task t : tasks) {
      result.add(buildResult(t));
    }

    return ResourceUtil.subList(result, offset, limit);
  }

  private SearchResult buildResult(Task t) {
    String detail = buildDetail(t);
    String url = buildUrl(t);
    String imageUrl = buildImageUrl(t);
    String excerpt = buildExcerpt(t);    
    return new SearchResult(url, t.getTitle(), excerpt, detail, imageUrl, t.getCreatedTime().getTime(), 0);
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

  private String buildDetail(Task t) {
    StringBuilder detail = new StringBuilder();
    
    if (t.getStatus() != null) {
      detail.append(t.getStatus().getProject().getName()).append(" - ");
    }
    if (t.getPriority() != null && !t.getPriority().equals(Priority.UNDEFINED)) {
      detail.append(PRIORITY).append(t.getPriority().name()).append(" - ");      
    }
    if (t.getDueDate() != null) {
      detail.append(DUE_FOR).append(StringUtil.DATE_TIME_FORMAT.format(t.getDueDate()));
    }

    return detail.toString();
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

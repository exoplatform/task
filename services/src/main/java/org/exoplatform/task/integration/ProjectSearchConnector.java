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
import org.exoplatform.task.domain.Project;
import org.exoplatform.task.service.ProjectService;
import org.exoplatform.task.util.ProjectUtil;
import org.exoplatform.task.util.ResourceUtil;
import org.exoplatform.task.util.StringUtil;
import org.exoplatform.task.util.UserUtil;
import org.exoplatform.web.WebAppController;

public class ProjectSearchConnector extends SearchServiceConnector {

  private ProjectService projectService;  
  
  private WebAppController controller;
  
  public ProjectSearchConnector(InitParams initParams, ProjectService projectService, WebAppController controller) {
    super(initParams);
    this.projectService = projectService;
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
    //List<Project> projects = projectService.findProjectByKeyWord(currentUser, query, buildOrderBy(sort, order));
    List<Project> projects = projectService.findProjects(UserUtil.getMemberships(currentUser), query, buildOrderBy(sort, order));

    for (Project p : projects) {
      result.add(buildResult(p));
    }
    
    return ResourceUtil.subList(result, offset, limit);
  }

  private SearchResult buildResult(Project p) {
    String detail = p.getDescription();
    String url = buildUrl(p);
    String imageUrl = buildImageUrl(p);
    return new SearchResult(url, p.getName(), "", detail, imageUrl, p.getDueDate().getTime(), 0);
  }
  
  private String buildImageUrl(Project p) {
    return null;
  }

  private String buildUrl(Project p) {
    ExoContainer container = ExoContainerContext.getCurrentContainer();
    return ProjectUtil.buildProjectURL(p, SiteKey.portal("intranet"), container, controller.getRouter());
  }

  private OrderBy buildOrderBy(String sort, String order) {
    String orderBy = null;
    if (StringUtil.ORDERBY_DATE.equals(sort)) {
      orderBy = ProjectUtil.DUE_DATE;
    } else if (StringUtil.ORDERBY_TITLE.equals(sort)) {
      orderBy = ProjectUtil.NAME;
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

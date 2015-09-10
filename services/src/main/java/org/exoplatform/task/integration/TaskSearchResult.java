/*
 * Copyright (C) 2003-2015 eXo Platform SAS.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package org.exoplatform.task.integration;

import org.exoplatform.commons.api.search.data.SearchResult;

public class TaskSearchResult extends SearchResult {
  private String projectName;
  
  private String priority;
  
  private String dueDate;
  
  private boolean completed;
  
  public TaskSearchResult(String url,
                          String title,
                          String excerpt,
                          String imageUrl,
                          String projectName,
                          String priority,
                          String dueDate,
                          boolean completed,
                          long date,
                          long relevancy) {
    super(url, title, excerpt, null, imageUrl, date, relevancy);
    this.dueDate = dueDate;
    this.priority = priority;
    this.projectName = projectName;
    this.completed = completed;
  }

  public String getProjectName() {
    return projectName;
  }

  public void setProjectName(String projectName) {
    this.projectName = projectName;
  }

  public String getPriority() {
    return priority;
  }

  public void setPriority(String priority) {
    this.priority = priority;
  }

  public String getDueDate() {
    return dueDate;
  }

  public void setDueDate(String dueDate) {
    this.dueDate = dueDate;
  }

}

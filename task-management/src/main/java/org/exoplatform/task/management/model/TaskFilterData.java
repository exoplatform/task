/*
 * Copyright (C) 2015 eXo Platform SAS.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */

package org.exoplatform.task.management.model;

import java.io.Serializable;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import juzu.SessionScoped;

import org.exoplatform.task.domain.Priority;
import org.exoplatform.task.util.TaskUtil.DUE;

@SessionScoped
public class TaskFilterData implements Serializable {
  private static final long      serialVersionUID = 3251730267730418354L;

  private Map<FilterKey, Filter> filters          = new HashMap<FilterKey, Filter>();

  private boolean                enabled;

  public enum FILTER_NAME {
    KEYWORD, LABEL, STATUS, ASSIGNEE, DUE, PRIORITY, SHOW_COMPLETE
  }

  public Filter getFilter(FilterKey key) {
    Filter filter = filters.get(key);
    if (filter == null) {
      synchronized (this) {
        filter = filters.get(key);
        if (filter == null) {
          filter = new Filter();
          filters.put(key, filter);
        }
      }
    }
    return filter;
  }

  public void clear() {
    filters.clear();
  }

  public static class Filter implements Serializable {
    private static final long        serialVersionUID = -1199208518296375306L;

    private boolean enabled = false;
    private Map<FILTER_NAME, Object> data             = new ConcurrentHashMap<FILTER_NAME, Object>();

    public boolean isEnabled() {
      return enabled;
    }

    public void setEnabled(boolean enabled) {
      this.enabled = enabled;
    }

    public String getKeyword() {
      Object keyword = data.get(FILTER_NAME.KEYWORD);
      if (keyword != null) {
        return (String) keyword;
      } else {
        return "";
      }
    }

    public void setKeyword(String keyword) {
      data.put(FILTER_NAME.KEYWORD, keyword);
    }

    public List<Long> getLabel() {
      return getList(FILTER_NAME.LABEL);
    }

    public void setLabel(List<Long> labels) {
      data.put(FILTER_NAME.LABEL, labels);
    }

    public Long getStatus() {
      return (Long) data.get(FILTER_NAME.STATUS);
    }

    public void setStatus(Long statusId) {
      if (statusId == null) {
        data.remove(FILTER_NAME.STATUS);
      } else {
        data.put(FILTER_NAME.STATUS, statusId);        
      }
    }

    public List<String> getAssignee() {
      return getList(FILTER_NAME.ASSIGNEE);
    }

    public void setAssignee(List<String> assignees) {
      data.put(FILTER_NAME.ASSIGNEE, assignees);
    }

    public DUE getDue() {
      return (DUE) data.get(FILTER_NAME.DUE);
    }

    public void setDue(DUE due) {
      if (due == null) {
        data.remove(FILTER_NAME.DUE);
      } else {
        data.put(FILTER_NAME.DUE, due);        
      }
    }

    public Priority getPriority() {
      return (Priority) data.get(FILTER_NAME.PRIORITY);
    }

    public void setPriority(Priority priority) {
      if (priority == null) {
        data.remove(FILTER_NAME.PRIORITY);
      } else {
        data.put(FILTER_NAME.PRIORITY, priority);        
      }
    }

    public boolean isShowCompleted() {
      Object showCompleted = data.get(FILTER_NAME.SHOW_COMPLETE);
      if (showCompleted != null) {
        return (Boolean) showCompleted;
      } else {
        return false;
      }
    }

    public void setShowCompleted(boolean showCompleted) {
      data.put(FILTER_NAME.SHOW_COMPLETE, showCompleted);
    }

    public void updateFilterData(String filterLabelIds,
                                 String statusId,
                                 String dueDate,
                                 String priority,
                                 String assignee,
                                 Boolean showCompleted,
                                 String keyword) {
      List<Long> searchLabelIds = new LinkedList<Long>();
      if (filterLabelIds != null) {
        for (String id : filterLabelIds.split(",")) {
          if (!(id = id.trim()).isEmpty()) {
            try {
              searchLabelIds.add(Long.parseLong(id));
            } catch (Exception ex) {
            }
          }
        }
      }

      List<String> searchAssignee = new LinkedList<String>();
      if (assignee != null) {
        for (String u : assignee.split(",")) {
          if (!(u = u.trim()).isEmpty()) {
            searchAssignee.add(u);
          }
        }
      }

      if (keyword != null) {
        this.setKeyword(keyword);
      }
      if (filterLabelIds != null) {
        this.setLabel(searchLabelIds);
      }
      if (statusId != null) {
        if (statusId.isEmpty()) {
          this.setStatus(null);
        } else {
          this.setStatus(Long.parseLong(statusId));
        }
      }
      if (assignee != null) {
        this.setAssignee(searchAssignee);
      }
      if (dueDate != null) {
        if (dueDate.isEmpty()) {
          this.setDue(null);
        } else {
          this.setDue(DUE.valueOf(dueDate.toUpperCase()));
        }
      }
      if (priority != null) {
        if (priority.isEmpty()) {
          this.setPriority(null);
        } else {
          this.setPriority(Priority.valueOf(priority.toUpperCase()));
        }
      }
      if (showCompleted != null) {
        this.setShowCompleted(showCompleted);
      }
    }

    private <T> List<T> getList(FILTER_NAME name) {
      Object tmp = data.get(name);
      if (tmp != null) {
        return (List<T>) tmp;
      } else {
        return Collections.emptyList();
      }
    }

  }

  public static class FilterKey implements Serializable {
    private static final long serialVersionUID = 1465557792143606223L;

    private Long              projectId;

    private Long              labelId;

    private DUE               dueDate;

    private FilterKey(Long projectId, DUE dueDate, Long labelId) {
      this.projectId = projectId;
      this.labelId = labelId;
      this.dueDate = dueDate;
    }

    public static FilterKey withLabel(Long labelId) {
      return new FilterKey(null, null, labelId);
    }

    public static FilterKey withProject(Long projectId, DUE dueDate) {
      return new FilterKey(projectId, dueDate, null);
    }

    @Override
    public int hashCode() {
      final int prime = 31;
      int result = 1;
      result = prime * result + ((dueDate == null) ? 0 : dueDate.hashCode());
      result = prime * result + ((labelId == null) ? 0 : labelId.hashCode());
      result = prime * result + ((projectId == null) ? 0 : projectId.hashCode());
      return result;
    }

    @Override
    public boolean equals(Object obj) {
      if (this == obj)
        return true;
      if (obj == null)
        return false;
      if (getClass() != obj.getClass())
        return false;
      FilterKey other = (FilterKey) obj;
      if (dueDate != other.dueDate)
        return false;
      if (labelId == null) {
        if (other.labelId != null)
          return false;
      } else if (!labelId.equals(other.labelId))
        return false;
      if (projectId == null) {
        if (other.projectId != null)
          return false;
      } else if (!projectId.equals(other.projectId))
        return false;
      return true;
    }

  }
}

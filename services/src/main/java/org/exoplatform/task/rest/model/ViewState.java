package org.exoplatform.task.rest.model;

import org.exoplatform.task.domain.Priority;
import org.exoplatform.task.util.TaskUtil;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public class ViewState {

  private String id;
  private String orderBy;
  private String groupBy;

  public ViewState(String id) {
    this.id = id;
  }

  public String getId() {
    return this.id;
  }

  public static String buildId(Long projectId, Long labelId, String filter) {
    StringBuilder sBuilder = new StringBuilder();

    if (labelId != null && labelId > 0) {
      sBuilder.append("label@").append(labelId);
    } else if (projectId!=null && projectId > 0) {
      sBuilder.append("project@").append(projectId);
    } else {
      sBuilder.append("due@").append(filter);
    }

    return sBuilder.toString();
  }

  public String getOrderBy() {
    return orderBy;
  }

  public void setOrderBy(String orderBy) {
    this.orderBy = orderBy;
  }

  public void setGroupBy(String groupBy) {
    this.groupBy = groupBy;
  }

  public String getGroupBy() {
    return groupBy;
  }

  public boolean isEmpty() {
    return this.groupBy == null && this.orderBy == null;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    ViewState viewState = (ViewState) o;
    return Objects.equals(id, viewState.id) &&
            Objects.equals(orderBy, viewState.orderBy) &&
            Objects.equals(groupBy, viewState.groupBy);
  }

  @Override
  public int hashCode() {
    return Objects.hash(id, orderBy, groupBy);
  }

  public static class Filter {
    private final String id;
    private boolean enabled;
    private String keyword = "";
    private Long status;
    private boolean completed;
    private List<Long> labels = Collections.emptyList();
    private List<String> assignees = Collections.emptyList();
    private List<String> coworkers = Collections.emptyList();
    private List<String> watchers = Collections.emptyList();
    private TaskUtil.DUE due;
    private Priority priority;

    public Filter(String id) {
      this.id = id;
    }

    @Override
    public boolean equals(Object o) {
      if (o == this)
        return true;
      if (!(o instanceof Filter))
        return false;
      final Filter other = (Filter) o;
      if (!other.canEqual((Object) this))
        return false;
      String thisId = this.getId();
      String otherId = other.getId();
      if (thisId == null ? otherId != null : !thisId.equals(otherId))
        return false;
      if (this.isEnabled() != other.isEnabled())
        return false;
      String thisKeyword = this.getKeyword();
      String otherKeyword = other.getKeyword();
      if (thisKeyword == null ? otherKeyword != null : !thisKeyword.equals(otherKeyword))
        return false;
      Long thisStatus = this.getStatus();
      Long otherStatus = other.getStatus();
      if (thisStatus == null ? otherStatus != null : !thisStatus.equals(otherStatus))
        return false;
      if (this.isShowCompleted() != other.isShowCompleted())
        return false;
      String thisLabels = this.getLabelsInString();
      String otherLabels = other.getLabelsInString();
      if (thisLabels == null ? otherLabels != null : !thisLabels.equals(otherLabels))
        return false;
      List<String> thisAssignees = this.getAssignees();
      List<String> otherAssignees = other.getAssignees();
      if (thisAssignees == null ? otherAssignees != null : !thisAssignees.equals(otherAssignees))
        return false;
      List<String> thisCoworkers = this.getCoworkers();
      List<String> otherCoworkers = other.getCoworkers();
      if (thisCoworkers == null ? otherCoworkers != null : !thisCoworkers.equals(otherCoworkers))
        return false;
      List<String> thisWatchers = this.getWatchers();
      List<String> otherWatchers = other.getWatchers();
      if (thisWatchers == null ? otherWatchers != null : !thisWatchers.equals(otherWatchers))
        return false;
      TaskUtil.DUE thisDue = this.getDue();
      TaskUtil.DUE otherDue = other.getDue();
      if (thisDue == null ? otherDue != null : !thisDue.equals(otherDue))
        return false;
      Priority thisPriority = this.getPriority();
      Priority otherOriority = other.getPriority();
      return thisPriority == null ? otherOriority == null : thisPriority.equals(otherOriority);
    }

    protected boolean canEqual(final Object other) {
      return other instanceof Filter;
    }

    @Override
    public int hashCode() {
      final int PRIME = 59;
      int result = 1;
      String thisId = this.getId();
      result = result * PRIME + (thisId == null ? 43 : thisId.hashCode());
      result = result * PRIME + (this.isEnabled() ? 79 : 97);
      String thisKeyword = this.getKeyword();
      result = result * PRIME + (thisKeyword == null ? 43 : thisKeyword.hashCode());
      Long thisStatus = this.getStatus();
      result = result * PRIME + (thisStatus == null ? 43 : thisStatus.hashCode());
      result = result * PRIME + (this.isShowCompleted() ? 79 : 97);
      String thisLabels = this.getLabelsInString();
      result = result * PRIME + (thisLabels == null ? 43 : thisLabels.hashCode());
      List<String> thisAssignees = this.getAssignees();
      result = result * PRIME + (thisAssignees == null ? 43 : thisAssignees.hashCode());
      List<String> thisCoworkers = this.getCoworkers();
      result = result * PRIME + (thisCoworkers == null ? 43 : thisCoworkers.hashCode());
       List<String> thisWatchers = this.getWatchers();
      result = result * PRIME + (thisWatchers == null ? 43 : thisWatchers.hashCode());
      TaskUtil.DUE thisDue = this.getDue();
      result = result * PRIME + (thisDue == null ? 43 : thisDue.hashCode());
      Priority thisPriority = this.getPriority();
      result = result * PRIME + (thisPriority == null ? 43 : thisPriority.hashCode());
      return result;
    }

    public String getId() {
      return this.id;
    }

    public boolean isEnabled() {
      return enabled;
    }

    public void setEnabled(boolean enabled) {
      this.enabled = enabled;
    }

    public String getKeyword() {
      return keyword;
    }

    public void setKeyword(String keyword) {
      this.keyword = keyword;
    }

    public List<Long> getLabel() {
      return labels;
    }

    public String getLabelsInString() {
      if (labels != null) {
        StringBuilder sb = new StringBuilder();
        for (Long id: labels) {
          sb.append(id.toString()).append(",");
        }

        return sb.toString();
      }
      return null;
    }

    public void setLabels(String labels) {
      if (labels != null) {
        List<Long> searchLabelIds = new ArrayList<>();
        for (String id : labels.split(",")) {
          if (!(id = id.trim()).isEmpty()) {
            try {
              searchLabelIds.add(Long.parseLong(id));
            } catch (Exception ex) {
            }
          }
        }
        this.labels = searchLabelIds;
      }
    }

    public Long getStatus() {
      return status;
    }

    public void setStatus(Long statusId) {
      this.status = statusId;
    }

    public List<String> getAssignees() {
      return assignees;
    }

    public List<String> getCoworkers() {
      return coworkers;
    }
    public List<String> getWatchers() {
      return watchers;
    }

    public String getAssigneesInString() {
      if (assignees != null) {
        return String.join(",", assignees);
      }
      return null;
    }

    public void setAssignees(String assignees) {
      if (assignees != null) {
        List<String> searchAssignee = new ArrayList<>();
        for (String u : assignees.split(",")) {
          if (!(u = u.trim()).isEmpty()) {
            searchAssignee.add(u);
          }
        }
        this.assignees = searchAssignee;
      }
    }

    public String getCoworkersInString() {
      if (coworkers != null) {
        return String.join(",", coworkers);
      }
      return null;
    }

    public void setCoworkers(String coworkers) {
      if (coworkers != null) {
        List<String> searchCoworkers = new ArrayList<>();
        for (String u : coworkers.split(",")) {
          if (!(u = u.trim()).isEmpty()) {
            searchCoworkers.add(u);
          }
        }
        this.coworkers = searchCoworkers;
      }
    }

    public void setWatchers(String watchers) {
      if (watchers != null) {
        List<String> searchWatchers = new ArrayList<>();
        for (String u : watchers.split(",")) {
          if (!(u = u.trim()).isEmpty()) {
            searchWatchers.add(u);
          }
        }
        this.watchers = searchWatchers;
      }
    }

    public TaskUtil.DUE getDue() {
      return due;
    }

    public void setDue(TaskUtil.DUE due) {
      this.due = due;
    }

    public Priority getPriority() {
      return priority;
    }

    public void setPriority(Priority priority) {
      this.priority = priority;
    }

    public boolean isShowCompleted() {
      return completed;
    }

    public void setShowCompleted(boolean bln) {
      this.completed = bln;
    }

    public void updateFilterData(String filterLabelIds,
                                 String statusId,
                                 String dueDate,
                                 String priority,
                                 String assignee,
                                 String coworker,
                                 String watcher,
                                 Boolean showCompleted,
                                 String keyword) {
      if (filterLabelIds!=null) {
        this.setLabels(filterLabelIds);
      }else {
        this.getLabel().clear();
      }
      if (assignee!=null) {
        this.setAssignees(assignee);
      }else {
        this.getAssignees().clear();
      }
      if (coworker!=null) {
        this.setCoworkers(coworker);
      }else {
        this.getCoworkers().clear();
      }
      if (watcher!=null) {
        this.setWatchers(watcher);
      }else {
        this.getWatchers().clear();
      }
      if (keyword != null) {
        this.setKeyword(keyword);
      }else{
        this.setKeyword(null);
      }

      if (statusId != null) {
        if (statusId.isEmpty()) {
          this.setStatus(null);
        } else {
          this.setStatus(Long.parseLong(statusId));
        }
      }
      else {
        this.setStatus(null);
      }

      if (dueDate != null) {
        if (dueDate.isEmpty()) {
          this.setDue(null);
        } else {
          this.setDue(TaskUtil.DUE.valueOf(dueDate.toUpperCase()));
        }
      }else {
        this.setDue(null);
      }
      if (priority != null) {
        if (priority.isEmpty()) {
          this.setPriority(null);
        } else {
          this.setPriority(Priority.valueOf(priority.toUpperCase()));
        }
      }else {
        this.setPriority(null);
      }
      if (showCompleted != null) {
        this.setShowCompleted(showCompleted);
      }else {
        this.setShowCompleted(false);
      }
    }
  }
}

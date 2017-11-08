package org.exoplatform.task.management.model;

import org.exoplatform.task.domain.Priority;
import org.exoplatform.task.util.TaskUtil;

import java.util.*;

public class ViewState {

  private String id;
  private String orderBy;
  private String groupBy;
  private ViewType viewType;

  public ViewState(String id) {
    this.id = id;
  }

  public String getId() {
    return this.id;
  }

  public static String buildId(Long projectId, Long labelId, String filter) {
    StringBuilder sBuilder = new StringBuilder();

    if (labelId > 0) {
      sBuilder.append("label@").append(labelId);
    } else if (projectId > 0) {
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

  public void setViewType(ViewType viewType) {
    this.viewType = viewType;
  }

  public ViewType getViewType() {
    return viewType;
  }

  public static class Filter {
    private final String id;
    private boolean enabled;
    private String keyword = "";
    private Long status;
    private boolean completed;
    private List<Long> labels = Collections.emptyList();
    private List<String> assignees = Collections.emptyList();
    private TaskUtil.DUE due;
    private Priority priority;

    public Filter(String id) {
      this.id = id;
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
                                 Boolean showCompleted,
                                 String keyword) {
      this.setLabels(filterLabelIds);
      this.setAssignees(assignee);

      if (keyword != null) {
        this.setKeyword(keyword);
      }

      if (statusId != null) {
        if (statusId.isEmpty()) {
          this.setStatus(null);
        } else {
          this.setStatus(Long.parseLong(statusId));
        }
      }

      if (dueDate != null) {
        if (dueDate.isEmpty()) {
          this.setDue(null);
        } else {
          this.setDue(TaskUtil.DUE.valueOf(dueDate.toUpperCase()));
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
  }
}

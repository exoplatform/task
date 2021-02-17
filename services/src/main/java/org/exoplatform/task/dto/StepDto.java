package org.exoplatform.task.dto;

import java.io.Serializable;
import java.util.Date;

import lombok.Data;

@Data
public class StepDto implements Serializable {
  private long    id;

  private String  author;

  private String  name;

  private Date    createdTime;

  private TaskDto task;

  private boolean completed;

}

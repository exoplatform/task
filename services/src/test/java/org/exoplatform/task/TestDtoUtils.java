/**
 * Copyright (C) 2003-2020 eXo Platform SAS.
 * <p>
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * <p>
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 * <p>
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program. If not, see http://www.gnu.org/licenses/ .
 */

package org.exoplatform.task;

import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import org.exoplatform.task.domain.Priority;
import org.exoplatform.task.dto.CommentDto;
import org.exoplatform.task.dto.ProjectDto;
import org.exoplatform.task.dto.StatusDto;
import org.exoplatform.task.dto.TaskDto;

public class TestDtoUtils {

  public static long        EXISTING_TASK_ID      = 1;

  public static long        UNEXISTING_TASK_ID    = 2;

  public static long        EXISTING_PROJECT_ID   = 1;

  public static long        UNEXISTING_PROJECT_ID = 2;

  public static long        EXISTING_STATUS_ID    = 1;

  public static long        UNEXISTING_STATUS_ID  = 2;

  public static long        EXISTING_COMMENT_ID   = 1;

  public static long        UNEXISTING_COMMENT_ID = 2;

  public static TaskDto getDefaultTask() {
    return getDefaultTaskWithId(EXISTING_TASK_ID);
  }

  public static TaskDto getDefaultTaskWithId(long id) {
    TaskDto task = new TaskDto();
    task.setId(id);
    task.setTitle("Default task");
    task.setAssignee("root");
    task.setCreatedBy("root");
    task.setPriority(Priority.NORMAL);
    task.setCreatedTime(new Date());
    Set<String> coworker = new HashSet<String>();
    task.setCoworker(coworker);
    return task;
  }

  public static CommentDto getDefaultComment() {
    CommentDto comment = new CommentDto();
    comment.setId(EXISTING_COMMENT_ID);
    comment.setComment("Bla bla");
    comment.setAuthor("Tib");
    comment.setCreatedTime(new Date());
    return comment;
  }

  public static StatusDto getDefaultStatus() {
    StatusDto status = new StatusDto();
    status.setId(EXISTING_STATUS_ID);
    status.setName("TODO");
    status.setRank(1);
    return status;
  }

  public static ProjectDto getDefaultProject() {
    ProjectDto project = new ProjectDto();
    project.setId(EXISTING_PROJECT_ID);
    project.setName("Default project");
    project.setDescription("The default project");
    project.setDueDate(new Date());
    project.setLastModifiedDate(System.currentTimeMillis());
    Set<String> participator = new HashSet<String>();
    participator.add("Tib");
    project.setParticipator(participator);
    Set<String> managers = new HashSet<String>();
    managers.add("Tib");
    project.setManager(managers);
    return project;
  }

  public static ProjectDto getParentProject() {
    ProjectDto project = new ProjectDto();
    project.setId(EXISTING_PROJECT_ID);
    project.setName("Default project");
    project.setDescription("The default project");
    project.setDueDate(new Date());
    Set<String> participator = new HashSet<String>();
    participator.add("Tib");
    project.setParticipator(participator);
    Set<String> managers = new HashSet<String>();
    managers.add("Tib");
    project.setManager(managers);
    return project;
  }

}

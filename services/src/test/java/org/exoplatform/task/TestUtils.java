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
package org.exoplatform.task;

import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import org.exoplatform.social.core.identity.model.Profile;
import org.exoplatform.social.core.identity.provider.OrganizationIdentityProvider;
import org.exoplatform.task.domain.Comment;
import org.exoplatform.task.domain.Project;
import org.exoplatform.task.domain.Status;
import org.exoplatform.task.domain.Task;
import org.exoplatform.task.dto.CommentDto;
import org.exoplatform.task.dto.LabelDto;
import org.exoplatform.task.dto.ProjectDto;
import org.exoplatform.task.dto.StatusDto;
import org.exoplatform.task.dto.TaskDto;
import org.exoplatform.task.dto.UserSettingDto;
import org.exoplatform.task.model.User;

/**
 * Created by The eXo Platform SAS
 * Author : Thibault Clement
 * tclement@exoplatform.com
 * 4/15/15
 */
public class TestUtils {

  public static long EXISTING_TASK_ID = 1;
  public static long UNEXISTING_TASK_ID = 2;

  public static long EXISTING_PROJECT_ID = 1;
  public static long UNEXISTING_PROJECT_ID = 2;

  public static long EXISTING_STATUS_ID = 1;
  public static long UNEXISTING_STATUS_ID = 2;

  public static long EXISTING_COMMENT_ID = 1;
  public static long UNEXISTING_COMMENT_ID = 2;

  public static long EXISTING_LABEL_ID = 1;
  public static long UNEXISTING_LABEL_ID = 2;

  public static Task getDefaultTask() {
    return getDefaultTaskWithId(EXISTING_TASK_ID);
  }

  public static TaskDto getDefaultTaskDto() {
    return getDefaultTaskDtoWithId(EXISTING_TASK_ID);
  }

  public static Task getDefaultTaskWithId(long id) {
    Task task = new Task();
    task.setId(id);
    task.setTitle("Default task");
    task.setAssignee("root");
    task.setCreatedBy("root");
    task.setCreatedTime(new Date());
    return task;
  }
  public static TaskDto getDefaultTaskDtoWithId(long id) {
    TaskDto task = new TaskDto();
    task.setId(id);
    task.setTitle("Default task");
    task.setAssignee("root");
    task.setCreatedBy("root");
    task.setCreatedTime(new Date());
    return task;
  }

  public static Comment getDefaultComment() {
    Comment comment = new Comment();
    comment.setId(EXISTING_COMMENT_ID);
    comment.setComment("Bla bla");
    comment.setAuthor("Tib");
    comment.setCreatedTime(new Date());
    comment.setTask(getDefaultTask());
    return comment;
  }

  public static Comment getDefaultCommentWithMention() {
    Comment comment = new Comment();
    comment.setId(EXISTING_COMMENT_ID);
    comment.setComment("Bla bla @testa");
    comment.setAuthor("Tib");
    comment.setCreatedTime(new Date());
    comment.setTask(getDefaultTask());
    return comment;
  }

  public static CommentDto getDefaultCommentDto() {
    CommentDto comment = new CommentDto();
    comment.setId(EXISTING_COMMENT_ID);
    comment.setComment("Bla bla");
    comment.setAuthor("Tib");
    comment.setCreatedTime(new Date());
    comment.setTask(getDefaultTaskDto());
    return comment;
  }

  public static Status getDefaultStatus() {
    Status status = new Status();
    status.setId(EXISTING_STATUS_ID);
    status.setName("TODO");
    status.setRank(1);
    return status;
  }
  public static StatusDto getDefaultStatusDto() {
    StatusDto status = new StatusDto();
    status.setId(EXISTING_STATUS_ID);
    status.setName("TODO");
    status.setRank(1);
    return status;
  }

  public static UserSettingDto getDefaultUserSettingDto(){
    UserSettingDto userSettingDto = new UserSettingDto();
    userSettingDto.setUsername("user");
    userSettingDto.setShowHiddenProject(true);
    userSettingDto.setShowHiddenLabel(true);
    return userSettingDto;
  }

  public static LabelDto getDefaultLabel() {
    LabelDto labelDto = new LabelDto();
    labelDto.setId(EXISTING_LABEL_ID);
    labelDto.setName("TODO");
    labelDto.setUsername("label");
    return labelDto;
  }

  public static Project getDefaultProject() {
    Project project = new Project();
    project.setId(EXISTING_PROJECT_ID);
    project.setName("Default project");
    project.setDescription("The default project");
    project.setDueDate(new Date());
    Set<String> managers = new HashSet<String>();
    managers.add("Tib");
    project.setManager(managers);
    return project;
  }
  public static ProjectDto getDefaultProjectDto() {
    ProjectDto project = new ProjectDto();
    project.setId(EXISTING_PROJECT_ID);
    project.setName("Default project");
    project.setDescription("The default project");
    project.setDueDate(new Date());
    Set<String> managers = new HashSet<String>();
    managers.add("Tib");
    project.setManager(managers);
    return project;
  }

  public static User getUser() {
    User user = new User();
    user.setUsername("root");
    user.setDisplayName("root");
    user.setFirstName("root");
    user.setLastName("root");
    user.setEmail("root@gmail.com");

    return user;
  }

  public static User getUserA() {
    User user = new User();
    user.setUsername("userA");
    user.setDisplayName("userA");
    user.setFirstName("userA");
    user.setLastName("userA");
    user.setEmail("userA@gmail.com");

    return user;
  }

  public static org.exoplatform.social.core.identity.model.Identity getUserAIdentity() {
    org.exoplatform.social.core.identity.model.Identity userIdentity = new org.exoplatform.social.core.identity.model.Identity(OrganizationIdentityProvider.NAME, "userA");

    userIdentity.setEnable(true);
    userIdentity.setDeleted(false);
    userIdentity.setRemoteId("userA");

    Profile userProfile = new Profile(userIdentity);
    userProfile.setProperty(Profile.FULL_NAME, "userA");
    userProfile.setProperty(Profile.AVATAR, "/userA.png");
    userIdentity.setProfile(userProfile);

    return userIdentity;
  }

}


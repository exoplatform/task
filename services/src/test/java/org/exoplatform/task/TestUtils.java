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

import liquibase.Liquibase;
import liquibase.database.Database;
import liquibase.database.DatabaseFactory;
import liquibase.database.jvm.JdbcConnection;
import liquibase.exception.LiquibaseException;
import liquibase.resource.ClassLoaderResourceAccessor;
import org.exoplatform.task.domain.Comment;
import org.exoplatform.task.domain.Project;
import org.exoplatform.task.domain.Status;
import org.exoplatform.task.domain.Task;
import org.exoplatform.task.dto.*;
import org.exoplatform.task.model.User;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

/**
 * Created by The eXo Platform SAS
 * Author : Thibault Clement
 * tclement@exoplatform.com
 * 4/15/15
 */
public class TestUtils {

  private static Connection conn;
  private static Liquibase liquibase;

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

  public static void initH2DB() throws SQLException,
      ClassNotFoundException, LiquibaseException {

    Class.forName("org.h2.Driver");
    conn = DriverManager.getConnection("jdbc:h2:target/h2-db", "sa", "");

    initDB();
  }

  public static void initHSQLDB() throws LiquibaseException, SQLException,
      ClassNotFoundException {

    Class.forName("org.hsqldb.jdbcDriver");
    conn = DriverManager.getConnection("jdbc:hsqldb:file:target/hsql-db", "sa", "");

    initDB();
  }

  private static void initDB() throws LiquibaseException {
    Database database = DatabaseFactory.getInstance()
        .findCorrectDatabaseImplementation(new JdbcConnection(conn));

    liquibase = new Liquibase("db/changelog/task.db.changelog-1.0.0.xml", new ClassLoaderResourceAccessor(), database);
    liquibase.update((String)null);
    liquibase = new Liquibase("db/changelog/task.db.changelog-1.1.0.xml", new ClassLoaderResourceAccessor(), database);
    liquibase.update((String)null);
    liquibase = new Liquibase("db/changelog/task.db.changelog-1.3.0.xml", new ClassLoaderResourceAccessor(), database);
    liquibase.update((String)null);
    liquibase = new Liquibase("db/changelog/task.db.changelog-2.1.0.xml", new ClassLoaderResourceAccessor(), database);
    liquibase.update((String)null);
    liquibase = new Liquibase("db/changelog/task.db.changelog-3.0.0.xml", new ClassLoaderResourceAccessor(), database);
    liquibase.update((String)null);
    liquibase = new Liquibase("db/changelog/task.db.changelog-3.1.0.xml", new ClassLoaderResourceAccessor(), database);
    liquibase.update((String)null);
  }

  public static void closeDB() throws LiquibaseException, SQLException {
    liquibase.rollback(1000, null);
    conn.close();
  }

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

}


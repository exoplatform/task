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

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import org.exoplatform.task.domain.Priority;
import org.exoplatform.task.dto.CommentDto;
import org.exoplatform.task.dto.ProjectDto;
import org.exoplatform.task.dto.StatusDto;
import org.exoplatform.task.dto.TaskDto;

import liquibase.Liquibase;
import liquibase.database.Database;
import liquibase.database.DatabaseFactory;
import liquibase.database.jvm.JdbcConnection;
import liquibase.exception.LiquibaseException;
import liquibase.resource.ClassLoaderResourceAccessor;

public class TestDtoUtils {

  private static Connection conn;

  private static Liquibase  liquibase;

  public static long        EXISTING_TASK_ID      = 1;

  public static long        UNEXISTING_TASK_ID    = 2;

  public static long        EXISTING_PROJECT_ID   = 1;

  public static long        UNEXISTING_PROJECT_ID = 2;

  public static long        EXISTING_STATUS_ID    = 1;

  public static long        UNEXISTING_STATUS_ID  = 2;

  public static long        EXISTING_COMMENT_ID   = 1;

  public static long        UNEXISTING_COMMENT_ID = 2;

  public static void initH2DB() throws SQLException, ClassNotFoundException, LiquibaseException {

    Class.forName("org.h2.Driver");
    conn = DriverManager.getConnection("jdbc:h2:target/h2-db", "sa", "");

    initDB();
  }

  public static void initHSQLDB() throws LiquibaseException, SQLException, ClassNotFoundException {

    Class.forName("org.hsqldb.jdbcDriver");
    conn = DriverManager.getConnection("jdbc:hsqldb:file:target/hsql-db", "sa", "");

    initDB();
  }

  private static void initDB() throws LiquibaseException {
    Database database = DatabaseFactory.getInstance().findCorrectDatabaseImplementation(new JdbcConnection(conn));

    liquibase = new Liquibase("db/changelog/task.db.changelog-1.0.0.xml", new ClassLoaderResourceAccessor(), database);
    liquibase.update((String) null);
    liquibase = new Liquibase("db/changelog/task.db.changelog-1.1.0.xml", new ClassLoaderResourceAccessor(), database);
    liquibase.update((String) null);
    liquibase = new Liquibase("db/changelog/task.db.changelog-1.3.0.xml", new ClassLoaderResourceAccessor(), database);
    liquibase.update((String) null);
    liquibase = new Liquibase("db/changelog/task.db.changelog-2.1.0.xml", new ClassLoaderResourceAccessor(), database);
    liquibase.update((String) null);
    liquibase = new Liquibase("db/changelog/task.db.changelog-3.0.0.xml", new ClassLoaderResourceAccessor(), database);
    liquibase.update((String) null);
    liquibase = new Liquibase("db/changelog/task.db.changelog-3.1.0.xml", new ClassLoaderResourceAccessor(), database);
    liquibase.update((String) null);
    liquibase = new Liquibase("db/changelog/task.db.changelog-3.2.0.xml", new ClassLoaderResourceAccessor(), database);
    liquibase.update((String) null);
  }

  public static void closeDB() throws LiquibaseException, SQLException {
    liquibase.rollback(1000, null);
    conn.close();
  }

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

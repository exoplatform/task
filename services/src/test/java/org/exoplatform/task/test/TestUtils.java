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
package org.exoplatform.task.test;

import liquibase.Liquibase;
import liquibase.database.Database;
import liquibase.database.DatabaseFactory;
import liquibase.database.jvm.JdbcConnection;
import liquibase.exception.LiquibaseException;
import liquibase.resource.FileSystemResourceAccessor;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * Created by The eXo Platform SAS
 * Author : Thibault Clement
 * tclement@exoplatform.com
 * 4/15/15
 */
public class TestUtils {

  private static Connection conn;
  private static Liquibase liquibase;

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

    liquibase = new Liquibase("../task-management/src/main/resources/db/changelog/db.changelog-1.0.0.xml",
        new FileSystemResourceAccessor(), database);
    liquibase.update((String)null);
  }

  public static void closeDB() throws LiquibaseException, SQLException {
    //liquibase.rollback(1000, null);
    conn.close();
  }
}


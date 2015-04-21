package org.exoplatform.task.test;

import java.sql.SQLException;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;

import liquibase.exception.LiquibaseException;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

public class EntityManagerTest {

  private EntityManagerFactory entityManagerFactory;
  private EntityManager entityManager;

  @BeforeClass
  public static void createTable() throws SQLException,
      ClassNotFoundException, LiquibaseException {
    TestUtils.initH2DB();
  }

  @AfterClass
  public static void removeTable() throws LiquibaseException, SQLException {
    TestUtils.closeDB();
  }

  @Before
  public void initEntityManager() throws Exception {
    entityManagerFactory = Persistence.createEntityManagerFactory("org.exoplatform.task");
    entityManager = entityManagerFactory.createEntityManager();
    // Start transaction
    entityManager.getTransaction().begin();
  }


  @After
  public void closeEntityManager() throws Exception {
    // Close Transaction
    entityManager.close();
    entityManagerFactory.close();
  }

  @Test
  public void testEntityManagerFactoryNotNull() {
    Assert.assertNotNull(entityManagerFactory);
  }
}

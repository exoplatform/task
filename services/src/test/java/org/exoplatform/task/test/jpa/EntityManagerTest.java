package org.exoplatform.task.test.jpa;

import liquibase.exception.LiquibaseException;
import org.exoplatform.task.test.TestUtils;
import org.junit.*;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import java.sql.SQLException;

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

package org.exoplatform.task.test;

import liquibase.exception.LiquibaseException;
import org.exoplatform.task.domain.Project;
import org.junit.*;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import java.sql.SQLException;
import java.util.List;

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

  @Test
  public void testCreationTask() {
    Project project = new Project();
    project.setName("test project");

    // Add some tasks in DB
    entityManager.persist(project);

    // Commit
    entityManager.getTransaction().commit();

    // Queries all projects from DB
    List<Project> result = entityManager.createQuery("from Project", Project.class).getResultList();
    Assert.assertEquals(project.getName(), result.get(0).getName());
  }

}

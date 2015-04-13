package org.exoplatform.task.test;

import liquibase.Liquibase;
import liquibase.database.Database;
import liquibase.database.DatabaseFactory;
import liquibase.database.jvm.JdbcConnection;
import liquibase.exception.LiquibaseException;
import liquibase.resource.FileSystemResourceAccessor;
import org.exoplatform.task.domain.Project;
import org.junit.*;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.List;

public class EntityManagerTest {

  private EntityManagerFactory entityManagerFactory;
  private EntityManager entityManager;
  private static Connection conn;
  private static Liquibase liquibase;

  @BeforeClass
  public static void createTable() throws SQLException,
      ClassNotFoundException, LiquibaseException {

    Class.forName("org.h2.Driver");
    conn = DriverManager.getConnection("jdbc:h2:target/h2-db", "sa", "");

    Database database = DatabaseFactory.getInstance()
        .findCorrectDatabaseImplementation(new JdbcConnection(conn));

    liquibase = new Liquibase("../task-management/src/main/resources/db/changelog/db.changelog-1.0.0.xml",
        new FileSystemResourceAccessor(), database);
    liquibase.update((String)null);

  }

  @AfterClass
  public static void removeTable() throws LiquibaseException, SQLException {
    //liquibase.rollback(1000, null);
    conn.close();
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


package org.exoplatform.task.domain;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;

import java.util.List;

import junit.framework.TestCase;

public class EntityManagerTest extends TestCase {
    private EntityManagerFactory entityManagerFactory;

    @Override
    protected void setUp() throws Exception {
        entityManagerFactory = Persistence.createEntityManagerFactory( "org.exoplatform.task" );
    }

    @Override
    protected void tearDown() throws Exception {
        entityManagerFactory.close();
    }

    public void testEntityManagerFactoryNotNull() {
        assertNotNull(entityManagerFactory);
    }

    public void testCreationTask() {
        Project project = new Project();
        project.setName("test project");

        //Add some tasks in DB
        EntityManager entityManager = entityManagerFactory.createEntityManager();
        //Start transaction
        entityManager.getTransaction().begin();

        entityManager.persist(project);

        //Commit
        entityManager.getTransaction().commit();
        //Close Transaction
        entityManager.close();

        // Get Task and display them
        entityManager = entityManagerFactory.createEntityManager();
        //Queries all projects from DB
        List<Project> result = entityManager.createQuery( "from Project", Project.class ).getResultList();
        assertEquals(project.getName(), result.get(0).getName());
        entityManager.close();
    }
}

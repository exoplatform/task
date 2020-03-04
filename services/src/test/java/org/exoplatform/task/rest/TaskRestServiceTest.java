package org.exoplatform.task.rest;

import org.exoplatform.services.rest.impl.RuntimeDelegateImpl;
import org.exoplatform.services.security.ConversationState;
import org.exoplatform.services.security.Identity;
import org.exoplatform.task.domain.Project;
import org.exoplatform.task.domain.Status;
import org.exoplatform.task.domain.Task;
import org.exoplatform.task.service.ProjectService;
import org.exoplatform.task.service.StatusService;
import org.exoplatform.task.service.TaskService;
import org.junit.Before;
import org.junit.Test;

import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import javax.ws.rs.core.Response;
import javax.ws.rs.ext.RuntimeDelegate;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class TaskRestServiceTest {
    @Mock
    TaskService taskService;
    @Mock
    ProjectService projectService;
    @Mock
    StatusService statusService;


    @Before
    public void setup() {
        RuntimeDelegate.setInstance(new RuntimeDelegateImpl());
    }

    @Test
    public void testGetTasks() throws Exception {
        // Given
        TaskRestService taskRestService = new TaskRestService(taskService, projectService, statusService);
        Identity root = new Identity("root");
        ConversationState.setCurrent(new ConversationState(root));
        Task task1 = new Task();
        Task task2 = new Task();
        Task task3 = new Task();
        Task task4 = new Task();
        List<Task> uncompletedTasks = new ArrayList<Task>();
        task1.setCompleted(true);
        uncompletedTasks.add(task2);
        uncompletedTasks.add(task3);
        uncompletedTasks.add(task4);
        List<Task> overdueTasks = new ArrayList<Task>();
        overdueTasks.add(task1);
        overdueTasks.add(task2);

        when(taskService.getUncompletedTasks("root")).thenReturn(uncompletedTasks);
        when(taskService.countUncompletedTasks("root")).thenReturn(Long.valueOf(uncompletedTasks.size()));
        when(taskService.getOverdueTasks("root")).thenReturn(overdueTasks);
        when(taskService.countOverdueTasks("root")).thenReturn(Long.valueOf(overdueTasks.size()));

        // When
        Response response = taskRestService.getTasks("overdue", 0, 20, false);
        Response response1 = taskRestService.getTasks("", 0, 20, false);

        // Then
        assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
        List<Task> tasks = (List<Task>) response.getEntity();
        assertNotNull(tasks);
        assertEquals(2, tasks.size());

        assertEquals(Response.Status.OK.getStatusCode(), response1.getStatus());
        List<Task> tasks1 = (List<Task>) response1.getEntity();
        assertNotNull(tasks1);
        assertEquals(3, tasks1.size());

    }

    @Test
    public void testUpdateTaskById() throws Exception {
        // Given
        TaskRestService taskRestService = new TaskRestService(taskService, projectService, statusService);
        Identity john = new Identity("john");
        ConversationState.setCurrent(new ConversationState(john));
        Task task1 = new Task();
        Task task2 = new Task();
        task1.setId(1);
        task1.setTitle("oldTask");
        task1.setCreatedBy("john");
        task1.setAssignee("john");
        task2.setId(2);
        task2.setTitle("updatedTask");
        taskService.createTask(task1);
        taskService.createTask(task2);


        when(taskService.getTask(1)).thenReturn(task1);
        when(taskService.updateTask(task2)).thenReturn(task2);


        // When
        Response response = taskRestService.updateTaskById(1, task2);

        // Then
        assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
        Task task = (Task) response.getEntity();
        assertNotNull(task);
        assertEquals("updatedTask", task.getTitle());
    }

    @Test
    public void testGetDefaultStatusByProjectId() throws Exception {
        // Given
        TaskRestService taskRestService = new TaskRestService(taskService, projectService, statusService);
        Identity john = new Identity("john");
        ConversationState.setCurrent(new ConversationState(john));
        Project project = new Project();
        project.setId(1);
        Set<String> manager = new HashSet<String>();
        manager.add("john");
        project.setManager(manager);
        Status status = new Status();
        status.setId(1);
        status.setName("status 1");

        when(projectService.getProject(1L)).thenReturn(project);
        when(statusService.getDefaultStatus(1L)).thenReturn(status);


        // When
        Response response = taskRestService.getDefaultStatusByProjectId(1);

        // Then
        assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
        Status status1 = (Status) response.getEntity();
        assertNotNull(status1);
        assertEquals("status 1", status1.getName());
    }
}
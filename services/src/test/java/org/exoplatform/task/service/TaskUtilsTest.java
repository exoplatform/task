package org.exoplatform.task.service;


import org.exoplatform.calendar.model.Event;
import org.exoplatform.container.ExoContainer;
import org.exoplatform.container.ExoContainerContext;
import org.exoplatform.container.PortalContainer;
import org.exoplatform.services.idgenerator.IDGeneratorService;
import org.exoplatform.services.jcr.util.IdGenerator;
import org.exoplatform.services.listener.ListenerService;
import org.exoplatform.services.security.ConversationState;
import org.exoplatform.services.security.Identity;
import org.exoplatform.task.TestUtils;
import org.exoplatform.task.dao.*;
import org.exoplatform.task.domain.Comment;
import org.exoplatform.task.domain.Project;
import org.exoplatform.task.domain.Status;
import org.exoplatform.task.domain.Task;
import org.exoplatform.task.model.User;
import org.exoplatform.task.service.impl.TaskServiceImpl;
import org.exoplatform.task.util.ProjectUtil;
import org.exoplatform.task.util.TaskUtil;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.runners.MockitoJUnitRunner;
import org.mockito.stubbing.Answer;

import java.util.Collections;
import java.util.Date;
import java.util.Locale;
import java.util.ResourceBundle;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Mockito.when;


@RunWith(MockitoJUnitRunner.class)
public class TaskUtilsTest {

    private TaskService taskService;

    private ListenerService listenerService;

    private ResourceBundle rb_en;

    @Mock
    private ExoContainer container;
    @Mock
    private TaskHandler taskHandler;
    @Mock
    private CommentHandler commentHandler;
    @Mock
    private StatusHandler statusHandler;
    @Mock
    private LabelHandler labelHandler;
    @Mock
    private DAOHandler daoHandler;
    @Mock
    private UserService userService;
    @Mock
    IdGenerator idGenerator;
    @Mock
    private IDGeneratorService idGeneratorService;

    //ArgumentCaptors are how you can retrieve objects that were passed into a method call
    @Captor
    private ArgumentCaptor<Comment> commentCaptor;

    @Before
    public void setUp() throws Exception {
        // Make sure the container is started to prevent the ExoTransactional annotation to fail
        PortalContainer.getInstance();

        listenerService = new ListenerService(new ExoContainerContext(container));

        //Mock DAO handler to return Mocked DAO
        when(daoHandler.getTaskHandler()).thenReturn(taskHandler);
        when(daoHandler.getCommentHandler()).thenReturn(commentHandler);
        when(daoHandler.getStatusHandler()).thenReturn(statusHandler);
        when(daoHandler.getLabelHandler()).thenReturn(labelHandler);

        taskService = new TaskServiceImpl(daoHandler, listenerService);

        //Mock some DAO methods
        when(taskHandler.create(any(Task.class))).thenReturn(TestUtils.getDefaultTask());
        when(taskHandler.update(any(Task.class))).thenReturn(TestUtils.getDefaultTask());
        //Mock taskHandler.find(id) to return default task for id = TestUtils.EXISTING_TASK_ID (find(id) return null otherwise)
        when(taskHandler.find(TestUtils.EXISTING_TASK_ID)).thenReturn(TestUtils.getDefaultTask());
        when(taskHandler.getCoworker(anyLong())).thenReturn(Collections.emptySet());
        when(labelHandler.findLabelsByTask(anyLong(), any())).thenReturn(null);
        when(statusHandler.find(TestUtils.EXISTING_STATUS_ID)).thenReturn(TestUtils.getDefaultStatus());
        when(commentHandler.find(TestUtils.EXISTING_COMMENT_ID)).thenReturn(TestUtils.getDefaultComment());
        IdGenerator idGenerator = new IdGenerator(idGeneratorService);
        when(idGeneratorService.generateStringID(any(String.class))).thenReturn(Long.toString(System.currentTimeMillis()));
        when(userService.loadUser(any())).thenAnswer(new Answer<User>() {
            @Override
            public User answer(InvocationOnMock invocation) throws Throwable {
                return new User(invocation.getArguments()[0].toString(), null, null, null, null, null, null);
            }
        });

        Identity root = new Identity("root");
        ConversationState.setCurrent(new ConversationState(root));
        rb_en = ResourceBundle.getBundle("locale.portlet.taskManagement", Locale.ENGLISH);
    }

    @After
    public void tearDown() {
        taskService = null;
        ConversationState.setCurrent(null);
    }


    @Test
    public void testTaskEvent() throws Exception {

        Task task = TestUtils.getDefaultTask();
        task.setStartDate(new Date());
        task.setEndDate(new Date());
        Status status = TestUtils.getDefaultStatus();
        Project project = TestUtils.getDefaultProject();
        Event event = new Event();
        TaskUtil.buildEvent(event,task);

        Assert.assertEquals(String.valueOf(ProjectUtil.TODO_PROJECT_ID), event.getCalendarId());

        status.setProject(project);
        task.setStatus(status);
        TaskUtil.buildEvent(event,task);
        Assert.assertEquals(task.getStatus().getProject().getId(),Long.parseLong(event.getCalendarId()));
        Assert.assertEquals(task.getTitle(), event.getSummary());
        Assert.assertEquals(task.getDescription(), event.getDescription());
        status.setProject(project);
    }
}

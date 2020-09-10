package org.exoplatform.task.service;

import org.exoplatform.commons.utils.ListAccess;
import org.exoplatform.task.dto.CommentDto;
import org.exoplatform.task.dto.TaskDto;
import org.exoplatform.task.exception.EntityNotFoundException;

import java.util.List;

public interface CommentService {

    String TASK_COMMENT_CREATION = "exo.task.taskCommentCreation";

    CommentDto getComment(long commentId);

    ListAccess<CommentDto> getComments(long taskId);

    CommentDto addComment(TaskDto task, String username, String commentText) throws EntityNotFoundException;

    CommentDto addComment(TaskDto task, long parentCommentId, String username, String comment) throws EntityNotFoundException;

    void removeComment(long commentId) throws EntityNotFoundException;

    /**
     * Fetch sub comments of designed comments
     *
     * @param listComments the given list of comments.
     */
    void loadSubComments(List<CommentDto> listComments);
}

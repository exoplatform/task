package org.exoplatform.task.service;

import java.util.List;

import org.exoplatform.task.dto.CommentDto;
import org.exoplatform.task.dto.TaskDto;
import org.exoplatform.task.exception.EntityNotFoundException;

public interface CommentService {

  String TASK_COMMENT_CREATION = "exo.task.taskCommentCreation";

  CommentDto getComment(long commentId);

  CommentDto addComment(TaskDto task, String username, String commentText) throws EntityNotFoundException;

  CommentDto addComment(TaskDto task, long parentCommentId, String username, String comment) throws EntityNotFoundException;

  void removeComment(long commentId) throws EntityNotFoundException;

  List<CommentDto> getComments(long taskId, int offset, int limit);

  List<CommentDto> getCommentsWithSubs(long taskId, int offset, int limit);

  int countComments(long taskId);

  /**
   * Fetch sub comments of designed comments
   *
   * @param listComments the given list of comments.
   * @return
   */
  List<CommentDto> loadSubComments(List<CommentDto> listComments);
}

package org.exoplatform.task.storage;

import java.util.List;

import org.exoplatform.task.domain.Comment;
import org.exoplatform.task.dto.CommentDto;
import org.exoplatform.task.dto.TaskDto;
import org.exoplatform.task.exception.EntityNotFoundException;

public interface CommentStorage {

  CommentDto getComment(long commentId);

  List<CommentDto> getCommentsWithSubs(long taskId, int offset, int limit);

  List<CommentDto> getComments(long taskId, int offset, int limit);

  int countComments(long taskId);

  CommentDto addComment(TaskDto task, String username, String commentText) throws EntityNotFoundException;

  CommentDto addComment(TaskDto task, long parentCommentId, String username, String comment) throws EntityNotFoundException;

  void removeComment(long commentId) throws EntityNotFoundException;

  /**
   * Fetch sub comments of designed comments
   *
   * @param listComments the given list of comments.
   * @return List of SubComments
   */
  List<CommentDto> loadSubComments(List<CommentDto> listComments);

}

package org.exoplatform.task.storage.impl;

import org.exoplatform.commons.utils.ListAccess;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.task.dao.DAOHandler;
import org.exoplatform.task.domain.Comment;
import org.exoplatform.task.dto.CommentDto;
import org.exoplatform.task.dto.TaskDto;
import org.exoplatform.task.exception.EntityNotFoundException;
import org.exoplatform.task.storage.CommentStorage;

import javax.inject.Inject;
import java.util.List;
import java.util.Objects;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class CommentStorageImpl implements CommentStorage {

    private static final Log LOG = ExoLogger.getExoLogger(CommentStorageImpl.class);

    private static final Pattern pattern = Pattern.compile("@([^\\s]+)|@([^\\s]+)$");


    @Inject
    private final DAOHandler daoHandler;


    public CommentStorageImpl(DAOHandler daoHandler) {
        this.daoHandler = daoHandler;
    }

    @Override
    public CommentDto getComment(long commentId) {
        return commentToDto(daoHandler.getCommentHandler().find(commentId));
    }

    @Override
    public ListAccess<CommentDto> getComments(long taskId) {
        return (ListAccess<CommentDto>) commentToDto((Comment) daoHandler.getCommentHandler().findComments(taskId));
    }

    @Override
    public CommentDto addComment(TaskDto task, String username, String commentText) throws EntityNotFoundException {
        return null;
    }

    @Override
    public CommentDto addComment(TaskDto task, long parentCommentId, String username, String comment) throws EntityNotFoundException {
        return null;
    }

    @Override
    public void removeComment(long commentId) throws EntityNotFoundException {
        daoHandler.getCommentHandler().delete(commentToEntity(getComment(commentId)));
    }

    @Override
    public void loadSubComments(List<CommentDto> listComments) {
        List<Comment> comments = listComments.stream().map(this::commentToEntity).collect(Collectors.toList());
        daoHandler.getCommentHandler().getSubComments(comments);
    }

    @Override
    public Comment commentToEntity(CommentDto commentDto) {
        Comment comment = new Comment();
        comment.setId(commentDto.getId());
        comment.setAuthor(commentDto.getAuthor());
        comment.setComment(commentDto.getComment());
        comment.setCreatedTime(commentDto.getCreatedTime());
        comment.setTask(commentDto.getTask());
        return comment;
    }

    @Override
    public CommentDto commentToDto(Comment comment) {
        CommentDto commentDto = new CommentDto();
        commentDto.setId(comment.getId());
        commentDto.setAuthor(comment.getAuthor());
        commentDto.setComment(comment.getComment());
        commentDto.setCreatedTime(comment.getCreatedTime());
        commentDto.setTask(comment.getTask());
        return commentDto;
    }

    @Override
    public List<CommentDto> listCommentsToDtos(List<Comment> comments) {
        return comments.stream()
                .filter(Objects::nonNull)
                .map(this::commentToDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<Comment> listCommentsToEntitys(List<CommentDto> commentDtos) {
        return commentDtos.stream()
                .filter(Objects::nonNull)
                .map(this::commentToEntity)
                .collect(Collectors.toList());
    }
}

package org.exoplatform.task.storage.impl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import javax.inject.Inject;

import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.task.dao.DAOHandler;
import org.exoplatform.task.domain.Comment;
import org.exoplatform.task.dto.CommentDto;
import org.exoplatform.task.dto.TaskDto;
import org.exoplatform.task.exception.EntityNotFoundException;
import org.exoplatform.task.storage.CommentStorage;

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
    public List<CommentDto> getCommentsWithSubs(long taskId, int offset, int limit) {
        try {
            List<Comment>  comments =  Arrays.asList(daoHandler.getCommentHandler().findComments(taskId).load(offset, limit));
            List<Comment> subComments = daoHandler.getCommentHandler().getSubComments(comments);
            List<CommentDto> commentsDto = comments.stream().map(this::commentToDto).collect(Collectors.toList());
            List<CommentDto> subCommentsDto = subComments.stream().map(this::commentToDto).collect(Collectors.toList());
            for (CommentDto comment : commentsDto) {
                comment.setSubComments(subCommentsDto.stream()
                        .filter(subComment -> subComment.getParentComment().getId() == comment.getId())
                        .collect(Collectors.toList()));
            }
        return commentsDto;
        } catch (Exception e) {
            return new ArrayList<CommentDto>();
        }
    }

    @Override
    public List<CommentDto> getComments(long taskId, int offset, int limit) {
        try {
        return Arrays.asList(daoHandler.getCommentHandler().findComments(taskId).load(offset, limit)).stream().map(this::commentToDto).collect(Collectors.toList());
        } catch (Exception e) {
            return new ArrayList<CommentDto>();
        }
    }

    @Override
    public int countComments(long taskId) {
        try {
        return daoHandler.getCommentHandler().findComments(taskId).getSize();
        } catch (Exception e) {
            return 0;
        }
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
    public List<CommentDto> loadSubComments(List<CommentDto> listComments) {
        List<Comment> comments = listComments.stream().map(this::commentToEntity).collect(Collectors.toList());
        comments = daoHandler.getCommentHandler().getSubComments(comments);
        return  comments.stream().map(this::commentToDto).collect(Collectors.toList());
    }

    @Override
    public Comment commentToEntity(CommentDto commentDto) {
        if(commentDto==null){
            return null;
        }
        Comment comment = new Comment();
        comment.setId(commentDto.getId());
        comment.setAuthor(commentDto.getAuthor());
        comment.setComment(commentDto.getComment());
        if (commentDto.getParentComment()!=null) comment.setParentComment(commentToEntity(commentDto.getParentComment()));
        comment.setCreatedTime(commentDto.getCreatedTime());
        comment.setTask(commentDto.getTask());
        return comment;
    }

    @Override
    public CommentDto commentToDto(Comment comment) {
        if(comment==null){
            return null;
        }
        CommentDto commentDto = new CommentDto();
        commentDto.setId(comment.getId());
        commentDto.setAuthor(comment.getAuthor());
        commentDto.setComment(comment.getComment());
        if (comment.getParentComment()!=null) commentDto.setParentComment(commentToDto(comment.getParentComment()));
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

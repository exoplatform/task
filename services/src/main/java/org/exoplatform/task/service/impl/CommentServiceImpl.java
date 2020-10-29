package org.exoplatform.task.service.impl;

import org.exoplatform.commons.api.persistence.ExoTransactional;
import org.exoplatform.commons.utils.ListAccess;
import org.exoplatform.services.listener.ListenerService;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.social.core.identity.model.Identity;
import org.exoplatform.social.core.identity.provider.OrganizationIdentityProvider;
import org.exoplatform.social.core.service.LinkProvider;
import org.exoplatform.task.dao.DAOHandler;
import org.exoplatform.task.dto.CommentDto;
import org.exoplatform.task.dto.TaskDto;
import org.exoplatform.task.exception.EntityNotFoundException;
import org.exoplatform.task.service.CommentService;
import org.exoplatform.task.storage.CommentStorage;
import org.exoplatform.task.storage.TaskStorage;

import javax.inject.Inject;
import java.util.Date;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CommentServiceImpl implements CommentService {
    private static final Log LOG = ExoLogger.getExoLogger(CommentServiceImpl.class);
    private static final Pattern pattern = Pattern.compile("@([^\\s]+)|@([^\\s]+)$");

    @Inject
    private TaskStorage taskStorage;

    @Inject
    private CommentStorage commentStorage;

    @Inject
    private DAOHandler daoHandler;

    private ListenerService listenerService;

    public CommentServiceImpl(TaskStorage taskStorage, CommentStorage commentStorage, DAOHandler daoHandler, ListenerService listenerService) {
        this.taskStorage = taskStorage;
        this.commentStorage = commentStorage;
        this.daoHandler = daoHandler;
        this.listenerService = listenerService;
    }

    @Override
    public CommentDto getComment(long commentId) {
        CommentDto comment = commentStorage.getComment(commentId);
        comment.setComment(substituteUsernames(comment.getComment()));
        return comment;
    }

    @Override
    public List<CommentDto> getComments(long taskId, int offset, int limit) {
        return commentStorage.getComments(taskId,offset,limit);
    }

    @Override
    public int countComments(long taskId) {
        return commentStorage.countComments(taskId);
    }

    @Override
    public void loadSubComments(List<CommentDto> listComments) {
        if (listComments == null || listComments.isEmpty()) {
            return;
        }
        listComments.forEach(comment -> comment.setComment(substituteUsernames(comment.getComment())));
        commentStorage.loadSubComments(listComments);
    }

    @Override
    @ExoTransactional
    public CommentDto addComment(TaskDto task, long parentCommentId, String username, String comment) throws EntityNotFoundException {

        CommentDto newComment = new CommentDto();
        newComment.setTask(taskStorage.toEntity(task));
        newComment.setAuthor(username);
        newComment.setComment(comment);
        newComment.setCreatedTime(new Date());
        if (parentCommentId > 0) {
            CommentDto parentComment = commentStorage.getComment(parentCommentId);
            if (parentComment.getParentComment() != null) {
                parentComment = commentStorage.commentToDto(parentComment.getParentComment());
            }
            newComment.setParentComment(commentStorage.commentToEntity(parentComment));
        }
        CommentDto obj = commentStorage.commentToDto(daoHandler.getCommentHandler().create(commentStorage.commentToEntity(newComment)));

        try {
            listenerService.broadcast(TASK_COMMENT_CREATION, null, obj);
        } catch (Exception e) {
            LOG.error("Error while broadcasting task creation event", e);
        }

        return obj;
    }

    @Override
    @ExoTransactional
    public CommentDto addComment(TaskDto task, String username, String comment) throws EntityNotFoundException {
        return addComment(task, 0, username, comment);
    }

    @Override
    @ExoTransactional
    public void removeComment(long commentId) throws EntityNotFoundException {

        CommentDto comment = commentStorage.getComment(commentId);

        if (comment == null) {
            LOG.info("Can not find comment with ID: " + commentId);
            throw new EntityNotFoundException(commentId, CommentDto.class);
        }

        commentStorage.removeComment(commentId);
    }

    private String substituteUsernames(String message) {
        if (message == null || message.trim().isEmpty()) {
            return message;
        }
        //
        Matcher matcher = pattern.matcher(message);

        // Replace all occurrences of pattern in input
        StringBuffer buf = new StringBuffer();
        while (matcher.find()) {
            // Get the match result
            String username = matcher.group().substring(1);
            if (username == null || username.isEmpty()) {
                continue;
            }
            Identity identity = LinkProvider.getIdentityManager().getOrCreateIdentity(OrganizationIdentityProvider.NAME, username, false);
            if (identity == null || identity.isDeleted() || !identity.isEnable()) {
                continue;
            }
            try {
                username = LinkProvider.getProfileLink(username, "dw");
            } catch (Exception e) {
                continue;
            }
            // Insert replacement
            if (username != null) {
                matcher.appendReplacement(buf, username);
            }
        }
        if (buf.length() > 0) {
            matcher.appendTail(buf);
            return buf.toString();
        }
        return message;
    }
}

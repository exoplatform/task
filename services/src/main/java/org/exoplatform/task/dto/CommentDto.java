package org.exoplatform.task.dto;

import lombok.Data;
import org.exoplatform.task.domain.Comment;
import org.exoplatform.task.domain.Task;

import java.io.Serializable;
import java.util.*;


@Data
public class CommentDto implements Serializable {
    private long id;

    private String author;

    private String comment;

    private Date createdTime;

    private Task task;

    private CommentDto parentComment;

    private List<CommentDto> subComments;

    private Set<String> mentionedUsers;

    public CommentDto clone() {
        CommentDto commentDto=new CommentDto();
        commentDto.setId(this.getId());
        commentDto.setAuthor(this.getAuthor());
        commentDto.setComment(this.getComment());
        commentDto.setSubComments(this.getSubComments());
        commentDto.setCreatedTime(this.getCreatedTime());
        commentDto.setTask(this.getTask().clone());

        return commentDto;
    }

}

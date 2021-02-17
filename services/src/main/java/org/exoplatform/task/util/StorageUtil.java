package org.exoplatform.task.util;

import org.exoplatform.commons.utils.HTMLSanitizer;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.task.domain.*;
import org.exoplatform.task.dto.*;
import org.exoplatform.task.service.UserService;
import org.exoplatform.task.storage.ProjectStorage;
import org.exoplatform.task.storage.impl.TaskStorageImpl;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public final class StorageUtil{

    private static final Log LOG = ExoLogger.getExoLogger(StorageUtil.class);

    public static ChangeLog changeLogToEntity(ChangeLogEntry changeLogEntry, UserService userService) {
        ChangeLog changeLog = new ChangeLog();
        changeLog.setId(changeLogEntry.getId());
        changeLog.setTask(changeLogEntry.getTask());
        changeLog.setAuthor(changeLogEntry.getAuthor());
        changeLog.setActionName(changeLogEntry.getActionName());
        changeLog.setCreatedTime(changeLogEntry.getCreatedTime());
        changeLog.setTarget(changeLogEntry.getTarget());
        return changeLog;
    }

    public static ChangeLogEntry changeLogToDto(ChangeLog changeLog, UserService userService) {
        ChangeLogEntry changeLogEntry = new ChangeLogEntry();
        changeLogEntry.setId(changeLog.getId());
        changeLogEntry.setTask(changeLog.getTask());
        changeLogEntry.setAuthor(changeLog.getAuthor());
        changeLogEntry.setActionName(changeLog.getActionName());
        changeLogEntry.setCreatedTime(changeLog.getCreatedTime());
        changeLogEntry.setTarget(changeLog.getTarget());
        changeLogEntry.setAuthorFullName(userService.loadUser(changeLog.getAuthor()).getDisplayName());
        changeLogEntry.setAuthorAvatarUrl(userService.loadUser(changeLog.getAuthor()).getAvatar());
        if(changeLog.getActionName().equals("assign")||changeLog.getActionName().equals("unassign")){
            changeLogEntry.setTargetFullName(userService.loadUser(changeLog.getTarget()).getDisplayName());
        }
        return changeLogEntry;
    }

    public static Task taskToEntity(TaskDto taskDto) {
        if(taskDto==null){
            return null;
        }
        Task taskEntity = new Task();
        taskEntity.setId(taskDto.getId());
        taskEntity.setTitle(taskDto.getTitle());
        taskEntity.setDescription(taskDto.getDescription());
        taskEntity.setPriority(taskDto.getPriority());
        taskEntity.setContext(taskDto.getContext());
        taskEntity.setAssignee(taskDto.getAssignee());
        taskEntity.setCoworker(taskDto.getCoworker());
        taskEntity.setWatcher(taskDto.getWatcher());
        taskEntity.setStatus(statusToEntity(taskDto.getStatus()));
        taskEntity.setRank(taskDto.getRank());
        taskEntity.setActivityId(taskDto.getActivityId());
        taskEntity.setCompleted(taskDto.isCompleted());
        taskEntity.setCreatedBy(taskDto.getCreatedBy());
        taskEntity.setCreatedTime(taskDto.getCreatedTime());
        taskEntity.setEndDate(taskDto.getEndDate());
        taskEntity.setStartDate(taskDto.getStartDate());
        taskEntity.setDueDate(taskDto.getDueDate());
        return taskEntity;
    }

    public static TaskDto taskToDto(Task taskEntity,ProjectStorage projectStorage) {
        if(taskEntity==null){
            return null;
        }
        TaskDto task = new TaskDto();
        task.setId(taskEntity.getId());
        task.setTitle(taskEntity.getTitle());
        if(taskEntity.getDescription()!=null) {
            try {
                task.setDescription(HTMLSanitizer.sanitize(taskEntity.getDescription()));
            } catch (Exception e) {
                LOG.warn("Task description cannot be sanitized",e);
            }
        }
        task.setPriority(taskEntity.getPriority());
        task.setContext(taskEntity.getContext());
        task.setAssignee(taskEntity.getAssignee());
        task.setCoworker(taskEntity.getCoworker());
        task.setWatcher(taskEntity.getWatcher());
        task.setStatus(statusToDTO(taskEntity.getStatus(),projectStorage));
        task.setRank(taskEntity.getRank());
        task.setActivityId(taskEntity.getActivityId());
        task.setCompleted(taskEntity.isCompleted());
        task.setCreatedBy(taskEntity.getCreatedBy());
        task.setCreatedTime(taskEntity.getCreatedTime());
        task.setEndDate(taskEntity.getEndDate());
        task.setStartDate(taskEntity.getStartDate());
        task.setDueDate(taskEntity.getDueDate());
        return task;
    }


    public static Status statusToEntity(StatusDto statusDto) {
        if(statusDto==null){
            return null;
        }
        Status status = new Status();
        status.setId(statusDto.getId());
        status.setName(statusDto.getName());
        status.setRank(statusDto.getRank());
        status.setProject(projectToEntity(statusDto.getProject()));
        return status;
    }

    public static StatusDto statusToDTO(Status status, ProjectStorage projectStorage) {
        if(status==null){
            return null;
        }
        StatusDto statusDto = new StatusDto();
        statusDto.setId(status.getId());
        statusDto.setName(status.getName());
        statusDto.setRank(status.getRank());
        statusDto.setProject(projectToDto(status.getProject(),projectStorage));
        return statusDto;
    }

    public static List<StatusDto> listStatusToDTOs(List<Status> status, ProjectStorage projectStorage) {
        return status.stream()
                .map((Status status1) -> statusToDTO(status1,projectStorage))
                .collect(Collectors.toList());
    }

    public static List<Status> listStatusToEntitys(List<StatusDto> status) {
        return status.stream()
                .map(StorageUtil::statusToEntity)
                .collect(Collectors.toList());
    }


    public static Project projectToEntity(ProjectDto projectDto) {
        if(projectDto==null){
            return null;
        }
        Project project = new Project();
        project.setId(projectDto.getId());
        project.setName(projectDto.getName());
        project.setDescription(projectDto.getDescription());
        project.setColor(projectDto.getColor());
        project.setDueDate(projectDto.getDueDate());
        project.setLastModifiedDate(projectDto.getLastModifiedDate());
        project.setParticipator(projectDto.getParticipator());
        project.setManager(projectDto.getManager());
        project.setParent(projectToEntity(projectDto.getParent()));
        //if(projectDto.getStatus()!=null)project.setStatus(projectDto.getStatus().stream().map(status -> statusToEntity(status)).collect(Collectors.toSet()));
        //if(projectDto.getChildren()!=null)project.setChildren(projectDto.getChildren().stream().map(this::projectToEntity).collect(Collectors.toList()));
        return project;

    }

    public static ProjectDto projectToDto(Project project, ProjectStorage projectStorage) {
        if(project==null){
            return null;
        }
        ProjectDto projectDto = new ProjectDto();
        projectDto.setId(project.getId());
        projectDto.setName(project.getName());
        projectDto.setDescription(project.getDescription());
        projectDto.setColor(project.getColor());
        projectDto.setDueDate(project.getDueDate());
        projectDto.setLastModifiedDate(project.getLastModifiedDate());
        projectDto.setParticipator(projectStorage.getParticipator(project.getId()));
        projectDto.setManager(projectStorage.getManager(project.getId()));
        projectDto.setParent(projectToDto(project.getParent(),projectStorage));
        //if(project.getStatus()!=null)projectDto.setStatus(project.getStatus().stream().map(status -> statusToDTO(status,projectStorage)).collect(Collectors.toSet()));

        //if(project.getChildren()!=null)projectDto.setChildren(project.getChildren().stream().map(this::projectToDto).collect(Collectors.toList()));
        return projectDto;
    }


    public static Label labelToEntity(LabelDto labelDto) {
        if(labelDto==null){
            return null;
        }
        Label label = new Label();
        label.setId(labelDto.getId());
        label.setUsername(labelDto.getUsername());
        label.setName(labelDto.getName());
        label.setColor(labelDto.getColor());
        label.setHidden(labelDto.isHidden());
        label.setParent(labelToEntity(labelDto.getParent()));
        return label;
    }

    public static LabelDto labelToDto(Label label) {
        if(label==null){
            return null;
        }
        LabelDto labelDto = new LabelDto(label);
        labelDto.setId(label.getId());
        labelDto.setUsername(label.getUsername());
        labelDto.setName(label.getName());
        labelDto.setColor(label.getColor());
        labelDto.setHidden(label.isHidden());
        labelDto.setParent(labelToDto(label.getParent()));
        return labelDto;
    }



    public static Label mappingLabelToEntity(LabelDto labelDto) {
        if(labelDto==null){
            return null;
        }
        Label label = new Label();
        label.setId(labelDto.getId());
        label.setUsername(labelDto.getUsername());
        label.setName(labelDto.getName());
        label.setColor(labelDto.getColor());
        label.setHidden(labelDto.isHidden());
        label.setParent(labelToEntity(labelDto.getParent()));
        return label;
    }

    public static Comment commentToEntity(CommentDto commentDto) {
        if(commentDto==null){
            return null;
        }
        Comment comment = new Comment();
        comment.setId(commentDto.getId());
        comment.setAuthor(commentDto.getAuthor());
        comment.setComment(commentDto.getComment());
        if (commentDto.getParentComment()!=null) comment.setParentComment(commentToEntity(commentDto.getParentComment()));
        comment.setCreatedTime(commentDto.getCreatedTime());
        comment.setTask(taskToEntity(commentDto.getTask()));
        return comment;
    }

    public static CommentDto commentToDto(Comment comment,ProjectStorage projectStorage) {
        if(comment==null){
            return null;
        }
        CommentDto commentDto = new CommentDto();
        commentDto.setId(comment.getId());
        commentDto.setAuthor(comment.getAuthor());
        commentDto.setComment(comment.getComment());
        if (comment.getParentComment()!=null) commentDto.setParentComment(commentToDto(comment.getParentComment(),projectStorage));
        commentDto.setCreatedTime(comment.getCreatedTime());
        commentDto.setTask(taskToDto(comment.getTask(),projectStorage));
        return commentDto;
    }

    public static Step stepToEntity(StepDto stepDto) {
        if(stepDto==null){
            return null;
        }
        Step step = new Step();
        step.setId(stepDto.getId());
        step.setAuthor(stepDto.getAuthor());
        step.setName(stepDto.getName());
        step.setCreatedTime(stepDto.getCreatedTime());
        step.setCompleted(stepDto.isCompleted());
        step.setTask(taskToEntity(stepDto.getTask()));
        return step;
    }

    public static StepDto stepToDto(Step step,ProjectStorage projectStorage) {
        if(step==null){
            return null;
        }
        StepDto stepDto = new StepDto();
        stepDto.setId(step.getId());
        stepDto.setAuthor(step.getAuthor());
        stepDto.setName(step.getName());
        stepDto.setCreatedTime(step.getCreatedTime());
        stepDto.setCompleted(step.isCompleted());
        stepDto.setTask(taskToDto(step.getTask(),projectStorage));
        return stepDto;
    }

    public static List<CommentDto> listCommentsToDtos(List<Comment> comments, ProjectStorage projectStorage) {
        return comments.stream()
                .filter(Objects::nonNull)
                .map((Comment comment) -> commentToDto(comment,projectStorage))
                .collect(Collectors.toList());
    }

    public static List<Comment> listCommentsToEntitys(List<CommentDto> commentDtos) {
        return commentDtos.stream()
                .filter(Objects::nonNull)
                .map(StorageUtil::commentToEntity)
                .collect(Collectors.toList());
    }
}

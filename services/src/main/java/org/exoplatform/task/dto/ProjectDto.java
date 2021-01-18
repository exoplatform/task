package org.exoplatform.task.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.services.security.Identity;
import org.exoplatform.services.security.MembershipEntry;
import org.exoplatform.task.domain.Project;
import org.exoplatform.task.domain.Status;
import org.exoplatform.task.domain.UserSetting;

import java.io.Serializable;
import java.util.*;


@Data
@NoArgsConstructor
public class ProjectDto implements Serializable {
    private static final Log LOG           = ExoLogger.getLogger(ProjectDto.class);


    private long      id;

    private String    name;

    private String    description;

    private String    color;

    private Set<StatusDto> status ;

    private Set<String> manager;

    private Set<String> participator;

    private Date dueDate;

    private Long lastModifiedDate;;

    private ProjectDto parent;

    private List<ProjectDto> children;

    private Set<UserSetting> hiddenOn;

    private String spaceName;

    public ProjectDto(String name, String description, HashSet<StatusDto> statuses, Set<String> managers, Set<String> participators) {
        this.name=name;
        this.description=description;
        this.status=statuses;
        this.manager=managers;
        this.participator=participators;
    }


    public ProjectDto clone(boolean cloneTask) {
        ProjectDto project = new ProjectDto();
        project.setId(getId());
        project.setName(this.getName());
        project.setDescription(this.getDescription());
        project.setColor(this.getColor());
        project.setDueDate(this.getDueDate());
        if (this.getParent() != null) {
            project.setParent(getParent().clone(false));
        }
        project.status = new HashSet<StatusDto>();
        project.children = new LinkedList<ProjectDto>();

        return project;
    }

    public boolean canView(Identity user) {
        Set<String> permissions = new HashSet<String>();
        Set<String> Participants = getParticipator();
        Set<String> managers = getManager();
        if(Participants!=null){
            permissions.addAll(Participants);
        }
        if(managers!=null){
            permissions.addAll(managers);
        }
        return hasPermission(user, permissions);
    }

    public boolean canEdit(Identity user) {
        return hasPermission(user, getManager());
    }

    private boolean hasPermission(Identity user, Set<String> permissions) {
        if (permissions.contains(user.getUserId())) {
            return true;
        } else {
            Set<MembershipEntry> memberships = new HashSet<MembershipEntry>();
            for (String per : permissions) {
                MembershipEntry entry = MembershipEntry.parse(per);
                if (entry != null) {
                    memberships.add(entry);
                }
            }

            for (MembershipEntry entry :  user.getMemberships()) {
                if (memberships.contains(entry)) {
                    return true;
                }
            }
        }

        return false;
    }


}

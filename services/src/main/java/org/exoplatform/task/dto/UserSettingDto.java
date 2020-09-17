package org.exoplatform.task.dto;

import lombok.Data;
import org.exoplatform.task.domain.Project;
import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;


@Data
public class UserSettingDto implements Serializable {
    private String username;

    private boolean showHiddenProject = false;

    private boolean showHiddenLabel = false;

    private Set<Project> hiddenProjects;

    public UserSettingDto clone() {
        UserSettingDto setting = new UserSettingDto();
        setting.setShowHiddenProject(isShowHiddenProject());
        Set<Project> hiddenProjects = new HashSet<Project>();
        if (getHiddenProjects() != null) {
            for (Project p : getHiddenProjects()) {
                hiddenProjects.add(p.clone(false));
            }
        }
        setting.setHiddenProjects(hiddenProjects);
        return setting;
    }

}

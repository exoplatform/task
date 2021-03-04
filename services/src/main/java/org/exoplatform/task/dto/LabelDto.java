package org.exoplatform.task.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.exoplatform.task.domain.Label;

import java.io.Serializable;
import java.util.List;

@Data
@NoArgsConstructor
public class LabelDto implements Serializable {
    private long      id;

    private String username;

    private String    name;

    private String    color;

    private boolean hidden;

    private boolean canEdit;

    private LabelDto parent;

    private ProjectDto project;

    private List<LabelDto> children;


    public LabelDto getParent() {
        return parent;
    }

    public void setParent(LabelDto parent) {
        if(this.equals(parent)){
          this.parent = null;
        } else {
            this.parent = parent;
        }
    }
}

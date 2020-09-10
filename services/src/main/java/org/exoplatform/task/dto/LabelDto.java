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

    private Label parent;

    private List<Label> children;


    public LabelDto(Label label){
        this.id=label.getId();
        this.username=label.getUsername();
        this.name=label.getName();
        this.color=label.getColor();
        this.hidden=label.isHidden();
        this.parent=label.getParent();
    }

}

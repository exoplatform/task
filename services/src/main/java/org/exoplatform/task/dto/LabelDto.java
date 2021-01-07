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

    private LabelDto parent;

    private List<LabelDto> children;

    public LabelDto(Label label){
        this.id = label.getId();
        this.username = label.getUsername();
        this.name = label.getName();
        this.color = label.getColor();
        this.hidden = label.isHidden();
        this.parent = labelToDto(label.getParent());
    }

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
}

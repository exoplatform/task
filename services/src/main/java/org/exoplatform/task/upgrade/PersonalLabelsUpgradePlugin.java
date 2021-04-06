package org.exoplatform.task.upgrade;

import org.exoplatform.commons.upgrade.UpgradeProductPlugin;
import org.exoplatform.commons.utils.ListAccess;
import org.exoplatform.container.xml.InitParams;
import org.exoplatform.task.dao.DAOHandler;
import org.exoplatform.task.domain.Label;
import org.exoplatform.task.domain.LabelTaskMapping;
import org.exoplatform.task.domain.Project;
import org.exoplatform.task.domain.Task;
import org.exoplatform.task.dto.LabelDto;
import org.exoplatform.task.storage.LabelStorage;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class PersonalLabelsUpgradePlugin extends UpgradeProductPlugin {

    private DAOHandler daoHandler;

    public PersonalLabelsUpgradePlugin(DAOHandler daoHandler, InitParams initParams) {
        super(initParams);
        this.daoHandler = daoHandler;

    }

    @Override
    public void processUpgrade(String oldVersion, String newVersion) {
        List<Label> labels = daoHandler.getLabelHandler().findOldLabels();
        for (Label label : labels) {
            List<LabelTaskMapping> lm = daoHandler.getLabelTaskMappingHandler().findLabelMappingByLabel(label.getId());
            for (LabelTaskMapping labelTaskMapping : lm) {
                if (labelTaskMapping.getTask().getStatus() != null && labelTaskMapping.getTask().getStatus().getProject() != null) {
                    List<Label> lab = daoHandler.getLabelHandler().findLabelsByUserAndProject(labelTaskMapping.getLabel().getUsername(), labelTaskMapping.getTask().getStatus().getProject().getId());
                    if (lab.size() > 0) {
                        labelTaskMapping.setLabel(lab.get(0));
                        daoHandler.getLabelTaskMappingHandler().update(labelTaskMapping);
                    } else {
                        Label newLabel = daoHandler.getLabelHandler().create(new Label(labelTaskMapping.getLabel().getName(), labelTaskMapping.getLabel().getUsername(), labelTaskMapping.getTask().getStatus().getProject()));
                        labelTaskMapping.setLabel(newLabel);
                        daoHandler.getLabelTaskMappingHandler().update(labelTaskMapping);
                    }
                }
            }
            daoHandler.getLabelHandler().delete(label);
        }

    }
}

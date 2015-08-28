package org.exoplatform.task.integration;

import org.exoplatform.container.component.BaseComponentPlugin;
import org.exoplatform.container.xml.InitParams;
import org.exoplatform.container.xml.ValuesParam;

import java.util.ArrayList;
import java.util.List;

/**
 * Changelog plugin to add Liquibase changelog path during the data initialization
 */
public class ChangeLogsPlugin extends BaseComponentPlugin {

  private List<String> changelogPaths = new ArrayList<String>();

  public ChangeLogsPlugin(InitParams initParams) {
    ValuesParam changelogs = initParams.getValuesParam("changelogs");

    if(changelogs != null) {
      changelogPaths.addAll(changelogs.getValues());
    }
  }

  public List<String> getChangelogPaths() {
    return changelogPaths;
  }

  public void setChangelogPaths(List<String> changelogPaths) {
    this.changelogPaths = changelogPaths;
  }
}

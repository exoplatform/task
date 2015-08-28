package org.exoplatform.task.integration;

import liquibase.Liquibase;
import liquibase.database.Database;
import liquibase.database.DatabaseFactory;
import liquibase.database.jvm.JdbcConnection;
import liquibase.exception.DatabaseException;
import liquibase.exception.LiquibaseException;
import liquibase.resource.ClassLoaderResourceAccessor;
import org.exoplatform.container.xml.InitParams;
import org.exoplatform.container.xml.ValueParam;
import org.picocontainer.Startable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Startable service to initialize all the data with Liquibase.
 * Changelog files are added by external plugins.
 */
public class DataInitializer implements Startable {

  private static final Logger LOG = LoggerFactory.getLogger(DataInitializer.class);

  private String liquibaseDatasourceName;

  private String liquibaseContexts;

  private List<ChangeLogsPlugin> changeLogsPlugins = new ArrayList<ChangeLogsPlugin>();

  public DataInitializer(InitParams initParams) {
    ValueParam liquibaseDatasourceNameParam = initParams.getValueParam("liquibase.datasource");
    if(liquibaseDatasourceNameParam != null && liquibaseDatasourceNameParam.getValue() != null) {
      liquibaseDatasourceName = liquibaseDatasourceNameParam.getValue();
    } else {
      liquibaseDatasourceName = "exo-jcr_portal";
    }

    ValueParam liquibaseContextsParam = initParams.getValueParam("liquibase.contexts");
    if(liquibaseContextsParam != null && liquibaseContextsParam.getValue() != null) {
      liquibaseContexts = liquibaseContextsParam.getValue();
    } else {
      liquibaseContexts = "production";
    }
  }

  /**
   * Add a changelogs plugin
   * @param changeLogsPlugin Changelogs plugin to add
   */
  public void addChangeLogsPlugin(ChangeLogsPlugin changeLogsPlugin) {
    this.changeLogsPlugins.add(changeLogsPlugin);
  }

  /**
   * Start the initializing of the data with Liquibase.
   * Iterates over all the changelogs injected by the change logs plugins and executes them.
   */
  @Override
  public void start() {
    if(!changeLogsPlugins.isEmpty()) {
      try {
        LOG.info("Starting data initialization with Liquibase with datasource " + liquibaseDatasourceName);

        Database database = DatabaseFactory.getInstance()
                .findCorrectDatabaseImplementation(new JdbcConnection(getDatasource(liquibaseDatasourceName).getConnection()));

        for (ChangeLogsPlugin changeLogsPlugin : this.changeLogsPlugins) {
          LOG.info("Processing changelogs of " + changeLogsPlugin.getName());
          try {
            for (String changelogsPath : changeLogsPlugin.getChangelogPaths()) {
              LOG.info("  * processing changelog " + changelogsPath);
              Liquibase liquibase = new Liquibase(changelogsPath, new ClassLoaderResourceAccessor(), database);
              liquibase.update(liquibaseContexts);
            }
          } catch (LiquibaseException e) {
            LOG.error("Error while processing changelogs of plugin " + changeLogsPlugin.getName() + " - Cause : " + e.getMessage(), e);
          }
        }
      } catch (DatabaseException e) {
        LOG.error("Error while initializing liquibase database - Cause : " + e.getMessage(), e);
      } catch (SQLException e) {
        LOG.error("Error while getting a JDBC connection from datasource " + liquibaseDatasourceName + " - Cause : " + e.getMessage(), e);
      }
    } else {
      LOG.info("No data to initialize with Liquibase");
    }
  }

  @Override
  public void stop() {
  }

  /**
   * Lookup for a datasource with the given name
   * @param datasourceName Name of the datasource to retrieve
   * @return The datasource with the given name
   */
  protected DataSource getDatasource(String datasourceName) {
    DataSource dataSource = null;
    try {
      Context initCtx = new InitialContext();
      Context envCtx = (Context) initCtx.lookup("java:comp/env");

      // Look up our data source - Datasource name = Tenant name = Database name
      dataSource = (DataSource) envCtx.lookup(datasourceName);
    } catch (NamingException e) {
      LOG.error("Cannot find datasource " + datasourceName + " - Cause : " + e.getMessage(), e);
    }

    return dataSource;
  }
}

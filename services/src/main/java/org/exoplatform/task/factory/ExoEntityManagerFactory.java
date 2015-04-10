/* 
* Copyright (C) 2003-2015 eXo Platform SAS.
*
* This program is free software: you can redistribute it and/or modify
* it under the terms of the GNU Affero General Public License as published by
* the Free Software Foundation, either version 3 of the License, or
* (at your option) any later version.
*
* This program is distributed in the hope that it will be useful,
* but WITHOUT ANY WARRANTY; without even the implied warranty of
* MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
* GNU Affero General Public License for more details.
*
* You should have received a copy of the GNU Affero General Public License
* along with this program. If not, see http://www.gnu.org/licenses/ .
*/
package org.exoplatform.task.factory;

import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import java.util.logging.Logger;

/**
 * Created by The eXo Platform SAS
 * Author : Thibault Clement
 * tclement@exoplatform.com
 * 4/8/15
 */
public class ExoEntityManagerFactory implements ServletContextListener {

  private static final Logger LOG = Logger.getLogger("EntityManagerFactoryServletListener");
  private static EntityManagerFactory entityManagerFactory;

  @Override
  public void contextInitialized(ServletContextEvent event) {
    entityManagerFactory = Persistence.createEntityManagerFactory("org.exoplatform.task");
    LOG.info("JPA EntityManagerFactory created");
  }

  @Override
  public void contextDestroyed(ServletContextEvent event) {
    LOG.info("JPA EntityManagerFactory is closing");
    if (entityManagerFactory != null) {
      entityManagerFactory.close();
      LOG.info("JPA EntityManagerFactory closed");
    }
  }

  public static EntityManagerFactory getEntityManagerFactory() {
    if (entityManagerFactory == null) {
      throw new IllegalStateException("Context is not initialized yet");
    }
    return entityManagerFactory;
  }


}


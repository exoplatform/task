/* 
* Copyright (C) 2003-2015 eXo Platform SAS.
*
* This program is free software: you can redistribute it and/or modify
* it under the terms of the GNU Lesser General Public License as published by
* the Free Software Foundation, either version 3 of the License, or
* (at your option) any later version.
*
* This program is distributed in the hope that it will be useful,
* but WITHOUT ANY WARRANTY; without even the implied warranty of
* MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
* GNU Lesser General Public License for more details.
*
* You should have received a copy of the GNU Lesser General Public License
* along with this program. If not, see http://www.gnu.org/licenses/ .
*/
package org.exoplatform.task.exception;

/**
 * Created by The eXo Platform SAS
 * Author : Thibault Clement
 * tclement@exoplatform.com
 * 6/4/15
 */
public abstract class AbstractEntityException extends Exception {

  private Long entityId;
  private String entityType;
  private Integer httpStatusCode;

  public AbstractEntityException(Long entityId, String entityType, Integer httpStatusCode) {
    this.entityId = entityId;
    this.entityType = entityType;
    this.httpStatusCode = httpStatusCode;
  }

  @Override
  public String getMessage() {
    if (entityId != null && entityType != null) {
      return "Exception on "+entityType+" with ID: "+entityId;
    }
    return super.getMessage();
  }

  public Long getEntityId() {
    return entityId;
  }

  public String getEntityType() {
    return entityType;
  }

  public Integer getHttpStatusCode() {
    return httpStatusCode;
  }
}

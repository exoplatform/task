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
public class NotAllowedOperationOnEntityException extends AbstractEntityException {

  private String operation;

  public NotAllowedOperationOnEntityException(long id, Class<?> entityType, String operation) {
    super(id, entityType);
    this.operation = operation;
  }

  @Override
  public String getMessage() {
    return "Not allow operation: [ "+operation+" ] attempted on "+getEntityType()+" with ID: "+getEntityId();
  }

  public String getOperation() {
    return operation;
  }
}


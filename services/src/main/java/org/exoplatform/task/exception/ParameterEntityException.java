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
public class ParameterEntityException extends AbstractEntityException {

  private String param;
  private String value;
  private String exception;
  private Throwable cause;

  public ParameterEntityException(Long entityId, Class<?> entityType, String param, String value, String exception,
                                  Throwable cause) {
    super(entityId, entityType);
    this.param = param;
    this.value = value;
    this.exception = exception;
    this.cause = cause;
  }

  @Override
  public String getMessage() {
    StringBuffer message = new StringBuffer("Parameter "+param+" with value = " + value + " on "+getEntityType()+
        " with ID: "+getEntityId()+" "+exception);
    if (cause != null) message.append(" Original cause is: "+cause);
    return message.toString();
  }

  public String getParam() {
    return param;
  }

  public String getException() {
    return exception;
  }

  public String getValue() {
    return value;
  }
}


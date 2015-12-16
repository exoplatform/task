/*
 * Copyright (C) 2015 eXo Platform SAS.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */

package org.exoplatform.task.management.controller;

import javax.inject.Inject;

import java.util.ResourceBundle;

import juzu.Path;
import juzu.Response;
import juzu.impl.common.Tools;
import juzu.request.Phase;
import juzu.request.RequestContext;
import juzu.request.RequestLifeCycle;

import org.exoplatform.commons.utils.HTMLEntityEncoder;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.task.exception.EntityNotFoundException;
import org.exoplatform.task.exception.NotAllowedOperationOnEntityException;
import org.exoplatform.task.exception.ParameterEntityException;
import org.exoplatform.task.exception.UnAuthorizedOperationException;

/**
 * @author <a href="mailto:tuyennt@exoplatform.com">Tuyen Nguyen The</a>.
 */
public class AbstractController implements RequestLifeCycle {
  protected final Log LOG = ExoLogger.getExoLogger(getClass());
  
  public static enum MSG_TYPE {
    INFO, WARNING, ERROR
  }

  @Inject
  ResourceBundle bundle;
  
  @Inject
  @Path("messageDialog.gtmpl")
  org.exoplatform.task.management.templates.messageDialog messageDialog;
  
  private String noPermissionMsg;

  public Response buildMSGDialog(String message, MSG_TYPE msgType) {
    return messageDialog
        .with()
        .msg(message)
        .type(msgType)
        .ok().withCharset(Tools.UTF_8);
  }

  protected String getNoPermissionMsg() {
    if (noPermissionMsg == null) {
      noPermissionMsg = bundle.getString("popup.msg.noPermission");
    }
    return noPermissionMsg;
  }

  @Override
  public void beginRequest(RequestContext context) {

  }

  @Override
  public void endRequest(RequestContext context) {
    Response res = context.getResponse();
    if (res instanceof Response.Error) {
      Response.Error error = (Response.Error)res;
      Throwable ex = error.getCause();
      if (ex != null) {
        //
        if (!(ex instanceof UnAuthorizedOperationException)) {
          LOG.error("Exception while process request", ex);   
        }

        //TODO: we only overwrite response for resource method now
        if (context.getPhase() == Phase.RESOURCE) {

          HTMLEntityEncoder encoder = HTMLEntityEncoder.getInstance();
          String message = encoder.encodeHTML(ex.getMessage());

          Response response = null;
          if (ex instanceof EntityNotFoundException) {
            response = Response.status(404).body(message);
          } else if (ex instanceof NotAllowedOperationOnEntityException) {
            response = Response.status(403).body(message);
          } else if (ex instanceof UnAuthorizedOperationException) {
            response = Response.status(401).body(message);
          } else if (ex instanceof ParameterEntityException) {
            response = Response.status(406).body(message);
          }

          //
          if (response != null) {
            context.setResponse(response);
          }
        }
      }
    }
  }
}

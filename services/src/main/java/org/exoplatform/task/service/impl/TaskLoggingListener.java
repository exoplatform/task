/**
 * Copyright (C) 2015 eXo Platform SAS.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Affero General Public License
 * as published by the Free Software Foundation; either version 3
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, see<http://www.gnu.org/licenses/>.
 **/
  
package org.exoplatform.task.service.impl;

import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.services.security.ConversationState;
import org.exoplatform.task.exception.TaskNotFoundException;
import org.exoplatform.task.service.TaskListener;
import org.exoplatform.task.service.TaskService;
import org.exoplatform.task.service.impl.TaskEvent.Type;

public class TaskLoggingListener implements TaskListener {

  private static final Log LOG = ExoLogger.getExoLogger(TaskLoggingListener.class);
  
  @Override
  public void event(TaskEvent event) {
    TaskService service = event.getContext();
    
    String username = ConversationState.getCurrent().getIdentity().getUserId();
    String[] msg = getMsg(event);
    try {
      if (msg[0] != null) {
        service.addTaskLog(event.getTask().getId(),username, msg[0], msg[1]);        
      }
    } catch (TaskNotFoundException e) {
      LOG.error(e);
    }
  }

  private String[] getMsg(TaskEvent event) {
    String msg, val = "";
    
    Type type = event.getType();    
    switch (type) {
    case CREATED:
      msg = "log.created";
      break;
    case ADD_LABEL:
      msg = "log.add_label";      
      val = StringUtils.join((Set)event.getNewVal(), ",");
      break;
    case ADD_SUBTASK:
      msg = "log.add_subtask";
      break;
    case EDIT_ASSIGNEE:
      if (event.getNewVal() != null) {
        msg = "log.assign";
        val = event.getNewVal().toString();
      } else {
        msg = "log.unassign";
        val = event.getOldVal().toString();
      }
      break;
    case EDIT_DESCRIPTION:
      msg = "log.edit_description";
      break;
    case EDIT_DUEDATE:
      msg = "log.edit_duedate";
      break;
    case  EDIT_PROJECT:
      msg = "log.edit_project";
      val = event.getNewVal().toString();
      break;
    case EDIT_STATUS:
      msg = "log.edit_status";
      val = event.getNewVal().toString();
      break;
    case  EDIT_TITLE:
      msg = "log.edit_title";
      break;
    case EDIT_WORKPLAN:
      msg = "log.edit_workplan";
      break;
    case MARK_DONE:
      if (Boolean.TRUE.equals(event.getNewVal())) {
        msg = "log.mark_done";
      } else {
        msg = null;
      }
      break;
    default:
      throw new IllegalArgumentException(type.name() + " event not support");
    }
    
    return new String[] {msg, val};
  }
}

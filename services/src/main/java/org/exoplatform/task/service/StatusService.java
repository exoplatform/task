package org.exoplatform.task.service;

import java.util.List;

import org.exoplatform.task.domain.Project;
import org.exoplatform.task.domain.Status;
import org.exoplatform.task.exception.NotAllowedOperationOnEntityException;
import org.exoplatform.task.exception.StatusNotFoundException;

public interface StatusService {
  
  List<String> getDefaultStatus();
    
  Status createStatus(Project project, String status);
  
  Status deleteStatus(long statusID) throws StatusNotFoundException, NotAllowedOperationOnEntityException;
  
  Status updateStatus(long id, String name) throws StatusNotFoundException, NotAllowedOperationOnEntityException;
  
  Status swapPosition(long statusID, long otherID) throws NotAllowedOperationOnEntityException;
}

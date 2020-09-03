import {tasksConstants} from "./tasksConstants";
export function getProjectsList () {
  return fetch(`${tasksConstants.PORTAL}/${tasksConstants.PORTAL_REST}/tasks/projects`, {
    method: 'GET',
    credentials: 'include',
  }).then(resp => {
    if (!resp || !resp.ok) {
      throw new Error('Response code indicates a server error', resp);
    } else {
      return resp.json();
    }
  });
}

export function getMyTasksList () {
  return fetch(`${tasksConstants.PORTAL}/${tasksConstants.PORTAL_REST}/tasks`, {
    method: 'GET',
    credentials: 'include',
  }).then(resp => {
    if (!resp || !resp.ok) {
      throw new Error('Response code indicates a server error', resp);
    } else {
      return resp.json();
    }
  });
}

export function getLabelsByTaskId(taskId) {
  return fetch(`${tasksConstants.PORTAL}/${tasksConstants.PORTAL_REST}/tasks/labels/${taskId}`, {
    method: 'GET',
    credentials: 'include',
  }).then(resp => {
    if (!resp || !resp.ok) {
      throw new Error('Response code indicates a server error', resp);
    } else {
      return resp.json();
    }
  });

}

import {tasksConstants} from "./tasksConstants";

export function getMyTasksList (query,offset, limit) {
  return fetch(`${tasksConstants.PORTAL}/${tasksConstants.PORTAL_REST}/tasks?q=${query || ''}&offset=${offset || 0}&limit=${limit|| 0}&returnDetails=true`, {
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

export function filterTasksList (tasks,offset, limit) {
  return fetch(`${tasksConstants.PORTAL}/${tasksConstants.PORTAL_REST}/tasks/filter?projectId=-2&query=${tasks.query || ''}&dueDate=${tasks.dueDate || ''}&priority=${tasks.priority || ''}&showCompleted=${tasks.showCompleteTasks || ''}&assignee=${tasks.assignee || ''}&offset=${offset || 0}&limit=${limit|| 0}`, {
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

export function getStatusesByProjectId(projectId) {
  return fetch(`${tasksConstants.PORTAL}/${tasksConstants.PORTAL_REST}/projects/projects/statuses/${projectId}`, {
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

export function getTasksByProjectId(projectId) {
  return fetch(`${tasksConstants.PORTAL}/${tasksConstants.PORTAL_REST}/tasks/project/${projectId}?returnDetails=true`, {
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

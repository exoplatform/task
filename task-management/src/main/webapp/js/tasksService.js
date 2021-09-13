import { tasksConstants } from './tasksConstants';

export function getMyTasksList(type, query, offset, limit) {
  document.dispatchEvent(new CustomEvent('displayTopBarLoading'));
  return fetch(`${tasksConstants.PORTAL}/${tasksConstants.PORTAL_REST}/tasks?status=${type || ''}&q=${query || ''}&offset=${offset || 0}&limit=${limit|| 0}&returnDetails=true`, {
    method: 'GET',
    credentials: 'include',
  }).then(resp => {
    if (!resp || !resp.ok) {
      throw new Error('Response code indicates a server error', resp);
    } else {
      return resp.json();
    }
  }).finally(() => document.dispatchEvent(new CustomEvent('hideTopBarLoading')));
}

export function filterTasksList(tasks, groupBy, orderBy, filterLabelIds, projectId) {
  document.dispatchEvent(new CustomEvent('displayTopBarLoading'));
  return fetch(`${tasksConstants.PORTAL}/${tasksConstants.PORTAL_REST}/tasks/filter?projectId=${projectId || tasks.projectId || -2}&query=${tasks.query || ''}&dueDate=${tasks.dueDate || ''}&priority=${tasks.priority || ''}&statusId=${tasks.statusId || ''}&showCompletedTasks=${tasks.showCompletedTasks || ''}&assignee=${tasks.assignee || ''}&watcher=${tasks.watcher || ''}&coworker=${tasks.coworker || ''}&groupBy=${groupBy || tasks.groupBy || ''}&orderBy=${orderBy || tasks.orderBy || ''}&dueCategory=${tasks.dueCategory || ''}&filterLabelIds=${filterLabelIds || tasks.filterLabelIds || ''}&offset=${tasks.offset || 0}&limit=${tasks.limit|| 0}`, {
    method: 'GET',
    credentials: 'include',
  }).then(resp => {
    if (!resp || !resp.ok) {
      throw new Error('Response code indicates a server error', resp);
    } else {
      return resp.json();
    }
  }).finally(() => document.dispatchEvent(new CustomEvent('hideTopBarLoading')));
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

export function getLabelsByProjectId(projectId) {
  return fetch(`${tasksConstants.PORTAL}/${tasksConstants.PORTAL_REST}/tasks/labels/project/${projectId}`, {
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


export function getTaskById(taskId) {
  return fetch(`${tasksConstants.PORTAL}/${tasksConstants.PORTAL_REST}/tasks/${taskId}`, {
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

export function updateCompleted(task) {
  document.dispatchEvent(new CustomEvent('displayTopBarLoading'));
  return fetch(`${tasksConstants.PORTAL}/${tasksConstants.PORTAL_REST}/tasks/updateCompleted/${task.id}?&isCompleted=${task.isCompleted}`, {
    headers: {
      'Content-Type': 'application/json'
    },
    credentials: 'include',
    method: 'PUT',
    body: JSON.stringify(task)
  }).then(resp => resp.json()).finally(() => document.dispatchEvent(new CustomEvent('hideTopBarLoading')));
}

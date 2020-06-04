export function getUserInformations(userName) {
  return fetch(`/portal/rest/v1/social/users/${userName}`, {
    method: 'GET',
    credentials: 'include',
  }).then((resp) => {
    if(resp && resp.ok) {
      return resp.json();
    }
    else {
      throw new Error ('Error when getting user informations');
    }
  })
}

export function updateTask(taskId, task) {
  return fetch(`/portal/rest/tasks/${taskId}`, {
    headers: {
      Accept: 'application/json',
      'Content-Type': 'application/json'
    },
    method: 'PUT',
    credentials: 'include',
    body: JSON.stringify(task)
  }).then((resp) => {
    if(resp && resp.ok) {
      return resp.json();
    }
    else {
      throw new Error ('Error when updating task');
    }
  })
}

export function getMyAllLabels() {
  return fetch('/portal/rest/tasks/labels', {
    method: 'GET',
    credentials: 'include',
  }).then((resp) => {
    if(resp && resp.ok) {
      return resp.json();
    }
    else {
      throw new Error ('Error when getting my all labels');
    }
  })
}

export function getTaskLabels(taskId) {
  return fetch(`/portal/rest/tasks/labels/${taskId}`, {
    method: 'GET',
    credentials: 'include',
  }).then((resp) => {
    if(resp && resp.ok) {
      return resp.json();
    }
    else {
      throw new Error ('Error when getting task labels');
    }
  })
}

export function addTaskToLabel(taskId, label) {
  return fetch(`/portal/rest/tasks/labels/${taskId}`, {
    headers: {
      Accept: 'application/json',
      'Content-Type': 'application/json',
    },
    method: 'POST',
    credentials: 'include',
    body: JSON.stringify(label)
  }).then((resp) => {
    if(resp && resp.ok) {
      return resp.json();
    }
    else {
      throw new Error ('Error when adding task to label');
    }
  })
}

export function removeTaskFromLabel(taskId, labelId) {
  return fetch(`/portal/rest/tasks/labels/${taskId}/${labelId}`, {
    credentials: 'include',
    method: "delete",
  }).then((resp) => {
    if(resp && resp.ok) {
      return resp.json();
    }
    else {
      throw new Error ('Error when deleting task from label');
    }
  })
}

export function getProjects() {
  return fetch('/portal/rest/tasks/projects', {
    method: 'GET',
    credentials: 'include',
  }).then((resp) => {
    if(resp && resp.ok) {
      return resp.json();
    }
    else {
      throw new Error ('Error when getting projects');
    }
  })
}

export function getDefaultStatusByProjectId(projectId) {
  return fetch(`/portal/rest/tasks/projects/status/${projectId}`, {
    method: 'GET',
    credentials: 'include',
  }).then((resp) => {
    if(resp && resp.ok) {
      return resp.json();
    }
    else {
      throw new Error ('Error when getting default status');
    }
  })
}

export function getStatusesByProjectId(projectId) {
  return fetch(`/portal/rest/tasks/projects/statuses/${projectId}`, {
    method: 'GET',
    credentials: 'include',
  }).then((resp) => {
    if(resp && resp.ok) {
      return resp.json();
    } 
    else {
      throw new Error ('Error when getting project statuses');
    }
  })
}

export function getAllUsers() {
  return fetch('/portal/rest/v1/social/users', {
    method: 'GET',
    credentials: 'include',
  }).then((resp) => {
    if(resp && resp.ok) {
      return resp.json();
    }
    else {
      throw new Error ('Error when getting users');
    }
  })
}

export function getTaskLogs(taskId) {
  return fetch(`/rest/tasks/logs/${taskId}`, {
    method: 'GET',
    credentials: 'include',
  }).then((resp) => {
    if(resp && resp.ok) {
      return resp.json();
    }
    else {
      throw new Error ('Error when getting task logs');
    }
  })
}

export function getTaskComments(taskId) {
  return fetch(`/portal/rest/tasks/comments/${taskId}`, {
    method: 'GET',
    credentials: 'include',
  }).then((resp) => {
    if(resp && resp.ok) {
      return resp.json();
    }
    else {
      throw new Error ('Error when getting task comments');
    }
  })
}

export function addTaskComments(taskId, comment) {
  return fetch(`/portal/rest/tasks/comments/${taskId}`, {
    method: 'POST',
    credentials: 'include',
    headers: {
      Accept: 'application/json',
      'Content-Type': 'application/json',
    },
    body: comment
  }).then((resp) => {
    if(resp && resp.ok) {
      return resp.json();
    }
    else {
      throw new Error ('Error when adding task comment');
    }
  })
}

export function addTaskSubComment(taskId, parentCommentId, comment) {
  return fetch(`/portal/rest/tasks/comments/${parentCommentId}/${taskId}`, {
    method: 'POST',
    credentials: 'include',
    headers: {
      Accept: 'application/json',
      'Content-Type': 'application/json',
    },
    body: comment
  }).then((resp) => {
    if(resp && resp.ok) {
      return resp.json();
    }
    else {
      throw new Error ('Error when adding task sub comment');
    }
  })
}

export function removeTaskComment(commentId) {
  return fetch(`/portal/rest/tasks/comments/${commentId}`, {
    method: "delete",
    credentials: 'include',
  }).then((resp) => {
    if(resp && resp.ok) {
      return resp.json();
    }
    else {
      throw new Error ('Error when removing task comment');
    }
  })
}

export function findUsersToMention(query) {
  return fetch(`/portal/rest/tasks/usersToMention/${query}`, {
    method: 'GET',
    credentials: 'include',
  }).then((resp) => {
    if(resp && resp.ok) {
      return resp.json();
    }
    else {
      throw new Error ('Error when getting users to mention');
    }
  })
}

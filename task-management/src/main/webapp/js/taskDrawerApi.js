import { tasksConstants } from './tasksConstants';

export function getUserInformations(userName) {
  return fetch(`/portal/rest/v1/social/users/${userName}`, {
    method: 'GET',
    credentials: 'include',
  }).then((resp) => {
    if (resp && resp.ok) {
      return resp.json();
    } else {
      throw new Error('Error when getting user informations');
    }
  });
}

export function updateTask(taskId, task) {
  if (taskId) {
    document.dispatchEvent(new CustomEvent('displayTopBarLoading'));
    return fetch(`/portal/rest/tasks/${taskId}`, {
      method: 'PUT',
      credentials: 'include',
      headers: {
        'Content-Type': 'application/json'
      },
      body: JSON.stringify(task),
    }).then(resp => {
      if (!resp || !resp.ok) {
        return resp.text().then((text) => {
          throw new Error(text);
        });
      } else {
        return resp.json();
      }
    }).finally(() => document.dispatchEvent(new CustomEvent('hideTopBarLoading')));
  } else {
    return Promise.resolve();
  }
}

export function addTask(task) {
  document.dispatchEvent(new CustomEvent('displayTopBarLoading'));
  return fetch('/portal/rest/tasks', {
    headers: {
      Accept: 'application/json',
      'Content-Type': 'application/json'
    },
    method: 'POST',
    credentials: 'include',
    body: JSON.stringify(task)
  }).then((resp) => {
    if (resp && resp.ok) {
      return resp.json();
    } else {
      throw new Error('Error when adding task');
    }
  }).finally(() => document.dispatchEvent(new CustomEvent('hideTopBarLoading')));
}

export function getMyAllLabels() {
  return fetch('/portal/rest/tasks/labels', {
    method: 'GET',
    credentials: 'include',
  }).then((resp) => {
    if (resp && resp.ok) {
      return resp.json();
    } else {
      throw new Error('Error when getting my all labels');
    }
  });
}

export function getProjectLabels(projectId) {
  return fetch(`/portal/rest/tasks/labels/project/${projectId}`, {
    method: 'GET',
    credentials: 'include',
  }).then((resp) => {
    if (resp && resp.ok) {
      return resp.json();
    } else {
      throw new Error('Error when getting my all labels');
    }
  });
}

export function getTaskLabels(taskId) {
  return fetch(`/portal/rest/tasks/labels/${taskId}`, {
    method: 'GET',
    credentials: 'include',
  }).then((resp) => {
    if (resp && resp.ok) {
      return resp.json();
    } else {
      throw new Error('Error when getting task labels');
    }
  });
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
    if (resp && resp.ok) {
      return resp.json();
    } else {
      throw new Error('Error when adding task to label');
    }
  });
}

export function removeTaskFromLabel(taskId, labelId) {
  return fetch(`/portal/rest/tasks/labels/${taskId}/${labelId}`, {
    credentials: 'include',
    method: 'delete',
  }).then((resp) => {
    if (resp && resp.ok) {
      return resp;
    } else {
      throw new Error('Error when deleting task from label');
    }
  });
}

export function addLabel(label) {
  return fetch('/portal/rest/tasks/labels', {
    headers: {
      Accept: 'application/json',
      'Content-Type': 'application/json',
    },
    method: 'POST',
    credentials: 'include',
    body: JSON.stringify(label)
  }).then((resp) => {
    if (resp && resp.ok) {
      return resp.json();
    } else {
      throw new Error('Error when adding task to label');
    }
  });
}

export function editLabel(label) {
  return fetch(`/portal/rest/tasks/labels/${label.id}`, {
    headers: {
      Accept: 'application/json',
      'Content-Type': 'application/json',
    },
    method: 'PUT',
    credentials: 'include',
    body: JSON.stringify(label)
  }).then((resp) => {
    if (resp && resp.ok) {
      return resp.json();
    } else {
      throw new Error('Error when updateing label');
    }
  });
}

export function removeLabel(labelId) {
  return fetch(`/portal/rest/tasks/labels/${labelId}`, {
    credentials: 'include',
    method: 'delete',
  }).then((resp) => {
    if (resp && resp.ok) {
      return resp;
    } else {
      throw new Error('Error when deleting label');
    }
  });
}

export function getProjects() {
  return fetch('/portal/rest/projects/projects', {
    method: 'GET',
    credentials: 'include',
  }).then((resp) => {
    if (resp && resp.ok) {
      return resp.json();
    } else {
      throw new Error('Error when getting projects');
    }
  });
}

export function getDefaultStatusByProjectId(projectId) {
  return fetch(`/portal/rest/projects/projects/status/${projectId}`, {
    method: 'GET',
    credentials: 'include',
  }).then((resp) => {
    if (resp && resp.ok) {
      return resp.json();
    } else {
      throw new Error('Error when getting default status');
    }
  });
}

export function getStatusesByProjectId(projectId) {
  return fetch(`/portal/rest/projects/projects/statuses/${projectId}`, {
    method: 'GET',
    credentials: 'include',
  }).then((resp) => {
    if (resp && resp.ok) {
      return resp.json();
    } else {
      throw new Error('Error when getting project statuses');
    }
  });
}

export function getAllUsers() {
  return fetch('/portal/rest/v1/social/users', {
    method: 'GET',
    credentials: 'include',
  }).then((resp) => {
    if (resp && resp.ok) {
      return resp.json();
    } else {
      throw new Error('Error when getting users');
    }
  });
}

export function getTaskLogs(taskId) {
  return fetch(`/portal/rest/tasks/logs/${taskId}`, {
    method: 'GET',
    credentials: 'include',
  }).then((resp) => {
    if (resp && resp.ok) {
      return resp.json();
    } else {
      throw new Error('Error when getting task logs');
    }
  });
}

export function getTaskComments(taskId) {
  return fetch(`/portal/rest/tasks/comments/${taskId}`, {
    method: 'GET',
    credentials: 'include',
  }).then((resp) => {
    if (resp && resp.ok) {
      return resp.json();
    } else {
      throw new Error('Error when getting task comments');
    }
  });
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
    if (resp && resp.ok) {
      return resp.json();
    } else {
      throw new Error('Error when adding task comment');
    }
  });
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
    if (resp && resp.ok) {
      return resp.json();
    } else {
      throw new Error('Error when adding task sub comment');
    }
  });
}

export function removeTaskComment(commentId) {
  return fetch(`/portal/rest/tasks/comments/${commentId}`, {
    method: 'delete',
    credentials: 'include',
  }).then((resp) => {
    if (resp && resp.ok) {
      return resp.json();
    } else {
      throw new Error('Error when removing task comment');
    }
  });
}

export function findUsersToMention(projectId, query) {
  const fetchUrl = projectId ? `/portal/rest/projects/projectParticipants/${projectId}/${query}` :
    `/portal/rest/tasks/usersToMention/${query}`;
  return fetch(fetchUrl, {
    method: 'GET',
    credentials: 'include',
  }).then((resp) => {
    if (resp && resp.ok) {
      return resp.json();
    } else {
      throw new Error('Error when getting users to mention');
    }
  });
}

export function getSuggestedUsers(query, projectName) {
  return fetch(`/portal/rest/tasks/users/${query}/${projectName}`, {
    method: 'GET',
    credentials: 'include',
  }).then((resp) => {
    if (resp && resp.ok) {
      return resp.json();
    } else {
      throw new Error('Error when getting suggested users');
    }
  });
}

export function getUser(username) {
  return fetch(`/portal/rest/v1/social/users/${username}`, {
    method: 'GET',
    credentials: 'include',
  }).then((resp) => {
    if (resp && resp.ok) {
      return resp.json();
    } else {
      throw new Error('Error when getting user');
    }
  });
}

export function urlVerify(text) {
  return text.replace(/((?:href|src)=")?((((https?|ftp|file):\/\/)|www\.)[-a-zA-Z0-9+&@#/%?=~_|!:,.;]*[-a-zA-Z0-9+&@#/%=~_|])/ig,
    function(matchedText, hrefOrSrc) {
      // the second group of the regex captures the html attribute 'html' or 'src',
      // so if it exists it means that it is already an html link or an image and it should not be converted
      if (hrefOrSrc) {
        return matchedText;
      }
      let url = matchedText;
      if (url.indexOf('www.') === 0) {
        url = `//${url}`;
      }
      return `<a href="${url}" target="_blank">${matchedText}</a>`;
    });
}

export function cloneTask(taskId) {
  return fetch(`${tasksConstants.PORTAL}/${tasksConstants.PORTAL_REST}/tasks/clone/${taskId}`, {
    credentials: 'include',
    method: 'POST',
  }).then(resp => {
    if (resp && resp.ok) {
      return resp.json();
    } else {
      throw new Error('Error when cloning task');
    }
  });
}
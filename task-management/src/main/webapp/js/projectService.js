import {tasksConstants} from "./tasksConstants";

export function getProjectsList (offset, limit) {
  return fetch(`${tasksConstants.PORTAL}/${tasksConstants.PORTAL_REST}/projects/projects?offset=${offset || 0}&limit=${limit|| 0}`, {
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

export function addProject(project) {
  return fetch(`${tasksConstants.PORTAL}/${tasksConstants.PORTAL_REST}/projects/createproject`, {
    headers: {
      'Content-Type': 'application/json'
    },
    credentials: 'include',
    method: 'POST',
    body: JSON.stringify(project)
  }).then((data) => {
    return data.json();
  });
}

export function updateProjectInfo(project) {
  return fetch(`${tasksConstants.PORTAL}/${tasksConstants.PORTAL_REST}/projects/updateproject/${project.id}`, {
    headers: {
      'Content-Type': 'application/json'
    },
    credentials: 'include',
    method: 'PUT',
    body: JSON.stringify(project)
  }).then(resp => resp.json());
}

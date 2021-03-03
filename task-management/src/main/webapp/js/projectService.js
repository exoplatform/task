import { tasksConstants } from './tasksConstants';

export function getProjectsList(spaceName, query, projectFilter, offset, limit, participatorParam) {
  return fetch(`${tasksConstants.PORTAL}/${tasksConstants.PORTAL_REST}/projects/projects?q=${query || ''}&spaceName=${spaceName || ''}&projectsFilter=${projectFilter || ''}&offset=${offset || 0}&limit=${limit|| 0}&participatorParam=${participatorParam|| false}`, {
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

export function getProjectStats(id) {
  return fetch(`${tasksConstants.PORTAL}/${tasksConstants.PORTAL_REST}/projects/project/statistics/${id}`, {
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

export function getProject(id, participatorParam) {
  return fetch(`${tasksConstants.PORTAL}/${tasksConstants.PORTAL_REST}/projects/projects/${id}?&participatorParam=${participatorParam|| false}`, {
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

export function deleteProjectInfo(project) {
  return fetch(`${tasksConstants.PORTAL}/${tasksConstants.PORTAL_REST}/projects/${project.id}?&deleteChild=false`, {
    credentials: 'include',
    method: 'DELETE'
  });
}

export function cloneProject(project) {
  return fetch(`${tasksConstants.PORTAL}/${tasksConstants.PORTAL_REST}/projects/cloneproject`, {
    headers: {
      'Content-Type': 'application/json'
    },
    credentials: 'include',
    method: 'POST',
    body: JSON.stringify(project)
  });
}

export function updateProjectColor(project, color) {
  return fetch(`${tasksConstants.PORTAL}/${tasksConstants.PORTAL_REST}/projects/changeProjectColor/${project.id}?&color=${color}`, {
    headers: {
      'Content-Type': 'application/json'
    },
    credentials: 'include',
    method: 'PUT',
    body: JSON.stringify(project)
  }).then(resp => resp.json());
}

export function saveFilterSettings(valueOfFilter) {
  return fetch(`${eXo.env.portal.context}/${eXo.env.portal.rest}/v1/settings/USER,${eXo.env.portal.userName}/APPLICATION,task-${valueOfFilter.projectId}/filterTask`, {
    method: 'PUT',
    credentials: 'include',
    headers: {
      Accept: 'application/json',
      'Content-Type': 'application/json',
    },
    body: JSON.stringify({'value': JSON.stringify(valueOfFilter)}),
  }).then((resp) => {
    if (resp && resp.ok) {
      return resp;
    } else {
      throw new Error('Error setting filter  settings');
    }
  }).then(resp => {
    return resp;
  });
}
export function getFilterSettings(ProjectId) {
  return fetch(`${eXo.env.portal.context}/${eXo.env.portal.rest}/v1/settings/USER,${eXo.env.portal.userName}/APPLICATION,task-${ProjectId}/filterTask`, {
    method: 'GET',
    credentials: 'include',
    headers: {
      Accept: 'application/json',
      'Content-Type': 'application/json',
    },
  }).then((resp) => {
    if (resp && resp.ok) {
      return resp.json();
    } else if (resp && resp.status === 404) {
      return null;
    } else {
      throw new Error('Error getting filter settings');
    }
  }).then(resp => {
    return resp;
  });
}

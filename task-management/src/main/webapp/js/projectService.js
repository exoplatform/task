import { tasksConstants } from "./tasksConstants";

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
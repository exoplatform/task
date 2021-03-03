import { tasksConstants } from "./tasksConstants";


export function createStatus(status) {
    return fetch(`${tasksConstants.PORTAL}/${tasksConstants.PORTAL_REST}/status`, {
        headers: {
            'Content-Type': 'application/json'
        },
        credentials: 'include',
        method: 'POST',
        body: JSON.stringify(status)
    }).then(resp => resp.json());
}

export function updateStatus(status) {
    return fetch(`${tasksConstants.PORTAL}/${tasksConstants.PORTAL_REST}/status/${status.id}`, {
        headers: {
            'Content-Type': 'application/json'
        },
        credentials: 'include',
        method: 'PUT',
        body: JSON.stringify(status)
    }).then(resp => resp.json());
}

export function moveStatus(statusList) {
    return fetch(`${tasksConstants.PORTAL}/${tasksConstants.PORTAL_REST}/status/move`, {
        headers: {
            'Content-Type': 'application/json'
        },
        credentials: 'include',
        method: 'PUT',
        body: JSON.stringify(statusList)
    }).then(resp => resp.json());
}

export function deleteStatus(statusId) {
    return fetch(`${tasksConstants.PORTAL}/${tasksConstants.PORTAL_REST}/status/${statusId}`, {
        credentials: 'include',
        method: 'DELETE',
    })
}
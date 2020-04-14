export function getMyAllTasks(itemsLimit) {
  return fetch(`/portal/rest/tasks?limit=${itemsLimit}`, {
    method: 'GET',
  }).then((resp) => {
    if(resp && resp.ok) {
      return resp.json();
    } else {
      throw new Error ('Error when getting my all tasks');
    }
  })
}

export function getMyIncomingTasks(itemsLimit) {
  return fetch(`/portal/rest/tasks?status=incoming&limit=${itemsLimit}&returnSize=true`, {
    method: 'GET',
  }).then((resp) => {
    if(resp && resp.ok) {
      return resp.json();
    } 
    else {
      throw new Error ('Error when getting my incoming tasks');
    }
  })
}

export function getMyOverdueTasks(itemsLimit) {
  return fetch(`/portal/rest/tasks?status=overdue&limit=${itemsLimit}&returnSize=true`, {
    method: 'GET',
  }).then((resp) => {
    if(resp && resp.ok) {
      return resp.json();
    } 
    else {
      throw new Error ('Error when getting my overdue tasks');
    }
  })
}
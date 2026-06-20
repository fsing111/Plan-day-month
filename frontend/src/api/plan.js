import request from './request'

// Plan APIs will be implemented in plan module
export function getPlanList(params) {
  return request.get('/plans', { params })
}

export function getPlanDetail(id) {
  return request.get(`/plans/${id}`)
}

export function createPlan(data) {
  return request.post('/plans', data)
}

export function updatePlan(id, data) {
  return request.put(`/plans/${id}`, data)
}

export function submitPlan(id) {
  return request.post(`/plans/${id}/submit`)
}

export function deletePlan(id) {
  return request.delete(`/plans/${id}`)
}

export function getCalendarData(params) {
  return request.get('/plans/calendar', { params })
}

export function getRollupOptions(id, params) {
  return request.get(`/plans/${id}/rollup-options`, { params })
}

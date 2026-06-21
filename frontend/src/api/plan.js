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

export function withdrawPlan(id) {
  return request.post(`/plans/${id}/withdraw`)
}

export function copyPlan(id) {
  return request.post(`/plans/copy/${id}`)
}

export function getPlanRevisions(id) {
  return request.get(`/plans/${id}/revisions`)
}

export function exportPlans(params) {
  return request.get('/export/plans', { params, responseType: 'blob' })
}

export function getApprovedPlansWithoutAchievement() {
  return request.get('/plans/approved-without-achievement')
}

export function getCategories() {
  return request.get('/plans/categories')
}

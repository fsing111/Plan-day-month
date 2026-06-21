import request from './request'

// Achievement APIs will be implemented in achievement module
export function getAchievementList(params) {
  return request.get('/achievements', { params })
}

export function getAchievementDetail(id) {
  return request.get(`/achievements/${id}`)
}

export function createAchievement(data) {
  return request.post('/achievements', data)
}

export function updateAchievement(id, data) {
  return request.put(`/achievements/${id}`, data)
}

export function submitAchievement(id) {
  return request.post(`/achievements/${id}/submit`)
}

export function getPlanAchievement(planId) {
  return request.get(`/plans/${planId}/achievement`)
}

export function withdrawAchievement(id) {
  return request.post(`/achievements/${id}/withdraw`)
}

export function deleteAchievement(id) {
  return request.delete(`/achievements/${id}`)
}

export function exportAchievements(params) {
  return request.get('/export/achievements', { params, responseType: 'blob' })
}

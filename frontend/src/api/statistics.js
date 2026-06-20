import request from './request'

// Statistics APIs will be implemented in statistics module
export function getPersonalStats(params) {
  return request.get('/statistics/personal', { params })
}

export function getTeamStats(params) {
  return request.get('/statistics/team', { params })
}

export function getTrendData(params) {
  return request.get('/statistics/trend', { params })
}

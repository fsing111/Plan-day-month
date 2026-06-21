import request from './request'

export function getOperationLogs(params) {
  return request.get('/admin/logs', { params })
}

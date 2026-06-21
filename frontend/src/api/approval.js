import request from './request'

// Approval APIs will be implemented in approval module
export function getPendingApprovals(params) {
  return request.get('/approvals/pending', { params })
}

export function getApprovalHistory(params) {
  return request.get('/approvals/history', { params })
}

export function approveRecord(recordId, data) {
  return request.post(`/approvals/${recordId}/approve`, data)
}

export function rejectRecord(recordId, data) {
  return request.post(`/approvals/${recordId}/reject`, data)
}

export function transferRecord(recordId, data) {
  return request.post(`/approvals/${recordId}/transfer`, data)
}

export function getApprovalTimeline(targetType, targetId) {
  return request.get(`/approvals/${targetType}/${targetId}/timeline`)
}

export function batchApprove(recordIds, comment) {
  return request.post('/approvals/batch-approve', { recordIds, comment })
}

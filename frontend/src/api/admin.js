import request from './request'

// Admin APIs will be implemented in admin module
export function getUserList(params) {
  return request.get('/admin/users', { params })
}

export function createUser(data) {
  return request.post('/admin/users', data)
}

export function updateUser(id, data) {
  return request.put(`/admin/users/${id}`, data)
}

export function disableUser(id) {
  return request.put(`/admin/users/${id}/disable`)
}

export function resetPassword(id, data) {
  return request.put(`/admin/users/${id}/reset-password`, data)
}

export function getDepartmentList() {
  return request.get('/admin/departments')
}

export function createDepartment(data) {
  return request.post('/admin/departments', data)
}

export function updateDepartment(id, data) {
  return request.put(`/admin/departments/${id}`, data)
}

export function getApprovalChains(params) {
  return request.get('/admin/approval-chains', { params })
}

export function createApprovalChain(data) {
  return request.post('/admin/approval-chains', data)
}

export function updateApprovalChain(id, data) {
  return request.put(`/admin/approval-chains/${id}`, data)
}

export function deleteApprovalChain(id) {
  return request.delete(`/admin/approval-chains/${id}`)
}

export function getCategories(params) {
  return request.get('/admin/categories', { params })
}

export function createCategory(data) {
  return request.post('/admin/categories', data)
}

export function updateCategory(id, data) {
  return request.put(`/admin/categories/${id}`, data)
}

export function deleteCategory(id) {
  return request.delete(`/admin/categories/${id}`)
}

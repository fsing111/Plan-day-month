import request from './request'

// File APIs will be implemented in file module
export function uploadFile(formData) {
  return request.post('/files/upload', formData, {
    headers: { 'Content-Type': 'multipart/form-data' }
  })
}

export function downloadFile(id) {
  return request.get(`/files/${id}/download`, { responseType: 'blob' })
}

export function deleteFile(id) {
  return request.delete(`/files/${id}`)
}

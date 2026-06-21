import request from './request'

export function getRecycleBinItems(params) {
  return request.get('/recycle-bin', { params })
}

export function restoreItem(id) {
  return request.post(`/recycle-bin/${id}/restore`)
}

export function permanentDelete(id) {
  return request.delete(`/recycle-bin/${id}`)
}

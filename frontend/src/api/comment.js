import request from './request'

export function getComments(targetId, targetType) {
  return request.get('/comments', { params: { targetId, targetType } })
}

export function addComment(data) {
  return request.post('/comments', data)
}

export function deleteComment(id) {
  return request.delete(`/comments/${id}`)
}

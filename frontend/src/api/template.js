import request from './request'

export function getTemplates() {
  return request.get('/templates')
}

export function saveTemplate(data) {
  return request.post('/templates', data)
}

export function deleteTemplate(id) {
  return request.delete(`/templates/${id}`)
}

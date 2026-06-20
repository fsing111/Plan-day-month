import request from './request'

/**
 * User login
 */
export function login(data) {
  return request.post('/auth/login', data)
}

/**
 * User logout
 */
export function logout() {
  return request.post('/auth/logout')
}

/**
 * Get current user info
 */
export function getCurrentUser() {
  return request.get('/auth/me')
}

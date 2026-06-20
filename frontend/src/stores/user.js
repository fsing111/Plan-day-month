import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import { getToken, setToken, removeToken, getUser, setUser, removeUser } from '@/utils/auth'

export const useUserStore = defineStore('user', () => {
  const token = ref(getToken())
  const user = ref(getUser())

  const isLoggedIn = computed(() => !!token.value)
  const role = computed(() => user.value?.role || '')
  const isAdmin = computed(() => user.value?.role === 'ADMIN')
  const isLeader = computed(() => user.value?.role === 'LEADER' || user.value?.role === 'ADMIN')

  function login(userData, tokenValue) {
    token.value = tokenValue
    user.value = userData
    setToken(tokenValue)
    setUser(userData)
  }

  function logout() {
    token.value = null
    user.value = null
    removeToken()
    removeUser()
  }

  return {
    token,
    user,
    isLoggedIn,
    role,
    isAdmin,
    isLeader,
    login,
    logout
  }
})

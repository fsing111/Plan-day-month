import { defineStore } from 'pinia'
import { ref } from 'vue'

export const useNotificationStore = defineStore('notification', () => {
  const unreadCount = ref(0)

  function setUnreadCount(count) {
    unreadCount.value = count
  }

  function incrementUnread() {
    unreadCount.value++
  }

  function decrementUnread(n = 1) {
    unreadCount.value = Math.max(0, unreadCount.value - n)
  }

  return {
    unreadCount,
    setUnreadCount,
    incrementUnread,
    decrementUnread
  }
})

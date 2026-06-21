import { Client } from '@stomp/stompjs'
import SockJS from 'sockjs-client'
import { getToken } from './auth'
import { useNotificationStore } from '@/stores/notification'

let stompClient = null

/**
 * Connect to WebSocket notification channel
 */
export function connectWebSocket() {
  const token = getToken()
  if (!token) return

  const socket = new SockJS('/ws/notifications')
  stompClient = new Client({
    webSocketFactory: () => socket,
    connectHeaders: {
      Authorization: `Bearer ${token}`
    },
    debug: () => {},
    reconnectDelay: 5000,
    heartbeatIncoming: 4000,
    heartbeatOutgoing: 4000,
    onConnect: () => {
      console.log('[WebSocket] Connected to notification channel')
      stompClient.subscribe('/user/queue/notifications', (message) => {
        try {
          const data = JSON.parse(message.body)
          handleNotification(data)
        } catch (e) {
          console.error('[WebSocket] Failed to parse notification:', e)
        }
      })
    },
    onStompError: (frame) => {
      console.error('[WebSocket] STOMP error:', frame.headers['message'])
    }
  })

  stompClient.activate()
}

/**
 * Disconnect WebSocket
 */
export function disconnectWebSocket() {
  if (stompClient) {
    stompClient.deactivate()
    stompClient = null
    console.log('[WebSocket] Disconnected')
  }
}

/**
 * Handle incoming notification
 */
function handleNotification(data) {
  const store = useNotificationStore()
  store.incrementUnread()

  // Show message notification
  if (data.type === 'NOTIFICATION' && data.data) {
    const { title } = data.data
    // Use browser Notification API if available
    if (Notification.permission === 'granted') {
      new Notification('计划管理系统', { body: title })
    }
  }
}

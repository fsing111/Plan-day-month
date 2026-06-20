<template>
  <div class="page-container">
    <div class="page-header">
      <h2>通知列表</h2>
      <div class="header-actions">
        <el-button
          v-if="unreadCount > 0"
          type="primary"
          size="default"
          @click="handleReadAll"
        >
          <el-icon><Check /></el-icon>
          全部标记已读
        </el-button>
      </div>
    </div>

    <!-- Notification Cards -->
    <div v-loading="loading" class="notification-list">
      <div
        v-for="item in notifications"
        :key="item.id"
        class="notification-item"
        :class="{ 'is-unread': !item.isRead }"
        @click="handleClick(item)"
      >
        <div class="notification-left">
          <div class="notification-icon">
            <el-icon :size="22">
              <component :is="typeIcon(item.type)" />
            </el-icon>
          </div>
        </div>
        <div class="notification-body">
          <div class="notification-header">
            <span class="notification-title" :class="{ 'font-bold': !item.isRead }">
              {{ item.title }}
            </span>
            <span class="notification-time">{{ formatTime(item.createdAt) }}</span>
          </div>
          <div v-if="item.content" class="notification-content">
            {{ item.content }}
          </div>
        </div>
        <div class="notification-right">
          <!-- Unread dot -->
          <span v-if="!item.isRead" class="unread-dot" />
          <!-- Mark as read -->
          <el-button
            v-if="!item.isRead"
            link
            type="primary"
            size="small"
            @click.stop="handleMarkRead(item)"
          >
            标为已读
          </el-button>
          <!-- Delete -->
          <el-popconfirm
            title="确定删除此通知？"
            @confirm="handleDelete(item.id)"
            @click.stop
          >
            <template #reference>
              <el-button link type="danger" size="small">
                <el-icon><Delete /></el-icon>
              </el-button>
            </template>
          </el-popconfirm>
        </div>
      </div>

      <el-empty v-if="!loading && notifications.length === 0" description="暂无通知" />
    </div>

    <!-- Pagination -->
    <div class="pagination-wrap">
      <el-pagination
        v-model:current-page="pagination.page"
        v-model:page-size="pagination.pageSize"
        :page-sizes="[10, 20, 50]"
        :total="pagination.total"
        layout="total, sizes, prev, pager, next, jumper"
        @size-change="fetchData"
        @current-change="fetchData"
      />
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import {
  Check, Delete, Bell, WarningFilled, CircleCheckFilled,
  CircleCloseFilled, Clock, Message
} from '@element-plus/icons-vue'
import { getNotifications, markAsRead, markAllAsRead, deleteNotification } from '@/api/notification'
import { useNotificationStore } from '@/stores/notification'
import { formatDate } from '@/utils/date'

const router = useRouter()
const notificationStore = useNotificationStore()

const loading = ref(false)
const notifications = ref([])
const unreadCount = ref(0)

const pagination = reactive({
  page: 1,
  pageSize: 20,
  total: 0
})

const typeIconMap = {
  PLAN_REJECTED: CircleCloseFilled,
  PLAN_APPROVED: CircleCheckFilled,
  NEW_APPROVAL_PLAN: Bell,
  NEW_APPROVAL_ACHV: Bell,
  REMIND_SUBMIT_PLAN: Clock,
  REMIND_APPROVE: Clock,
  ACHV_REJECTED: CircleCloseFilled,
  ACHV_APPROVED: CircleCheckFilled
}

function typeIcon(type) {
  return typeIconMap[type] || Message
}

function formatTime(timeStr) {
  if (!timeStr) return ''
  const d = new Date(timeStr)
  const now = new Date()
  const diff = now - d

  // Within 1 hour: show relative time
  if (diff < 3600000) {
    const minutes = Math.floor(diff / 60000)
    return minutes <= 0 ? '刚刚' : `${minutes}分钟前`
  }
  // Today: show time
  if (d.toDateString() === now.toDateString()) {
    return formatDate(timeStr, 'HH:mm')
  }
  // Yesterday
  const yesterday = new Date(now)
  yesterday.setDate(yesterday.getDate() - 1)
  if (d.toDateString() === yesterday.toDateString()) {
    return `昨天 ${formatDate(timeStr, 'HH:mm')}`
  }
  // Older: full date
  return formatDate(timeStr, 'YYYY-MM-DD HH:mm')
}

async function fetchData() {
  loading.value = true
  try {
    const params = { page: pagination.page, pageSize: pagination.pageSize }
    const res = await getNotifications(params)
    if (res.code === 200 && res.data) {
      notifications.value = res.data.records || []
      pagination.total = res.data.total || 0
      unreadCount.value = res.data.unreadCount || 0
      notificationStore.setUnreadCount(unreadCount.value)
    }
  } catch {
    ElMessage.error('获取通知列表失败')
  } finally {
    loading.value = false
  }
}

async function handleMarkRead(item) {
  if (item.isRead) return
  try {
    await markAsRead(item.id)
    item.isRead = true
    notificationStore.decrementUnread()
    unreadCount.value = Math.max(0, unreadCount.value - 1)
  } catch {
    ElMessage.error('操作失败')
  }
}

async function handleReadAll() {
  try {
    await markAllAsRead()
    notifications.value.forEach(n => n.isRead = true)
    notificationStore.setUnreadCount(0)
    unreadCount.value = 0
    ElMessage.success('已全部标记为已读')
  } catch {
    ElMessage.error('操作失败')
  }
}

function handleClick(item) {
  // Mark as read on click
  if (!item.isRead) {
    handleMarkRead(item)
  }
  // Navigate to related business
  if (item.relatedId && item.relatedType) {
    if (item.relatedType === 'PLAN') {
      router.push(`/plans/${item.relatedId}`)
    } else if (item.relatedType === 'ACHIEVEMENT') {
      router.push(`/achievements/${item.relatedId}`)
    }
  }
}

async function handleDelete(id) {
  try {
    await deleteNotification(id)
    notifications.value = notifications.value.filter(n => n.id !== id)
    ElMessage.success('已删除')
    fetchData()
  } catch {
    ElMessage.error('删除失败')
  }
}

onMounted(() => { fetchData() })
</script>

<style scoped>
.page-container { padding: 20px; }
.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}
.page-header h2 { margin: 0; }
.header-actions { display: flex; gap: 8px; }

.notification-list {
  background: #fff;
  border-radius: 4px;
  min-height: 200px;
}

.notification-item {
  display: flex;
  align-items: flex-start;
  padding: 16px 20px;
  border-bottom: 1px solid #f0f0f0;
  cursor: pointer;
  transition: background 0.2s;
}

.notification-item:hover {
  background: #f5f7fa;
}

.notification-item.is-unread {
  background: #ecf5ff;
}

.notification-item.is-unread:hover {
  background: #d9ecff;
}

.notification-left {
  margin-right: 12px;
  flex-shrink: 0;
}

.notification-icon {
  width: 36px;
  height: 36px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 50%;
  background: #ecf0f5;
  color: #909399;
}

.is-unread .notification-icon {
  background: #e6f7ff;
  color: #409eff;
}

.notification-body {
  flex: 1;
  min-width: 0;
}

.notification-header {
  display: flex;
  justify-content: space-between;
  align-items: baseline;
  margin-bottom: 4px;
}

.notification-title {
  font-size: 14px;
  color: #303133;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  flex: 1;
  margin-right: 12px;
}

.notification-title.font-bold {
  font-weight: 600;
}

.notification-time {
  font-size: 12px;
  color: #c0c4cc;
  flex-shrink: 0;
}

.notification-content {
  font-size: 13px;
  color: #909399;
  line-height: 1.5;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.notification-right {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-left: 12px;
  flex-shrink: 0;
}

.unread-dot {
  width: 8px;
  height: 8px;
  border-radius: 50%;
  background: #f56c6c;
}

.pagination-wrap {
  display: flex;
  justify-content: flex-end;
  margin-top: 16px;
}
</style>

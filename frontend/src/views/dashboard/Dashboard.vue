<template>
  <div class="page-container">
    <div class="page-header">
      <h2>工作台</h2>
      <span class="welcome-text">欢迎回来，{{ userStore.user?.realName || '用户' }}</span>
    </div>

    <!-- 概览卡片 -->
    <el-row :gutter="16" class="stats-row">
      <el-col :xs="12" :sm="6">
        <el-card shadow="hover" class="stat-card">
          <div class="stat-value">{{ dashboard.todayPlanCount }}</div>
          <div class="stat-label">今日计划</div>
        </el-card>
      </el-col>
      <el-col :xs="12" :sm="6">
        <el-card shadow="hover" class="stat-card">
          <div class="stat-value">{{ dashboard.pendingApprovalCount }}</div>
          <div class="stat-label">待审批</div>
        </el-card>
      </el-col>
      <el-col :xs="12" :sm="6">
        <el-card shadow="hover" class="stat-card warning">
          <div class="stat-value">{{ dashboard.rejectedCount }}</div>
          <div class="stat-label">待修改</div>
        </el-card>
      </el-col>
      <el-col :xs="12" :sm="6">
        <el-card shadow="hover" class="stat-card success">
          <div class="stat-value">{{ dashboard.weeklyCompletionRate }}%</div>
          <div class="stat-label">本周完成率</div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 领导专属 -->
    <el-row v-if="userStore.isLeader" :gutter="16" class="stats-row">
      <el-col :xs="12" :sm="6">
        <el-card shadow="hover" class="stat-card">
          <div class="stat-value">{{ dashboard.teamTotalMembers }}</div>
          <div class="stat-label">团队成员</div>
        </el-card>
      </el-col>
      <el-col :xs="12" :sm="6">
        <el-card shadow="hover" class="stat-card success">
          <div class="stat-value">{{ dashboard.teamSubmittedToday }}</div>
          <div class="stat-label">今日已提交</div>
        </el-card>
      </el-col>
      <el-col :xs="12" :sm="6">
        <el-card shadow="hover" class="stat-card danger">
          <div class="stat-value">{{ dashboard.overdueCount }}</div>
          <div class="stat-label">逾期预警</div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 快捷入口 -->
    <el-row :gutter="16" class="quick-actions">
      <el-col :span="24">
        <el-card shadow="never">
          <template #header>快捷操作</template>
          <el-space wrap>
            <el-button type="primary" @click="$router.push('/plans/create')">新建日报</el-button>
            <el-button @click="$router.push('/plans/create?type=weekly')">新建周报</el-button>
            <el-button @click="$router.push('/plans/create?type=monthly')">新建月报</el-button>
            <el-button type="success" @click="$router.push('/plans')">查看计划</el-button>
            <el-button v-if="userStore.isLeader" type="warning" @click="$router.push('/approvals/pending')">审批中心</el-button>
          </el-space>
        </el-card>
      </el-col>
    </el-row>

    <!-- 最近通知 -->
    <el-row :gutter="16" class="recent-section">
      <el-col :span="24">
        <el-card shadow="never">
          <template #header>最近通知</template>
          <div v-if="dashboard.recentNotifications && dashboard.recentNotifications.length > 0">
            <div v-for="n in dashboard.recentNotifications" :key="n.id" class="notification-item">
              <el-tag size="small" style="margin-right: 8px">{{ n.title }}</el-tag>
              <span>{{ n.content }}</span>
            </div>
          </div>
          <el-empty v-else description="暂无通知" :image-size="60" />
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useUserStore } from '@/stores/user'
import { getDashboard } from '@/api/dashboard'

const userStore = useUserStore()
const dashboard = reactive({
  todayPlanCount: 0,
  pendingApprovalCount: 0,
  rejectedCount: 0,
  weeklyCompletionRate: 0,
  teamTotalMembers: 0,
  teamSubmittedToday: 0,
  overdueCount: 0,
  recentNotifications: []
})

onMounted(async () => {
  try {
    const res = await getDashboard()
    if (res.code === 200 && res.data) {
      Object.assign(dashboard, res.data)
    }
  } catch { /* ignore */ }
})
</script>

<style scoped>
.welcome-text { color: #909399; font-size: 14px; }
.stats-row { margin-bottom: 16px; }
.stat-card { text-align: center; }
.stat-value { font-size: 28px; font-weight: bold; color: #409eff; }
.stat-card.warning .stat-value { color: #e6a23c; }
.stat-card.success .stat-value { color: #67c23a; }
.stat-card.danger .stat-value { color: #f56c6c; }
.stat-label { font-size: 13px; color: #909399; margin-top: 4px; }
.quick-actions { margin-bottom: 16px; }
.notification-item { padding: 8px 0; border-bottom: 1px solid #ebeef5; }
</style>

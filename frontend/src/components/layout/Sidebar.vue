<template>
  <div class="sidebar">
    <div class="sidebar-logo">
      <h2>📋 计划管理系统</h2>
    </div>
    <el-menu
      :default-active="activeMenu"
      background-color="#304156"
      text-color="#bfcbd9"
      active-text-color="#409eff"
      router
    >
      <el-sub-menu index="plans-group">
        <template #title>
          <el-icon><Document /></el-icon>
          <span>工作计划</span>
        </template>
        <el-menu-item index="/plans">计划列表</el-menu-item>
        <el-menu-item index="/plans/create">新建日报</el-menu-item>
        <el-menu-item index="/plans/create?type=weekly">新建周报</el-menu-item>
        <el-menu-item index="/plans/create?type=monthly">新建月报</el-menu-item>
        <el-menu-item index="/plans/calendar">日历视图</el-menu-item>
      </el-sub-menu>

      <el-sub-menu index="achievements-group">
        <template #title>
          <el-icon><Checked /></el-icon>
          <span>成果管理</span>
        </template>
        <el-menu-item index="/achievements">成果列表</el-menu-item>
      </el-sub-menu>

      <el-sub-menu index="stats-group">
        <template #title>
          <el-icon><DataAnalysis /></el-icon>
          <span>查询统计</span>
        </template>
        <el-menu-item index="/statistics/personal">个人统计</el-menu-item>
        <el-menu-item v-if="userStore.isLeader" index="/statistics/team">团队统计</el-menu-item>
      </el-sub-menu>

      <el-sub-menu v-if="userStore.isLeader" index="approvals-group">
        <template #title>
          <el-icon><Files /></el-icon>
          <span>审批中心</span>
        </template>
        <el-menu-item index="/approvals/pending">待审批</el-menu-item>
        <el-menu-item index="/approvals/history">审批历史</el-menu-item>
      </el-sub-menu>

      <el-sub-menu v-if="userStore.isAdmin" index="admin-group">
        <template #title>
          <el-icon><Setting /></el-icon>
          <span>系统管理</span>
        </template>
        <el-menu-item index="/admin/users">用户管理</el-menu-item>
        <el-menu-item index="/admin/departments">部门管理</el-menu-item>
        <el-menu-item index="/admin/approval-chains">审批链配置</el-menu-item>
        <el-menu-item index="/admin/categories">分类管理</el-menu-item>
      </el-sub-menu>

      <el-menu-item index="/profile">
        <el-icon><User /></el-icon>
        <span>个人中心</span>
      </el-menu-item>
    </el-menu>
  </div>
</template>

<script setup>
import { computed } from 'vue'
import { useRoute } from 'vue-router'
import { useUserStore } from '@/stores/user'

const route = useRoute()
const userStore = useUserStore()

const activeMenu = computed(() => route.path)
</script>

<style scoped>
.sidebar {
  height: 100%;
  display: flex;
  flex-direction: column;
}

.sidebar-logo {
  padding: 16px 20px;
  text-align: center;
}

.sidebar-logo h2 {
  color: #fff;
  font-size: 16px;
  margin: 0;
  white-space: nowrap;
}

.el-menu {
  border-right: none;
  flex: 1;
}
</style>

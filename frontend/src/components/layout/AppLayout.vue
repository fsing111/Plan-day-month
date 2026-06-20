<template>
  <el-container class="app-layout">
    <el-aside width="220px" class="app-sidebar">
      <Sidebar />
    </el-aside>
    <el-container>
      <el-header class="app-header" height="60px">
        <Topbar />
      </el-header>
      <el-main class="app-main">
        <router-view />
      </el-main>
    </el-container>
  </el-container>
</template>

<script setup>
import Sidebar from './Sidebar.vue'
import Topbar from './Topbar.vue'
</script>

<style scoped>
.app-layout {
  height: 100vh;
}

.app-sidebar {
  background-color: #304156;
  overflow-y: auto;
}

.app-header {
  background-color: #fff;
  border-bottom: 1px solid #e6e6e6;
  display: flex;
  align-items: center;
  padding: 0 20px;
}

.app-main {
  background-color: #f5f7fa;
  overflow-y: auto;
}
</style>

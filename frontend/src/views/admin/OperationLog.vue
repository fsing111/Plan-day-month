<template>
  <div class="page-container">
    <div class="page-header"><h2>操作日志</h2></div>
    <div class="search-bar">
      <el-form :model="searchForm" inline>
        <el-form-item label="操作人">
          <el-input v-model="searchForm.username" placeholder="用户名" clearable style="width:150px" />
        </el-form-item>
        <el-form-item label="操作类型">
          <el-select v-model="searchForm.operation" placeholder="全部" clearable style="width:140px">
            <el-option label="创建" value="CREATE" />
            <el-option label="修改" value="UPDATE" />
            <el-option label="删除" value="DELETE" />
            <el-option label="提交" value="SUBMIT" />
            <el-option label="撤回" value="WITHDRAW" />
            <el-option label="通过" value="APPROVE" />
            <el-option label="驳回" value="REJECT" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">搜索</el-button>
          <el-button @click="handleReset">重置</el-button>
        </el-form-item>
      </el-form>
    </div>
    <el-table v-loading="loading" :data="tableData" stripe border>
      <el-table-column prop="username" label="操作人" width="100" />
      <el-table-column prop="operation" label="操作" width="80" />
      <el-table-column prop="targetType" label="目标类型" width="100" />
      <el-table-column prop="targetId" label="目标ID" width="80" />
      <el-table-column prop="summary" label="摘要" min-width="150" show-overflow-tooltip />
      <el-table-column prop="ipAddress" label="IP" width="140" />
      <el-table-column prop="createdAt" label="时间" width="160" />
    </el-table>
    <div class="pagination-wrap">
      <el-pagination
        v-model:current-page="pagination.page"
        v-model:page-size="pagination.pageSize"
        :page-sizes="[10, 20, 50]"
        :total="pagination.total"
        layout="total, sizes, prev, pager, next"
        @size-change="fetchData"
        @current-change="fetchData"
      />
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { getOperationLogs } from '@/api/log'

const router = useRouter()
const userStore = useUserStore()
const loading = ref(false)
const tableData = ref([])
const searchForm = reactive({ username: '', operation: '' })
const pagination = reactive({ page: 1, pageSize: 20, total: 0 })

onMounted(() => {
  if (!userStore.isLeader) {
    ElMessage.warning('没有权限访问该页面')
    router.replace('/dashboard')
    return
  }
  fetchData()
})

async function fetchData() {
  loading.value = true
  try {
    const params = { page: pagination.page, pageSize: pagination.pageSize }
    if (searchForm.username) params.username = searchForm.username
    if (searchForm.operation) params.operation = searchForm.operation
    const res = await getOperationLogs(params)
    if (res.code === 200 && res.data) {
      tableData.value = res.data.records || []
      pagination.total = res.data.total || 0
    }
  } catch { ElMessage.error('获取日志失败') }
  finally { loading.value = false }
}

function handleSearch() { pagination.page = 1; fetchData() }
function handleReset() { searchForm.username = ''; searchForm.operation = ''; handleSearch() }
</script>

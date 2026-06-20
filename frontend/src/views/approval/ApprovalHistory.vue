<template>
  <div class="page-container">
    <div class="page-header">
      <h2>审批历史</h2>
    </div>

    <!-- Search Bar -->
    <div class="search-bar">
      <el-form :model="searchForm" inline>
        <el-form-item label="审批类型">
          <el-select v-model="searchForm.targetType" placeholder="全部" clearable style="width: 140px">
            <el-option label="计划审批" value="PLAN" />
            <el-option label="成果验收" value="ACHIEVEMENT" />
          </el-select>
        </el-form-item>
        <el-form-item label="审批结果">
          <el-select v-model="searchForm.action" placeholder="全部" clearable style="width: 120px">
            <el-option label="通过" value="APPROVE" />
            <el-option label="驳回" value="REJECT" />
            <el-option label="转审" value="TRANSFER" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">
            <el-icon><Search /></el-icon>
            搜索
          </el-button>
          <el-button @click="handleReset">重置</el-button>
        </el-form-item>
      </el-form>
    </div>

    <!-- Table -->
    <el-table v-loading="loading" :data="tableData" stripe border style="width: 100%">
      <el-table-column label="类型" width="80">
        <template #default="{ row }">
          <el-tag :type="row.targetType === 'PLAN' ? '' : 'success'" size="small">
            {{ row.targetType === 'PLAN' ? '计划' : '成果' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="title" label="标题" min-width="180" show-overflow-tooltip />
      <el-table-column prop="submitterName" label="提交人" width="100" />
      <el-table-column label="审批结果" width="90">
        <template #default="{ row }">
          <el-tag :type="actionColor(row.action)" size="small" effect="dark">
            {{ actionLabel(row.action) }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="comment" label="审批意见" min-width="160" show-overflow-tooltip />
      <el-table-column prop="approvalLevel" label="级别" width="70">
        <template #default="{ row }">
          第{{ row.approvalLevel }}级
        </template>
      </el-table-column>
      <el-table-column prop="approvedAt" label="审批时间" width="160" />
      <el-table-column label="操作" width="100" fixed="right">
        <template #default="{ row }">
          <el-button
            link
            type="primary"
            size="small"
            @click="goToDetail(row)"
          >
            查看详情
          </el-button>
        </template>
      </el-table-column>
    </el-table>

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
import { Search } from '@element-plus/icons-vue'
import { getApprovalHistory } from '@/api/approval'

const router = useRouter()
const loading = ref(false)
const tableData = ref([])

const searchForm = reactive({
  targetType: '',
  action: ''
})

const pagination = reactive({
  page: 1,
  pageSize: 20,
  total: 0
})

const actionMap = { APPROVE: '通过', REJECT: '驳回', TRANSFER: '转审' }
function actionLabel(a) { return actionMap[a] || a }
function actionColor(a) {
  const map = { APPROVE: 'success', REJECT: 'danger', TRANSFER: 'warning' }
  return map[a] || 'info'
}

async function fetchData() {
  loading.value = true
  try {
    const params = { page: pagination.page, pageSize: pagination.pageSize }
    if (searchForm.targetType) params.targetType = searchForm.targetType
    if (searchForm.action) params.action = searchForm.action

    const res = await getApprovalHistory(params)
    if (res.code === 200 && res.data) {
      tableData.value = res.data.records || []
      pagination.total = res.data.total || 0
    }
  } catch {
    ElMessage.error('获取审批历史失败')
  } finally {
    loading.value = false
  }
}

function handleSearch() {
  pagination.page = 1
  fetchData()
}

function handleReset() {
  searchForm.targetType = ''
  searchForm.action = ''
  handleSearch()
}

function goToDetail(row) {
  if (row.targetType === 'PLAN') {
    router.push(`/plans/${row.targetId}`)
  } else {
    router.push(`/achievements/${row.targetId}`)
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
.search-bar {
  background: #fff;
  padding: 16px 16px 0;
  border-radius: 4px;
  margin-bottom: 16px;
}
.pagination-wrap {
  display: flex;
  justify-content: flex-end;
  margin-top: 16px;
}
</style>

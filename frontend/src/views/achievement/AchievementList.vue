<template>
  <div class="page-container">
    <div class="page-header">
      <h2>成果列表</h2>
    </div>

    <!-- Search Bar -->
    <div class="search-bar">
      <el-form :model="searchForm" inline>
        <el-form-item label="状态">
          <el-select v-model="searchForm.status" placeholder="全部" clearable style="width: 140px">
            <el-option label="待填写" value="PENDING" />
            <el-option label="已提交" value="SUBMITTED" />
            <el-option label="验收中" value="APPROVING" />
            <el-option label="已通过" value="APPROVED" />
            <el-option label="待修改" value="REJECTED" />
          </el-select>
        </el-form-item>
        <el-form-item label="计划类型">
          <el-select v-model="searchForm.planType" placeholder="全部" clearable style="width: 140px">
            <el-option label="日报" value="DAILY" />
            <el-option label="周报" value="WEEKLY" />
            <el-option label="月报" value="MONTHLY" />
          </el-select>
        </el-form-item>
        <el-form-item label="关键字">
          <el-input v-model="searchForm.keyword" placeholder="搜索计划标题" clearable style="width: 220px" @keyup.enter="handleSearch" />
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

    <!-- Approved Plans Without Achievement -->
    <el-card v-if="approvedPlans.length > 0" shadow="never" class="approved-plans-card">
      <template #header>
        <div class="card-header">
          <span class="card-title">✅ 可提交成果的计划</span>
          <el-tag type="success" size="small">{{ approvedPlans.length }} 个</el-tag>
        </div>
      </template>
      <div class="approved-plan-list">
        <div v-for="p in approvedPlans" :key="p.id" class="approved-plan-item">
          <div class="plan-info">
            <el-tag :type="planTypeColor(p.planType)" size="small">{{ planTypeLabel(p.planType) }}</el-tag>
            <span class="plan-title">{{ p.title }}</span>
            <el-tag :type="priorityColor(p.priority)" size="small">{{ priorityLabel(p.priority) }}</el-tag>
          </div>
          <el-button type="success" size="small" @click="$router.push(`/achievements/submit/${p.id}`)">
            提交成果
          </el-button>
        </div>
      </div>
    </el-card>

    <!-- Table -->
    <el-table v-loading="loading" :data="tableData" stripe border style="width: 100%">
      <el-table-column prop="planTitle" label="关联计划" min-width="160" show-overflow-tooltip />
      <el-table-column prop="planType" label="计划类型" width="80">
        <template #default="{ row }">
          <el-tag :type="planTypeColor(row.planType)" size="small">
            {{ planTypeLabel(row.planType) }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="status" label="状态" width="90">
        <template #default="{ row }">
          <el-tag :type="achievementStatusColor(row.status)" size="small">
            {{ achievementStatusLabel(row.status) }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="actualQty" label="实际完成" min-width="120" show-overflow-tooltip />
      <el-table-column prop="actualHours" label="耗时(小时)" width="100" />
      <el-table-column prop="submittedAt" label="提交时间" width="160" />
      <el-table-column label="操作" width="240" fixed="right">
        <template #default="{ row }">
          <el-button link type="primary" size="small" @click="$router.push(`/achievements/${row.id}`)">
            查看
          </el-button>
          <el-button
            v-if="canEdit(row.status)"
            link
            type="warning"
            size="small"
            @click="$router.push(`/achievements/submit/${row.planId}?edit=${row.id}`)"
          >
            编辑
          </el-button>
          <el-button
            v-if="canSubmit(row.status)"
            link
            type="success"
            size="small"
            @click="handleSubmit(row)"
          >
            提交
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
import { ElMessage } from 'element-plus'
import { Search } from '@element-plus/icons-vue'
import { getAchievementList, submitAchievement } from '@/api/achievement'
import { getApprovedPlansWithoutAchievement } from '@/api/plan'

const loading = ref(false)
const tableData = ref([])
const approvedPlans = ref([])

const searchForm = reactive({
  status: '',
  planType: '',
  keyword: ''
})

const pagination = reactive({
  page: 1,
  pageSize: 20,
  total: 0
})

const planTypeMap = { DAILY: '日报', WEEKLY: '周报', MONTHLY: '月报' }
const achievementStatusMap = { PENDING: '待填写', SUBMITTED: '已提交', APPROVING: '验收中', APPROVED: '已通过', REJECTED: '待修改' }
const priorityMap = { HIGH: '高', MEDIUM: '中', LOW: '低' }

function planTypeLabel(t) { return planTypeMap[t] || t }
function planTypeColor(t) { return t === 'DAILY' ? '' : t === 'WEEKLY' ? 'warning' : 'info' }
function priorityLabel(p) { return priorityMap[p] || p }
function priorityColor(p) {
  const map = { HIGH: 'danger', MEDIUM: 'warning', LOW: '' }
  return map[p] || 'info'
}
function achievementStatusLabel(s) { return achievementStatusMap[s] || s }
function achievementStatusColor(s) {
  const map = { PENDING: 'info', SUBMITTED: '', APPROVING: 'warning', APPROVED: 'success', REJECTED: 'danger' }
  return map[s] || 'info'
}

function canEdit(status) { return status === 'PENDING' || status === 'REJECTED' }
function canSubmit(status) { return status === 'PENDING' || status === 'REJECTED' }

async function fetchData() {
  loading.value = true
  try {
    const params = { page: pagination.page, pageSize: pagination.pageSize }
    if (searchForm.status) params.status = searchForm.status
    if (searchForm.planType) params.planType = searchForm.planType
    if (searchForm.keyword) params.keyword = searchForm.keyword

    const res = await getAchievementList(params)
    if (res.code === 200 && res.data) {
      tableData.value = res.data.records || []
      pagination.total = res.data.total || 0
    }
  } catch {
    ElMessage.error('获取成果列表失败')
  } finally {
    loading.value = false
  }
}

function handleSearch() {
  pagination.page = 1
  fetchData()
}

function handleReset() {
  searchForm.status = ''
  searchForm.planType = ''
  searchForm.keyword = ''
  handleSearch()
}

async function handleSubmit(row) {
  try {
    await submitAchievement(row.id)
    ElMessage.success('提交成功')
    fetchData()
  } catch {
    ElMessage.error('提交失败')
  }
}

async function loadApprovedPlans() {
  try {
    const res = await getApprovedPlansWithoutAchievement()
    if (res.code === 200 && res.data) {
      approvedPlans.value = res.data || []
    }
  } catch { /* ignore */ }
}

onMounted(() => {
  fetchData()
  loadApprovedPlans()
})
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

.approved-plans-card {
  margin-bottom: 16px;
}

.approved-plans-card .card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.approved-plans-card .card-title {
  font-weight: 600;
  font-size: 15px;
}

.approved-plan-list {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.approved-plan-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 10px 12px;
  background: #f0f9eb;
  border-radius: 6px;
  border: 1px solid #e1f3d8;
}

.approved-plan-item .plan-info {
  display: flex;
  align-items: center;
  gap: 10px;
}

.approved-plan-item .plan-title {
  font-weight: 500;
  color: #303133;
  max-width: 400px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}
</style>

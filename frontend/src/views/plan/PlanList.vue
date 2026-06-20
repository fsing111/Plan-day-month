<template>
  <div class="page-container">
    <div class="page-header">
      <h2>计划列表</h2>
      <el-button type="primary" @click="$router.push('/plans/create')">
        <el-icon><Plus /></el-icon>
        新建计划
      </el-button>
    </div>

    <!-- 搜索栏 -->
    <div class="search-bar">
      <el-form :model="searchForm" inline>
        <el-form-item label="计划类型">
          <el-select v-model="searchForm.planType" placeholder="全部" clearable style="width: 140px">
            <el-option label="日报" value="DAILY" />
            <el-option label="周报" value="WEEKLY" />
            <el-option label="月报" value="MONTHLY" />
          </el-select>
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="searchForm.status" placeholder="全部" clearable style="width: 140px">
            <el-option label="草稿" value="DRAFT" />
            <el-option label="已提交" value="SUBMITTED" />
            <el-option label="审批中" value="APPROVING" />
            <el-option label="已通过" value="APPROVED" />
            <el-option label="已驳回" value="REJECTED" />
          </el-select>
        </el-form-item>
        <el-form-item label="关键字">
          <el-input v-model="searchForm.keyword" placeholder="搜索标题" clearable style="width: 200px" @keyup.enter="handleSearch" />
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

    <!-- 表格 -->
    <el-table v-loading="loading" :data="tableData" stripe border style="width: 100%">
      <el-table-column prop="title" label="标题" min-width="180" show-overflow-tooltip />
      <el-table-column prop="planType" label="类型" width="80">
        <template #default="{ row }">
          <el-tag :type="planTypeColor(row.planType)" size="small">
            {{ planTypeLabel(row.planType) }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="priority" label="优先级" width="80">
        <template #default="{ row }">
          <el-tag :type="priorityColor(row.priority)" size="small">
            {{ priorityLabel(row.priority) }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="status" label="状态" width="90">
        <template #default="{ row }">
          <el-tag :type="statusColor(row.status)" size="small">
            {{ statusLabel(row.status) }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="startTime" label="开始时间" width="160" />
      <el-table-column prop="endTime" label="截止时间" width="160" />
      <el-table-column prop="userName" label="创建人" width="100" />
      <el-table-column prop="createdAt" label="创建时间" width="160" />
      <el-table-column label="操作" width="220" fixed="right">
        <template #default="{ row }">
          <el-button link type="primary" size="small" @click="$router.push(`/plans/${row.id}`)">
            查看
          </el-button>
          <el-button
            link
            type="warning"
            size="small"
            :disabled="!canEdit(row.status)"
            @click="$router.push(`/plans/create?edit=${row.id}`)"
          >
            编辑
          </el-button>
          <el-button
            link
            type="success"
            size="small"
            :disabled="!canSubmit(row.status)"
            @click="handleSubmit(row)"
          >
            提交
          </el-button>
          <el-popconfirm
            title="确定删除该计划吗？"
            confirm-button-text="确定"
            cancel-button-text="取消"
            @confirm="handleDelete(row.id)"
          >
            <template #reference>
              <el-button link type="danger" size="small" :disabled="!canDelete(row.status)">
                删除
              </el-button>
            </template>
          </el-popconfirm>
        </template>
      </el-table-column>
    </el-table>

    <!-- 分页 -->
    <div class="pagination-wrap">
      <el-pagination
        v-model:current-page="pagination.page"
        v-model:page-size="pagination.pageSize"
        :page-sizes="[10, 20, 50]"
        :total="pagination.total"
        layout="total, sizes, prev, pager, next, jumper"
        @size-change="fetchPlans"
        @current-change="fetchPlans"
      />
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { Plus, Search } from '@element-plus/icons-vue'
import { getPlanList, deletePlan, submitPlan } from '@/api/plan'

const loading = ref(false)
const tableData = ref([])

const searchForm = reactive({
  planType: '',
  status: '',
  keyword: ''
})

const pagination = reactive({
  page: 1,
  pageSize: 20,
  total: 0
})

const planTypeMap = { DAILY: '日报', WEEKLY: '周报', MONTHLY: '月报' }
const statusMap = { DRAFT: '草稿', SUBMITTED: '已提交', APPROVING: '审批中', APPROVED: '已通过', REJECTED: '已驳回' }
const priorityMap = { HIGH: '高', MEDIUM: '中', LOW: '低' }

function planTypeLabel(type) { return planTypeMap[type] || type }
function planTypeColor(type) { return type === 'DAILY' ? '' : type === 'WEEKLY' ? 'warning' : 'info' }
function statusLabel(status) { return statusMap[status] || status }
function statusColor(status) {
  const map = { DRAFT: 'info', SUBMITTED: '', APPROVING: 'warning', APPROVED: 'success', REJECTED: 'danger' }
  return map[status] || 'info'
}
function priorityLabel(p) { return priorityMap[p] || p }
function priorityColor(p) {
  const map = { HIGH: 'danger', MEDIUM: 'warning', LOW: '' }
  return map[p] || 'info'
}

function canEdit(status) { return status === 'DRAFT' || status === 'REJECTED' }
function canSubmit(status) { return status === 'DRAFT' || status === 'REJECTED' }
function canDelete(status) { return status === 'DRAFT' }

async function fetchPlans() {
  loading.value = true
  try {
    const params = { page: pagination.page, pageSize: pagination.pageSize }
    if (searchForm.planType) params.planType = searchForm.planType
    if (searchForm.status) params.status = searchForm.status
    if (searchForm.keyword) params.keyword = searchForm.keyword

    const res = await getPlanList(params)
    if (res.code === 200 && res.data) {
      tableData.value = res.data.records || []
      pagination.total = res.data.total || 0
    }
  } catch {
    ElMessage.error('获取计划列表失败')
  } finally {
    loading.value = false
  }
}

function handleSearch() {
  pagination.page = 1
  fetchPlans()
}

function handleReset() {
  searchForm.planType = ''
  searchForm.status = ''
  searchForm.keyword = ''
  handleSearch()
}

async function handleSubmit(row) {
  try {
    await submitPlan(row.id)
    ElMessage.success('提交成功')
    fetchPlans()
  } catch {
    ElMessage.error('提交失败')
  }
}

async function handleDelete(id) {
  try {
    await deletePlan(id)
    ElMessage.success('删除成功')
    fetchPlans()
  } catch {
    ElMessage.error('删除失败')
  }
}

onMounted(() => { fetchPlans() })
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

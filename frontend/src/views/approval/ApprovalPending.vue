<template>
  <div class="page-container">
    <div class="page-header">
      <h2>待审批</h2>
    </div>

    <!-- Tabs: Plan Approval / Achievement Approval -->
    <el-tabs v-model="activeTab" @tab-change="handleTabChange">
      <el-tab-pane label="计划审批" name="PLAN" />
      <el-tab-pane label="成果验收" name="ACHIEVEMENT" />
    </el-tabs>

    <!-- Pending List -->
    <el-table v-loading="loading" :data="tableData" stripe border style="width: 100%">
      <el-table-column prop="submitterName" label="提交人" width="100" />
      <el-table-column prop="title" label="标题" min-width="180" show-overflow-tooltip />
      <el-table-column label="类型" width="80">
        <template #default="{ row }">
          <el-tag :type="row.planType ? '' : 'info'" size="small">
            {{ row.planType ? planTypeLabel(row.planType) : '成果' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="priority" label="优先级" width="80">
        <template #default="{ row }">
          <el-tag v-if="row.priority" :type="priorityColor(row.priority)" size="small">
            {{ priorityLabel(row.priority) }}
          </el-tag>
          <span v-else>-</span>
        </template>
      </el-table-column>
      <el-table-column label="当前级别" width="90">
        <template #default="{ row }">
          <span>第 {{ row.approvalLevel }} / {{ row.totalLevels }} 级</span>
        </template>
      </el-table-column>
      <el-table-column label="同级审批状态" width="160">
        <template #default="{ row }">
          <div v-if="row.peerApprovals && row.peerApprovals.length > 0" class="peer-status">
            <div v-for="(peer, idx) in row.peerApprovals" :key="idx" class="peer-tag">
              <span>{{ peer.approverName }}：</span>
              <el-tag
                :type="peer.action === 'APPROVE' ? 'success' : peer.action === 'REJECT' ? 'danger' : 'warning'"
                size="small"
              >
                {{ peer.action === 'APPROVE' ? '通过' : peer.action === 'REJECT' ? '驳回' : '转审' }}
              </el-tag>
            </div>
          </div>
          <span v-else class="no-peer">待审批</span>
        </template>
      </el-table-column>
      <el-table-column prop="submittedAt" label="提交时间" width="160" />
      <el-table-column label="操作" width="100" fixed="right">
        <template #default="{ row }">
          <el-button type="primary" size="small" @click="openApprovalDialog(row)">
            审批
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

    <!-- Approval Dialog -->
    <ApprovalDialog
      v-model="dialogVisible"
      :record="selectedRecord"
      @done="fetchData"
    />
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { getPendingApprovals } from '@/api/approval'
import ApprovalDialog from '@/components/approval/ApprovalDialog.vue'

const loading = ref(false)
const tableData = ref([])
const activeTab = ref('PLAN')
const dialogVisible = ref(false)
const selectedRecord = ref(null)

const pagination = reactive({
  page: 1,
  pageSize: 20,
  total: 0
})

const planTypeMap = { DAILY: '日报', WEEKLY: '周报', MONTHLY: '月报' }
const priorityMap = { HIGH: '高', MEDIUM: '中', LOW: '低' }

function planTypeLabel(t) { return planTypeMap[t] || t }
function priorityLabel(p) { return priorityMap[p] || p }
function priorityColor(p) {
  const map = { HIGH: 'danger', MEDIUM: 'warning', LOW: '' }
  return map[p] || 'info'
}

async function fetchData() {
  loading.value = true
  try {
    const params = {
      page: pagination.page,
      pageSize: pagination.pageSize,
      targetType: activeTab.value
    }
    const res = await getPendingApprovals(params)
    if (res.code === 200 && res.data) {
      tableData.value = res.data.records || []
      pagination.total = res.data.total || 0
    }
  } catch {
    ElMessage.error('获取待审批列表失败')
  } finally {
    loading.value = false
  }
}

function handleTabChange() {
  pagination.page = 1
  fetchData()
}

function openApprovalDialog(record) {
  selectedRecord.value = record
  dialogVisible.value = true
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
.pagination-wrap {
  display: flex;
  justify-content: flex-end;
  margin-top: 16px;
}
.peer-status {
  display: flex;
  flex-direction: column;
  gap: 2px;
}
.peer-tag {
  font-size: 12px;
  display: flex;
  align-items: center;
  gap: 4px;
}
.no-peer {
  color: #909399;
  font-size: 12px;
}
</style>

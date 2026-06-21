<template>
  <div class="page-container">
    <div class="page-header">
      <h2>审批链配置</h2>
      <el-button type="primary" @click="openCreateDialog">
        <el-icon><Plus /></el-icon>
        新增配置
      </el-button>
    </div>

    <!-- Filter -->
    <div class="search-bar">
      <el-form :model="filterForm" inline>
        <el-form-item label="部门">
          <el-select v-model="filterForm.deptId" placeholder="全部" clearable style="width: 160px" @change="fetchData">
            <el-option v-for="d in deptOptions" :key="d.id" :label="d.name" :value="d.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="适用类型">
          <el-select v-model="filterForm.planType" placeholder="全部" clearable style="width: 140px" @change="fetchData">
            <el-option label="日报" value="DAILY" />
            <el-option label="周报" value="WEEKLY" />
            <el-option label="月报" value="MONTHLY" />
            <el-option label="成果验收" value="ACHIEVEMENT" />
          </el-select>
        </el-form-item>
      </el-form>
    </div>

    <!-- Table -->
    <el-table v-loading="loading" :data="tableData" stripe border style="width: 100%">
      <el-table-column prop="deptName" label="部门" width="120" />
      <el-table-column label="适用类型" width="100">
        <template #default="{ row }">
          <el-tag :type="planTypeColor(row.planType)" size="small">
            {{ planTypeLabel(row.planType) }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="approvalLevel" label="审批级别" width="80" align="center">
        <template #default="{ row }">
          第{{ row.approvalLevel }}级
        </template>
      </el-table-column>
      <el-table-column prop="approverName" label="审批人" width="120" />
      <el-table-column prop="sortOrder" label="顺序" width="70" align="center" />
      <el-table-column prop="createdAt" label="创建时间" width="160" />
      <el-table-column label="操作" width="140" fixed="right">
        <template #default="{ row }">
          <el-button link type="warning" size="small" @click="openEditDialog(row)">编辑</el-button>
          <el-popconfirm title="确定删除？" @confirm="handleDelete(row.id)">
            <template #reference>
              <el-button link type="danger" size="small">删除</el-button>
            </template>
          </el-popconfirm>
        </template>
      </el-table-column>
    </el-table>

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

    <!-- Dialog -->
    <el-dialog
      v-model="dialogVisible"
      :title="dialogTitle"
      width="480px"
      :close-on-click-modal="false"
    >
      <el-form ref="formRef" :model="form" :rules="rules" label-width="90px">
        <el-form-item label="部门" prop="deptId">
          <el-select v-model="form.deptId" placeholder="请选择" style="width:100%">
            <el-option v-for="d in deptOptions" :key="d.id" :label="d.name" :value="d.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="适用类型" prop="planType">
          <el-select v-model="form.planType" placeholder="请选择" style="width:100%">
            <el-option label="日报" value="DAILY" />
            <el-option label="周报" value="WEEKLY" />
            <el-option label="月报" value="MONTHLY" />
            <el-option label="成果验收" value="ACHIEVEMENT" />
          </el-select>
        </el-form-item>
        <el-form-item label="审批级别" prop="approvalLevel">
          <el-input-number v-model="form.approvalLevel" :min="1" :max="5" style="width:100%" />
        </el-form-item>
        <el-form-item label="审批人" prop="approverId">
          <el-select v-model="form.approverId" placeholder="请选择" filterable style="width:100%">
            <el-option
              v-for="u in userOptions"
              :key="u.id"
              :label="`${u.realName} (${u.username})`"
              :value="u.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="排序">
          <el-input-number v-model="form.sortOrder" :min="0" style="width:100%" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="saving" @click="handleSave">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { useRouter } from 'vue-router'
import { Plus } from '@element-plus/icons-vue'
import { useUserStore } from '@/stores/user'
import { getApprovalChains, createApprovalChain, updateApprovalChain, deleteApprovalChain, getDepartmentList, getUserList } from '@/api/admin'

const router = useRouter()
const userStore = useUserStore()
const loading = ref(false)
const saving = ref(false)
const dialogVisible = ref(false)
const tableData = ref([])
const deptOptions = ref([])
const userOptions = ref([])
const editingRecord = ref(null)
const formRef = ref(null)

const isEdit = computed(() => !!editingRecord.value)
const dialogTitle = computed(() => isEdit.value ? '编辑审批链' : '新增审批链')

const filterForm = reactive({
  deptId: '',
  planType: ''
})

const pagination = reactive({
  page: 1,
  pageSize: 20,
  total: 0
})

const form = reactive({
  deptId: null,
  planType: 'DAILY',
  approvalLevel: 1,
  approverId: null,
  sortOrder: 0
})

const rules = {
  deptId: [{ required: true, message: '请选择部门', trigger: 'change' }],
  planType: [{ required: true, message: '请选择类型', trigger: 'change' }],
  approvalLevel: [{ required: true, message: '请输入审批级别', trigger: 'blur' }],
  approverId: [{ required: true, message: '请选择审批人', trigger: 'change' }]
}

const planTypeMap = { DAILY: '日报', WEEKLY: '周报', MONTHLY: '月报', ACHIEVEMENT: '成果验收' }
function planTypeLabel(t) { return planTypeMap[t] || t }
function planTypeColor(t) {
  const map = { DAILY: '', WEEKLY: 'warning', MONTHLY: 'info', ACHIEVEMENT: 'success' }
  return map[t] || ''
}

async function fetchData() {
  loading.value = true
  try {
    const params = { page: pagination.page, pageSize: pagination.pageSize }
    if (filterForm.deptId) params.deptId = filterForm.deptId
    if (filterForm.planType) params.planType = filterForm.planType

    const res = await getApprovalChains(params)
    if (res.code === 200 && res.data) {
      tableData.value = res.data || []
      pagination.total = (res.data || []).length
    }
  } catch {
    ElMessage.error('获取审批链配置失败')
  } finally {
    loading.value = false
  }
}

async function loadOptions() {
  try {
    const [deptRes, userRes] = await Promise.all([
      getDepartmentList(),
      getUserList({ pageSize: 100 })
    ])
    if (deptRes.code === 200) deptOptions.value = deptRes.data || []
    if (userRes.code === 200) userOptions.value = (userRes.data.records || []).filter(u => u.role !== 'EMPLOYEE')
  } catch { /* ignore */ }
}

function openCreateDialog() {
  editingRecord.value = null
  form.deptId = null
  form.planType = 'DAILY'
  form.approvalLevel = 1
  form.approverId = null
  form.sortOrder = 0
  dialogVisible.value = true
}

function openEditDialog(row) {
  editingRecord.value = row
  form.deptId = row.deptId
  form.planType = row.planType
  form.approvalLevel = row.approvalLevel
  form.approverId = row.approverId
  form.sortOrder = row.sortOrder || 0
  dialogVisible.value = true
}

async function handleSave() {
  const valid = await formRef.value?.validate().catch(() => false)
  if (!valid) return

  saving.value = true
  try {
    const data = {
      deptId: form.deptId,
      planType: form.planType,
      approvalLevel: form.approvalLevel,
      approverId: form.approverId,
      sortOrder: form.sortOrder
    }

    if (isEdit.value) {
      await updateApprovalChain(editingRecord.value.id, data)
      ElMessage.success('修改成功')
    } else {
      await createApprovalChain(data)
      ElMessage.success('创建成功')
    }
    dialogVisible.value = false
    fetchData()
  } catch (error) {
    ElMessage.error(error?.message || '操作失败')
  } finally {
    saving.value = false
  }
}

async function handleDelete(id) {
  try {
    await deleteApprovalChain(id)
    ElMessage.success('删除成功')
    fetchData()
  } catch {
    ElMessage.error('删除失败')
  }
}

onMounted(() => {
  if (!userStore.isLeader) {
    ElMessage.warning('没有权限访问该页面')
    router.replace('/dashboard')
    return
  }
  fetchData()
  loadOptions()
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
</style>

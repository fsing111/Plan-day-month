<template>
  <div class="page-container">
    <div class="page-header">
      <h2>用户管理</h2>
      <el-button type="primary" @click="openCreateDialog">
        <el-icon><Plus /></el-icon>
        新增用户
      </el-button>
    </div>

    <!-- Search -->
    <div class="search-bar">
      <el-form :model="searchForm" inline>
        <el-form-item label="关键字">
          <el-input v-model="searchForm.keyword" placeholder="用户名或姓名" clearable style="width: 220px" @keyup.enter="handleSearch" />
        </el-form-item>
        <el-form-item label="角色">
          <el-select v-model="searchForm.role" placeholder="全部" clearable style="width: 120px">
            <el-option label="员工" value="EMPLOYEE" />
            <el-option label="领导" value="LEADER" />
          </el-select>
        </el-form-item>
        <el-form-item label="部门">
          <el-select v-model="searchForm.deptId" placeholder="全部" clearable style="width: 160px">
            <el-option v-for="d in deptOptions" :key="d.id" :label="d.name" :value="d.id" />
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
      <el-table-column prop="username" label="用户名" width="120" />
      <el-table-column prop="realName" label="姓名" width="100" />
      <el-table-column label="角色" width="90">
        <template #default="{ row }">
          <el-tag :type="roleColor(row.role)" size="small">
            {{ roleLabel(row.role) }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="deptName" label="部门" width="120" />
      <el-table-column prop="leaderName" label="直属领导" width="100" />
      <el-table-column label="状态" width="80">
        <template #default="{ row }">
          <el-tag :type="row.enabled ? 'success' : 'danger'" size="small">
            {{ row.enabled ? '启用' : '禁用' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="email" label="邮箱" width="180" show-overflow-tooltip />
      <el-table-column prop="createdAt" label="创建时间" width="160" />
      <el-table-column label="操作" width="220" fixed="right">
        <template #default="{ row }">
          <el-button link type="primary" size="small" @click="openEditDialog(row)">编辑</el-button>
          <el-button
            link
            :type="row.enabled ? 'warning' : 'success'"
            size="small"
            @click="handleToggleDisable(row)"
          >
            {{ row.enabled ? '禁用' : '启用' }}
          </el-button>
          <el-popconfirm
            title="确定重置该用户密码吗？"
            @confirm="handleResetPassword(row)"
          >
            <template #reference>
              <el-button link type="danger" size="small">重置密码</el-button>
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

    <!-- Create/Edit Dialog -->
    <el-dialog
      v-model="dialogVisible"
      :title="dialogTitle"
      width="520px"
      :close-on-click-modal="false"
    >
      <el-form ref="formRef" :model="form" :rules="formRules" label-width="90px">
        <el-form-item label="用户名" prop="username">
          <el-input v-model="form.username" :disabled="isEdit" placeholder="登录用户名" maxlength="50" />
        </el-form-item>
        <el-form-item v-if="!isEdit" label="密码" prop="password">
          <el-input v-model="form.password" type="password" placeholder="初始密码" show-password />
        </el-form-item>
        <el-form-item label="姓名" prop="realName">
          <el-input v-model="form.realName" placeholder="真实姓名" maxlength="50" />
        </el-form-item>
        <el-form-item label="角色" prop="role">
          <el-select v-model="form.role" placeholder="请选择" style="width:100%">
            <el-option label="员工" value="EMPLOYEE" />
            <el-option label="领导" value="LEADER" />
          </el-select>
        </el-form-item>
        <el-form-item label="部门" prop="deptId">
          <el-select v-model="form.deptId" placeholder="请选择" style="width:100%">
            <el-option v-for="d in deptOptions" :key="d.id" :label="d.name" :value="d.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="直属领导">
          <el-select v-model="form.leaderId" placeholder="请选择" clearable style="width:100%">
            <el-option
              v-for="u in leaderOptions"
              :key="u.id"
              :label="`${u.realName} (${u.username})`"
              :value="u.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="邮箱">
          <el-input v-model="form.email" placeholder="邮箱地址" />
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
import { Plus, Search } from '@element-plus/icons-vue'
import { useUserStore } from '@/stores/user'
import { getUserList, createUser, updateUser, disableUser, resetPassword } from '@/api/admin'
import { getDepartmentList } from '@/api/admin'

const router = useRouter()
const userStore = useUserStore()
const loading = ref(false)
const tableData = ref([])
const dialogVisible = ref(false)
const saving = ref(false)
const editingUser = ref(null)
const deptOptions = ref([])
const leaderOptions = ref([])
const formRef = ref(null)

const isEdit = computed(() => !!editingUser.value)
const dialogTitle = computed(() => isEdit.value ? '编辑用户' : '新增用户')

const searchForm = reactive({
  keyword: '',
  role: '',
  deptId: ''
})

const pagination = reactive({
  page: 1,
  pageSize: 20,
  total: 0
})

const form = reactive({
  username: '',
  password: '',
  realName: '',
  role: 'EMPLOYEE',
  deptId: null,
  leaderId: null,
  email: ''
})

const formRules = {
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  realName: [{ required: true, message: '请输入姓名', trigger: 'blur' }],
  role: [{ required: true, message: '请选择角色', trigger: 'change' }],
  deptId: [{ required: true, message: '请选择部门', trigger: 'change' }]
}

const roleMap = { EMPLOYEE: '员工', LEADER: '领导' }
function roleLabel(r) { return roleMap[r] || r }
function roleColor(r) {
  const map = { EMPLOYEE: '', LEADER: 'warning' }
  return map[r] || 'info'
}

async function fetchData() {
  loading.value = true
  try {
    const params = { page: pagination.page, pageSize: pagination.pageSize }
    if (searchForm.keyword) params.keyword = searchForm.keyword
    if (searchForm.role) params.role = searchForm.role
    if (searchForm.deptId) params.deptId = searchForm.deptId

    const res = await getUserList(params)
    if (res.code === 200 && res.data) {
      tableData.value = res.data.records || []
      pagination.total = res.data.total || 0
    }
  } catch {
    ElMessage.error('获取用户列表失败')
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
    if (userRes.code === 200) leaderOptions.value = (userRes.data.records || []).filter(u => u.role !== 'EMPLOYEE')
  } catch { /* ignore */ }
}

function handleSearch() {
  pagination.page = 1
  fetchData()
}

function handleReset() {
  searchForm.keyword = ''
  searchForm.role = ''
  searchForm.deptId = ''
  handleSearch()
}

function openCreateDialog() {
  editingUser.value = null
  form.username = ''
  form.password = ''
  form.realName = ''
  form.role = 'EMPLOYEE'
  form.deptId = null
  form.leaderId = null
  form.email = ''
  dialogVisible.value = true
}

function openEditDialog(row) {
  editingUser.value = row
  form.username = row.username
  form.password = ''
  form.realName = row.realName
  form.role = row.role
  form.deptId = row.deptId
  form.leaderId = row.leaderId
  form.email = row.email || ''
  dialogVisible.value = true
}

async function handleSave() {
  const valid = await formRef.value?.validate().catch(() => false)
  if (!valid) return

  saving.value = true
  try {
    const data = {
      realName: form.realName,
      role: form.role,
      deptId: form.deptId,
      leaderId: form.leaderId,
      email: form.email
    }

    if (isEdit.value) {
      await updateUser(editingUser.value.id, data)
      ElMessage.success('修改成功')
    } else {
      await createUser({ ...data, username: form.username, password: form.password })
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

async function handleToggleDisable(row) {
  try {
    await disableUser(row.id)
    row.enabled = row.enabled ? 0 : 1
    ElMessage.success(row.enabled ? '已启用' : '已禁用')
    fetchData()
  } catch {
    ElMessage.error('操作失败')
  }
}

async function handleResetPassword(row) {
  try {
    await resetPassword(row.id, { newPassword: '123456' })
    ElMessage.success('密码已重置为默认密码')
  } catch {
    ElMessage.error('操作失败')
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

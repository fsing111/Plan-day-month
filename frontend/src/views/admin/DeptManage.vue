<template>
  <div class="page-container">
    <div class="page-header">
      <h2>部门管理</h2>
      <el-button type="primary" @click="openCreateDialog(null)">
        <el-icon><Plus /></el-icon>
        新增部门
      </el-button>
    </div>

    <!-- Tree Table -->
    <el-table
      v-loading="loading"
      :data="tableData"
      row-key="id"
      stripe
      border
      style="width: 100%"
      default-expand-all
    >
      <el-table-column prop="name" label="部门名称" min-width="200" />
      <el-table-column prop="sortOrder" label="排序号" width="100" align="center" />
      <el-table-column prop="createdAt" label="创建时间" width="180" />
      <el-table-column label="操作" width="200" fixed="right">
        <template #default="{ row }">
          <el-button link type="primary" size="small" @click="openCreateDialog(row)">
            添加子部门
          </el-button>
          <el-button link type="warning" size="small" @click="openEditDialog(row)">
            编辑
          </el-button>
          <el-popconfirm
            title="确定删除该部门吗？如有子部门也将一并删除"
            @confirm="handleDelete(row.id)"
          >
            <template #reference>
              <el-button link type="danger" size="small">删除</el-button>
            </template>
          </el-popconfirm>
        </template>
      </el-table-column>
    </el-table>

    <!-- Dialog -->
    <el-dialog
      v-model="dialogVisible"
      :title="dialogTitle"
      width="480px"
      :close-on-click-modal="false"
    >
      <el-form ref="formRef" :model="form" :rules="rules" label-width="90px">
        <el-form-item label="部门名称" prop="name">
          <el-input v-model="form.name" placeholder="请输入部门名称" maxlength="100" />
        </el-form-item>
        <el-form-item label="上级部门">
          <el-input :model-value="parentDeptName" disabled />
        </el-form-item>
        <el-form-item label="排序号">
          <el-input-number v-model="form.sortOrder" :min="0" :step="1" style="width:100%" />
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
import { getDepartmentList, createDepartment, updateDepartment } from '@/api/admin'

const router = useRouter()
const userStore = useUserStore()
const loading = ref(false)
const saving = ref(false)
const dialogVisible = ref(false)
const tableData = ref([])
const allDepts = ref([])
const parentDept = ref(null)
const editingDept = ref(null)
const formRef = ref(null)

const isEdit = computed(() => !!editingDept.value)
const dialogTitle = computed(() => isEdit.value ? '编辑部门' : '新增部门')
const parentDeptName = computed(() => {
  if (isEdit.value && editingDept.value?.parentId) {
    const p = allDepts.value.find(d => d.id === editingDept.value.parentId)
    return p?.name || '-'
  }
  return parentDept.value?.name || '顶级部门'
})

const form = reactive({
  name: '',
  sortOrder: 0
})

const rules = {
  name: [{ required: true, message: '请输入部门名称', trigger: 'blur' }]
}

async function fetchData() {
  loading.value = true
  try {
    const res = await getDepartmentList()
    if (res.code === 200 && res.data) {
      allDepts.value = res.data || []
      tableData.value = buildTree(allDepts.value)
    }
  } catch {
    ElMessage.error('获取部门列表失败')
  } finally {
    loading.value = false
  }
}

function buildTree(depts) {
  const map = {}
  const roots = []
  depts.forEach(d => {
    map[d.id] = { ...d, children: [] }
  })
  depts.forEach(d => {
    if (d.parentId && map[d.parentId]) {
      map[d.parentId].children.push(map[d.id])
    } else {
      roots.push(map[d.id])
    }
  })
  return roots
}

function openCreateDialog(parentRow) {
  editingDept.value = null
  parentDept.value = parentRow
  form.name = ''
  form.sortOrder = 0
  dialogVisible.value = true
}

function openEditDialog(row) {
  editingDept.value = row
  form.name = row.name
  form.sortOrder = row.sortOrder || 0
  dialogVisible.value = true
}

async function handleSave() {
  const valid = await formRef.value?.validate().catch(() => false)
  if (!valid) return

  saving.value = true
  try {
    const data = {
      name: form.name,
      sortOrder: form.sortOrder
    }

    if (isEdit.value) {
      data.parentId = editingDept.value.parentId
      await updateDepartment(editingDept.value.id, data)
      ElMessage.success('修改成功')
    } else {
      data.parentId = parentDept.value?.id || null
      await createDepartment(data)
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
    // The API may not have delete endpoint for department, this depends on backend
    ElMessage.warning('删除部门功能需要在后端确认')
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
</style>

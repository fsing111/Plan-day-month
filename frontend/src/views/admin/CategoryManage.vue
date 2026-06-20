<template>
  <div class="page-container">
    <div class="page-header">
      <h2>分类管理</h2>
      <el-button type="primary" @click="openCreateDialog">
        <el-icon><Plus /></el-icon>
        新增分类
      </el-button>
    </div>

    <!-- Table -->
    <el-table v-loading="loading" :data="tableData" stripe border style="width: 100%">
      <el-table-column prop="name" label="分类名称" min-width="160" />
      <el-table-column prop="deptName" label="所属部门" width="140">
        <template #default="{ row }">
          {{ row.deptName || '全局' }}
        </template>
      </el-table-column>
      <el-table-column prop="sortOrder" label="排序号" width="80" align="center" />
      <el-table-column prop="createdAt" label="创建时间" width="180" />
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

    <!-- Dialog -->
    <el-dialog
      v-model="dialogVisible"
      :title="dialogTitle"
      width="480px"
      :close-on-click-modal="false"
    >
      <el-form ref="formRef" :model="form" :rules="rules" label-width="90px">
        <el-form-item label="分类名称" prop="name">
          <el-input v-model="form.name" placeholder="请输入分类名称" maxlength="100" />
        </el-form-item>
        <el-form-item label="所属部门">
          <el-select v-model="form.deptId" placeholder="全局（不限制部门）" clearable style="width:100%">
            <el-option v-for="d in deptOptions" :key="d.id" :label="d.name" :value="d.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="排序号">
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
import { Plus } from '@element-plus/icons-vue'
import { getCategories, createCategory, updateCategory, deleteCategory, getDepartmentList } from '@/api/admin'

const loading = ref(false)
const saving = ref(false)
const dialogVisible = ref(false)
const tableData = ref([])
const deptOptions = ref([])
const editingRecord = ref(null)
const formRef = ref(null)

const isEdit = computed(() => !!editingRecord.value)
const dialogTitle = computed(() => isEdit.value ? '编辑分类' : '新增分类')

const form = reactive({
  name: '',
  deptId: null,
  sortOrder: 0
})

const rules = {
  name: [{ required: true, message: '请输入分类名称', trigger: 'blur' }]
}

async function fetchData() {
  loading.value = true
  try {
    const res = await getCategories()
    if (res.code === 200) {
      tableData.value = res.data || []
    }
  } catch {
    ElMessage.error('获取分类列表失败')
  } finally {
    loading.value = false
  }
}

async function loadDepts() {
  try {
    const res = await getDepartmentList()
    if (res.code === 200) deptOptions.value = res.data || []
  } catch { /* ignore */ }
}

function openCreateDialog() {
  editingRecord.value = null
  form.name = ''
  form.deptId = null
  form.sortOrder = 0
  dialogVisible.value = true
}

function openEditDialog(row) {
  editingRecord.value = row
  form.name = row.name
  form.deptId = row.deptId
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
      deptId: form.deptId,
      sortOrder: form.sortOrder
    }

    if (isEdit.value) {
      await updateCategory(editingRecord.value.id, data)
      ElMessage.success('修改成功')
    } else {
      await createCategory(data)
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
    await deleteCategory(id)
    ElMessage.success('删除成功')
    fetchData()
  } catch {
    ElMessage.error('删除失败')
  }
}

onMounted(() => {
  fetchData()
  loadDepts()
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

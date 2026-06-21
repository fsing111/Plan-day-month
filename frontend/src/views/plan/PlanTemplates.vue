<template>
  <div class="page-container">
    <div class="page-header">
      <h2>我的模板</h2>
      <el-button type="primary" @click="openCreateDialog">
        <el-icon><Plus /></el-icon>
        新建模板
      </el-button>
    </div>
    <el-table v-loading="loading" :data="templates" stripe border>
      <el-table-column prop="name" label="模板名称" min-width="150" />
      <el-table-column prop="planType" label="类型" width="80">
        <template #default="{ row }">
          <el-tag :type="planTypeColor(row.planType)" size="small">{{ planTypeLabel(row.planType) }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="title" label="标题" min-width="180" />
      <el-table-column prop="createdAt" label="创建时间" width="160" />
      <el-table-column label="操作" width="180">
        <template #default="{ row }">
          <el-button link type="primary" size="small" @click="useTemplate(row)">使用</el-button>
          <el-popconfirm title="确定删除？" @confirm="handleDelete(row.id)">
            <template #reference>
              <el-button link type="danger" size="small">删除</el-button>
            </template>
          </el-popconfirm>
        </template>
      </el-table-column>
    </el-table>

    <!-- Create Dialog -->
    <el-dialog
      v-model="dialogVisible"
      :title="dialogTitle"
      width="520px"
      :close-on-click-modal="false"
    >
      <el-form ref="formRef" :model="form" :rules="rules" label-width="90px">
        <el-form-item label="模板名称" prop="name">
          <el-input v-model="form.name" placeholder="如：日常开发日报" maxlength="50" />
        </el-form-item>
        <el-form-item label="计划类型" prop="planType">
          <el-select v-model="form.planType" placeholder="请选择" style="width:100%">
            <el-option label="日报" value="DAILY" />
            <el-option label="周报" value="WEEKLY" />
            <el-option label="月报" value="MONTHLY" />
          </el-select>
        </el-form-item>
        <el-form-item label="标题模板" prop="title">
          <el-input v-model="form.title" placeholder="如：每日开发计划" maxlength="100" />
        </el-form-item>
        <el-form-item label="优先级">
          <el-radio-group v-model="form.priority">
            <el-radio value="LOW">低</el-radio>
            <el-radio value="MEDIUM">中</el-radio>
            <el-radio value="HIGH">高</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="量化指标">
          <el-input v-model="form.quantTarget" placeholder="如：完成3个功能点" maxlength="200" />
        </el-form-item>
        <el-form-item label="详细描述">
          <el-input v-model="form.description" type="textarea" :rows="3" placeholder="计划描述模板" maxlength="500" />
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
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { Plus } from '@element-plus/icons-vue'
import { getTemplates, saveTemplate, deleteTemplate } from '@/api/template'

const router = useRouter()
const loading = ref(false)
const saving = ref(false)
const dialogVisible = ref(false)
const templates = ref([])
const formRef = ref(null)

const planTypeMap = { DAILY: '日报', WEEKLY: '周报', MONTHLY: '月报' }
function planTypeLabel(t) { return planTypeMap[t] || t }
function planTypeColor(t) { return t === 'DAILY' ? '' : t === 'WEEKLY' ? 'warning' : 'info' }

const dialogTitle = '新建模板'

const form = reactive({
  name: '',
  planType: 'DAILY',
  title: '',
  priority: 'MEDIUM',
  quantTarget: '',
  description: ''
})

const rules = {
  name: [{ required: true, message: '请输入模板名称', trigger: 'blur' }],
  planType: [{ required: true, message: '请选择计划类型', trigger: 'change' }],
  title: [{ required: true, message: '请输入标题模板', trigger: 'blur' }]
}

onMounted(fetchTemplates)

async function fetchTemplates() {
  loading.value = true
  try {
    const res = await getTemplates()
    if (res.code === 200) templates.value = res.data || []
  } catch { ElMessage.error('获取模板失败') }
  finally { loading.value = false }
}

function useTemplate(row) {
  router.push(`/plans/create?templateId=${row.id}`)
}

function openCreateDialog() {
  form.name = ''
  form.planType = 'DAILY'
  form.title = ''
  form.priority = 'MEDIUM'
  form.quantTarget = ''
  form.description = ''
  dialogVisible.value = true
}

async function handleSave() {
  const valid = await formRef.value?.validate().catch(() => false)
  if (!valid) return

  saving.value = true
  try {
    await saveTemplate({
      name: form.name,
      planType: form.planType,
      title: form.title,
      priority: form.priority,
      quantTarget: form.quantTarget,
      description: form.description
    })
    ElMessage.success('模板创建成功')
    dialogVisible.value = false
    fetchTemplates()
  } catch (error) {
    ElMessage.error(error?.message || '创建失败')
  } finally {
    saving.value = false
  }
}

async function handleDelete(id) {
  try {
    await deleteTemplate(id)
    ElMessage.success('删除成功')
    fetchTemplates()
  } catch { ElMessage.error('删除失败') }
}
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

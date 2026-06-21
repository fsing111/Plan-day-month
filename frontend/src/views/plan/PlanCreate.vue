<template>
  <div class="page-container">
    <div class="page-header">
      <h2>{{ isEdit ? '编辑计划' : '新建计划' }}</h2>
    </div>

    <div class="form-card">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="100px" size="default">
        <el-row :gutter="20">
          <el-col :span="8">
            <el-form-item label="计划类型" prop="planType">
              <el-select v-model="form.planType" placeholder="请选择" style="width:100%" @change="onPlanTypeChange">
                <el-option label="日报" value="DAILY" />
                <el-option label="周报" value="WEEKLY" />
                <el-option label="月报" value="MONTHLY" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="优先级" prop="priority">
              <el-radio-group v-model="form.priority">
                <el-radio value="LOW">低</el-radio>
                <el-radio value="MEDIUM">中</el-radio>
                <el-radio value="HIGH">高</el-radio>
              </el-radio-group>
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="项目分类" prop="categoryId">
              <el-select v-model="form.categoryId" placeholder="请选择" clearable style="width:100%">
                <el-option
                  v-for="cat in categories"
                  :key="cat.id"
                  :label="cat.name"
                  :value="cat.id"
                />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>

        <el-form-item label="计划标题" prop="title">
          <el-input v-model="form.title" placeholder="请输入计划标题" maxlength="200" show-word-limit />
        </el-form-item>

        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="开始时间" prop="startTime">
              <el-date-picker
                v-model="form.startTime"
                type="datetime"
                placeholder="选择开始时间"
                format="YYYY-MM-DD HH:mm:ss"
                value-format="YYYY-MM-DDTHH:mm:ss"
                style="width:100%"
              />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="截止时间" prop="endTime">
              <el-date-picker
                v-model="form.endTime"
                type="datetime"
                placeholder="选择截止时间"
                format="YYYY-MM-DD HH:mm:ss"
                value-format="YYYY-MM-DDTHH:mm:ss"
                style="width:100%"
              />
            </el-form-item>
          </el-col>
        </el-row>

        <el-form-item label="量化指标">
          <el-input v-model="form.quantTarget" placeholder="可量化的完成指标（如：完成2个接口开发）" maxlength="200" show-word-limit />
        </el-form-item>

        <!-- 关联计划（仅周报/月报显示） -->
        <el-form-item v-if="showRefPlans" label="关联计划">
          <el-select
            v-model="form.refPlanIds"
            multiple
            placeholder="选择要关联的下级已通过计划"
            style="width:100%"
            :loading="refPlansLoading"
          >
            <el-option
              v-for="p in refPlanOptions"
              :key="p.id"
              :label="`${p.title} (${p.startTime})`"
              :value="p.id"
            />
          </el-select>
        </el-form-item>

        <el-form-item label="详细描述">
          <div class="editor-wrap">
            <QuillEditor
              v-model:content="form.description"
              contentType="html"
              theme="snow"
              toolbar="full"
              :style="{ height: '250px' }"
            />
          </div>
        </el-form-item>

        <el-form-item>
          <el-button type="primary" :loading="saving" @click="handleSave(false)">
            <el-icon><Document /></el-icon>
            保存草稿
          </el-button>
          <el-button type="success" :loading="submitting" @click="handleSave(true)">
            <el-icon><Upload /></el-icon>
            直接提交
          </el-button>
          <el-button @click="$router.back()">取消</el-button>
        </el-form-item>
      </el-form>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted, watch } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import { Document, Upload } from '@element-plus/icons-vue'
import { QuillEditor } from '@vueup/vue-quill'
import '@vueup/vue-quill/dist/vue-quill.snow.css'
import { createPlan, updatePlan, getPlanDetail, getRollupOptions, getCategories } from '@/api/plan'

const router = useRouter()
const route = useRoute()

const isEdit = computed(() => !!route.query.edit)
const editId = computed(() => route.query.edit ? Number(route.query.edit) : null)

const formRef = ref(null)
const saving = ref(false)
const submitting = ref(false)
const categories = ref([])
const refPlansLoading = ref(false)
const refPlanOptions = ref([])

const form = reactive({
  planType: 'DAILY',
  title: '',
  priority: 'MEDIUM',
  startTime: '',
  endTime: '',
  categoryId: null,
  quantTarget: '',
  description: '',
  refPlanIds: []
})

const rules = {
  planType: [{ required: true, message: '请选择计划类型', trigger: 'change' }],
  title: [{ required: true, message: '请输入计划标题', trigger: 'blur' }],
  priority: [{ required: true, message: '请选择优先级', trigger: 'change' }],
  startTime: [{ required: true, message: '请选择开始时间', trigger: 'change' }],
  endTime: [{ required: true, message: '请选择截止时间', trigger: 'change' }]
}

const showRefPlans = computed(() => form.planType === 'WEEKLY' || form.planType === 'MONTHLY')

function onPlanTypeChange() {
  form.refPlanIds = []
  if (showRefPlans.value) {
    loadRefPlanOptions()
  }
}

async function loadCategories() {
  try {
    const res = await getCategories()
    if (res.code === 200) {
      categories.value = res.data || []
    }
  } catch {
    // categories are optional, ignore error
  }
}

async function loadRefPlanOptions() {
  if (!form.startTime || !form.endTime) return
  refPlansLoading.value = true
  try {
    const startDate = form.startTime.substring(0, 10)
    const endDate = form.endTime.substring(0, 10)
    const res = await getRollupOptions(editId.value || 0, {
      planType: form.planType,
      startDate,
      endDate
    })
    if (res.code === 200) {
      refPlanOptions.value = res.data || []
    }
  } catch {
    // ignore
  } finally {
    refPlansLoading.value = false
  }
}

async function loadPlanForEdit() {
  if (!isEdit.value || !editId.value) return
  try {
    const res = await getPlanDetail(editId.value)
    if (res.code === 200 && res.data) {
      const plan = res.data
      form.planType = plan.planType || 'DAILY'
      form.title = plan.title || ''
      form.priority = plan.priority || 'MEDIUM'
      form.startTime = toIsoString(plan.startTime)
      form.endTime = toIsoString(plan.endTime)
      form.categoryId = plan.categoryId || null
      form.quantTarget = plan.quantTarget || ''
      form.description = plan.description || ''
    }
  } catch {
    ElMessage.error('获取计划信息失败')
  }
}

function toIsoString(dateStr) {
  if (!dateStr) return ''
  // Convert "yyyy-MM-dd HH:mm:ss" to "yyyy-MM-ddTHH:mm:ss"
  return dateStr.replace(' ', 'T')
}

async function handleSave(submitDirectly) {
  const valid = await formRef.value?.validate().catch(() => false)
  if (!valid) return

  const action = submitDirectly ? submitting : saving
  action.value = true

  try {
    const data = {
      planType: form.planType,
      title: form.title,
      priority: form.priority,
      startTime: form.startTime,
      endTime: form.endTime,
      categoryId: form.categoryId,
      quantTarget: form.quantTarget,
      description: form.description,
      refPlanIds: form.refPlanIds.length > 0 ? form.refPlanIds : null,
      submitDirectly
    }

    if (isEdit.value) {
      await updatePlan(editId.value, data)
      ElMessage.success('修改成功')
    } else {
      await createPlan(data)
      ElMessage.success(submitDirectly ? '提交成功' : '保存成功')
    }
    router.push('/plans')
  } catch (error) {
    ElMessage.error(error?.message || '操作失败')
  } finally {
    action.value = false
  }
}

watch([() => form.startTime, () => form.endTime], () => {
  if (showRefPlans.value && form.startTime && form.endTime) {
    loadRefPlanOptions()
  }
})

onMounted(() => {
  loadCategories()
  loadPlanForEdit()
})
</script>

<style scoped>
.page-container { padding: 20px; }
.page-header { margin-bottom: 20px; }
.page-header h2 { margin: 0; }

.form-card {
  background: #fff;
  padding: 24px;
  border-radius: 4px;
}

.editor-wrap {
  width: 100%;
  margin-bottom: 20px;
}
</style>

<template>
  <div class="page-container">
    <div class="page-header">
      <h2>{{ isEdit ? '编辑成果' : '提交成果' }}</h2>
    </div>

    <div class="form-card">
      <!-- Selected Plan Info -->
      <el-alert v-if="selectedPlan" type="info" :closable="false" show-icon class="plan-alert">
        <template #title>
          <span>关联计划：</span>
          <el-tag :type="planTypeColor(selectedPlan.planType)" size="small">{{ planTypeLabel(selectedPlan.planType) }}</el-tag>
          <span class="plan-title">{{ selectedPlan.title }}</span>
          <span class="plan-meta">
            优先级：{{ priorityLabel(selectedPlan.priority) }} |
            指标：{{ selectedPlan.quantTarget || '未设置' }}
          </span>
        </template>
      </el-alert>

      <el-form ref="formRef" :model="form" :rules="rules" label-width="100px" size="default">
        <!-- Rich Text: Completion Description -->
        <el-form-item label="完成说明" prop="description">
          <div class="editor-wrap">
            <QuillEditor
              v-model:content="form.description"
              contentType="html"
              theme="snow"
              toolbar="full"
              :style="{ height: '200px' }"
            />
          </div>
        </el-form-item>

        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="实际完成数量">
              <el-input v-model="form.actualQty" placeholder="如：修复3个Bug" maxlength="200" show-word-limit />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="实际耗时(小时)">
              <el-input-number v-model="form.actualHours" :min="0" :step="0.5" :precision="1" placeholder="0" style="width:100%" />
            </el-form-item>
          </el-col>
        </el-row>

        <el-form-item label="遇到的问题">
          <el-input
            v-model="form.issues"
            type="textarea"
            :rows="3"
            placeholder="描述过程中遇到的问题或阻塞（可选）"
            maxlength="1000"
            show-word-limit
          />
        </el-form-item>

        <el-form-item label="备注">
          <el-input
            v-model="form.remark"
            type="textarea"
            :rows="2"
            placeholder="其他补充说明（可选）"
            maxlength="500"
            show-word-limit
          />
        </el-form-item>

        <!-- File Upload -->
        <el-form-item label="附件">
          <FileUpload v-model="attachments" />
        </el-form-item>

        <!-- Plan vs Actual Preview -->
        <el-form-item v-if="selectedPlan" label="计划对比">
          <PlanVsActual
            :plan="selectedPlan"
            :achievement="{ actualQty: form.actualQty, actualHours: form.actualHours, issues: form.issues, remark: form.remark }"
          />
        </el-form-item>

        <!-- Actions -->
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
import { ref, reactive, computed, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import { Document, Upload } from '@element-plus/icons-vue'
import { QuillEditor } from '@vueup/vue-quill'
import '@vueup/vue-quill/dist/vue-quill.snow.css'
import { createAchievement, updateAchievement, getAchievementDetail } from '@/api/achievement'
import { getPlanDetail, getPlanAchievement } from '@/api/plan'
import FileUpload from '@/components/common/FileUpload.vue'
import PlanVsActual from '@/components/achievement/PlanVsActual.vue'

const router = useRouter()
const route = useRoute()

const isEdit = computed(() => !!route.query.edit)
const editId = computed(() => route.query.edit ? Number(route.query.edit) : null)
const planId = computed(() => Number(route.params.planId))

const formRef = ref(null)
const saving = ref(false)
const submitting = ref(false)
const attachments = ref([])
const selectedPlan = ref(null)
const loadingPlan = ref(false)

const form = reactive({
  description: '',
  actualQty: '',
  actualHours: null,
  issues: '',
  remark: ''
})

const rules = {
  description: [{ required: true, message: '请填写完成说明', trigger: 'blur' }]
}

const planTypeMap = { DAILY: '日报', WEEKLY: '周报', MONTHLY: '月报' }
const priorityMap = { HIGH: '高', MEDIUM: '中', LOW: '低' }

function planTypeLabel(t) { return planTypeMap[t] || t }
function planTypeColor(t) { return t === 'DAILY' ? '' : t === 'WEEKLY' ? 'warning' : 'info' }
function priorityLabel(p) { return priorityMap[p] || p }

async function loadPlan() {
  loadingPlan.value = true
  try {
    const res = await getPlanDetail(planId.value)
    if (res.code === 200 && res.data) {
      selectedPlan.value = res.data
    } else {
      ElMessage.error('计划不存在')
      router.push('/achievements')
    }
  } catch {
    ElMessage.error('获取计划信息失败')
  } finally {
    loadingPlan.value = false
  }
}

async function loadAchievementForEdit() {
  if (!isEdit.value || !editId.value) return
  try {
    const res = await getAchievementDetail(editId.value)
    if (res.code === 200 && res.data) {
      const achv = res.data.achievement || res.data
      form.description = achv.description || ''
      form.actualQty = achv.actualQty || ''
      form.actualHours = achv.actualHours || null
      form.issues = achv.issues || ''
      form.remark = achv.remark || ''
      if (achv.attachments) {
        attachments.value = achv.attachments
      }
    }
  } catch {
    ElMessage.error('获取成果信息失败')
  }
}

async function handleSave(submitDirectly) {
  const valid = await formRef.value?.validate().catch(() => false)
  if (!valid) return

  const action = submitDirectly ? submitting : saving
  action.value = true

  try {
    const data = {
      planId: planId.value,
      description: form.description,
      actualQty: form.actualQty || null,
      actualHours: form.actualHours,
      issues: form.issues || null,
      remark: form.remark || null,
      attachmentIds: attachments.value.map(a => a.id),
      submitDirectly
    }

    if (isEdit.value) {
      await updateAchievement(editId.value, data)
      ElMessage.success('修改成功')
    } else {
      await createAchievement(data)
      ElMessage.success(submitDirectly ? '提交成功' : '保存成功')
    }
    router.push('/achievements')
  } catch (error) {
    ElMessage.error(error?.message || '操作失败')
  } finally {
    action.value = false
  }
}

onMounted(() => {
  loadPlan()
  loadAchievementForEdit()
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

.plan-alert {
  margin-bottom: 20px;
}

.plan-alert .plan-title {
  margin-left: 8px;
  font-weight: 500;
}

.plan-alert .plan-meta {
  margin-left: 16px;
  font-size: 13px;
  color: #909399;
}

.editor-wrap {
  width: 100%;
  margin-bottom: 20px;
}
</style>

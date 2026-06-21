<template>
  <div class="page-container">
    <div class="page-header">
      <h2>计划详情</h2>
      <div class="header-actions">
        <el-button
          v-if="canEdit"
          type="warning"
          @click="$router.push(`/plans/create?edit=${plan.id}`)"
        >
          <el-icon><Edit /></el-icon>
          编辑
        </el-button>
        <el-button
          v-if="plan.status === 'APPROVED'"
          type="success"
          @click="$router.push(`/achievements/submit/${plan.id}`)"
        >
          <el-icon><Upload /></el-icon>
          提交成果
        </el-button>
        <el-button
          v-if="canSubmit"
          type="success"
          :loading="submitting"
          @click="handleSubmit"
        >
          <el-icon><Upload /></el-icon>
          提交
        </el-button>
        <el-popconfirm
          v-if="canDelete"
          title="确定删除该计划吗？"
          @confirm="handleDelete"
        >
          <template #reference>
            <el-button type="danger">
              <el-icon><Delete /></el-icon>
              删除
            </el-button>
          </template>
        </el-popconfirm>
        <el-button @click="$router.push('/plans')">返回列表</el-button>
      </div>
    </div>

    <el-row v-loading="loading" :gutter="20">
      <!-- 基本信息 -->
      <el-col :span="16">
        <el-card shadow="never">
          <template #header>
            <div class="card-header">
              <span class="card-title">基本信息</span>
              <el-tag :type="statusColor(plan.status)" size="default">
                {{ statusLabel(plan.status) }}
              </el-tag>
            </div>
          </template>

          <el-descriptions :column="2" border>
            <el-descriptions-item label="计划标题" :span="2">
              {{ plan.title }}
            </el-descriptions-item>
            <el-descriptions-item label="计划类型">
              <el-tag :type="planTypeColor(plan.planType)" size="small">
                {{ planTypeLabel(plan.planType) }}
              </el-tag>
            </el-descriptions-item>
            <el-descriptions-item label="优先级">
              <el-tag :type="priorityColor(plan.priority)" size="small">
                {{ priorityLabel(plan.priority) }}
              </el-tag>
            </el-descriptions-item>
            <el-descriptions-item label="开始时间">{{ plan.startTime }}</el-descriptions-item>
            <el-descriptions-item label="截止时间">{{ plan.endTime }}</el-descriptions-item>
            <el-descriptions-item label="创建人">{{ plan.userName }}</el-descriptions-item>
            <el-descriptions-item label="创建时间">{{ plan.createdAt }}</el-descriptions-item>
            <el-descriptions-item label="量化指标" :span="2">
              {{ plan.quantTarget || '未设置' }}
            </el-descriptions-item>
          </el-descriptions>
        </el-card>

        <!-- 详细描述 -->
        <el-card v-if="plan.description" shadow="never" class="section-card">
          <template #header>
            <span class="card-title">详细描述</span>
          </template>
          <div class="description-content" v-html="plan.description" />
        </el-card>
      </el-col>

      <!-- 右侧：审批状态 & 关联成果 -->
      <el-col :span="8">
        <el-card shadow="never">
          <template #header>
            <span class="card-title">审批状态</span>
          </template>
          <ApprovalTimeline
            v-if="timelineData"
            :records="timelineData"
          />
          <div v-else class="approval-placeholder">
            <p>当前状态：{{ statusLabel(plan.status) }}</p>
          </div>
        </el-card>

        <el-card shadow="never" class="section-card">
          <template #header>
            <span class="card-title">关联成果</span>
          </template>
          <div class="achievement-placeholder">
            <div v-if="plan.hasAchievement">
              <el-button link type="primary" @click="$router.push(`/achievements/${plan.achievementId}`)">
                查看成果详情
              </el-button>
            </div>
            <div v-else-if="plan.status === 'APPROVED'">
              <p class="hint">该计划已通过，可提交成果</p>
              <el-button type="success" size="small" @click="$router.push(`/achievements/submit/${plan.id}`)">
                <el-icon><Upload /></el-icon>
                提交成果
              </el-button>
            </div>
            <el-empty
              v-else
              description="暂无成果"
              :image-size="60"
            />
          </div>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { Edit, Upload, Delete } from '@element-plus/icons-vue'
import { getPlanDetail, submitPlan, deletePlan } from '@/api/plan'
import { getApprovalTimeline } from '@/api/approval'
import ApprovalTimeline from '@/components/approval/ApprovalTimeline.vue'

const route = useRoute()
const router = useRouter()

const loading = ref(false)
const submitting = ref(false)
const timelineData = ref(null)

const plan = reactive({
  id: null,
  title: '',
  planType: '',
  priority: '',
  status: '',
  startTime: '',
  endTime: '',
  userName: '',
  createdAt: '',
  description: '',
  quantTarget: '',
  hasAchievement: false,
  achievementId: null
})

const planTypeMap = { DAILY: '日报', WEEKLY: '周报', MONTHLY: '月报' }
const statusMap = { DRAFT: '草稿', SUBMITTED: '已提交', APPROVING: '审批中', APPROVED: '已通过', REJECTED: '待修改', ARCHIVED: '已归档', OVERDUE: '已逾期' }
const priorityMap = { HIGH: '高', MEDIUM: '中', LOW: '低' }

function planTypeLabel(t) { return planTypeMap[t] || t }
function planTypeColor(t) { return t === 'DAILY' ? '' : t === 'WEEKLY' ? 'warning' : 'info' }
function statusLabel(s) { return statusMap[s] || s }
function statusColor(s) {
  const map = { DRAFT: 'info', SUBMITTED: '', APPROVING: 'warning', APPROVED: 'success', REJECTED: 'danger' }
  return map[s] || 'info'
}
function priorityLabel(p) { return priorityMap[p] || p }
function priorityColor(p) {
  const map = { HIGH: 'danger', MEDIUM: 'warning', LOW: '' }
  return map[p] || 'info'
}

const canEdit = computed(() => plan.status === 'DRAFT' || plan.status === 'REJECTED')
const canSubmit = computed(() => plan.status === 'DRAFT' || plan.status === 'REJECTED')
const canDelete = computed(() => plan.status === 'DRAFT')

async function loadDetail() {
  loading.value = true
  try {
    const id = Number(route.params.id)
    const res = await getPlanDetail(id)
    if (res.code === 200 && res.data) {
      Object.assign(plan, res.data)
      // Load approval timeline
      if (plan.status !== 'DRAFT') {
        try {
          const tlRes = await getApprovalTimeline('PLAN', plan.id)
          if (tlRes.code === 200 && tlRes.data) {
            timelineData.value = tlRes.data.records || tlRes.data
          }
        } catch { /* timeline is optional */ }
      }
    } else {
      ElMessage.error('计划不存在')
      router.push('/plans')
    }
  } catch {
    ElMessage.error('获取计划详情失败')
    router.push('/plans')
  } finally {
    loading.value = false
  }
}

async function handleSubmit() {
  submitting.value = true
  try {
    await submitPlan(plan.id)
    ElMessage.success('提交成功')
    loadDetail()
  } catch {
    ElMessage.error('提交失败')
  } finally {
    submitting.value = false
  }
}

async function handleDelete() {
  try {
    await deletePlan(plan.id)
    ElMessage.success('删除成功')
    router.push('/plans')
  } catch {
    ElMessage.error('删除失败')
  }
}

onMounted(() => { loadDetail() })
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
.header-actions { display: flex; gap: 8px; }

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
.card-title { font-weight: 600; font-size: 15px; }
.section-card { margin-top: 16px; }

.description-content {
  min-height: 60px;
  line-height: 1.8;
}
.description-content :deep(img) { max-width: 100%; }

.approval-placeholder {
  text-align: center;
  padding: 24px 0;
  color: #909399;
}
.approval-placeholder p { margin: 8px 0 0; }
.approval-placeholder .hint { font-size: 13px; color: #c0c4cc; }

.achievement-placeholder {
  min-height: 80px;
  display: flex;
  align-items: center;
  justify-content: center;
}
</style>

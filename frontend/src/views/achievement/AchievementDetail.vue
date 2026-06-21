<template>
  <div class="page-container">
    <div class="page-header">
      <h2>成果详情</h2>
      <div class="header-actions">
        <el-button
          v-if="canEdit"
          type="warning"
          @click="$router.push(`/achievements/submit/${achievement.planId}?edit=${achievement.id}`)"
        >
          <el-icon><Edit /></el-icon>
          编辑
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
        <el-button @click="$router.push('/achievements')">返回列表</el-button>
      </div>
    </div>

    <el-row v-loading="loading" :gutter="20">
      <!-- Main Content -->
      <el-col :span="16">
        <!-- Achievement Info -->
        <el-card shadow="never">
          <template #header>
            <div class="card-header">
              <span class="card-title">成果信息</span>
              <el-tag :type="achievementStatusColor(achievement.status)" size="default">
                {{ achievementStatusLabel(achievement.status) }}
              </el-tag>
            </div>
          </template>

          <el-descriptions :column="2" border>
            <el-descriptions-item label="关联计划" :span="2">
              <el-link type="primary" @click="$router.push(`/plans/${achievement.planId}`)">
                {{ plan?.title || '-' }}
              </el-link>
            </el-descriptions-item>
            <el-descriptions-item label="计划类型">
              <el-tag :type="planTypeColor(plan?.planType)" size="small">
                {{ planTypeLabel(plan?.planType) }}
              </el-tag>
            </el-descriptions-item>
            <el-descriptions-item label="优先级">
              <el-tag :type="priorityColor(plan?.priority)" size="small">
                {{ priorityLabel(plan?.priority) }}
              </el-tag>
            </el-descriptions-item>
            <el-descriptions-item label="实际完成数量">{{ achievement.actualQty || '未填写' }}</el-descriptions-item>
            <el-descriptions-item label="实际耗时">{{ achievement.actualHours != null ? `${achievement.actualHours} 小时` : '未填写' }}</el-descriptions-item>
            <el-descriptions-item label="提交时间" :span="2">{{ achievement.submittedAt || '-' }}</el-descriptions-item>
          </el-descriptions>
        </el-card>

        <!-- Completion Description -->
        <el-card v-if="achievement.description" shadow="never" class="section-card">
          <template #header>
            <span class="card-title">完成说明</span>
          </template>
          <div class="description-content" v-html="achievement.description" />
        </el-card>

        <!-- Issues & Remarks -->
        <el-card v-if="achievement.issues || achievement.remark" shadow="never" class="section-card">
          <template #header>
            <span class="card-title">问题与备注</span>
          </template>
          <div v-if="achievement.issues" class="info-block">
            <div class="info-label">遇到的问题：</div>
            <div class="info-value issues-text">{{ achievement.issues }}</div>
          </div>
          <div v-if="achievement.remark" class="info-block">
            <div class="info-label">备注：</div>
            <div class="info-value">{{ achievement.remark }}</div>
          </div>
        </el-card>

        <!-- Attachments -->
        <el-card shadow="never" class="section-card">
          <template #header>
            <span class="card-title">附件</span>
          </template>
          <FilePreview :attachments="achievement.attachments || []" />
        </el-card>
      </el-col>

      <!-- Sidebar: Comparison + Approval Timeline -->
      <el-col :span="8">
        <PlanVsActual
          v-if="plan"
          :plan="plan"
          :achievement="achievement"
          :comparison-status="comparison?.status"
          class="section-card"
        />

        <el-card shadow="never" class="section-card">
          <template #header>
            <span class="card-title">审批时间线</span>
          </template>
          <ApprovalTimeline
            v-if="timelineData"
            :records="timelineData"
          />
          <el-empty v-else description="暂无审批记录" :image-size="60" />
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { Edit, Upload } from '@element-plus/icons-vue'
import { getAchievementDetail, submitAchievement } from '@/api/achievement'
import { getApprovalTimeline } from '@/api/approval'
import FilePreview from '@/components/common/FilePreview.vue'
import PlanVsActual from '@/components/achievement/PlanVsActual.vue'
import ApprovalTimeline from '@/components/approval/ApprovalTimeline.vue'

const route = useRoute()
const router = useRouter()

const loading = ref(false)
const submitting = ref(false)
const achievement = reactive({})
const plan = ref(null)
const comparison = ref(null)
const timelineData = ref(null)

const planTypeMap = { DAILY: '日报', WEEKLY: '周报', MONTHLY: '月报' }
const achievementStatusMap = { PENDING: '待填写', SUBMITTED: '已提交', APPROVING: '验收中', APPROVED: '已通过', REJECTED: '待修改' }
const priorityMap = { HIGH: '高', MEDIUM: '中', LOW: '低' }

function planTypeLabel(t) { return planTypeMap[t] || t }
function planTypeColor(t) { return t === 'DAILY' ? '' : t === 'WEEKLY' ? 'warning' : 'info' }
function achievementStatusLabel(s) { return achievementStatusMap[s] || s }
function achievementStatusColor(s) {
  const map = { PENDING: 'info', SUBMITTED: '', APPROVING: 'warning', APPROVED: 'success', REJECTED: 'danger' }
  return map[s] || 'info'
}
function priorityLabel(p) { return priorityMap[p] || p }
function priorityColor(p) {
  const map = { HIGH: 'danger', MEDIUM: 'warning', LOW: '' }
  return map[p] || 'info'
}

const canEdit = computed(() => achievement.status === 'PENDING' || achievement.status === 'REJECTED')
const canSubmit = computed(() => achievement.status === 'PENDING' || achievement.status === 'REJECTED')

async function loadDetail() {
  loading.value = true
  try {
    const id = Number(route.params.id)
    const res = await getAchievementDetail(id)
    if (res.code === 200 && res.data) {
      const data = res.data
      Object.assign(achievement, data.achievement || data)
      plan.value = data.plan || null
      comparison.value = data.comparison || null

      // Load approval timeline if achievement is in approval flow
      if (achievement.status !== 'PENDING') {
        try {
          const tlRes = await getApprovalTimeline('ACHIEVEMENT', achievement.id)
          if (tlRes.code === 200 && tlRes.data) {
            timelineData.value = tlRes.data.records || tlRes.data
          }
        } catch {
          // Timeline is optional
        }
      }
    } else {
      ElMessage.error('成果不存在')
      router.push('/achievements')
    }
  } catch {
    ElMessage.error('获取成果详情失败')
    router.push('/achievements')
  } finally {
    loading.value = false
  }
}

async function handleSubmit() {
  submitting.value = true
  try {
    await submitAchievement(achievement.id)
    ElMessage.success('提交成功')
    loadDetail()
  } catch {
    ElMessage.error('提交失败')
  } finally {
    submitting.value = false
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

.info-block {
  margin-bottom: 16px;
}
.info-block:last-child { margin-bottom: 0; }
.info-label {
  font-weight: 500;
  color: #606266;
  margin-bottom: 4px;
}
.info-value {
  color: #303133;
}
.issues-text {
  color: #e6a23c;
  white-space: pre-wrap;
}
</style>

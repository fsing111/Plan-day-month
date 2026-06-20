<template>
  <div class="plan-vs-actual">
    <el-card shadow="never">
      <template #header>
        <div class="card-header">
          <span class="card-title">计划 vs 实际对比</span>
          <el-tag :type="statusTagType" size="default" effect="dark">
            {{ statusLabel }}
          </el-tag>
        </div>
      </template>

      <el-descriptions :column="2" border size="default">
        <el-descriptions-item label="计划标题" :span="2">
          {{ plan?.title || '-' }}
        </el-descriptions-item>

        <el-descriptions-item label="计划指标">
          <span class="plan-value">{{ plan?.quantTarget || '未设置' }}</span>
        </el-descriptions-item>
        <el-descriptions-item label="实际完成">
          <span :class="quantityMatchClass">
            {{ achievement?.actualQty || '未填写' }}
          </span>
        </el-descriptions-item>

        <el-descriptions-item label="计划时间">
          <span class="time-range">
            {{ formatTime(plan?.startTime) }} ~ {{ formatTime(plan?.endTime) }}
          </span>
        </el-descriptions-item>
        <el-descriptions-item label="实际耗时">
          <span :class="hoursMatchClass">
            {{ achievement?.actualHours != null ? `${achievement.actualHours} 小时` : '未填写' }}
          </span>
        </el-descriptions-item>

        <el-descriptions-item v-if="achievement?.issues" label="遇到的问题" :span="2">
          <div class="issues-text">{{ achievement.issues }}</div>
        </el-descriptions-item>

        <el-descriptions-item v-if="achievement?.remark" label="备注" :span="2">
          {{ achievement.remark }}
        </el-descriptions-item>
      </el-descriptions>
    </el-card>
  </div>
</template>

<script setup>
import { computed } from 'vue'
import { formatDate } from '@/utils/date'

const props = defineProps({
  plan: {
    type: Object,
    default: () => ({})
  },
  achievement: {
    type: Object,
    default: () => ({})
  },
  comparisonStatus: {
    type: String,
    default: ''
  }
})

const statusMap = {
  MATCH: '已达成',
  PARTIAL: '部分达成',
  EXCEED: '超额完成',
  NOT_MATCH: '未达成'
}

const statusTagTypeMap = {
  MATCH: 'success',
  PARTIAL: 'warning',
  EXCEED: '',
  NOT_MATCH: 'danger'
}

const statusLabel = computed(() => {
  return statusMap[props.comparisonStatus] || '未比较'
})

const statusTagType = computed(() => {
  return statusTagTypeMap[props.comparisonStatus] || 'info'
})

const quantityMatchClass = computed(() => {
  const map = { MATCH: 'match-text', EXCEED: 'exceed-text', NOT_MATCH: 'not-match-text', PARTIAL: 'partial-text' }
  return map[props.comparisonStatus] || ''
})

const hoursMatchClass = computed(() => {
  if (props.achievement?.actualHours == null) return ''
  return 'match-text'
})

function formatTime(timeStr) {
  return formatDate(timeStr, 'YYYY-MM-DD HH:mm')
}
</script>

<style scoped>
.plan-vs-actual {
  width: 100%;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.card-title {
  font-weight: 600;
  font-size: 15px;
}

.plan-value {
  color: #909399;
}

.time-range {
  font-size: 13px;
  color: #606266;
}

.match-text {
  color: #67c23a;
  font-weight: 500;
}

.exceed-text {
  color: #409eff;
  font-weight: 500;
}

.not-match-text {
  color: #f56c6c;
  font-weight: 500;
}

.partial-text {
  color: #e6a23c;
  font-weight: 500;
}

.issues-text {
  white-space: pre-wrap;
  color: #e6a23c;
}
</style>

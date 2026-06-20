<template>
  <div class="approval-timeline">
    <el-timeline v-if="formattedRecords.length > 0">
      <el-timeline-item
        v-for="(level, idx) in formattedRecords"
        :key="idx"
        :timestamp="level.timeLabel"
        :color="level.color"
        placement="top"
      >
        <div class="timeline-level">
          <div class="level-header">
            <el-tag :type="level.tagType" size="small">{{ level.label }}</el-tag>
            <span class="level-name">{{ level.levelLabel }}</span>
          </div>
          <div class="level-detail">
            <div v-for="(approval, aIdx) in level.approvals" :key="aIdx" class="approval-item">
              <div class="approver-row">
                <span class="approver-name">{{ approval.approverName }}</span>
                <el-tag :type="actionColor(approval.action)" size="small" effect="dark">
                  {{ actionLabel(approval.action) }}
                </el-tag>
              </div>
              <div v-if="approval.comment" class="approval-comment">
                「{{ approval.comment }}」
              </div>
              <div v-if="approval.approvedAt" class="approval-time">
                {{ approval.approvedAt }}
              </div>
            </div>
          </div>
        </div>
      </el-timeline-item>
    </el-timeline>
    <el-empty v-else description="暂无审批记录" :image-size="60" />
  </div>
</template>

<script setup>
import { computed } from 'vue'

const props = defineProps({
  records: {
    type: [Array, Object],
    default: () => []
  }
})

const actionMap = { APPROVE: '通过', REJECT: '驳回', TRANSFER: '转审' }
const actionColorMap = {
  APPROVE: 'success',
  REJECT: 'danger',
  TRANSFER: 'warning'
}

function actionLabel(a) { return actionMap[a] || '待审批' }
function actionColor(a) { return actionColorMap[a] || 'info' }

const formattedRecords = computed(() => {
  const records = Array.isArray(props.records) ? props.records : []
  if (records.length === 0) return []

  // If data is already formatted with approvalLevel grouping
  if (records[0]?.approvalLevel != null && records[0]?.approvals) {
    return records.map(level => ({
      ...level,
      tagType: getLevelTagType(level.approvals),
      color: getLevelColor(level.approvals),
      timeLabel: getLevelTime(level.approvals)
    }))
  }

  // If data is a flat list of approval records, group by level
  const grouped = {}
  for (const record of records) {
    const level = record.approvalLevel || 1
    if (!grouped[level]) {
      grouped[level] = {
        approvalLevel: level,
        levelLabel: `第${level}级审批`,
        approvals: []
      }
    }
    grouped[level].approvals.push(record)
  }

  return Object.values(grouped).map(level => ({
    ...level,
    tagType: getLevelTagType(level.approvals),
    color: getLevelColor(level.approvals),
    timeLabel: getLevelTime(level.approvals)
  }))
})

function getLevelTagType(approvals) {
  if (!approvals || approvals.length === 0) return 'info'
  const hasReject = approvals.some(a => a.action === 'REJECT')
  const hasTransfer = approvals.some(a => a.action === 'TRANSFER')
  const allApproved = approvals.every(a => a.action === 'APPROVE')
  const allPending = approvals.every(a => !a.action)

  if (hasReject) return 'danger'
  if (hasTransfer) return 'warning'
  if (allApproved) return 'success'
  if (allPending) return 'info'
  return 'warning'
}

function getLevelColor(approvals) {
  if (!approvals || approvals.length === 0) return '#909399'
  const hasReject = approvals.some(a => a.action === 'REJECT')
  const allApproved = approvals.every(a => a.action === 'APPROVE')

  if (hasReject) return '#f56c6c'
  if (allApproved) return '#67c23a'
  return '#e6a23c'
}

function getLevelTime(approvals) {
  if (!approvals || approvals.length === 0) return ''
  const times = approvals
    .filter(a => a.approvedAt)
    .map(a => a.approvedAt)
  return times.length > 0 ? times.join(' / ') : '待审批'
}
</script>

<style scoped>
.approval-timeline {
  width: 100%;
}

.timeline-level {
  padding-bottom: 4px;
}

.level-header {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 8px;
}

.level-name {
  font-weight: 500;
  font-size: 14px;
  color: #303133;
}

.level-detail {
  margin-left: 0;
}

.approval-item {
  padding: 8px 12px;
  background: #f5f7fa;
  border-radius: 6px;
  margin-bottom: 6px;
}

.approval-item:last-child {
  margin-bottom: 0;
}

.approver-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.approver-name {
  font-size: 13px;
  color: #606266;
  font-weight: 500;
}

.approval-comment {
  font-size: 13px;
  color: #909399;
  margin-top: 4px;
  font-style: italic;
}

.approval-time {
  font-size: 12px;
  color: #c0c4cc;
  margin-top: 4px;
}
</style>

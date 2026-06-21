<template>
  <el-dialog
    v-model="visible"
    :title="dialogTitle"
    width="560px"
    :close-on-click-modal="false"
    append-to-body
    @close="handleClose"
  >
    <div v-if="record" class="approval-dialog-body">
      <!-- Record Summary -->
      <el-descriptions :column="2" border size="small" class="record-summary">
        <el-descriptions-item label="提交人">{{ record.submitterName }}</el-descriptions-item>
        <el-descriptions-item label="类型">
          <el-tag :type="record.targetType === 'PLAN' ? '' : 'success'" size="small">
            {{ record.targetType === 'PLAN' ? '计划' : '成果' }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="标题" :span="2">{{ record.title }}</el-descriptions-item>
      </el-descriptions>

      <!-- Peer Approvals -->
      <div v-if="record.peerApprovals && record.peerApprovals.length > 0" class="peer-section">
        <div class="section-label">同级审批意见</div>
        <div v-for="(peer, idx) in record.peerApprovals" :key="idx" class="peer-item">
          <span class="peer-name">{{ peer.approverName }}</span>
          <el-tag :type="peer.action === 'APPROVE' ? 'success' : 'danger'" size="small">
            {{ peer.action === 'APPROVE' ? '通过' : '驳回' }}
          </el-tag>
          <span v-if="peer.comment" class="peer-comment">「{{ peer.comment }}」</span>
          <span class="peer-time">{{ peer.approvedAt }}</span>
        </div>
      </div>

      <!-- Approval Comment -->
      <div class="comment-section">
        <div class="section-label">审批意见</div>
        <el-input
          v-model="comment"
          type="textarea"
          :rows="3"
          :placeholder="actionMode === 'reject' ? '请输入驳回原因（必填）' : '请输入审批意见（可选）'"
          maxlength="500"
          show-word-limit
        />
      </div>

      <!-- Transfer Target (only for transfer action) -->
      <div v-if="actionMode === 'transfer'" class="transfer-section">
        <div class="section-label">转审给</div>
        <el-select
          v-model="transferTargetId"
          placeholder="选择同部门用户"
          filterable
          style="width: 100%"
        >
          <el-option
            v-for="user in deptUsers"
            :key="user.id"
            :label="`${user.realName} (${user.username})`"
            :value="user.id"
          />
        </el-select>
      </div>
    </div>

    <template #footer>
      <div class="dialog-footer">
        <el-button @click="visible = false">取消</el-button>
        <el-button type="warning" :loading="loading" @click="handleTransfer">
          转审
        </el-button>
        <el-tooltip :content="!comment.trim() ? '驳回必须填写具体原因' : ''" :disabled="!!comment.trim()">
          <el-button type="danger" :loading="loading" :disabled="!comment.trim()" @click="handleReject">
            驳回
          </el-button>
        </el-tooltip>
        <el-button type="success" :loading="loading" @click="handleApprove">
          通过
        </el-button>
      </div>
    </template>
  </el-dialog>
</template>

<script setup>
import { ref, computed, watch } from 'vue'
import { ElMessage } from 'element-plus'
import { approveRecord, rejectRecord, transferRecord } from '@/api/approval'
import { getDepartmentList } from '@/api/admin'

const props = defineProps({
  modelValue: {
    type: Boolean,
    default: false
  },
  record: {
    type: Object,
    default: null
  }
})

const emit = defineEmits(['update:modelValue', 'done'])

const visible = computed({
  get: () => props.modelValue,
  set: (val) => emit('update:modelValue', val)
})

const loading = ref(false)
const comment = ref('')
const actionMode = ref('')
const transferTargetId = ref(null)
const deptUsers = ref([])

const dialogTitle = computed(() => {
  return props.record?.targetType === 'PLAN' ? '审批计划' : '验收成果'
})

watch(() => props.record, async (val) => {
  if (val) {
    comment.value = ''
    actionMode.value = ''
    transferTargetId.value = null
    // Load department users for transfer
    await loadDeptUsers()
  }
})

async function loadDeptUsers() {
  try {
    const res = await getDepartmentList()
    if (res.code === 200 && res.data) {
      // Flatten all users from departments
      // In a real scenario, this would have a dedicated API for department users
      deptUsers.value = []
    }
  } catch {
    deptUsers.value = []
  }
}

async function handleApprove() {
  actionMode.value = 'approve'
  await submitAction(() => approveRecord(props.record.id, { comment: comment.value }))
}

async function handleReject() {
  actionMode.value = 'reject'
  await submitAction(() => rejectRecord(props.record.id, { comment: comment.value }))
}

async function handleTransfer() {
  if (!transferTargetId.value) {
    ElMessage.warning('请选择转审目标用户')
    return
  }
  actionMode.value = 'transfer'
  await submitAction(() => transferRecord(props.record.id, {
    targetUserId: transferTargetId.value,
    comment: comment.value
  }))
}

async function submitAction(apiCall) {
  loading.value = true
  try {
    await apiCall()
    const labels = { approve: '已通过', reject: '待修改', transfer: '已转审' }
    ElMessage.success(labels[actionMode.value] || '操作成功')
    emit('done')
    visible.value = false
  } catch (error) {
    ElMessage.error(error?.message || '操作失败')
  } finally {
    loading.value = false
  }
}

function handleClose() {
  comment.value = ''
  actionMode.value = ''
}
</script>

<style scoped>
.approval-dialog-body {
  padding: 0;
}

.record-summary {
  margin-bottom: 16px;
}

.section-label {
  font-weight: 600;
  font-size: 14px;
  color: #303133;
  margin-bottom: 8px;
}

.peer-section {
  margin-bottom: 16px;
  padding: 12px;
  background: #f5f7fa;
  border-radius: 6px;
}

.peer-item {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 6px 0;
  font-size: 13px;
}

.peer-item:not(:last-child) {
  border-bottom: 1px dashed #e4e7ed;
}

.peer-name {
  font-weight: 500;
  color: #606266;
}

.peer-comment {
  color: #909399;
  font-style: italic;
}

.peer-time {
  margin-left: auto;
  font-size: 12px;
  color: #c0c4cc;
}

.comment-section {
  margin-bottom: 16px;
}

.transfer-section {
  margin-bottom: 16px;
}

.dialog-footer {
  display: flex;
  justify-content: flex-end;
  gap: 8px;
}
</style>

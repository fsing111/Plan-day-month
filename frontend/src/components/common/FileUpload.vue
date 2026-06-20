<template>
  <div class="file-upload">
    <el-upload
      ref="uploadRef"
      v-model:file-list="fileList"
      :action="uploadUrl"
      :headers="uploadHeaders"
      :accept="accept"
      :limit="5"
      :max-size="10 * 1024 * 1024"
      :on-success="handleSuccess"
      :on-error="handleError"
      :on-exceed="handleExceed"
      :on-remove="handleRemove"
      :before-upload="beforeUpload"
      :auto-upload="true"
      list-type="text"
      drag
    >
      <el-icon class="el-icon--upload"><UploadFilled /></el-icon>
      <div class="el-upload__text">
        将文件拖到此处，或<em>点击上传</em>
      </div>
      <template #tip>
        <div class="el-upload__tip">
          支持图片、PDF、Word、Excel 格式，单文件 ≤ 10MB，最多 5 个附件
        </div>
      </template>
    </el-upload>
  </div>
</template>

<script setup>
import { ref, computed, watch } from 'vue'
import { ElMessage } from 'element-plus'
import { UploadFilled } from '@element-plus/icons-vue'
import { getToken } from '@/utils/auth'

const props = defineProps({
  modelValue: {
    type: Array,
    default: () => []
  }
})

const emit = defineEmits(['update:modelValue'])

const uploadRef = ref(null)
const fileList = ref([])

const uploadUrl = '/api/v1/files/upload'
const uploadHeaders = computed(() => ({
  Authorization: `Bearer ${getToken()}`
}))

const accept = '.jpg,.jpeg,.png,.gif,.bmp,.pdf,.doc,.docx,.xls,.xlsx,.txt,.zip'

// Sync with modelValue
watch(() => props.modelValue, (val) => {
  if (val && val.length > 0) {
    fileList.value = val.map(item => ({
      id: item.id,
      name: item.fileName,
      size: item.fileSize,
      url: item.filePath,
      status: 'success'
    }))
  }
}, { immediate: true })

function beforeUpload(file) {
  const allowedTypes = [
    'image/jpeg', 'image/png', 'image/gif', 'image/bmp',
    'application/pdf',
    'application/msword',
    'application/vnd.openxmlformats-officedocument.wordprocessingml.document',
    'application/vnd.ms-excel',
    'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet',
    'text/plain',
    'application/zip'
  ]
  if (!allowedTypes.includes(file.type)) {
    ElMessage.error(`不支持的文件格式：${file.type}`)
    return false
  }
  return true
}

function handleSuccess(response, uploadFile) {
  if (response.code === 200 && response.data) {
    // Store the attachment ID from server response
    uploadFile.attachmentId = response.data.id
    emitUpdate()
  } else {
    ElMessage.error(response.message || '上传失败')
    const idx = fileList.value.indexOf(uploadFile)
    if (idx > -1) fileList.value.splice(idx, 1)
  }
}

function handleError() {
  ElMessage.error('上传失败，请重试')
}

function handleExceed() {
  ElMessage.warning('最多上传 5 个附件')
}

function handleRemove(uploadFile) {
  emitUpdate()
}

function handlePreview(uploadFile) {
  if (uploadFile.url) {
    window.open(uploadFile.url, '_blank')
  }
}

function emitUpdate() {
  const attachments = fileList.value
    .filter(f => f.status === 'success' && (f.attachmentId || f.id))
    .map(f => ({
      id: f.attachmentId || f.id,
      fileName: f.name,
      fileSize: f.size,
      filePath: f.url || f.response?.data?.filePath
    }))
  emit('update:modelValue', attachments)
}
</script>

<style scoped>
.file-upload {
  width: 100%;
}
</style>

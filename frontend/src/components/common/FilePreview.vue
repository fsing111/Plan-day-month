<template>
  <div class="file-preview">
    <div v-if="attachments.length === 0" class="empty-state">
      <el-empty description="暂无附件" :image-size="60" />
    </div>
    <div v-else class="file-list">
      <div
        v-for="file in attachments"
        :key="file.id"
        class="file-item"
      >
        <div class="file-info">
          <el-icon :size="20" class="file-icon">
            <component :is="fileIcon(file)" />
          </el-icon>
          <span class="file-name" :title="file.fileName">{{ file.fileName }}</span>
          <span class="file-size">{{ formatSize(file.fileSize) }}</span>
        </div>
        <div class="file-actions">
          <el-button
            v-if="isImage(file)"
            link
            type="primary"
            size="small"
            @click="previewImage(file)"
          >
            <el-icon><View /></el-icon>
            预览
          </el-button>
          <el-button
            link
            type="primary"
            size="small"
            @click="downloadFile(file)"
          >
            <el-icon><Download /></el-icon>
            下载
          </el-button>
        </div>
      </div>
    </div>

    <!-- Image Preview Dialog -->
    <el-dialog v-model="previewVisible" title="图片预览" width="720px" append-to-body>
      <div class="image-preview-container">
        <img :src="previewSrc" alt="preview" class="preview-image" />
      </div>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { View, Download, PictureFilled, Document } from '@element-plus/icons-vue'
import { downloadFile as downloadFileApi } from '@/api/file'

defineProps({
  attachments: {
    type: Array,
    default: () => []
  }
})

const previewVisible = ref(false)
const previewSrc = ref('')

const imageTypes = ['image/jpeg', 'image/png', 'image/gif', 'image/bmp', 'image/webp']

function fileIcon(file) {
  const imageTypes = ['image/jpeg', 'image/png', 'image/gif', 'image/bmp', 'image/webp']
  return imageTypes.includes(file.fileType) ? PictureFilled : Document
}

function isImage(file) {
  return imageTypes.includes(file.fileType)
}

function previewImage(file) {
  previewSrc.value = `/api/v1/files/${file.id}/preview`
  previewVisible.value = true
}

async function downloadFile(file) {
  try {
    const res = await downloadFileApi(file.id)
    const url = window.URL.createObjectURL(new Blob([res]))
    const link = document.createElement('a')
    link.href = url
    link.download = file.fileName
    document.body.appendChild(link)
    link.click()
    document.body.removeChild(link)
    window.URL.revokeObjectURL(url)
  } catch {
    // Fallback: open direct download link
    window.open(`/api/v1/files/${file.id}/download`, '_blank')
  }
}

function formatSize(bytes) {
  if (!bytes) return '0 B'
  const units = ['B', 'KB', 'MB', 'GB']
  let i = 0
  let size = bytes
  while (size >= 1024 && i < units.length - 1) {
    size /= 1024
    i++
  }
  return `${size.toFixed(1)} ${units[i]}`
}
</script>

<style scoped>
.file-preview {
  width: 100%;
}

.file-list {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.file-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 10px 12px;
  background: #f5f7fa;
  border-radius: 6px;
  transition: background 0.2s;
}

.file-item:hover {
  background: #ecf0f5;
}

.file-info {
  display: flex;
  align-items: center;
  gap: 8px;
  flex: 1;
  min-width: 0;
}

.file-icon {
  color: #909399;
  flex-shrink: 0;
}

.file-name {
  font-size: 14px;
  color: #303133;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.file-size {
  font-size: 12px;
  color: #909399;
  flex-shrink: 0;
}

.file-actions {
  display: flex;
  gap: 4px;
  flex-shrink: 0;
  margin-left: 12px;
}

.empty-state {
  min-height: 80px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.image-preview-container {
  display: flex;
  justify-content: center;
  align-items: center;
  min-height: 200px;
}

.preview-image {
  max-width: 100%;
  max-height: 500px;
  object-fit: contain;
}
</style>

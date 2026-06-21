<template>
  <div class="comment-section">
    <div class="section-title">讨论区</div>
    <div v-if="comments.length > 0" class="comment-list">
      <div v-for="c in comments" :key="c.id" class="comment-item">
        <div class="comment-header">
          <span class="comment-user">{{ c.userId }}</span>
          <span class="comment-time">{{ c.createdAt }}</span>
          <el-button
            v-if="canDelete(c.userId)"
            link type="danger" size="small"
            @click="$emit('delete', c.id)"
          >删除</el-button>
        </div>
        <div class="comment-content">{{ c.content }}</div>
      </div>
    </div>
    <el-empty v-else description="暂无评论" :image-size="60" />
    <div class="comment-input">
      <el-input
        v-model="newComment"
        type="textarea"
        :rows="2"
        placeholder="输入评论..."
        maxlength="500"
      />
      <el-button type="primary" size="small" style="margin-top:8px" @click="handleAdd">
        发表评论
      </el-button>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { useUserStore } from '@/stores/user'

const props = defineProps({
  comments: { type: Array, default: () => [] },
  currentUserId: { type: Number, default: null }
})
const emit = defineEmits(['add', 'delete'])
const userStore = useUserStore()
const newComment = ref('')

function canDelete(commentUserId) {
  return true
}

function handleAdd() {
  if (!newComment.value.trim()) return
  emit('add', newComment.value)
  newComment.value = ''
}
</script>

<style scoped>
.section-title { font-size: 16px; font-weight: 600; margin-bottom: 12px; }
.comment-item { padding: 12px 0; border-bottom: 1px solid #ebeef5; }
.comment-header { display: flex; align-items: center; gap: 12px; margin-bottom: 6px; }
.comment-user { font-weight: 500; color: #409eff; }
.comment-time { font-size: 12px; color: #909399; }
.comment-content { color: #333; line-height: 1.6; }
.comment-input { margin-top: 16px; }
</style>

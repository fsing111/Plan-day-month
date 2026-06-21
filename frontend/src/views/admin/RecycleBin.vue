<template>
  <div class="page-container">
    <div class="page-header"><h2>回收站</h2></div>
    <el-table v-loading="loading" :data="tableData" stripe border>
      <el-table-column prop="originalTable" label="来源" width="80">
        <template #default="{ row }">
          <el-tag :type="row.originalTable === 'plan' ? '' : 'success'" size="small">
            {{ row.originalTable === 'plan' ? '计划' : '成果' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="title" label="标题" min-width="180" />
      <el-table-column prop="deletedAt" label="删除时间" width="160" />
      <el-table-column prop="canRestoreUntil" label="恢复截止" width="160" />
      <el-table-column label="操作" width="180">
        <template #default="{ row }">
          <el-button link type="primary" size="small" @click="handleRestore(row.id)">恢复</el-button>
          <el-popconfirm title="彻底删除后不可恢复！" @confirm="handlePermanentDelete(row.id)">
            <template #reference>
              <el-button link type="danger" size="small">彻底删除</el-button>
            </template>
          </el-popconfirm>
        </template>
      </el-table-column>
    </el-table>
    <div class="pagination-wrap">
      <el-pagination
        v-model:current-page="pagination.page"
        v-model:page-size="pagination.pageSize"
        :page-sizes="[10, 20, 50]"
        :total="pagination.total"
        layout="total, sizes, prev, pager, next"
        @size-change="fetchData"
        @current-change="fetchData"
      />
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { getRecycleBinItems, restoreItem, permanentDelete } from '@/api/recycleBin'

const router = useRouter()
const userStore = useUserStore()
const loading = ref(false)
const tableData = ref([])
const pagination = reactive({ page: 1, pageSize: 20, total: 0 })

onMounted(() => {
  if (!userStore.isLeader) {
    ElMessage.warning('没有权限访问该页面')
    router.replace('/dashboard')
    return
  }
  fetchData()
})

async function fetchData() {
  loading.value = true
  try {
    const res = await getRecycleBinItems({ page: pagination.page, pageSize: pagination.pageSize })
    if (res.code === 200 && res.data) {
      tableData.value = res.data.records || []
      pagination.total = res.data.total || 0
    }
  } catch { ElMessage.error('获取失败') }
  finally { loading.value = false }
}

async function handleRestore(id) {
  try {
    await restoreItem(id)
    ElMessage.success('恢复成功')
    fetchData()
  } catch { ElMessage.error('恢复失败') }
}

async function handlePermanentDelete(id) {
  try {
    await permanentDelete(id)
    ElMessage.success('已彻底删除')
    fetchData()
  } catch { ElMessage.error('删除失败') }
}
</script>

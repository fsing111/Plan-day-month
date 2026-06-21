<template>
  <div class="page-container">
    <div class="page-header">
      <h2>团队统计</h2>
      <div class="header-filters">
        <el-date-picker
          v-model="dateRange"
          type="daterange"
          range-separator="至"
          start-placeholder="开始日期"
          end-placeholder="结束日期"
          value-format="YYYY-MM-DD"
          @change="fetchData"
        />
      </div>
    </div>

    <div v-loading="loading">
      <!-- Summary Cards -->
      <el-row :gutter="16" class="summary-row">
        <el-col :span="6">
          <div class="stat-card">
            <div class="stat-number">{{ teamSummary.totalMembers || 0 }}</div>
            <div class="stat-label">团队成员</div>
          </div>
        </el-col>
        <el-col :span="6">
          <div class="stat-card">
            <div class="stat-number">{{ teamSummary.totalPlans || 0 }}</div>
            <div class="stat-label">计划总数</div>
          </div>
        </el-col>
        <el-col :span="6">
          <div class="stat-card stat-card-info">
            <div class="stat-number">{{ teamSummary.completionRate ?? 0 }}%</div>
            <div class="stat-label">团队完成率</div>
          </div>
        </el-col>
        <el-col :span="6">
          <div class="stat-card stat-card-success">
            <div class="stat-number">{{ teamSummary.avgOnTimeRate ?? 0 }}%</div>
            <div class="stat-label">平均按时率</div>
          </div>
        </el-col>
      </el-row>

      <!-- Member Ranking Table -->
      <el-card shadow="never" class="section-card">
        <template #header>
          <span class="card-title">成员完成率排名</span>
        </template>
        <el-table :data="members" stripe border style="width: 100%">
          <el-table-column type="index" label="排名" width="60" />
          <el-table-column prop="userName" label="姓名" width="120" />
          <el-table-column prop="totalPlans" label="计划总数" width="100" />
          <el-table-column prop="approved" label="已通过" width="80" />
          <el-table-column label="完成率" width="120">
            <template #default="{ row }">
              <el-progress
                :percentage="row.completionRate || 0"
                :color="progressColor(row.completionRate)"
                :stroke-width="16"
              />
            </template>
          </el-table-column>
          <el-table-column label="按时提交率" width="120">
            <template #default="{ row }">
              <el-progress
                :percentage="row.onTimeRate || 0"
                :color="progressColor(row.onTimeRate)"
                :stroke-width="16"
              />
            </template>
          </el-table-column>
        </el-table>
      </el-card>

      <!-- Unsubmitted Today -->
      <el-card v-if="unsubmittedToday.length > 0" shadow="never" class="section-card">
        <template #header>
          <span class="card-title">今日未提交日报</span>
          <el-tag type="warning" size="small">{{ unsubmittedToday.length }}人</el-tag>
        </template>
        <div class="unsubmitted-list">
          <el-tag
            v-for="user in unsubmittedToday"
            :key="user.userId"
            class="unsubmitted-tag"
          >
            {{ user.userName }}
          </el-tag>
        </div>
      </el-card>

      <!-- Bar Chart: Member Comparison -->
      <el-card shadow="never" class="section-card">
        <BarChart
          v-if="memberChartCategories.length > 0"
          title="成员完成率对比"
          :categories="memberChartCategories"
          :series="memberChartSeries"
          :height="350"
          y-axis-name="%"
          :horizontal="true"
        />
        <el-empty v-else description="暂无数据" :image-size="60" />
      </el-card>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { getTeamStats } from '@/api/statistics'
import BarChart from '@/components/charts/BarChart.vue'

const router = useRouter()
const userStore = useUserStore()
const loading = ref(false)
const dateRange = ref([])

const teamSummary = reactive({
  totalMembers: 0,
  totalPlans: 0,
  approvedPlans: 0,
  completionRate: 0,
  avgOnTimeRate: 0
})

const members = ref([])
const unsubmittedToday = ref([])

const memberChartCategories = computed(() => members.value.map(m => m.userName))
const memberChartSeries = computed(() => [{
  name: '完成率',
  data: members.value.map(m => m.completionRate || 0),
  labelFormatter: '{c}%'
}])

function progressColor(rate) {
  if (rate >= 90) return '#67c23a'
  if (rate >= 70) return '#e6a23c'
  return '#f56c6c'
}

async function fetchData() {
  loading.value = true
  try {
    const params = {}
    if (dateRange.value && dateRange.value.length === 2) {
      params.startDate = dateRange.value[0]
      params.endDate = dateRange.value[1]
    }

    const res = await getTeamStats(params)
    if (res.code === 200 && res.data) {
      const data = res.data
      Object.assign(teamSummary, data.teamSummary || {})
      members.value = data.members || []
      unsubmittedToday.value = data.unsubmittedToday || []
    }
  } catch {
    ElMessage.error('获取团队统计失败')
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  if (!userStore.isLeader) {
    ElMessage.warning('没有权限访问该页面')
    router.replace('/dashboard')
    return
  }
  fetchData()
})
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
.header-filters { display: flex; gap: 12px; }

.summary-row { margin-bottom: 16px; }

.stat-card {
  background: #fff;
  border-radius: 6px;
  padding: 20px;
  text-align: center;
  border-left: 4px solid #409eff;
}

.stat-card-info { border-left-color: #909399; }
.stat-card-success { border-left-color: #67c23a; }

.stat-number {
  font-size: 28px;
  font-weight: 700;
  color: #303133;
  margin-bottom: 4px;
}

.stat-label {
  font-size: 13px;
  color: #909399;
}

.section-card { margin-top: 16px; }

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
.card-title { font-weight: 600; font-size: 15px; }

.unsubmitted-list {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}
</style>

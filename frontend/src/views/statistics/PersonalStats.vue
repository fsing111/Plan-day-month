<template>
  <div class="page-container">
    <div class="page-header">
      <h2>个人统计</h2>
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
        <el-select v-model="planTypeFilter" placeholder="计划类型" clearable style="width: 120px" @change="fetchData">
          <el-option label="日报" value="DAILY" />
          <el-option label="周报" value="WEEKLY" />
          <el-option label="月报" value="MONTHLY" />
        </el-select>
      </div>
    </div>

    <div v-loading="loading">
      <!-- Summary Cards -->
      <el-row :gutter="16" class="summary-row">
        <el-col :span="6">
          <div class="stat-card">
            <div class="stat-number">{{ summary.totalPlans || 0 }}</div>
            <div class="stat-label">计划总数</div>
          </div>
        </el-col>
        <el-col :span="6">
          <div class="stat-card stat-card-success">
            <div class="stat-number">{{ summary.approvedPlans || 0 }}</div>
            <div class="stat-label">已通过</div>
          </div>
        </el-col>
        <el-col :span="6">
          <div class="stat-card stat-card-info">
            <div class="stat-number">{{ summary.completionRate ?? 0 }}%</div>
            <div class="stat-label">完成率</div>
          </div>
        </el-col>
        <el-col :span="6">
          <div class="stat-card stat-card-warning">
            <div class="stat-number">{{ summary.onTimeSubmitRate ?? 0 }}%</div>
            <div class="stat-label">按时提交率</div>
          </div>
        </el-col>
      </el-row>

      <!-- Charts Row 1 -->
      <el-row :gutter="16" class="chart-row">
        <el-col :span="12">
          <el-card shadow="never">
            <PieChart
              v-if="pieData.length > 0"
              title="状态分布"
              :data="pieData"
              :height="320"
            />
            <el-empty v-else description="暂无数据" :image-size="60" />
          </el-card>
        </el-col>
        <el-col :span="12">
          <el-card shadow="never">
            <BarChart
              v-if="byTypeCategories.length > 0"
              title="按计划类型统计"
              :categories="byTypeCategories"
              :series="byTypeSeries"
              :height="320"
              y-axis-name="数量"
            />
            <el-empty v-else description="暂无数据" :image-size="60" />
          </el-card>
        </el-col>
      </el-row>

      <!-- Charts Row 2 -->
      <el-row :gutter="16" class="chart-row">
        <el-col :span="12">
          <el-card shadow="never">
            <BarChart
              v-if="byPriorityCategories.length > 0"
              title="按优先级统计"
              :categories="byPriorityCategories"
              :series="byPrioritySeries"
              :height="320"
              y-axis-name="数量"
            />
            <el-empty v-else description="暂无数据" :image-size="60" />
          </el-card>
        </el-col>
        <el-col :span="12">
          <el-card shadow="never">
            <LineChart
              v-if="trendCategories.length > 0"
              title="完成率趋势"
              :categories="trendCategories"
              :series="trendSeries"
              :height="320"
            />
            <el-empty v-else description="暂无数据" :image-size="60" />
          </el-card>
        </el-col>
      </el-row>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { getPersonalStats } from '@/api/statistics'
import PieChart from '@/components/charts/PieChart.vue'
import BarChart from '@/components/charts/BarChart.vue'
import LineChart from '@/components/charts/LineChart.vue'

const loading = ref(false)
const dateRange = ref([])
const planTypeFilter = ref('')

const summary = reactive({
  totalPlans: 0,
  approvedPlans: 0,
  rejectedPlans: 0,
  completionRate: 0,
  onTimeSubmitRate: 0
})

const byType = ref({})
const byPriority = ref({})
const trend = ref([])

// Status pie chart
const pieData = computed(() => {
  const statusLabels = {
    DRAFT: '草稿', SUBMITTED: '已提交', APPROVING: '审批中',
    APPROVED: '已通过', REJECTED: '已驳回'
  }
  const statusColors = {
    DRAFT: '#909399', SUBMITTED: '#409eff', APPROVING: '#e6a23c',
    APPROVED: '#67c23a', REJECTED: '#f56c6c'
  }
  // Derive from summary
  return [
    { name: '已通过', value: summary.approvedPlans || 0, itemStyle: { color: '#67c23a' } },
    { name: '已驳回', value: summary.rejectedPlans || 0, itemStyle: { color: '#f56c6c' } },
    { name: '其他', value: Math.max(0, (summary.totalPlans || 0) - (summary.approvedPlans || 0) - (summary.rejectedPlans || 0)), itemStyle: { color: '#e6a23c' } }
  ].filter(d => d.value > 0)
})

// By type chart
const byTypeCategories = computed(() => {
  return Object.keys(byType.value).map(k => {
    const map = { DAILY: '日报', WEEKLY: '周报', MONTHLY: '月报' }
    return map[k] || k
  })
})

const byTypeSeries = computed(() => [
  {
    name: '已通过',
    data: Object.values(byType.value).map(v => v.approved || 0)
  },
  {
    name: '总数',
    data: Object.values(byType.value).map(v => v.total || 0)
  }
])

// By priority chart
const byPriorityCategories = computed(() => {
  return Object.keys(byPriority.value).map(k => {
    const map = { HIGH: '高', MEDIUM: '中', LOW: '低' }
    return map[k] || k
  })
})

const byPrioritySeries = computed(() => [
  {
    name: '已通过',
    data: Object.values(byPriority.value).map(v => v.approved || 0)
  },
  {
    name: '总数',
    data: Object.values(byPriority.value).map(v => v.total || 0)
  }
])

// Trend chart
const trendCategories = computed(() => trend.value.map(t => t.period || ''))
const trendSeries = computed(() => [{
  name: '完成率',
  data: trend.value.map(t => t.rate || 0),
  labelFormatter: '{c}%'
}])

async function fetchData() {
  loading.value = true
  try {
    const params = {}
    if (dateRange.value && dateRange.value.length === 2) {
      params.startDate = dateRange.value[0]
      params.endDate = dateRange.value[1]
    }
    if (planTypeFilter.value) params.planType = planTypeFilter.value

    const res = await getPersonalStats(params)
    if (res.code === 200 && res.data) {
      const data = res.data
      Object.assign(summary, data.summary || {})
      byType.value = data.byType || {}
      byPriority.value = data.byPriority || {}
      trend.value = data.trend || []
    }
  } catch {
    ElMessage.error('获取统计数据失败')
  } finally {
    loading.value = false
  }
}

onMounted(() => { fetchData() })
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

.stat-card-success { border-left-color: #67c23a; }
.stat-card-info { border-left-color: #909399; }
.stat-card-warning { border-left-color: #e6a23c; }

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

.chart-row {
  margin-bottom: 16px;
}
</style>

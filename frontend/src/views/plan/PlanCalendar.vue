<template>
  <div class="page-container">
    <div class="page-header">
      <h2>日历视图</h2>
      <div class="header-controls">
        <el-radio-group v-model="planTypeFilter" size="default" @change="reloadCalendar">
          <el-radio-button value="">全部</el-radio-button>
          <el-radio-button value="DAILY">日报</el-radio-button>
          <el-radio-button value="WEEKLY">周报</el-radio-button>
          <el-radio-button value="MONTHLY">月报</el-radio-button>
        </el-radio-group>
      </div>
    </div>

    <div class="calendar-card">
      <FullCalendar ref="calendarRef" :options="calendarOptions" />
    </div>

    <!-- 事件详情弹窗 -->
    <el-dialog v-model="dialogVisible" :title="selectedPlan?.title" width="480px">
      <template v-if="selectedPlan">
        <el-descriptions :column="1" border size="small">
          <el-descriptions-item label="类型">
            {{ planTypeLabel(selectedPlan.planType) }}
          </el-descriptions-item>
          <el-descriptions-item label="优先级">
            <el-tag :type="priorityColor(selectedPlan.priority)" size="small">
              {{ priorityLabel(selectedPlan.priority) }}
            </el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="状态">
            <el-tag :type="statusColor(selectedPlan.status)" size="small">
              {{ statusLabel(selectedPlan.status) }}
            </el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="日期">{{ selectedPlan.startTime }}</el-descriptions-item>
        </el-descriptions>
      </template>
      <template #footer>
        <el-button @click="dialogVisible = false">关闭</el-button>
        <el-button type="primary" @click="goToDetail">查看详情</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, watch } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import FullCalendar from '@fullcalendar/vue3'
import dayGridPlugin from '@fullcalendar/daygrid'
import timeGridPlugin from '@fullcalendar/timegrid'
import interactionPlugin from '@fullcalendar/interaction'
import zhLocale from '@fullcalendar/core/locales/zh-cn'
import { getCalendarData } from '@/api/plan'

const router = useRouter()
const calendarRef = ref(null)
const dialogVisible = ref(false)
const selectedPlan = ref(null)
const planTypeFilter = ref('')

const planTypeMap = { DAILY: '日报', WEEKLY: '周报', MONTHLY: '月报' }
const statusMap = { DRAFT: '草稿', SUBMITTED: '已提交', APPROVING: '审批中', APPROVED: '已通过', REJECTED: '已驳回' }
const priorityMap = { HIGH: '高', MEDIUM: '中', LOW: '低' }

function planTypeLabel(t) { return planTypeMap[t] || t }
function statusLabel(s) { return statusMap[s] || s }
function statusColor(s) {
  const map = { DRAFT: 'info', SUBMITTED: '', APPROVING: 'warning', APPROVED: 'success', REJECTED: 'danger' }
  return map[s] || 'info'
}
function priorityLabel(p) { return priorityMap[p] || p }
function priorityColor(p) {
  const map = { HIGH: 'danger', MEDIUM: 'warning', LOW: '' }
  return map[p] || 'info'
}

function priorityOrder(p) {
  return p === 'HIGH' ? 0 : p === 'MEDIUM' ? 1 : 2
}

const calendarOptions = reactive({
  plugins: [dayGridPlugin, timeGridPlugin, interactionPlugin],
  initialView: 'dayGridMonth',
  locale: zhLocale,
  firstDay: 1,
  headerToolbar: {
    left: 'prev,next today',
    center: 'title',
    right: 'dayGridMonth,timeGridWeek'
  },
  buttonText: {
    today: '今天',
    month: '月',
    week: '周'
  },
  height: 'auto',
  events: fetchEvents,
  eventClick: handleEventClick,
  datesSet: reloadCalendar,
  eventDisplay: 'block',
  eventTimeFormat: {
    hour: '2-digit',
    minute: '2-digit',
    hour12: false
  }
})

async function fetchEvents({ start, end }, successCallback, failureCallback) {
  try {
    const year = start.getFullYear()
    const month = start.getMonth() + 1
    const params = { year, month }
    if (planTypeFilter.value) params.planType = planTypeFilter.value

    const res = await getCalendarData(params)
    if (res.code === 200 && res.data) {
      const events = []
      const data = res.data
      for (const [date, plans] of Object.entries(data)) {
        for (const plan of plans) {
          events.push({
            id: String(plan.id),
            title: plan.title,
            start: plan.startTime || date,
            allDay: true,
            extendedProps: {
              planType: plan.planType,
              priority: plan.priority,
              status: plan.status
            },
            backgroundColor: eventColor(plan.priority),
            borderColor: eventColor(plan.priority),
            textColor: '#fff',
            classNames: [`priority-${plan.priority.toLowerCase()}`]
          })
        }
      }
      // Sort by priority within each day
      events.sort((a, b) => {
        if (a.start !== b.start) return a.start > b.start ? 1 : -1
        return priorityOrder(a.extendedProps.priority) - priorityOrder(b.extendedProps.priority)
      })
      successCallback(events)
    } else {
      successCallback([])
    }
  } catch (err) {
    failureCallback(err)
  }
}

function eventColor(priority) {
  const map = { HIGH: '#f56c6c', MEDIUM: '#e6a23c', LOW: '#909399' }
  return map[priority] || '#409eff'
}

function handleEventClick({ event }) {
  const props = event.extendedProps
  selectedPlan.value = {
    id: event.id,
    title: event.title,
    startTime: event.startStr,
    planType: props.planType,
    priority: props.priority,
    status: props.status
  }
  dialogVisible.value = true
}

function goToDetail() {
  if (selectedPlan.value?.id) {
    dialogVisible.value = false
    router.push(`/plans/${selectedPlan.value.id}`)
  }
}

function reloadCalendar() {
  const api = calendarRef.value?.getApi()
  if (api) {
    api.refetchEvents()
  }
}
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

.calendar-card {
  background: #fff;
  padding: 16px;
  border-radius: 4px;
}

:deep(.fc) {
  font-size: 14px;
}
:deep(.fc .fc-toolbar-title) {
  font-size: 18px;
}
:deep(.fc .fc-button) {
  font-size: 13px;
  padding: 4px 12px;
}
:deep(.fc .fc-daygrid-event) {
  border-radius: 3px;
  padding: 2px 4px;
  font-size: 12px;
  cursor: pointer;
}
:deep(.fc .fc-timegrid-slot) {
  height: 40px;
}
</style>

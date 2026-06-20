<template>
  <div ref="chartRef" class="pie-chart" :style="{ height: height + 'px' }" />
</template>

<script setup>
import { ref, onMounted, watch, markRaw } from 'vue'
import * as echarts from 'echarts'

const props = defineProps({
  data: { type: Array, default: () => [] },
  title: { type: String, default: '' },
  height: { type: Number, default: 300 },
  color: { type: Array, default: () => ['#409eff', '#67c23a', '#e6a23c', '#f56c6c', '#909399'] }
})

const chartRef = ref(null)
let chart = null

function initChart() {
  if (!chartRef.value) return

  if (!chart) {
    chart = markRaw(echarts.init(chartRef.value))
  }

  const option = {
    title: {
      text: props.title,
      left: 'center',
      textStyle: { fontSize: 14 }
    },
    tooltip: {
      trigger: 'item',
      formatter: '{b}: {c} ({d}%)'
    },
    legend: {
      orient: 'vertical',
      left: 'left',
      top: 'middle'
    },
    color: props.color,
    series: [{
      type: 'pie',
      radius: ['45%', '70%'],
      center: ['60%', '55%'],
      avoidLabelOverlap: false,
      itemStyle: {
        borderRadius: 4,
        borderColor: '#fff',
        borderWidth: 2
      },
      label: {
        show: false
      },
      emphasis: {
        label: {
          show: true,
          fontSize: 14,
          fontWeight: 'bold'
        }
      },
      data: props.data
    }]
  }

  chart.setOption(option, true)
}

watch(() => props.data, () => { initChart() }, { deep: true })

onMounted(() => { initChart() })
</script>

<style scoped>
.pie-chart {
  width: 100%;
}
</style>

<template>
  <div ref="chartRef" class="bar-chart" :style="{ height: height + 'px' }" />
</template>

<script setup>
import { ref, onMounted, watch, markRaw } from 'vue'
import * as echarts from 'echarts'

const props = defineProps({
  categories: { type: Array, default: () => [] },
  series: { type: Array, default: () => [] },
  title: { type: String, default: '' },
  height: { type: Number, default: 300 },
  yAxisName: { type: String, default: '' },
  color: { type: Array, default: () => ['#409eff', '#67c23a', '#e6a23c', '#f56c6c'] },
  horizontal: { type: Boolean, default: false }
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
      trigger: 'axis',
      axisPointer: { type: 'shadow' }
    },
    legend: {
      data: props.series.map(s => s.name),
      bottom: 0
    },
    grid: {
      left: '3%',
      right: '4%',
      bottom: props.series.length > 1 ? '12%' : '3%',
      containLabel: true
    },
    color: props.color,
    xAxis: props.horizontal ? {
      type: 'value',
      name: props.yAxisName
    } : {
      type: 'category',
      data: props.categories,
      axisLabel: { rotate: 30 }
    },
    yAxis: props.horizontal ? {
      type: 'category',
      data: props.categories
    } : {
      type: 'value',
      name: props.yAxisName
    },
    series: props.series.map(s => ({
      ...s,
      type: 'bar',
      barMaxWidth: 40,
      label: {
        show: true,
        position: props.horizontal ? 'right' : 'top',
        formatter: s.labelFormatter || '{c}'
      }
    }))
  }

  chart.setOption(option, true)
}

watch(() => [props.categories, props.series], () => { initChart() }, { deep: true })

onMounted(() => { initChart() })
</script>

<style scoped>
.bar-chart {
  width: 100%;
}
</style>

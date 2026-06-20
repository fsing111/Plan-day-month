<template>
  <div ref="chartRef" class="line-chart" :style="{ height: height + 'px' }" />
</template>

<script setup>
import { ref, onMounted, watch, markRaw } from 'vue'
import * as echarts from 'echarts'

const props = defineProps({
  categories: { type: Array, default: () => [] },
  series: { type: Array, default: () => [] },
  title: { type: String, default: '' },
  height: { type: Number, default: 300 },
  yAxisName: { type: String, default: '%' },
  color: { type: Array, default: () => ['#409eff', '#67c23a', '#e6a23c'] }
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
      trigger: 'axis'
    },
    legend: {
      data: props.series.map(s => s.name),
      bottom: 0
    },
    grid: {
      left: '3%',
      right: '4%',
      bottom: '12%',
      containLabel: true
    },
    color: props.color,
    xAxis: {
      type: 'category',
      data: props.categories,
      boundaryGap: false
    },
    yAxis: {
      type: 'value',
      name: props.yAxisName,
      min: 0,
      max: 100
    },
    series: props.series.map(s => ({
      ...s,
      type: 'line',
      smooth: true,
      label: {
        show: true,
        formatter: s.labelFormatter || '{c}%'
      }
    }))
  }

  chart.setOption(option, true)
}

watch(() => [props.categories, props.series], () => { initChart() }, { deep: true })

onMounted(() => { initChart() })
</script>

<style scoped>
.line-chart {
  width: 100%;
}
</style>

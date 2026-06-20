# 模块：统计分析（statistics）

> **优先级**：P2  
> **依赖**：`common`、`auth`、`user`、`plan`、`achievement`（需要计划和成果数据）  
> **说明**：个人统计、团队统计、趋势图表

---

## 一、后端 - 统计服务

- [ ] **T01** [后端] 创建 `StatisticsService`
  - `getPersonalStats(userId, startDate, endDate, planType)`
    - 总计划数、通过数、驳回数
    - 完成率 = 已通过 / 总数 × 100%
    - 按时提交率 = 在计划开始时间之前提交的计划数 / 总数 × 100%
    - 按 planType 分组统计
    - 按 priority 分组统计
  - `getTeamStats(leaderId, startDate, endDate, planType)`
    - 团队整体完成率
    - 平均按时提交率
    - 每个成员的个人统计
    - 当天未提交日报的成员
  - `getTrend(userId, periodType, periods)`
    - 按周/按月返回各周期的完成率
    - 最近 N 个周期的趋势数据
- [ ] **T02** [后端] 创建 `StatisticsController`
  - `GET /api/v1/statistics/personal` 个人统计
  - `GET /api/v1/statistics/team` 团队统计（仅 LEADER/ADMIN 可访问）
  - `GET /api/v1/statistics/trend` 趋势数据

## 二、前端 - 个人统计

- [ ] **T03** [前端] 实现个人统计页 `views/statistics/PersonalStats.vue`
  - 筛选条件：日期范围、计划类型
  - 概览卡片：总计划数、完成率、按时提交率、驳回数
  - 饼图：计划状态分布（PieChart 组件）
  - 柱状图：按类型/优先级完成率对比（BarChart 组件）
  - 折线图：近期完成率趋势（LineChart 组件）

## 三、前端 - 团队统计（领导可见）

- [ ] **T04** [前端] 实现团队统计页 `views/statistics/TeamStats.vue`
  - 路由仅 LEADER/ADMIN 可见
  - 团队概览卡片：成员数、整体完成率、平均按时率
  - 成员完成率排行榜表格
  - 当天未提交日报人员列表（标红提示）
  - 柱状图：成员完成率对比

## 四、前端 - 图表组件

- [ ] **T05** [前端] 封装 `components/charts/PieChart.vue`
  - 基于 ECharts 饼图，接收 `{ data: [{name, value}], title }` props
- [ ] **T06** [前端] 封装 `components/charts/BarChart.vue`
  - 基于 ECharts 柱状图，接收 `{ categories, series, title }` props
- [ ] **T07** [前端] 封装 `components/charts/LineChart.vue`
  - 基于 ECharts 折线图，接收 `{ xAxis, data, title }` props

## 五、前端 - API

- [ ] **T08** [前端] 封装统计相关 API `api/statistics.js`

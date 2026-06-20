import { createRouter, createWebHistory } from 'vue-router'

const routes = [
  {
    path: '/login',
    name: 'Login',
    component: () => import('@/views/login/LoginView.vue'),
    meta: { requiresAuth: false, title: '登录' }
  },
  {
    path: '/',
    component: () => import('@/components/layout/AppLayout.vue'),
    meta: { requiresAuth: true },
    redirect: '/plans',
    children: [
      {
        path: 'plans',
        name: 'PlanList',
        component: () => import('@/views/plan/PlanList.vue'),
        meta: { title: '计划列表' }
      },
      {
        path: 'plans/create',
        name: 'PlanCreate',
        component: () => import('@/views/plan/PlanCreate.vue'),
        meta: { title: '新建计划' }
      },
      {
        path: 'plans/calendar',
        name: 'PlanCalendar',
        component: () => import('@/views/plan/PlanCalendar.vue'),
        meta: { title: '日历视图' }
      },
      {
        path: 'plans/:id',
        name: 'PlanDetail',
        component: () => import('@/views/plan/PlanDetail.vue'),
        meta: { title: '计划详情' }
      },
      {
        path: 'achievements',
        name: 'AchievementList',
        component: () => import('@/views/achievement/AchievementList.vue'),
        meta: { title: '成果列表' }
      },
      {
        path: 'achievements/submit/:planId',
        name: 'AchievementSubmit',
        component: () => import('@/views/achievement/AchievementSubmit.vue'),
        meta: { title: '提交成果' }
      },
      {
        path: 'achievements/:id',
        name: 'AchievementDetail',
        component: () => import('@/views/achievement/AchievementDetail.vue'),
        meta: { title: '成果详情' }
      },
      {
        path: 'approvals/pending',
        name: 'ApprovalPending',
        component: () => import('@/views/approval/ApprovalPending.vue'),
        meta: { title: '待审批', roles: ['LEADER', 'ADMIN'] }
      },
      {
        path: 'approvals/history',
        name: 'ApprovalHistory',
        component: () => import('@/views/approval/ApprovalHistory.vue'),
        meta: { title: '审批历史', roles: ['LEADER', 'ADMIN'] }
      },
      {
        path: 'statistics/personal',
        name: 'PersonalStats',
        component: () => import('@/views/statistics/PersonalStats.vue'),
        meta: { title: '个人统计' }
      },
      {
        path: 'statistics/team',
        name: 'TeamStats',
        component: () => import('@/views/statistics/TeamStats.vue'),
        meta: { title: '团队统计', roles: ['LEADER', 'ADMIN'] }
      },
      {
        path: 'notifications',
        name: 'NotificationList',
        component: () => import('@/views/notification/NotificationList.vue'),
        meta: { title: '通知列表' }
      },
      {
        path: 'admin/users',
        name: 'UserManage',
        component: () => import('@/views/admin/UserManage.vue'),
        meta: { title: '用户管理', roles: ['ADMIN'] }
      },
      {
        path: 'admin/departments',
        name: 'DeptManage',
        component: () => import('@/views/admin/DeptManage.vue'),
        meta: { title: '部门管理', roles: ['ADMIN'] }
      },
      {
        path: 'admin/approval-chains',
        name: 'ApprovalChainConfig',
        component: () => import('@/views/admin/ApprovalChainConfig.vue'),
        meta: { title: '审批链配置', roles: ['ADMIN'] }
      },
      {
        path: 'admin/categories',
        name: 'CategoryManage',
        component: () => import('@/views/admin/CategoryManage.vue'),
        meta: { title: '分类管理', roles: ['ADMIN'] }
      },
      {
        path: 'profile',
        name: 'Profile',
        component: () => import('@/views/profile/ProfileView.vue'),
        meta: { title: '个人中心' }
      }
    ]
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

// Navigation guard
router.beforeEach((to, from, next) => {
  const token = localStorage.getItem('token')
  if (to.meta.requiresAuth !== false && !token) {
    next('/login')
  } else if (to.path === '/login' && token) {
    next('/')
  } else {
    next()
  }
})

export default router

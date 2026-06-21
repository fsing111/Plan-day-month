<template>
  <div class="page-container">
    <div class="page-header">
      <h2>个人中心</h2>
    </div>

    <el-row :gutter="20">
      <!-- Left: User Info Card -->
      <el-col :span="8">
        <el-card shadow="never">
          <div class="profile-card">
            <div class="avatar-section">
              <el-avatar :size="80" icon="UserFilled" />
            </div>
            <div class="user-name">{{ userStore.user?.realName || '-' }}</div>
            <div class="user-role">
              <el-tag :type="roleColor(userStore.role)" size="default">
                {{ roleLabel(userStore.role) }}
              </el-tag>
            </div>
            <el-divider />
            <el-descriptions :column="1" size="small">
              <el-descriptions-item label="用户名">{{ userStore.user?.username || '-' }}</el-descriptions-item>
              <el-descriptions-item label="部门">{{ userStore.user?.deptName || '-' }}</el-descriptions-item>
              <el-descriptions-item label="直属领导">{{ userStore.user?.leaderName || '无' }}</el-descriptions-item>
              <el-descriptions-item label="邮箱">{{ userStore.user?.email || '未设置' }}</el-descriptions-item>
              <el-descriptions-item label="手机">{{ userStore.user?.phone || '未设置' }}</el-descriptions-item>
            </el-descriptions>
          </div>
        </el-card>
      </el-col>

      <!-- Right: Edit Forms -->
      <el-col :span="16">
        <!-- Change Password -->
        <el-card shadow="never" class="section-card">
          <template #header>
            <span class="card-title">修改密码</span>
          </template>
          <el-form
            ref="pwdFormRef"
            :model="pwdForm"
            :rules="pwdRules"
            label-width="100px"
            style="max-width: 400px"
          >
            <el-form-item label="旧密码" prop="oldPassword">
              <el-input v-model="pwdForm.oldPassword" type="password" show-password placeholder="请输入旧密码" />
            </el-form-item>
            <el-form-item label="新密码" prop="newPassword">
              <el-input v-model="pwdForm.newPassword" type="password" show-password placeholder="请输入新密码（至少6位）" />
            </el-form-item>
            <el-form-item label="确认密码" prop="confirmPassword">
              <el-input v-model="pwdForm.confirmPassword" type="password" show-password placeholder="请确认新密码" />
            </el-form-item>
            <el-form-item>
              <el-button type="primary" :loading="pwdSaving" @click="handleChangePassword">
                修改密码
              </el-button>
            </el-form-item>
          </el-form>
        </el-card>

        <!-- Edit Profile -->
        <el-card shadow="never" class="section-card">
          <template #header>
            <span class="card-title">基本信息</span>
          </template>
          <el-form
            ref="infoFormRef"
            :model="infoForm"
            label-width="100px"
            style="max-width: 400px"
          >
            <el-form-item label="邮箱">
              <el-input v-model="infoForm.email" placeholder="请输入邮箱" />
            </el-form-item>
            <el-form-item label="手机号">
              <el-input v-model="infoForm.phone" placeholder="请输入手机号" maxlength="20" />
            </el-form-item>
            <el-form-item>
              <el-button type="primary" :loading="infoSaving" @click="handleUpdateInfo">
                保存修改
              </el-button>
            </el-form-item>
          </el-form>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { useUserStore } from '@/stores/user'
import { getCurrentUser } from '@/api/auth'
import { updateUser } from '@/api/admin'

const userStore = useUserStore()

const pwdFormRef = ref(null)
const infoFormRef = ref(null)
const pwdSaving = ref(false)
const infoSaving = ref(false)

const pwdForm = reactive({
  oldPassword: '',
  newPassword: '',
  confirmPassword: ''
})

const pwdRules = {
  oldPassword: [{ required: true, message: '请输入旧密码', trigger: 'blur' }],
  newPassword: [
    { required: true, message: '请输入新密码', trigger: 'blur' },
    { min: 6, message: '密码至少6位', trigger: 'blur' }
  ],
  confirmPassword: [
    { required: true, message: '请确认新密码', trigger: 'blur' },
    {
      validator: (rule, value, callback) => {
        if (value !== pwdForm.newPassword) {
          callback(new Error('两次输入的密码不一致'))
        } else {
          callback()
        }
      },
      trigger: 'blur'
    }
  ]
}

const infoForm = reactive({
  email: '',
  phone: ''
})

const roleMap = { EMPLOYEE: '员工', LEADER: '领导' }
function roleLabel(r) { return roleMap[r] || r }
function roleColor(r) {
  const map = { EMPLOYEE: '', LEADER: 'warning' }
  return map[r] || 'info'
}

async function loadUserInfo() {
  try {
    const res = await getCurrentUser()
    if (res.code === 200 && res.data) {
      userStore.user = res.data
      infoForm.email = res.data.email || ''
      infoForm.phone = res.data.phone || ''
    }
  } catch {
    // Use store data as fallback
    infoForm.email = userStore.user?.email || ''
    infoForm.phone = userStore.user?.phone || ''
  }
}

async function handleChangePassword() {
  const valid = await pwdFormRef.value?.validate().catch(() => false)
  if (!valid) return

  pwdSaving.value = true
  try {
    await updateUser(userStore.user.id, {
      oldPassword: pwdForm.oldPassword,
      newPassword: pwdForm.newPassword
    })
    ElMessage.success('密码修改成功，请重新登录')
    // Clear form
    pwdForm.oldPassword = ''
    pwdForm.newPassword = ''
    pwdForm.confirmPassword = ''
    // Logout
    userStore.logout()
    window.location.href = '/login'
  } catch (error) {
    ElMessage.error(error?.message || '密码修改失败')
  } finally {
    pwdSaving.value = false
  }
}

async function handleUpdateInfo() {
  infoSaving.value = true
  try {
    await updateUser(userStore.user.id, {
      email: infoForm.email,
      phone: infoForm.phone
    })
    ElMessage.success('保存成功')
    // Refresh user info
    loadUserInfo()
  } catch (error) {
    ElMessage.error(error?.message || '保存失败')
  } finally {
    infoSaving.value = false
  }
}

onMounted(() => { loadUserInfo() })
</script>

<style scoped>
.page-container { padding: 20px; }
.page-header { margin-bottom: 20px; }
.page-header h2 { margin: 0; }

.profile-card {
  text-align: center;
}

.avatar-section {
  margin-bottom: 12px;
}

.user-name {
  font-size: 20px;
  font-weight: 600;
  color: #303133;
  margin-bottom: 8px;
}

.user-role {
  margin-bottom: 8px;
}

.section-card {
  margin-bottom: 20px;
}
.section-card:last-child { margin-bottom: 0; }

.card-title {
  font-weight: 600;
  font-size: 15px;
}
</style>

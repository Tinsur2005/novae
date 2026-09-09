<template>
  <el-container class="layout">
    <!-- 左侧菜单 -->
    <el-aside :width="isCollapse ? '64px' : '220px'" class="layout-aside">
      <!-- LOGO -->
      <div class="logo">
        <el-avatar :size="36" src="" shape="square">
          <img src="@/assets/logo.png" alt="logo"/>
        </el-avatar>
        <transition name="fade">
          <span v-show="!isCollapse" class="logo-title">盲盒商城</span>
        </transition>
      </div>
      <!-- 菜单 -->
      <el-menu
          class="layout-menu"
          :default-active="route.path"
          :collapse="isCollapse"
          :collapse-transition="false"
          router
          background-color="#001529"
          text-color="rgba(255,255,255,0.68)"
          active-text-color="#ffffff"
      >
        <el-menu-item index="/admin">
          <el-icon>
            <User/>
          </el-icon>
          <template #title>管理员管理</template>
        </el-menu-item>
        <!-- 以下菜单为后续模块预留 -->
        <el-menu-item index="/goods" disabled>
          <el-icon>
            <Goods/>
          </el-icon>
          <template #title>
            盲盒管理
            <el-tag size="small" type="info" effect="dark" class="menu-tag">开发中</el-tag>
          </template>
        </el-menu-item>
        <el-menu-item index="/order" disabled>
          <el-icon>
            <Tickets/>
          </el-icon>
          <template #title>
            订单管理
            <el-tag size="small" type="info" effect="dark" class="menu-tag">开发中</el-tag>
          </template>
        </el-menu-item>
        <el-menu-item index="/stat" disabled>
          <el-icon>
            <DataAnalysis/>
          </el-icon>
          <template #title>
            数据统计
            <el-tag size="small" type="info" effect="dark" class="menu-tag">开发中</el-tag>
          </template>
        </el-menu-item>
      </el-menu>
    </el-aside>

    <el-container>
      <!-- 顶部栏 -->
      <el-header class="layout-header">
        <div class="header-left">
          <el-icon class="collapse-btn" @click="isCollapse = !isCollapse">
            <Expand v-if="isCollapse"/>
            <Fold v-else/>
          </el-icon>
          <el-breadcrumb separator="/">
            <el-breadcrumb-item>首页</el-breadcrumb-item>
            <el-breadcrumb-item>{{ currentTitle }}</el-breadcrumb-item>
          </el-breadcrumb>
        </div>
        <div class="header-right">
          <el-dropdown @command="handleCommand">
            <span class="admin-info">
              <el-avatar :size="32" :src="adminStore.admin?.avatar || ''">
                {{ (adminStore.admin?.name || 'A').slice(0, 1).toUpperCase() }}
              </el-avatar>
              <span class="admin-name">{{ adminStore.admin?.name || '管理员' }}</span>
              <el-icon>
                <ArrowDown/>
              </el-icon>
            </span>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item command="password">
                  <el-icon>
                    <Lock/>
                  </el-icon>
                  修改密码
                </el-dropdown-item>
                <el-dropdown-item command="logout" divided>
                  <el-icon>
                    <SwitchButton/>
                  </el-icon>
                  退出登录
                </el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </div>
      </el-header>

      <!-- 内容区 -->
      <el-main class="layout-main">
        <router-view/>
      </el-main>
    </el-container>
  </el-container>

  <!-- 修改密码弹窗 -->
  <el-dialog v-model="passwordDialogVisible" title="修改密码" width="420px" destroy-on-close>
    <el-form ref="passwordFormRef" :model="passwordForm" :rules="passwordRules" label-width="80px">
      <el-form-item label="旧密码" prop="oldPassword">
        <el-input v-model="passwordForm.oldPassword" type="password" show-password placeholder="请输入旧密码"/>
      </el-form-item>
      <el-form-item label="新密码" prop="newPassword">
        <el-input v-model="passwordForm.newPassword" type="password" show-password placeholder="请输入新密码"/>
      </el-form-item>
      <el-form-item label="确认密码" prop="confirmPassword">
        <el-input v-model="passwordForm.confirmPassword" type="password" show-password placeholder="请再次输入新密码"/>
      </el-form-item>
    </el-form>
    <template #footer>
      <el-button @click="passwordDialogVisible = false">取消</el-button>
      <el-button type="primary" :loading="passwordLoading" @click="submitPassword">确定</el-button>
    </template>
  </el-dialog>
</template>

<script setup>
import {computed, reactive, ref} from 'vue'
import {useRoute, useRouter} from 'vue-router'
import {ElMessage, ElMessageBox} from 'element-plus'
import request from '@/utils/request'
import {useTokenStore} from '@/store/token.js'
import {useAdminInfoStore} from '@/store/adminInfo.js'

const route = useRoute()
const router = useRouter()
const tokenStore = useTokenStore()
const adminStore = useAdminInfoStore()

const isCollapse = ref(false)

// 面包屑标题
const titleMap = {'/admin': '管理员管理', '/goods': '盲盒管理', '/order': '订单管理', '/stat': '数据统计'}
const currentTitle = computed(() => titleMap[route.path] || '首页')

// 加载当前登录管理员信息
const loadAdminInfo = async () => {
  try {
    const res = await request.get('/admins/adminInfo')
    if (res.code === 1) {
      adminStore.setAdminInfo(res.data)
    } else {
      ElMessage.error(res.msg || '获取管理员信息失败')
    }
  } catch (e) {
    /* 拦截器已统一提示 */
  }
}
loadAdminInfo()

// 下拉菜单指令
const handleCommand = (command) => {
  if (command === 'logout') {
    logout()
  } else if (command === 'password') {
    passwordDialogVisible.value = true
  }
}

// 退出登录
const logout = () => {
  ElMessageBox.confirm('确定要退出登录吗？', '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(() => {
    tokenStore.removeToken()
    adminStore.removeAdminInfo()
    ElMessage.success('已退出登录')
    router.push('/login')
  }).catch(() => {
  })
}

// 修改密码
const passwordDialogVisible = ref(false)
const passwordLoading = ref(false)
const passwordFormRef = ref()
const passwordForm = reactive({
  oldPassword: '',
  newPassword: '',
  confirmPassword: ''
})
const passwordRules = {
  oldPassword: [{required: true, message: '请输入旧密码', trigger: 'blur'}],
  newPassword: [
    {required: true, message: '请输入新密码', trigger: 'blur'},
    {min: 6, max: 20, message: '密码长度为 6 - 20 位', trigger: 'blur'}
  ],
  confirmPassword: [
    {required: true, message: '请再次输入新密码', trigger: 'blur'},
    {
      validator: (rule, value, callback) => {
        if (value !== passwordForm.newPassword) {
          callback(new Error('两次输入的密码不一致'))
        } else {
          callback()
        }
      },
      trigger: 'blur'
    }
  ]
}

const submitPassword = () => {
  passwordFormRef.value.validate(async (valid) => {
    if (!valid) return
    passwordLoading.value = true
    try {
      const res = await request.put('/admins/resetPassword', {
        oldPassword: passwordForm.oldPassword,
        newPassword: passwordForm.newPassword
      })
      if (res.code === 1) {
        ElMessage.success('密码修改成功，请重新登录')
        passwordDialogVisible.value = false
        tokenStore.removeToken()
        adminStore.removeAdminInfo()
        router.push('/login')
      } else {
        ElMessage.error(res.msg || '密码修改失败')
      }
    } catch (e) {
      /* 拦截器已统一提示 */
    } finally {
      passwordLoading.value = false
    }
  })
}
</script>

<style scoped>
.layout {
  height: 100vh;
}

.layout-aside {
  background-color: #001529;
  transition: width 0.2s;
  overflow-x: hidden;
}

.logo {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 10px;
  height: 60px;
  overflow: hidden;
}

.logo-title {
  color: #ffffff;
  font-size: 18px;
  font-weight: bold;
  white-space: nowrap;
}

.layout-menu {
  border-right: none;
}

.menu-tag {
  margin-left: 6px;
}

.layout-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  background-color: #ffffff;
  border-bottom: 1px solid #e6e6e6;
}

.header-left {
  display: flex;
  align-items: center;
  gap: 16px;
}

.collapse-btn {
  font-size: 20px;
  cursor: pointer;
}

.collapse-btn:hover {
  color: #409eff;
}

.admin-info {
  display: flex;
  align-items: center;
  gap: 8px;
  cursor: pointer;
  outline: none;
}

.admin-name {
  color: #303133;
}

.layout-main {
  background-color: #f0f2f5;
}

.fade-enter-active,
.fade-leave-active {
  transition: opacity 0.2s;
}

.fade-enter-from,
.fade-leave-to {
  opacity: 0;
}
</style>
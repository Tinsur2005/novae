<template>
  <div class="login-page">
    <el-card class="login-card" shadow="always">
      <div class="login-logo">
        <img src="@/assets/logo.png" alt="logo" class="logo-img"/>
        <span class="logo-title">盲盒商城后台管理</span>
      </div>
      <el-form ref="formRef" :model="form" :rules="rules" size="large">
        <el-form-item prop="name">
          <el-input v-model="form.name" placeholder="请输入用户名" :prefix-icon="'User'"/>
        </el-form-item>
        <el-form-item prop="password">
          <el-input
              v-model="form.password"
              type="password"
              show-password
              placeholder="请输入密码"
              :prefix-icon="'Lock'"
              @keyup.enter="handleLogin"
          />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" class="login-btn" :loading="loading" @click="handleLogin">登 录</el-button>
        </el-form-item>
      </el-form>
    </el-card>
  </div>
</template>

<script setup>
import {reactive, ref} from 'vue'
import {useRouter} from 'vue-router'
import {ElMessage} from 'element-plus'
import request from '@/utils/request'
import {useTokenStore} from '@/store/token.js'
import {useAdminInfoStore} from '@/store/adminInfo.js'

const router = useRouter()
const tokenStore = useTokenStore()
const adminStore = useAdminInfoStore()

const loading = ref(false)
const formRef = ref()
const form = reactive({
  name: '',
  password: ''
})
const rules = {
  name: [{required: true, message: '请输入用户名', trigger: 'blur'}],
  password: [{required: true, message: '请输入密码', trigger: 'blur'}]
}

const handleLogin = () => {
  formRef.value.validate(async (valid) => {
    if (!valid) return
    loading.value = true
    try {
      const res = await request.post('/admins/login', form)
      if (res.code === 1) {
        tokenStore.setToken(res.data)
        adminStore.removeAdminInfo()
        ElMessage.success('登录成功')
        router.push('/')
      } else {
        ElMessage.error(res.msg || '登录失败')
      }
    } catch (e) {
      /* 拦截器已统一提示 */
    } finally {
      loading.value = false
    }
  })
}
</script>

<style scoped>
.login-page {
  display: flex;
  align-items: center;
  justify-content: center;
  height: 100vh;
  background: linear-gradient(135deg, #1f2d3d 0%, #2b4b6b 100%);
}

.login-card {
  width: 380px;
  border-radius: 8px;
}

.login-logo {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 10px;
  margin-bottom: 24px;
}

.logo-img {
  width: 40px;
  height: 40px;
}

.logo-title {
  font-size: 20px;
  font-weight: bold;
  color: #303133;
}

.login-btn {
  width: 100%;
}
</style>

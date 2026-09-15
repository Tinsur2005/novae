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
          />
        </el-form-item>
        <el-form-item prop="captcha">
          <div class="captcha-row">
            <el-input v-model="form.captcha" placeholder="请输入验证码" :prefix-icon="'Key'"
                      @keyup.enter="handleLogin"/>
            <img class="captcha-img" :src="captchaSrc" alt="验证码" title="点击刷新" @click="refreshCaptcha">
          </div>
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
import adminApi from '@/api/admin/admin.js'
import captchaApi from '@/api/service/captcha.js'
import {useTokenStore} from '@/store/token.js'
import {useAdminInfoStore} from '@/store/adminInfo.js'

const router = useRouter()
const tokenStore = useTokenStore()
const adminStore = useAdminInfoStore()

const loading = ref(false)
const formRef = ref()
const form = reactive({
  name: '',
  password: '',
  captcha: '',
  uuid: ''
})
const rules = {
  name: [{required: true, message: '请输入用户名', trigger: 'blur'}],
  password: [{required: true, message: '请输入密码', trigger: 'blur'}],
  captcha: [
    {required: true, message: '请输入验证码', trigger: 'blur'},
    {min: 4, max: 4, message: '验证码为4位字符', trigger: 'blur'}
  ]
}

//验证码图片地址，加时间戳参数防止浏览器缓存，点击图片刷新
const captchaSrc = ref('')
const refreshCaptcha = () => {
  captchaApi.captcha().then(result => {
    captchaSrc.value = result.data.captcha
    form.uuid = result.data.uuid
  })
}
refreshCaptcha()

const handleLogin = () => {
  formRef.value.validate(async (valid) => {
    if (!valid) return
    loading.value = true
    try {
      const res = await adminApi.login(form)
      if (res.code === 1) {
        tokenStore.setToken(res.data)
        adminStore.removeAdminInfo()
        ElMessage.success('登录成功')
        router.push('/')
      } else {
        ElMessage.error(res.msg || '登录失败')
        //验证码是一次性的，登录失败后刷新图片重新获取
        refreshCaptcha()
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

/*验证码输入框和图片同一行显示*/
.captcha-row {
  display: flex;
  width: 100%;
  gap: 8px;
}

.captcha-img {
  height: 40px;
  width: 120px;
  cursor: pointer;
  border-radius: 4px;
}
</style>

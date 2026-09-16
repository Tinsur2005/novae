<script setup>
  import {ref} from "vue";
  import userApi from "@/api/user/user.js";
  import {showToast, showSuccessToast} from "vant";
  import {useRoute, useRouter} from 'vue-router'
  import {useTokenStore} from '@/store/token.js'
  import {useCartCountStore} from '@/store/cartCount.js'

  const route = useRoute()
  const router = useRouter()
  const tokenStore = useTokenStore();
  const cartCountStore = useCartCountStore()

  const loginInfo = ref({
    name: '',
    password: ''
  })

  const login = () => {
    userApi.login(loginInfo.value).then(result => {
      if (result.code === 1) {
        showSuccessToast(result.msg || '登录成功')
        tokenStore.setToken(result.data)
        //登录后刷新购物车角标
        cartCountStore.refreshCount()
        //跳回原本想访问的页面，没有则去首页
        router.push(route.query.redirect || '/')
      } else {
        showToast(result.msg || '登录失败')
      }
    })
  }

  //表单校验模型
  const rules = ref({
    name: [
      {required: true, message: '请输入用户名'},
      {min: 2, max: 16, message: '用户名的长度必须为2~16位'}
    ],
    password: [
      {required: true, message: '请输入密码'},
      {min: 3, max: 16, message: '密码长度必须为3~16位'}
    ]
  })

  const submit = (values) => {
    login()
  }

  //去注册
  const toRegister = () => router.push('/register')
</script>

<template>
  <div class="login">
    <!-- 渐变背景上的装饰泡泡 -->
    <div class="bubble bubble-1"></div>
    <div class="bubble bubble-2"></div>
    <div class="bubble bubble-3"></div>

    <!-- 商城Logo：盲盒造型 -->
    <div class="login-logo">
      <van-icon name="gift-o" color="#fff" size="38"/>
    </div>
    <h1 class="login-title">TinsurMall</h1>
    <p class="login-subtitle">开启属于你的惊喜时刻</p>

    <!-- 登录表单 -->
    <van-form class="login-form" @submit="submit">
      <van-cell-group inset round>
        <van-field v-model="loginInfo.name" name="name" label="用户名" placeholder="请输入用户名"
                   left-icon="contact" :rules="rules.name" clearable/>
        <van-field v-model="loginInfo.password" name="password" label="密码" placeholder="请输入密码"
                   left-icon="lock" type="password" :rules="rules.password" @keyup.enter="login"/>
      </van-cell-group>
      <div class="login-button-wrap">
        <van-button round block type="primary" class="gradient-button" native-type="submit">
          登 录
        </van-button>
      </div>
    </van-form>

    <p class="login-register">还没有账号？<span @click="toRegister">立即注册</span></p>
  </div>
</template>

<style scoped>
  .login {
    position: relative;
    min-height: 100vh;
    display: flex;
    flex-direction: column;
    align-items: center;
    padding-top: 90px;
    overflow: hidden;
    background: var(--app-gradient-light);
  }

  /* 漂浮的装饰泡泡，增加潮玩氛围 */
  .bubble {
    position: absolute;
    border-radius: 50%;
    opacity: 0.5;
    pointer-events: none;
  }

  .bubble-1 {
    width: 120px;
    height: 120px;
    top: -30px;
    right: -30px;
    background: linear-gradient(135deg, rgba(255, 110, 196, 0.5), rgba(168, 84, 247, 0.4));
  }

  .bubble-2 {
    width: 70px;
    height: 70px;
    top: 150px;
    left: -20px;
    background: linear-gradient(135deg, rgba(168, 84, 247, 0.35), rgba(255, 110, 196, 0.3));
  }

  .bubble-3 {
    width: 44px;
    height: 44px;
    top: 60px;
    left: 70px;
    background: linear-gradient(135deg, rgba(255, 110, 196, 0.4), rgba(255, 200, 100, 0.3));
  }

  .login-logo {
    width: 76px;
    height: 76px;
    border-radius: 24px;
    background: var(--app-gradient);
    display: flex;
    align-items: center;
    justify-content: center;
    font-size: 38px;
    box-shadow: 0 8px 20px rgba(168, 84, 247, 0.35);
  }

  .login-title {
    margin: 16px 0 4px;
    font-size: 24px;
    letter-spacing: 1px;
    background: var(--app-gradient);
    -webkit-background-clip: text;
    background-clip: text;
    color: transparent;
  }

  .login-subtitle {
    margin: 0 0 32px;
    font-size: 13px;
    color: #999;
  }

  .login-form {
    width: 100%;
  }

  .login-button-wrap {
    margin: 24px 16px 0;
  }

  .login-register {
    margin-top: 20px;
    font-size: 13px;
    color: #999;
  }

  .login-register span {
    color: var(--app-primary);
    font-weight: 600;
  }
</style>

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

  //关闭登录页，返回上一个页面
  const goBack = () => router.back()
</script>

<template>
  <div class="auth-page">
    <!-- 左上角关闭 -->
    <van-icon name="cross" class="auth-close" @click="goBack"/>

    <!-- 大标题 -->
    <h1 class="auth-title">账号登录</h1>

    <!-- 登录表单 -->
    <van-form class="auth-form" @submit="submit">
      <div class="field-block">
        <div class="field-label">用户名</div>
        <van-field v-model="loginInfo.name" name="name" placeholder="请输入用户名"
                   :border="false" :rules="rules.name" clearable/>
      </div>
      <div class="field-block">
        <div class="field-label">密码</div>
        <van-field v-model="loginInfo.password" name="password" placeholder="请输入密码"
                   :border="false" type="password" :rules="rules.password" @keyup.enter="login"/>
      </div>
      <van-button round block native-type="submit" class="auth-button">
        登 录
      </van-button>
    </van-form>

    <p class="auth-switch">- 还没有账号？<span @click="toRegister">立即注册</span> -</p>
  </div>
</template>

<style scoped>
  .auth-page {
    min-height: 100vh;
    display: flex;
    flex-direction: column;
    padding: 16px 28px 40px;
    box-sizing: border-box;
    background: #fff;
  }

  .auth-close {
    align-self: flex-start;
    font-size: 24px;
    color: #1a1a1a;
    cursor: pointer;
  }

  .auth-title {
    margin: 96px 0 0;
    font-size: 30px;
    font-weight: 700;
    color: #1a1a1a;
  }

  .auth-form {
    margin-top: 56px;
  }

  /* 下划线风格的字段块：上方小灰 label + 无框输入 + 底部分隔线 */
  .field-block {
    margin-bottom: 36px;
  }

  .field-label {
    font-size: 14px;
    color: #999;
  }

  .field-block :deep(.van-field) {
    padding: 10px 0 12px;
    background: transparent;
  }

  .field-block :deep(.van-field__control) {
    font-size: 18px;
    color: #1a1a1a;
  }

  .field-block::after {
    content: '';
    display: block;
    height: 1px;
    background: #ececec;
  }

  /* 大胶囊提交按钮 */
  .auth-button {
    margin-top: 32px;
    height: 52px;
    font-size: 17px;
    font-weight: 600;
    border: none;
    background: var(--app-primary);
    color: #fff;
  }

  .auth-switch {
    margin-top: auto;
    padding-top: 40px;
    text-align: center;
    font-size: 13px;
    color: #999;
  }

  .auth-switch span {
    color: #1a1a1a;
    font-weight: 600;
  }
</style>
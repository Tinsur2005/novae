<script setup>
  import {ref} from "vue";
  import userApi from "@/api/user/user.js";
  import {showToast, showSuccessToast} from "vant";
  import {useRouter} from 'vue-router'

  const router = useRouter()

  const registerInfo = ref({
    name: '',
    password: '',
    confirmPassword: ''
  })

  //表单校验模型
  const rules = ref({
    name: [
      {required: true, message: '请输入用户名'},
      {min: 2, max: 16, message: '用户名的长度必须为2~16位'}
    ],
    password: [
      {required: true, message: '请输入密码'},
      {min: 3, max: 16, message: '密码长度必须为3~16位'}
    ],
    confirmPassword: [
      {required: true, message: '请再次输入密码'},
      {validator: (value) => value === registerInfo.value.password, message: '两次输入的密码不一致'}
    ]
  })

  const submit = (values) => {
    //确认密码只用于校验，不提交给后台
    const {confirmPassword, ...user} = values
    userApi.register(user).then(result => {
      if (result.code === 1) {
        showSuccessToast(result.msg || '注册成功')
        //注册成功跳登录页
        router.push('/login')
      } else {
        showToast(result.msg || '注册失败')
      }
    })
  }

  //去登录
  const toLogin = () => router.push('/login')
</script>

<template>
  <div class="register">
    <!-- 渐变背景上的装饰泡泡 -->
    <div class="bubble bubble-1"></div>
    <div class="bubble bubble-2"></div>

    <!-- 商城Logo：盲盒造型 -->
    <div class="register-logo">
      <van-icon name="smile-o" color="#fff" size="38"/>
    </div>
    <h1 class="register-title">创建账号</h1>
    <p class="register-subtitle">注册后即可开启惊喜盲盒之旅</p>

    <!-- 注册表单 -->
    <van-form class="register-form" @submit="submit">
      <van-cell-group inset round>
        <van-field v-model="registerInfo.name" name="name" label="用户名" placeholder="请输入用户名"
                   left-icon="contact" :rules="rules.name" clearable/>
        <van-field v-model="registerInfo.password" name="password" label="密码" placeholder="请输入密码"
                   left-icon="lock" type="password" :rules="rules.password"/>
        <van-field v-model="registerInfo.confirmPassword" name="confirmPassword" label="确认密码" placeholder="请再次输入密码"
                   left-icon="lock" type="password" :rules="rules.confirmPassword"/>
      </van-cell-group>
      <div class="register-button-wrap">
        <van-button round block type="primary" class="gradient-button" native-type="submit">
          注 册
        </van-button>
      </div>
    </van-form>

    <p class="register-login">已有账号？<span @click="toLogin">去登录</span></p>
  </div>
</template>

<style scoped>
  .register {
    position: relative;
    min-height: 100vh;
    display: flex;
    flex-direction: column;
    align-items: center;
    padding-top: 80px;
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
    width: 110px;
    height: 110px;
    top: -20px;
    left: -30px;
    background: linear-gradient(135deg, rgba(255, 110, 196, 0.5), rgba(168, 84, 247, 0.4));
  }

  .bubble-2 {
    width: 64px;
    height: 64px;
    top: 170px;
    right: -14px;
    background: linear-gradient(135deg, rgba(168, 84, 247, 0.35), rgba(255, 110, 196, 0.3));
  }

  .register-logo {
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

  .register-title {
    margin: 16px 0 4px;
    font-size: 22px;
    background: var(--app-gradient);
    -webkit-background-clip: text;
    background-clip: text;
    color: transparent;
  }

  .register-subtitle {
    margin: 0 0 28px;
    font-size: 13px;
    color: #999;
  }

  .register-form {
    width: 100%;
  }

  .register-button-wrap {
    margin: 24px 16px 0;
  }

  .register-login {
    margin-top: 20px;
    font-size: 13px;
    color: #999;
  }

  .register-login span {
    color: var(--app-primary);
    font-weight: 600;
  }
</style>

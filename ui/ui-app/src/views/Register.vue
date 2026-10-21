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

  //关闭注册页，返回上一个页面
  const goBack = () => router.back()
</script>

<template>
  <div class="auth-page">
    <!-- 左上角关闭 -->
    <van-icon name="cross" class="auth-close" @click="goBack"/>

    <!-- 大标题 -->
    <h1 class="auth-title">创建账号</h1>

    <!-- 注册表单 -->
    <van-form class="auth-form" @submit="submit">
      <div class="field-block">
        <div class="field-label">用户名</div>
        <van-field v-model="registerInfo.name" name="name" placeholder="请输入用户名"
                   :border="false" :rules="rules.name" clearable/>
      </div>
      <div class="field-block">
        <div class="field-label">密码</div>
        <van-field v-model="registerInfo.password" name="password" placeholder="请输入密码"
                   :border="false" type="password" :rules="rules.password"/>
      </div>
      <div class="field-block">
        <div class="field-label">确认密码</div>
        <van-field v-model="registerInfo.confirmPassword" name="confirmPassword" placeholder="请再次输入密码"
                   :border="false" type="password" :rules="rules.confirmPassword"/>
      </div>
      <van-button round block native-type="submit" class="auth-button">
        注 册
      </van-button>
    </van-form>

    <p class="auth-switch">- 已有账号？<span @click="toLogin">去登录</span> -</p>
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

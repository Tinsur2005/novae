<script setup>
  import {ref, onMounted} from 'vue'
  import {showToast, showSuccessToast} from 'vant'
  import userApi from '@/api/user/user.js'
  import PageHeader from '@/components/PageHeader.vue'
  import request from '@/utils/request.js'
  import {useUserInfoStore} from '@/store/userInfo.js'
  import {formatImage} from '@/utils/format.js'

  const userInfoStore = useUserInfoStore()

  //个人信息表单
  const user = ref({})
  onMounted(() => {
    userApi.userInfo().then(result => {
      if (result.code === 1) {
        user.value = result.data || {}
      }
    })
  })

  //选择头像后上传到OSS，成功后回填图片地址
  const afterReadAvatar = (file) => {
    const formData = new FormData()
    formData.append('file', file.file)
    formData.append('folder', 'avatar')
    request.post('/service/upload', formData).then(result => {
      if (result.code === 1) {
        user.value.avatar = result.data
        showSuccessToast('头像上传成功')
      } else {
        showToast(result.msg || '头像上传失败')
      }
    }).finally(() => {
      //无论成功失败都清空预览，头像展示以user.avatar为准
      fileList.value = []
    })
  }
  //van-uploader的文件列表，只做选择文件的载体
  const fileList = ref([])

  //表单校验模型
  const rules = ref({
    name: [
      {required: true, message: '请输入用户名'},
      {min: 2, max: 16, message: '用户名的长度必须为2~16位'}
    ],
    phone: [
      {pattern: /^1\d{10}$/, message: '手机号格式不正确'}
    ],
    email: [
      {validator: (value) => !value || /^[\w.-]+@[\w-]+(\.[\w-]+)+$/.test(value), message: '邮箱格式不正确'}
    ]
  })

  //保存个人信息
  const submit = (values) => {
    userApi.update(user.value).then(result => {
      if (result.code === 1) {
        showSuccessToast(result.msg || '保存成功')
        //同步个人中心页面的用户信息
        userInfoStore.setUserInfo(user.value)
      } else {
        showToast(result.msg || '保存失败')
      }
    })
  }
</script>

<template>
  <div class="profile pb-action">
    <PageHeader title="个人信息"/>

    <!-- 头像上传 -->
    <div class="avatar-area">
      <van-uploader :after-read="afterReadAvatar" v-model="fileList" :max-count="1" :deletable="false">
        <div class="avatar-wrap">
          <img v-if="user.avatar" class="avatar" :src="formatImage(user.avatar)" alt="头像">
          <div v-else class="avatar avatar-default">
            <van-icon name="user-o" size="30" color="#c0c4cc"/>
          </div>
          <div class="avatar-camera">
            <van-icon name="photograph" color="#fff" size="14"/>
          </div>
        </div>
      </van-uploader>
      <span class="avatar-tip">点击更换头像</span>
    </div>

    <!-- 资料表单 -->
    <van-form class="profile-form" @submit="submit">
      <van-cell-group inset round>
        <van-field v-model="user.name" label="用户名" placeholder="请输入用户名" :rules="rules.name" clearable/>
        <van-field v-model="user.phone" label="手机号" placeholder="请输入手机号" maxlength="11" type="tel" :rules="rules.phone"/>
        <van-field v-model="user.email" label="邮箱" placeholder="请输入邮箱" :rules="rules.email"/>
      </van-cell-group>
      <div class="save-button-wrap">
        <van-button round block type="primary" class="gradient-button" native-type="submit">保存</van-button>
      </div>
    </van-form>
  </div>
</template>

<style scoped>
  .avatar-area {
    display: flex;
    flex-direction: column;
    align-items: center;
    padding: 24px 0 8px;
  }

  .avatar-wrap {
    position: relative;
  }

  .avatar {
    width: 72px;
    height: 72px;
    border-radius: 50%;
    object-fit: cover;
  }

  .avatar-default {
    display: flex;
    align-items: center;
    justify-content: center;
    font-size: 30px;
    background: var(--app-gradient-light);
  }

  /* 右下角的相机角标 */
  .avatar-camera {
    position: absolute;
    right: 0;
    bottom: 0;
    width: 24px;
    height: 24px;
    border-radius: 50%;
    display: flex;
    align-items: center;
    justify-content: center;
    background: var(--app-gradient);
    border: 2px solid #fff;
    box-sizing: content-box;
  }

  .avatar-tip {
    display: block;
    margin-top: 6px;
    font-size: 12px;
    color: #999;
    text-align: center;
  }

  .save-button-wrap {
    margin: 24px 16px 0;
  }
</style>

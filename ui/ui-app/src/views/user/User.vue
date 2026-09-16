<script setup>
  import {computed, onMounted} from 'vue'
  import {useRouter} from 'vue-router'
  import {showToast, showConfirmDialog, showSuccessToast} from 'vant'
  import userApi from '@/api/user/user.js'
  import {useTokenStore} from '@/store/token.js'
  import {useUserInfoStore} from '@/store/userInfo.js'
  import {useCartCountStore} from '@/store/cartCount.js'
  import {formatImage} from '@/utils/format.js'

  const router = useRouter()
  const tokenStore = useTokenStore()
  const userInfoStore = useUserInfoStore()
  const cartCountStore = useCartCountStore()

  //用户信息（store持久化，进入页面时再刷新一次）
  const user = computed(() => userInfoStore.user || {})

  onMounted(() => {
    userApi.userInfo().then(result => {
      if (result.code === 1) {
        userInfoStore.setUserInfo(result.data)
      }
    })
  })

  //菜单配置
  const menus = [
    {title: '个人信息', icon: 'user-o', path: '/user/profile'},
    {title: '收货地址', icon: 'location-o', path: '/shipping'},
    {title: '我的订单', icon: 'orders-o', path: '/order'}
  ]

  //退出登录
  const logout = () => {
    showConfirmDialog({
      title: '提示',
      message: '确认退出登录吗？',
      confirmButtonText: '确认',
      cancelButtonText: '取消'
    }).then(() => {
      tokenStore.removeToken()
      userInfoStore.removeUserInfo()
      cartCountStore.clear()
      showSuccessToast('已退出登录')
      router.push('/login')
    }).catch(() => {
    })
  }
</script>

<template>
  <div class="user pb-tab">
    <!-- 用户信息卡片：渐变背景 -->
    <div class="user-card">
      <div class="user-deco user-deco-1"></div>
      <div class="user-deco user-deco-2"></div>
      <img v-if="user.avatar" class="user-avatar" :src="formatImage(user.avatar)" alt="头像">
      <div v-else class="user-avatar user-avatar-default">
        <van-icon name="user-o" size="30" color="#fff"/>
      </div>
      <div class="user-info">
        <div class="user-name">{{ user.name || '盲盒玩家' }}</div>
        <div class="user-contact">
          <span v-if="user.phone">{{ user.phone }}</span>
          <span v-if="user.phone && user.email"> | </span>
          <span v-if="user.email">{{ user.email }}</span>
          <span v-if="!user.phone && !user.email">点击下方编辑个人信息</span>
        </div>
      </div>
    </div>

    <!-- 功能菜单 -->
    <div class="menu-card card">
      <div v-for="menu in menus" :key="menu.path" class="menu-item" @click="router.push(menu.path)">
        <div class="menu-icon-wrap">
          <van-icon class="menu-icon" :name="menu.icon" size="18"/>
        </div>
        <span class="menu-title">{{ menu.title }}</span>
        <van-icon class="menu-arrow" name="arrow"/>
      </div>
    </div>

    <!-- 退出登录 -->
    <div class="logout-card card" @click="logout">
      <van-icon name="exchange" size="18"/>
      <span>退出登录</span>
    </div>

    <p class="copyright">TinsurMall 盲盒商城 ©2026</p>
  </div>
</template>

<style scoped>
  /* 渐变用户卡片 */
  .user-card {
    position: relative;
    display: flex;
    align-items: center;
    gap: 14px;
    padding: 30px 16px 26px;
    background: var(--app-gradient);
    border-radius: 0 0 20px 20px;
    overflow: hidden;
  }

  /* 卡片上的装饰泡泡 */
  .user-deco {
    position: absolute;
    border-radius: 50%;
    background: rgba(255, 255, 255, 0.15);
    pointer-events: none;
  }

  .user-deco-1 {
    width: 90px;
    height: 90px;
    top: -30px;
    right: 30px;
  }

  .user-deco-2 {
    width: 50px;
    height: 50px;
    bottom: -16px;
    right: -10px;
  }

  .user-avatar {
    width: 56px;
    height: 56px;
    border-radius: 50%;
    object-fit: cover;
    flex-shrink: 0;
    border: 2px solid rgba(255, 255, 255, 0.7);
  }

  .user-avatar-default {
    display: flex;
    align-items: center;
    justify-content: center;
    font-size: 26px;
    background: rgba(255, 255, 255, 0.25);
  }

  .user-name {
    font-size: 18px;
    font-weight: 700;
    color: #fff;
  }

  .user-contact {
    margin-top: 4px;
    font-size: 12px;
    color: rgba(255, 255, 255, 0.85);
  }

  .menu-card {
    margin: 10px 8px 0;
    overflow: hidden;
  }

  .menu-item {
    display: flex;
    align-items: center;
    gap: 10px;
    padding: 15px 14px;
    border-bottom: 1px solid #f5f5f5;
  }

  .menu-item:last-child {
    border-bottom: none;
  }

  /* 菜单图标用浅紫圆角底，更有潮玩感 */
  .menu-icon-wrap {
    width: 32px;
    height: 32px;
    border-radius: 10px;
    display: flex;
    align-items: center;
    justify-content: center;
    background: var(--app-gradient-light);
  }

  .menu-icon {
    color: var(--app-primary);
  }

  .menu-title {
    font-size: 15px;
    color: #303133;
  }

  .menu-arrow {
    margin-left: auto;
    color: #c0c4cc;
  }

  .logout-card {
    display: flex;
    align-items: center;
    justify-content: center;
    gap: 6px;
    margin: 8px;
    padding: 15px 0;
    font-size: 15px;
    color: var(--app-primary);
  }

  .copyright {
    margin-top: 24px;
    text-align: center;
    font-size: 11px;
    color: #c0c4cc;
  }
</style>

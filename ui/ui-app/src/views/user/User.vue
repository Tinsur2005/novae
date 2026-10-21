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

  //订单状态入口，点击跳订单列表对应tab
  const orderEntries = [
    {title: '待付款', icon: 'credit-pay', tab: -1},
    {title: '待发货', icon: 'tosend', tab: 1},
    {title: '待收货', icon: 'logistics', tab: 2},
    {title: '已完成', icon: 'completed', tab: 3}
  ]

  //服务菜单
  const menus = [
    {title: '个人信息', icon: 'user-o', path: '/user/profile'},
    {title: '收货地址', icon: 'location-o', path: '/shipping'}
  ]

  //去订单列表的指定状态tab
  const toOrder = (tab) => router.push(tab === undefined ? '/order' : '/order?tab=' + tab)

  //头像：去个人信息
  const toProfile = () => router.push('/user/profile')

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
    <!-- 顶部用户区：头像 + 用户名 + 设置按钮 -->
    <div class="user-header">
      <img v-if="user.avatar" class="user-avatar" :src="formatImage(user.avatar)" alt="头像" @click="toProfile">
      <div v-else class="user-avatar user-avatar-default" @click="toProfile">
        <van-icon name="user-o" size="28" color="#c0c4cc"/>
      </div>
      <div class="user-info">
        <div class="user-name">{{ user.name || '盲盒玩家' }}</div>
        <div class="user-contact">
          <span v-if="user.phone">{{ user.phone }}</span>
          <span v-if="user.phone && user.email"> | </span>
          <span v-if="user.email">{{ user.email }}</span>
          <span v-if="!user.phone && !user.email">点击头像编辑个人信息</span>
        </div>
      </div>
    </div>

    <!-- 我的订单卡片：全部入口 + 四个状态 -->
    <div class="order-card card">
      <div class="card-head" @click="toOrder()">
        <span class="card-title">我的订单</span>
        <span class="card-more">全部 <van-icon name="arrow"/></span>
      </div>
      <div class="order-entries">
        <div v-for="entry in orderEntries" :key="entry.title" class="entry-item" @click="toOrder(entry.tab)">
          <van-icon :name="entry.icon" size="26"/>
          <span class="entry-title">{{ entry.title }}</span>
        </div>
      </div>
    </div>

    <!-- 服务菜单卡片 -->
    <div class="menu-card card">
      <div class="card-head">
        <span class="card-title">我的服务</span>
      </div>
      <div v-for="menu in menus" :key="menu.path" class="menu-item" @click="router.push(menu.path)">
        <van-icon class="menu-icon" :name="menu.icon" size="18"/>
        <span class="menu-title">{{ menu.title }}</span>
        <van-icon class="menu-arrow" name="arrow"/>
      </div>
    </div>

    <!-- 退出登录 -->
    <div class="logout-card card" @click="logout">
      <van-icon name="exchange" size="18"/>
      <span>退出登录</span>
    </div>

    <p class="copyright">Novae星绽 ©2026</p>
  </div>
</template>

<style scoped>
  /* 顶部用户区 */
  .user-header {
    display: flex;
    align-items: center;
    gap: 14px;
    padding: 32px 16px 20px;
  }

  .user-avatar {
    width: 60px;
    height: 60px;
    border-radius: 50%;
    object-fit: cover;
    flex-shrink: 0;
    border: 2px solid #fff;
    background: #fff;
    cursor: pointer;
  }

  .user-avatar-default {
    display: flex;
    align-items: center;
    justify-content: center;
  }

  .user-info {
    flex: 1;
    min-width: 0;
  }

  .user-name {
    font-size: 19px;
    font-weight: 700;
    color: #1a1a1a;
  }

  .user-contact {
    margin-top: 4px;
    font-size: 12px;
    color: #999;
  }

  /* 卡片头：标题 + 更多入口 */
  .card-head {
    display: flex;
    align-items: center;
    justify-content: space-between;
    padding: 16px 14px 4px;
    cursor: pointer;
  }

  .card-title {
    font-size: 16px;
    font-weight: 700;
    color: #1a1a1a;
  }

  .card-more {
    display: flex;
    align-items: center;
    gap: 2px;
    font-size: 13px;
    color: #999;
  }

  /* 订单四宫格 */
  .order-card {
    margin: 0 10px;
  }

  .order-entries {
    display: flex;
    padding: 8px 0 18px;
  }

  .entry-item {
    flex: 1;
    display: flex;
    flex-direction: column;
    align-items: center;
    gap: 8px;
    color: #1a1a1a;
    cursor: pointer;
  }

  .entry-title {
    font-size: 12px;
    color: #666;
  }

  /* 服务菜单 */
  .menu-card {
    margin: 10px 10px 0;
    overflow: hidden;
  }

  .menu-card .card-head {
    cursor: default;
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

  .menu-icon {
    color: #1a1a1a;
  }

  .menu-title {
    font-size: 15px;
    color: #303133;
  }

  .menu-arrow {
    margin-left: auto;
    color: #c0c4cc;
  }

  /* 退出登录 */
  .logout-card {
    display: flex;
    align-items: center;
    justify-content: center;
    gap: 6px;
    margin: 12px 10px 0;
    padding: 15px 0;
    font-size: 15px;
    color: #1a1a1a;
  }

  .copyright {
    margin-top: 24px;
    text-align: center;
    font-size: 11px;
    color: #c0c4cc;
  }
</style>

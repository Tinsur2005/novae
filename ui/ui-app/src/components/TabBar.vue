<script setup>
  import {useCartCountStore} from '@/store/cartCount.js'

  const cartCountStore = useCartCountStore()

  //底部导航的四个标签页，route模式会自动高亮当前路由
  const tabs = [
    {path: '/home', title: '首页', icon: 'wap-home-o'},
    {path: '/category', title: '分类', icon: 'apps-o'},
    {path: '/cart', title: '购物车', icon: 'shopping-cart-o'},
    {path: '/user', title: '我的', icon: 'user-o'}
  ]
</script>

<template>
  <!-- 固定定位的van-tabbar，route模式根据当前路由自动选中 -->
  <van-tabbar route safe-area-inset-bottom class="app-fixed">
    <van-tabbar-item v-for="tab in tabs" :key="tab.path" replace :to="tab.path" :icon="tab.icon">
      {{ tab.title }}
      <!-- 购物车角标：显示购物车商品总数量，数量为0时不显示 -->
      <template v-if="tab.path === '/cart'" #icon>
        <div class="tab-icon">
          <van-icon name="shopping-cart-o"/>
          <span v-if="cartCountStore.count > 0" class="tab-badge">
            {{ cartCountStore.count > 99 ? '99+' : cartCountStore.count }}
          </span>
        </div>
      </template>
    </van-tabbar-item>
  </van-tabbar>
</template>

<style scoped>
  .tab-icon {
    position: relative;
    line-height: 1;
  }

  /* 购物车角标用品牌渐变底色，配合盲盒主题 */
  .tab-badge {
    position: absolute;
    top: -5px;
    left: 12px;
    min-width: 16px;
    height: 16px;
    line-height: 16px;
    padding: 0 4px;
    box-sizing: border-box;
    background: var(--app-gradient);
    color: #fff;
    font-size: 10px;
    text-align: center;
    border-radius: 8px;
  }
</style>

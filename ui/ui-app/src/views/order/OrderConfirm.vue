<script setup>
  import PageHeader from '@/components/PageHeader.vue'
</script>

<template>
  <div class="order-confirm">
    <PageHeader title="确认订单"/>
    <!-- 订单模块后端尚未开发，先做占位页面 -->
    <van-empty image="search" description="订单模块开发中，敬请期待">
      <van-button round type="primary" class="gradient-button" to="/cart">返回购物车</van-button>
    </van-empty>
  </div>
</template>

<style scoped>
</style>

<script setup>
  import {useRouter} from 'vue-router'

  //props：title标题，back是否显示返回按钮
  const props = defineProps({
    title: String,
    back: {
      type: Boolean,
      default: true
    }
  })

  const router = useRouter()
  const goBack = () => router.back()
</script>

<template>
  <!-- 吸顶的页面头部：返回按钮 + 居中标题 + 右侧插槽 -->
  <header class="page-header">
    <van-nav-bar :title="title" :left-arrow="back" @click-left="goBack">
      <template #right>
        <slot name="right"></slot>
      </template>
    </van-nav-bar>
  </header>
</template>

<style scoped>
  .page-header {
    position: sticky;
    top: 0;
    z-index: 100;
  }
</style>

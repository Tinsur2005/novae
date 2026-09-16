<script setup>
  import {ref, onUnmounted, watch} from 'vue'
  import {useRoute} from 'vue-router'
  import productApi from '@/api/product/product.js'
  import PageHeader from '@/components/PageHeader.vue'
  import ProductCard from '@/components/ProductCard.vue'

  const route = useRoute()

  //列表数据
  const list = ref([])
  //分页信息和搜索条件
  const productQuery = ref({
    name: '',
    categoryId: null,
    page: 1,
    limit: 10,
    //前台只看上架商品
    status: 1
  })
  //van-list的加载状态：loading加载中，finished全部加载完不再触发
  const loading = ref(false)
  const finished = ref(false)

  //页面标题：搜索关键字或分类名称
  const title = ref('商品列表')

  const onLoad = () => {
    productApi.list(productQuery.value).then(result => {
      if (result.code === 1) {
        //追加到列表后面实现无限加载
        list.value = [...list.value, ...(result.data.records || [])]
        //加载完所有数据后告诉van-list不再触发加载
        if (list.value.length >= result.data.total) {
          finished.value = true
        }
      } else {
        finished.value = true
      }
      //本次加载结束，如果内容不满一屏van-list会自动再触发一次
      loading.value = false
    }).catch(() => {
      loading.value = false
      finished.value = true
    })
    //为下一次触底加载准备页码
    productQuery.value.page++
  }

  //重置查询条件并重新加载（从首页再次搜索时路由query变化）
  const reload = () => {
    productQuery.value.name = route.query.name || ''
    productQuery.value.categoryId = route.query.categoryId || null
    productQuery.value.page = 1
    list.value = []
    finished.value = false
    //分类页跳转时用title指定标题；name是商品名搜索关键字，不能既当标题又当过滤条件
    title.value = route.query.title || (route.query.name ? '搜索：' + route.query.name : '商品列表')
    //手动触发第一页加载
    loading.value = true
    onLoad()
  }
  reload()
  watch(() => route.query, reload)

  //离开页面时停止观察，防止后台继续加载
  onUnmounted(() => {
    finished.value = true
  })
</script>

<template>
  <div class="product-list pb-action">
    <!-- 页面头部：标题为搜索关键字或分类名称 -->
    <PageHeader :title="title"/>

    <!-- 商品两列宫格，van-list负责触底自动加载 -->
    <van-list v-model:loading="loading" :finished="finished" v-if="list.length > 0 || loading"
              finished-text="— 没有更多了 —">
      <div class="product-grid">
        <ProductCard v-for="product in list" :key="product.id" :product="product"/>
      </div>
    </van-list>

    <!-- 空状态 -->
    <van-empty v-if="!loading && list.length === 0" description="暂无相关商品"/>
  </div>
</template>

<style scoped>
  .product-grid {
    display: grid;
    grid-template-columns: 1fr 1fr;
    gap: 8px;
    padding: 8px;
  }
</style>

<script setup>
  import {ref} from 'vue'
  import {useRouter} from 'vue-router'
  import {showToast} from 'vant'
  import categoryApi from '@/api/category/category.js'
  import productApi from '@/api/product/product.js'
  import ProductCard from '@/components/ProductCard.vue'
  import logo from '@/assets/logo.png'

  const router = useRouter()

  //搜索关键字
  const keyword = ref('')
  const onSearch = () => {
    if (!keyword.value.trim()) {
      showToast('请输入要搜索的商品名称')
      return
    }
    router.push({path: '/product', query: {name: keyword.value.trim()}})
  }

  //分类标签行：推荐（不筛选）+ 全部二级分类，商品挂在二级分类上，筛选按二级分类id
  const tabs = ref([{id: null, name: '推荐'}])
  const activeTab = ref(0)
  categoryApi.tree().then(result => {
    if (result.code === 1) {
      const subList = (result.data || []).flatMap(category => category.children || [])
      tabs.value = [...tabs.value, ...subList.map(sub => ({id: sub.id, name: sub.name}))]
    }
  })

  //商品瀑布流列表，van-list负责触底自动加载
  const list = ref([])
  const loading = ref(false)
  const finished = ref(false)
  const productQuery = ref({
    page: 1,
    limit: 10,
    //只看上架商品
    status: 1,
    categoryId: null
  })

  const onLoad = () => {
    productApi.list(productQuery.value).then(result => {
      if (result.code === 1) {
        list.value = [...list.value, ...(result.data.records || [])]
        if (list.value.length >= result.data.total) {
          finished.value = true
        }
      } else {
        finished.value = true
      }
      loading.value = false
    }).catch(() => {
      loading.value = false
      finished.value = true
    })
    //为下一次触底加载准备页码
    productQuery.value.page++
  }

  //切换分类标签，重置列表重新加载
  const selectTab = (index) => {
    if (activeTab.value === index) {
      return
    }
    activeTab.value = index
    productQuery.value.categoryId = tabs.value[index].id
    productQuery.value.page = 1
    list.value = []
    finished.value = false
    loading.value = true
    onLoad()
  }

  //进入首页先加载第一页
  onLoad()
</script>

<template>
  <div class="home pb-tab">
    <!-- 顶部：LOGO + 胶囊搜索框 -->
    <div class="home-top">
      <img class="brand" :src="logo" alt="Novae星绽">
      <div class="search-box">
        <van-search v-model="keyword" placeholder="搜索商品" shape="round"
                    background="transparent" @search="onSearch"/>
      </div>
    </div>

    <!-- 分类标签行：文字式 tab，可横向滑动 -->
    <div class="tab-row">
      <div v-for="(tab, index) in tabs" :key="index" class="tab-item"
           :class="{active: index === activeTab}" @click="selectTab(index)">
        {{ tab.name }}
      </div>
    </div>

    <!-- 限时秒杀入口 -->
    <div class="seckill-entry" @click="router.push('/seckill')">
      <van-icon name="clock-o" size="18"/>
      <span class="seckill-title">限时秒杀</span>
      <span class="seckill-sub">好物限量抢</span>
      <van-icon class="seckill-arrow" name="arrow"/>
    </div>

    <!-- 商品两列瀑布流 -->
    <van-list v-model:loading="loading" :finished="finished" finished-text="— 没有更多了 —">
      <div class="product-grid">
        <ProductCard v-for="product in list" :key="product.id" :product="product"/>
      </div>
    </van-list>
  </div>
</template>

<style scoped>
  /* 首页整体白底，与截图一致 */
  .home {
    min-height: 100vh;
    background-color: #fff;
  }

  /* 顶部白色栏：品牌字 + 搜索框 */
  .home-top {
    display: flex;
    align-items: center;
    gap: 10px;
    padding: 10px 12px;
    background-color: #fff;
  }

  .brand {
    flex-shrink: 0;
    height: 48px;
    object-fit: contain;
  }

  .search-box {
    flex: 1;
    min-width: 0;
  }

  :deep(.search-box .van-search) {
    padding: 0;
  }

  :deep(.search-box .van-search__content) {
    background-color: #f5f6f7;
  }

  /* 分类标签行：白底文字 tab，激活项黑色加粗 */
  .tab-row {
    display: flex;
    gap: 26px;
    padding: 12px 14px 8px;
    overflow-x: auto;
    background-color: #fff;
    /*隐藏横向滚动条*/
    scrollbar-width: none;
  }

  .tab-row::-webkit-scrollbar {
    display: none;
  }

  .tab-item {
    flex-shrink: 0;
    color: #999;
    font-size: 15px;
    cursor: pointer;
  }

  .tab-item.active {
    color: var(--app-primary);
    font-weight: 700;
  }

  /* 限时秒杀入口 */
  .seckill-entry {
    display: flex;
    align-items: center;
    gap: 6px;
    margin: 10px 12px 0;
    padding: 12px 14px;
    border-radius: var(--app-card-radius);
    background-color: var(--app-primary);
    color: #fff;
    cursor: pointer;
  }

  .seckill-title {
    font-size: 15px;
    font-weight: 700;
  }

  .seckill-sub {
    font-size: 12px;
    opacity: 0.7;
  }

  .seckill-arrow {
    margin-left: auto;
  }

  /* 商品两列宫格：白底页面，与顶部连成一体 */
  .product-grid {
    display: grid;
    grid-template-columns: 1fr 1fr;
    gap: 20px 10px;
    padding: 10px 12px;
    background-color: #fff;
  }
</style>
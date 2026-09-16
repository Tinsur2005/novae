<script setup>
  import {ref} from 'vue'
  import {useRouter} from 'vue-router'
  import {showToast} from 'vant'
  import categoryApi from '@/api/category/category.js'
  import productApi from '@/api/product/product.js'
  import ProductCard from '@/components/ProductCard.vue'

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

  //首页轮播的盲盒宣传位
  const banners = ref([
    {text: '新品首发', title: '神秘新系列上线', desc: '每一盒都是未知的惊喜', icon: 'gift-o'},
    {text: '热门推荐', title: '隐藏款等你来抽', desc: '手气王就是你', icon: 'star-o'},
    {text: '限时活动', title: '入盒享优惠', desc: '潮玩好物限时特惠', icon: 'fire-o'}
  ])

  //分类宫格的渐变色和图标，按顺序循环使用
  const catStyles = ref([
    {background: 'linear-gradient(135deg, #ff9a9e 0%, #fecfef 100%)', icon: 'gift-o'},
    {background: 'linear-gradient(135deg, #a18cd1 0%, #fbc2eb 100%)', icon: 'gem-o'},
    {background: 'linear-gradient(135deg, #fbc2eb 0%, #a6c1ee 100%)', icon: 'star-o'},
    {background: 'linear-gradient(135deg, #f6d365 0%, #fda085 100%)', icon: 'fire-o'},
    {background: 'linear-gradient(135deg, #84fab0 0%, #8fd3f4 100%)', icon: 'flower-o'},
    {background: 'linear-gradient(135deg, #a1c4fd 0%, #c2e9fb 100%)', icon: 'medal-o'},
    {background: 'linear-gradient(135deg, #ffd1ff 0%, #fad0c4 100%)', icon: 'smile-o'},
    {background: 'linear-gradient(135deg, #e0c3fc 0%, #8ec5fc 100%)', icon: 'music-o'}
  ])

  //一级分类宫格
  const categoryList = ref([])
  categoryApi.tree().then(result => {
    if (result.code === 1) {
      //tree接口返回的就是一级分类数组，children是二级分类
      categoryList.value = result.data || []
    }
  })

  //点击一级分类，跳到分类页并定位到该分类
  const toCategory = (category) => {
    router.push({path: '/category', query: {id: category.id}})
  }

  //新品推荐，取第一页上架商品（按创建时间倒序）
  const productList = ref([])
  productApi.list({page: 1, limit: 10, status: 1}).then(result => {
    if (result.code === 1) {
      productList.value = result.data.records || []
    }
  })
</script>

<template>
  <div class="home pb-tab">
    <!-- 顶部渐变区：搜索栏 -->
    <div class="home-top">
      <div class="home-banner-title">TinsurMall 盲盒商城</div>
      <van-search v-model="keyword" placeholder="搜索心仪的盲盒好物" shape="round"
                  class="home-search" @search="onSearch"/>
    </div>

    <!-- 盲盒宣传轮播 -->
    <van-swipe class="home-swipe" :autoplay="3000" indicator-color="#a854f7" round>
      <van-swipe-item v-for="banner in banners" :key="banner.text" class="banner-item">
        <van-icon class="banner-icon" :name="banner.icon" size="44" color="#fff"/>
        <div class="banner-body">
          <div class="banner-text">{{ banner.text }}</div>
          <div class="banner-title">{{ banner.title }}</div>
          <div class="banner-desc">{{ banner.desc }}</div>
        </div>
      </van-swipe-item>
    </van-swipe>

    <!-- 分类宫格 -->
    <div class="section">
      <div class="section-title">潮玩分类</div>
      <div class="cat-grid">
        <div v-for="(category, index) in categoryList" :key="category.id" class="cat-item"
             @click="toCategory(category)">
          <div class="cat-icon" :style="{background: catStyles[index % catStyles.length].background}">
            <van-icon :name="catStyles[index % catStyles.length].icon" size="22" color="#fff"/>
          </div>
          <div class="cat-name ellipsis-1">{{ category.name }}</div>
        </div>
      </div>
    </div>

    <!-- 新品推荐 -->
    <div class="section">
      <div class="section-title">新品推荐</div>
      <div class="product-grid">
        <ProductCard v-for="product in productList" :key="product.id" :product="product"/>
      </div>
    </div>
  </div>
</template>

<style scoped>
  /* 顶部渐变区 */
  .home-top {
    background: var(--app-gradient);
    padding: 10px 0 14px;
    border-radius: 0 0 20px 20px;
  }

  .home-banner-title {
    padding: 2px 16px 6px;
    font-size: 17px;
    font-weight: 700;
    color: #fff;
    letter-spacing: 1px;
  }

  /* 搜索框白底半透明，融进渐变里 */
  .home-search {
    background: transparent;
    padding: 0 12px;
  }

  :deep(.home-search .van-search__content) {
    background-color: rgba(255, 255, 255, 0.9);
  }

  /* 盲盒宣传轮播 */
  .home-swipe {
    margin: 10px 12px 0;
    height: 120px;
  }

  .banner-item {
    display: flex;
    align-items: center;
    gap: 14px;
    padding: 0 22px;
    background: linear-gradient(135deg, #ffb6e0 0%, #c79bf7 100%);
  }

  .banner-icon {
    filter: drop-shadow(0 4px 8px rgba(0, 0, 0, 0.1));
  }

  .banner-body {
    color: #fff;
    text-shadow: 0 1px 2px rgba(0, 0, 0, 0.08);
  }

  .banner-text {
    display: inline-block;
    padding: 1px 8px;
    border: 1px solid rgba(255, 255, 255, 0.8);
    border-radius: 10px;
    font-size: 11px;
  }

  .banner-title {
    margin-top: 5px;
    font-size: 18px;
    font-weight: 700;
  }

  .banner-desc {
    margin-top: 3px;
    font-size: 12px;
    opacity: 0.9;
  }

  .section {
    margin: 10px 12px 0;
  }

  /* 分类宫格 */
  .cat-grid {
    display: grid;
    grid-template-columns: repeat(4, 1fr);
    gap: 8px;
  }

  .cat-item {
    background-color: #fff;
    border-radius: var(--app-card-radius);
    padding: 12px 4px 10px;
    text-align: center;
    box-shadow: 0 2px 8px rgba(168, 84, 247, 0.06);
  }

  .cat-icon {
    width: 44px;
    height: 44px;
    margin: 0 auto 6px;
    border-radius: 16px;
    display: flex;
    align-items: center;
    justify-content: center;
    font-size: 22px;
  }

  .cat-name {
    font-size: 13px;
    font-weight: 600;
    color: #303133;
  }

  /* 商品两列宫格 */
  .product-grid {
    display: grid;
    grid-template-columns: 1fr 1fr;
    gap: 8px;
  }
</style>

<script setup>
  import {ref, computed} from 'vue'
  import {useRoute, useRouter} from 'vue-router'
  import {showToast, showSuccessToast} from 'vant'
  import productApi from '@/api/product/product.js'
  import cartApi from '@/api/cart/cart.js'
  import PageHeader from '@/components/PageHeader.vue'
  import {useCartCountStore} from '@/store/cartCount.js'
  import {formatImage, formatPrice} from '@/utils/format.js'

  const route = useRoute()
  const router = useRouter()
  const cartCountStore = useCartCountStore()

  //商品详情
  const product = ref({})
  productApi.selectById(route.params.id).then(result => {
    if (result.code === 1 && result.data) {
      product.value = result.data
    }
  })

  //轮播图：主图 + 副图（副图在数据库中是逗号分隔的地址串）
  const images = computed(() => [product.value.mainImage, ...(product.value.subImages || '').split(',')]
      .map(url => (url || '').trim()).filter(url => url))

  //购买数量
  const count = ref(1)
  //库存为0时步进器的最大值也要至少为1，否则组件校验不通过
  const maxCount = computed(() => Math.max(1, product.value.stock || 0))
  const soldOut = computed(() => (product.value.stock || 0) <= 0)

  //加入购物车
  const addCart = () => {
    if (soldOut.value) {
      showToast('该商品已售罄')
      return
    }
    cartApi.add({productId: product.value.id, count: count.value}).then(result => {
      if (result.code === 1) {
        showSuccessToast('已加入购物车')
        //刷新底部TabBar的购物车角标
        cartCountStore.refreshCount()
      } else {
        showToast(result.msg || '加入购物车失败')
      }
    })
  }

  //跳转购物车
  const toCart = () => router.push('/cart')
</script>

<template>
  <div class="detail pb-action">
    <PageHeader title="商品详情"/>

    <!-- 商品轮播图 -->
    <van-swipe :autoplay="4000" indicator-color="#a854f7" class="detail-swipe">
      <van-swipe-item v-for="image in images" :key="image">
        <img class="detail-img" :src="formatImage(image)" :alt="product.name">
      </van-swipe-item>
    </van-swipe>

    <!-- 商品信息 -->
    <div class="detail-info card">
      <div class="detail-name">{{ product.name }}</div>
      <div v-if="product.subtitle" class="detail-subtitle">{{ product.subtitle }}</div>
      <div class="detail-price-row">
        <span class="detail-price">{{ formatPrice(product.price) }}</span>
        <span class="detail-stock">库存 {{ product.stock }}</span>
      </div>
      <div class="detail-count-row">
        <span class="detail-count-label">数量</span>
        <van-stepper v-model="count" :min="1" :max="maxCount" :disabled="soldOut" button-size="26"/>
      </div>
    </div>

    <!-- 商品详情 -->
    <div class="detail-desc card">
      <div class="detail-desc-title">商品详情</div>
      <div v-if="product.detail" class="detail-desc-content">{{ product.detail }}</div>
      <van-empty v-else description="暂无详细介绍" image-size="60"/>
    </div>

    <!-- 底部操作栏：购物车入口 + 加入购物车 -->
    <div class="detail-bar app-fixed">
      <div class="bar-cart" @click="toCart">
        <div class="bar-cart-icon">
          <van-icon name="shopping-cart-o" size="20"/>
          <span v-if="cartCountStore.count > 0" class="bar-cart-badge">
            {{ cartCountStore.count > 99 ? '99+' : cartCountStore.count }}
          </span>
        </div>
        <span class="bar-cart-text">购物车</span>
      </div>
      <van-button class="bar-button gradient-button" round type="primary" :disabled="soldOut" @click="addCart">
        {{ soldOut ? '已售罄' : '加入购物车' }}
      </van-button>
    </div>
  </div>
</template>

<style scoped>
  .detail-swipe {
    background-color: #fff;
  }

  .detail-img {
    width: 100%;
    aspect-ratio: 1 / 1;
    object-fit: contain;
    background-color: #fff;
  }

  .detail-info {
    margin: 8px;
    padding: 12px;
  }

  .detail-name {
    font-size: 16px;
    font-weight: 600;
    color: #303133;
    line-height: 1.4;
  }

  .detail-subtitle {
    margin-top: 4px;
    font-size: 12px;
    color: #999;
    line-height: 1.4;
  }

  .detail-price-row {
    display: flex;
    align-items: baseline;
    justify-content: space-between;
    margin-top: 10px;
  }

  .detail-price {
    font-size: 22px;
    font-weight: 700;
    color: var(--app-pink);
  }

  .detail-stock {
    font-size: 12px;
    color: #999;
  }

  .detail-count-row {
    display: flex;
    align-items: center;
    justify-content: space-between;
    margin-top: 12px;
    padding-top: 12px;
    border-top: 1px solid #f5f5f5;
  }

  .detail-count-label {
    font-size: 14px;
    color: #303133;
  }

  .detail-desc {
    margin: 8px;
    padding: 12px;
  }

  .detail-desc-title {
    font-size: 15px;
    font-weight: 600;
    color: #303133;
    margin-bottom: 8px;
  }

  .detail-desc-content {
    font-size: 14px;
    color: #606266;
    line-height: 1.6;
    white-space: pre-wrap;
    word-break: break-all;
  }

  /* 底部操作栏 */
  .detail-bar {
    bottom: 0;
    display: flex;
    align-items: center;
    height: calc(50px + env(safe-area-inset-bottom));
    padding: 0 12px env(safe-area-inset-bottom);
    box-sizing: border-box;
    background-color: #fff;
    border-top: 1px solid #f0f0f0;
  }

  .bar-cart {
    position: relative;
    display: flex;
    flex-direction: column;
    align-items: center;
    width: 56px;
    color: #666;
  }

  .bar-cart-icon {
    position: relative;
    line-height: 1;
  }

  .bar-cart-text {
    font-size: 10px;
    margin-top: 2px;
  }

  .bar-cart-badge {
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

  .bar-button {
    flex: 1;
    margin-left: 12px;
    height: 38px;
    font-size: 15px;
    font-weight: 600;
  }
</style>

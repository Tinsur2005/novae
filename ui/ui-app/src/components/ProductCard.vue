<script setup>
  import {computed} from 'vue'
  import {useRouter} from 'vue-router'
  import {formatImage} from '@/utils/format.js'

  //props：商品对象
  const props = defineProps({
    product: {
      type: Object,
      required: true
    }
  })

  const router = useRouter()
  //点击卡片跳转商品详情
  const toDetail = () => router.push('/product/' + props.product.id)

  const soldOut = computed(() => (props.product.stock || 0) <= 0)

  //价格展示：整数不带小数（如 ¥69），非整数保留两位小数
  const showPrice = computed(() => {
    const price = Number(props.product.price || 0)
    return Number.isInteger(price) ? String(price) : price.toFixed(2)
  })
</script>

<template>
  <div class="product-card" @click="toDetail">
    <!-- 图片区：圆角满幅大图，售罄时中间盖一个灰色胶囊 -->
    <div class="product-card-media">
      <img class="product-card-img" :src="formatImage(product.mainImage)" :alt="product.name" loading="lazy">
      <span v-if="soldOut" class="media-soldout">已售罄</span>
    </div>
    <div class="product-card-body">
      <div class="product-card-name ellipsis-2">{{ product.name }}</div>
      <div class="product-card-price-row">
        <span class="price-symbol">¥</span>
        <span class="price-num">{{ showPrice }}</span>
      </div>
    </div>
  </div>
</template>

<style scoped>
  .product-card {
    cursor: pointer;
  }

  .product-card-media {
    position: relative;
    border-radius: 12px;
    overflow: hidden;
    background-color: #f5f6f7;
  }

  .product-card-img {
    display: block;
    width: 100%;
    aspect-ratio: 1 / 1;
    object-fit: cover;
  }

  .media-soldout {
    position: absolute;
    left: 50%;
    top: 50%;
    transform: translate(-50%, -50%);
    background-color: rgba(0, 0, 0, 0.45);
    color: #fff;
    font-size: 12px;
    padding: 4px 12px;
    border-radius: 13px;
  }

  .product-card-body {
    padding: 8px 2px 2px;
  }

  .product-card-name {
    font-size: 15px;
    font-weight: 500;
    color: #1a1a1a;
    line-height: 1.4;
  }

  .product-card-price-row {
    display: flex;
    align-items: baseline;
    margin-top: 4px;
    color: var(--app-primary);
  }

  .price-symbol {
    font-size: 13px;
    font-weight: 700;
  }

  .price-num {
    margin-left: 1px;
    font-size: 19px;
    font-weight: 700;
  }
</style>

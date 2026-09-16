<script setup>
  import {useRouter} from 'vue-router'
  import {formatImage, formatPrice} from '@/utils/format.js'

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
</script>

<template>
  <div class="product-card" @click="toDetail">
    <img class="product-card-img" :src="formatImage(product.mainImage)" :alt="product.name" loading="lazy">
    <div class="product-card-body">
      <div class="product-card-name ellipsis-2">{{ product.name }}</div>
      <div v-if="product.subtitle" class="product-card-sub ellipsis-1">{{ product.subtitle }}</div>
      <div class="product-card-price">{{ formatPrice(product.price) }}</div>
    </div>
  </div>
</template>

<style scoped>
  .product-card {
    background-color: #fff;
    border-radius: var(--app-card-radius);
    overflow: hidden;
    box-shadow: 0 2px 8px rgba(168, 84, 247, 0.06);
  }

  .product-card-img {
    width: 100%;
    aspect-ratio: 1 / 1;
    object-fit: contain;
    background-color: #fff;
  }

  .product-card-body {
    padding: 8px 10px 10px;
  }

  .product-card-name {
    font-size: 14px;
    color: #303133;
    line-height: 1.4;
    /*名字占两行，没有副标题时高度也对齐*/
    min-height: 39px;
  }

  .product-card-sub {
    margin-top: 2px;
    font-size: 12px;
    color: #999;
  }

  .product-card-price {
    margin-top: 6px;
    font-size: 16px;
    color: var(--app-pink);
    font-weight: 600;
  }
</style>

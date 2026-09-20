<script setup>
  import {computed} from 'vue'
  import {useRouter} from 'vue-router'
  import {formatImage, formatPrice} from '@/utils/format.js'

  //图片区域的马卡龙纯色底，按商品id循环取色
  const palette = ['#e8f4ff', '#e8f8ec', '#fff6e0', '#ffefef', '#f3e8ff']

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

  const mediaBg = computed(() => palette[(Number(props.product.id) || 0) % palette.length])
  const soldOut = computed(() => (props.product.stock || 0) <= 0)
</script>

<template>
  <div class="product-card" @click="toDetail">
    <!-- 图片区：纯色浅色底，售罄时中间盖一个灰色胶囊 -->
    <div class="product-card-media" :style="{background: mediaBg}">
      <img class="product-card-img" :src="formatImage(product.mainImage)" :alt="product.name" loading="lazy">
      <span v-if="soldOut" class="media-soldout">已售罄</span>
    </div>
    <div class="product-card-body">
      <div class="product-card-name ellipsis-2">{{ product.name }}</div>
      <div class="product-card-row">
        <!-- 库存角标 -->
        <span class="stock-tag" :class="{gray: soldOut}">{{ soldOut ? '已售罄' : '现货' }}</span>
        <span class="product-card-price">{{ formatPrice(product.price) }}</span>
      </div>
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

  .product-card-media {
    position: relative;
  }

  .product-card-img {
    width: 100%;
    aspect-ratio: 1 / 1;
    object-fit: contain;
    padding: 6px;
    box-sizing: border-box;
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
    padding: 8px 10px 10px;
  }

  .product-card-name {
    font-size: 14px;
    font-weight: 600;
    color: #303133;
    line-height: 1.4;
    /*名字占两行，没有副标题时高度也对齐*/
    min-height: 39px;
  }

  .product-card-row {
    display: flex;
    align-items: center;
    justify-content: space-between;
    margin-top: 6px;
  }

  .stock-tag {
    font-size: 11px;
    font-weight: 600;
    color: var(--app-primary);
    background-color: var(--app-primary-light);
    padding: 2px 8px;
    border-radius: 9px;
  }

  .stock-tag.gray {
    color: #999;
    background-color: #f2f3f5;
  }

  .product-card-price {
    font-size: 16px;
    color: var(--app-primary);
    font-weight: 700;
  }
</style>
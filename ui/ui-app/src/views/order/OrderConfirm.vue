<script setup>
  import {ref, computed, onMounted} from 'vue'
  import {useRouter} from 'vue-router'
  import {showToast, showSuccessToast} from 'vant'
  import cartApi from '@/api/cart/cart.js'
  import orderApi from '@/api/order/order.js'
  import shippingApi from '@/api/shipping/shipping.js'
  import PageHeader from '@/components/PageHeader.vue'
  import {useCartCountStore} from '@/store/cartCount.js'
  import {formatImage, formatPrice} from '@/utils/format.js'

  const router = useRouter()
  const cartCountStore = useCartCountStore()

  //购物车中已勾选的商品
  const items = ref([])
  const loading = ref(true)

  const loadItems = () => {
    loading.value = true
    cartApi.list().then(result => {
      if (result.code === 1) {
        items.value = (result.data || []).filter(item => item.product && item.selected === 1)
      }
      loading.value = false
    }).catch(() => {
      loading.value = false
    })
  }

  //收货地址
  const shippingList = ref([])
  const addressIndex = ref(0)

  const loadShipping = () => {
    shippingApi.list().then(result => {
      if (result.code === 1) {
        shippingList.value = result.data || []
        //默认选中默认地址，没有默认地址时选中第一个
        const defaultIndex = shippingList.value.findIndex(shipping => shipping.isDefault === 1)
        addressIndex.value = defaultIndex >= 0 ? defaultIndex : 0
      }
    })
  }

  onMounted(() => {
    loadItems()
    loadShipping()
  })

  //当前选中的收货地址
  const currentAddress = computed(() => shippingList.value[addressIndex.value])

  //完整地址：省市区 + 详细地址（部分字段可能为空）
  const fullAddress = (shipping) =>
      [shipping.receiverProvince, shipping.receiverCity, shipping.receiverDistrict, shipping.receiverAddress]
          .filter(Boolean).join(' ')

  //选择地址的弹层
  const addressVisible = ref(false)
  const chooseAddress = (index) => {
    addressIndex.value = index
    addressVisible.value = false
  }

  //合计
  const totalCount = computed(() => items.value.reduce((sum, item) => sum + item.count, 0))
  const totalPrice = computed(() => items.value.reduce((sum, item) => sum + item.product.price * item.count, 0))

  //提交订单
  const submitting = ref(false)
  const submitOrder = () => {
    if (!currentAddress.value) {
      showToast('请先选择收货地址')
      return
    }
    submitting.value = true
    orderApi.create({shippingId: currentAddress.value.id}).then(result => {
      submitting.value = false
      if (result.code === 1) {
        showSuccessToast('下单成功')
        //下单后购物车已勾选的商品会被清除，刷新角标
        cartCountStore.refreshCount()
        router.replace('/order')
      } else {
        showToast(result.msg || '下单失败')
      }
    }).catch(() => {
      submitting.value = false
    })
  }

  const toShipping = () => router.push('/shipping')
  const toCart = () => router.push('/cart')
</script>

<template>
  <div class="confirm pb-action">
    <PageHeader title="确认订单"/>

    <!-- 加载中 -->
    <div v-if="loading" class="list-loading">加载中...</div>

    <template v-if="!loading && items.length > 0">
      <!-- 收货地址 -->
      <div class="addr-card card" @click="addressVisible = true">
        <template v-if="currentAddress">
          <div class="addr-row">
            <span class="addr-name">{{ currentAddress.receiverName }}</span>
            <span class="addr-mobile">{{ currentAddress.receiverMobile }}</span>
            <van-icon class="addr-arrow" name="arrow"/>
          </div>
          <div class="addr-detail">{{ fullAddress(currentAddress) }}</div>
        </template>
        <div v-else class="addr-empty">
          <span>请添加收货地址</span>
          <van-icon class="addr-arrow" name="arrow"/>
        </div>
      </div>

      <!-- 商品清单 -->
      <div class="goods-card card">
        <div v-for="item in items" :key="item.id" class="goods-item">
          <img class="goods-img" :src="formatImage(item.product.mainImage)" :alt="item.product.name">
          <div class="goods-info">
            <div class="goods-name ellipsis-2">{{ item.product.name }}</div>
            <div v-if="item.product.subtitle" class="goods-sub ellipsis-1">{{ item.product.subtitle }}</div>
          </div>
          <div class="goods-right">
            <span class="goods-price">{{ formatPrice(item.product.price) }}</span>
            <span class="goods-count">×{{ item.count }}</span>
          </div>
        </div>
        <div class="goods-total">
          <span>共 {{ totalCount }} 件商品，合计：</span>
          <span class="price">{{ formatPrice(totalPrice) }}</span>
        </div>
      </div>
    </template>

    <!-- 没有已勾选的商品 -->
    <van-empty v-if="!loading && items.length === 0" description="没有已勾选的商品" image="search">
      <van-button round type="primary" class="brand-button" @click="toCart">去购物车</van-button>
    </van-empty>

    <!-- 底部提交栏 -->
    <van-submit-bar v-if="!loading && items.length > 0" class="app-fixed" :price="Math.round(totalPrice * 100)"
                    label="合计：" button-text="提交订单" :loading="submitting" @submit="submitOrder"/>

    <!-- 选择收货地址弹层 -->
    <van-popup v-model:show="addressVisible" round position="bottom" :style="{maxHeight: '70%'}">
      <div class="popup-title">选择收货地址</div>
      <div v-if="shippingList.length > 0" class="addr-list">
        <div v-for="(shipping, index) in shippingList" :key="shipping.id" class="addr-option"
             :class="{active: index === addressIndex}" @click="chooseAddress(index)">
          <div class="addr-row">
            <span class="addr-name">{{ shipping.receiverName }}</span>
            <span class="addr-mobile">{{ shipping.receiverMobile }}</span>
          </div>
          <div class="addr-detail">{{ fullAddress(shipping) }}</div>
          <van-icon v-if="index === addressIndex" class="addr-option-check" name="success"/>
        </div>
      </div>
      <van-empty v-else description="暂无收货地址" image-size="60"/>
      <div class="addr-manage">
        <van-button block plain round type="primary" @click="toShipping">管理收货地址</van-button>
      </div>
    </van-popup>
  </div>
</template>

<style scoped>
  .addr-card {
    margin: 8px;
    padding: 14px 12px;
  }

  .addr-row {
    display: flex;
    align-items: center;
    gap: 8px;
  }

  .addr-name {
    font-size: 15px;
    font-weight: 600;
    color: #303133;
  }

  .addr-mobile {
    font-size: 13px;
    color: #666;
  }

  .addr-arrow {
    margin-left: auto;
    color: #999;
  }

  .addr-detail {
    margin-top: 6px;
    font-size: 13px;
    color: #606266;
    line-height: 1.5;
  }

  .addr-empty {
    display: flex;
    align-items: center;
    justify-content: center;
    font-size: 14px;
    color: #999;
    padding: 8px 0;
  }

  /* 商品清单 */
  .goods-card {
    margin: 8px;
    padding: 4px 12px;
  }

  .goods-item {
    display: flex;
    gap: 10px;
    padding: 12px 0;
    border-bottom: 1px solid #f5f5f5;
  }

  .goods-img {
    width: 64px;
    height: 64px;
    flex-shrink: 0;
    border-radius: 8px;
    object-fit: contain;
    background-color: #fafafa;
  }

  .goods-info {
    flex: 1;
    min-width: 0;
  }

  .goods-name {
    font-size: 14px;
    color: #303133;
    line-height: 1.4;
  }

  .goods-sub {
    margin-top: 2px;
    font-size: 12px;
    color: #999;
  }

  .goods-right {
    display: flex;
    flex-direction: column;
    align-items: flex-end;
    flex-shrink: 0;
  }

  .goods-price {
    font-size: 14px;
    font-weight: 600;
    color: var(--app-pink);
  }

  .goods-count {
    margin-top: 4px;
    font-size: 12px;
    color: #999;
  }

  .goods-total {
    display: flex;
    align-items: baseline;
    justify-content: flex-end;
    gap: 4px;
    padding: 12px 0;
    font-size: 13px;
    color: #666;
  }

  .goods-total .price {
    font-size: 17px;
  }

  /* 选择地址弹层 */
  .popup-title {
    padding: 16px 0 8px;
    text-align: center;
    font-size: 16px;
    font-weight: 600;
    color: #303133;
  }

  .addr-list {
    max-height: 45vh;
    overflow-y: auto;
    padding: 0 12px;
  }

  .addr-option {
    position: relative;
    padding: 12px;
    margin-bottom: 8px;
    border: 1px solid #eee;
    border-radius: var(--app-card-radius);
  }

  .addr-option.active {
    border-color: var(--app-primary);
    background-color: var(--app-primary-light);
  }

  .addr-option-check {
    position: absolute;
    right: 12px;
    top: 50%;
    transform: translateY(-50%);
    color: var(--app-primary);
  }

  .addr-manage {
    padding: 12px;
  }
</style>
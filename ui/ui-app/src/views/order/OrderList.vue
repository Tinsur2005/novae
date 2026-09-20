<script setup>
  import {ref, onMounted} from 'vue'
  import {useRouter} from 'vue-router'
  import orderApi from '@/api/order/order.js'
  import PageHeader from '@/components/PageHeader.vue'
  import {formatImage, formatPrice} from '@/utils/format.js'

  const router = useRouter()

  //订单状态：-1-未付款 0-已取消 1-待发货 2-已发货 3-交易成功 4-交易关闭 5-已退款
  const statusMap = {
    '-1': {text: '待付款', type: 'warning'},
    0: {text: '已取消', type: 'default'},
    1: {text: '待发货', type: 'primary'},
    2: {text: '已发货', type: 'primary'},
    3: {text: '交易成功', type: 'success'},
    4: {text: '交易关闭', type: 'danger'},
    5: {text: '已退款', type: 'default'}
  }

  //订单列表
  const list = ref([])
  const loading = ref(true)

  onMounted(() => {
    orderApi.list().then(result => {
      if (result.code === 1) {
        list.value = result.data || []
      }
      loading.value = false
    }).catch(() => {
      loading.value = false
    })
  })

  const toHome = () => router.push('/home')
</script>

<template>
  <div class="order-list pb-action">
    <PageHeader title="我的订单"/>

    <!-- 加载中 -->
    <div v-if="loading" class="list-loading">加载中...</div>

    <!-- 空状态 -->
    <van-empty v-if="!loading && list.length === 0" description="暂无订单" image="search">
      <van-button round type="primary" class="brand-button" @click="toHome">去逛逛</van-button>
    </van-empty>

    <!-- 订单列表 -->
    <div v-if="!loading" class="orders">
      <div v-for="order in list" :key="order.orderNo" class="order-card card">
        <!-- 订单头部：订单号 + 状态 -->
        <div class="order-header">
          <span class="order-no">订单号：{{ order.orderNo }}</span>
          <van-tag :type="statusMap[order.status]?.type || 'default'">
            {{ statusMap[order.status]?.text || '未知状态' }}
          </van-tag>
        </div>
        <!-- 订单商品 -->
        <div v-for="item in order.orderItemList" :key="item.id" class="order-item">
          <img class="order-img" :src="formatImage(item.productImage)" :alt="item.productName">
          <div class="order-info">
            <div class="order-name ellipsis-2">{{ item.productName }}</div>
          </div>
          <div class="order-right">
            <span class="order-price">{{ formatPrice(item.currentUnitPrice) }}</span>
            <span class="order-count">×{{ item.quantity }}</span>
          </div>
        </div>
        <!-- 订单底部：下单时间 + 实付金额 -->
        <div class="order-footer">
          <span class="order-time">{{ order.createTime }}</span>
          <span class="order-pay">实付 <span class="price">{{ formatPrice(order.payment) }}</span></span>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
  .orders {
    padding: 8px;
  }

  .order-card {
    padding: 12px;
    margin-bottom: 8px;
  }

  .order-header {
    display: flex;
    align-items: center;
    justify-content: space-between;
  }

  .order-no {
    font-size: 12px;
    color: #999;
  }

  .order-item {
    display: flex;
    gap: 10px;
    padding: 12px 0 0;
  }

  .order-img {
    width: 64px;
    height: 64px;
    flex-shrink: 0;
    border-radius: 8px;
    object-fit: contain;
    background-color: #fafafa;
  }

  .order-info {
    flex: 1;
    min-width: 0;
  }

  .order-name {
    font-size: 14px;
    color: #303133;
    line-height: 1.4;
  }

  .order-right {
    display: flex;
    flex-direction: column;
    align-items: flex-end;
    flex-shrink: 0;
  }

  .order-price {
    font-size: 14px;
    color: #303133;
  }

  .order-count {
    margin-top: 4px;
    font-size: 12px;
    color: #999;
  }

  .order-footer {
    display: flex;
    align-items: center;
    justify-content: space-between;
    margin-top: 12px;
    padding-top: 10px;
    border-top: 1px solid #f5f5f5;
  }

  .order-time {
    font-size: 12px;
    color: #999;
  }

  .order-pay {
    display: flex;
    align-items: baseline;
    font-size: 13px;
    color: #666;
  }

  .order-pay .price {
    font-size: 17px;
  }
</style>
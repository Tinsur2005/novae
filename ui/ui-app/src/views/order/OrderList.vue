<script setup>
  import {ref, computed, onMounted} from 'vue'
  import {useRoute, useRouter} from 'vue-router'
  import {showToast, showSuccessToast} from 'vant'
  import orderApi from '@/api/order/order.js'
  import PageHeader from '@/components/PageHeader.vue'
  import {formatImage, formatPrice} from '@/utils/format.js'

  const route = useRoute()
  const router = useRouter()

  //订单状态：-1-未付款 0-已取消 1-待发货 2-已发货 3-交易成功 4-交易关闭 5-已退款
  const statusMap = {
    '-1': {text: '待付款', cls: 'st-pay'},
    0: {text: '已取消', cls: 'st-gray'},
    1: {text: '待发货', cls: 'st-dark'},
    2: {text: '已发货', cls: 'st-dark'},
    3: {text: '交易成功', cls: 'st-gray'},
    4: {text: '交易关闭', cls: 'st-gray'},
    5: {text: '已退款', cls: 'st-gray'}
  }

  //状态筛选 tabs
  const tabs = [
    {label: '全部', value: 'all'},
    {label: '待付款', value: -1},
    {label: '待发货', value: 1},
    {label: '待收货', value: 2},
    {label: '已完成', value: 3}
  ]
  const activeTab = ref('all')

  //支持通过 ?tab= 直达指定状态（我的页面订单入口跳转过来）
  const initTab = () => {
    const tab = route.query.tab
    if (tab !== undefined && tabs.some(t => t.value === Number(tab))) {
      activeTab.value = Number(tab)
    }
  }
  initTab()

  //订单列表
  const list = ref([])
  const loading = ref(true)

  //按当前 tab 过滤订单
  const filteredList = computed(() => {
    if (activeTab.value === 'all') {
      return list.value
    }
    return list.value.filter(order => order.status === activeTab.value)
  })

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

  //未付款订单的支付倒计时：截止时间 = 下单时间 + 30分钟
  const remainTime = (order) => {
    //把 yyyy-MM-dd HH:mm:ss 换成斜杠格式，兼容 iOS 的 Date 解析
    const createTime = new Date(order.createTime.replace(/-/g, '/')).getTime()
    return Math.max(createTime + 30 * 60 * 1000 - Date.now(), 0)
  }

  //倒计时走完，先在前台把订单标成已取消，真实状态以刷新回来的数据为准
  const onCountdownFinish = (order) => {
    order.status = 0
  }

  //虚拟支付：支付成功后订单在前台直接变为待发货
  const payOrder = (order) => {
    orderApi.pay(order.orderNo).then(result => {
      if (result.code === 1) {
        showSuccessToast(result.msg || '支付成功')
        order.status = 1
      } else {
        showToast(result.msg || '支付失败')
      }
    })
  }

  const toHome = () => router.push('/home')

  //点击订单卡片进入订单详情
  const toDetail = (orderNo) => router.push('/order/' + orderNo)

  //点击订单中的商品进入商品详情（商品已删除时详情页会提示商品不存在）
  const toProduct = (item) => router.push('/product/' + item.productId)
</script>

<template>
  <div class="order-list pb-action">
    <PageHeader title="我的订单"/>

    <!-- 状态筛选 tabs -->
    <div class="order-tabs">
      <div v-for="tab in tabs" :key="tab.value" class="tab-item"
           :class="{active: activeTab === tab.value}" @click="activeTab = tab.value">
        {{ tab.label }}
      </div>
    </div>

    <!-- 加载中 -->
    <div v-if="loading" class="list-loading">加载中...</div>

    <template v-if="!loading">
      <!-- 订单列表 -->
      <div v-if="filteredList.length > 0" class="orders">
        <div v-for="order in filteredList" :key="order.orderNo" class="order-card card" @click="toDetail(order.orderNo)">
          <!-- 订单头部：订单号 + 状态 -->
          <div class="order-header">
            <span class="order-no">订单号：{{ order.orderNo }}</span>
            <span class="order-status" :class="statusMap[order.status]?.cls">
              {{ statusMap[order.status]?.text || '未知状态' }}
            </span>
          </div>
          <!-- 未付款订单显示支付倒计时 -->
          <div v-if="order.status === -1" class="order-countdown-row">
            <van-icon name="clock-o"/>
            <span class="countdown-label">支付剩余时间</span>
            <van-count-down :time="remainTime(order)" format="mm 分 ss 秒" class="order-countdown"
                            @finish="onCountdownFinish(order)"/>
          </div>
          <!-- 订单商品 -->
          <div v-for="item in order.orderItemList" :key="item.id" class="order-item" @click.stop="toProduct(item)">
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
          <!-- 未付款订单显示支付按钮 -->
          <div v-if="order.status === -1" class="order-actions">
            <van-button round size="small" class="pay-button" @click.stop="payOrder(order)">立即支付</van-button>
          </div>
        </div>
      </div>

      <!-- 空状态 -->
      <div v-else class="order-empty">
        <div class="empty-icon">
          <van-icon name="orders-o"/>
        </div>
        <p class="empty-text">没有相关订单</p>
        <van-button round class="empty-button" @click="toHome">去逛逛</van-button>
      </div>
    </template>
  </div>
</template>

<style scoped>
  /* 状态筛选 tabs：白底黑字，激活项加粗 + 黑色短横下划线 */
  .order-tabs {
    display: flex;
    position: sticky;
    top: 46px;
    z-index: 99;
    background: #fff;
    padding: 0 8px;
  }

  .tab-item {
    flex: 1;
    position: relative;
    text-align: center;
    padding: 12px 0 12px;
    font-size: 15px;
    color: #1a1a1a;
    cursor: pointer;
  }

  .tab-item.active {
    font-weight: 700;
  }

  .tab-item.active::after {
    content: '';
    position: absolute;
    left: 50%;
    bottom: 4px;
    transform: translateX(-50%);
    width: 24px;
    height: 4px;
    border-radius: 2px;
    background: #1a1a1a;
  }

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

  /* 订单状态文字：进行中黑色加粗、结束态灰色 */
  .order-status {
    font-size: 13px;
    font-weight: 600;
  }

  .st-pay {
    color: #1a1a1a;
  }

  .st-dark {
    color: #1a1a1a;
  }

  .st-gray {
    color: #999;
  }

  /* 支付倒计时行 */
  .order-countdown-row {
    display: flex;
    align-items: center;
    gap: 4px;
    margin-top: 8px;
    font-size: 12px;
    color: #999;
  }

  .countdown-label {
    margin-right: 2px;
  }

  .order-countdown {
    color: #1a1a1a;
    font-weight: 600;
  }

  .order-item {
    display: flex;
    gap: 10px;
    padding: 12px 0 0;
    cursor: pointer;
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

  /* 支付按钮行：黑底白字胶囊 */
  .order-actions {
    display: flex;
    justify-content: flex-end;
    margin-top: 10px;
  }

  .pay-button {
    padding: 0 18px;
    border: none;
    background: var(--app-primary);
    color: #fff;
    font-weight: 600;
  }

  /* 空状态：浅灰圆底图标 + 黑色胶囊按钮 */
  .order-empty {
    display: flex;
    flex-direction: column;
    align-items: center;
    padding-top: 120px;
  }

  .empty-icon {
    width: 120px;
    height: 120px;
    border-radius: 50%;
    display: flex;
    align-items: center;
    justify-content: center;
    background: #f0f1f3;
    color: #c0c4cc;
    font-size: 52px;
  }

  .empty-text {
    margin-top: 20px;
    font-size: 14px;
    color: #999;
  }

  .empty-button {
    margin-top: 24px;
    height: 52px;
    padding: 0 48px;
    border: none;
    background: var(--app-primary);
    color: #fff;
    font-size: 17px;
    font-weight: 600;
  }
</style>
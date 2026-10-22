<script setup>
  import {ref, computed, onMounted} from 'vue'
  import {useRoute, useRouter} from 'vue-router'
  import {showToast, showSuccessToast, showConfirmDialog} from 'vant'
  import orderApi from '@/api/order/order.js'
  import shippingApi from '@/api/shipping/shipping.js'
  import PageHeader from '@/components/PageHeader.vue'
  import {formatImage, formatPrice} from '@/utils/format.js'

  const route = useRoute()
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

  //订单详情
  const order = ref(null)
  const loading = ref(true)

  onMounted(() => loadDetail())

  //查询订单详情，选择收货地址后重新加载时不再展示整页loading
  const loadDetail = (showLoading = true) => {
    if (showLoading) loading.value = true
    orderApi.detail(route.params.orderNo).then(result => {
      if (result.code === 1) {
        order.value = result.data
      } else {
        showToast(result.msg || '订单不存在')
      }
      loading.value = false
    }).catch(() => {
      loading.value = false
    })
  }

  //====================收货地址选择====================
  //未付款订单（秒杀订单支付前必须先选地址），点地址卡片弹出地址选择
  const addressPickerVisible = ref(false)
  const addressList = ref([])

  const openAddressPicker = () => {
    if (order.value.status !== -1) {
      return
    }
    shippingApi.list().then(result => {
      if (result.code === 1) {
        addressList.value = result.data || []
        addressPickerVisible.value = true
      } else {
        showToast(result.msg || '获取收货地址失败')
      }
    })
  }

  //选中一个地址，保存到订单后刷新详情
  const chooseAddress = (item) => {
    orderApi.setShipping(order.value.orderNo, item.id).then(result => {
      if (result.code === 1) {
        addressPickerVisible.value = false
        showSuccessToast(result.msg || '收货地址已保存')
        loadDetail(false)
      } else {
        showToast(result.msg || '保存收货地址失败')
      }
    })
  }

  //跳转地址管理页，返回时重新加载地址列表和订单详情
  const toShippingManage = () => {
    addressPickerVisible.value = false
    router.push('/shipping')
  }

  //商品总额 = 实付金额 - 运费
  const goodsTotal = computed(() => order.value ? order.value.payment - order.value.postage : 0)

  //未付款订单的支付倒计时：截止时间 = 下单时间 + 30分钟
  const remainTime = (order) => {
    //把 yyyy-MM-dd HH:mm:ss 换成斜杠格式，兼容 iOS 的 Date 解析
    const createTime = new Date(order.createTime.replace(/-/g, '/')).getTime()
    return Math.max(createTime + 30 * 60 * 1000 - Date.now(), 0)
  }

  //倒计时走完，先在前台把订单标成已取消，真实状态以刷新回来的数据为准
  const onCountdownFinish = () => {
    if (order.value) order.value.status = 0
  }

  //虚拟支付：支付成功后订单在前台直接变为待发货；没有收货地址时先去选地址
  const payOrder = () => {
    if (!order.value.shipping) {
      openAddressPicker()
      return
    }
    orderApi.pay(order.value.orderNo).then(result => {
      if (result.code === 1) {
        showSuccessToast(result.msg || '支付成功')
        order.value.status = 1
      } else {
        showToast(result.msg || '支付失败')
      }
    })
  }

  //确认收货：已发货订单确认后变为交易成功
  const confirmReceipt = () => {
    showConfirmDialog({
      title: '提示',
      message: '确认已收到商品吗？确认后订单将完成交易',
      confirmButtonText: '确认收货',
      cancelButtonText: '取消'
    }).then(() => {
      orderApi.confirm(order.value.orderNo).then(result => {
        if (result.code === 1) {
          showSuccessToast(result.msg || '确认收货成功')
          order.value.status = 3
        } else {
          showToast(result.msg || '确认收货失败')
        }
      })
    }).catch(() => {
    })
  }

  //点击订单中的商品进入商品详情（商品已删除时详情页会提示商品不存在）
  const toProduct = (item) => router.push('/product/' + item.productId)
</script>

<template>
  <div class="order-detail pb-action">
    <PageHeader title="订单详情"/>

    <!-- 加载中 -->
    <div v-if="loading" class="list-loading">加载中...</div>

    <!-- 订单不存在 -->
    <van-empty v-if="!loading && !order" description="订单不存在" image="search"/>

    <template v-if="!loading && order">
      <!-- 状态卡：状态标签 + 未付款时的支付倒计时 -->
      <div class="status-card card">
        <div class="status-row">
          <van-tag :type="statusMap[order.status]?.type || 'default'" size="large">
            {{ statusMap[order.status]?.text || '未知状态' }}
          </van-tag>
          <div v-if="order.status === -1" class="status-countdown">
            <van-icon name="clock-o"/>
            <span class="countdown-label">支付剩余时间</span>
            <van-count-down :time="remainTime(order)" format="mm 分 ss 秒" class="countdown"
                            @finish="onCountdownFinish"/>
          </div>
        </div>
        <div v-if="order.status === -1" class="status-tip">请尽快完成支付，超时订单将自动取消</div>
      </div>

      <!-- 收货地址：未付款订单可点击选择/修改（秒杀订单支付前必须选） -->
      <div v-if="order.shipping || order.status === -1" class="address-card card" @click="openAddressPicker">
        <van-icon name="location-o" class="address-icon"/>
        <div class="address-info">
          <template v-if="order.shipping">
            <div class="address-receiver">
              <span class="receiver-name">{{ order.shipping.receiverName }}</span>
              <span class="receiver-mobile">{{ order.shipping.receiverMobile }}</span>
              <van-tag v-if="order.shipping.isDefault === 1" color="#1a1a1a" size="medium">默认</van-tag>
            </div>
            <div class="address-detail ellipsis-2">
              {{ order.shipping.receiverProvince + order.shipping.receiverCity + order.shipping.receiverDistrict + order.shipping.receiverAddress }}
            </div>
          </template>
          <template v-else>
            <div class="address-empty">请选择收货地址</div>
            <div class="address-empty-tip">支付前需要选择收货地址</div>
          </template>
        </div>
        <van-icon v-if="order.status === -1" name="arrow" class="address-arrow"/>
      </div>

      <!-- 商品清单 -->
      <div class="goods-card card">
        <div v-for="item in order.orderItemList" :key="item.id" class="goods-item" @click="toProduct(item)">
          <img class="goods-img" :src="formatImage(item.productImage)" :alt="item.productName">
          <div class="goods-info">
            <div class="goods-name ellipsis-2">{{ item.productName }}</div>
          </div>
          <div class="goods-right">
            <span class="goods-price">{{ formatPrice(item.currentUnitPrice) }}</span>
            <span class="goods-count">×{{ item.quantity }}</span>
          </div>
        </div>
      </div>

      <!-- 金额明细 -->
      <div class="amount-card card">
        <div class="amount-row">
          <span class="amount-label">商品总额</span>
          <span class="amount-value">{{ formatPrice(goodsTotal) }}</span>
        </div>
        <div class="amount-row">
          <span class="amount-label">运费</span>
          <span class="amount-value">{{ formatPrice(order.postage) }}</span>
        </div>
        <div class="amount-row total">
          <span class="amount-label">实付款</span>
          <span class="amount-value price">{{ formatPrice(order.payment) }}</span>
        </div>
      </div>

      <!-- 订单信息 -->
      <div class="info-card card">
        <div class="info-row">
          <span class="info-label">订单编号</span>
          <span class="info-value">{{ order.orderNo }}</span>
        </div>
        <div class="info-row">
          <span class="info-label">下单时间</span>
          <span class="info-value">{{ order.createTime }}</span>
        </div>
        <div v-if="order.paymentTime" class="info-row">
          <span class="info-label">支付时间</span>
          <span class="info-value">{{ order.paymentTime }}</span>
        </div>
        <div v-if="order.sendTime" class="info-row">
          <span class="info-label">发货时间</span>
          <span class="info-value">{{ order.sendTime }}</span>
        </div>
        <div v-if="order.endTime" class="info-row">
          <span class="info-label">交易完成时间</span>
          <span class="info-value">{{ order.endTime }}</span>
        </div>
        <div v-if="order.closeTime" class="info-row">
          <span class="info-label">关闭时间</span>
          <span class="info-value">{{ order.closeTime }}</span>
        </div>
      </div>

      <!-- 未付款订单底部支付栏 -->
      <van-submit-bar v-if="order.status === -1" class="app-fixed" :price="Math.round(order.payment * 100)"
                      label="实付：" button-text="立即支付" @submit="payOrder"/>
      <!-- 已发货订单底部确认收货栏 -->
      <van-submit-bar v-else-if="order.status === 2" class="app-fixed" button-text="确认收货"
                      @submit="confirmReceipt"/>

      <!-- 收货地址选择弹层 -->
      <van-popup v-model:show="addressPickerVisible" position="bottom" round>
        <div class="address-picker">
          <div class="picker-title">选择收货地址</div>
          <div class="picker-list">
            <div v-for="item in addressList" :key="item.id" class="picker-item"
                 :class="{active: order.shipping && order.shipping.id === item.id}" @click="chooseAddress(item)">
              <div class="picker-receiver">
                <span class="receiver-name">{{ item.receiverName }}</span>
                <span class="receiver-mobile">{{ item.receiverMobile }}</span>
                <van-tag v-if="item.isDefault === 1" color="#1a1a1a" size="medium">默认</van-tag>
              </div>
              <div class="picker-address ellipsis-2">
                {{ item.receiverProvince + item.receiverCity + item.receiverDistrict + item.receiverAddress }}
              </div>
            </div>
            <van-empty v-if="addressList.length === 0" description="还没有收货地址" image-size="60"/>
          </div>
          <!-- 去地址管理页新增/编辑地址，返回后刷新列表 -->
          <van-button round block plain class="manage-button" @click="toShippingManage">管理收货地址</van-button>
        </div>
      </van-popup>
    </template>
  </div>
</template>

<style scoped>
  /* 状态卡 */
  .status-card {
    margin: 8px;
    padding: 14px 12px;
  }

  .status-row {
    display: flex;
    align-items: center;
    justify-content: space-between;
  }

  .status-countdown {
    display: flex;
    align-items: center;
    gap: 4px;
    font-size: 12px;
    color: #999;
  }

  .countdown-label {
    margin: 0 2px;
  }

  .countdown {
    color: var(--app-primary);
    font-weight: 600;
  }

  .status-tip {
    margin-top: 8px;
    font-size: 12px;
    color: #999;
  }

  /* 收货地址卡 */
  .address-card {
    display: flex;
    align-items: center;
    gap: 10px;
    margin: 8px;
    padding: 14px 12px;
    cursor: pointer;
  }

  .address-icon {
    flex-shrink: 0;
    font-size: 20px;
    color: var(--app-primary);
  }

  .address-info {
    flex: 1;
    min-width: 0;
  }

  .address-receiver {
    display: flex;
    align-items: center;
    gap: 8px;
  }

  .receiver-name {
    font-size: 14px;
    font-weight: 600;
    color: #1a1a1a;
  }

  .receiver-mobile {
    font-size: 13px;
    color: #666;
  }

  .address-detail {
    margin-top: 4px;
    font-size: 13px;
    color: #606266;
    line-height: 1.4;
  }

  .address-empty {
    font-size: 14px;
    font-weight: 600;
    color: var(--app-primary);
  }

  .address-empty-tip {
    margin-top: 4px;
    font-size: 12px;
    color: #999;
  }

  .address-arrow {
    flex-shrink: 0;
    color: #c8c9cc;
  }

  /* 收货地址选择弹层 */
  .address-picker {
    padding: 16px 12px calc(16px + env(safe-area-inset-bottom));
  }

  .picker-title {
    text-align: center;
    font-size: 15px;
    font-weight: 600;
    color: #1a1a1a;
  }

  .picker-list {
    max-height: 50vh;
    overflow-y: auto;
    margin-top: 12px;
  }

  .picker-item {
    padding: 12px;
    border: 1px solid #f0f0f0;
    border-radius: 8px;
    margin-bottom: 8px;
    cursor: pointer;
  }

  .picker-item.active {
    border-color: var(--app-primary);
  }

  .picker-receiver {
    display: flex;
    align-items: center;
    gap: 8px;
  }

  .picker-address {
    margin-top: 4px;
    font-size: 13px;
    color: #606266;
    line-height: 1.4;
  }

  /* 管理收货地址按钮 */
  .manage-button {
    margin-top: 4px;
    color: var(--app-primary);
    border-color: var(--app-primary);
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
    cursor: pointer;
  }

  .goods-item:last-child {
    border-bottom: none;
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

  .goods-right {
    display: flex;
    flex-direction: column;
    align-items: flex-end;
    flex-shrink: 0;
  }

  .goods-price {
    font-size: 14px;
    color: #303133;
  }

  .goods-count {
    margin-top: 4px;
    font-size: 12px;
    color: #999;
  }

  /* 金额明细 */
  .amount-card {
    margin: 8px;
    padding: 12px;
  }

  .amount-row {
    display: flex;
    align-items: center;
    justify-content: space-between;
    padding: 4px 0;
    font-size: 13px;
  }

  .amount-label {
    color: #999;
  }

  .amount-value {
    color: #303133;
  }

  .amount-row.total {
    margin-top: 6px;
    padding-top: 10px;
    border-top: 1px solid #f5f5f5;
    font-size: 14px;
  }

  .amount-row.total .price {
    color: var(--app-primary);
    font-size: 17px;
    font-weight: 600;
  }

  /* 订单信息 */
  .info-card {
    margin: 8px;
    padding: 12px;
  }

  .info-row {
    display: flex;
    align-items: baseline;
    justify-content: space-between;
    gap: 12px;
    padding: 4px 0;
    font-size: 13px;
  }

  .info-label {
    flex-shrink: 0;
    color: #999;
  }

  .info-value {
    color: #606266;
    word-break: break-all;
    text-align: right;
  }
</style>
<script setup>
  import {ref, onMounted, onUnmounted} from 'vue'
  import {useRouter} from 'vue-router'
  import {showToast, showSuccessToast} from 'vant'
  import seckillApi from '@/api/seckill/seckill.js'
  import orderApi from '@/api/order/order.js'
  import PageHeader from '@/components/PageHeader.vue'
  import {formatImage, formatPrice} from '@/utils/format.js'

  const router = useRouter()

  //活动列表（每项含 productList，stock 为 Redis 实时库存）
  const activityList = ref([])
  //本地时钟，每秒刷新，按活动时间渲染三态
  const now = ref(Date.now())
  //每个活动上一次的状态，用于跨过开始/结束边界时刷新数据
  const prevStateMap = ref({})
  let clockTimer = null

  const loadData = () => {
    seckillApi.listPublished().then(result => {
      if (result.code === 1) {
        activityList.value = result.data || []
        //记录当前状态快照
        const stateMap = {}
        activityList.value.forEach(activity => {
          stateMap[activity.id] = getState(activity)
        })
        prevStateMap.value = stateMap
      }
    })
  }
  loadData()

  //解析后端时间字符串（yyyy-MM-dd HH:mm:ss）：iOS Safari 不认横杠格式，需替换成斜杠
  const parseTime = (time) => new Date(String(time).replace(/-/g, '/')).getTime()

  //活动三态：0未开始 1进行中 2已结束
  const getState = (activity) => {
    const start = parseTime(activity.startTime)
    const end = parseTime(activity.endTime)
    if (now.value < start) {
      return 0
    }
    if (now.value > end) {
      return 2
    }
    return 1
  }

  //倒计时文案：距开始/距结束，mm:ss（超过1小时显示 hh:mm:ss）
  const formatCountdown = (seconds) => {
    const diff = Math.max(0, Math.floor(seconds / 1000))
    const h = Math.floor(diff / 3600)
    const m = Math.floor((diff % 3600) / 60)
    const s = diff % 60
    const pad = (n) => String(n).padStart(2, '0')
    return h > 0 ? `${h}:${pad(m)}:${pad(s)}` : `${pad(m)}:${pad(s)}`
  }

  //每秒刷新本地时钟；有活动跨过开始/结束边界时重新拉数据（按钮态与库存同步）
  onMounted(() => {
    clockTimer = setInterval(() => {
      now.value = Date.now()
      let shouldReload = false
      activityList.value.forEach(activity => {
        if (prevStateMap.value[activity.id] !== getState(activity)) {
          shouldReload = true
        }
      })
      if (shouldReload) {
        loadData()
      }
    }, 1000)
  })
  onUnmounted(() => clearInterval(clockTimer))

  //====================抢购流程====================
  //全局 loading 标志：防按钮重复点击
  const buying = ref(false)

  const buy = (activity, product) => {
    if (buying.value) {
      return
    }
    if (getState(activity) !== 1 || (product.stock || 0) <= 0) {
      return
    }
    buying.value = true
    seckillApi.seckill({activityId: activity.id, productId: product.productId}).then(result => {
      if (result.code === 1) {
        //拿到订单号，轮询订单是否落库（order-service 异步完成，通常几百毫秒）
        pollOrder(result.data, product)
      } else {
        //已售罄/已抢过/活动不在进行中：提示错误文案，刷新列表同步最新库存
        buying.value = false
        showToast(result.msg)
        loadData()
      }
    }).catch(() => {
      buying.value = false
    })
  }

  //每300ms查一次订单是否落库，最多15次（约4.5秒）
  const pollOrder = (orderNo, product) => {
    let count = 0
    const pollTimer = setInterval(() => {
      count++
      orderApi.selectByOrderNo(orderNo).then(result => {
        if (result.code === 1 && result.data) {
          //查到订单，跳转订单详情支付
          clearInterval(pollTimer)
          buying.value = false
          showSuccessToast('抢购成功，请尽快支付')
          router.replace('/order/' + orderNo)
        } else if (count >= 15) {
          //超时：消息还在队列里，订单最终会出现
          clearInterval(pollTimer)
          buying.value = false
          showToast('订单生成中，请稍后到订单列表查看')
          router.replace('/order')
        }
      }).catch(() => {
        if (count >= 15) {
          clearInterval(pollTimer)
          buying.value = false
          showToast('订单生成中，请稍后到订单列表查看')
          router.replace('/order')
        }
      })
    }, 300)
  }

  //按钮文案与禁用态
  const buttonText = (activity, product) => {
    const state = getState(activity)
    if (state === 0) {
      return '未开始'
    }
    if (state === 2) {
      return '已结束'
    }
    return (product.stock || 0) > 0 ? '立即秒杀' : '已售罄'
  }
  const buttonDisabled = (activity, product) => {
    return buying.value || getState(activity) !== 1 || (product.stock || 0) <= 0
  }
</script>

<template>
  <div class="seckill">
    <PageHeader title="限时秒杀"/>

    <!--活动卡片列表-->
    <div v-for="activity in activityList" :key="activity.id" class="activity card">
      <div class="activity-head">
        <div class="activity-name ellipsis-1">{{ activity.name }}</div>
        <div class="activity-countdown">
          <template v-if="getState(activity) === 0">
            <span class="countdown-label">距开始</span>
            <span class="countdown-time">{{ formatCountdown(parseTime(activity.startTime) - now) }}</span>
          </template>
          <template v-else-if="getState(activity) === 1">
            <van-icon name="clock-o"/>
            <span class="countdown-label">距结束</span>
            <span class="countdown-time">{{ formatCountdown(parseTime(activity.endTime) - now) }}</span>
          </template>
          <template v-else>
            <span class="countdown-label">已结束</span>
          </template>
        </div>
      </div>

      <!--活动下秒杀商品列表-->
      <div v-for="product in activity.productList" :key="product.productId" class="seckill-product">
        <img class="product-image" :src="formatImage(product.productImage)" alt="商品图片"
             @error="$event.target.style.display='none'">
        <div class="product-info">
          <div class="product-name ellipsis-2">{{ product.productName }}</div>
          <div class="product-price">
            <span class="seckill-price">{{ formatPrice(product.seckillPrice) }}</span>
            <span class="origin-price">{{ formatPrice(product.productPrice) }}</span>
            <span class="remain-stock">仅剩 {{ product.stock ?? 0 }} 件</span>
          </div>
        </div>
        <van-button round size="small" class="buy-button"
                    :disabled="buttonDisabled(activity, product)"
                    @click="buy(activity, product)">
          {{ buttonText(activity, product) }}
        </van-button>
      </div>
      <!--缓存丢失或活动未配置商品-->
      <div v-if="!activity.productList || activity.productList.length === 0" class="empty-tip">
        暂无秒杀商品
      </div>
    </div>

    <!--无活动-->
    <div v-if="activityList.length === 0" class="empty-tip card">当前没有进行中的秒杀活动</div>
  </div>
</template>

<style scoped>
  .seckill {
    padding-bottom: 12px;
  }

  .activity {
    margin: 8px;
    padding: 12px;
  }

  .activity-head {
    display: flex;
    align-items: center;
    justify-content: space-between;
    gap: 8px;
    padding-bottom: 8px;
    border-bottom: 1px dashed #f0f0f0;
  }

  .activity-name {
    font-size: 15px;
    font-weight: 600;
    color: #1a1a1a;
  }

  .activity-countdown {
    display: flex;
    align-items: center;
    gap: 4px;
    font-size: 12px;
    color: #666;
    white-space: nowrap;
  }

  .countdown-time {
    font-weight: 700;
    color: var(--app-primary);
  }

  .seckill-product {
    display: flex;
    align-items: center;
    gap: 10px;
    padding: 10px 0;
    border-bottom: 1px solid #f5f5f5;
  }

  .seckill-product:last-child {
    border-bottom: none;
  }

  .product-image {
    width: 80px;
    height: 80px;
    object-fit: cover;
    border-radius: 8px;
    background-color: #f5f6f7;
  }

  .product-info {
    flex: 1;
    overflow: hidden;
  }

  .product-name {
    font-size: 13px;
    color: #303133;
    line-height: 1.4;
  }

  .product-price {
    display: flex;
    align-items: baseline;
    gap: 8px;
    margin-top: 8px;
  }

  .seckill-price {
    font-size: 17px;
    font-weight: 700;
    color: var(--app-primary);
  }

  .origin-price {
    font-size: 12px;
    color: #999;
    text-decoration: line-through;
  }

  .remain-stock {
    font-size: 11px;
    color: #999;
  }

  .buy-button {
    width: 76px;
    border: none;
    background: var(--app-primary);
    color: #fff;
    font-weight: 600;
  }

  .buy-button:disabled {
    background: #c8c9cc;
    color: #fff;
  }

  .empty-tip {
    padding: 24px 12px;
    text-align: center;
    font-size: 13px;
    color: #999;
  }
</style>

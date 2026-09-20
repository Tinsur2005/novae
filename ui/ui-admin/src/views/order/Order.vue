<template>
  <el-card shadow="never">
    <!-- 搜索栏 -->
    <el-form :inline="true" :model="searchForm" class="search-form">
      <el-form-item label="订单号">
        <el-input v-model="searchForm.orderNo" placeholder="请输入订单号" clearable @keyup.enter="handleSearch"/>
      </el-form-item>
      <el-form-item label="用户ID">
        <el-input v-model="searchForm.userId" placeholder="请输入用户ID" clearable @keyup.enter="handleSearch"/>
      </el-form-item>
      <el-form-item label="订单状态">
        <el-select v-model="searchForm.status" placeholder="全部状态" clearable style="width: 140px">
          <el-option v-for="(item, key) in statusMap" :key="key" :label="item.text" :value="Number(key)"/>
        </el-select>
      </el-form-item>
      <el-form-item label="下单时间">
        <el-date-picker
            v-model="searchForm.dateRange"
            type="daterange"
            range-separator="至"
            start-placeholder="开始日期"
            end-placeholder="结束日期"
            value-format="YYYY-MM-DD"
        />
      </el-form-item>
      <el-form-item>
        <el-button type="primary" :icon="'Search'" @click="handleSearch">查询</el-button>
        <el-button :icon="'Refresh'" @click="handleReset">重置</el-button>
      </el-form-item>
    </el-form>

    <!-- 数据表格 -->
    <el-table :data="tableData" border stripe v-loading="loading">
      <el-table-column prop="orderNo" label="订单号" width="200" align="center"/>
      <el-table-column prop="userId" label="用户ID" width="90" align="center"/>
      <el-table-column label="商品明细" min-width="180">
        <template #default="{ row }">
          <el-button type="primary" link :icon="'Tickets'" @click="handleDetail(row)">
            查看明细（{{ (row.orderItemList || []).length }}）
          </el-button>
        </template>
      </el-table-column>
      <el-table-column label="实付金额" width="110" align="center">
        <template #default="{ row }">
          <span class="order-payment">{{ formatPrice(row.payment) }}</span>
        </template>
      </el-table-column>
      <el-table-column label="运费" width="90" align="center">
        <template #default="{ row }">{{ formatPrice(row.postage) }}</template>
      </el-table-column>
      <el-table-column label="支付方式" width="100" align="center">
        <template #default="{ row }">{{ paymentTypeMap[row.paymentType] || '未知' }}</template>
      </el-table-column>
      <el-table-column label="订单状态" width="100" align="center">
        <template #default="{ row }">
          <el-tag :type="statusMap[row.status]?.type || 'info'">
            {{ statusMap[row.status]?.text || '未知状态' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="下单时间" width="170" align="center">
        <template #default="{ row }">{{ formatTime(row.createTime) }}</template>
      </el-table-column>
      <el-table-column label="发货时间" width="170" align="center">
        <template #default="{ row }">{{ formatTime(row.sendTime) }}</template>
      </el-table-column>
      <el-table-column label="操作" width="170" align="center" fixed="right">
        <template #default="{ row }">
          <el-button type="primary" link :icon="'Van'" :disabled="!canSend(row)" @click="handleSend(row)">发货</el-button>
          <el-button type="warning" link :icon="'CircleClose'" :disabled="!canClose(row)" @click="handleClose(row)">
            关闭
          </el-button>
          <el-button type="danger" link :icon="'Delete'" @click="handleDelete(row)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <!-- 分页 -->
    <el-pagination
        class="pagination"
        v-model:current-page="page.current"
        v-model:page-size="page.limit"
        :page-sizes="[10, 20, 50, 100]"
        :total="page.total"
        layout="total, sizes, prev, pager, next, jumper"
        @size-change="handleSizeChange"
        @current-change="handleCurrentChange"
    />
  </el-card>

  <!-- 订单明细弹窗 -->
  <el-dialog v-model="detailVisible" title="订单明细" width="720px" destroy-on-close>
    <el-descriptions :column="2" border class="detail-desc">
      <el-descriptions-item label="订单号">{{ detailOrder.orderNo }}</el-descriptions-item>
      <el-descriptions-item label="订单状态">
        <el-tag :type="statusMap[detailOrder.status]?.type || 'info'">
          {{ statusMap[detailOrder.status]?.text || '未知状态' }}
        </el-tag>
      </el-descriptions-item>
      <el-descriptions-item label="实付金额">{{ formatPrice(detailOrder.payment) }}</el-descriptions-item>
      <el-descriptions-item label="运费">{{ formatPrice(detailOrder.postage) }}</el-descriptions-item>
      <el-descriptions-item label="下单时间">{{ formatTime(detailOrder.createTime) }}</el-descriptions-item>
      <el-descriptions-item label="发货时间">{{ formatTime(detailOrder.sendTime) }}</el-descriptions-item>
    </el-descriptions>

    <el-table :data="detailOrder.orderItemList || []" border stripe class="detail-table">
      <el-table-column label="商品图片" width="90" align="center">
        <template #default="{ row }">
          <el-image class="item-image" :src="row.productImage" fit="contain" :preview-src-list="[row.productImage]"/>
        </template>
      </el-table-column>
      <el-table-column prop="productName" label="商品名称" min-width="160" show-overflow-tooltip/>
      <el-table-column label="单价" width="100" align="center">
        <template #default="{ row }">{{ formatPrice(row.currentUnitPrice) }}</template>
      </el-table-column>
      <el-table-column prop="quantity" label="数量" width="80" align="center"/>
      <el-table-column label="小计" width="110" align="center">
        <template #default="{ row }">{{ formatPrice(row.totalPrice) }}</template>
      </el-table-column>
    </el-table>
  </el-dialog>
</template>

<script setup>
import {onMounted, reactive, ref} from 'vue'
import {ElMessage, ElMessageBox} from 'element-plus'
import dayjs from 'dayjs'
import orderApi from '@/api/order/order.js'

// 订单状态：-1-未付款 0-已取消 1-待发货 2-已发货 3-交易成功 4-交易关闭 5-已退款
const statusMap = {
  '-1': {text: '待付款', type: 'warning'},
  0: {text: '已取消', type: 'info'},
  1: {text: '待发货', type: 'primary'},
  2: {text: '已发货', type: 'primary'},
  3: {text: '交易成功', type: 'success'},
  4: {text: '交易关闭', type: 'danger'},
  5: {text: '已退款', type: 'info'}
}
// 支付方式：1-微信 2-支付宝 3-其他
const paymentTypeMap = {1: '微信', 2: '支付宝', 3: '其他'}

// ---------------- 列表查询 ----------------
const loading = ref(false)
const tableData = ref([])
const searchForm = reactive({
  orderNo: '',
  userId: '',
  status: null,
  dateRange: []
})
const page = reactive({
  current: 1,
  limit: 10,
  total: 0
})

const formatTime = (time) => time ? dayjs(time).format('YYYY-MM-DD HH:mm:ss') : ''
const formatPrice = (price) => '¥' + Number(price || 0).toFixed(2)

const loadData = async () => {
  loading.value = true
  try {
    const params = {
      page: page.current,
      limit: page.limit
    }
    if (searchForm.orderNo) params.orderNo = searchForm.orderNo
    if (searchForm.userId) params.userId = searchForm.userId
    if (searchForm.status !== null && searchForm.status !== '') params.status = searchForm.status
    if (searchForm.dateRange && searchForm.dateRange.length === 2) {
      params.beginCreateTime = searchForm.dateRange[0]
      params.endCreateTime = searchForm.dateRange[1]
    }
    const res = await orderApi.list(params)
    if (res.code === 1) {
      tableData.value = res.data.records || []
      page.total = Number(res.data.total) || 0
    } else {
      ElMessage.error(res.msg || '查询失败')
    }
  } catch (e) {
    /* 拦截器已统一提示 */
  } finally {
    loading.value = false
  }
}

onMounted(loadData)

const handleSearch = () => {
  page.current = 1
  loadData()
}

const handleReset = () => {
  searchForm.orderNo = ''
  searchForm.userId = ''
  searchForm.status = null
  searchForm.dateRange = []
  handleSearch()
}

const handleSizeChange = () => {
  page.current = 1
  loadData()
}

const handleCurrentChange = () => {
  loadData()
}

// ---------------- 订单明细 ----------------
const detailVisible = ref(false)
const detailOrder = ref({})

const handleDetail = (row) => {
  detailOrder.value = row
  detailVisible.value = true
}

// ---------------- 发货 / 关闭 / 删除 ----------------
// 待付款(-1)和待发货(1)的订单都可以发货
const canSend = (row) => row.status === -1 || row.status === 1
const canClose = (row) => row.status === -1 || row.status === 1 || row.status === 2

const handleSend = (row) => {
  ElMessageBox.confirm(`确定要给订单「${row.orderNo}」发货吗？`, '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    const res = await orderApi.send(row.orderNo)
    if (res.code === 1) {
      ElMessage.success('发货成功')
      loadData()
    } else {
      ElMessage.error(res.msg || '发货失败')
    }
  }).catch(() => {
  })
}

const handleClose = (row) => {
  ElMessageBox.confirm(`确定要关闭订单「${row.orderNo}」吗？关闭后下单时扣掉的库存会加回去`, '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    const res = await orderApi.close(row.orderNo)
    if (res.code === 1) {
      ElMessage.success('关闭成功')
      loadData()
    } else {
      ElMessage.error(res.msg || '关闭失败')
    }
  }).catch(() => {
  })
}

const handleDelete = (row) => {
  ElMessageBox.confirm(`确定要删除订单「${row.orderNo}」吗？`, '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    const res = await orderApi.deleteById(row.orderNo)
    if (res.code === 1) {
      ElMessage.success('删除成功')
      loadData()
    } else {
      ElMessage.error(res.msg || '删除失败')
    }
  }).catch(() => {
  })
}
</script>

<style scoped>
.order-payment {
  color: #f56c6c;
  font-weight: 600;
}

.detail-desc {
  margin-bottom: 16px;
}

.detail-table .item-image {
  width: 56px;
  height: 56px;
}
</style>
<script setup>
  import activityApi from '@/api/seckill/activity.js'
  import productApi from '@/api/product/product.js'
  import {ref} from 'vue'
  import {ElMessage, ElMessageBox} from 'element-plus'

  //表格数据
  const list = ref([])
  const total = ref(0)
  //分页信息和搜索条件
  const activityQuery = ref({
    name: '',
    status: null,
    page: 1,
    limit: 10
  })

  const loadData = () => {
    activityApi.list(activityQuery.value).then(result => {
      list.value = result.data.records
      total.value = result.data.total
    })
  }
  loadData()

  const onSearch = () => {
    activityQuery.value.page = 1
    loadData()
  }

  //列表四态实时计算：未发布（草稿） / 未开始 / 进行中 / 已结束
  const parseTime = (time) => new Date(String(time).replace(/-/g, '/')).getTime()
  const getState = (row) => {
    if (row.status !== 1) {
      return {text: '未发布', type: 'info'}
    }
    const now = Date.now()
    const start = parseTime(row.startTime)
    const end = parseTime(row.endTime)
    if (now < start) {
      return {text: '未开始', type: 'warning'}
    }
    if (now > end) {
      return {text: '已结束', type: 'info'}
    }
    return {text: '进行中', type: 'success'}
  }

  //发布状态
  const statusMap = {0: '未发布', 1: '已发布'}

  //删除活动（连同商品与缓存一起删除）
  const deleteById = (id) => {
    ElMessageBox.confirm(
        '删除活动会同时删除活动下商品与秒杀缓存，确认删除么?',
        '提示',
        {
          confirmButtonText: '确认',
          cancelButtonText: '取消',
          type: 'warning',
          lockScroll: false //防止抖动
        }
    ).then(() => {
      activityApi.deleteById(id).then(result => {
        if (result.code === 1) {
          ElMessage.success(result.msg)
          loadData()
        } else {
          ElMessage.error(result.msg)
        }
      })
    })
  }

  //====================新增/编辑弹窗====================
  const dialogFormVisible = ref(false)
  const title = ref()
  //活动表单：name、起止时间、状态下拉、商品列表
  const activity = ref({})
  const timeRange = ref([])

  const showAddDialog = () => {
    dialogFormVisible.value = true
    title.value = '添加活动'
    activity.value = {status: 0, productList: []}
    timeRange.value = []
    selectedProductIds.value = []
    productOptions.value = []
  }

  const showUpdateDialog = (id) => {
    dialogFormVisible.value = true
    title.value = '编辑活动'
    activity.value = {}
    //活动回显 + 活动下商品回显
    activityApi.selectById(id).then(result => {
      Object.assign(activity.value, result.data, {productList: []})
      timeRange.value = [result.data.startTime, result.data.endTime]
      activityApi.listProduct(id).then(productResult => {
        activity.value.productList = productResult.data
        //回显商品进下拉options，否则el-select只显示原始ID
        productOptions.value = productResult.data.map(product => ({
          id: product.productId,
          name: product.productName,
          mainImage: product.productImage,
          price: product.productPrice
        }))
        selectedProductIds.value = productResult.data.map(product => product.productId)
      })
    })
  }

  //====================商品远程搜索====================
  //远程搜索的商品选项
  const productOptions = ref([])
  //已选商品ID（el-select multiple 的 value）
  const selectedProductIds = ref([])
  const searching = ref(false)

  const searchProduct = (keyword) => {
    if (!keyword) {
      productOptions.value = []
      return
    }
    searching.value = true
    productApi.list({page: 1, limit: 20, name: keyword}).then(result => {
      //合并已回显的商品，避免编辑时下拉里丢失已选项
      const merged = [...result.data.records]
      activity.value.productList.forEach(selected => {
        if (!merged.some(item => item.id === selected.productId)) {
          merged.push({
            id: selected.productId,
            name: selected.productName,
            mainImage: selected.productImage,
            price: selected.productPrice
          })
        }
      })
      productOptions.value = merged
    }).finally(() => {
      searching.value = false
    })
  }

  //选中/取消选中商品：维护 productList（快照自动捕获）
  const changeProduct = (ids) => {
    const productList = []
    ids.forEach(id => {
      //已在列表中的保留（保留用户填写的秒杀价/库存）
      const exist = activity.value.productList.find(product => product.productId === id)
      if (exist) {
        productList.push(exist)
      } else {
        const product = productOptions.value.find(item => item.id === id)
        //快照自动捕获：商品名称/图片/普通价在配置时从商品服务数据复制
        productList.push({
          productId: product.id,
          productName: product.name,
          productImage: product.mainImage,
          productPrice: product.price,
          seckillPrice: null,
          totalStock: null
        })
      }
    })
    activity.value.productList = productList
  }

  //移除商品（同步下拉选中值与表格）
  const removeProduct = (productId) => {
    selectedProductIds.value = selectedProductIds.value.filter(id => id !== productId)
    activity.value.productList = activity.value.productList.filter(product => product.productId !== productId)
  }

  //提交前校验
  const validate = () => {
    if (!activity.value.name) {
      ElMessage.warning('请输入活动名称')
      return false
    }
    if (!timeRange.value || timeRange.value.length !== 2) {
      ElMessage.warning('请选择活动起止时间')
      return false
    }
    if (!activity.value.productList || activity.value.productList.length === 0) {
      ElMessage.warning('请至少配置一个秒杀商品')
      return false
    }
    //重复商品校验（uk_activity_product 唯一键是服务端兜底）
    const idSet = new Set()
    for (const product of activity.value.productList) {
      if (idSet.has(product.productId)) {
        ElMessage.warning('活动内商品不能重复')
        return false
      }
      idSet.add(product.productId)
      if (product.seckillPrice == null || product.seckillPrice <= 0) {
        ElMessage.warning(`请为商品【${product.productName}】填写大于0的秒杀价`)
        return false
      }
      if (product.totalStock == null || product.totalStock <= 0) {
        ElMessage.warning(`请为商品【${product.productName}】填写大于0的限量库存`)
        return false
      }
    }
    return true
  }

  const addOrUpdate = () => {
    if (!validate()) {
      return
    }
    activity.value.startTime = timeRange.value[0]
    activity.value.endTime = timeRange.value[1]
    if (activity.value.id) {
      activityApi.update(activity.value.id, activity.value).then(result => {
        if (result.code === 1) {
          ElMessage.success(result.msg)
          dialogFormVisible.value = false
          loadData()
        } else {
          ElMessage.error(result.msg)
        }
      })
    } else {
      activityApi.add(activity.value).then(result => {
        if (result.code === 1) {
          ElMessage.success(result.msg)
          dialogFormVisible.value = false
          loadData()
        } else {
          ElMessage.error(result.msg)
        }
      })
    }
  }
</script>

<template>
  <el-card>
    <el-form :inline="true">
      <el-form-item label="活动名称">
        <el-input v-model="activityQuery.name" placeholder="请输入活动名称" clearable style="width: 200px"/>
      </el-form-item>
      <el-form-item label="状态">
        <el-select v-model="activityQuery.status" placeholder="请选择状态" clearable style="width: 140px">
          <el-option
              v-for="(text, key) in statusMap"
              :key="key"
              :label="text"
              :value="Number(key)"
          />
        </el-select>
      </el-form-item>
      <el-form-item>
        <el-button type="primary" @click="onSearch">搜索</el-button>
      </el-form-item>
      <el-form-item>
        <el-button type="success" @click="showAddDialog">添加活动</el-button>
      </el-form-item>
    </el-form>
    <el-table :data="list" border style="width: 100%" show-overflow-tooltip>
      <el-table-column prop="id" label="活动ID" width="200"/>
      <el-table-column prop="name" label="活动名称" width="200"/>
      <el-table-column prop="startTime" label="开始时间" width="180"/>
      <el-table-column prop="endTime" label="结束时间" width="180"/>
      <el-table-column prop="status" label="发布状态" width="100">
        <template #default="{row}">
          <el-tag>{{ statusMap[row.status] }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="活动状态（实时）" width="120">
        <template #default="{row}">
          <el-tag :type="getState(row).type">{{ getState(row).text }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="createTime" label="创建时间" width="180"/>
      <el-table-column align="center" width="180px" fixed="right" label="操作">
        <template #default="{ row }">
          <el-button size="small" type="primary" @click="showUpdateDialog(row.id)">编辑</el-button>
          <el-button size="small" type="danger" @click="deleteById(row.id)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>
    <el-pagination
        v-model:current-page="activityQuery.page"
        v-model:page-size="activityQuery.limit"
        :page-sizes="[10, 20, 30, 40]"
        layout="total, sizes, prev, pager, next, jumper"
        :total="total"
        @change="loadData"
        style="margin-top: 20px; justify-content: flex-end"
    />
  </el-card>

  <!--添加/编辑活动弹窗-->
  <el-dialog v-model="dialogFormVisible" :title="title" width="800" :lock-scroll="false">
    <el-form label-width="100px">
      <el-form-item label="活动名称">
        <el-input v-model="activity.name" placeholder="请输入活动名称" autocomplete="off"/>
      </el-form-item>
      <el-form-item label="活动时间">
        <el-date-picker
            v-model="timeRange"
            type="datetimerange"
            value-format="YYYY-MM-DD HH:mm:ss"
            range-separator="至"
            start-placeholder="开始时间"
            end-placeholder="结束时间"
        />
      </el-form-item>
      <el-form-item label="发布状态">
        <!--发布是显式动作；"已结束"由时间派生，不在表单中提供-->
        <el-select v-model="activity.status" style="width: 200px">
          <el-option label="未发布（草稿，前台不可见）" :value="0"/>
          <el-option label="已发布（前台可见）" :value="1"/>
        </el-select>
        <span style="margin-left: 10px; color: #999; font-size: 12px;">
          编辑已发布活动会重建缓存：库存重置、已购记录清空
        </span>
      </el-form-item>
      <el-form-item label="秒杀商品">
        <el-select
            v-model="selectedProductIds"
            multiple
            filterable
            remote
            reserve-keyword
            :remote-method="searchProduct"
            :loading="searching"
            placeholder="输入商品名称搜索并添加"
            style="width: 100%"
            @change="changeProduct"
        >
          <el-option
              v-for="product in productOptions"
              :key="product.id"
              :label="product.name"
              :value="product.id"
          />
        </el-select>
      </el-form-item>
    </el-form>
    <!--已配置的秒杀商品列表：快照只读展示，秒杀价/库存可编辑-->
    <el-table v-if="activity.productList && activity.productList.length > 0"
              :data="activity.productList" border style="width: 100%">
      <el-table-column prop="productImage" label="图片" width="80">
        <template #default="{row}">
          <img :src="row.productImage" style="max-height: 40px; max-width: 60px;" alt="商品图片"/>
        </template>
      </el-table-column>
      <el-table-column prop="productName" label="商品名称（快照）"/>
      <el-table-column prop="productPrice" label="普通价（快照）" width="120"/>
      <el-table-column label="秒杀价" width="140">
        <template #default="{row}">
          <el-input-number v-model="row.seckillPrice" :min="0.01" :precision="2" :controls="false" style="width: 120px"/>
        </template>
      </el-table-column>
      <el-table-column label="限量库存" width="140">
        <template #default="{row}">
          <el-input-number v-model="row.totalStock" :min="1" :precision="0" :controls="false" style="width: 120px"/>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="80">
        <template #default="{row}">
          <el-button size="small" type="danger" @click="removeProduct(row.productId)">移除</el-button>
        </template>
      </el-table-column>
    </el-table>
    <template #footer>
      <div class="dialog-footer">
        <el-button @click="dialogFormVisible = false">取消</el-button>
        <el-button type="primary" @click="addOrUpdate">确认</el-button>
      </div>
    </template>
  </el-dialog>
</template>

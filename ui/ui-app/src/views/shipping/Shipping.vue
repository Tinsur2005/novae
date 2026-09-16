<script setup>
  import {ref, onMounted} from 'vue'
  import {showToast, showSuccessToast, showConfirmDialog} from 'vant'
  import shippingApi from '@/api/shipping/shipping.js'
  import PageHeader from '@/components/PageHeader.vue'

  //收货地址列表
  const list = ref([])
  const loading = ref(true)

  const loadData = () => {
    loading.value = true
    shippingApi.list().then(result => {
      if (result.code === 1) {
        list.value = result.data || []
      }
      loading.value = false
    }).catch(() => {
      loading.value = false
    })
  }
  onMounted(loadData)

  //新增/编辑弹层
  const dialogVisible = ref(false)
  const isEdit = ref(false)
  const title = ref('新增收货地址')
  const shippingForm = ref({})

  const showAdd = () => {
    isEdit.value = false
    title.value = '新增收货地址'
    shippingForm.value = {isDefault: 0}
    dialogVisible.value = true
  }

  const showEdit = (shipping) => {
    isEdit.value = true
    title.value = '编辑收货地址'
    //拷贝一份，避免修改弹层时直接改动列表数据
    shippingForm.value = {...shipping}
    dialogVisible.value = true
  }

  //表单校验模型
  const rules = ref({
    receiverName: [
      {required: true, message: '请输入收货人姓名'}
    ],
    receiverMobile: [
      {required: true, message: '请输入手机号'},
      {pattern: /^1\d{10}$/, message: '手机号格式不正确'}
    ],
    receiverProvince: [
      {required: true, message: '请输入省份'}
    ],
    receiverCity: [
      {required: true, message: '请输入城市'}
    ],
    receiverDistrict: [
      {required: true, message: '请输入区/县'}
    ],
    receiverAddress: [
      {required: true, message: '请输入详细地址'}
    ]
  })

  const submit = (values) => {
    const request = isEdit.value
        ? shippingApi.update(shippingForm.value.id, shippingForm.value)
        : shippingApi.add(shippingForm.value)
    request.then(result => {
      if (result.code === 1) {
        showSuccessToast(result.msg || '保存成功')
        dialogVisible.value = false
        loadData()
      } else {
        showToast(result.msg || '保存失败')
      }
    })
  }

  //设为默认地址
  const setDefault = (shipping) => {
    shippingApi.setDefault(shipping.id).then(result => {
      if (result.code === 1) {
        showSuccessToast('已设为默认')
        loadData()
      } else {
        showToast(result.msg || '设置失败')
      }
    })
  }

  //删除收货地址
  const deleteById = (shipping) => {
    showConfirmDialog({
      title: '提示',
      message: '确认删除该收货地址吗？',
      confirmButtonText: '确认',
      cancelButtonText: '取消'
    }).then(() => {
      shippingApi.deleteById(shipping.id).then(result => {
        if (result.code === 1) {
          showSuccessToast('删除成功')
          loadData()
        } else {
          showToast(result.msg || '删除失败')
        }
      })
    }).catch(() => {
    })
  }
</script>

<template>
  <div class="shipping pb-action">
    <PageHeader title="收货地址">
      <template #right>
        <span class="add-link" @click="showAdd">新增</span>
      </template>
    </PageHeader>

    <!-- 加载中 -->
    <div v-if="loading" class="list-loading">加载中...</div>

    <!-- 空状态 -->
    <van-empty v-if="!loading && list.length === 0" description="暂无收货地址" image="location">
      <van-button round type="primary" class="gradient-button" @click="showAdd">新增地址</van-button>
    </van-empty>

    <!-- 地址列表 -->
    <div v-if="!loading" class="shipping-list">
      <div v-for="shipping in list" :key="shipping.id" class="shipping-card card">
        <div class="shipping-row">
          <span class="shipping-name">{{ shipping.receiverName }}</span>
          <span class="shipping-mobile">{{ shipping.receiverMobile }}</span>
          <van-tag v-if="shipping.isDefault === 1" round color="linear-gradient(135deg, #ff6ec4, #a854f7)">
            默认
          </van-tag>
        </div>
        <div class="shipping-detail">
          {{ [shipping.receiverProvince, shipping.receiverCity, shipping.receiverDistrict,
              shipping.receiverAddress].filter(Boolean).join(' ') }}
        </div>
        <div class="shipping-ops">
          <van-button v-if="shipping.isDefault !== 1" size="small" plain round @click="setDefault(shipping)">设为默认</van-button>
          <van-button size="small" plain round type="primary" @click="showEdit(shipping)">编辑</van-button>
          <van-button size="small" plain round type="danger" @click="deleteById(shipping)">删除</van-button>
        </div>
      </div>
    </div>

    <!-- 新增/编辑弹层：从底部弹出 -->
    <van-popup v-model:show="dialogVisible" round position="bottom" :style="{maxHeight: '88%'}">
      <div class="popup-title">{{ title }}</div>
      <van-form class="popup-form" @submit="submit">
        <van-cell-group inset>
          <van-field v-model="shippingForm.receiverName" label="收货人" placeholder="请输入收货人姓名"
                     :rules="rules.receiverName" clearable/>
          <van-field v-model="shippingForm.receiverMobile" label="手机号" placeholder="请输入手机号"
                     type="tel" maxlength="11" :rules="rules.receiverMobile"/>
          <van-field v-model="shippingForm.receiverProvince" label="省份" placeholder="省份" :rules="rules.receiverProvince"/>
          <van-field v-model="shippingForm.receiverCity" label="城市" placeholder="城市" :rules="rules.receiverCity"/>
          <van-field v-model="shippingForm.receiverDistrict" label="区/县" placeholder="区/县" :rules="rules.receiverDistrict"/>
          <van-field v-model="shippingForm.receiverAddress" label="详细地址" placeholder="街道、门牌号等"
                     type="textarea" rows="2" autosize :rules="rules.receiverAddress"/>
          <van-field label="默认地址">
            <template #input>
              <van-switch v-model="shippingForm.isDefault" :active-value="1" :inactive-value="0" size="22"/>
            </template>
          </van-field>
        </van-cell-group>
        <div class="popup-buttons">
          <van-button block round @click="dialogVisible = false">取消</van-button>
          <van-button block round type="primary" class="gradient-button" native-type="submit">保存</van-button>
        </div>
      </van-form>
    </van-popup>
  </div>
</template>

<style scoped>
  .add-link {
    color: var(--app-primary);
    font-weight: 500;
  }

  .shipping-list {
    padding: 8px;
  }

  .shipping-card {
    padding: 14px 12px;
    margin-bottom: 8px;
  }

  .shipping-row {
    display: flex;
    align-items: center;
    gap: 8px;
  }

  .shipping-name {
    font-size: 15px;
    font-weight: 600;
    color: #303133;
  }

  .shipping-mobile {
    font-size: 13px;
    color: #666;
  }

  .shipping-detail {
    margin-top: 6px;
    font-size: 13px;
    color: #606266;
    line-height: 1.5;
  }

  .shipping-ops {
    display: flex;
    justify-content: flex-end;
    gap: 8px;
    margin-top: 10px;
  }

  /* 底部弹层 */
  .popup-title {
    padding: 16px 0 8px;
    text-align: center;
    font-size: 16px;
    font-weight: 600;
    color: #303133;
  }

  .popup-buttons {
    display: flex;
    gap: 12px;
    padding: 16px;
  }
</style>

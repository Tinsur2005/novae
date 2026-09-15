<template>
  <el-card shadow="never">
    <!-- 搜索栏 -->
    <el-form :inline="true" :model="searchForm" class="search-form">
      <el-form-item label="名称">
        <el-input v-model="searchForm.name" placeholder="请输入商品名称" clearable @keyup.enter="handleSearch"/>
      </el-form-item>
      <el-form-item label="分类">
        <el-cascader
            v-model="searchForm.categoryId"
            :options="categoryTree"
            :props="categoryProps"
            placeholder="请选择二级分类"
            clearable
            style="width: 200px"
        />
      </el-form-item>
      <el-form-item label="创建时间">
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

    <!-- 操作栏 -->
    <div class="toolbar">
      <el-button type="primary" :icon="'Plus'" @click="handleAdd">新增</el-button>
      <el-button type="danger" :icon="'Delete'" :disabled="selection.length === 0" @click="handleDeleteBatch">
        批量删除
      </el-button>
    </div>

    <!-- 数据表格 -->
    <el-table :data="tableData" border stripe v-loading="loading" @selection-change="handleSelectionChange">
      <el-table-column type="selection" width="50" align="center"/>
      <el-table-column prop="id" label="ID" width="70" align="center"/>
      <el-table-column label="主图" width="90" align="center">
        <template #default="{ row }">
          <el-image
              v-if="row.mainImage"
              :src="row.mainImage"
              :preview-src-list="[row.mainImage]"
              preview-teleported
              fit="cover"
              style="width: 48px; height: 48px; border-radius: 4px"
          />
          <span v-else>-</span>
        </template>
      </el-table-column>
      <el-table-column prop="name" label="商品名称" min-width="160" show-overflow-tooltip/>
      <el-table-column prop="categoryName" label="所属分类" width="130" show-overflow-tooltip/>
      <el-table-column label="类型" width="90" align="center">
        <template #default="{ row }">
          <el-tag :type="row.type === 2 ? 'warning' : 'primary'">{{ row.type === 2 ? '盲盒' : '普通商品' }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="price" label="价格(元)" width="100" align="center"/>
      <el-table-column prop="stock" label="库存" width="80" align="center"/>
      <el-table-column label="状态" width="90" align="center">
        <template #default="{ row }">
          <el-switch
              v-model="row.status"
              :active-value="1"
              :inactive-value="0"
              :before-change="() => beforeStatusChange(row)"
              @change="handleStatusChange(row)"
          />
        </template>
      </el-table-column>
      <el-table-column label="创建时间" width="170" align="center">
        <template #default="{ row }">{{ formatTime(row.createTime) }}</template>
      </el-table-column>
      <el-table-column label="操作" width="160" align="center" fixed="right">
        <template #default="{ row }">
          <el-button type="primary" link :icon="'Edit'" @click="handleEdit(row)">编辑</el-button>
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

  <!-- 新增 / 编辑弹窗 -->
  <el-dialog v-model="dialogVisible" :title="form.id ? '编辑商品' : '新增商品'" width="560px" destroy-on-close>
    <el-form ref="formRef" :model="form" :rules="rules" label-width="90px">
      <el-form-item label="商品名称" prop="name">
        <el-input v-model="form.name" placeholder="请输入商品名称"/>
      </el-form-item>
      <el-form-item label="副标题" prop="subtitle">
        <el-input v-model="form.subtitle" placeholder="请输入副标题"/>
      </el-form-item>
      <el-form-item label="分类" prop="categoryId">
        <el-cascader
            v-model="form.categoryId"
            :options="categoryTree"
            :props="categoryProps"
            placeholder="请选择二级分类"
            style="width: 100%"
        />
      </el-form-item>
      <el-form-item label="商品类型" prop="type">
        <el-radio-group v-model="form.type">
          <el-radio :value="1">普通商品</el-radio>
          <el-radio :value="2">盲盒</el-radio>
        </el-radio-group>
      </el-form-item>
      <el-form-item label="价格(元)" prop="price">
        <el-input-number v-model="form.price" :min="0" :precision="2" :step="1"/>
      </el-form-item>
      <el-form-item label="库存" prop="stock">
        <el-input-number v-model="form.stock" :min="0" :step="1" :precision="0"/>
      </el-form-item>
      <el-form-item label="状态" prop="status">
        <el-radio-group v-model="form.status">
          <el-radio :value="1">在售</el-radio>
          <el-radio :value="0">下架</el-radio>
        </el-radio-group>
      </el-form-item>
      <el-form-item label="主图" prop="mainImage">
        <el-upload
            class="main-image-uploader"
            action="/api/service/upload"
            name="file"
            :data="{folder: 'product'}"
            :headers="uploadHeaders"
            :show-file-list="false"
            :on-success="handleMainImageSuccess"
            accept="image/*"
        >
          <el-image v-if="form.mainImage" :src="form.mainImage" fit="cover" class="main-image"/>
          <el-icon v-else class="main-image-uploader-icon">
            <Plus/>
          </el-icon>
        </el-upload>
      </el-form-item>
    </el-form>
    <template #footer>
      <el-button @click="dialogVisible = false">取消</el-button>
      <el-button type="primary" :loading="submitLoading" @click="handleSubmit">确定</el-button>
    </template>
  </el-dialog>
</template>

<script setup>
import {computed, onMounted, reactive, ref} from 'vue'
import {ElMessage, ElMessageBox} from 'element-plus'
import dayjs from 'dayjs'
import productApi from '@/api/product/product.js'
import categoryApi from '@/api/category/category.js'
import {useTokenStore} from '@/store/token.js'

const tokenStore = useTokenStore()

// ---------------- 分类树（级联选择器） ----------------
const categoryTree = ref([])
const categoryProps = {
  value: 'id',
  label: 'name',
  children: 'children',
  //只选中最后一级（二级分类）的id
  emitPath: false
}

const loadCategoryTree = async () => {
  try {
    const res = await categoryApi.tree()
    if (res.code === 1) {
      //删除空的children，让二级分类成为级联选择器的叶子节点
      const removeEmptyChildren = (categoryList) => {
        categoryList.forEach(category => {
          if (category.children && category.children.length > 0) {
            removeEmptyChildren(category.children)
          } else {
            delete category.children
          }
        })
      }
      categoryTree.value = res.data || []
      removeEmptyChildren(categoryTree.value)
    }
  } catch (e) {
    /* 拦截器已统一提示 */
  }
}

// ---------------- 列表查询 ----------------
const loading = ref(false)
const tableData = ref([])
const selection = ref([])
const searchForm = reactive({
  name: '',
  categoryId: null,
  dateRange: []
})
const page = reactive({
  current: 1,
  limit: 10,
  total: 0
})

const formatTime = (time) => time ? dayjs(time).format('YYYY-MM-DD HH:mm:ss') : ''

const loadData = async () => {
  loading.value = true
  try {
    const params = {
      page: page.current,
      limit: page.limit
    }
    if (searchForm.name) params.name = searchForm.name
    if (searchForm.categoryId) params.categoryId = searchForm.categoryId
    if (searchForm.dateRange && searchForm.dateRange.length === 2) {
      params.beginCreateTime = searchForm.dateRange[0]
      params.endCreateTime = searchForm.dateRange[1]
    }
    const res = await productApi.list(params)
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

const handleSearch = () => {
  page.current = 1
  loadData()
}

const handleReset = () => {
  searchForm.name = ''
  searchForm.categoryId = null
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

// ---------------- 状态开关 ----------------
const beforeStatusChange = (row) => {
  return new Promise((resolve, reject) => {
    const text = row.status === 1 ? '下架' : '上架'
    ElMessageBox.confirm(`确定要${text}商品「${row.name}」吗？`, '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    }).then(() => resolve(true)).catch(() => reject())
  })
}

const handleStatusChange = async (row) => {
  try {
    const res = await productApi.updateStatus(row.id, row.status)
    if (res.code === 1) {
      ElMessage.success('状态修改成功')
    } else {
      ElMessage.error(res.msg || '状态修改失败')
      loadData()
    }
  } catch (e) {
    loadData()
  }
}

// ---------------- 新增 / 编辑 ----------------
const dialogVisible = ref(false)
const submitLoading = ref(false)
const formRef = ref()
const form = reactive({
  id: null,
  categoryId: null,
  type: 1,
  name: '',
  subtitle: '',
  price: 0,
  stock: 0,
  status: 1,
  mainImage: ''
})
const rules = {
  name: [{required: true, message: '请输入商品名称', trigger: 'blur'}],
  categoryId: [{required: true, message: '请选择分类', trigger: 'change'}],
  type: [{required: true, message: '请选择商品类型', trigger: 'change'}],
  price: [{required: true, message: '请输入价格', trigger: 'blur'}],
  stock: [{required: true, message: '请输入库存', trigger: 'blur'}]
}

const uploadHeaders = computed(() => ({Authorization: tokenStore.token}))

const handleMainImageSuccess = (res) => {
  if (res.code === 1) {
    form.mainImage = res.data
  } else {
    ElMessage.error(res.msg || '图片上传失败')
  }
}

const handleAdd = () => {
  Object.assign(form, {
    id: null,
    categoryId: null,
    type: 1,
    name: '',
    subtitle: '',
    price: 0,
    stock: 0,
    status: 1,
    mainImage: ''
  })
  dialogVisible.value = true
}

const handleEdit = async (row) => {
  try {
    const res = await productApi.selectById(row.id)
    if (res.code === 1) {
      const product = res.data
      Object.assign(form, {
        id: product.id,
        categoryId: product.categoryId,
        type: product.type,
        name: product.name,
        subtitle: product.subtitle || '',
        price: product.price,
        stock: product.stock,
        status: product.status,
        mainImage: product.mainImage || ''
      })
      dialogVisible.value = true
    } else {
      ElMessage.error(res.msg || '查询商品失败')
    }
  } catch (e) {
    /* 拦截器已统一提示 */
  }
}

const handleSubmit = () => {
  formRef.value.validate(async (valid) => {
    if (!valid) return
    submitLoading.value = true
    try {
      let res
      if (form.id) {
        res = await productApi.update(form.id, form)
      } else {
        res = await productApi.add(form)
      }
      if (res.code === 1) {
        ElMessage.success(form.id ? '修改成功' : '新增成功')
        dialogVisible.value = false
        loadData()
      } else {
        ElMessage.error(res.msg || '保存失败')
      }
    } catch (e) {
      /* 拦截器已统一提示 */
    } finally {
      submitLoading.value = false
    }
  })
}

// ---------------- 删除 ----------------
const handleDelete = (row) => {
  ElMessageBox.confirm(`确定要删除商品「${row.name}」吗？`, '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    try {
      const res = await productApi.deleteById(row.id)
      if (res.code === 1) {
        ElMessage.success('删除成功')
        loadData()
      } else {
        ElMessage.error(res.msg || '删除失败')
      }
    } catch (e) {
      /* 拦截器已统一提示 */
    }
  }).catch(() => {
  })
}

const handleSelectionChange = (rows) => {
  selection.value = rows
}

const handleDeleteBatch = () => {
  ElMessageBox.confirm(`确定要删除选中的 ${selection.value.length} 个商品吗？`, '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    try {
      const ids = selection.value.map((row) => row.id)
      const res = await productApi.deleteAll(ids)
      if (res.code === 1) {
        ElMessage.success('批量删除成功')
        loadData()
      } else {
        ElMessage.error(res.msg || '批量删除失败')
      }
    } catch (e) {
      /* 拦截器已统一提示 */
    }
  }).catch(() => {
  })
}

onMounted(() => {
  loadData()
  loadCategoryTree()
})
</script>

<style scoped>
.search-form .el-form-item {
  margin-bottom: 12px;
}

.toolbar {
  margin-bottom: 16px;
}

.pagination {
  margin-top: 16px;
  justify-content: flex-end;
}

.main-image-uploader {
  display: flex;
  justify-content: center;
  width: 120px;
  height: 120px;
  border: 1px dashed var(--el-border-color);
  border-radius: 6px;
  cursor: pointer;
  overflow: hidden;
  transition: border-color 0.2s;
}

.main-image-uploader:hover {
  border-color: var(--el-color-primary);
}

.main-image-uploader-icon {
  width: 120px;
  height: 120px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 24px;
  color: #8c939d;
}

.main-image {
  width: 120px;
  height: 120px;
}
</style>

<template>
  <el-card shadow="never">
    <!-- 操作栏 -->
    <div class="toolbar">
      <el-button type="primary" :icon="'Plus'" @click="handleAdd(0)">新增顶级分类</el-button>
      <el-button :icon="'Refresh'" @click="loadData">刷新</el-button>
    </div>

    <!-- 树形表格 -->
    <el-table :data="tableData" border stripe v-loading="loading" row-key="id" default-expand-all>
      <el-table-column prop="name" label="分类名称" min-width="240"/>
      <el-table-column prop="sort" label="排序" width="90" align="center"/>
      <el-table-column label="状态" width="100" align="center">
        <template #default="{ row }">
          <el-tag :type="row.status === 1 ? 'success' : 'info'">{{ row.status === 1 ? '启用' : '停用' }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="创建时间" width="170" align="center">
        <template #default="{ row }">{{ formatTime(row.createTime) }}</template>
      </el-table-column>
      <el-table-column label="操作" width="220" align="center" fixed="right">
        <template #default="{ row }">
          <el-button type="success" link :icon="'Plus'" @click="handleAdd(row.id)">添加子分类</el-button>
          <el-button type="primary" link :icon="'Edit'" @click="handleEdit(row)">编辑</el-button>
          <el-button type="danger" link :icon="'Delete'" @click="handleDelete(row)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>
  </el-card>

  <!-- 新增 / 编辑弹窗 -->
  <el-dialog v-model="dialogVisible" :title="form.id ? '编辑分类' : '新增分类'" width="480px" destroy-on-close>
    <el-form ref="formRef" :model="form" :rules="rules" label-width="80px">
      <el-form-item label="上级分类">
        <el-input :model-value="form.parentId === 0 ? '顶级分类' : parentName" disabled/>
      </el-form-item>
      <el-form-item label="分类名称" prop="name">
        <el-input v-model="form.name" placeholder="请输入分类名称"/>
      </el-form-item>
      <el-form-item label="排序" prop="sort">
        <el-input-number v-model="form.sort" :min="0" :max="9999"/>
      </el-form-item>
      <el-form-item label="状态" prop="status">
        <el-radio-group v-model="form.status">
          <el-radio :value="1">启用</el-radio>
          <el-radio :value="0">停用</el-radio>
        </el-radio-group>
      </el-form-item>
    </el-form>
    <template #footer>
      <el-button @click="dialogVisible = false">取消</el-button>
      <el-button type="primary" :loading="submitLoading" @click="handleSubmit">确定</el-button>
    </template>
  </el-dialog>
</template>

<script setup>
import {onMounted, reactive, ref} from 'vue'
import {ElMessage, ElMessageBox} from 'element-plus'
import dayjs from 'dayjs'
import request from '@/utils/request'

// ---------------- 树形列表 ----------------
const loading = ref(false)
const tableData = ref([])

const formatTime = (time) => time ? dayjs(time).format('YYYY-MM-DD HH:mm:ss') : ''

const loadData = async () => {
  loading.value = true
  try {
    const res = await request.get('/category/tree')
    if (res.code === 1) {
      tableData.value = res.data || []
    } else {
      ElMessage.error(res.msg || '查询失败')
    }
  } catch (e) {
    /* 拦截器已统一提示 */
  } finally {
    loading.value = false
  }
}

// ---------------- 新增 / 编辑 ----------------
const dialogVisible = ref(false)
const submitLoading = ref(false)
const formRef = ref()
const form = reactive({
  id: null,
  parentId: 0,
  name: '',
  sort: 0,
  status: 1
})
const rules = {
  name: [{required: true, message: '请输入分类名称', trigger: 'blur'}]
}

// 上级分类名称（用于弹窗展示）
const parentName = ref('')

const findNameById = (list, id) => {
  for (const item of list) {
    if (item.id === id) return item.name
    if (item.children) {
      const name = findNameById(item.children, id)
      if (name) return name
    }
  }
  return ''
}

const handleAdd = (parentId) => {
  parentName.value = parentId === 0 ? '' : findNameById(tableData.value, parentId)
  Object.assign(form, {
    id: null,
    parentId: parentId,
    name: '',
    sort: 0,
    status: 1
  })
  dialogVisible.value = true
}

const handleEdit = (row) => {
  parentName.value = row.parentId === 0 ? '' : findNameById(tableData.value, row.parentId)
  Object.assign(form, {
    id: row.id,
    parentId: row.parentId,
    name: row.name,
    sort: row.sort,
    status: row.status
  })
  dialogVisible.value = true
}

const handleSubmit = () => {
  formRef.value.validate(async (valid) => {
    if (!valid) return
    submitLoading.value = true
    try {
      let res
      if (form.id) {
        res = await request.put(`/category/${form.id}`, form)
      } else {
        res = await request.post('/category', form)
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
  ElMessageBox.confirm(`确定要删除分类「${row.name}」吗？`, '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    try {
      const res = await request.delete(`/category/${row.id}`)
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

onMounted(() => {
  loadData()
})
</script>

<style scoped>
.toolbar {
  margin-bottom: 16px;
}
</style>

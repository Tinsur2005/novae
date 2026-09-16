<template>
  <el-card shadow="never">
    <!-- 搜索栏 -->
    <el-form :inline="true" :model="searchForm" class="search-form">
      <el-form-item label="用户名">
        <el-input v-model="searchForm.name" placeholder="请输入用户名" clearable @keyup.enter="handleSearch"/>
      </el-form-item>
      <el-form-item label="邮箱">
        <el-input v-model="searchForm.email" placeholder="请输入邮箱" clearable @keyup.enter="handleSearch"/>
      </el-form-item>
      <el-form-item label="手机号">
        <el-input v-model="searchForm.phone" placeholder="请输入手机号" clearable @keyup.enter="handleSearch"/>
      </el-form-item>
      <el-form-item label="注册时间">
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
      <el-button type="danger" :icon="'Delete'" :disabled="selection.length === 0" @click="handleDeleteBatch">
        批量删除
      </el-button>
    </div>

    <!-- 数据表格 -->
    <el-table :data="tableData" border stripe v-loading="loading" @selection-change="handleSelectionChange">
      <el-table-column type="selection" width="50" align="center"/>
      <el-table-column prop="id" label="ID" width="70" align="center"/>
      <el-table-column label="头像" width="80" align="center">
        <template #default="{ row }">
          <el-avatar :size="36" :src="row.avatar || ''">{{ (row.name || 'U').slice(0, 1).toUpperCase() }}</el-avatar>
        </template>
      </el-table-column>
      <el-table-column prop="name" label="用户名" min-width="120"/>
      <el-table-column prop="email" label="邮箱" min-width="160" show-overflow-tooltip/>
      <el-table-column prop="phone" label="手机号" width="130"/>
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
      <el-table-column label="注册时间" width="170" align="center">
        <template #default="{ row }">{{ formatTime(row.createTime) }}</template>
      </el-table-column>
      <el-table-column label="操作" width="80" align="center" fixed="right">
        <template #default="{ row }">
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
</template>

<script setup>
import {onMounted, reactive, ref} from 'vue'
import {ElMessage, ElMessageBox} from 'element-plus'
import dayjs from 'dayjs'
import userApi from '@/api/user/user.js'

// ---------------- 列表查询 ----------------
const loading = ref(false)
const tableData = ref([])
const selection = ref([])
const searchForm = reactive({
  name: '',
  email: '',
  phone: '',
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
    if (searchForm.email) params.email = searchForm.email
    if (searchForm.phone) params.phone = searchForm.phone
    if (searchForm.dateRange && searchForm.dateRange.length === 2) {
      params.beginCreateTime = searchForm.dateRange[0]
      params.endCreateTime = searchForm.dateRange[1]
    }
    const res = await userApi.list(params)
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
  searchForm.email = ''
  searchForm.phone = ''
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
    const text = row.status === 1 ? '禁用' : '启用'
    ElMessageBox.confirm(`确定要${text}用户「${row.name}」吗？`, '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    }).then(() => resolve(true)).catch(() => reject())
  })
}

const handleStatusChange = async (row) => {
  try {
    const res = await userApi.updateStatus(row.id, row.status)
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

// ---------------- 删除 ----------------
const handleDelete = (row) => {
  ElMessageBox.confirm(`确定要删除用户「${row.name}」吗？`, '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    try {
      const res = await userApi.deleteById(row.id)
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
  ElMessageBox.confirm(`确定要删除选中的 ${selection.value.length} 个用户吗？`, '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    try {
      const ids = selection.value.map((row) => row.id)
      const res = await userApi.deleteAll(ids)
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
</style>
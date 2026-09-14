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
      <el-table-column label="头像" width="80" align="center">
        <template #default="{ row }">
          <el-avatar :size="36" :src="row.avatar || ''">{{ (row.name || 'A').slice(0, 1).toUpperCase() }}</el-avatar>
        </template>
      </el-table-column>
      <el-table-column prop="name" label="用户名" min-width="120"/>
      <el-table-column prop="email" label="邮箱" min-width="160" show-overflow-tooltip/>
      <el-table-column prop="phone" label="手机号" width="130"/>
      <el-table-column label="角色" width="100" align="center">
        <template #default="{ row }">
          <el-tag :type="row.role === 0 ? 'primary' : 'info'">{{ row.role === 0 ? '管理员' : '普通用户' }}</el-tag>
        </template>
      </el-table-column>
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
  <el-dialog v-model="dialogVisible" :title="form.id ? '编辑管理员' : '新增管理员'" width="520px" destroy-on-close>
    <el-form ref="formRef" :model="form" :rules="rules" label-width="80px">
      <el-form-item label="头像">
        <el-upload
            class="avatar-uploader"
            action="/api/service/upload"
            name="file"
            :data="{folder: 'avatar'}"
            :headers="uploadHeaders"
            :show-file-list="false"
            :on-success="handleAvatarSuccess"
            accept="image/*"
        >
          <el-avatar v-if="form.avatar" :size="72" :src="form.avatar"/>
          <el-icon v-else class="avatar-uploader-icon">
            <Plus/>
          </el-icon>
        </el-upload>
      </el-form-item>
      <el-form-item label="用户名" prop="name">
        <el-input v-model="form.name" placeholder="请输入用户名"/>
      </el-form-item>
      <el-form-item label="密码" prop="password">
        <el-input
            v-model="form.password"
            type="password"
            show-password
            :placeholder="form.id ? '不修改密码请留空' : '请输入密码'"
        />
      </el-form-item>
      <el-form-item label="邮箱" prop="email">
        <el-input v-model="form.email" placeholder="请输入邮箱"/>
      </el-form-item>
      <el-form-item label="手机号" prop="phone">
        <el-input v-model="form.phone" placeholder="请输入手机号"/>
      </el-form-item>
      <el-form-item label="角色" prop="role">
        <el-select v-model="form.role" placeholder="请选择角色" style="width: 100%">
          <el-option label="管理员" :value="0"/>
          <el-option label="普通用户" :value="1"/>
        </el-select>
      </el-form-item>
      <el-form-item label="状态" prop="status">
        <el-radio-group v-model="form.status">
          <el-radio :value="1">正常</el-radio>
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
import {computed, onMounted, reactive, ref} from 'vue'
import {ElMessage, ElMessageBox} from 'element-plus'
import dayjs from 'dayjs'
import request from '@/utils/request'
import {useTokenStore} from '@/store/token.js'

const tokenStore = useTokenStore()

// ---------------- 列表查询 ----------------
const loading = ref(false)
const tableData = ref([])
const selection = ref([])
const searchForm = reactive({
  name: '',
  email: '',
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
    if (searchForm.dateRange && searchForm.dateRange.length === 2) {
      params.beginCreateTime = searchForm.dateRange[0]
      params.endCreateTime = searchForm.dateRange[1]
    }
    const res = await request.get('/admin', {params})
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
    const text = row.status === 1 ? '停用' : '启用'
    ElMessageBox.confirm(`确定要${text}用户「${row.name}」吗？`, '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    }).then(() => resolve(true)).catch(() => reject())
  })
}

const handleStatusChange = async (row) => {
  try {
    const res = await request.put(`/admin/${row.id}`, {status: row.status})
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
  name: '',
  password: '',
  avatar: '',
  email: '',
  phone: '',
  role: 0,
  status: 1
})
const rules = {
  name: [{required: true, message: '请输入用户名', trigger: 'blur'}],
  password: [
    {
      validator: (rule, value, callback) => {
        // 编辑时留空表示不修改密码
        if (!form.id && !value) {
          callback(new Error('请输入密码'))
        } else if (value && (value.length < 6 || value.length > 20)) {
          callback(new Error('密码长度为 6 - 20 位'))
        } else {
          callback()
        }
      },
      trigger: 'blur'
    }
  ],
  email: [{type: 'email', message: '邮箱格式不正确', trigger: 'blur'}],
  phone: [{pattern: /^1[3-9]\d{9}$/, message: '手机号格式不正确', trigger: 'blur'}],
  role: [{required: true, message: '请选择角色', trigger: 'change'}]
}

const uploadHeaders = computed(() => ({Authorization: tokenStore.token}))

const handleAvatarSuccess = (res) => {
  if (res.code === 1) {
    form.avatar = res.data
  } else {
    ElMessage.error(res.msg || '头像上传失败')
  }
}

const handleAdd = () => {
  Object.assign(form, {
    id: null,
    name: '',
    password: '',
    avatar: '',
    email: '',
    phone: '',
    role: 0,
    status: 1
  })
  dialogVisible.value = true
}

const handleEdit = (row) => {
  Object.assign(form, {
    id: row.id,
    name: row.name,
    password: '',
    avatar: row.avatar || '',
    email: row.email || '',
    phone: row.phone || '',
    role: row.role,
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
        // 编辑时密码留空则不提交密码字段
        const data = {...form}
        if (!data.password) delete data.password
        res = await request.put(`/admin/${form.id}`, data)
      } else {
        res = await request.post('/admin', form)
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
  ElMessageBox.confirm(`确定要删除用户「${row.name}」吗？`, '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    try {
      const res = await request.delete(`/admin/${row.id}`)
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
      const res = await request.delete('/admin', {data: ids})
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

.avatar-uploader {
  display: flex;
  justify-content: center;
  width: 72px;
  height: 72px;
  border: 1px dashed var(--el-border-color);
  border-radius: 50%;
  cursor: pointer;
  overflow: hidden;
  transition: border-color 0.2s;
}

.avatar-uploader:hover {
  border-color: var(--el-color-primary);
}

.avatar-uploader-icon {
  width: 72px;
  height: 72px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 24px;
  color: #8c939d;
}
</style>

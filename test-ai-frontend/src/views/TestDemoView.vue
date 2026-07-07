<template>
  <div class="test-demo-container">
    <el-card class="box-card">
      <template #header>
        <div class="card-header">
          <span>测试功能验证 - 数据库查询展示</span>
        </div>
      </template>

      <!-- 查询条件 -->
      <el-form :model="queryForm" inline>
        <el-form-item label="名称">
          <el-input
            v-model="queryForm.name"
            placeholder="请输入名称"
            clearable
            @keyup.enter="handleQuery"
          />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleQuery">查询</el-button>
          <el-button @click="handleReset">重置</el-button>
        </el-form-item>
      </el-form>

      <!-- 数据表格 -->
      <el-table
        v-loading="loading"
        :data="tableData"
        border
        style="width: 100%"
      >
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="name" label="名称" />
        <el-table-column prop="value" label="值" />
        <el-table-column prop="description" label="描述" />
        <el-table-column prop="createTime" label="创建时间" width="180" />
      </el-table>

      <!-- 空数据提示 -->
      <el-empty v-if="!loading && tableData.length === 0" description="暂无数据" />
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, reactive } from 'vue'
import { ElMessage } from 'element-plus'
import { listTestDemo } from '../api/testDemo'
import type { TestDemo } from '../types/api'

// 查询表单
const queryForm = reactive({
  name: ''
})

// 表格数据
const tableData = ref<TestDemo[]>([])

// 加载状态
const loading = ref(false)

/**
 * 查询数据
 */
const handleQuery = async (): Promise<void> => {
  loading.value = true
  try {
    const name = queryForm.name.trim() || undefined
    const data = await listTestDemo(name)
    tableData.value = data || []
  } catch (error) {
    console.error('查询失败:', error)
    ElMessage.error('查询失败')
    tableData.value = []
  } finally {
    loading.value = false
  }
}

/**
 * 重置查询
 */
const handleReset = (): void => {
  queryForm.name = ''
  handleQuery()
}

// 页面加载时查询
onMounted(() => {
  handleQuery()
})
</script>

<style scoped>
.test-demo-container {
  padding: 20px;
  max-width: 1200px;
  margin: 0 auto;
}

.card-header {
  font-size: 18px;
  font-weight: bold;
}
</style>

<template>
  <div class="task-management-page">
    <el-container>
      <el-header class="page-header">
        <div class="header-content">
          <h1 class="title">AI测试用例生成平台</h1>
          <el-menu
            :default-active="activeMenu"
            mode="horizontal"
            :ellipsis="false"
            @select="handleMenuSelect"
            class="nav-menu"
          >
            <el-menu-item index="generate">用例生成</el-menu-item>
            <el-menu-item index="tasks">任务管理</el-menu-item>
          </el-menu>
        </div>
      </el-header>

      <el-main class="main-content">
        <div class="page-title-section">
          <div>
            <h2 class="page-title">任务管理</h2>
            <p class="page-subtitle">查看和管理所有AI测试用例生成任务</p>
          </div>
          <div class="page-actions">
            <el-button @click="handleExport">导出报告</el-button>
            <el-button type="primary" @click="goToGenerate">新建任务</el-button>
          </div>
        </div>

        <el-row :gutter="20" class="stats-row">
          <el-col :span="6">
            <el-card class="stat-card total">
              <div class="stat-content">
                <div class="stat-icon">📊</div>
                <div class="stat-info">
                  <div class="stat-number">{{ taskStats.total }}</div>
                  <div class="stat-label">任务总数</div>
                </div>
              </div>
            </el-card>
          </el-col>
          <el-col :span="6">
            <el-card class="stat-card completed">
              <div class="stat-content">
                <div class="stat-icon">✅</div>
                <div class="stat-info">
                  <div class="stat-number">{{ taskStats.completed }}</div>
                  <div class="stat-label">已完成</div>
                </div>
              </div>
            </el-card>
          </el-col>
          <el-col :span="6">
            <el-card class="stat-card processing">
              <div class="stat-content">
                <div class="stat-icon">🔄</div>
                <div class="stat-info">
                  <div class="stat-number">{{ taskStats.processing }}</div>
                  <div class="stat-label">进行中</div>
                </div>
              </div>
            </el-card>
          </el-col>
          <el-col :span="6">
            <el-card class="stat-card failed">
              <div class="stat-content">
                <div class="stat-icon">❌</div>
                <div class="stat-info">
                  <div class="stat-number">{{ taskStats.failed }}</div>
                  <div class="stat-label">失败</div>
                </div>
              </div>
            </el-card>
          </el-col>
        </el-row>

        <el-card class="filter-card">
          <el-form :inline="true" :model="filters" class="filter-form">
            <el-form-item label="任务状态">
              <el-select v-model="filters.taskStatus" placeholder="全部状态" clearable style="width: 150px">
                <el-option :label="item.label" :value="item.value" v-for="item in statusOptions" :key="item.value" />
              </el-select>
            </el-form-item>
            <el-form-item label="文档类型">
              <el-select v-model="filters.documentType" placeholder="全部类型" clearable style="width: 150px">
                <el-option label="需求文档" value="REQUIREMENT" />
                <el-option label="设计文档" value="DESIGN" />
              </el-select>
            </el-form-item>
            <el-form-item label="任务号" label-width="60px">
              <el-input v-model="filters.search" placeholder="输入任务名称搜索" clearable style="width: 250px" />
            </el-form-item>
            <el-form-item>
              <el-button type="primary" @click="loadTasks">查询</el-button>
              <el-button @click="resetFilters">重置</el-button>
            </el-form-item>
          </el-form>
        </el-card>

        <el-card class="table-card">
          <el-table :data="taskList" style="width: 100%" v-loading="loading">
            <el-table-column prop="taskName" label="任务名称" min-width="200" />
            <el-table-column prop="documentType" label="文档类型" width="120">
              <template #default="{ row }">
                <el-tag type="info">{{ row.documentType === 'REQUIREMENT' ? '需求文档' : '设计文档' }}</el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="taskStatus" label="状态" width="100">
              <template #default="{ row }">
                <el-tag :type="getStatusType(row.taskStatus)" size="small">
                  {{ getStatusText(row.taskStatus) }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="resultCount" label="用例数量" width="100">
              <template #default="{ row }">
                {{ row.resultCount || '-' }}
              </template>
            </el-table-column>
            <el-table-column prop="qualityScore" label="质量评分" width="100">
              <template #default="{ row }">
                {{ row.qualityScore ? row.qualityScore.toFixed(1) : '-' }}
              </template>
            </el-table-column>
            <el-table-column prop="duration" label="耗时" width="100">
              <template #default="{ row }">
                {{ row.duration ? `${row.duration}s` : '-' }}
              </template>
            </el-table-column>
            <el-table-column prop="createTime" label="创建时间" width="180">
              <template #default="{ row }">
                {{ formatTime(row.createTime) }}
              </template>
            </el-table-column>
            <el-table-column label="操作" width="200" fixed="right">
              <template #default="{ row }">
                <el-button type="primary" link @click="goToDetail(row.id)">详情</el-button>
                <el-button type="primary" link @click="handleDownload(row)" v-if="row.taskStatus === 2">
                  下载结果
                </el-button>
                <el-button type="danger" link @click="handleDelete(row.id)">删除</el-button>
              </template>
            </el-table-column>
          </el-table>

          <div class="pagination-section">
            <el-pagination
              v-model:current-page="pagination.pageNum"
              v-model:page-size="pagination.pageSize"
              :page-sizes="[10, 20, 50, 100]"
              :total="pagination.total"
              layout="total, sizes, prev, pager, next, jumper"
              @size-change="handleSizeChange"
              @current-change="handlePageChange"
            />
          </div>
        </el-card>
      </el-main>
    </el-container>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getTaskList, deleteTask, exportAiResponse, type Task } from '@/api/task'

const router = useRouter()

const activeMenu = ref('tasks')
const loading = ref(false)
const taskList = ref<Task[]>([])

const filters = ref({
  taskStatus: undefined as number | undefined,
  documentType: undefined as string | undefined,
  search: ''
})

const pagination = ref({
  pageNum: 1,
  pageSize: 10,
  total: 0
})

const statusOptions = [
  { label: '待处理', value: 0 },
  { label: '进行中', value: 1 },
  { label: '已完成', value: 2 },
  { label: '失败', value: 3 }
]

const taskStats = computed(() => {
  const stats = {
    total: taskList.value.length,
    completed: 0,
    processing: 0,
    failed: 0
  }
  taskList.value.forEach(task => {
    if (task.taskStatus === 2) stats.completed++
    else if (task.taskStatus === 1) stats.processing++
    else if (task.taskStatus === 3) stats.failed++
  })
  return stats
})

const handleMenuSelect = (index: string) => {
  if (index === 'generate') {
    router.push('/generate')
  } else if (index === 'tasks') {
    router.push('/tasks')
  }
}

const loadTasks = async () => {
  loading.value = true
  try {
    const response = await getTaskList({
      taskStatus: filters.value.taskStatus,
      documentType: filters.value.documentType,
      pageNum: pagination.value.pageNum,
      pageSize: pagination.value.pageSize
    })
    taskList.value = response.list
    pagination.value.total = response.total
  } catch (error) {
    ElMessage.error('加载任务列表失败')
  } finally {
    loading.value = false
  }
}

const resetFilters = () => {
  filters.value = {
    taskStatus: undefined,
    documentType: undefined,
    search: ''
  }
  pagination.value.pageNum = 1
  loadTasks()
}

const handleSizeChange = (size: number) => {
  pagination.value.pageSize = size
  loadTasks()
}

const handlePageChange = (page: number) => {
  pagination.value.pageNum = page
  loadTasks()
}

const goToGenerate = () => {
  router.push('/generate')
}

const goToDetail = (id: number) => {
  router.push(`/tasks/${id}`)
}

const handleDownload = async (task: Task) => {
  try {
    const response = await exportAiResponse(task.id)
    const blob = new Blob([response.data], { type: 'text/markdown;charset=UTF-8' })
    const url = URL.createObjectURL(blob)
    const link = document.createElement('a')
    link.href = url
    
    // 从响应头中获取文件名
    const contentDisposition = response.headers['content-disposition']
    let fileName = `testcase_result_${task.id}.md`
    if (contentDisposition) {
      const match = contentDisposition.match(/filename[^;=\n]*=((['"]).*?\2|[^;\n]*)/)
      if (match && match[1]) {
        fileName = match[1].replace(/['"]/g, '')
      }
    }
    link.download = decodeURIComponent(fileName)
    
    document.body.appendChild(link)
    link.click()
    document.body.removeChild(link)
    URL.revokeObjectURL(url)
    ElMessage.success('下载成功')
  } catch (error: any) {
    console.error('下载失败:', error)
    ElMessage.error('下载失败: ' + (error.message || '未知错误'))
  }
}

const handleDelete = async (id: number) => {
  try {
    await ElMessageBox.confirm('确定要删除这个任务吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    await deleteTask(id)
    ElMessage.success('删除成功')
    loadTasks()
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('删除失败')
    }
  }
}

const handleExport = () => {
  ElMessage.info('导出功能开发中...')
}

const formatTime = (time: string) => {
  const date = new Date(time)
  return date.toLocaleString('zh-CN')
}

const getStatusType = (status: number) => {
  const types: Record<number, any> = {
    0: 'info',
    1: 'primary',
    2: 'success',
    3: 'danger'
  }
  return types[status] || 'info'
}

const getStatusText = (status: number) => {
  const texts: Record<number, string> = {
    0: '待处理',
    1: '进行中',
    2: '已完成',
    3: '失败'
  }
  return texts[status] || '未知'
}

onMounted(() => {
  loadTasks()
})
</script>

<style scoped lang="scss">
.task-management-page {
  min-height: 100vh;
  background-color: #f5f7fa;
}

.page-header {
  background-color: #fff;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
  padding: 0;

  .header-content {
    max-width: 1400px;
    margin: 0 auto;
    padding: 0 20px;
    display: flex;
    align-items: center;
    gap: 40px;
    height: 64px;
  }

  .title {
    font-size: 20px;
    font-weight: 600;
    color: #303133;
    margin: 0;
  }

  .nav-menu {
    flex: 1;
    border: none;
  }
}

.main-content {
  max-width: 1400px;
  margin: 24px auto;
  padding: 0 20px;
}

.page-title-section {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 20px;

  .page-title {
    font-size: 24px;
    font-weight: 600;
    color: #303133;
    margin: 0 0 8px 0;
  }

  .page-subtitle {
    font-size: 14px;
    color: #909399;
    margin: 0;
  }

  .page-actions {
    display: flex;
    gap: 12px;
  }
}

.stats-row {
  margin-bottom: 20px;
}

.stat-card {
  .stat-content {
    display: flex;
    align-items: center;
    gap: 16px;

    .stat-icon {
      font-size: 32px;
    }

    .stat-info {
      flex: 1;

      .stat-number {
        font-size: 24px;
        font-weight: 700;
        margin-bottom: 4px;
      }

      .stat-label {
        font-size: 13px;
        color: #909399;
      }
    }
  }

  &.total .stat-number {
    color: #409eff;
  }
  &.completed .stat-number {
    color: #67c23a;
  }
  &.processing .stat-number {
    color: #409eff;
  }
  &.failed .stat-number {
    color: #f56c6c;
  }
}

.filter-card {
  margin-bottom: 20px;

  .filter-form {
    margin: 0;
  }
}

.table-card {
  .pagination-section {
    display: flex;
    justify-content: flex-end;
    margin-top: 20px;
  }
}
</style>

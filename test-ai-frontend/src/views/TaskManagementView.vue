<template>
  <div class="task-management">
    <div class="page-header">
      <h1 class="page-title">任务管理</h1>
      <p class="page-description">查看和管理所有 AI 测试用例生成任务</p>
    </div>

    <!-- 统计卡片 -->
    <el-row :gutter="20" class="statistics-cards">
      <el-col :span="6">
        <el-card class="stat-card">
          <div class="stat-header">
            <span class="stat-label">总任务数</span>
            <el-icon class="stat-icon"><Document /></el-icon>
          </div>
          <div class="stat-value">{{ statistics.totalCount }}</div>
          <div class="stat-trend positive">
            <el-icon><Top /></el-icon>
            <span>12.5% 较上月</span>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card class="stat-card">
          <div class="stat-header">
            <span class="stat-label">已完成任务</span>
            <el-icon class="stat-icon success"><CircleCheck /></el-icon>
          </div>
          <div class="stat-value">{{ statistics.successCount }}</div>
          <div class="stat-trend positive">
            <el-icon><Top /></el-icon>
            <span>18.2% 较上月</span>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card class="stat-card">
          <div class="stat-header">
            <span class="stat-label">进行中任务</span>
            <el-icon class="stat-icon primary"><Loading /></el-icon>
          </div>
          <div class="stat-value">{{ statistics.processingCount }}</div>
          <div class="stat-trend">
            <span>2 较昨日</span>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card class="stat-card">
          <div class="stat-header">
            <span class="stat-label">失败任务</span>
            <el-icon class="stat-icon danger"><CircleClose /></el-icon>
          </div>
          <div class="stat-value">{{ statistics.failedCount }}</div>
          <div class="stat-trend negative">
            <el-icon><Bottom /></el-icon>
            <span>3.1% 较上月</span>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 评价弹窗 -->
    <TaskFeedbackDialog
      v-model="feedbackDialogVisible"
      :task="currentTask"
      @submit-success="handleFeedbackSuccess"
    />

    <!-- 任务详情弹窗 -->
    <TaskDetailDialog
      v-model="detailDialogVisible"
      :task="currentDetailTask"
      @success="handleFeedbackSuccess"
    />

    <!-- 筛选区域 -->
    <el-card class="filter-card">
      <el-form :inline="true" :model="filterForm" class="filter-form">
        <el-form-item label="任务状态">
          <el-select v-model="filterForm.taskStatus" placeholder="全部状态" clearable style="width: 150px">
            <el-option label="进行中" :value="1" />
            <el-option label="已完成" :value="2" />
            <el-option label="任务失败" :value="3" />
            <el-option label="已评价" :value="4" />
          </el-select>
        </el-form-item>
        <el-form-item label="文档类型">
          <el-select v-model="filterForm.documentType" placeholder="全部类型" clearable style="width: 150px">
            <el-option label="需求文档" value="REQUIREMENT" />
            <el-option label="设计文档" value="DESIGN" />
            <el-option label="测试文档" value="TEST" />
          </el-select>
        </el-form-item>
        <el-form-item label="创建时间">
          <el-select v-model="filterForm.timeRange" placeholder="时间范围" clearable style="width: 150px">
            <el-option label="近 7 天" value="7" />
            <el-option label="近 30 天" value="30" />
            <el-option label="近 90 天" value="90" />
          </el-select>
        </el-form-item>
        <el-form-item label="任务号" class="search-item">
          <el-input
            v-model="filterForm.taskName"
            placeholder="输入任务号搜索"
            prefix-icon="Search"
            clearable
            style="width: 250px"
            @keyup.enter="handleSearch"
          />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">搜索</el-button>
          <el-button @click="handleReset">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 任务列表 -->
    <el-card class="table-card">
      <el-table
        :data="taskList"
        v-loading="loading"
        style="width: 100%"
        border
        stripe
      >
        <el-table-column type="selection" width="55" />
        <el-table-column label="任务名称" min-width="250">
          <template #default="{ row }">
            <div class="task-name">
              <el-icon class="task-icon">
                <Document v-if="getPrimaryDocumentType(row) === 'REQUIREMENT'" />
                <Folder v-else-if="getPrimaryDocumentType(row) === 'DESIGN'" />
                <Files v-else />
              </el-icon>
              <div>
                <div class="task-name-text">{{ row.taskName }}</div>
              </div>
            </div>
          </template>
        </el-table-column>
        <el-table-column label="文档类型" min-width="160">
          <template #default="{ row }">
            <div class="document-types">
              <template v-if="row.documentTypes && row.documentTypes.length > 0">
                <el-tag
                  v-for="type in row.documentTypes"
                  :key="type"
                  :type="getDocumentTypeColor(type)"
                  size="small"
                  class="doc-type-tag"
                >
                  {{ getDocumentTypeText(type) }}
                </el-tag>
              </template>
              <span v-else class="no-type">-</span>
            </div>
          </template>
        </el-table-column>
        <el-table-column label="状态" width="120">
          <template #default="{ row }">
            <el-tag :type="getStatusColor(row.taskStatus)">
              {{ getStatusText(row.taskStatus) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="用例数量" width="100" align="center">
          <template #default="{ row }">
            {{ row.resultCount || '-' }}
          </template>
        </el-table-column>
        <el-table-column label="生成时间" width="180">
          <template #default="{ row }">
            {{ formatDateTime(row.createTime) }}
          </template>
        </el-table-column>
        <el-table-column label="耗时" width="100" align="center">
          <template #default="{ row }">
            {{ formatDuration(row.duration) }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" size="small" v-if="row.taskStatus !== 3" @click="handleView(row)">
              详情
            </el-button>
            <el-button link type="primary" size="small" v-if="row.taskStatus === 2 || row.taskStatus === 4" @click="handleDownload(row)">
              下载
            </el-button>
            <el-button link type="primary" size="small" v-if="row.taskStatus === 2" @click="handleEvaluate(row)">
              评价
            </el-button>
            <el-button link type="danger" size="small" v-if="row.taskStatus === 1" @click="handleCancel(row)">
              取消
            </el-button>
            <el-button link type="danger" size="small" v-if="row.taskStatus === 3" @click="handleDelete(row)">
              删除
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <!-- 分页 -->
      <div class="pagination-container">
        <el-pagination
          v-model:current-page="pagination.current"
          v-model:page-size="pagination.size"
          :page-sizes="[10, 20, 50, 100]"
          :total="pagination.total"
          layout="total, sizes, prev, pager, next, jumper"
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
        />
      </div>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Document, Folder, Files, CircleCheck, CircleClose, Loading, Top, Bottom, Search } from '@element-plus/icons-vue'
import { getTaskStatistics, getTaskList, getTaskDetail, downloadTestCase } from '../api/task'
import TaskFeedbackDialog from '../components/TaskFeedbackDialog.vue'
import TaskDetailDialog from '../components/TaskDetailDialog.vue'

interface Task {
  id: number
  taskName: string
  documentTypes: string[]
  documentType?: string
  taskStatus: number
  resultCount: number
  createTime: string
  duration: number
  [key: string]: any
}

interface Statistics {
  totalCount: number
  successCount: number
  failedCount: number
  processingCount: number
}

const loading = ref(false)
const taskList = ref<Task[]>([])
const statistics = ref<Statistics>({
  totalCount: 0,
  successCount: 0,
  failedCount: 0,
  processingCount: 0
})

const feedbackDialogVisible = ref(false)
const currentTask = ref<Task | null>(null)

const detailDialogVisible = ref(false)
const currentDetailTask = ref<Task | null>(null)

const filterForm = reactive({
  taskName: '',
  taskStatus: undefined as number | undefined,
  documentType: '',
  timeRange: ''
})

const pagination = reactive({
  current: 1,
  size: 10,
  total: 0
})

/**
 * 加载统计信息
 */
const loadStatistics = async () => {
  try {
    const res = await getTaskStatistics()
    if (res) {
      statistics.value = {
        totalCount: res.totalCount || 0,
        successCount: res.successCount || 0,
        failedCount: res.failedCount || 0,
        processingCount: res.processingCount || 0
      }
    }
  } catch (error) {
    console.error('加载统计数据失败', error)
  }
}

/**
 * 获取当前时间（北京时间格式）
 */
const getCurrentTime = () => {
  const now = new Date()
  const year = now.getFullYear()
  const month = String(now.getMonth() + 1).padStart(2, '0')
  const day = String(now.getDate()).padStart(2, '0')
  const hours = String(now.getHours()).padStart(2, '0')
  const minutes = String(now.getMinutes()).padStart(2, '0')
  const seconds = String(now.getSeconds()).padStart(2, '0')
  return `${year}-${month}-${day}T${hours}:${minutes}:${seconds}`
}

/**
 * 加载任务列表
 */
const loadTaskList = async () => {
  loading.value = true
  try {
    const params = {
      current: pagination.current,
      size: pagination.size,
      taskName: filterForm.taskName || undefined,
      taskStatus: filterForm.taskStatus,
      documentType: filterForm.documentType || undefined,
      startTime: filterForm.timeRange ? getStartTime(filterForm.timeRange) : undefined,
      endTime: filterForm.timeRange ? getCurrentTime() : undefined
    }
    
    const res = await getTaskList(params)
    if (res) {
      taskList.value = res.records || []
      pagination.total = res.total || 0
    }
  } catch (error) {
    console.error('加载任务列表失败', error)
    ElMessage.error('加载任务列表失败')
  } finally {
    loading.value = false
  }
}

/**
 * 获取开始时间
 */
const getStartTime = (range: string) => {
  const now = new Date()
  const days = parseInt(range)
  now.setDate(now.getDate() - days)
  // 返回北京时间格式的字符串（不带时区）
  const year = now.getFullYear()
  const month = String(now.getMonth() + 1).padStart(2, '0')
  const day = String(now.getDate()).padStart(2, '0')
  const hours = String(now.getHours()).padStart(2, '0')
  const minutes = String(now.getMinutes()).padStart(2, '0')
  const seconds = String(now.getSeconds()).padStart(2, '0')
  return `${year}-${month}-${day}T${hours}:${minutes}:${seconds}`
}

/**
 * 搜索
 */
const handleSearch = () => {
  pagination.current = 1
  loadTaskList()
}

/**
 * 重置
 */
const handleReset = () => {
  filterForm.taskName = ''
  filterForm.taskStatus = undefined
  filterForm.documentType = ''
  filterForm.timeRange = ''
  pagination.current = 1
  loadTaskList()
}

/**
 * 分页大小改变
 */
const handleSizeChange = () => {
  loadTaskList()
}

/**
 * 页码改变
 */
const handleCurrentChange = () => {
  loadTaskList()
}

/**
 * 查看详情
 */
const handleView = async (row: Task) => {
  try {
    const res = await getTaskDetail(row.id)
    if (res) {
      currentDetailTask.value = res
      detailDialogVisible.value = true
    }
  } catch (error) {
    console.error('获取任务详情失败', error)
    ElMessage.error('获取任务详情失败')
  }
}

/**
 * 评价
 */
const handleEvaluate = (row: Task) => {
  currentTask.value = row
  feedbackDialogVisible.value = true
}

/**
 * 评价提交成功
 */
const handleFeedbackSuccess = () => {
  loadTaskList()
  loadStatistics()
}

/**
 * 下载测试用例文件
 */
const handleDownload = async (row: Task) => {
  try {
    const blob = await downloadTestCase(row.id)
    const url = window.URL.createObjectURL(blob)
    const link = document.createElement('a')
    link.href = url
    
    // 从 taskName 中提取文件名，去掉扩展名后加 .md
    let fileName = row.taskName || 'test-cases'
    if (fileName.includes('.')) {
      fileName = fileName.substring(0, fileName.lastIndexOf('.'))
    }
    fileName = fileName + '.md'
    
    link.download = fileName
    document.body.appendChild(link)
    link.click()
    document.body.removeChild(link)
    window.URL.revokeObjectURL(url)
    ElMessage.success('文件下载成功')
  } catch (error) {
    console.error('下载失败', error)
    ElMessage.error('文件下载失败，请重试')
  }
}

/**
 * 取消任务
 */
const handleCancel = (row: Task) => {
  ElMessageBox.confirm('确定要取消该任务吗？', '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(() => {
    ElMessage.success('任务已取消')
    loadTaskList()
  }).catch(() => {})
}

/**
 * 删除任务
 */
const handleDelete = (row: Task) => {
  ElMessageBox.confirm('确定要删除该任务吗？', '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(() => {
    ElMessage.success('任务已删除')
    loadTaskList()
    loadStatistics()
  }).catch(() => {})
}

/**
 * 获取状态文本
 */
const getStatusText = (status: number) => {
  const statusMap: Record<number, string> = {
    1: '生成中',
    2: '已完成',
    3: '生成失败',
    4: '已评价'
  }
  return statusMap[status] || '未知'
}

/**
 * 获取状态颜色
 */
const getStatusColor = (status: number) => {
  const colorMap: Record<number, string> = {
    1: 'primary',
    2: 'success',
    3: 'danger',
    4: 'warning'
  }
  return colorMap[status] || 'info'
}

/**
 * 获取文档类型文本
 */
const getDocumentTypeText = (type: string) => {
  if (!type) return '-'
  const typeMap: Record<string, string> = {
    'REQUIREMENT': '需求文档',
    'DESIGN': '设计文档',
    'TEST': '测试文档'
  }
  return typeMap[type] || type
}

/**
 * 获取文档类型颜色
 */
const getDocumentTypeColor = (type: string) => {
  if (!type) return 'info'
  const colorMap: Record<string, string> = {
    'REQUIREMENT': 'primary',
    'DESIGN': 'success',
    'TEST': 'warning'
  }
  return colorMap[type] || 'info'
}

/**
 * 获取任务的主要文档类型（用于任务名称旁的图标）
 * 优先取 documentTypes 第一个，否则回退到 documentType
 */
const getPrimaryDocumentType = (row: Task) => {
  if (row.documentTypes && row.documentTypes.length > 0) {
    return row.documentTypes[0]
  }
  return row.documentType || ''
}

/**
 * 格式化日期时间
 */
const formatDateTime = (dateStr: string) => {
  if (!dateStr) return '-'
  const date = new Date(dateStr)
  return date.toLocaleString('zh-CN', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit'
  })
}

/**
 * 格式化持续时间
 */
const formatDuration = (seconds: number) => {
  if (!seconds) return '-'
  const mins = Math.floor(seconds / 60)
  const secs = seconds % 60
  return `${mins}分${secs}秒`
}

onMounted(() => {
  loadStatistics()
  loadTaskList()
})
</script>

<style scoped lang="scss">
.task-management {
  padding: 20px;

  .page-header {
    margin-bottom: 20px;

    .page-title {
      font-size: 24px;
      font-weight: 600;
      color: #1f2937;
      margin: 0 0 8px 0;
    }

    .page-description {
      font-size: 14px;
      color: #6b7280;
      margin: 0;
    }
  }

  .statistics-cards {
    margin-bottom: 20px;

    .stat-card {
      border-radius: 8px;
      
      .stat-header {
        display: flex;
        justify-content: space-between;
        align-items: center;
        margin-bottom: 12px;

        .stat-label {
          font-size: 14px;
          color: #6b7280;
        }

        .stat-icon {
          font-size: 20px;
          color: #3b82f6;

          &.success {
            color: #10b981;
          }

          &.primary {
            color: #3b82f6;
          }

          &.danger {
            color: #ef4444;
          }
        }
      }

      .stat-value {
        font-size: 32px;
        font-weight: 600;
        color: #1f2937;
        margin-bottom: 8px;
      }

      .stat-trend {
        display: flex;
        align-items: center;
        font-size: 12px;
        gap: 4px;

        &.positive {
          color: #10b981;
        }

        &.negative {
          color: #ef4444;
        }
      }
    }
  }

  .filter-card {
    margin-bottom: 20px;
    border-radius: 8px;

    .filter-form {
      display: flex;
      flex-wrap: wrap;
      gap: 12px;

      .search-item {
        margin-left: auto;
      }
    }
  }

  .table-card {
    border-radius: 8px;

    .task-name {
      display: flex;
      align-items: center;
      gap: 12px;

      .task-icon {
        font-size: 24px;
        color: #3b82f6;
      }

      .task-name-text {
        font-weight: 500;
        color: #1f2937;
        margin-bottom: 4px;
      }

      .task-id {
        font-size: 12px;
        color: #9ca3af;
      }
    }

    .document-types {
      display: flex;
      flex-wrap: wrap;
      gap: 4px;
      align-items: center;

      .doc-type-tag {
        margin: 0;
      }

      .no-type {
        color: #9ca3af;
        font-size: 12px;
      }
    }

    .pagination-container {
      display: flex;
      justify-content: flex-end;
      margin-top: 20px;
    }
  }
}
</style>

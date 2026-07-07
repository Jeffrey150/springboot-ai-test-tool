<template>
  <el-dialog
    v-model="visible"
    :title="''"
    width="800px"
    :close-on-click-modal="false"
    :show-close="false"
    @close="handleClose"
  >
    <div class="task-detail-dialog">
      <!-- 头部信息 -->
      <div class="dialog-header">
        <div class="header-left">
          <div class="task-icon">
            <el-icon><Document /></el-icon>
          </div>
          <div class="task-info">
            <div class="task-name">{{ taskDetail.taskName }}</div>
            <div class="task-meta">
              <el-tag :type="getStatusType(taskDetail.taskStatus)" size="small">
                {{ getStatusText(taskDetail.taskStatus) }}
              </el-tag>
              <span class="task-id">ID: TASK-{{ taskDetail.id }}</span>
            </div>
          </div>
        </div>
        <el-button link @click="handleClose">
          <el-icon><Close /></el-icon>
        </el-button>
      </div>

      <!-- 任务信息卡片 -->
      <div class="info-card">
        <div class="info-item">
          <div class="info-label">文档类型</div>
          <div class="info-value">{{ getDocumentTypeText(taskDetail.documentType) }}</div>
        </div>
        <div class="info-item">
          <div class="info-label">生成用例数量</div>
          <div class="info-value">{{ taskDetail.resultCount || 0 }}条</div>
        </div>
        <div class="info-item">
          <div class="info-label">开始时间</div>
          <div class="info-value">{{ formatDateTime(taskDetail.startTime) }}</div>
        </div>
        <div class="info-item">
          <div class="info-label">完成时间</div>
          <div class="info-value">{{ formatDateTime(taskDetail.endTime) }}</div>
        </div>
        <div class="info-item">
          <div class="info-label">总耗时</div>
          <div class="info-value">{{ formatDuration(taskDetail.duration) }}</div>
        </div>
      </div>

      <!-- 用例预览 -->
      <div class="preview-section">
        <div class="preview-title">用例预览</div>
        <div class="preview-content" v-html="renderedMarkdown"></div>
      </div>

      <!-- 底部按钮 -->
      <div class="dialog-footer">
        <el-button type="primary" @click="handleDownload">
          <el-icon><Download /></el-icon>
          下载用例文件
        </el-button>
        <el-button v-if="taskDetail.taskStatus !== 4" @click="handleEvaluate">
          <el-icon><Star /></el-icon>
          评价用例质量
        </el-button>
        <el-button @click="handleClose">关闭</el-button>
      </div>
    </div>
  </el-dialog>

  <!-- 评价弹窗 -->
  <TaskFeedbackDialog
    v-if="taskDetail.id"
    v-model="feedbackDialogVisible"
    :task="taskDetail"
    @submit-success="handleFeedbackSuccess"
  />
</template>

<script setup lang="ts">
import { ref, computed, watch } from 'vue'
import { ElMessage } from 'element-plus'
import { Document, Close, Download, Star } from '@element-plus/icons-vue'
import { marked } from 'marked'
import { downloadTestCase } from '../api/task'
import TaskFeedbackDialog from './TaskFeedbackDialog.vue'

interface TaskDetail {
  id: number
  taskName: string
  documentType: string
  taskStatus: number
  resultCount: number
  startTime: string
  endTime: string
  duration: number
  qualityScore: number
  aiResponse: string
  createTime: string
}

const props = defineProps<{
  modelValue: boolean
  task: TaskDetail | null
}>()

const emit = defineEmits<{
  'update:modelValue': [value: boolean]
  success: []
}>()

const visible = computed({
  get: () => props.modelValue,
  set: (val) => emit('update:modelValue', val)
})

const taskDetail = ref<TaskDetail>({
  id: 0,
  taskName: '',
  documentType: '',
  taskStatus: 0,
  resultCount: 0,
  startTime: '',
  endTime: '',
  duration: 0,
  qualityScore: 0,
  aiResponse: '',
  createTime: ''
})

const feedbackDialogVisible = ref(false)

const renderedMarkdown = computed(() => {
  if (!taskDetail.value.aiResponse) {
    return '<p style="color: #909399; text-align: center; padding: 40px 0;">暂无用例内容</p>'
  }
  return marked(taskDetail.value.aiResponse)
})

const getStatusText = (status: number) => {
  const statusMap: Record<number, string> = {
    1: '进行中',
    2: '已完成',
    3: '生成失败',
    4: '已评价'
  }
  return statusMap[status] || '未知'
}

const getStatusType = (status: number) => {
  const typeMap: Record<number, string> = {
    1: 'warning',
    2: 'success',
    3: 'danger',
    4: 'info'
  }
  return typeMap[status] || 'info'
}

const getDocumentTypeText = (type: string) => {
  const typeMap: Record<string, string> = {
    'REQUIREMENT': '需求文档',
    'DESIGN': '设计文档',
    'TEST': '测试文档'
  }
  return typeMap[type] || type
}

const formatDateTime = (dateTime: string) => {
  if (!dateTime) return '-'
  return dateTime.replace('T', ' ').substring(0, 19)
}

const formatDuration = (seconds: number) => {
  if (!seconds) return '-'
  const minutes = Math.floor(seconds / 60)
  const secs = seconds % 60
  if (minutes > 0) {
    return `${minutes}分${secs}秒`
  }
  return `${secs}秒`
}

const handleClose = () => {
  visible.value = false
}

const handleDownload = async () => {
  try {
    const blob = await downloadTestCase(taskDetail.value.id)
    const url = window.URL.createObjectURL(blob)
    const link = document.createElement('a')
    link.href = url

    let fileName = taskDetail.value.taskName || 'test-cases'
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

const handleEvaluate = () => {
  if (taskDetail.value.taskStatus === 3) {
    ElMessage.warning('任务生成失败，无法评价')
    return
  }
  feedbackDialogVisible.value = true
}

const handleFeedbackSuccess = () => {
  emit('success')
}

const open = (task: TaskDetail) => {
  taskDetail.value = task
  visible.value = true
}

watch(
  () => props.task,
  (newTask) => {
    if (newTask) {
      taskDetail.value = newTask
    }
  },
  { immediate: true }
)

defineExpose({ open })
</script>

<style scoped lang="scss">
.task-detail-dialog {
  .dialog-header {
    display: flex;
    justify-content: space-between;
    align-items: flex-start;
    margin-bottom: 24px;

    .header-left {
      display: flex;
      align-items: center;
      gap: 12px;

      .task-icon {
        width: 40px;
        height: 40px;
        background: #fef0f0;
        border-radius: 8px;
        display: flex;
        align-items: center;
        justify-content: center;
        color: #f56c6c;
        font-size: 20px;
      }

      .task-info {
        .task-name {
          font-size: 16px;
          font-weight: 600;
          color: #303133;
          margin-bottom: 4px;
        }

        .task-meta {
          display: flex;
          align-items: center;
          gap: 12px;

          .task-id {
            font-size: 12px;
            color: #909399;
          }
        }
      }
    }
  }

  .info-card {
    display: flex;
    background: #f5f7fa;
    border-radius: 8px;
    padding: 16px 24px;
    margin-bottom: 24px;

    .info-item {
      flex: 1;

      .info-label {
        font-size: 12px;
        color: #909399;
        margin-bottom: 4px;
      }

      .info-value {
        font-size: 14px;
        color: #303133;
        font-weight: 500;
      }
    }
  }

  .preview-section {
    margin-bottom: 24px;

    .preview-title {
      font-size: 14px;
      font-weight: 600;
      color: #303133;
      margin-bottom: 12px;
    }

    .preview-content {
      border: 1px solid #e4e7ed;
      border-radius: 8px;
      padding: 16px;
      max-height: 400px;
      overflow-y: auto;
      background: #fafafa;

      :deep(h1) {
        font-size: 20px;
        margin: 0 0 12px;
        color: #303133;
      }

      :deep(h2) {
        font-size: 16px;
        margin: 16px 0 8px;
        color: #303133;
      }

      :deep(h3) {
        font-size: 14px;
        margin: 12px 0 6px;
        color: #303133;
      }

      :deep(p) {
        font-size: 13px;
        color: #606266;
        line-height: 1.8;
        margin: 8px 0;
      }

      :deep(ul), :deep(ol) {
        padding-left: 20px;
        margin: 8px 0;

        li {
          font-size: 13px;
          color: #606266;
          line-height: 1.8;
        }
      }

      :deep(code) {
        background: #f0f2f5;
        padding: 2px 6px;
        border-radius: 4px;
        font-size: 12px;
        color: #e6a23c;
      }

      :deep(pre) {
        background: #1e293b;
        color: #e2e8f0;
        padding: 12px 16px;
        border-radius: 6px;
        overflow-x: auto;
        margin: 12px 0;

        code {
          background: none;
          color: inherit;
          padding: 0;
        }
      }

      :deep(blockquote) {
        border-left: 3px solid #409eff;
        padding-left: 12px;
        margin: 12px 0;
        color: #606266;
      }

      :deep(table) {
        width: 100%;
        border-collapse: collapse;
        margin: 12px 0;

        th, td {
          border: 1px solid #e4e7ed;
          padding: 8px 12px;
          font-size: 13px;
        }

        th {
          background: #f5f7fa;
          font-weight: 600;
        }
      }
    }
  }

  .dialog-footer {
    display: flex;
    justify-content: space-between;
    padding-top: 16px;
    border-top: 1px solid #e4e7ed;
  }
}
</style>

<template>
  <div class="document-generate-page">
    <el-container>
      <!-- <el-header class="page-header">
        <div class="header-content">
          <div class="logo-section">
            <div class="logo-icon">AI</div>
            <span class="logo-text">TestAI</span>
          </div>
          <el-menu
            :default-active="activeMenu"
            mode="horizontal"
            :ellipsis="false"
            @select="handleMenuSelect"
            class="nav-menu"
          >
            <el-menu-item index="generate">首页</el-menu-item>
            <el-menu-item index="tasks">任务管理</el-menu-item>
            <el-menu-item index="documents">文档管理</el-menu-item>
            <el-menu-item index="settings">设置</el-menu-item>
          </el-menu>
        </div>
      </el-header> -->

      <el-main class="main-content">
        <div class="page-title-section">
          <h1 class="page-title">AI测试用例生成助手</h1>
          <p class="page-subtitle">上传需求文档或设计文档，一键生成全面、高质量的测试用例，提升测试效率</p>
        </div>

        <el-row :gutter="20" class="main-row">
          <el-col :xs="24" :sm="24" :md="24" :lg="18" :xl="17" class="main-col">
            <el-card class="main-card">
              <div class="card-section">
                <div class="section-header">
                  <span class="section-title">上传文档</span>
                </div>

                <div class="form-section">
                  <div class="section-label">任务号 <span class="required">*</span></div>
                  <el-input
                    v-model="taskNo"
                    placeholder="请输入任务号，如 CDXB-00001"
                    class="task-no-input"
                  />
                  <div v-if="!taskNo" class="error-hint">任务号为必填项</div>
                </div>

                <div class="form-section">
                  <div class="section-label">需求文档（可选）</div>
                  <el-upload
                    drag
                    :action="uploadUrl + '?documentType=REQUIREMENT'"
                    :before-upload="handleBeforeUpload"
                    :on-success="(response, file, fileList) => handleUploadSuccess(response, file, fileList, 'REQUIREMENT')"
                    :on-error="handleUploadError"
                    :on-remove="(file) => handleRemove(file, 'REQUIREMENT')"
                    :file-list="requirementFileList"
                    multiple
                    accept=".pdf,.doc,.docx,.md"
                    class="upload-area"
                  >
                    <div class="upload-icon-wrapper">📋</div>
                    <div class="upload-text">拖拽需求文档到此处，或<em>点击选择文件</em></div>
                    <div class="upload-hint">支持PDF、Word、Markdown格式，单个文件不超过50MB</div>
                    <div class="upload-formats">
                      <span class="format-tag">PDF</span>
                      <span class="format-tag">DOCX</span>
                      <span class="format-tag">DOC</span>
                      <span class="format-tag">MD</span>
                    </div>
                  </el-upload>
                  <div v-if="requirementDocuments.length > 0" class="uploaded-docs-info">
                    <span class="docs-count">已上传 {{ requirementDocuments.length }} 个需求文档</span>
                  </div>
                </div>

                <div class="form-section">
                  <div class="section-label">设计文档（可选）</div>
                  <el-upload
                    drag
                    :action="uploadUrl + '?documentType=DESIGN'"
                    :before-upload="handleBeforeUpload"
                    :on-success="(response, file, fileList) => handleUploadSuccess(response, file, fileList, 'DESIGN')"
                    :on-error="handleUploadError"
                    :on-remove="(file) => handleRemove(file, 'DESIGN')"
                    :file-list="designFileList"
                    multiple
                    accept=".pdf,.doc,.docx,.md"
                    class="upload-area"
                  >
                    <div class="upload-icon-wrapper">📐</div>
                    <div class="upload-text">拖拽设计文档到此处，或<em>点击选择文件</em></div>
                    <div class="upload-hint">支持PDF、Word、Markdown格式，单个文件不超过50MB</div>
                    <div class="upload-formats">
                      <span class="format-tag">PDF</span>
                      <span class="format-tag">DOCX</span>
                      <span class="format-tag">DOC</span>
                      <span class="format-tag">MD</span>
                    </div>
                  </el-upload>
                  <div v-if="designDocuments.length > 0" class="uploaded-docs-info">
                    <span class="docs-count">已上传 {{ designDocuments.length }} 个设计文档</span>
                  </div>
                </div>

                <div v-if="uploadedDocuments.length > 0" class="combined-docs-summary">
                  <el-divider content-position="left">
                    <span class="summary-title">📄 已上传文档汇总</span>
                  </el-divider>
                  <div class="docs-list">
                    <div v-for="doc in uploadedDocuments" :key="doc.id" class="doc-item">
                      <span class="doc-type-badge" :class="doc.documentType.toLowerCase()">
                        {{ doc.documentType === 'REQUIREMENT' ? '需求' : '设计' }}
                      </span>
                      <span class="doc-name">{{ doc.fileName }}</span>
                      <span class="doc-size">{{ formatFileSize(doc.fileSize) }}</span>
                    </div>
                  </div>
                </div>
              </div>

              <div class="card-divider"></div>

              <div class="card-section">
              </div>

              <div class="card-divider"></div>

              <div class="card-section">
                <div class="section-header">
                  <span class="section-title">AI模型配置</span>
                </div>

                <el-form :model="aiConfig" label-width="100px" class="config-form">
                  <el-row class="config-row">
                    <el-col :xs="24" class="config-col-full">
                      <el-form-item label="平台" class="form-item">
                        <template #label>
                          <span>平台</span>
                          <span class="required">*</span>
                        </template>
                        <el-select v-model="aiConfig.platform" @change="handlePlatformChange" class="config-select-full">
                          <el-option label="阿里云百炼" value="aliyun" />
                          <el-option label="OpenAI" value="openai" />
                          <el-option label="Gemini" value="gemini" />
                          <el-option label="自定义" value="custom" />
                        </el-select>
                      </el-form-item>
                      <div v-if="!aiConfig.platform" class="error-hint">平台为必填项</div>
                    </el-col>
                  </el-row>
                  <el-row class="config-row">
                    <el-col :xs="24" class="config-col-full">
                      <el-form-item label="API地址" class="form-item">
                        <template #label>
                          <span>API地址</span>
                          <span class="required">*</span>
                        </template>
                        <el-input v-model="aiConfig.apiUrl" placeholder="请输入API地址" class="config-input-full" />
                      </el-form-item>
                      <div v-if="!aiConfig.apiUrl" class="error-hint">API地址为必填项</div>
                    </el-col>
                  </el-row>
                  <el-row class="config-row">
                    <el-col :xs="24" class="config-col-full">
                      <el-form-item label="模型" class="form-item">
                        <template #label>
                          <span>模型</span>
                          <span class="required">*</span>
                        </template>
                        <el-input v-model="aiConfig.model" placeholder="例如：qwen-plus" class="config-input-full" />
                      </el-form-item>
                      <div v-if="!aiConfig.model" class="error-hint">模型为必填项</div>
                    </el-col>
                  </el-row>
                  <el-row :gutter="16" class="config-row">
                    <el-col :xs="24" class="config-col-full">
                      <el-form-item label="API Key" class="form-item">
                        <template #label>
                          <span>API Key</span>
                          <span class="required">*</span>
                        </template>
                        <el-input v-model="aiConfig.apiKey" type="password" placeholder="请输入API Key" show-password class="config-input-full" />
                      </el-form-item>
                      <div v-if="!aiConfig.apiKey" class="error-hint">API Key为必填项</div>
                    </el-col>
                  </el-row>
                </el-form>
              </div>

              <div class="action-buttons">
                <el-button type="primary" size="large" :loading="generating" :disabled="!canGenerate" @click="handleGenerate" class="primary-btn">
                  开始生成测试用例
                </el-button>
              </div>
            </el-card>
          </el-col>

          <el-col :xs="24" :sm="24" :md="24" :lg="6" :xl="7" class="side-col">
            <div class="right-panel">
              <el-card class="stats-card">
                <div class="card-header-row">
                  <span class="card-title">使用统计</span>
                  <el-select v-model="statsPeriod" class="period-select">
                    <el-option label="本月" value="month" />
                    <el-option label="本周" value="week" />
                    <el-option label="全部" value="all" />
                  </el-select>
                </div>
                <div class="stats-content">
                  <div class="stat-item">
                    <div class="stat-icon blue-icon">📊</div>
                    <div class="stat-info">
                      <div class="stat-number">{{ stats.completedTestCaseCount.toLocaleString() }}</div>
                      <div class="stat-label">已生成用例</div>
                    </div>
                  </div>
                  <div class="stat-divider"></div>
                  <div class="stat-item">
                    <div class="stat-icon green-icon">✅</div>
                    <div class="stat-info">
                      <div class="stat-number">{{ stats.completedTaskCount }}</div>
                      <div class="stat-label">已完成任务</div>
                    </div>
                  </div>
                  <div class="stat-divider"></div>
                  <div class="stat-item">
                    <div class="stat-icon purple-icon">⏱️</div>
                    <div class="stat-info">
                      <div class="stat-number">{{ formatDuration(stats.completedTotalDuration) }}</div>
                      <div class="stat-label">用例生成耗时</div>
                    </div>
                  </div>
                </div>
              </el-card>

              <el-card class="recent-tasks-card">
                <div class="card-header-row">
                  <span class="card-title">最近任务</span>
                  <el-button type="text" class="view-all-btn" @click="goToTasks">查看全部 →</el-button>
                </div>
                <div class="task-list">
                  <div v-for="task in recentTasks" :key="task.id" class="task-item" @click="goToTaskDetail(task.id)">
                    <div class="task-info">
                      <div class="task-header">
                        <span class="task-name">{{ task.taskName }}</span>
                        <el-tag :type="getStatusType(task.taskStatus)" size="small" class="status-tag">
                          {{ getStatusText(task.taskStatus) }}
                        </el-tag>
                      </div>
                      <div class="task-meta">
                        <template v-if="task.taskStatus === 2 || task.taskStatus === 4">
                          <span class="meta-item">生成用例：{{ task.resultCount || 0 }}条</span>
                          <span v-if="task.duration" class="meta-separator">·</span>
                          <span v-if="task.duration" class="meta-item">耗时{{ formatDuration(task.duration) }}</span>
                          <span class="meta-item meta-spacer"></span>
                          <el-button type="text" size="small" class="action-btn download-btn" @click.stop="handleDownload(task)">
                            <span>下载</span>
                          </el-button>
                        </template>
                      </div>
                      <div v-if="task.taskStatus === 1" class="task-progress">
                        <div class="progress-bar">
                          <div class="progress-fill" :style="{ width: (task.progress || 50) + '%' }"></div>
                        </div>
                        <span class="progress-text">已完成{{ task.progress || 50 }}%</span>
                      </div>
                    </div>
                  </div>
                  <el-empty v-if="recentTasks.length === 0" description="暂无任务" class="empty-state" />
                </div>
              </el-card>

              <el-card class="tips-card">
                <div class="card-header-row">
                  <span class="tip-icon">💡</span>
                  <span class="card-title">使用小贴士</span>
                </div>
                <ul class="tips-list">
                  <li>
                    <span class="tip-number">1</span>
                    <span>上传清晰的需求文档，包含完整的功能描述和业务规则，生成的用例质量更高</span>
                  </li>
                  <li>
                    <span class="tip-number">2</span>
                    <span>生成完成后可以对结果进行评价，帮助我们不断优化生成效果</span>
                  </li>
                </ul>
              </el-card>
            </div>
          </el-col>
        </el-row>
      </el-main>

      <el-dialog v-model="showProgressDialog" title="正在生成测试用例" width="500px" :close-on-click-modal="false" class="progress-dialog">
        <div class="progress-content">
          <el-progress :percentage="progress" :status="progress === 100 ? 'success' : undefined" :stroke-width="12" class="main-progress" />
          <div class="progress-status">{{ progressStatus }}</div>
          <div class="progress-tips">请耐心等待，生成时间取决于文档复杂度</div>
        </div>
      </el-dialog>

      <TaskDetailDialog
        v-model="detailDialogVisible"
        :task="currentDetailTask"
      />
    </el-container>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, watch } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { getTaskList, generateTestCases, downloadTestCase, getTaskDetail, type Task, type AiConfig } from '@/api/task'
import { getTaskStatistics, type TaskStatistics } from '@/api/taskStatistics'
import TaskDetailDialog from '@/components/TaskDetailDialog.vue'

const router = useRouter()

const activeMenu = ref('generate')
const taskNo = ref('')
const requirementFileList = ref<any[]>([])
const designFileList = ref<any[]>([])
const requirementDocuments = ref<any[]>([])
const designDocuments = ref<any[]>([])
const generating = ref(false)
const showProgressDialog = ref(false)
const progress = ref(0)
const progressStatus = ref('正在准备...')
const statsPeriod = ref('month')

const uploadedDocuments = computed(() => {
  return [...requirementDocuments.value, ...designDocuments.value]
})

const aiConfig = ref<AiConfig>({
  platform: 'aliyun',
  apiUrl: '',
  model: '',
  apiKey: ''
})

const stats = ref<TaskStatistics>({
  completedTaskCount: 0,
  evaluatedTaskCount: 0,
  completedTestCaseCount: 0,
  evaluatedTestCaseCount: 0,
  completedTotalDuration: 0,
  evaluatedTotalDuration: 0
})

const recentTasks = ref<Task[]>([])

const detailDialogVisible = ref(false)
const currentDetailTask = ref<any>(null)

const canGenerate = computed(() => {
  return taskNo.value.trim() &&
    uploadedDocuments.value.length > 0 &&
    aiConfig.value.platform &&
    aiConfig.value.model &&
    aiConfig.value.apiKey
})

const uploadUrl = '/documents/upload'

const handleMenuSelect = (index: string) => {
  if (index === 'generate') {
    router.push('/generate')
  } else if (index === 'tasks') {
    router.push('/tasks')
  } else if (index === 'documents') {
    router.push('/documents')
  } else if (index === 'settings') {
    router.push('/settings')
  }
}

const handleBeforeUpload = (file: File) => {
  const isValidSize = file.size / 1024 / 1024 < 50
  if (!isValidSize) {
    ElMessage.error('文件大小不能超过 50MB')
    return false
  }
  const fileName = file.name.toLowerCase()
  const isAllowed = fileName.endsWith('.pdf') || fileName.endsWith('.doc') || fileName.endsWith('.docx') || fileName.endsWith('.md')
  if (!isAllowed) {
    ElMessage.error('只支持 PDF、DOC、DOCX、Markdown 格式')
    return false
  }
  return true
}

const handleUploadSuccess = (response: any, file: any, fileList: any, docType: string) => {
  console.log('========== 上传响应开始 ==========')
  console.log('响应类型:', typeof response)
  console.log('响应是否为对象:', response !== null && typeof response === 'object')
  console.log('响应完整数据:', response)
  console.log('文档类型:', docType)
  console.log('========== 上传响应结束 ==========')
  
  let data = response
  
  if (response && typeof response === 'object' && response.code !== undefined && response.data !== undefined) {
    data = response.data
  }
  
  const docs = Array.isArray(data) ? data : [data]
  
  if (docType === 'REQUIREMENT') {
    requirementDocuments.value = [...requirementDocuments.value, ...docs]
    ElMessage.success(`成功上传 ${docs.length} 个需求文档`)
  } else {
    designDocuments.value = [...designDocuments.value, ...docs]
    ElMessage.success(`成功上传 ${docs.length} 个设计文档`)
  }
  
  console.log('需求文档:', requirementDocuments.value)
  console.log('设计文档:', designDocuments.value)
}

const handleUploadError = (error: any) => {
  let errorMsg = '文件上传失败'
  if (error.response) {
    const data = error.response.data
    if (data && data.msg) {
      errorMsg = data.msg
    } else if (error.response.status === 500) {
      errorMsg = '服务器内部错误'
    } else if (error.response.status === 400) {
      errorMsg = '请求参数错误'
    }
  } else if (error.message) {
    errorMsg = error.message
  }
  ElMessage.error(errorMsg)
}

const handleRemove = (file: any, docType: string) => {
  if (docType === 'REQUIREMENT') {
    requirementDocuments.value = []
  } else {
    designDocuments.value = []
  }
}

const formatFileSize = (bytes: number) => {
  if (bytes < 1024) return bytes + ' B'
  if (bytes < 1024 * 1024) return (bytes / 1024).toFixed(1) + ' KB'
  return (bytes / (1024 * 1024)).toFixed(1) + ' MB'
}

const handlePlatformChange = (platform: string) => {
  const platformUrls: Record<string, string> = {
    aliyun: 'https://dashscope.aliyuncs.com/compatible-mode/v1/chat/completions',
    openai: 'https://api.openai.com/v1/chat/completions',
    gemini: 'https://generativelanguage.googleapis.com/v1beta/models'
  }
  aiConfig.value.apiUrl = platformUrls[platform] || ''
  aiConfig.value.model = ''
  aiConfig.value.apiKey = ''
}

const handleGenerate = async () => {
  if (!taskNo.value.trim()) {
    ElMessage.warning('请输入任务号')
    return
  }

  if (uploadedDocuments.value.length === 0) {
    ElMessage.warning('请先上传文档')
    return
  }

  generating.value = true
  showProgressDialog.value = true
  progress.value = 10
  progressStatus.value = '正在解析文档...'

  try {
    progress.value = 30
    progressStatus.value = '正在提取功能点...'

    const requestData = {
      documentIds: uploadedDocuments.value.map(doc => Number(doc.id)),
      taskName: uploadedDocuments.value.length === 1 
        ? uploadedDocuments.value[0].fileName 
        : `批量生成_${uploadedDocuments.value.length}个文档`,
      taskNo: taskNo.value.trim(),
      aiConfig: aiConfig.value
    }

    console.log('请求数据:', requestData)

    const response = await generateTestCases(requestData)

    progress.value = 100
    progressStatus.value = '调用大模型成功'

    ElMessage.success('任务已提交，正在生成中...')

    // 刷新最近任务列表
    await loadRecentTasks()

    setTimeout(() => {
      router.push(`/tasks/${response.data.taskId}`)
    }, 1500)
  } catch (error) {
    ElMessage.error('生成失败，请重试')
  } finally {
    generating.value = false
  }
}

const loadStats = async () => {
  try {
    const response = await getTaskStatistics(statsPeriod.value)
    if (response) {
      stats.value = response
    }
  } catch (error) {
    console.error('加载统计数据失败', error)
  }
}

const loadRecentTasks = async () => {
  try {
    const response = await getTaskList({ current: 1, size: 10 })
    if (response && response.records && Array.isArray(response.records)) {
      recentTasks.value = response.records.map((task: Task) => ({
        ...task,
        progress: task.taskStatus === 1 ? Math.floor(Math.random() * 60) + 20 : 100,
        remainingTime: task.taskStatus === 1 ? `${Math.floor(Math.random() * 2) + 1}分${Math.floor(Math.random() * 60)}秒` : '0秒'
      }))
    }
  } catch (error) {
    console.error('加载任务列表失败', error)
  }
}

const formatDuration = (seconds: number) => {
  if (!seconds) return ''
  const hours = Math.floor(seconds / 3600)
  const minutes = Math.floor((seconds % 3600) / 60)
  const secs = Math.floor(seconds % 60)
  
  if (hours > 0) {
    return `${hours}小时${minutes}分${secs}秒`
  } else if (minutes > 0) {
    return `${minutes}分${secs}秒`
  } else {
    return `${secs}秒`
  }
}

const handleDownload = async (task: Task) => {
  try {
    const blob = await downloadTestCase(task.id)
    const url = window.URL.createObjectURL(blob)
    const link = document.createElement('a')
    link.href = url
    link.download = `${task.taskName}.md`
    document.body.appendChild(link)
    link.click()
    document.body.removeChild(link)
    window.URL.revokeObjectURL(url)
    ElMessage.success('下载成功')
  } catch (error) {
    ElMessage.error('下载失败')
  }
}

const goToTasks = () => {
  router.push('/task-management')
}

const goToTaskDetail = async (id: number) => {
  try {
    const response = await getTaskDetail(id)
    currentDetailTask.value = response
    detailDialogVisible.value = true
  } catch (error) {
    ElMessage.error('获取任务详情失败')
  }
}

const getStatusType = (status: number) => {
  const types: Record<number, any> = {
    0: 'info',
    1: 'primary',
    2: 'success',
    3: 'danger',
    4: 'warning'
  }
  return types[status] || 'info'
}

const getStatusText = (status: number) => {
  const texts: Record<number, string> = {
    0: '待处理',
    1: '生成中',
    2: '已完成',
    3: '生成失败',
    4: '已评价'
  }
  return texts[status] || '未知'
}

const getTaskIcon = (status: number) => {
  const icons: Record<number, string> = {
    0: '📋',
    1: '🔄',
    2: '✅',
    3: '❌'
  }
  return icons[status] || '📋'
}

const getTaskIconClass = (status: number) => {
  const classes: Record<number, string> = {
    0: 'icon-pending',
    1: 'icon-processing',
    2: 'icon-success',
    3: 'icon-error'
  }
  return classes[status] || 'icon-pending'
}

const formatTime = (dateStr: string) => {
  if (!dateStr) return ''
  const date = new Date(dateStr)
  const now = new Date()
  const diff = now.getTime() - date.getTime()
  const minutes = Math.floor(diff / 60000)
  const hours = Math.floor(diff / 3600000)
  const days = Math.floor(diff / 86400000)
  
  if (minutes < 1) return '刚刚'
  if (minutes < 60) return `${minutes}分钟前`
  if (hours < 24) return `${hours}小时前`
  if (days < 7) return `${days}天前`
  
  return date.toLocaleDateString('zh-CN', { 
    month: 'short', 
    day: 'numeric',
    hour: '2-digit',
    minute: '2-digit'
  })
}

onMounted(() => {
  loadStats()
  loadRecentTasks()
  handlePlatformChange(aiConfig.value.platform)
})

// 监听统计周期变化
watch(statsPeriod, () => {
  loadStats()
})
</script>

<style scoped lang="scss">
.document-generate-page {
  min-height: 100vh;
  background: linear-gradient(135deg, #f0f4f8 0%, #e8eef4 100%);
}

.page-header {
  background-color: #ffffff;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.05);
  padding: 0;
  position: sticky;
  top: 0;
  z-index: 100;

  .header-content {
    max-width: 1440px;
    margin: 0 auto;
    padding: 0 24px;
    display: flex;
    align-items: center;
    justify-content: space-between;
    height: 64px;
  }

  .logo-section {
    display: flex;
    align-items: center;
    gap: 12px;

    .logo-icon {
      width: 38px;
      height: 38px;
      background: linear-gradient(135deg, #6366f1 0%, #8b5cf6 100%);
      border-radius: 10px;
      display: flex;
      align-items: center;
      justify-content: center;
      color: #fff;
      font-size: 16px;
      font-weight: 700;
      box-shadow: 0 4px 12px rgba(99, 102, 241, 0.3);
    }

    .logo-text {
      font-size: 20px;
      font-weight: 700;
      color: #1e293b;
      letter-spacing: -0.5px;
    }
  }

  .nav-menu {
    border: none;
    background: transparent;

    .el-menu-item {
      margin: 0 4px;
      padding: 0 16px;
      font-size: 14px;
      color: #64748b;
      transition: all 0.2s;
      border-radius: 8px;

      &:hover {
        background-color: #f1f5f9;
        color: #334155;
      }

      &.is-active {
        color: #6366f1;
        font-weight: 500;
      }
    }
  }
}

.main-content {
  max-width: 1440px;
  margin: 20px auto;
  padding: 0 24px;
}

.page-title-section {
  margin-bottom: 20px;

  .page-title {
    font-size: 28px;
    font-weight: 700;
    color: #0f172a;
    margin: 0 0 8px 0;

    .title-icon {
      color: #6366f1;
      font-size: 28px;
    }
  }

  .page-subtitle {
    font-size: 14px;
    color: #64748b;
    margin: 0;
    line-height: 1.6;
  }
}

.main-row {
  display: flex;
  align-items: stretch;

  .main-col {
    display: flex;
    flex-direction: column;

    .main-card {
      flex: 1;
      min-height: 0;
    }

    @media (max-width: 768px) {
      margin-bottom: 20px;
    }
  }

  .side-col {
    display: flex;
    flex-direction: column;

    @media (max-width: 768px) {
      order: -1;
    }
  }
}

.main-card {
  border-radius: 16px;
  box-shadow: 0 4px 24px rgba(0, 0, 0, 0.06);
  border: none;
  overflow: hidden;

  .card-section {
    padding: 20px;

    &:not(:last-child) {
      padding-bottom: 0;
    }
  }

  .card-divider {
    height: 1px;
    background: linear-gradient(90deg, transparent, #e2e8f0, transparent);
    margin: 0 20px;
  }

  .section-header {
    display: flex;
    align-items: center;
    gap: 10px;
    margin-bottom: 18px;

    .section-icon {
      width: 28px;
      height: 28px;
      background: linear-gradient(135deg, #6366f1 0%, #8b5cf6 100%);
      border-radius: 8px;
      display: flex;
      align-items: center;
      justify-content: center;
      color: #fff;
      font-size: 14px;
    }

    .section-title {
      font-size: 15px;
      font-weight: 600;
      color: #1e293b;
    }
  }

  .form-section {
    margin-bottom: 20px;

    &:last-child {
      margin-bottom: 0;
    }

    .section-label {
      font-size: 13px;
      font-weight: 600;
      color: #334155;
      margin-bottom: 5px;

      .required {
        color: #ef4444;
        margin-left: 2px;
      }
    }

    .doc-type-hint {
      font-size: 12px;
      color: #94a3b8;
      margin-bottom: 10px;
    }

    .task-no-input {
      width: 100%;
      max-width: 400px;
    }

    .error-hint {
      font-size: 12px;
      color: #ef4444;
      margin-top: 6px;
    }
  }

  

  .upload-area {
    border: 2px dashed #cbd5e1;
    border-radius: 10px;
    padding: 20px 16px;
    text-align: center;
    transition: all 0.3s;
    background-color: #fafbfc;
    min-height: 100px;
    display: flex;
    flex-direction: column;
    align-items: center;
    justify-content: center;

    &:hover {
      border-color: #6366f1;
      background-color: #f5f5ff;
    }

    &.is-dragover {
      border-color: #6366f1;
      background-color: #f0f1ff;
    }

    :deep(.el-upload-list__item) {
      max-width: 100%;
    }

    :deep(.el-upload-list__item-name) {
      overflow: hidden;
      text-overflow: ellipsis;
      white-space: nowrap;
      max-width: 200px;
      flex-shrink: 0;
    }

    .upload-icon-wrapper {
      width: 40px;
      height: 40px;
      margin: 0 auto 10px;
      background: linear-gradient(135deg, #e0e7ff 0%, #c7d2fe 100%);
      border-radius: 10px;
      display: flex;
      align-items: center;
      justify-content: center;
      font-size: 20px;
      box-shadow: 0 4px 16px rgba(99, 102, 241, 0.12);
    }

    .upload-text {
      font-size: 13px;
      color: #334155;
      margin-bottom: 4px;

      em {
        color: #6366f1;
        font-style: normal;
        font-weight: 600;
      }
    }

    .upload-hint {
      font-size: 11px;
      color: #94a3b8;
      margin-bottom: 8px;
    }

    .upload-formats {
      display: flex;
      justify-content: center;
      gap: 8px;

      .format-tag {
        padding: 3px 10px;
        background: #f1f5f9;
        border-radius: 16px;
        font-size: 11px;
        color: #64748b;
        font-weight: 500;
      }
    }
  }

  .uploaded-docs-info {
    margin-top: 10px;
    padding: 8px 12px;
    background-color: #f0fdf4;
    border-radius: 8px;

    .docs-count {
      font-size: 13px;
      color: #059669;
      font-weight: 500;
    }
  }

  .combined-docs-summary {
    margin-top: 12px;
    padding-top: 16px;

    .summary-title {
      font-size: 13px;
      font-weight: 600;
      color: #334155;
    }

    .docs-list {
      margin-top: 10px;
      max-height: 200px;
      overflow-y: auto;
    }

    .doc-item {
      display: flex;
      align-items: center;
      gap: 10px;
      padding: 8px 12px;
      background-color: #f8fafc;
      border-radius: 8px;
      margin-bottom: 6px;

      &:last-child {
        margin-bottom: 0;
      }

      .doc-type-badge {
        padding: 3px 10px;
        border-radius: 4px;
        font-size: 11px;
        font-weight: 600;

        &.requirement {
          background-color: #dbeafe;
          color: #1d4ed8;
        }

        &.design {
          background-color: #fce7f3;
          color: #be185d;
        }
      }

      .doc-name {
        flex: 1;
        font-size: 13px;
        color: #334155;
        overflow: hidden;
        text-overflow: ellipsis;
        white-space: nowrap;
      }

      .doc-size {
        font-size: 12px;
        color: #94a3b8;
        flex-shrink: 0;
      }
    }
  }

  .config-form {
    .config-row {
      margin-bottom: 12px;
    }

    .config-col {
      .form-item {
        margin-bottom: 0;
      }
    }

    .config-col-full {
      .form-item {
        margin-bottom: 0;
      }
    }

    .error-hint {
      font-size: 12px;
      color: #ef4444;
      margin-top: 6px;
    }

    .required {
      color: #ef4444;
      margin-left: 2px;
    }

    .config-select {
      width: 100%;
      height: 36px;
      border-radius: 8px;
    }

    .config-select-full {
      width: 100%;
      height: 36px;
      border-radius: 8px;
    }

    .config-input {
      width: 100%;
      height: 36px;
      border-radius: 8px;
    }

    .config-input-full {
      width: 100%;
      height: 36px;
      border-radius: 8px;
    }
  }

  .action-buttons {
    display: flex;
    gap: 12px;
    padding: 20px;
    background-color: #fafbfc;
    border-top: 1px solid #f1f5f9;

    .primary-btn {
      flex: 1;
      height: 44px;
      border-radius: 10px;
      font-size: 14px;
      font-weight: 600;
      background: linear-gradient(135deg, #6366f1 0%, #8b5cf6 100%);
      border: none;
      box-shadow: 0 4px 16px rgba(99, 102, 241, 0.3);
      transition: all 0.2s;

      &:hover:not(:disabled) {
        transform: translateY(-1px);
        box-shadow: 0 6px 20px rgba(99, 102, 241, 0.4);
      }

      &:disabled {
        opacity: 0.5;
        box-shadow: none;
      }
    }

    .secondary-btn {
      padding: 0 28px;
      height: 44px;
      border-radius: 10px;
      font-size: 14px;
      font-weight: 500;
      background-color: #f1f5f9;
      border: 1px solid #e2e8f0;
      color: #64748b;
      transition: all 0.2s;

      &:hover {
        background-color: #e2e8f0;
        color: #334155;
      }
    }
  }
}

.right-panel {
  display: flex;
  flex-direction: column;
  gap: 16px;
  height: 100%;
  overflow-y: auto;
}

.stats-card {
  border-radius: 16px;
  border: none;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.05);
  overflow: hidden;

  .card-header-row {
    display: flex;
    justify-content: space-between;
    align-items: center;
    padding: 14px 16px;
    border-bottom: 1px solid #f1f5f9;

    .card-title {
      font-size: 14px;
      font-weight: 600;
      color: #334155;
    }

    .period-select {
      width: 85px;
      font-size: 12px;
    }
  }

  .stats-content {
    padding: 16px;

    .stat-item {
      display: flex;
      align-items: center;
      gap: 12px;
      padding: 6px 0;

      .stat-icon {
        width: 38px;
        height: 38px;
        border-radius: 10px;
        display: flex;
        align-items: center;
        justify-content: center;
        font-size: 18px;

        &.blue-icon {
          background: linear-gradient(135deg, #3b82f6 0%, #1d4ed8 100%);
        }

        &.green-icon {
          background: linear-gradient(135deg, #10b981 0%, #059669 100%);
        }

        &.purple-icon {
          background: linear-gradient(135deg, #8b5cf6 0%, #7c3aed 100%);
        }
      }

      .stat-info {
        flex: 1;

        .stat-number {
          font-size: 20px;
          font-weight: 700;
          color: #1e293b;
        }

        .stat-label {
          font-size: 12px;
          color: #94a3b8;
          margin-top: 2px;
        }
      }
    }

    .stat-divider {
      height: 1px;
      background-color: #f1f5f9;
      margin: 6px 0;
    }
  }
}

.recent-tasks-card {
  border-radius: 16px;
  border: none;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.05);
  overflow: hidden;

  .card-header-row {
    display: flex;
    justify-content: space-between;
    align-items: center;
    padding: 14px 16px;
    border-bottom: 1px solid #f1f5f9;

    .card-title {
      font-size: 14px;
      font-weight: 600;
      color: #334155;
    }

    .view-all-btn {
      font-size: 12px;
      color: #6366f1;
      padding: 0;
    }
  }

  .task-list {
    padding: 10px;
    max-height: 420px;
    overflow-y: auto;

    &::-webkit-scrollbar {
      width: 4px;
    }

    &::-webkit-scrollbar-thumb {
      background-color: #e2e8f0;
      border-radius: 4px;
    }

    .task-item {
      display: flex;
      justify-content: space-between;
      align-items: center;
      padding: 12px;
      border-radius: 8px;
      cursor: pointer;
      transition: all 0.2s;
      margin-bottom: 3px;

      &:hover {
        background-color: #f8fafc;
        transform: translateX(4px);
      }

      &:last-child {
        margin-bottom: 0;
      }

      .task-info {
        flex: 1;
        min-width: 0;

        .task-header {
          display: flex;
          align-items: center;
          justify-content: space-between;
          margin-bottom: 4px;

          .task-name {
            font-size: 13px;
            font-weight: 500;
            color: #334155;
            white-space: nowrap;
            overflow: hidden;
            text-overflow: ellipsis;
          }

          .status-tag {
            font-size: 11px;
            flex-shrink: 0;
            margin-left: 8px;
          }
        }

        .task-meta {
          font-size: 11px;
          color: #94a3b8;
          display: flex;
          align-items: center;
          gap: 4px;

          .meta-separator {
            color: #cbd5e1;
          }

          .meta-spacer {
            flex: 1;
          }

          .download-btn {
            padding: 0;
            font-size: 11px;
            color: #6366f1;
            flex-shrink: 0;

            &:hover {
              text-decoration: underline;
            }
          }
        }

        .task-progress {
          .mini-progress {
            margin-bottom: 4px;
          }

          .progress-text {
            font-size: 11px;
            color: #94a3b8;
            display: block;
          }
        }
      }
    }

    .empty-state {
      padding: 24px 0;
    }
  }
}

.tips-card {
  border-radius: 16px;
  border: none;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.05);
  background: linear-gradient(135deg, #f0fdf4 0%, #ecfdf5 100%);
  overflow: hidden;

  .card-header-row {
    display: flex;
    align-items: center;
    gap: 10px;
    padding: 18px 20px;
    border-bottom: 1px solid #d1fae5;

    .tip-icon {
      font-size: 18px;
    }

    .card-title {
      font-size: 15px;
      font-weight: 600;
      color: #334155;
    }
  }

  .tips-list {
    padding: 18px 20px;
    margin: 0;
    list-style: none;

    li {
      display: flex;
      gap: 10px;
      font-size: 13px;
      color: #475569;
      line-height: 1.6;
      margin-bottom: 14px;
      padding: 8px 0;

      &:last-child {
        margin-bottom: 0;
      }

      .tip-number {
        width: 22px;
        height: 22px;
        background: linear-gradient(135deg, #10b981 0%, #059669 100%);
        border-radius: 50%;
        display: flex;
        align-items: center;
        justify-content: center;
        font-size: 11px;
        font-weight: 600;
        color: #fff;
        flex-shrink: 0;
      }
    }
  }
}

.progress-dialog {
  border-radius: 16px;

  .progress-content {
    padding: 40px;
    text-align: center;

    .main-progress {
      margin-bottom: 20px;
    }

    .progress-status {
      font-size: 16px;
      font-weight: 600;
      color: #334155;
      margin-bottom: 10px;
    }

    .progress-tips {
      font-size: 13px;
      color: #94a3b8;
    }
  }
}

@media (max-width: 768px) {
  .main-content {
    margin: 16px auto;
    padding: 0 16px;
  }

  .page-title-section {
    margin-bottom: 20px;

    .page-title {
      font-size: 24px;
    }

    .page-subtitle {
      font-size: 14px;
    }
  }

  .main-card {
    .card-section {
      padding: 20px;
    }

    .card-divider {
      margin: 0 20px;
    }

    .action-buttons {
      padding: 20px;
      flex-direction: column;

      .primary-btn,
      .secondary-btn {
        width: 100%;
      }
    }
  }

  .right-panel {
    gap: 16px;
  }
}
</style>

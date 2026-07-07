<template>
  <div class="task-detail-page">
    <el-container>
      <el-header class="page-header">
        <div class="header-content">
          <el-button icon="ArrowLeft" link @click="goBack">返回任务列表</el-button>
          <h1 class="title">{{ task?.taskName || '任务详情' }}</h1>
          <div class="header-actions">
            <el-button @click="handleExport">导出用例</el-button>
            <el-button type="primary" @click="handleGenerateAgain" v-if="task?.taskStatus === 2">
              重新生成
            </el-button>
          </div>
        </div>
      </el-header>

      <el-main class="main-content">
        <div v-if="loading" class="loading-container">
          <el-icon class="is-loading"><Loading /></el-icon>
          <div>加载中...</div>
        </div>

        <div v-else>
          <el-row :gutter="24">
            <el-col :span="18">
              <el-card class="info-card">
                <template #header>
                  <div class="card-header">
                    <span>任务信息</span>
                    <el-tag :type="getStatusType(task.taskStatus)" size="large">
                      {{ getStatusText(task.taskStatus) }}
                    </el-tag>
                  </div>
                </template>

                <el-descriptions :column="2" border>
                  <el-descriptions-item label="任务ID">
                    {{ task.id }}
                  </el-descriptions-item>
                  <el-descriptions-item label="文档类型">
                    {{ task.documentType === 'REQUIREMENT' ? '需求文档' : '设计文档' }}
                  </el-descriptions-item>
                  <el-descriptions-item label="创建时间">
                    {{ formatTime(task.createTime) }}
                  </el-descriptions-item>
                  <el-descriptions-item label="开始时间">
                    {{ task.startTime ? formatTime(task.startTime) : '-' }}
                  </el-descriptions-item>
                  <el-descriptions-item label="完成时间">
                    {{ task.endTime ? formatTime(task.endTime) : '-' }}
                  </el-descriptions-item>
                  <el-descriptions-item label="耗时">
                    {{ task.duration ? `${task.duration}s` : '-' }}
                  </el-descriptions-item>
                  <el-descriptions-item label="生成用例数">
                    {{ task.resultCount || '-' }}
                  </el-descriptions-item>
                  <el-descriptions-item label="质量评分">
                    {{ task.qualityScore ? `${task.qualityScore.toFixed(1)}/10` : '-' }}
                  </el-descriptions-item>
                </el-descriptions>

                <div v-if="task.taskStatus === 2 && !hasSubmittedFeedback" class="feedback-section">
                  <el-divider>用户反馈</el-divider>
                  <el-form :model="feedbackForm" label-width="100px">
                    <el-form-item label="质量评分">
                      <el-rate v-model="feedbackForm.rating" :max="5" />
                    </el-form-item>
                    <el-form-item label="反馈内容">
                      <el-input
                        v-model="feedbackForm.feedback"
                        type="textarea"
                        :rows="3"
                        placeholder="请输入您的反馈意见"
                      />
                    </el-form-item>
                    <el-form-item>
                      <el-button type="primary" @click="handleSubmitFeedback" :loading="submittingFeedback">
                        提交反馈
                      </el-button>
                    </el-form-item>
                  </el-form>
                </div>
              </el-card>

              <el-card class="test-cases-card">
                <template #header>
                  <div class="card-header">
                    <span>测试用例 ({{ testCases.length }})</span>
                    <div class="header-tools">
                      <el-select v-model="filterPriority" placeholder="优先级" clearable style="width: 120px">
                        <el-option label="高" :value="1" />
                        <el-option label="中" :value="2" />
                        <el-option label="低" :value="3" />
                      </el-select>
                      <el-select v-model="filterType" placeholder="类型" clearable style="width: 140px">
                        <el-option label="功能测试" value="功能测试" />
                        <el-option label="边界测试" value="边界测试" />
                        <el-option label="异常测试" value="异常测试" />
                      </el-select>
                    </div>
                  </div>
                </template>

                <div class="test-cases-list">
                  <div v-for="testCase in filteredTestCases" :key="testCase.id" class="test-case-item">
                    <div class="test-case-header">
                      <div class="test-case-name">
                        <span class="name-label">用例名称：</span>
                        <span class="name-text">{{ testCase.caseName }}</span>
                      </div>
                      <div class="test-case-meta">
                        <el-tag :type="getPriorityType(testCase.priority)" size="small">
                          {{ getPriorityText(testCase.priority) }}
                        </el-tag>
                        <el-tag type="info" size="small">{{ testCase.caseType }}</el-tag>
                        <el-tag v-if="testCase.qualityScore" type="warning" size="small">
                          评分 {{ testCase.qualityScore.toFixed(1) }}
                        </el-tag>
                      </div>
                    </div>
                    <div class="test-case-body">
                      <el-collapse>
                        <el-collapse-item title="查看详情">
                          <div class="case-details">
                            <div class="detail-item">
                              <div class="detail-label">前置条件</div>
                              <div class="detail-content">{{ testCase.preConditions || '无' }}</div>
                            </div>
                            <div class="detail-item">
                              <div class="detail-label">操作步骤</div>
                              <div class="detail-content">
                                <pre>{{ testCase.steps }}</pre>
                              </div>
                            </div>
                            <div class="detail-item">
                              <div class="detail-label">预期结果</div>
                              <div class="detail-content">
                                <pre>{{ testCase.expectedResults }}</pre>
                              </div>
                            </div>
                          </div>
                        </el-collapse-item>
                      </el-collapse>
                    </div>
                  </div>
                  <el-empty v-if="filteredTestCases.length === 0" description="暂无测试用例" />
                </div>
              </el-card>
            </el-col>

            <el-col :span="6">
              <div class="right-panel">
                <el-card class="progress-card" v-if="task.taskStatus === 1">
                  <template #header>
                    <div class="card-header">
                      <span>生成进度</span>
                    </div>
                  </template>
                  <div class="progress-content">
                    <el-progress
                      :percentage="currentProgress"
                      :status="currentProgress === 100 ? 'success' : undefined"
                    />
                    <div class="progress-status">{{ progressStatus }}</div>
                  </div>
                </el-card>

                <el-card class="stats-card">
                  <template #header>
                    <div class="card-header">
                      <span>用例统计</span>
                    </div>
                  </template>
                  <div class="stats-list">
                    <div class="stat-row">
                      <span class="stat-label">总用例数</span>
                      <span class="stat-value">{{ testCases.length }}</span>
                    </div>
                    <div class="stat-row">
                      <span class="stat-label">功能测试</span>
                      <span class="stat-value">{{ getTypeCount('功能测试') }}</span>
                    </div>
                    <div class="stat-row">
                      <span class="stat-label">边界测试</span>
                      <span class="stat-value">{{ getTypeCount('边界测试') }}</span>
                    </div>
                    <div class="stat-row">
                      <span class="stat-label">异常测试</span>
                      <span class="stat-value">{{ getTypeCount('异常测试') }}</span>
                    </div>
                    <el-divider />
                    <div class="stat-row">
                      <span class="stat-label">高优先级</span>
                      <span class="stat-value">{{ getPriorityCount(1) }}</span>
                    </div>
                    <div class="stat-row">
                      <span class="stat-label">中优先级</span>
                      <span class="stat-value">{{ getPriorityCount(2) }}</span>
                    </div>
                    <div class="stat-row">
                      <span class="stat-label">低优先级</span>
                      <span class="stat-value">{{ getPriorityCount(3) }}</span>
                    </div>
                  </div>
                </el-card>

                <el-card class="actions-card">
                  <template #header>
                    <div class="card-header">
                      <span>操作</span>
                    </div>
                  </template>
                  <div class="actions-list">
                    <el-button type="primary" style="width: 100%; margin-bottom: 12px" @click="handleExport">
                      导出全部
                    </el-button>
                    <el-button style="width: 100%; margin-bottom: 12px" @click="handleCopy">
                      复制用例
                    </el-button>
                    <el-button style="width: 100%" @click="handleGenerateAgain" v-if="task.taskStatus === 2">
                      重新生成
                    </el-button>
                  </div>
                </el-card>
              </div>
            </el-col>
          </el-row>
        </div>
      </el-main>
    </el-container>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, onUnmounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ArrowLeft, Loading } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import { getTask, submitTaskFeedback, type Task } from '@/api/task'
import { getTestCasesByTaskId, type TestCase } from '@/api/testCase'

const router = useRouter()
const route = useRoute()

const loading = ref(true)
const submittingFeedback = ref(false)
const task = ref<Task | null>(null)
const testCases = ref<TestCase[]>([])
const filterPriority = ref<number | undefined>()
const filterType = ref<string>()
const currentProgress = ref(0)
const progressStatus = ref('正在解析文档...')
let pollTimer: number | null = null

const feedbackForm = ref({
  rating: 0,
  feedback: ''
})

const hasSubmittedFeedback = computed(() => {
  return task.value?.feedback !== null
})

const filteredTestCases = computed(() => {
  return testCases.value.filter(testCase => {
    const matchPriority = filterPriority.value === undefined || testCase.priority === filterPriority.value
    const matchType = filterType.value === undefined || testCase.caseType === filterType.value
    return matchPriority && matchType
  })
})

const goBack = () => {
  router.push('/tasks')
}

const loadTask = async () => {
  loading.value = true
  try {
    const taskId = Number(route.params.id)
    const [taskResponse, casesResponse] = await Promise.all([
      getTask(taskId),
      getTestCasesByTaskId(taskId)
    ])
    task.value = taskResponse.data
    testCases.value = casesResponse.data
  } catch (error) {
    ElMessage.error('加载任务详情失败')
  } finally {
    loading.value = false
  }
}

const startPolling = () => {
  if (task.value?.taskStatus !== 1) return

  pollTimer = window.setInterval(async () => {
    try {
      const taskId = Number(route.params.id)
      const [taskResponse, casesResponse] = await Promise.all([
        getTask(taskId),
        getTestCasesByTaskId(taskId)
      ])
      task.value = taskResponse.data
      testCases.value = casesResponse.data

      currentProgress.value = Math.min(currentProgress.value + 10, 90)
      progressStatus.value = '正在生成测试用例...'

      if (task.value.taskStatus !== 1) {
        if (pollTimer) clearInterval(pollTimer)
        currentProgress.value = 100
        progressStatus.value = '调用大模型成功'
      }
    } catch (error) {
      console.error('轮询失败', error)
    }
  }, 3000)
}

const handleSubmitFeedback = async () => {
  if (!feedbackForm.value.rating) {
    ElMessage.warning('请选择评分')
    return
  }

  submittingFeedback.value = true
  try {
    const taskId = Number(route.params.id)
    await submitTaskFeedback(taskId, feedbackForm.value)
    ElMessage.success('反馈提交成功')
    await loadTask()
  } catch (error) {
    ElMessage.error('提交失败')
  } finally {
    submittingFeedback.value = false
  }
}

const handleExport = () => {
  ElMessage.info('导出功能开发中...')
}

const handleCopy = () => {
  ElMessage.info('复制功能开发中...')
}

const handleGenerateAgain = () => {
  router.push('/generate')
}

const getTypeCount = (type: string) => {
  return testCases.value.filter(tc => tc.caseType === type).length
}

const getPriorityCount = (priority: number) => {
  return testCases.value.filter(tc => tc.priority === priority).length
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

const getPriorityType = (priority: number) => {
  const types: Record<number, any> = {
    1: 'danger',
    2: 'warning',
    3: 'info'
  }
  return types[priority] || 'info'
}

const getPriorityText = (priority: number) => {
  const texts: Record<number, string> = {
    1: '高',
    2: '中',
    3: '低'
  }
  return texts[priority] || '未知'
}

onMounted(async () => {
  await loadTask()
  startPolling()
})

onUnmounted(() => {
  if (pollTimer) clearInterval(pollTimer)
})
</script>

<style scoped lang="scss">
.task-detail-page {
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
    gap: 20px;
    height: 64px;

    .title {
      flex: 1;
      font-size: 18px;
      font-weight: 600;
      color: #303133;
      margin: 0;
      text-overflow: ellipsis;
      overflow: hidden;
      white-space: nowrap;
    }

    .header-actions {
      display: flex;
      gap: 12px;
    }
  }
}

.main-content {
  max-width: 1400px;
  margin: 24px auto;
  padding: 0 20px;
}

.loading-container {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  height: 400px;
  gap: 16px;
  color: #909399;
}

.info-card,
.test-cases-card,
.progress-card,
.stats-card,
.actions-card {
  margin-bottom: 20px;

  .card-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    font-weight: 600;
  }
}

.info-card {
  .feedback-section {
    margin-top: 20px;
  }
}

.test-cases-card {
  .header-tools {
    display: flex;
    gap: 12px;
  }
}

.test-cases-list {
  .test-case-item {
    border: 1px solid #e4e7ed;
    border-radius: 8px;
    margin-bottom: 16px;
    overflow: hidden;

    &:last-child {
      margin-bottom: 0;
    }

    .test-case-header {
      padding: 16px;
      background-color: #fafafa;
      display: flex;
      justify-content: space-between;
      align-items: center;

      .test-case-name {
        flex: 1;
        overflow: hidden;

        .name-label {
          font-size: 14px;
          color: #909399;
        }

        .name-text {
          font-size: 14px;
          font-weight: 600;
          color: #303133;
        }
      }

      .test-case-meta {
        display: flex;
        gap: 8px;
        flex-shrink: 0;
      }
    }

    .test-case-body {
      padding: 0 16px;
    }
  }
}

.case-details {
  padding: 16px 0;

  .detail-item {
    margin-bottom: 16px;

    &:last-child {
      margin-bottom: 0;
    }

    .detail-label {
      font-size: 13px;
      font-weight: 600;
      color: #606266;
      margin-bottom: 8px;
    }

    .detail-content {
      font-size: 14px;
      color: #303133;
      line-height: 1.8;
      white-space: pre-wrap;
      word-break: break-word;

      pre {
        margin: 0;
        font-family: inherit;
        white-space: pre-wrap;
        word-break: break-word;
      }
    }
  }
}

.right-panel {
  display: flex;
  flex-direction: column;
}

.progress-card {
  .progress-content {
    padding: 12px 0;
    text-align: center;

    .progress-status {
      margin-top: 12px;
      font-size: 13px;
      color: #606266;
    }
  }
}

.stats-card {
  .stats-list {
    .stat-row {
      display: flex;
      justify-content: space-between;
      padding: 8px 0;
      font-size: 14px;

      .stat-label {
        color: #606266;
      }

      .stat-value {
        font-weight: 600;
        color: #303133;
      }
    }
  }
}

.actions-card {
  .actions-list {
    padding: 8px 0;
  }
}
</style>

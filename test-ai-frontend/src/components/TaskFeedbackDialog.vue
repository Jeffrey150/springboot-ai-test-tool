<template>
  <el-dialog
    v-model="dialogVisible"
    title="评价测试用例生成结果"
    width="600px"
    :close-on-click-modal="false"
    @close="handleClose"
  >
    <div class="feedback-content">
      <p class="feedback-description">
        您的评价将帮助我们不断优化生成效果，感谢您的反馈！
      </p>

      <!-- 任务信息 -->
      <el-card class="task-info-card" v-if="currentTask">
        <div class="task-info">
          <el-icon class="task-icon"><Document /></el-icon>
          <div class="task-details">
            <div class="task-name">{{ currentTask.taskName }}</div>
            <div class="task-meta">
              生成时间：{{ formatDateTime(currentTask.createTime) }} · 
              用例数量：{{ currentTask.resultCount }}条
            </div>
          </div>
        </div>
      </el-card>

      <el-form :model="feedbackForm" ref="formRef" class="feedback-form">
        <!-- 用例整体质量 -->
        <el-form-item label="用例整体质量" required>
          <div class="rating-section">
            <div class="rating-score">{{ feedbackForm.qualityScore }}分</div>
            <div class="star-rating">
              <el-icon
                v-for="star in 5"
                :key="star"
                class="star"
                :class="{ active: star <= feedbackForm.qualityScore }"
                @click="handleRate(star)"
              >
                <Star filled />
              </el-icon>
            </div>
            <div class="rating-labels">
              <span>1分 很差</span>
              <span>3分 一般</span>
              <span>5分 很好</span>
            </div>
          </div>
        </el-form-item>

        <!-- 用例采纳率 -->
        <el-form-item label="用例采纳率" required>
          <div class="adoption-section">
            <div class="adoption-score">{{ feedbackForm.adoptionRate }}%</div>
            <el-slider
              v-model="feedbackForm.adoptionRate"
              :min="0"
              :max="100"
              :step="1"
              class="adoption-slider"
            />
            <div class="adoption-labels">
              <span>0%</span>
              <span>50%</span>
              <span>100%</span>
            </div>
          </div>
        </el-form-item>

        <!-- 补充建议 -->
        <el-form-item label="补充建议（选填）">
          <div class="suggestion-section">
            <div class="suggestion-header">
              <span>0/250 字</span>
            </div>
            <el-input
              v-model="feedbackForm.suggestion"
              type="textarea"
              :rows="4"
              placeholder="请分享您的使用感受或改进建议，比如：用例覆盖是否全面、描述是否清晰、是否符合业务预期等..."
              :maxlength="250"
              resize="none"
              class="suggestion-input"
            />
          </div>
        </el-form-item>
      </el-form>
    </div>

    <template #footer>
      <div class="dialog-footer">
        <el-button @click="handleClose">取消</el-button>
        <el-button type="primary" @click="handleSubmit" :loading="submitting">
          提交评价
        </el-button>
      </div>
    </template>
  </el-dialog>
</template>

<script setup lang="ts">
import { ref, reactive, watch } from 'vue'
import { ElMessage, type FormInstance } from 'element-plus'
import { Document, Star } from '@element-plus/icons-vue'
import { submitFeedback } from '@/api/feedback'

interface Task {
  id: number
  taskName: string
  createTime: string
  resultCount: number
  [key: string]: any
}

interface FeedbackForm {
  qualityScore: number
  adoptionRate: number
  suggestion: string
}

const props = defineProps<{
  modelValue: boolean
  task: Task | null
}>()

const emit = defineEmits<{
  (e: 'update:modelValue', value: boolean): void
  (e: 'submit-success'): void
}>()

const dialogVisible = ref(false)
const submitting = ref(false)
const formRef = ref<FormInstance>()

const feedbackForm = reactive<FeedbackForm>({
  qualityScore: 0,
  adoptionRate: 50,
  suggestion: ''
})

const currentTask = ref<Task | null>(null)

watch(() => props.modelValue, (val) => {
  dialogVisible.value = val
  if (val && props.task) {
    currentTask.value = props.task
    // 重置表单
    feedbackForm.qualityScore = 0
    feedbackForm.adoptionRate = 50
    feedbackForm.suggestion = ''
  }
})

watch(dialogVisible, (val) => {
  emit('update:modelValue', val)
})

/**
 * 评分
 */
const handleRate = (score: number) => {
  feedbackForm.qualityScore = score
}

/**
 * 关闭弹窗
 */
const handleClose = () => {
  dialogVisible.value = false
  formRef.value?.resetFields()
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
 * 提交评价
 */
const handleSubmit = async () => {
  if (!currentTask.value) {
    ElMessage.error('任务信息为空')
    return
  }

  if (feedbackForm.qualityScore === 0) {
    ElMessage.warning('请选择用例整体质量评分')
    return
  }

  submitting.value = true
  try {
    await submitFeedback({
      taskId: currentTask.value.id,
      qualityScore: feedbackForm.qualityScore,
      adoptionRate: feedbackForm.adoptionRate,
      suggestion: feedbackForm.suggestion || undefined
    })
    
    ElMessage.success('评价提交成功')
    emit('submit-success')
    handleClose()
  } catch (error: any) {
    console.error('提交评价失败', error)
    ElMessage.error(error.message || '提交评价失败，请重试')
  } finally {
    submitting.value = false
  }
}
</script>

<style scoped lang="scss">
.feedback-content {
  padding: 10px 0;

  .feedback-description {
    font-size: 14px;
    color: #6b7280;
    margin-bottom: 20px;
  }

  .task-info-card {
    margin-bottom: 24px;
    border-radius: 8px;
    background: #f9fafb;

    .task-info {
      display: flex;
      align-items: center;
      gap: 12px;

      .task-icon {
        font-size: 32px;
        color: #ef4444;
      }

      .task-details {
        flex: 1;

        .task-name {
          font-weight: 500;
          color: #1f2937;
          margin-bottom: 4px;
        }

        .task-meta {
          font-size: 13px;
          color: #6b7280;
        }
      }
    }
  }

  .feedback-form {
    .rating-section {
      width: 100%;

      .rating-score {
        font-size: 18px;
        font-weight: 600;
        color: #f97316;
        margin-bottom: 12px;
      }

      .star-rating {
        display: flex;
        gap: 8px;
        margin-bottom: 8px;

        .star {
          font-size: 32px;
          color: #e5e7eb;
          cursor: pointer;
          transition: all 0.2s;

          &:hover {
            transform: scale(1.1);
          }

          &.active {
            color: #f97316;
          }
        }
      }

      .rating-labels {
        display: flex;
        justify-content: space-between;
        font-size: 12px;
        color: #9ca3af;
      }
    }

    .adoption-section {
      width: 100%;

      .adoption-score {
        font-size: 18px;
        font-weight: 600;
        color: #f97316;
        margin-bottom: 12px;
      }

      .adoption-slider {
        margin-bottom: 8px;
      }

      .adoption-labels {
        display: flex;
        justify-content: space-between;
        font-size: 12px;
        color: #9ca3af;
      }
    }

    .suggestion-section {
      width: 100%;

      .suggestion-header {
        display: flex;
        justify-content: flex-end;
        margin-bottom: 8px;
        font-size: 12px;
        color: #9ca3af;
      }

      .suggestion-input {
        :deep(textarea) {
          resize: none;
        }
      }
    }
  }
}

.dialog-footer {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
}
</style>

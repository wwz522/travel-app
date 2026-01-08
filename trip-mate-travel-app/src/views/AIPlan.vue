<template>
  <div class="ai-plan-page">
    <div class="plan-container">
      <h1 class="page-title">
        <i class="el-icon-magic-stick"></i>
        AI智能路线规划
      </h1>
      <p class="page-subtitle">输入您的需求，AI将为您生成个性化旅行路线</p>

      <el-card class="plan-form-card">
        <el-form ref="planForm" :model="planForm" :rules="rules" label-width="120px">
          <el-form-item label="出发地" prop="origin">
            <el-input
              v-model="planForm.origin"
              placeholder="例如：北京、上海、广州（可选，填写后可计算往返交通费）"
              clearable
            />
          </el-form-item>

          <el-form-item label="目的地" prop="destination">
            <el-input
              v-model="planForm.destination"
              placeholder="例如：杭州、北京、成都"
              clearable
            />
          </el-form-item>

          <el-form-item label="行程天数" prop="days">
            <el-input-number
              v-model="planForm.days"
              :min="1"
              :max="30"
              placeholder="请输入天数"
            />
          </el-form-item>

          <el-form-item label="预算范围" prop="budget">
            <el-input
              v-model="planForm.budget"
              placeholder="例如：1000-2000"
              clearable
            >
              <template slot="prepend">￥</template>
            </el-input>
          </el-form-item>

          <el-form-item label="交通方式">
            <el-select v-model="planForm.travelMode" placeholder="请选择" clearable>
              <el-option label="高铁" value="high-speed-rail" />
              <el-option label="普通火车" value="train" />
              <el-option label="飞机" value="flight" />
              <el-option label="自驾" value="self-drive" />
              <el-option label="大巴" value="bus" />
            </el-select>
          </el-form-item>

          <el-form-item label="住宿档次">
            <el-select v-model="planForm.stayLevel" placeholder="请选择" clearable>
              <el-option label="经济型" value="budget" />
              <el-option label="舒适型" value="comfort" />
              <el-option label="豪华型" value="luxury" />
            </el-select>
          </el-form-item>

          <el-form-item label="旅行偏好">
            <el-input
              v-model="planForm.preferences"
              type="textarea"
              :rows="3"
              placeholder="例如：美食、文化、自然风光、购物等"
            />
          </el-form-item>

          <el-form-item>
            <el-button
              type="primary"
              :loading="loading"
              @click="handleGeneratePlan"
              size="large"
            >
              <i class="el-icon-magic-stick"></i>
              {{ loading ? 'AI正在规划中...' : '生成智能路线' }}
            </el-button>
            <el-button @click="handleReset" size="large">重置</el-button>
          </el-form-item>
        </el-form>
      </el-card>

      <!-- 规划结果 -->
      <el-card v-if="planResult" class="plan-result-card">
        <div slot="header" class="result-header">
          <span class="result-title">
            <i class="el-icon-document"></i>
            {{ planResult.title }}
          </span>
        </div>

        <div class="result-content">
          <div class="result-section">
            <h3><i class="el-icon-info"></i> 路线描述</h3>
            <p>{{ planResult.description }}</p>
          </div>

          <div class="result-section">
            <h3><i class="el-icon-map-location"></i> 详细行程</h3>
            <div v-if="planResult.itinerary && planResult.itinerary.trim()" class="itinerary-content" v-html="formatItinerary(planResult.itinerary)"></div>
            <div v-else class="itinerary-empty">
              <el-empty description="详细行程生成中，请稍候..." :image-size="100" />
            </div>
          </div>

          <div class="result-section">
            <h3><i class="el-icon-warning"></i> 出行提示</h3>
            <ul class="tips-list">
              <li v-for="(tip, index) in parseTips(planResult.tips)" :key="index">
                {{ tip }}
              </li>
            </ul>
          </div>

          <div class="result-section">
            <h3><i class="el-icon-money"></i> 预算分解</h3>
            <p>{{ planResult.budgetBreakdown }}</p>
          </div>

          <div class="result-actions">
            <el-button type="primary" @click="handleSaveAsRoute">
              <i class="el-icon-check"></i>
              保存为路线
            </el-button>
            <el-button @click="handleRegenerate">
              <i class="el-icon-refresh"></i>
              重新生成
            </el-button>
          </div>
        </div>
      </el-card>
    </div>
  </div>
</template>

<script>
import { planRoute, createRoute } from '@/api'

export default {
  name: 'AIPlan',
  data() {
    return {
      planForm: {
        origin: '',
        destination: '',
        days: 3,
        budget: '',
        travelMode: '',
        stayLevel: '',
        preferences: ''
      },
      rules: {
        destination: [
          { required: true, message: '请输入目的地', trigger: 'blur' }
        ],
        days: [
          { required: true, message: '请输入行程天数', trigger: 'blur' }
        ],
        budget: [
          { required: true, message: '请输入预算范围', trigger: 'blur' }
        ]
      },
      loading: false,
      planResult: null
    }
  },
  methods: {
    async handleGeneratePlan() {
      this.$refs.planForm.validate(async (valid) => {
        if (!valid) {
          return
        }

        if (!this.$store.getters.isLoggedIn) {
          this.$message.warning('请先登录')
          this.$router.push('/login')
          return
        }

        this.loading = true
        try {
          const response = await planRoute(this.planForm)
          this.planResult = response.data
          this.$message.success('路线规划成功！')
        } catch (error) {
          console.error('规划失败:', error)
          this.$message.error(error.response?.data?.message || '路线规划失败，请稍后重试')
        } finally {
          this.loading = false
        }
      })
    },
    handleReset() {
      this.$refs.planForm.resetFields()
      this.planResult = null
    },
    handleRegenerate() {
      this.planResult = null
      this.handleGeneratePlan()
    },
    async handleSaveAsRoute() {
      if (!this.planResult) {
        this.$message.warning('请先生成路线规划')
        return
      }

      try {
        // 从规划结果和表单中提取数据
        const routeData = {
          title: this.planResult.title,
          description: this.planResult.description,
          duration: this.planForm.days,
          budget: this.planForm.budget,
          tips: this.planResult.tips,
          tags: this.extractTags(),
          isDraft: true, // 保存为草稿
          cover: '' // 可以后续添加封面
        }

        // 将详细行程和预算信息合并到描述中
        const fullDescription = `${routeData.description}\n\n## 详细行程\n${this.planResult.itinerary}\n\n## 预算分解\n${this.planResult.budgetBreakdown}`
        routeData.description = fullDescription

        await createRoute(routeData)
        this.$message.success('已保存为草稿，可在个人中心继续编辑')
        this.$router.push('/profile')
      } catch (error) {
        console.error('保存失败:', error)
        this.$message.error(error.response?.data?.message || '保存失败，请稍后重试')
      }
    },
    extractTags() {
      const tags = []
      if (this.planForm.destination) {
        tags.push(this.planForm.destination)
      }
      if (this.planForm.preferences) {
        const prefs = this.planForm.preferences.split(/[,，\s]+/).filter(p => p.trim())
        tags.push(...prefs.slice(0, 3)) // 最多取3个偏好作为标签
      }
      return tags
    },
    formatItinerary(itinerary) {
      if (!itinerary) return ''
      // 将换行符转换为HTML换行，并保留格式
      let formatted = itinerary
        .replace(/\n/g, '<br>')
        .replace(/【([^】]+)】/g, '<strong style="color: #409eff; font-size: 16px; display: block; margin-top: 16px;">【$1】</strong>')
        .replace(/- ([^<]+)/g, '<div style="margin-left: 20px; margin-top: 8px;">• $1</div>')
        .replace(/\* ([^<]+)/g, '<div style="margin-left: 40px; margin-top: 4px; color: #606266;">○ $1</div>')
      return formatted
    },
    parseTips(tips) {
      if (!tips) return []
      // 按逗号或换行符分割
      return tips.split(/[,，\n]/).filter(tip => tip.trim())
    }
  }
}
</script>

<style scoped>
.ai-plan-page {
  min-height: calc(100vh - 200px);
  padding: 24px;
  background: var(--bg-color, #f5f7fa);
}

.plan-container {
  max-width: 1000px;
  margin: 0 auto;
}

.page-title {
  font-size: 32px;
  font-weight: 600;
  color: var(--text-primary, #303133);
  margin-bottom: 8px;
  display: flex;
  align-items: center;
  gap: 12px;
}

.page-subtitle {
  font-size: 16px;
  color: var(--text-secondary, #909399);
  margin-bottom: 32px;
}

.plan-form-card {
  margin-bottom: 24px;
}

.plan-result-card {
  margin-top: 24px;
}

.result-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.result-title {
  font-size: 20px;
  font-weight: 600;
  color: var(--text-primary, #303133);
  display: flex;
  align-items: center;
  gap: 8px;
}

.result-content {
  padding: 16px 0;
}

.result-section {
  margin-bottom: 32px;
}

.result-section:last-of-type {
  margin-bottom: 0;
}

.result-section h3 {
  font-size: 18px;
  font-weight: 600;
  color: var(--text-primary, #303133);
  margin-bottom: 12px;
  display: flex;
  align-items: center;
  gap: 8px;
}

.result-section p {
  font-size: 15px;
  line-height: 1.8;
  color: var(--text-regular, #606266);
}

.itinerary-content {
  font-size: 15px;
  line-height: 1.8;
  color: var(--text-regular, #606266);
  background: var(--bg-color, #f5f7fa);
  padding: 16px;
  border-radius: 8px;
  white-space: pre-wrap;
}

.tips-list {
  list-style: none;
  padding: 0;
  margin: 0;
}

.tips-list li {
  font-size: 15px;
  line-height: 1.8;
  color: var(--text-regular, #606266);
  padding: 8px 0;
  padding-left: 24px;
  position: relative;
}

.tips-list li::before {
  content: '•';
  position: absolute;
  left: 8px;
  color: var(--primary-color, #409eff);
  font-weight: bold;
}

.result-actions {
  margin-top: 32px;
  padding-top: 24px;
  border-top: 1px solid var(--border-color, #ebeef5);
  display: flex;
  gap: 12px;
}
</style>


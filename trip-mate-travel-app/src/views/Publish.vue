<template>
  <div class="publish-page">
    <div class="publish-container">
      <h1 class="page-title">{{ $route.query.mode === 'edit' ? '编辑内容' : '发布内容' }}</h1>

      <el-form ref="form" :model="form" :rules="rules" label-width="100px">
        <el-form-item label="内容类型" prop="type">
          <el-radio-group v-model="form.type">
            <el-radio label="route">旅游线路</el-radio>
            <el-radio label="attraction">景点点评</el-radio>
          </el-radio-group>
        </el-form-item>

        <el-form-item label="标题" prop="title">
          <el-input
            v-model="form.title"
            placeholder="请输入标题"
            maxlength="50"
            show-word-limit
          />
        </el-form-item>

        <el-form-item label="封面图片" prop="cover">
          <el-upload
            class="cover-uploader"
            action="#"
            :show-file-list="false"
            :before-upload="handleCoverUpload"
          >
            <img v-if="form.cover" :src="form.cover" class="cover-preview" />
            <i v-else class="el-icon-plus cover-uploader-icon"></i>
          </el-upload>
        </el-form-item>

        <el-form-item v-if="form.type === 'route'" label="行程天数">
          <el-input-number v-model="form.duration" :min="1" :max="30" />
        </el-form-item>

        <el-form-item v-if="form.type === 'route'" label="预算范围">
          <el-input v-model="form.budget" placeholder="例如：￥500-1000" />
        </el-form-item>

        <el-form-item v-if="form.type === 'attraction'" label="地点">
          <el-input v-model="form.location" placeholder="请输入景点位置" />
        </el-form-item>

        <el-form-item v-if="form.type === 'attraction'" label="评分">
          <el-rate v-model="form.rating" show-score />
        </el-form-item>

        <el-form-item label="详细描述" prop="description">
          <el-input
            v-model="form.description"
            type="textarea"
            :rows="6"
            placeholder="请输入详细描述..."
            maxlength="2000"
            show-word-limit
          />
        </el-form-item>

        <el-form-item v-if="form.type === 'route'" label="出行提示">
          <el-input
            v-model="form.tips"
            type="textarea"
            :rows="3"
            placeholder="例如：提前预订门票、携带身份证件等（用逗号分隔）"
            maxlength="500"
            show-word-limit
          />
        </el-form-item>

        <el-form-item label="标签">
          <el-tag
            v-for="tag in form.tags"
            :key="tag"
            closable
            @close="removeTag(tag)"
          >
            {{ tag }}
          </el-tag>
          <el-input
            v-if="tagInputVisible"
            ref="tagInput"
            v-model="tagInputValue"
            size="small"
            style="width: 100px; margin-left: 10px"
            @keyup.enter.native="addTag"
            @blur="addTag"
          />
          <el-button
            v-else
            size="small"
            style="margin-left: 10px"
            @click="showTagInput"
          >
            + 添加标签
          </el-button>
        </el-form-item>

        <el-form-item>
          <el-button type="primary" @click="submitForm">{{ $route.query.mode === 'edit' ? '更新' : '发布' }}</el-button>
          <el-button @click="resetForm">重置</el-button>
        </el-form-item>
      </el-form>
    </div>
  </div>
</template>

<script>
import { createRoute, createAttraction, uploadFile, getRouteDetail, getAttractionDetail, updateRoute, updateAttraction } from '@/api'

export default {
  name: 'Publish',
  data() {
    return {
      form: {
        type: 'route',
        title: '',
        cover: '',
        duration: 1,
        budget: '',
        location: '',
        rating: 0,
        description: '',
        tags: [],
        tips: '',
        openTime: '',
        ticketPrice: '',
        suggestedDuration: ''
      },
      rules: {
        title: [
          { required: true, message: '请输入标题', trigger: 'blur' }
        ],
        description: [
          { required: true, message: '请输入描述', trigger: 'blur' }
        ]
      },
      tagInputVisible: false,
      tagInputValue: '',
      uploading: false
    }
  },
  mounted() {
    // 如果是编辑模式，加载已有数据
    if (this.$route.query.id && this.$route.query.mode === 'edit') {
      this.loadContentForEdit()
    }
  },
  methods: {
    async handleCoverUpload(file) {
      const isImage = file.type.startsWith('image/')
      const isLt2M = file.size / 1024 / 1024 < 2

      if (!isImage) {
        this.$message.error('只能上传图片文件！')
        return false
      }
      if (!isLt2M) {
        this.$message.error('图片大小不能超过 2MB！')
        return false
      }

      this.uploading = true
      try {
        const res = await uploadFile(file)
        this.form.cover = res.data.url
        this.$message.success('图片上传成功')
      } catch (error) {
        console.error('图片上传失败:', error)
        this.$message.error('图片上传失败')
      } finally {
        this.uploading = false
      }
      return false
    },
    showTagInput() {
      this.tagInputVisible = true
      this.$nextTick(() => {
        this.$refs.tagInput.$refs.input.focus()
      })
    },
    addTag() {
      const value = this.tagInputValue.trim()
      if (value && !this.form.tags.includes(value)) {
        this.form.tags.push(value)
      }
      this.tagInputVisible = false
      this.tagInputValue = ''
    },
    removeTag(tag) {
      this.form.tags = this.form.tags.filter(t => t !== tag)
    },
    async loadContentForEdit() {
      try {
        const id = Number(this.$route.query.id)
        const type = this.$route.query.type || 'route'
        
        if (type === 'route') {
          const res = await getRouteDetail(id)
          const data = res.data
          this.form = {
            type: 'route',
            title: data.title || '',
            cover: data.cover || '',
            description: data.description || '',
            duration: data.duration || 1,
            budget: data.budget || '',
            tags: data.tags || [],
            tips: data.tips || ''
          }
        } else {
          const res = await getAttractionDetail(id)
          const data = res.data
          this.form = {
            type: 'attraction',
            title: data.title || '',
            cover: data.cover || '',
            description: data.description || '',
            location: data.location || '',
            rating: data.rating || 0,
            tags: data.tags || [],
            openTime: data.openTime || '',
            ticketPrice: data.ticketPrice || '',
            suggestedDuration: data.suggestedDuration || ''
          }
        }
      } catch (error) {
        console.error('加载内容失败:', error)
        this.$message.error('加载内容失败')
      }
    },
    async submitForm() {
      this.$refs.form.validate(async (valid) => {
        if (!valid) {
          return
        }

        try {
          const id = this.$route.query.id
          const isEdit = this.$route.query.mode === 'edit' && id

          if (this.form.type === 'route') {
            const routeData = {
              title: this.form.title,
              cover: this.form.cover,
              description: this.form.description,
              duration: this.form.duration,
              budget: this.form.budget,
              tags: Array.isArray(this.form.tags) ? this.form.tags : this.form.tags.split(',').filter(t => t.trim()),
              tips: this.form.tips || ''
            }

            if (isEdit) {
              await updateRoute(Number(id), routeData)
              this.$message.success('更新成功！')
            } else {
              await createRoute(routeData)
              this.$message.success('发布成功！')
            }
          } else {
            const attractionData = {
              title: this.form.title,
              cover: this.form.cover,
              description: this.form.description,
              location: this.form.location,
              rating: this.form.rating,
              tags: Array.isArray(this.form.tags) ? this.form.tags : this.form.tags.split(',').filter(t => t.trim())
            }
            
            if (isEdit) {
              await updateAttraction(Number(id), {
                ...attractionData,
                openTime: this.form.openTime || '',
                ticketPrice: this.form.ticketPrice || '',
                suggestedDuration: this.form.suggestedDuration || ''
              })
              this.$message.success('更新成功！')
            } else {
              await createAttraction(attractionData)
              this.$message.success('发布成功！')
            }
          }
          this.$router.push('/')
        } catch (error) {
          console.error('操作失败:', error)
          this.$message.error(error.response?.data?.message || '操作失败')
        }
      })
    },
    resetForm() {
      this.$refs.form.resetFields()
      this.form.tags = []
      this.form.cover = ''
    }
  }
}
</script>

<style lang="scss" scoped>
.publish-page {
  max-width: 800px;
  margin: 0 auto;
}

.publish-container {
  background: white;
  border-radius: var(--radius-md);
  padding: 32px;
  box-shadow: var(--shadow-sm);
}

.page-title {
  font-size: 28px;
  font-weight: 700;
  color: var(--color-text);
  margin-bottom: 32px;
}

.cover-uploader {
  .cover-preview {
    width: 360px;
    height: 200px;
    object-fit: cover;
    border-radius: var(--radius-sm);
  }

  .cover-uploader-icon {
    font-size: 28px;
    color: var(--color-text-light);
    width: 360px;
    height: 200px;
    line-height: 200px;
    text-align: center;
    border: 2px dashed var(--color-border);
    border-radius: var(--radius-sm);
    cursor: pointer;
    transition: all 0.3s;

    &:hover {
      border-color: var(--color-primary);
      color: var(--color-primary);
    }
  }
}
</style>

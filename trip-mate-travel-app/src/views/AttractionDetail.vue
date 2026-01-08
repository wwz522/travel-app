<template>
  <div v-loading="loading" class="attraction-detail">
    <div class="detail-header">
      <el-breadcrumb separator="/">
        <el-breadcrumb-item :to="{ path: '/' }">首页</el-breadcrumb-item>
        <el-breadcrumb-item>景点详情</el-breadcrumb-item>
      </el-breadcrumb>
    </div>

    <div class="detail-content">
      <div class="main-section">
        <div class="attraction-header">
          <div class="header-left">
            <h1 class="attraction-title">{{ attraction.title }}</h1>
            <div class="rating-section">
              <el-rate
                v-model="attraction.rating"
                disabled
                show-score
                text-color="#ff9900"
              />
              <span class="review-count">({{ attraction.reviewCount }} 条点评)</span>
            </div>
            <div class="location">
              <i class="el-icon-location"></i>
              {{ attraction.location }}
            </div>
          </div>
          <div class="header-actions">
            <el-button
              v-if="isAuthor"
              type="primary"
              icon="el-icon-edit"
              @click="handleEdit"
            >
              编辑
            </el-button>
            <el-button
              v-if="isAuthor"
              type="danger"
              icon="el-icon-delete"
              @click="handleDelete"
            >
              删除
            </el-button>
            <el-button
              :type="attraction.isFavorited ? 'warning' : 'default'"
              icon="el-icon-star-off"
              @click="handleFavorite"
            >
              {{ attraction.isFavorited ? '已收藏' : '收藏' }}
            </el-button>
          </div>
        </div>

        <div class="cover-image">
          <img :src="attraction.cover" :alt="attraction.title" />
        </div>

        <div class="attraction-info">
          <h2>景点介绍</h2>
          <p>{{ attraction.description }}</p>
        </div>

        <div class="info-cards">
          <div class="info-card">
            <i class="el-icon-time"></i>
            <div>
              <div class="card-label">开放时间</div>
              <div class="card-value">{{ attraction.openTime }}</div>
            </div>
          </div>
          <div class="info-card">
            <i class="el-icon-tickets"></i>
            <div>
              <div class="card-label">门票价格</div>
              <div class="card-value">{{ attraction.ticketPrice }}</div>
            </div>
          </div>
          <div class="info-card">
            <i class="el-icon-coordinate"></i>
            <div>
              <div class="card-label">建议游玩</div>
              <div class="card-value">{{ attraction.suggestedDuration }}</div>
            </div>
          </div>
        </div>

        <el-divider />

        <CommentSection
          v-if="attraction.id"
          :comments="comments"
          :target-type="attraction.type || 'attraction'"
          :target-id="Number(attraction.id)"
          @submit="handleCommentSubmit"
        />
      </div>

      <div class="sidebar">
        <div class="stats-card">
          <h3>景点数据</h3>
          <div class="stat-item">
            <span class="stat-label">综合评分</span>
            <span class="stat-value">{{ attraction.rating }}</span>
          </div>
          <div class="stat-item">
            <span class="stat-label">浏览量</span>
            <span class="stat-value">{{ attraction.views }}</span>
          </div>
          <div class="stat-item">
            <span class="stat-label">收藏数</span>
            <span class="stat-value">{{ attraction.favorites }}</span>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import { mapGetters } from 'vuex'
import CommentSection from '@/components/CommentSection.vue'
import { getAttractionDetail, deletePost } from '@/api'

export default {
  name: 'AttractionDetail',
  components: {
    CommentSection
  },
  data() {
    return {
      loading: true,
      attraction: {
        id: Number(this.$route.params.id) || 0,
        type: 'attraction',
        userId: null, // 添加userId用于判断是否是作者
        title: '',
        cover: '/placeholder.svg?height=480&width=1200',
        rating: 0,
        reviewCount: 0,
        location: '',
        description: '',
        openTime: '',
        ticketPrice: '',
        suggestedDuration: '',
        views: 0,
        favorites: 0,
        isFavorited: false
      },
      comments: []
    }
  },
  computed: {
    ...mapGetters(['isLoggedIn', 'currentUser']),
    isAuthor() {
      if (!this.isLoggedIn || !this.currentUser || !this.attraction.userId) {
        return false
      }
      return this.attraction.userId === this.currentUser.id
    }
  },
  mounted() {
    this.fetchAttractionDetail()
  },
  methods: {
    async fetchAttractionDetail() {
      this.loading = true
      try {
        const id = Number(this.$route.params.id)
        const res = await getAttractionDetail(id)
        const data = res.data || {}
        this.attraction = {
          id: data.id || id,
          type: data.type || 'attraction',
          userId: data.userId || null, // 保存作者ID
          title: data.title || '',
          cover: data.cover || '/placeholder.svg?height=480&width=1200',
          rating: data.rating || 0,
          reviewCount: data.comments || 0,
          location: data.location || '',
          description: data.description || '',
          openTime: data.openTime || '',
          ticketPrice: data.ticketPrice || '',
          suggestedDuration: data.suggestedDuration || '',
          views: data.views || 0,
          favorites: data.favorites || 0,
          isFavorited: data.isFavorited || false
        }
      } catch (error) {
        console.error('获取景点详情失败:', error)
        this.$message.error('获取景点详情失败')
      } finally {
        this.loading = false
      }
    },
    handleFavorite() {
      this.attraction.isFavorited = !this.attraction.isFavorited
      this.$message.success(this.attraction.isFavorited ? '收藏成功' : '取消收藏')
    },
    handleCommentSubmit(content) {
      this.$message.success('评论成功')
    },
    handleEdit() {
      // 跳转到发布页面，传递id作为编辑模式
      this.$router.push({
        path: '/publish',
        query: { 
          type: 'attraction',
          id: this.attraction.id,
          mode: 'edit'
        }
      })
    },
    handleDelete() {
      this.$confirm('确定要删除这个景点吗？删除后无法恢复。', '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(async () => {
        try {
          await deletePost(this.attraction.id)
          this.$message.success('删除成功')
          this.$router.push('/')
        } catch (error) {
          console.error('删除失败:', error)
          this.$message.error(error.response?.data?.message || '删除失败')
        }
      }).catch(() => {})
    }
  }
}
</script>

<style lang="scss" scoped>
.attraction-detail {
  max-width: 1200px;
  margin: 0 auto;
}

.detail-header {
  margin-bottom: 24px;
}

.detail-content {
  display: grid;
  grid-template-columns: 1fr 300px;
  gap: 24px;
}

.main-section {
  background: white;
  border-radius: var(--radius-md);
  padding: 32px;
  box-shadow: var(--shadow-sm);
}

.attraction-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 24px;
}

.attraction-title {
  font-size: 32px;
  font-weight: 700;
  color: var(--color-text);
  margin-bottom: 12px;
}

.rating-section {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 12px;

  .review-count {
    color: var(--color-text-secondary);
    font-size: 14px;
  }
}

.location {
  display: flex;
  align-items: center;
  gap: 6px;
  color: var(--color-text-secondary);
  font-size: 14px;

  i {
    color: var(--color-primary);
  }
}

.cover-image {
  width: 100%;
  height: 400px;
  border-radius: var(--radius-md);
  overflow: hidden;
  margin-bottom: 32px;

  img {
    width: 100%;
    height: 100%;
    object-fit: cover;
  }
}

.attraction-info {
  margin-bottom: 32px;

  h2 {
    font-size: 20px;
    font-weight: 600;
    color: var(--color-text);
    margin-bottom: 16px;
  }

  p {
    line-height: 1.8;
    color: var(--color-text-secondary);
  }
}

.info-cards {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 16px;
  margin-bottom: 32px;
}

.info-card {
  background: var(--color-bg-secondary);
  border-radius: var(--radius-md);
  padding: 20px;
  display: flex;
  align-items: center;
  gap: 16px;

  i {
    font-size: 32px;
    color: var(--color-primary);
  }

  .card-label {
    font-size: 13px;
    color: var(--color-text-secondary);
    margin-bottom: 4px;
  }

  .card-value {
    font-size: 16px;
    font-weight: 600;
    color: var(--color-text);
  }
}

.sidebar {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.stats-card {
  background: white;
  border-radius: var(--radius-md);
  padding: 20px;
  box-shadow: var(--shadow-sm);

  h3 {
    font-size: 16px;
    font-weight: 600;
    color: var(--color-text);
    margin-bottom: 20px;
  }

  .stat-item {
    display: flex;
    justify-content: space-between;
    align-items: center;
    padding: 12px 0;
    border-bottom: 1px solid var(--color-border);

    &:last-child {
      border-bottom: none;
    }

    .stat-label {
      color: var(--color-text-secondary);
    }

    .stat-value {
      font-size: 18px;
      font-weight: 600;
      color: var(--color-primary);
    }
  }
}
</style>

<template>
  <div v-loading="loading" class="route-detail">
    <div class="detail-header">
      <el-breadcrumb separator="/">
        <el-breadcrumb-item :to="{ path: '/' }">首页</el-breadcrumb-item>
        <el-breadcrumb-item>线路详情</el-breadcrumb-item>
      </el-breadcrumb>
    </div>

    <div class="detail-content">
      <div class="main-section">
        <div class="cover-image">
          <img :src="route.cover" :alt="route.title" />
        </div>

        <div class="route-header">
          <h1 class="route-title">{{ route.title }}</h1>
          <div class="route-meta">
            <el-tag type="info" v-if="route.duration">{{ route.duration }}</el-tag>
            <el-tag type="warning" v-if="route.budget">{{ route.budget }}</el-tag>
          </div>
        </div>

        <div class="author-section">
          <div class="author-info">
            <img :src="route.avatar || '/placeholder.svg?height=80&width=80'" :alt="route.author || '未知'" class="avatar" />
            <div>
              <div class="author-name">{{ route.author || '未知' }}</div>
              <div class="publish-time">{{ route.publishTime }}</div>
            </div>
          </div>

          <div class="action-buttons">
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
              :type="route.isFavorited ? 'warning' : 'default'"
              icon="el-icon-star-off"
              @click="handleFavorite"
            >
              {{ route.isFavorited ? '已收藏' : '收藏' }}
            </el-button>
            <el-button icon="el-icon-share">分享</el-button>
          </div>
        </div>

        <el-divider />

        <div class="route-description">
          <h2>线路介绍</h2>
          <p>{{ route.description }}</p>
        </div>

        <div class="route-spots">
          <h2>景点列表</h2>
          <div class="spots-timeline">
            <div
              v-for="(spot, index) in route.spots"
              :key="index"
              class="spot-item"
            >
              <div class="spot-index">{{ index + 1 }}</div>
              <div class="spot-content">
                <h3>{{ spot.name }}</h3>
                <p>{{ spot.description }}</p>
                <div class="spot-time">
                  <i class="el-icon-time"></i>
                  建议游玩时间：{{ spot.duration }}
                </div>
              </div>
            </div>
          </div>
        </div>

        <el-divider />

        <CommentSection
          v-if="route.id"
          :comments="comments"
          :target-type="route.type || 'route'"
          :target-id="Number(route.id)"
          @submit="handleCommentSubmit"
        />
      </div>

      <div class="sidebar">
        <div class="stats-card">
          <div class="stat-item">
            <i class="el-icon-star-off"></i>
            <div>
              <div class="stat-value">{{ route.likes }}</div>
              <div class="stat-label">点赞</div>
            </div>
          </div>
          <div class="stat-item">
            <i class="el-icon-collection"></i>
            <div>
              <div class="stat-value">{{ route.favorites }}</div>
              <div class="stat-label">收藏</div>
            </div>
          </div>
          <div class="stat-item">
            <i class="el-icon-view"></i>
            <div>
              <div class="stat-value">{{ route.views }}</div>
              <div class="stat-label">浏览</div>
            </div>
          </div>
        </div>

        <div class="tips-card">
          <h3>
            <i class="el-icon-warning-outline"></i>
            出行提示
          </h3>
          <ul>
            <li v-for="(tip, index) in route.tips" :key="index">
              {{ tip }}
            </li>
          </ul>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import { mapGetters } from 'vuex'
import CommentSection from '@/components/CommentSection.vue'
import { getRouteDetail, deletePost } from '@/api'

export default {
  name: 'RouteDetail',
  components: {
    CommentSection
  },
  data() {
    return {
      loading: true,
      route: {
        id: Number(this.$route.params.id) || 0,
        type: 'route',
        userId: null, // 添加userId用于判断是否是作者
        title: '',
        cover: '/placeholder.svg?height=480&width=1200',
        author: '',
        avatar: '',
        publishTime: '',
        duration: '',
        budget: '',
        description: '',
        tips: [],
        likes: 0,
        favorites: 0,
        views: 0,
        isFavorited: false
      },
      comments: []
    }
  },
  computed: {
    ...mapGetters(['isLoggedIn', 'currentUser']),
    isAuthor() {
      if (!this.isLoggedIn || !this.currentUser || !this.route.userId) {
        return false
      }
      return this.route.userId === this.currentUser.id
    }
  },
  mounted() {
    this.fetchRouteDetail()
  },
  methods: {
    async fetchRouteDetail() {
      this.loading = true
      try {
        const id = Number(this.$route.params.id)
        const res = await getRouteDetail(id)
        const data = res.data || {}
        
        // 处理 tips 字段（可能是字符串或数组）
        let tips = []
        if (data.tips) {
          if (Array.isArray(data.tips)) {
            tips = data.tips
          } else if (typeof data.tips === 'string') {
            tips = data.tips.split(',').filter(t => t.trim())
          }
        }
        
        this.route = {
          id: data.id || id,
          type: data.type || 'route',
          userId: data.userId || null, // 保存作者ID
          title: data.title || '',
          cover: data.cover || '/placeholder.svg?height=480&width=1200',
          author: data.author || '未知',
          avatar: data.avatar || '/placeholder.svg?height=80&width=80',
          publishTime: data.createdAt ? new Date(data.createdAt).toLocaleDateString() : '',
          duration: data.duration ? `${data.duration}天` : '',
          budget: data.budget || '',
          description: data.description || '',
          tips: tips,
          likes: data.likes || 0,
          favorites: data.favorites || 0,
          views: data.views || 0,
          isFavorited: data.isFavorited || false
        }
      } catch (error) {
        console.error('获取线路详情失败:', error)
        this.$message.error('获取线路详情失败')
      } finally {
        this.loading = false
      }
    },
    handleFavorite() {
      this.route.isFavorited = !this.route.isFavorited
      this.$message.success(this.route.isFavorited ? '收藏成功' : '取消收藏')
    },
    handleCommentSubmit(content) {
      this.$message.success('评论成功')
    },
    handleEdit() {
      // 跳转到发布页面，传递id作为编辑模式
      this.$router.push({
        path: '/publish',
        query: { 
          type: 'route',
          id: this.route.id,
          mode: 'edit'
        }
      })
    },
    handleDelete() {
      this.$confirm('确定要删除这条路线吗？删除后无法恢复。', '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(async () => {
        try {
          await deletePost(this.route.id)
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
.route-detail {
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

.cover-image {
  width: 100%;
  height: 400px;
  border-radius: var(--radius-md);
  overflow: hidden;
  margin-bottom: 24px;

  img {
    width: 100%;
    height: 100%;
    object-fit: cover;
  }
}

.route-title {
  font-size: 32px;
  font-weight: 700;
  color: var(--color-text);
  margin-bottom: 16px;
  line-height: 1.3;
}

.route-meta {
  display: flex;
  gap: 12px;
  margin-bottom: 24px;
}

.author-section {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 24px;
}

.author-info {
  display: flex;
  align-items: center;
  gap: 12px;

  .avatar {
    width: 48px;
    height: 48px;
    border-radius: 50%;
  }

  .author-name {
    font-weight: 600;
    color: var(--color-text);
    margin-bottom: 4px;
  }

  .publish-time {
    font-size: 13px;
    color: var(--color-text-secondary);
  }
}

.action-buttons {
  display: flex;
  gap: 12px;
}

.route-description,
.route-spots {
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

.spots-timeline {
  position: relative;
  padding-left: 40px;

  &::before {
    content: '';
    position: absolute;
    left: 15px;
    top: 0;
    bottom: 0;
    width: 2px;
    background: var(--color-border);
  }
}

.spot-item {
  position: relative;
  margin-bottom: 32px;

  &:last-child {
    margin-bottom: 0;
  }
}

.spot-index {
  position: absolute;
  left: -40px;
  top: 0;
  width: 32px;
  height: 32px;
  border-radius: 50%;
  background: var(--color-primary);
  color: white;
  display: flex;
  align-items: center;
  justify-content: center;
  font-weight: 600;
  font-size: 14px;
}

.spot-content {
  h3 {
    font-size: 18px;
    font-weight: 600;
    color: var(--color-text);
    margin-bottom: 8px;
  }

  p {
    color: var(--color-text-secondary);
    line-height: 1.6;
    margin-bottom: 8px;
  }
}

.spot-time {
  font-size: 13px;
  color: var(--color-text-light);
  display: flex;
  align-items: center;
  gap: 4px;
}

.sidebar {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.stats-card,
.tips-card {
  background: white;
  border-radius: var(--radius-md);
  padding: 20px;
  box-shadow: var(--shadow-sm);
}

.stats-card {
  display: flex;
  flex-direction: column;
  gap: 20px;

  .stat-item {
    display: flex;
    align-items: center;
    gap: 16px;

    i {
      font-size: 32px;
      color: var(--color-primary);
    }

    .stat-value {
      font-size: 24px;
      font-weight: 700;
      color: var(--color-text);
    }

    .stat-label {
      font-size: 13px;
      color: var(--color-text-secondary);
    }
  }
}

.tips-card {
  h3 {
    display: flex;
    align-items: center;
    gap: 8px;
    font-size: 16px;
    font-weight: 600;
    color: var(--color-text);
    margin-bottom: 16px;

    i {
      color: var(--color-warning);
    }
  }

  ul {
    list-style: none;

    li {
      padding: 8px 0;
      color: var(--color-text-secondary);
      line-height: 1.6;
      border-bottom: 1px solid var(--color-border);

      &:last-child {
        border-bottom: none;
      }

      &::before {
        content: '•';
        color: var(--color-primary);
        font-weight: bold;
        margin-right: 8px;
      }
    }
  }
}
</style>

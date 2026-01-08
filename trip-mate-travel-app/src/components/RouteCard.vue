<template>
  <div class="route-card" @click="$emit('click')">
    <div class="card-cover">
      <img :src="data.cover" :alt="data.title" />
      <div v-if="type === 'attraction' && data.rating" class="rating-badge">
        <i class="el-icon-star-on"></i>
        {{ data.rating }}
      </div>
    </div>

    <div class="card-content">
      <h3 class="card-title">{{ data.title }}</h3>
      
      <p v-if="data.description" class="card-description">
        {{ data.description }}
      </p>

      <div v-if="data.location" class="location">
        <i class="el-icon-location"></i>
        {{ data.location }}
      </div>

      <div class="card-tags">
        <el-tag
          v-for="tag in data.tags"
          :key="tag"
          size="small"
          effect="plain"
        >
          {{ tag }}
        </el-tag>
      </div>

      <div class="card-footer">
        <div class="author-info">
          <img :src="data.avatar" :alt="data.author" class="avatar" />
          <span>{{ data.author }}</span>
        </div>

        <div class="stats">
          <span 
            class="stat-item clickable" 
            :class="{ active: data.isLiked }"
            @click.stop="handleLike"
          >
            <i :class="data.isLiked ? 'el-icon-star-on' : 'el-icon-star-off'"></i>
            {{ data.likes }}
          </span>
          <span 
            class="stat-item clickable" 
            :class="{ active: data.isFavorited }"
            @click.stop="handleFavorite"
          >
            <i :class="data.isFavorited ? 'el-icon-collection-tag' : 'el-icon-collection'"></i>
            {{ data.favorites }}
          </span>
          <span class="stat-item">
            <i class="el-icon-chat-dot-round"></i>
            {{ data.comments }}
          </span>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import { likeContent, unlikeContent, favoriteContent, unfavoriteContent } from '@/api'

export default {
  name: 'RouteCard',
  props: {
    data: {
      type: Object,
      required: true
    },
    type: {
      type: String,
      default: 'route'
    }
  },
  data() {
    return {
      isLiked: false,
      isFavorited: false
    }
  },
  mounted() {
    this.isLiked = this.data.isLiked || false
    this.isFavorited = this.data.isFavorited || false
  },
  methods: {
    async handleLike() {
      if (!this.$store.getters.isLoggedIn) {
        this.$message.warning('请先登录')
        this.$router.push('/login')
        return
      }

      try {
        if (this.isLiked) {
          await unlikeContent({
            targetType: this.type,
            targetId: this.data.id
          })
          this.data.likes--
          this.isLiked = false
          this.$message.success('取消点赞')
        } else {
          await likeContent({
            targetType: this.type,
            targetId: this.data.id
          })
          this.data.likes++
          this.isLiked = true
          this.$message.success('点赞成功')
        }
      } catch (error) {
        console.error('点赞操作失败:', error)
      }
    },
    async handleFavorite() {
      if (!this.$store.getters.isLoggedIn) {
        this.$message.warning('请先登录')
        this.$router.push('/login')
        return
      }

      try {
        if (this.isFavorited) {
          await unfavoriteContent({
            targetType: this.type,
            targetId: this.data.id
          })
          this.data.favorites--
          this.isFavorited = false
          this.$message.success('取消收藏')
        } else {
          await favoriteContent({
            targetType: this.type,
            targetId: this.data.id
          })
          this.data.favorites++
          this.isFavorited = true
          this.$message.success('收藏成功')
        }
      } catch (error) {
        console.error('收藏操作失败:', error)
      }
    }
  }
}
</script>

<style lang="scss" scoped>
.route-card {
  background: white;
  border-radius: var(--radius-md);
  overflow: hidden;
  box-shadow: var(--shadow-sm);
  cursor: pointer;
  transition: all 0.3s ease;

  &:hover {
    transform: translateY(-4px);
    box-shadow: var(--shadow-md);
  }
}

.card-cover {
  position: relative;
  width: 100%;
  height: 220px;
  overflow: hidden;

  img {
    width: 100%;
    height: 100%;
    object-fit: cover;
    transition: transform 0.3s ease;
  }

  &:hover img {
    transform: scale(1.05);
  }
}

.rating-badge {
  position: absolute;
  top: 12px;
  right: 12px;
  background: rgba(255, 255, 255, 0.95);
  padding: 4px 12px;
  border-radius: 20px;
  font-weight: 600;
  color: var(--color-warning);
  display: flex;
  align-items: center;
  gap: 4px;
  font-size: 14px;
}

.card-content {
  padding: 16px;
}

.card-title {
  font-size: 18px;
  font-weight: 600;
  color: var(--color-text);
  margin-bottom: 8px;
  line-height: 1.4;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.card-description {
  font-size: 14px;
  color: var(--color-text-secondary);
  line-height: 1.6;
  margin-bottom: 12px;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.location {
  font-size: 13px;
  color: var(--color-text-secondary);
  display: flex;
  align-items: center;
  gap: 4px;
  margin-bottom: 12px;

  i {
    color: var(--color-primary);
  }
}

.card-tags {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  margin-bottom: 16px;
}

.card-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding-top: 12px;
  border-top: 1px solid var(--color-border);
}

.author-info {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 13px;
  color: var(--color-text-secondary);

  .avatar {
    width: 28px;
    height: 28px;
    border-radius: 50%;
    object-fit: cover;
  }
}

.stats {
  display: flex;
  gap: 16px;
}

.stat-item {
  font-size: 13px;
  color: var(--color-text-secondary);
  display: flex;
  align-items: center;
  gap: 4px;

  i {
    font-size: 14px;
  }

  &.clickable {
    cursor: pointer;
    transition: all 0.2s ease;

    &:hover {
      color: var(--color-primary);
    }

    &.active {
      color: var(--color-primary);
    }
  }
}
</style>

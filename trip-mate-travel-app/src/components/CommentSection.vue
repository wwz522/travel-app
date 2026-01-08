<template>
  <div class="comment-section">
    <h2>
      评论
      <span class="comment-count">({{ comments.length }})</span>
    </h2>

    <div v-if="isLoggedIn" class="comment-input">
      <el-input
        v-model="commentContent"
        type="textarea"
        :rows="3"
        placeholder="写下你的评论..."
        maxlength="500"
        show-word-limit
      />
      <div class="input-actions">
        <el-button
          type="primary"
          size="small"
          :disabled="!commentContent.trim()"
          @click="submitComment"
        >
          发表评论
        </el-button>
      </div>
    </div>

    <div v-else class="login-prompt">
      <el-button type="text" @click="$router.push('/login')">
        登录后发表评论
      </el-button>
    </div>

    <div class="comment-list">
      <div
        v-for="comment in comments"
        :key="comment.id"
        class="comment-item"
      >
        <img :src="comment.user.avatar" :alt="comment.user.name" class="avatar" />
        <div class="comment-content">
          <div class="comment-header">
            <span class="user-name">{{ comment.user.name }}</span>
            <span class="comment-time">{{ comment.time }}</span>
          </div>
          <p class="comment-text">{{ comment.content }}</p>
          <div class="comment-actions">
            <el-button 
              type="text" 
              size="small" 
              :icon="comment.isLiked ? 'el-icon-star-on' : 'el-icon-star-off'"
              :class="{ 'liked': comment.isLiked }"
              @click="handleLikeComment(comment)"
            >
              {{ comment.likes }}
            </el-button>
            <el-button v-if="canDeleteComment(comment)" type="text" size="small" icon="el-icon-delete" @click="handleDeleteComment(comment)">
              删除
            </el-button>
          </div>
        </div>
      </div>
    </div>

    <div v-if="comments.length === 0" class="empty-comments">
      <i class="el-icon-chat-line-round"></i>
      <p>暂无评论，快来抢沙发吧~</p>
    </div>
  </div>
</template>

<script>
import { mapGetters } from 'vuex'
import { getComments, addComment, deleteComment, likeComment } from '@/api'

export default {
  name: 'CommentSection',
  props: {
    targetType: {
      type: String,
      required: true
    },
    targetId: {
      type: Number,
      required: true
    }
  },
  data() {
    return {
      commentContent: '',
      comments: [],
      loading: false
    }
  },
  computed: {
    ...mapGetters(['isLoggedIn', 'currentUser'])
  },
  mounted() {
    if (this.targetId && !isNaN(this.targetId)) {
      this.fetchComments()
    }
  },
  watch: {
    targetId(newVal, oldVal) {
      if (newVal && !isNaN(newVal) && newVal !== oldVal) {
        this.fetchComments()
      }
    }
  },
  methods: {
    async fetchComments() {
      if (!this.targetId || isNaN(this.targetId)) {
        console.warn('targetId 无效:', this.targetId)
        this.comments = []
        return
      }
      
      this.loading = true
      try {
        console.log('获取评论，参数:', {
          targetType: this.targetType,
          targetId: this.targetId,
          targetIdType: typeof this.targetId
        })
        
        const res = await getComments({
          targetType: this.targetType,
          targetId: Number(this.targetId) // 确保是数字类型
        })
        
        console.log('评论接口响应:', res)
        console.log('响应数据类型:', typeof res.data, Array.isArray(res.data))
        
        // 后端返回的是 List，直接使用 res.data
        const list = Array.isArray(res.data) ? res.data : (res.data?.records || [])
        console.log('解析后的评论列表:', list, '长度:', list.length)
        
        this.comments = (list || []).map(comment => {
          const userInfo = comment.user || {}
          return {
            id: comment.id,
            userId: comment.userId ? Number(comment.userId) : null, // 保存评论作者ID，确保是数字类型
            user: {
              avatar: userInfo.avatar || 'https://i.pravatar.cc/150?img=1',
              name: userInfo.name || userInfo.username || '匿名用户'
            },
            content: comment.content || '',
            time: comment.time || this.formatTime(comment.createdAt),
            likes: comment.likes || 0,
            isLiked: comment.isLiked || false // 评论点赞状态
          }
        })
        console.log('最终评论列表:', this.comments)
      } catch (error) {
        console.error('获取评论失败:', error)
        console.error('错误消息:', error.message)
        console.error('错误代码:', error.code)
        console.error('错误响应:', error.response)
        if (error.response) {
          console.error('响应状态:', error.response.status)
          console.error('响应数据:', error.response.data)
        }
        // 不显示错误提示，避免干扰用户体验（评论为空时是正常的）
        this.comments = []
      } finally {
        this.loading = false
      }
    },
    async submitComment() {
      if (!this.commentContent.trim()) return
      
      try {
        await addComment({
          targetType: this.targetType,
          targetId: this.targetId,
          content: this.commentContent
        })
        this.$message.success('评论发表成功')
        this.commentContent = ''
        this.fetchComments()
      } catch (error) {
        console.error('发表评论失败:', error)
      }
    },
    formatTime(time) {
      const now = new Date()
      const commentTime = new Date(time)
      const diff = now - commentTime
      
      const minute = 60 * 1000
      const hour = 60 * minute
      const day = 24 * hour
      
      if (diff < minute) {
        return '刚刚'
      } else if (diff < hour) {
        return Math.floor(diff / minute) + '分钟前'
      } else if (diff < day) {
        return Math.floor(diff / hour) + '小时前'
      } else {
        return Math.floor(diff / day) + '天前'
      }
    },
    canDeleteComment(comment) {
      if (!this.isLoggedIn || !this.currentUser || !this.currentUser.id) {
        console.log('canDeleteComment检查失败:', {
          isLoggedIn: this.isLoggedIn,
          hasCurrentUser: !!this.currentUser,
          currentUserId: this.currentUser?.id
        })
        return false
      }
      // 只有评论作者可以删除自己的评论
      // 确保类型一致进行比较
      const commentUserId = Number(comment.userId)
      const currentUserId = Number(this.currentUser.id)
      const canDelete = commentUserId === currentUserId && !isNaN(commentUserId) && !isNaN(currentUserId)
      if (!canDelete) {
        console.log('canDeleteComment比较失败:', {
          commentUserId,
          currentUserId,
          commentUserIdType: typeof comment.userId,
          currentUserIdType: typeof this.currentUser.id
        })
      }
      return canDelete
    },
    async handleDeleteComment(comment) {
      this.$confirm('确定要删除这条评论吗？', '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(async () => {
        try {
          console.log('删除评论，commentId:', comment.id, 'commentUserId:', comment.userId, 'currentUserId:', this.currentUser?.id)
          await deleteComment(comment.id)
          this.$message.success('删除成功')
          // 延迟一下再刷新，确保后端已更新
          setTimeout(() => {
            this.fetchComments()
          }, 300)
        } catch (error) {
          console.error('删除评论失败:', error)
          console.error('错误详情:', error.response?.data)
          console.error('错误状态码:', error.response?.status)
          this.$message.error(error.response?.data?.message || '删除失败，请检查是否有权限')
        }
      }).catch(() => {})
    },
    async handleLikeComment(comment) {
      if (!this.isLoggedIn) {
        this.$message.warning('请先登录')
        this.$router.push('/login')
        return
      }

      try {
        await likeComment(comment.id)
        // 切换点赞状态
        comment.isLiked = !comment.isLiked
        comment.likes = comment.isLiked ? (comment.likes + 1) : Math.max(0, comment.likes - 1)
        this.$message.success(comment.isLiked ? '点赞成功' : '取消点赞')
      } catch (error) {
        console.error('点赞失败:', error)
        this.$message.error(error.response?.data?.message || '操作失败')
      }
    }
  }
}
</script>

<style lang="scss" scoped>
.comment-section {
  h2 {
    font-size: 20px;
    font-weight: 600;
    color: var(--color-text);
    margin-bottom: 20px;

    .comment-count {
      color: var(--color-text-secondary);
      font-weight: 400;
      font-size: 16px;
    }
  }
}

.comment-input {
  margin-bottom: 32px;

  .input-actions {
    margin-top: 12px;
    display: flex;
    justify-content: flex-end;
  }
}

.login-prompt {
  text-align: center;
  padding: 32px;
  background: var(--color-bg-secondary);
  border-radius: var(--radius-md);
  margin-bottom: 32px;
}

.comment-list {
  display: flex;
  flex-direction: column;
  gap: 24px;
}

.comment-item {
  display: flex;
  gap: 12px;

  .avatar {
    width: 40px;
    height: 40px;
    border-radius: 50%;
    flex-shrink: 0;
  }
}

.comment-content {
  flex: 1;
}

.comment-header {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 8px;

  .user-name {
    font-weight: 600;
    color: var(--color-text);
  }

  .comment-time {
    font-size: 13px;
    color: var(--color-text-light);
  }
}

.comment-text {
  color: var(--color-text-secondary);
  line-height: 1.6;
  margin-bottom: 8px;
}

.comment-actions {
  display: flex;
  gap: 8px;
}

.empty-comments {
  text-align: center;
  padding: 60px 20px;
  color: var(--color-text-secondary);

  i {
    font-size: 48px;
    margin-bottom: 12px;
    opacity: 0.5;
  }

  p {
    font-size: 14px;
  }
}
</style>

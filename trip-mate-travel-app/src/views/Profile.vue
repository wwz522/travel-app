<template>
  <div class="profile-page">
    <div v-if="!$store.getters.isLoggedIn" class="not-logged-in">
      <div class="not-logged-in-content">
        <i class="el-icon-user"></i>
        <h2>请先登录</h2>
        <p>登录后查看您的个人中心</p>
        <div class="action-buttons">
          <el-button type="primary" @click="$router.push('/login')">登录</el-button>
          <el-button @click="$router.push('/login')">注册</el-button>
        </div>
      </div>
    </div>

    <div v-else>
      <div class="profile-header">
        <div class="user-info">
          <img :src="(user && user.avatar) || '/placeholder.svg?height=100&width=100'" alt="用户头像" class="user-avatar" />
          <div class="user-details">
            <h1 class="user-name">{{ (user && user.username) || '未登录' }}</h1>
            <p class="user-bio">{{ (user && user.bio) || '这个人很懒，什么都没写' }}</p>
            <div class="user-stats">
              <div class="stat-item">
                <span class="stat-value">{{ userStats.posts }}</span>
                <span class="stat-label">发布</span>
              </div>
              <div class="stat-item">
                <span class="stat-value">{{ userStats.likes }}</span>
                <span class="stat-label">获赞</span>
              </div>
              <div class="stat-item">
                <span class="stat-value">{{ userStats.favorites }}</span>
                <span class="stat-label">收藏</span>
              </div>
            </div>
          </div>
        </div>
        <div class="header-actions">
          <el-button icon="el-icon-setting" @click="handleEditProfile">编辑资料</el-button>
          <el-button icon="el-icon-switch-button" @click="handleLogout">退出登录</el-button>
        </div>
      </div>

      <el-tabs v-model="activeTab" class="profile-tabs" @tab-click="handleTabChange">
        <el-tab-pane label="我的发布" name="posts">
          <div v-if="loading" class="loading-container">
            <el-skeleton :rows="3" animated />
          </div>
          <div v-else-if="myPosts.length === 0" class="empty-container">
            <el-empty description="暂无发布内容" />
          </div>
          <div v-else class="content-grid">
            <RouteCard
              v-for="item in myPosts"
              :key="item.type + '-' + item.id"
              :data="item"
              :type="item.type"
            />
          </div>
        </el-tab-pane>

        <el-tab-pane label="我的收藏" name="favorites">
          <div v-if="loading" class="loading-container">
            <el-skeleton :rows="3" animated />
          </div>
          <div v-else-if="myFavorites.length === 0" class="empty-container">
            <el-empty description="暂无收藏内容" />
          </div>
          <div v-else class="content-grid">
            <RouteCard
              v-for="item in myFavorites"
              :key="item.type + '-' + item.id"
              :data="item"
              :type="item.type"
            />
          </div>
        </el-tab-pane>

        <el-tab-pane label="我的草稿" name="drafts">
          <div v-if="loading" class="loading-container">
            <el-skeleton :rows="3" animated />
          </div>
          <div v-else-if="myDrafts.length === 0" class="empty-container">
            <el-empty description="暂无草稿" />
          </div>
          <div v-else class="content-grid">
            <div v-for="item in myDrafts" :key="'draft-' + item.id" class="draft-item">
              <RouteCard
                :data="item"
                :type="item.type"
              />
              <div class="draft-actions">
                <el-button type="primary" size="small" @click="handleEditDraft(item)">
                  <i class="el-icon-edit"></i> 编辑
                </el-button>
                <el-button type="success" size="small" @click="handlePublishDraft(item)">
                  <i class="el-icon-check"></i> 发布
                </el-button>
                <el-button type="danger" size="small" @click="handleDeleteDraft(item)">
                  <i class="el-icon-delete"></i> 删除
                </el-button>
              </div>
            </div>
          </div>
        </el-tab-pane>

        <el-tab-pane label="浏览历史" name="history">
          <div class="empty-container">
            <el-empty description="功能开发中..." />
          </div>
        </el-tab-pane>
      </el-tabs>

      <!-- 编辑资料弹窗 -->
      <el-dialog
        title="编辑资料"
        :visible.sync="editDialogVisible"
        width="600px"
        @close="handleDialogClose"
      >
        <el-form ref="editForm" :model="editForm" :rules="editRules" label-width="100px">
          <el-form-item label="用户名" prop="username">
            <el-input v-model="editForm.username" placeholder="请输入用户名" />
          </el-form-item>
          <el-form-item label="邮箱" prop="email">
            <el-input v-model="editForm.email" placeholder="请输入邮箱" />
          </el-form-item>
          <el-form-item label="头像">
            <el-upload
              class="avatar-uploader"
              action="#"
              :show-file-list="false"
              :before-upload="handleAvatarUpload"
            >
              <img v-if="editForm.avatar" :src="editForm.avatar" class="avatar-preview" />
              <i v-else class="el-icon-plus avatar-uploader-icon"></i>
            </el-upload>
          </el-form-item>
          <el-form-item label="个人简介">
            <el-input
              v-model="editForm.bio"
              type="textarea"
              :rows="3"
              placeholder="介绍一下自己吧~"
              maxlength="200"
              show-word-limit
            />
          </el-form-item>
        </el-form>
        <div slot="footer" class="dialog-footer">
          <el-button @click="editDialogVisible = false">取消</el-button>
          <el-button type="primary" :loading="saving" @click="handleSaveProfile">保存</el-button>
        </div>
      </el-dialog>
    </div>
  </div>
</template>

<script>
import RouteCard from '@/components/RouteCard.vue'
import { getMyRoutes, getMyAttractions, getMyFavorites, getDrafts, updateRoute, deletePost, getProfile, updateProfile, uploadFile } from '@/api'

export default {
  name: 'Profile',
  components: {
    RouteCard
  },
  data() {
    return {
      activeTab: 'posts',
      myPosts: [],
      myFavorites: [],
      myHistory: [],
      myDrafts: [],
      loading: false,
      editDialogVisible: false,
      saving: false,
      editForm: {
        username: '',
        email: '',
        avatar: '',
        bio: ''
      },
      editRules: {
        username: [
          { required: true, message: '请输入用户名', trigger: 'blur' },
          { min: 3, max: 20, message: '用户名长度在3到20个字符', trigger: 'blur' }
        ],
        email: [
          { required: true, message: '请输入邮箱', trigger: 'blur' },
          { type: 'email', message: '请输入正确的邮箱地址', trigger: 'blur' }
        ]
      },
      userStats: {
        posts: 0,
        likes: 0,
        favorites: 0
      }
    }
  },
  computed: {
    user() {
      return this.$store.getters.currentUser
    }
  },
  mounted() {
    console.log('Profile mounted - isLoggedIn:', this.$store.getters.isLoggedIn)
    console.log('Profile mounted - token:', this.$store.state.token)
    console.log('Profile mounted - user:', this.$store.getters.currentUser)
    
    if (!this.$store.getters.isLoggedIn || !this.$store.getters.currentUser) {
      this.$router.push('/login')
      return
    }
    
    this.loadUserContent()
    this.loadUserProfile()
  },
  methods: {
    async loadUserContent() {
      this.loading = true
      try {
        await Promise.all([
          this.loadMyPosts(),
          this.loadMyFavorites()
        ])
      } catch (error) {
        console.error('加载用户内容失败:', error)
        this.$message.error('加载失败')
      } finally {
        this.loading = false
      }
    },
    async loadMyPosts() {
      try {
        const [routesRes, attractionsRes] = await Promise.all([
          getMyRoutes(),
          getMyAttractions()
        ])

        const routes = routesRes.data.records.map(route => ({
          id: route.id,
          type: 'route',
          title: route.title,
          cover: route.cover,
          description: route.description,
          author: route.author || '匿名用户',
          avatar: route.avatar || 'https://i.pravatar.cc/150?img=1',
          duration: route.duration + '天',
          budget: route.budget || '未知',
          likes: route.likes,
          favorites: route.favorites,
          comments: route.comments,
          tags: route.tags || []
        }))

        const attractions = attractionsRes.data.records.map(attraction => ({
          id: attraction.id,
          type: 'attraction',
          title: attraction.title,
          cover: attraction.cover,
          description: attraction.description,
          author: attraction.author || '匿名用户',
          avatar: attraction.avatar || 'https://i.pravatar.cc/150?img=1',
          location: attraction.location || '未知',
          rating: attraction.rating,
          likes: attraction.likes,
          favorites: attraction.favorites,
          comments: attraction.comments,
          tags: attraction.tags || []
        }))

        this.myPosts = [...routes, ...attractions]
        this.userStats.posts = this.myPosts.length
        this.userStats.likes = routes.reduce((sum, r) => sum + r.likes, 0) + 
                              attractions.reduce((sum, a) => sum + a.likes, 0)
      } catch (error) {
        console.error('加载我的发布失败:', error)
        throw error
      }
    },
    async loadMyFavorites() {
      try {
        const res = await getMyFavorites()
        
        this.myFavorites = res.data.records.map(item => {
          const baseData = {
            id: item.id,
            type: item.type,
            title: item.title,
            cover: item.cover,
            description: item.description,
            author: item.author || '匿名用户',
            avatar: item.avatar || 'https://i.pravatar.cc/150?img=1',
            likes: item.likes,
            favorites: item.favorites,
            comments: item.comments,
            tags: item.tags || []
          }

          if (item.type === 'route') {
            return {
              ...baseData,
              duration: item.duration,
              budget: item.budget
            }
          } else {
            return {
              ...baseData,
              location: item.location,
              rating: item.rating
            }
          }
        })

        this.userStats.favorites = this.myFavorites.length
      } catch (error) {
        console.error('加载我的收藏失败:', error)
        throw error
      }
    },
    handleTabChange(tab) {
      if (tab.name === 'posts' && this.myPosts.length === 0) {
        this.loadMyPosts()
      } else if (tab.name === 'favorites' && this.myFavorites.length === 0) {
        this.loadMyFavorites()
      } else if (tab.name === 'drafts' && this.myDrafts.length === 0) {
        this.loadMyDrafts()
      }
    },
    async loadMyDrafts() {
      try {
        const response = await getDrafts()
        this.myDrafts = response.data.records || []
      } catch (error) {
        console.error('加载草稿失败:', error)
        this.$message.error('加载草稿失败')
      }
    },
    handleEditDraft(item) {
      // 跳转到发布页面，加载草稿数据
      this.$router.push({
        path: '/publish',
        query: { draftId: item.id }
      })
    },
    async handlePublishDraft(item) {
      try {
        await updateRoute(item.id, { isDraft: false })
        this.$message.success('发布成功！')
        // 重新加载数据
        await Promise.all([
          this.loadMyPosts(),
          this.loadMyDrafts()
        ])
      } catch (error) {
        console.error('发布失败:', error)
        this.$message.error(error.response?.data?.message || '发布失败')
      }
    },
    async handleDeleteDraft(item) {
      this.$confirm('确定要删除这个草稿吗？', '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(async () => {
        try {
          await deletePost(item.id)
          this.$message.success('删除成功')
          await this.loadMyDrafts()
        } catch (error) {
          console.error('删除失败:', error)
          this.$message.error(error.response?.data?.message || '删除失败')
        }
      }).catch(() => {})
    },
    async loadUserProfile() {
      try {
        const response = await getProfile()
        const userData = response.data
        this.editForm = {
          username: userData.username || '',
          email: userData.email || '',
          avatar: userData.avatar || '',
          bio: userData.bio || ''
        }
        // 更新store中的用户信息
        if (this.$store.getters.currentUser) {
          this.$store.commit('SET_USER', {
            ...this.$store.getters.currentUser,
            ...userData
          })
        }
      } catch (error) {
        console.error('加载用户资料失败:', error)
      }
    },
    handleEditProfile() {
      // 从当前用户信息填充表单
      if (this.$store.getters.currentUser) {
        const currentUser = this.$store.getters.currentUser
        this.editForm = {
          username: currentUser.username || '',
          email: currentUser.email || '',
          avatar: currentUser.avatar || '',
          bio: currentUser.bio || ''
        }
      }
      this.editDialogVisible = true
    },
    handleDialogClose() {
      this.$refs.editForm?.resetFields()
    },
    async handleAvatarUpload(file) {
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

      try {
        const res = await uploadFile(file)
        this.editForm.avatar = res.data.url
        this.$message.success('头像上传成功')
      } catch (error) {
        console.error('头像上传失败:', error)
        this.$message.error('头像上传失败')
      }
      return false
    },
    async handleSaveProfile() {
      this.$refs.editForm.validate(async (valid) => {
        if (!valid) {
          return
        }

        this.saving = true
        try {
          await updateProfile(this.editForm)
          this.$message.success('资料更新成功')
          this.editDialogVisible = false
          
          // 重新加载用户资料并更新store
          await this.loadUserProfile()
        } catch (error) {
          console.error('保存失败:', error)
          this.$message.error(error.response?.data?.message || '保存失败')
        } finally {
          this.saving = false
        }
      })
    },
    handleLogout() {
      this.$confirm('确定要退出登录吗？', '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(() => {
        this.$store.dispatch('logout')
        this.$message.success('已退出登录')
        this.$router.push('/login')
      }).catch(() => {
        // 用户取消，不做任何操作
      })
    }
  }
}
</script>

<style lang="scss" scoped>
.profile-page {
  max-width: 1200px;
  margin: 0 auto;
}

.profile-header {
  background: white;
  border-radius: var(--radius-md);
  padding: 32px;
  margin-bottom: 24px;
  box-shadow: var(--shadow-sm);
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
}

.header-actions {
  display: flex;
  gap: 12px;
}

.user-info {
  display: flex;
  gap: 24px;
}

.user-avatar {
  width: 100px;
  height: 100px;
  border-radius: 50%;
  object-fit: cover;
}

.user-details {
  flex: 1;
}

.user-name {
  font-size: 28px;
  font-weight: 700;
  color: var(--color-text);
  margin-bottom: 8px;
}

.user-bio {
  color: var(--color-text-secondary);
  margin-bottom: 20px;
  line-height: 1.6;
}

.user-stats {
  display: flex;
  gap: 32px;
}

.stat-item {
  display: flex;
  flex-direction: column;
  align-items: center;

  .stat-value {
    font-size: 24px;
    font-weight: 700;
    color: var(--color-text);
  }

  .stat-label {
    font-size: 13px;
    color: var(--color-text-secondary);
    margin-top: 4px;
  }
}

.profile-tabs {
  background: white;
  border-radius: var(--radius-md);
  padding: 24px;
  box-shadow: var(--shadow-sm);
}

.content-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(340px, 1fr));
  gap: 24px;
  margin-top: 20px;
}

.loading-container {
  padding: 40px 0;
}

.empty-container {
  padding: 60px 0;
  display: flex;
  justify-content: center;
  align-items: center;
}

.draft-item {
  position: relative;
}

.draft-actions {
  margin-top: 12px;
  display: flex;
  gap: 8px;
  justify-content: center;
  padding: 12px;
  background: #f5f7fa;
  border-radius: 8px;
}
</style>

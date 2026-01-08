<template>
  <div class="main-layout">
    <header class="app-header">
      <div class="header-content">
        <div class="logo" @click="$router.push('/')">
          <i class="el-icon-location-outline"></i>
          <span>TripMate</span>
        </div>
        
        <div class="search-bar">
          <el-input
            v-model="searchQuery"
            placeholder="搜索线路、景点..."
            prefix-icon="el-icon-search"
            @keyup.enter.native="handleSearch"
          />
        </div>

        <nav class="nav-menu">
          <el-button
            type="text"
            icon="el-icon-s-home"
            @click="$router.push('/')"
          >
            首页
          </el-button>
          <el-button
            v-if="isLoggedIn"
            type="text"
            icon="el-icon-magic-stick"
            @click="$router.push('/ai-plan')"
          >
            AI规划
          </el-button>
          <el-button
            v-if="isLoggedIn"
            type="text"
            icon="el-icon-edit"
            @click="$router.push('/publish')"
          >
            发布
          </el-button>
          <el-button
            v-if="isLoggedIn"
            type="text"
            icon="el-icon-user"
            @click="$router.push('/profile')"
          >
            我的
          </el-button>
          <el-button
            v-if="isLoggedIn"
            type="text"
            icon="el-icon-switch-button"
            @click="handleLogout"
          >
            退出
          </el-button>
          <el-button
            v-if="!isLoggedIn"
            type="primary"
            size="small"
            @click="$router.push('/login')"
          >
            登录
          </el-button>
        </nav>
      </div>
    </header>

    <main class="app-content">
      <router-view />
    </main>
  </div>
</template>

<script>
import { mapGetters } from 'vuex'

export default {
  name: 'MainLayout',
  data() {
    return {
      searchQuery: ''
    }
  },
  computed: {
    ...mapGetters(['isLoggedIn'])
  },
  methods: {
    handleSearch() {
      if (this.searchQuery.trim()) {
        // TODO: Implement search functionality
        this.$message.info(`搜索: ${this.searchQuery}`)
      }
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
.main-layout {
  min-height: 100vh;
}

.app-header {
  background: white;
  box-shadow: var(--shadow-sm);
  position: sticky;
  top: 0;
  z-index: 100;
}

.header-content {
  max-width: 1200px;
  margin: 0 auto;
  padding: 16px 24px;
  display: flex;
  align-items: center;
  gap: 32px;
}

.logo {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 24px;
  font-weight: 700;
  color: var(--color-primary);
  cursor: pointer;
  user-select: none;
  
  i {
    font-size: 28px;
  }
}

.search-bar {
  flex: 1;
  max-width: 480px;
}

.nav-menu {
  display: flex;
  align-items: center;
  gap: 8px;
}

.app-content {
  max-width: 1200px;
  margin: 0 auto;
  padding: 24px;
}
</style>

<template>
  <div class="home-page">
    <div class="filter-bar">
      <el-radio-group v-model="contentType" size="medium">
        <el-radio-button label="all">全部</el-radio-button>
        <el-radio-button label="route">线路</el-radio-button>
        <el-radio-button label="attraction">景点</el-radio-button>
      </el-radio-group>

      <el-select v-model="sortBy" placeholder="排序方式" style="width: 140px">
        <el-option label="最新发布" value="latest" />
        <el-option label="最热门" value="hot" />
        <el-option label="最多收藏" value="favorite" />
      </el-select>
    </div>

    <div v-loading="loading" class="content-grid">
      <RouteCard
        v-for="item in filteredContent"
        :key="item.type + '-' + item.id"
        :data="item"
        :type="item.type"
        @click="handleCardClick(item)"
      />
    </div>

    <div v-if="!loading && filteredContent.length === 0" class="empty-state">
      <i class="el-icon-folder-opened"></i>
      <p>暂无内容</p>
    </div>
  </div>
</template>

<script>
import RouteCard from '@/components/RouteCard.vue'
import { getRoutes, getAttractions } from '@/api'

export default {
  name: 'Home',
  components: {
    RouteCard
  },
  data() {
    return {
      contentType: 'all',
      sortBy: 'latest',
      loading: true,
      content: []
    }
  },
  computed: {
    filteredContent() {
      if (this.contentType === 'all') {
        return this.content
      }
      return this.content.filter(item => item.type === this.contentType)
    }
  },
  watch: {
    contentType() {
      this.fetchContent()
    },
    sortBy() {
      this.fetchContent()
    }
  },
  mounted() {
    this.fetchContent()
    // 支持搜索功能
    if (this.$route.query.q) {
      // 搜索功能已经在getRoutes/getAttractions中实现
      this.fetchContent()
    }
  },
  methods: {
    async fetchContent() {
      this.loading = true
      try {
        let routes = []
        let attractions = []
        const keyword = this.$route.query.q || ''

        if (this.contentType === 'all' || this.contentType === 'route') {
          const res = await getRoutes({
            page: 1,
            size: 100,
            sortBy: this.sortBy,
            keyword: keyword
          })
          routes = res.data.records.map(route => ({
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
        }

        if (this.contentType === 'all' || this.contentType === 'attraction') {
          const res = await getAttractions({
            page: 1,
            size: 100,
            sortBy: this.sortBy,
            keyword: keyword
          })
          attractions = res.data.records.map(attraction => ({
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
        }

        this.content = [...routes, ...attractions]
      } catch (error) {
        console.error('获取内容失败:', error)
        this.$message.error('获取内容失败')
      } finally {
        this.loading = false
      }
    },
    handleCardClick(item) {
      if (item.type === 'route') {
        this.$router.push(`/route/${item.id}`)
      } else {
        this.$router.push(`/attraction/${item.id}`)
      }
    }
  }
}
</script>

<style lang="scss" scoped>
.home-page {
  padding: 0;
}

.filter-bar {
  background: white;
  padding: 20px;
  border-radius: var(--radius-md);
  margin-bottom: 24px;
  display: flex;
  justify-content: space-between;
  align-items: center;
  box-shadow: var(--shadow-sm);
}

.content-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(340px, 1fr));
  gap: 24px;
}

.empty-state {
  text-align: center;
  padding: 80px 20px;
  color: var(--color-text-secondary);
  
  i {
    font-size: 64px;
    margin-bottom: 16px;
    opacity: 0.5;
  }
  
  p {
    font-size: 16px;
  }
}
</style>

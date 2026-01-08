# TripMate - 大学生旅游攻略分享平台

基于 Vue2 + Element-UI 开发的旅游攻略分享平台前端项目。

## 功能特性

- 🗺️ **首页动态流** - 展示旅游线路和景点卡片
- 📝 **线路详情** - 完整的线路规划和景点介绍
- ⭐ **景点点评** - 景点评分和用户评论
- 👤 **个人中心** - 管理发布内容和收藏
- ✍️ **内容发布** - 发布线路和景点信息
- 💬 **评论互动** - 点赞、收藏、评论功能

## 技术栈

- Vue 2.6
- Vue Router 3.x
- Vuex 3.x
- Element-UI 2.15
- Axios
- Sass

## 快速开始

### 安装依赖

```bash
npm install
```

### 开发环境运行

```bash
npm run serve
```

访问 http://localhost:8080

### 生产环境构建

```bash
npm run build
```

## 项目结构

```
src/
├── assets/          # 静态资源
├── components/      # 公共组件
├── layouts/         # 布局组件
├── router/          # 路由配置
├── store/           # Vuex 状态管理
├── views/           # 页面组件
├── App.vue          # 根组件
└── main.js          # 入口文件
```

## API 配置

在 `vue.config.js` 中配置后端 API 代理地址：

```js
proxy: {
  '/api': {
    target: 'http://localhost:3000',  // 后端服务地址
    changeOrigin: true
  }
}
```

## 设计说明

采用青春活力的设计风格，主色调为珊瑚红（#FF6B6B），配合清新的青色（#4ECDC4）作为点缀。界面简洁现代，适合大学生用户群体。

## License

MIT

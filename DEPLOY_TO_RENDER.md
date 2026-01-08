# Render 部署指南

## 前置准备

1. **GitHub 账号**：将项目上传到 GitHub
2. **Render 账号**：访问 https://render.com 注册（可用 GitHub 账号登录）
3. **绑定信用卡**：免费 tier 需要绑定（不会扣费）

## 部署步骤

### 第一步：上传代码到 GitHub

```bash
# 1. 初始化 Git（如果还没有）
git init

# 2. 创建 .gitignore（如果还没有）
# 确保忽略 node_modules、target、.idea 等

# 3. 提交代码
git add .
git commit -m "Initial commit for Render deployment"

# 4. 在 GitHub 创建仓库，然后推送
git remote add origin https://github.com/你的用户名/travel-app.git
git branch -M main
git push -u origin main
```

### 第二步：在 Render 创建数据库

1. 登录 Render Dashboard
2. 点击 "New +" → "PostgreSQL"（免费版只支持 PostgreSQL）
3. 填写信息：
   - Name: `tripmate-db`
   - Database: `tripmate`
   - User: `tripmate_user`
   - Plan: Free
4. 创建后，记下 **Internal Database URL**

### 第三步：修改数据库配置（PostgreSQL）

由于 Render 免费版只支持 PostgreSQL，需要：

1. **修改 pom.xml**：添加 PostgreSQL 依赖
2. **修改 application-render.yml**：使用 PostgreSQL 连接
3. **修改 schema.sql**：适配 PostgreSQL 语法

### 第四步：部署后端服务

1. 在 Render Dashboard 点击 "New +" → "Web Service"
2. 连接 GitHub 仓库
3. 配置：
   - **Name**: `tripmate-backend`
   - **Environment**: `Java`
   - **Build Command**: `cd tripmate-backend && ./mvnw clean package -DskipTests`
   - **Start Command**: `cd tripmate-backend && java -jar target/tripmate-backend-1.0.0.jar --spring.profiles.active=render`
   - **Environment Variables**:
     - `SPRING_PROFILES_ACTIVE` = `render`
     - `DATABASE_URL` = 从数据库服务复制（Internal Database URL）
     - `JWT_SECRET` = 生成随机字符串
     - `QIANWEN_API_KEY` = 你的通义千问 API Key
4. 点击 "Create Web Service"

### 第五步：部署前端

1. 在 Render Dashboard 点击 "New +" → "Static Site"
2. 连接 GitHub 仓库
3. 配置：
   - **Name**: `tripmate-frontend`
   - **Build Command**: `cd trip-mate-travel-app && npm install && npm run build`
   - **Publish Directory**: `trip-mate-travel-app/dist`
   - **Environment Variables**:
     - `VUE_APP_API_BASE_URL` = `https://tripmate-backend.onrender.com/api`
4. 点击 "Create Static Site"

### 第六步：修改前端 API 配置

在 `trip-mate-travel-app/src/utils/axios.js` 中，确保 baseURL 使用环境变量：

```javascript
const service = axios.create({
  baseURL: process.env.VUE_APP_API_BASE_URL || "/api",
  timeout: 60000,
})
```

## 注意事项

### 1. PostgreSQL vs MySQL
- Render 免费版只支持 PostgreSQL
- 需要修改数据库配置和 SQL 语法
- 或者使用付费 MySQL 服务

### 2. 文件上传
- Render 的文件系统是临时的
- 建议使用云存储（如 AWS S3、阿里云 OSS）存储上传的文件

### 3. 应用休眠
- 免费 tier 应用 15 分钟无访问会休眠
- 首次访问需要几秒启动时间

### 4. 数据库迁移
- 需要手动执行 schema.sql 创建表结构
- 可以通过 Render 的数据库控制台执行

## 替代方案：使用 MySQL

如果想继续使用 MySQL，可以考虑：

1. **PlanetScale**：免费 MySQL 服务
2. **Railway**：支持 MySQL，有免费额度
3. **Fly.io**：支持 MySQL，有免费额度

## 快速部署脚本

创建 `deploy.sh`：

```bash
#!/bin/bash
# 部署到 Render 的准备工作

echo "准备部署到 Render..."

# 1. 检查 Java 版本
java -version

# 2. 构建后端
cd tripmate-backend
./mvnw clean package -DskipTests
cd ..

# 3. 构建前端
cd trip-mate-travel-app
npm install
npm run build
cd ..

echo "构建完成！现在可以推送到 GitHub 并在 Render 部署了"
```


# 快速部署到 Render 指南

## ⚠️ 重要提示

**Render 免费版只支持 PostgreSQL，而你的项目使用 MySQL！**

有两个选择：
1. **改用 PostgreSQL**（推荐，免费）
2. **使用其他平台**（Railway、Fly.io 支持 MySQL）

---

## 方案一：使用 Render + PostgreSQL（推荐）

### 步骤 1：修改项目支持 PostgreSQL

#### 1.1 修改 pom.xml 添加 PostgreSQL 依赖

在 `tripmate-backend/pom.xml` 的 `<dependencies>` 中添加：

```xml
<!-- PostgreSQL 驱动 -->
<dependency>
    <groupId>org.postgresql</groupId>
    <artifactId>postgresql</artifactId>
    <scope>runtime</scope>
</dependency>
```

#### 1.2 修改 application-render.yml

已创建 `application-render.yml`，需要修改数据库连接：

```yaml
spring:
  datasource:
    driver-class-name: org.postgresql.Driver
    url: ${DATABASE_URL}
    username: ${DB_USERNAME}
    password: ${DB_PASSWORD}
```

#### 1.3 修改 schema.sql 适配 PostgreSQL

主要差异：
- `AUTO_INCREMENT` → `SERIAL` 或 `BIGSERIAL`
- `TIMESTAMP DEFAULT CURRENT_TIMESTAMP` → `TIMESTAMP DEFAULT NOW()`
- `ENGINE=InnoDB` → 删除（PostgreSQL 不需要）

### 步骤 2：上传到 GitHub

```bash
# 1. 初始化（如果还没有）
git init

# 2. 添加所有文件
git add .

# 3. 提交
git commit -m "Ready for Render deployment"

# 4. 在 GitHub 创建仓库，然后推送
git remote add origin https://github.com/你的用户名/travel-app.git
git branch -M main
git push -u origin main
```

### 步骤 3：在 Render 部署

1. **访问 https://render.com**，用 GitHub 账号登录
2. **创建 PostgreSQL 数据库**：
   - New + → PostgreSQL
   - Name: `tripmate-db`
   - Plan: Free
   - 创建后复制 **Internal Database URL**

3. **部署后端**：
   - New + → Web Service
   - 连接 GitHub 仓库
   - 配置：
     - **Name**: `tripmate-backend`
     - **Environment**: `Java`
     - **Build Command**: `cd tripmate-backend && ./mvnw clean package -DskipTests`
     - **Start Command**: `cd tripmate-backend && java -jar target/tripmate-backend-1.0.0.jar --spring.profiles.active=render`
     - **Environment Variables**:
       - `SPRING_PROFILES_ACTIVE` = `render`
       - `DATABASE_URL` = （从数据库服务复制 Internal Database URL）
       - `JWT_SECRET` = （点击 Generate 生成）
       - `QIANWEN_API_KEY` = `sk-132ad81a355943549e0d0a3da0652f3e`

4. **部署前端**：
   - New + → Static Site
   - 连接 GitHub 仓库
   - 配置：
     - **Name**: `tripmate-frontend`
     - **Build Command**: `cd trip-mate-travel-app && npm install && npm run build`
     - **Publish Directory**: `trip-mate-travel-app/dist`
     - **Environment Variables**:
       - `VUE_APP_API_BASE_URL` = `https://tripmate-backend.onrender.com/api`

5. **执行数据库迁移**：
   - 在 Render Dashboard 找到数据库服务
   - 点击 "Connect" → "psql"
   - 执行修改后的 schema.sql

---

## 方案二：使用 Railway（支持 MySQL）

### 优点
- ✅ 支持 MySQL
- ✅ 有免费额度（$5/月）
- ✅ 自动从 GitHub 部署
- ✅ 更简单

### 步骤

1. **访问 https://railway.app**，用 GitHub 登录
2. **New Project** → **Deploy from GitHub repo**
3. **添加 MySQL 服务**：
   - New → Database → MySQL
4. **部署后端**：
   - New → Service → GitHub Repo
   - 选择仓库
   - 设置：
     - **Root Directory**: `tripmate-backend`
     - **Build Command**: `./mvnw clean package -DskipTests`
     - **Start Command**: `java -jar target/tripmate-backend-1.0.0.jar`
5. **部署前端**：
   - New → Service → Static Site
   - 选择仓库
   - **Root Directory**: `trip-mate-travel-app`
   - **Build Command**: `npm install && npm run build`
   - **Output Directory**: `dist`

---

## 方案三：使用 Fly.io（支持 MySQL）

### 优点
- ✅ 支持 MySQL
- ✅ 有免费额度
- ✅ 全球 CDN

### 步骤

1. **安装 Fly CLI**：`curl -L https://fly.io/install.sh | sh`
2. **登录**：`fly auth login`
3. **初始化**：`fly launch`
4. **部署**：`fly deploy`

---

## 推荐顺序

1. **Railway**（最简单，支持 MySQL）
2. **Render + PostgreSQL**（免费，但需要改数据库）
3. **Fly.io**（功能强大，但配置复杂）

---

## 需要帮助？

告诉我你想用哪个方案，我可以：
1. 生成 PostgreSQL 版本的 schema.sql
2. 创建 Railway 的配置文件
3. 创建 Fly.io 的配置文件


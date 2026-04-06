# Tally 后端部署检查清单

## 项目初始化阶段

### ✅ 已完成的工作

- [x] 创建 Node.js + TypeScript 项目骨架
- [x] 配置 Express 应用框架
- [x] 设置 PostgreSQL 数据库连接
- [x] 创建数据库 Schema（4 张表）
- [x] 实现健康检查端点
- [x] 配置 Docker Compose 环境
- [x] 编写项目文档和快速开始指南

### ⏳ 待完成的工作

- [ ] 安装 npm 依赖包
- [ ] 启动 PostgreSQL 容器
- [ ] 验证开发环境
- [ ] 实现 Phase 2 功能

---

## 环境准备检查表

### 系统要求

- [x] Node.js 18+ 已安装
- [x] npm/yarn 已安装
- [ ] Docker & Docker Compose 已安装（需用户验证）
- [ ] 系统端口 3000 和 5432 可用（需用户验证）

### 代码仓库检查

- [x] 项目位置: `/home/realw/Tally/backend/`
- [x] 所有文件已创建
- [x] Git 忽略规则已配置 (.gitignore)
- [x] 环境变量模板已提供 (.env.example)

### 依赖包清单

**生产依赖** (8 个):
- [x] express@^4.18.2 - Web 框架
- [x] pg@^8.11.3 - PostgreSQL 驱动
- [x] ai@^4.3.15 - AI 模型集成
- [x] @ai-sdk/openai@^1.3.22 - OpenAI SDK
- [x] zod@^3.22.4 - 数据验证
- [x] dotenv@^16.3.1 - 环境变量管理
- [x] cors@^2.8.5 - CORS 中间件
- [x] uuid@^9.0.1 - UUID 生成

**开发依赖** (6 个):
- [x] typescript@^5.3.3 - TypeScript 编译器
- [x] ts-node-dev@^2.0.0 - 开发服务器
- [x] ts-node@^10.9.2 - TypeScript Node 运行时
- [x] @types/* - TypeScript 类型定义

### 数据库 Schema

**表结构**:
- [x] events - 日程表（8 字段）
- [x] expenses - 费用表（6 字段）
- [x] event_expense_links - 关联表
- [x] conversations - 对话历史表

**特性**:
- [x] UUID 主键
- [x] 自动时间戳 (created_at, updated_at)
- [x] 自动更新触发器
- [x] 性能索引

---

## 本地开发环境设置

### 第一步：环境变量配置

**操作步骤**:

```bash
cd /home/realw/Tally/backend

# 复制环境变量模板
cp .env.example .env

# 使用编辑器打开 .env
nano .env  # 或 vi/vim
```

**必填项**:

```env
# 数据库连接（使用 Docker Compose 默认值）
DATABASE_URL=postgresql://tally:tally_dev_password@localhost:5432/tally

# API 密钥（需用户手动填入）
KIMI_API_KEY=sk_xxx...                    # 从月之暗面获取
TAVILY_API_KEY=tvly_xxx...                # 从 Tavily 获取

# 服务端口（可选，默认 3000）
PORT=3000

# 运行环境（可选，默认 development）
NODE_ENV=development
```

**验证清单**:
- [ ] .env 文件已创建
- [ ] 已填入 KIMI_API_KEY
- [ ] 已填入 TAVILY_API_KEY
- [ ] DATABASE_URL 正确无误

### 第二步：启动 PostgreSQL

**前置要求**:
- [ ] Docker 已安装: `docker --version`
- [ ] Docker Compose 已安装: `docker-compose --version`

**启动步骤**:

```bash
cd /home/realw/Tally/backend

# 启动数据库容器
docker-compose up -d

# 验证容器状态
docker-compose ps

# 预期输出:
# NAME                COMMAND             STATUS
# tally-postgres      postgres:16-alpine  Up X seconds (healthy)
```

**故障排查**:

| 问题 | 解决方案 |
|------|---------|
| `Cannot connect to Docker daemon` | 启动 Docker Desktop 或 Docker 服务 |
| `port 5432 is already allocated` | 修改 docker-compose.yml 的端口映射 |
| `ERROR: no such service: postgres` | 确保在 backend 目录下运行命令 |

**验证清单**:
- [ ] docker-compose ps 显示 tally-postgres 为 healthy
- [ ] 可以 ping 数据库: `psql $DATABASE_URL -c "SELECT 1"`

### 第三步：安装依赖包

```bash
cd /home/realw/Tally/backend

# 安装依赖
npm install

# 预期输出:
# added XXX packages in Xs
```

**验证清单**:
- [ ] npm install 成功完成（无 error）
- [ ] node_modules 目录已创建
- [ ] package-lock.json 已生成

### 第四步：启动开发服务器

```bash
cd /home/realw/Tally/backend

# 启动开发服务器（热重载）
npm run dev

# 预期输出:
# ╔════════════════════════════════════════════════════════════╗
# ║         Tally 后端服务已启动                              ║
# ║         http://localhost:3000                              ║
# ╚════════════════════════════════════════════════════════════╝
```

**验证清单**:
- [ ] 服务成功启动（无错误消息）
- [ ] 监听端口 3000

### 第五步：验证 API

**在另一个终端运行**:

```bash
# 测试健康检查端点
curl http://localhost:3000/api/health

# 预期响应 (HTTP 200):
# {
#   "status": "ok",
#   "timestamp": "2026-04-06T...",
#   "database": "connected",
#   "service": "tally-backend"
# }

# 测试根端点
curl http://localhost:3000/

# 预期响应:
# {
#   "name": "Tally Backend",
#   "version": "1.0.0",
#   "description": "对位项目后端服务",
#   "endpoints": {
#     "health": "/api/health",
#     "ready": "/api/ready"
#   }
# }
```

**验证清单**:
- [ ] /api/health 返回 HTTP 200，status 为 "ok"
- [ ] database 字段为 "connected"
- [ ] 时间戳正确

---

## 代码质量检查

### TypeScript 配置

- [x] strict mode 启用
- [x] ES2022 编译目标
- [x] CommonJS 模块系统
- [x] 源文件映射启用
- [x] 未使用变量检测启用

### 代码风格规范

- [x] 所有注释使用中文
- [x] 中英文注释分隔符使用空格
- [x] 函数长度限制 < 50 行
- [x] 文件长度限制 < 800 行
- [x] 缩进使用 2 个空格（TypeScript 标准）

### 安全检查

- [x] 无硬编码 API 密钥
- [x] 环境变量通过 .env 管理
- [x] .gitignore 配置正确
- [x] CORS 中间件已配置
- [x] JSON 请求体大小限制 (10MB)

---

## 生产环境准备

### 构建步骤

```bash
# 编译 TypeScript
npm run build

# 预期输出:
# 输出目录: dist/
# 编译无错误和警告
```

### 启动生产服务器

```bash
# 启动生产环境
npm start

# 预期输出:
# [服务启动信息]
```

### 环境变量配置

**生产环境需要设置**:

```env
DATABASE_URL=postgresql://[user]:[password]@[host]:[port]/[dbname]
KIMI_API_KEY=[production_key]
TAVILY_API_KEY=[production_key]
PORT=3000
NODE_ENV=production
```

### 容器化部署

**Docker 镜像**:

```dockerfile
# Dockerfile (待创建)
FROM node:20-alpine
WORKDIR /app
COPY package*.json ./
RUN npm ci --only=production
COPY dist ./dist
EXPOSE 3000
CMD ["node", "dist/api/index.js"]
```

**Kubernetes 配置** (待创建):
- Deployment
- Service
- ConfigMap (环境变量)
- Secret (敏感数据)
- Probes (liveness, readiness)

---

## Phase 2 开发任务

### 日程域实现

- [ ] 实体定义 (Event 类)
- [ ] Zod 验证 Schema
- [ ] Repository 接口
- [ ] PostgreSQL 实现
- [ ] Service 业务逻辑
- [ ] 单元测试 (>= 80% 覆盖)
- [ ] 集成测试
- [ ] GET /api/events
- [ ] POST /api/events
- [ ] PUT /api/events/:id
- [ ] DELETE /api/events/:id

### 费用域实现

- [ ] 实体定义 (Expense 类)
- [ ] Zod 验证 Schema
- [ ] Repository 接口
- [ ] PostgreSQL 实现
- [ ] Service 业务逻辑
- [ ] 单元测试 (>= 80% 覆盖)
- [ ] 集成测试
- [ ] GET /api/expenses
- [ ] POST /api/expenses
- [ ] PUT /api/expenses/:id
- [ ] DELETE /api/expenses/:id

### AI Agent 模块

- [ ] Agent 框架设计
- [ ] Kimi API 集成
- [ ] Tavily API 集成
- [ ] 对话状态管理
- [ ] Agent 功能测试
- [ ] POST /api/agent/chat
- [ ] WebSocket 支持 (可选)

### 测试框架

- [ ] Jest 配置
- [ ] 测试工具库
- [ ] 单元测试模板
- [ ] 集成测试模板
- [ ] E2E 测试 (Supertest/Playwright)
- [ ] 覆盖率报告

---

## 监控和日志

### 日志配置

- [ ] 请求日志中间件 ✅ 已实现
- [ ] 数据库查询日志 ✅ 已实现
- [ ] 错误日志处理 ✅ 已实现
- [ ] 日志级别 (debug, info, warn, error)

### 性能监控

- [ ] 数据库连接池监控
- [ ] 响应时间指标
- [ ] 错误率追踪
- [ ] API 吞吐量监控

### 健康检查

- [x] /api/health - 服务和数据库状态 ✅ 已实现
- [x] /api/ready - Kubernetes readiness probe ✅ 已实现
- [ ] /api/metrics - Prometheus 指标 (待实现)

---

## 文档维护

### 已创建文档

- [x] README.md - 项目完整文档
- [x] SETUP.md - 快速开始指南
- [x] package.json - 项目配置说明
- [x] tsconfig.json - TypeScript 配置说明
- [x] docker-compose.yml - Docker 配置说明
- [x] migrations/001_initial.sql - 数据库 Schema 文档

### 待创建文档

- [ ] API 文档 (OpenAPI/Swagger)
- [ ] 架构设计文档
- [ ] 开发流程指南
- [ ] 故障排查指南
- [ ] 部署指南

---

## 故障排查快速指南

### npm install 失败

```bash
npm cache clean --force
rm -rf node_modules package-lock.json
npm install --verbose
```

### 数据库连接失败

```bash
# 检查数据库状态
docker-compose ps

# 查看错误日志
docker-compose logs postgres

# 重启数据库
docker-compose restart postgres

# 检查连接字符串
echo $DATABASE_URL
```

### 端口被占用

```bash
# 查看占用进程
lsof -i :3000    # Express 端口
lsof -i :5432    # PostgreSQL 端口

# 修改 docker-compose.yml 中的端口
# 或停止占用该端口的进程
```

### TypeScript 编译错误

```bash
# 检查 TypeScript 版本
npx tsc --version

# 清除编译缓存
rm -rf dist/

# 重新编译
npm run build --verbose
```

---

## 最终检查清单

### 本地开发环境

- [ ] 所有文件已创建在 `/home/realw/Tally/backend/`
- [ ] .env 文件已创建并填入 API 密钥
- [ ] PostgreSQL 容器已启动并显示 healthy
- [ ] npm install 已成功完成
- [ ] npm run dev 启动服务无错误
- [ ] curl /api/health 返回预期响应
- [ ] 可以访问 http://localhost:3000

### 代码质量

- [ ] 所有 TypeScript 文件通过类型检查
- [ ] 无 linter 警告
- [ ] 注释清晰完整
- [ ] 代码遵循编码规范

### 文档完整性

- [ ] README.md 已阅读
- [ ] SETUP.md 已按步骤执行
- [ ] .env.example 已理解
- [ ] 数据库 Schema 已理解

---

## 联系和支持

- **项目主文档**: `/home/realw/Tally/CLAUDE.md`
- **后端文档**: `/home/realw/Tally/backend/README.md`
- **快速开始**: `/home/realw/Tally/backend/SETUP.md`
- **全局规则**: `~/.claude/rules/common/`

---

**最后更新**: 2026-04-06  
**项目状态**: 骨架完成，等待 Phase 2 开发

# Tally 后端服务

Node.js + TypeScript 构建的对位项目后端服务。

## 项目结构

```
backend/
├── src/
│   ├── agents/              # AI Agent 模块（Phase 2 填充）
│   ├── domains/
│   │   ├── schedule/        # 日程域
│   │   └── expense/         # 费用域
│   ├── infrastructure/
│   │   └── db.ts            # PostgreSQL 连接管理
│   └── api/
│       ├── index.ts         # Express 应用入口
│       └── routes/
│           └── health.ts    # 健康检查路由
├── migrations/
│   └── 001_initial.sql      # 数据库初始 Schema
├── package.json
├── tsconfig.json
├── docker-compose.yml
├── .env.example
└── .gitignore
```

## 快速开始

### 前置要求

- Node.js 18+
- Docker & Docker Compose
- PostgreSQL 16 (通过 Docker 启动)

### 环境配置

1. 复制环境变量文件：
```bash
cp .env.example .env
```

2. 编辑 `.env` 文件，填入实际的 API 密钥：
```bash
KIMI_API_KEY=your_actual_kimi_key_here
TAVILY_API_KEY=your_actual_tavily_key_here
```

### 启动数据库

```bash
# 启动 PostgreSQL
docker-compose up -d

# 验证数据库就绪
docker-compose ps
# 应显示 postgres 状态为 healthy
```

### 开发环境运行

```bash
# 安装依赖
npm install

# 启动开发服务器（带热重载）
npm run dev

# 在另一个终端测试 API
curl http://localhost:3000/api/health
```

### 生产环境构建

```bash
# 编译 TypeScript
npm run build

# 启动生产服务器
npm start
```

## API 端点

### 健康检查

```bash
GET /api/health
# 响应:
# {
#   "status": "ok",
#   "timestamp": "2026-04-06T...",
#   "database": "connected",
#   "service": "tally-backend"
# }
```

### 就绪检查

```bash
GET /api/ready
# 用于 Kubernetes readiness probe
```

## 数据库

### 初始化

数据库会在 Docker Compose 启动时自动初始化，运行 `migrations/001_initial.sql` 中的 SQL 脚本。

### 表结构

- **events**: 日程表
- **expenses**: 费用表
- **event_expense_links**: 日程-费用关联表
- **conversations**: AI Agent 对话历史

### 手动迁移

```bash
npm run migrate
```

## 开发指南

### 代码风格

- 使用 TypeScript strict mode
- 中文注释
- 最大行数限制: 800 行/文件
- 最大函数长度: 50 行
- 不使用 `any` 类型

### TDD 工作流

1. 编写测试 (RED)
2. 编写最小实现 (GREEN)
3. 重构优化 (REFACTOR)
4. 验证覆盖率 >= 80%

### 提交规范

```
<type>: <description>

Optional body
```

类型: `feat`, `fix`, `refactor`, `docs`, `test`, `chore`, `perf`, `ci`

## Phase 2 计划

- [ ] AI Agent 模块实现
- [ ] 日程域功能完整实现
- [ ] 费用域功能完整实现
- [ ] REST API 端点
- [ ] 集成测试
- [ ] E2E 测试

## 环境变量说明

| 变量 | 说明 | 示例 |
|------|------|------|
| `DATABASE_URL` | PostgreSQL 连接字符串 | `postgresql://tally:password@localhost:5432/tally` |
| `KIMI_API_KEY` | 月之暗面 Kimi API 密钥 | - |
| `KIMI_BASE_URL` | Kimi API 基础 URL | `https://api.moonshot.cn/v1` |
| `KIMI_MODEL` | Kimi 模型名称 | `kimi-k2.5` |
| `TAVILY_API_KEY` | Tavily 网络搜索 API 密钥 | - |
| `PORT` | 服务监听端口 | `3000` |
| `NODE_ENV` | 运行环境 | `development` 或 `production` |

## 故障排查

### 数据库连接失败

```bash
# 检查 PostgreSQL 状态
docker-compose ps

# 查看日志
docker-compose logs postgres

# 重启数据库
docker-compose restart postgres
```

### npm install 失败

```bash
# 清除缓存
npm cache clean --force

# 重新安装
npm install
```

### 开发服务器无法启动

1. 检查 PORT 是否被占用
2. 验证 `.env` 文件中的 `DATABASE_URL` 配置
3. 检查 `node_modules` 是否完整：`rm -rf node_modules && npm install`

## 相关文档

- [项目主文档](/home/realw/Tally/CLAUDE.md)
- [全局开发规则](~/.claude/rules/common/)

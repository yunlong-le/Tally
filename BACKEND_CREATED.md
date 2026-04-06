# Tally 后端项目骨架已创建

**创建时间**: 2026-04-06  
**创建位置**: `/home/realw/Tally/backend/`

## 创建清单

### ✅ 目录结构

```
backend/
├── src/
│   ├── agents/                          (空目录 - Phase 2 填充)
│   ├── domains/
│   │   ├── schedule/                    (空目录)
│   │   └── expense/                     (空目录)
│   ├── infrastructure/
│   │   └── db.ts                        ✓ PostgreSQL 连接管理
│   └── api/
│       ├── index.ts                     ✓ Express 应用入口
│       ├── types.ts                     ✓ API 响应类型定义
│       └── routes/
│           └── health.ts                ✓ 健康检查路由
├── migrations/
│   └── 001_initial.sql                  ✓ 数据库 Schema (4 张表)
├── package.json                         ✓ 项目依赖配置
├── tsconfig.json                        ✓ TypeScript 配置
├── docker-compose.yml                   ✓ PostgreSQL Docker 配置
├── .env.example                         ✓ 环境变量模板
├── .gitignore                           ✓ Git 忽略规则
├── README.md                            ✓ 项目文档
└── SETUP.md                             ✓ 快速开始指南
```

### ✅ package.json 配置

- **依赖 8 个**: express, pg, ai, zod, dotenv, cors, uuid, @ai-sdk/openai
- **开发依赖 6 个**: typescript, ts-node, ts-node-dev, @types/* 包
- **npm 脚本 4 个**: dev, build, start, migrate

### ✅ 数据库 Schema

创建了 4 张表：

1. **events** - 日程表
   - 字段: id, title, start_time, end_time, location, description, created_at, updated_at
   - 索引: start_time, created_at

2. **expenses** - 费用表
   - 字段: id, description, amount, category, expense_date, created_at
   - 索引: expense_date, category

3. **event_expense_links** - 跨域关联
   - 字段: event_id, expense_id (主键), created_at
   - 关联: 外键指向 events 和 expenses

4. **conversations** - AI 对话历史
   - 字段: id, messages (JSONB), metadata (JSONB), created_at, updated_at
   - 索引: created_at

所有表自动维护 `updated_at` 时间戳。

### ✅ API 端点

已实现：
- `GET /` - 根端点，显示服务信息
- `GET /api/health` - 健康检查（包含数据库状态）
- `GET /api/ready` - 就绪检查（Kubernetes probe）

### ✅ 开发环境配置

- **TypeScript**: strict mode, ES2022 target
- **Express**: CORS、JSON 解析、错误处理
- **PostgreSQL**: 连接池（max 20 连接）
- **日志**: 请求日志、数据库查询时间

### ✅ 文档

- `README.md` - 项目完整文档
- `SETUP.md` - 快速开始指南
- `src/api/types.ts` - API 类型定义（已注释）

## 使用指南

### 第 1 步: 环境配置

```bash
cd /home/realw/Tally/backend

# 复制环境变量模板
cp .env.example .env

# 编辑 .env，填入真实的 API 密钥
# 注意: .env 文件在 git 中被忽略（不会被提交）
```

**必需的环境变量**:
```
DATABASE_URL=postgresql://tally:tally_dev_password@localhost:5432/tally
KIMI_API_KEY=sk_xxx...              # 月之暗面 Kimi API 密钥
TAVILY_API_KEY=tvly_xxx...          # Tavily 网络搜索密钥
PORT=3000
```

### 第 2 步: 启动数据库

```bash
# 启动 PostgreSQL 容器
docker-compose up -d

# 验证数据库状态
docker-compose ps
# 应显示 postgres 容器状态为 healthy

# 数据库会自动初始化为:
# - 数据库: tally
# - 用户: tally
# - 密码: tally_dev_password
```

### 第 3 步: 安装依赖

```bash
npm install
# 将安装 ~60 个依赖包到 node_modules/
```

### 第 4 步: 启动开发服务器

```bash
npm run dev
# 应输出:
# ╔════════════════════════════════════════════════════════════╗
# ║         Tally 后端服务已启动                              ║
# ║         http://localhost:3000                              ║
# ╚════════════════════════════════════════════════════════════╝
```

### 第 5 步: 验证服务

```bash
# 在另一个终端运行
curl http://localhost:3000/api/health

# 预期响应:
# {
#   "status": "ok",
#   "timestamp": "2026-04-06T...",
#   "database": "connected",
#   "service": "tally-backend"
# }
```

## 项目架构

### 分层设计

```
API Layer (Express Routes)
    ↓
Service Layer (业务逻辑)
    ↓
Domain Layer (实体、仓储、规则)
    ↓
Infrastructure Layer (数据库、外部服务)
```

### 目录组织

- **agents/** - AI Agent 模块（Phase 2）
- **domains/** - DDD 域模型
  - schedule/ - 日程域
  - expense/ - 费用域
- **infrastructure/** - 基础设施（数据库、第三方 API）
- **api/** - 路由和控制器

## 代码质量要求

遵循 `~/.claude/rules/common/` 中的全局规则：

- TypeScript strict mode
- 最大行数: 800 行/文件
- 最大函数: 50 行/函数
- 不可变性设计
- TDD 工作流: RED → GREEN → REFACTOR
- 最小测试覆盖: 80%

## Phase 2 任务清单

### 日程域 (domains/schedule/)
- [ ] Event 实体 (Zod 验证)
- [ ] EventRepository (接口 + PostgreSQL 实现)
- [ ] EventService (业务逻辑)
- [ ] CRUD 测试 (单元 + 集成)
- [ ] `/api/events` 路由

### 费用域 (domains/expense/)
- [ ] Expense 实体 (Zod 验证)
- [ ] ExpenseRepository (接口 + PostgreSQL 实现)
- [ ] ExpenseService (业务逻辑)
- [ ] CRUD 测试 (单元 + 集成)
- [ ] `/api/expenses` 路由

### AI Agent 模块 (agents/)
- [ ] Agent 类框架
- [ ] Kimi API 集成
- [ ] Tavily 网络搜索集成
- [ ] 对话状态管理
- [ ] `/api/agent/chat` 端点
- [ ] Agent 功能测试

### 测试
- [ ] Jest 配置
- [ ] 单元测试套件
- [ ] 集成测试套件
- [ ] E2E 测试 (关键流程)
- [ ] 覆盖率报告 (>= 80%)

## 常见命令

```bash
# 开发
npm run dev                 # 启动开发服务器

# 构建
npm run build              # 编译 TypeScript 为 JavaScript
npm start                  # 启动生产服务器

# 数据库
npm run migrate            # 手动运行迁移脚本

# Docker 管理
docker-compose up -d       # 启动数据库
docker-compose down        # 停止数据库
docker-compose ps          # 查看容器状态
docker-compose logs        # 查看日志
docker-compose restart     # 重启数据库
```

## 下一步

1. **按照 SETUP.md 进行快速开始**
   - 配置 .env
   - 启动 PostgreSQL
   - 安装依赖
   - 运行服务

2. **验证开发环境**
   - curl /api/health
   - 检查数据库连接

3. **开始 Phase 2 开发**
   - 使用 planner agent 制定计划
   - 使用 tdd-guide agent 进行 TDD
   - 使用 code-reviewer agent 进行代码审查

## 重要提示

⚠️ **不要提交 .env 文件到 Git**  
- `.env` 包含敏感的 API 密钥
- 已在 `.gitignore` 中排除
- 使用 `.env.example` 作为模板

⚠️ **npm install 前确保 Docker 已启动**  
- 某些依赖可能需要网络访问

⚠️ **开发过程中遵循 TDD**
- 先写测试，再实现代码
- 维持 80% 以上的覆盖率

## 文档位置

- 项目主文档: `/home/realw/Tally/CLAUDE.md`
- 后端项目文档: `/home/realw/Tally/backend/README.md`
- 快速开始: `/home/realw/Tally/backend/SETUP.md`
- 全局规则: `~/.claude/rules/common/`
- TypeScript 规则: `~/.claude/rules/typescript/` (如果已安装)

---

**项目状态**: ✅ 骨架完成，待 Phase 2 开发

**后续操作**: 参考 `/home/realw/Tally/backend/SETUP.md` 进行环境设置和验证

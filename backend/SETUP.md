# Tally 后端项目快速设置指南

## 项目已创建

✓ 完整的 Node.js + TypeScript 项目骨架已在 `/home/realw/Tally/backend/` 创建

## 目录结构确认

```
backend/
├── src/
│   ├── agents/                      # AI Agent 模块（Phase 2 填充）
│   ├── domains/
│   │   ├── schedule/                # 日程域（空目录）
│   │   └── expense/                 # 费用域（空目录）
│   ├── infrastructure/
│   │   └── db.ts                    # PostgreSQL 连接管理
│   └── api/
│       ├── index.ts                 # Express 应用入口
│       └── routes/
│           └── health.ts            # 健康检查路由
├── migrations/
│   └── 001_initial.sql              # 数据库初始 Schema (4 张表)
├── package.json                     # 项目依赖配置
├── tsconfig.json                    # TypeScript 编译配置
├── docker-compose.yml               # Docker Compose 配置
├── .env.example                     # 环境变量示例
├── .gitignore                       # Git 忽略规则
├── README.md                        # 项目文档
└── SETUP.md                         # 本文件
```

## 文件说明

### 核心文件

| 文件 | 说明 |
|------|------|
| `package.json` | 依赖配置 + npm 脚本 (dev, build, start, migrate) |
| `tsconfig.json` | strict mode + ES2022 target |
| `docker-compose.yml` | PostgreSQL 16 + 自动初始化 |
| `.env.example` | API keys 和数据库配置模板 |

### 源代码

| 文件 | 说明 |
|------|------|
| `src/api/index.ts` | Express 应用、CORS、JSON 解析、路由注册 |
| `src/api/routes/health.ts` | `/api/health` 和 `/api/ready` 端点 |
| `src/infrastructure/db.ts` | PostgreSQL 连接池、query 函数 |

### 数据库

| 表 | 用途 |
|-------|------|
| `events` | 日程数据（日期、时间、位置等） |
| `expenses` | 费用数据（金额、分类、日期等） |
| `event_expense_links` | 日程与费用的关联 |
| `conversations` | AI Agent 对话历史记录 |

## 立即开始

### 1. 手动环境设置 (必需步骤)

创建 `.env` 文件（不包含在 git 中以保护密钥）：

```bash
cd /home/realw/Tally/backend
cp .env.example .env

# 编辑 .env 文件，填入真实的 API 密钥
# KIMI_API_KEY=sk_xxx...
# TAVILY_API_KEY=tvly_xxx...
```

### 2. 启动 PostgreSQL

```bash
cd /home/realw/Tally/backend

# 启动数据库容器
docker-compose up -d

# 等待数据库就绪（应显示 healthy）
docker-compose ps

# 查看日志（可选）
docker-compose logs postgres
```

### 3. 安装依赖

```bash
npm install

# 输出应该显示 ~60 个依赖包
```

### 4. 启动开发服务器

```bash
npm run dev

# 输出应该显示:
# ╔════════════════════════════════════════════════════════════╗
# ║         Tally 后端服务已启动                              ║
# ║         http://localhost:3000                              ║
# ╚════════════════════════════════════════════════════════════╝
```

### 5. 验证服务

在另一个终端运行：

```bash
# 测试健康检查端点
curl http://localhost:3000/api/health

# 应返回:
# {
#   "status": "ok",
#   "timestamp": "2026-04-06T...",
#   "database": "connected",
#   "service": "tally-backend"
# }
```

## API 设计规范

### 已实现

- ✓ `/api/health` - 服务和数据库状态
- ✓ `/api/ready` - Kubernetes readiness probe

### Phase 2 计划

- [ ] `/api/events` - 日程 CRUD 操作
- [ ] `/api/expenses` - 费用 CRUD 操作
- [ ] `/api/agent/chat` - AI Agent 对话接口
- [ ] `/api/agent/analyze` - 数据分析接口

## 开发工作流

### 代码风格要求

遵循全局规则：`~/.claude/rules/common/`

- TypeScript strict mode
- 最大行数: 800 行/文件
- 最大函数: 50 行/函数
- 不可变性设计（避免 mutation）
- 中文注释

### TDD 流程

```
1. 编写测试 (RED) - 测试应失败
2. 实现代码 (GREEN) - 实现最小逻辑使测试通过
3. 重构优化 (REFACTOR) - 优化代码结构
4. 验证覆盖率 >= 80%
```

### 提交规范

```
feat: 添加新功能
fix: 修复 bug
refactor: 代码重构
docs: 文档更新
test: 测试相关
chore: 依赖更新
perf: 性能优化
ci: CI/CD 配置
```

## 故障排查

### 问题 1: npm install 失败

```bash
# 解决方案
npm cache clean --force
rm -rf node_modules package-lock.json
npm install
```

### 问题 2: Docker 找不到

```bash
# 检查 Docker 是否安装
docker --version
docker-compose --version

# 如果未安装，参考 Docker 官方文档
```

### 问题 3: 端口 5432 已被占用

```bash
# 检查占用进程
lsof -i :5432
# 或修改 docker-compose.yml 中的端口映射
```

### 问题 4: 数据库连接失败

```bash
# 检查数据库状态
docker-compose ps

# 查看数据库日志
docker-compose logs postgres

# 重启数据库
docker-compose restart postgres

# 重新初始化（删除数据）
docker-compose down -v
docker-compose up -d
```

## 依赖包说明

### 运行时依赖

- `express` - Web 框架
- `pg` - PostgreSQL 驱动
- `ai` + `@ai-sdk/openai` - AI 模型集成
- `zod` - 数据验证
- `dotenv` - 环境变量管理
- `cors` - CORS 中间件
- `uuid` - UUID 生成

### 开发依赖

- `typescript` - TypeScript 编译器
- `ts-node-dev` - 热重载开发服务器
- `@types/*` - TypeScript 类型定义

## 下一步

### Phase 2 任务

1. **日程域实现**
   - 创建 `src/domains/schedule/` 下的实体、仓储、服务
   - 实现 `/api/events` 路由

2. **费用域实现**
   - 创建 `src/domains/expense/` 下的实体、仓储、服务
   - 实现 `/api/expenses` 路由

3. **AI Agent 模块**
   - 在 `src/agents/` 中实现 Agent 类
   - 集成 Kimi 和 Tavily APIs
   - 创建 `/api/agent/chat` 端点

4. **测试覆盖**
   - 单元测试 (jest)
   - 集成测试 (API 端点)
   - E2E 测试 (关键用户流程)

## 参考文档

- 项目指南: `/home/realw/Tally/CLAUDE.md`
- 全局规则: `~/.claude/rules/common/`
- TypeScript 规则: `~/.claude/rules/typescript/` (如果已安装)
- Express 模式: 使用 code-reviewer agent 进行审查

## 常用命令

```bash
# 开发
npm run dev              # 启动开发服务器

# 构建
npm run build            # 编译 TypeScript
npm start               # 启动生产服务器

# 数据库
npm run migrate         # 运行迁移脚本

# Docker
docker-compose up -d    # 启动 PostgreSQL
docker-compose down     # 停止 PostgreSQL
docker-compose ps       # 查看容器状态
docker-compose logs     # 查看日志
```

## 许可证

MIT

---

**项目创建时间**: 2026-04-06  
**后端框架**: Express.js (Node.js)  
**数据库**: PostgreSQL 16  
**语言**: TypeScript  

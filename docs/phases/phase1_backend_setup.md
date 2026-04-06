# Phase 1: 基础架构搭建

**状态**: 🔄 进行中  
**目标**: 可运行的后端骨架 + Android 空壳项目

## 任务清单

- ✅ 1.7 更新 CLAUDE.md
- ✅ 1.8 创建进度追踪文档
- ⬜ 1.1 初始化 Node.js + TypeScript 后端项目
- ⬜ 1.2 Docker Compose 配置（PostgreSQL）
- ⬜ 1.3 数据库 Schema
- ⬜ 1.4 数据库连接层
- ⬜ 1.5 REST API 框架
- ⬜ 1.6 Android 项目初始化

## 验收标准

- `docker-compose up` 能正常启动 PostgreSQL
- `npm run dev` 能启动后端服务，`GET /health` 返回 200
- Android 项目能用 Gradle 编译成功（`./gradlew assembleDebug`）
- Android 模拟器能运行起来显示主界面（空白占位即可）

## 技术细节

### 后端依赖
```json
{
  "dependencies": {
    "express": "^4.18.x",
    "pg": "^8.x",
    "ai": "^4.x",
    "@ai-sdk/openai": "^1.x",
    "zod": "^3.x",
    "dotenv": "^16.x"
  },
  "devDependencies": {
    "typescript": "^5.x",
    "@types/express": "^4.x",
    "@types/pg": "^8.x",
    "ts-node-dev": "^2.x"
  }
}
```

### 数据库 Schema
```sql
CREATE TABLE events (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    title TEXT NOT NULL,
    start_time TIMESTAMPTZ NOT NULL,
    end_time TIMESTAMPTZ,
    location TEXT,
    description TEXT,
    created_at TIMESTAMPTZ DEFAULT NOW(),
    updated_at TIMESTAMPTZ DEFAULT NOW()
);

CREATE TABLE expenses (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    description TEXT NOT NULL,
    amount DECIMAL(10,2) NOT NULL,
    category TEXT,
    expense_date TIMESTAMPTZ NOT NULL,
    created_at TIMESTAMPTZ DEFAULT NOW()
);

CREATE TABLE event_expense_links (
    event_id UUID REFERENCES events(id) ON DELETE CASCADE,
    expense_id UUID REFERENCES expenses(id) ON DELETE CASCADE,
    PRIMARY KEY (event_id, expense_id)
);

CREATE TABLE conversations (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    messages JSONB NOT NULL DEFAULT '[]',
    created_at TIMESTAMPTZ DEFAULT NOW(),
    updated_at TIMESTAMPTZ DEFAULT NOW()
);
```

### Android 项目配置
- minSdk: 26, targetSdk: 35, compileSdk: 35
- 语言: Kotlin
- UI: Jetpack Compose
- 依赖: Retrofit, OkHttp, Kotlinx Coroutines, ViewModel

## 注意事项

- Docker Desktop 需在 Windows 端启动后，WSL 才能使用 docker 命令
- WSL 局域网 IP 获取：`ip addr show eth0 | grep inet`
- Android 连接后端地址：`http://<WSL_IP>:3000`

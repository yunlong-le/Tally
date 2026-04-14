# Phase 6 — 阿里云 ECS 部署

**状态**: 🟡 待实施  
**目标**: 将 Tally 后端服务部署到阿里云 ECS，使 Android App 可连接线上后端  
**方案确认日期**: 2026-04-11

---

## 已确认决策

| 决策项 | 结论 |
|--------|------|
| SSL | 暂无域名，先 HTTP 裸 80 端口部署验证，后续有域名再加 |
| 代码上传方式 | 本地 rsync 上传到 ECS（不走公开 git 仓库） |
| 部署方式 | docker compose，一条命令启动全部服务 |

---

## 服务器信息

| 项目 | 信息 |
|------|------|
| SSH 别名 | `ssh aliclaude` |
| 系统 | Ubuntu 22.04 LTS |
| vCPU | 2核 |
| 内存 | 1.6G（标称2G） |
| 磁盘 | 40G（已用5.6G） |
| Docker | ✅ v29.3.0 已安装 |
| docker-compose | ⬜ 未安装，需安装 v2.x |
| 已开放端口 | 22 (SSH), 80 (HTTP) |

---

## 最终架构

```
阿里云 ECS (2核 1.6G)
  ├── Nginx alpine (PORT 80 对外)
  │     └── 反向代理 → backend:3000（关闭缓冲支持SSE流式）
  ├── Node.js backend (PORT 3000 内网)
  │     └── 连接 → postgres:5432
  └── PostgreSQL 16-alpine (PORT 5432 内网)
```

---

## Kimi 需要新建/修改的文件

> 当前 `backend/docker-compose.yml` 只定义了 postgres 服务，需要扩展。

### 需要新建

1. **`backend/Dockerfile`** — 多阶段构建
   - Stage 1 (builder): `node:20-alpine`，`npm ci`，`npm run build`
   - Stage 2 (runner): 只安装生产依赖，复制 dist/ 和 migrations/
   - 必须设置 `ENV NODE_OPTIONS=--max-old-space-size=512`（1.6G 内存限制 Node 堆，防 OOM）
   - `EXPOSE 3000`，`CMD ["node", "dist/api/index.js"]`

2. **`backend/nginx/nginx.conf`** — HTTP 反向代理
   - `listen 80`，`server_name _`
   - `proxy_pass http://backend:3000`（服务名，不是 localhost）
   - **关键**：必须设置 `proxy_buffering off` — 否则 AI 流式对话 SSE 会卡住等到最后才推送
   - `proxy_read_timeout 300s`（AI 对话响应时间长）
   - 限流：`limit_req_zone ... rate=10r/s`，`burst=20`

### 需要替换（先备份原文件）

3. **`backend/docker-compose.yml`** — 从1个服务扩展为3个服务
   - 保留原有 `postgres` 服务配置
   - 新增 `backend` 服务：`build: .`，`depends_on: postgres (condition: service_healthy)`
   - 新增 `nginx` 服务：`ports: "80:80"`，挂载 `./nginx/nginx.conf`
   - **关键**：`backend` 服务的 `DATABASE_URL` host 必须是 `postgres`（docker 服务名），不是 `localhost`
   - **关键**：三个服务都加日志限制 `max-size: "10m", max-file: "3"`（否则会把 40G 磁盘写满）
   - 所有密钥通过 `${VAR}` 从宿主机环境变量读取，不硬编码

---

## 部署执行步骤（供 Kimi 参考，需用户在服务器上实操）

### 本地（开发机）
```bash
# 验证本地能编译
cd /home/realw/Tally/backend
docker build -t tally-backend-test .
docker rmi tally-backend-test

# 上传代码（排除不需要的目录）
rsync -avz --exclude='node_modules' --exclude='dist' --exclude='.env' \
    /home/realw/Tally/backend/ \
    aliclaude:/opt/tally/
```

### 服务器（SSH 进入后执行）
```bash
# 安装 docker-compose v2
sudo apt-get update && sudo apt-get install -y docker-compose-plugin
docker compose version  # 验证

# 创建部署目录
sudo mkdir -p /opt/tally && sudo chown $USER:$USER /opt/tally

# 创建生产 .env（手动填入真实密钥）
# 注意：POSTGRES_PASSWORD 必须与 DATABASE_URL 中一致，使用强密码
nano /opt/tally/.env
chmod 600 /opt/tally/.env

# 启动
cd /opt/tally
set -a && source .env && set +a
docker compose up --build -d
docker compose ps  # 验证三个服务都 Up
```

### 验证
```bash
# 服务器上
curl http://localhost/api/health
# 期望：{"success":true,"data":{"status":"ok","database":"connected",...}}

# 本地或手机
curl http://ECS_PUBLIC_IP/api/health
```

---

## 关键注意事项

| 项目 | 正确做法 | 错误做法 |
|------|---------|---------|
| DATABASE_URL host | `postgres`（服务名） | `localhost` / `127.0.0.1` |
| Nginx proxy_buffering | `off` | 默认开启（SSE 会卡） |
| Node 内存 | `NODE_OPTIONS=--max-old-space-size=512` | 不设置（可能 OOM） |
| 日志 | 每服务加 max-size 限制 | 不配置（磁盘写满） |
| .env 文件 | 服务器上手动创建，不进 git | 提交到 git / 复制进镜像 |

---

## 部署完成后验证清单

- [ ] `docker compose ps` 三个服务均为 Up (healthy/running)
- [ ] `curl http://localhost/api/health` 返回 database: connected
- [ ] 公网 IP 80 端口可访问
- [ ] Android App 改后端地址为 `http://ECS_PUBLIC_IP`，重新编译，跑一遍完整对话流程
  - 关键路径：搜索 `android/` 目录中的 `localhost` 或 `BASE_URL` 常量

---

## 当前执行状态

| 步骤 | 状态 |
|------|------|
| 创建 backend/Dockerfile | ✅ 已完成 |
| 创建 backend/nginx/nginx.conf | ✅ 已完成 |
| 替换 backend/docker-compose.yml | ✅ 已完成 |
| 本地 docker build 验证 | ✅ 已完成 |
| ECS 安装 docker-compose v2 | ✅ 已完成 |
| rsync 上传代码 | ✅ 已完成 |
| 服务器配置 .env | ✅ 已完成 |
| docker compose up --build | ✅ 已完成 |
| 健康检查验证 | ✅ 已通过 |
| Android App 连接验证 | 🟡 部分通过 |

## 部署结果

| 功能 | 状态 | 备注 |
|------|------|------|
| 后端健康检查 | ✅ 正常 | http://8.140.192.167/api/health |
| Nginx 限流 | ✅ 正常 | 无UA拒绝403，敏感路径404 |
| 聊天功能 | ✅ 正常 | AI回复流式正常 |
| 新建日程 | ✅ 正常 | 数据库表已创建 |
| 日历页面显示 | 🟡 待验证 | 已添加错误显示UI |
| 费用页面显示 | 🟡 待验证 | 已添加错误显示UI |
| 补充标题后卡住 | ❌ Bug | App卡住后退出，待排查 |

## 待办事项

1. **验证日历/费用页面** - 安装最新APK查看错误信息
2. **排查卡住Bug** - 需要Windows端Claude用adb logcat捕获崩溃日志
3. **后端日志监控** - 如有异常查看 `docker compose logs backend`

## 已知问题

### Bug: 补充标题后App卡住退出
- **现象**: AI询问补充标题，用户提供标题后App卡住然后退出
- **复现步骤**: 新建日程 → AI要求补充标题 → 输入标题 → 卡住退出
- **排查方向**: 
  - Android端：查看ANR或崩溃日志
  - 后端：检查SSE流是否正常关闭
  - 可能原因：流式响应挂起、状态更新死循环

## 服务器信息

| 项目 | 值 |
|------|-----|
| 公网IP | 8.140.192.167 |
| SSH | ssh aliclaude |
| 部署目录 | /opt/tally |
| 日志查看 | docker compose logs -f backend |

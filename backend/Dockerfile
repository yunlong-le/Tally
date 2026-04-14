# Tally Backend Dockerfile
# 多阶段构建，优化镜像大小和内存占用
# ECS 上使用淘宝镜像加速 npm install

# Stage 1: Builder
FROM node:20-alpine AS builder

WORKDIR /app

# 使用淘宝镜像加速
RUN npm config set registry https://registry.npmmirror.com

# 复制 package.json 并安装依赖
COPY package*.json ./
RUN npm ci

# 复制源码并构建
COPY . .
RUN npm run build

# Stage 2: Runtime
FROM node:20-alpine AS runner

WORKDIR /app

# 设置生产环境变量
ENV NODE_ENV=production
# 限制 Node.js 堆内存为 512MB（ECS 只有 1.6G 内存）
ENV NODE_OPTIONS=--max-old-space-size=512

# 使用淘宝镜像加速
RUN npm config set registry https://registry.npmmirror.com

# 复制生产依赖
COPY package*.json ./
RUN npm ci --omit=dev && npm cache clean --force

# 复制构建产物
COPY --from=builder /app/dist ./dist
COPY --from=builder /app/migrations ./migrations

# 暴露端口
EXPOSE 3000

# 健康检查
HEALTHCHECK --interval=30s --timeout=3s --start-period=5s --retries=3 \
    CMD node -e "require('http').get('http://localhost:3000/api/health', (r) => {process.exit(r.statusCode === 200 ? 0 : 1)})"

# 启动命令
CMD ["node", "dist/api/index.js"]

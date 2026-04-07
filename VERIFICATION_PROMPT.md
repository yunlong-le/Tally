# Windows Claude Code 真机验证提示词

## 项目背景

**项目名**: Tally（对位）— AI 日程和费用管理 Android App  
**阶段**: Phase 4.7 WebView Bridge 功能验证  
**日期**: 2026-04-07  
**前置**: Linux WSL 端已完成代码实现和 APK 编译

---

## 当前状态

### ✅ 已完成的功能

#### Phase 1-3（已验证）
- 后端：Node.js + Vercel AI SDK + Kimi 模型，ChatAPI 流式接口
- Android 聊天界面：Compose UI + StateFlow 状态管理 + OkHttp 流式客户端
- 真机验证：小米17 手机，adb reverse 隧道连接

#### Phase 4（日历/费用 WebView）✅ 已验证
- React + Vite WebView UI：月历视图、费用列表
- WebView 资源加载：Android assets → HTTPS appassets 映射
- 后端 API：`GET /api/events`、`GET /api/expenses`
- 真机验证：日历月历网格显示、费用数据加载

#### Phase 4.7（WebView Bridge）✅ 刚完成
**新增功能**: 点击日历/费用条目 → 自动跳转聊天 Tab + 预填上下文消息

**实现细节**:
- 新建: `TallyJsBridge.kt` — JavaScript → Android 桥接类
- 修改: `NavGraph.kt` — 注册 Bridge，共享 ChatViewModel，写入导航回调
- 修改: `ChatViewModel.kt` — 新增 `prefillInput(text)` 方法
- 修改: `CalendarView.tsx` — event-item 点击调用 `Android.navigateToChat(msg)`
- 修改: `ExpenseView.tsx` — expense-item 点击调用 `Android.navigateToChat(msg)`

**APK 文件**: `C:\Users\realw\Desktop\tally.apk`（已生成）

---

## 验证环境要求

### 硬件
- Android 手机（推荐小米17 或类似）
- USB 数据线
- Windows 10/11 PC

### 软件
- ADB（随 Android SDK 提供，或独立安装）
- 后端服务：WSL `localhost:3000`（通过 adb reverse 隧道转发）

---

## 验证检查清单

### 1. 环境准备

- [ ] 手机连接 Windows USB
- [ ] 手机开启开发者模式 + USB调试
- [ ] 运行 `adb devices` 确认手机已连接
- [ ] WSL 后端已启动：`npm run dev`（监听 `0.0.0.0:3000`）
- [ ] 建立 adb 隧道：`adb reverse tcp:3000 tcp:3000`

### 2. 安装 APK

- [ ] 卸载旧版本（如有）：`adb uninstall com.tally.app`
- [ ] 安装新 APK：`adb install -r C:\Users\realw\Desktop\tally.apk`
- [ ] 安装完成后启动 App

### 3. 聊天功能验证（基础，确保未破坏）

- [ ] 打开 App，进入聊天 Tab（默认）
- [ ] 输入任意消息，例如"今天几号"，点击发送
- [ ] 预期：收到 AI 回复，流式渲染消息
- [ ] 如果失败，检查:
  - adb logcat: `adb logcat | grep -i chat`
  - 是否有网络错误或 timeout

### 4. 日历功能验证（WebView Bridge 关键）

**步骤:**
1. 点击底部导航栏 "日历" Tab
2. 确认日历月历网格显示（无需数据，只看布局）
3. 如果有事件数据显示，点击任意日期上的「事件条目」
4. 预期：
   - UI 响应快速，无冻结
   - 自动跳转到聊天 Tab
   - 聊天输入框已预填消息，格式：`帮我查看[日期]的日程「[标题]」，时间是[时间]`
   - 消息中包含逻辑完整的上下文（日期、事件标题、时间）

**问题排查:**
- 如果点击无反应：
  - 检查 adb logcat: `adb logcat | grep -i webview`
  - 检查是否有 JS 错误：`adb logcat | grep -i console`
- 如果跳转后输入框为空：
  - 检查 prefillInput 是否被调用：`adb logcat | grep -i viewmodel`
- 如果输入框有内容但格式错误：
  - 检查 CalendarView.tsx 的 `onClick` 消息拼接逻辑

### 5. 费用功能验证（WebView Bridge 第二重验证）

**步骤:**
1. 点击底部导航栏 "费用" Tab
2. 确认费用列表显示（无需数据，只看布局 + 总计）
3. 如果有费用数据显示，点击任意「费用条目」
4. 预期：
   - UI 响应快速，无冻结
   - 自动跳转到聊天 Tab
   - 聊天输入框已预填消息，格式：`帮我分析这笔消费：[描述]，金额¥[金额]，日期[日期]，分类：[分类]`
   - 消息中包含完整的消费上下文（描述、金额、日期、分类）

**问题排查:**
- 同日历页签排查方向

### 6. 聊天功能验证（确认预填不影响 AI 交互）

**步骤:**
1. 从日历/费用页跳转过来，输入框已预填
2. 点击发送按钮
3. 预期：
   - 预填内容被发送给 AI
   - 收到 AI 回复并流式渲染
   - 消息历史正确（用户 + AI 交替显示）

---

## 预期成功标志

✅ 日历 Tab 点击事件 → 自动导航到聊天，输入框已填入日程上下文  
✅ 费用 Tab 点击条目 → 自动导航到聊天，输入框已填入消费上下文  
✅ 预填内容能正常发送给 AI 并收到回复  
✅ 不同 Tab 切换时，消息历史不丢失  
✅ 网络超时或错误时，有合理的错误提示  

---

## 如果验证失败

### 收集诊断信息

```bash
# 完整的 logcat 日志（最后 500 行）
adb logcat > logcat_dump.txt

# WebView 调试信息
adb logcat | grep -i "webview"

# JS 控制台输出
adb logcat | grep -i "console"

# 导航和 UI 信息
adb logcat | grep -i "navgraph\|bridge\|navigate"

# Kotlin 错误
adb logcat | grep -i "exception\|error" | head -50
```

### 常见问题

| 问题 | 排查方向 |
|------|--------|
| 页面加载不出来 | assets 路径、WebViewAssetLoader 配置 |
| 点击无反应 | JS Bridge 未注册、`Android` 对象不存在 |
| 输入框为空 | `prefillInput` 未调用、ViewModel 共享失败 |
| 导航不跳转 | `navController.navigate` 错误、Compose 导航栈问题 |
| 网络错误 | adb reverse 隧道未建立、后端未启动 |

---

## 验证完成后

- 📝 记录验证结果：成功 ✅ / 失败 ❌ 及原因
- 📸 如有问题，截图或录屏关键步骤
- 🔍 提供完整 logcat 日志
- 💾 下一步：返回 Linux WSL 端，根据结果修复或进入 Phase 5

---

## 联系信息

**如遇卡顿或紧急问题：**
- 检查 WSL 后端是否仍在运行
- 重新建立 adb reverse：`adb forward --remove-all && adb reverse tcp:3000 tcp:3000`
- 重新安装 APK：`adb uninstall com.tally.app && adb install C:\Users\realw\Desktop\tally.apk`
- 重启手机或 adb：`adb kill-server && adb start-server`

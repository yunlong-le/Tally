# Phase 4: WebView 日历/费用视图

**状态**: ✅ 完成（2026-04-06）  
**前置条件**: Phase 3 完成

## 任务清单

- ✅ 4.1 React 项目初始化（Vite + TypeScript，产物输出到 android/app/src/main/assets/www/）
- ⬜ 4.2 列表视图 Timeline（跳过，月视图足够 Demo）
- ⬜ 4.3 日视图（跳过）
- ✅ 4.4 月视图（CalendarView：月份网格 + 选日查看当日事件）
- ⬜ 4.5 日期拨盘 Dial（可选，跳过）
- ✅ 4.6 费用视图（ExpenseView：月份费用列表 + 总计）
- ⬜ 4.7 Android WebView Bridge（待做：点击日程/费用 → 跳转聊天 Tab 带上下文）
- ✅ 4.8 数据获取联调（后端 GET /api/events + GET /api/expenses，真机验证通过）

## 关键技术决策

- **WebView 加载方式**：`WebViewAssetLoader` 将 assets/www/ 映射到 `https://appassets.androidplatform.net/assets/`
- **构建格式**：Vite IIFE 格式（非 ES module），避免 `type="module" crossorigin` 在 Android WebView 的兼容性问题
- **混合内容**：NavGraph 设置 `mixedContentMode = MIXED_CONTENT_ALWAYS_ALLOW`，允许 HTTPS WebView 调用 `http://localhost:3000`
- **Hash 路由**：`index.html#/calendar` 和 `#/expense` 区分两个视图，App.tsx 读 `window.location.hash`

## 遗留（Phase 5 可选）

- **4.7 WebView Bridge**：在日历/费用视图中点击某条记录 → 调用 `Android.navigateToChat(message)` → 聊天 Tab 自动填入上下文

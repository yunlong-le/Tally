# Tally Android 项目骨架

## 项目信息

- **项目名**: Tally（对位）
- **平台**: Android
- **语言**: Kotlin
- **UI 框架**: Jetpack Compose
- **构建系统**: Gradle (Kotlin DSL)
- **最低 SDK**: 26 (Android 8.0)
- **目标 SDK**: 35
- **编译 SDK**: 35
- **JVM 目标**: 11

## 目录结构

```
android/
├── app/
│   ├── src/main/
│   │   ├── kotlin/com/tally/app/
│   │   │   ├── MainActivity.kt          # 主 Activity 入口
│   │   │   ├── ui/theme/
│   │   │   │   ├── Color.kt            # 深色主题色彩定义
│   │   │   │   ├── Theme.kt            # Material3 主题配置
│   │   │   │   └── Type.kt             # 排版配置
│   │   │   └── navigation/
│   │   │       └── NavGraph.kt         # 导航图配置
│   │   ├── res/
│   │   │   ├── values/
│   │   │   │   ├── strings.xml        # 字符串资源
│   │   │   │   └── themes.xml         # 传统主题（兼容）
│   │   │   └── drawable/
│   │   │       └── ic_launcher_background.xml # 启动器背景
│   │   └── AndroidManifest.xml         # 清单文件
│   ├── build.gradle.kts               # App 模块构建配置
│   └── proguard-rules.pro             # ProGuard 混淆规则
├── gradle/
│   └── wrapper/
│       └── gradle-wrapper.properties   # Gradle 包装器配置
├── build.gradle.kts                   # 根项目构建配置
├── settings.gradle.kts                # 项目设置和 Gradle 仓库
└── .gitignore
```

## 依赖版本

### Compose 生态
- Compose BOM: 2024.02.00
- Material3: Latest via BOM
- Activity Compose: 1.8.2
- Navigation Compose: 2.7.6
- ViewModel Compose: 2.7.0
- Lifecycle Runtime Compose: 2.7.0

### 网络与序列化
- Retrofit 2: 2.9.0
- OkHttp 3: 4.12.0
- Gson: 2.10.1

### 异步编程
- Coroutines: 1.7.3

### 测试
- JUnit 4: 4.13.2
- Espresso: 3.5.1
- AndroidX Test: 1.1.5

## 构建和运行

### 前置条件

- Java 11 或更高版本
- Android SDK (API level 35)
- Android 模拟器或实体设备

### 编译调试版本

```bash
cd android
./gradlew assembleDebug
```

### 编译发布版本

```bash
./gradlew assembleRelease
```

### 运行测试

```bash
./gradlew test
```

### 运行 Android 仪器化测试

```bash
./gradlew connectedAndroidTest
```

## Gradle 包装器

项目使用 Gradle 8.6，通过包装器自动管理。首次运行 gradlew 命令时会自动下载依赖。

### 给予执行权限（Linux/macOS）

```bash
chmod +x gradlew
```

## 主题和样式

项目使用**深色主题**（Material3 + Custom Colors）：

- **主绿色** (#1DB954): 确认按钮、活跃状态
- **深黑背景** (#111111): 主背景
- **卡片背景** (#1E1E1E): UI 卡片
- **文本主色** (#FFFFFF): 主文字
- **文本次色** (#888888): 辅助文字
- **错误红** (#FF4444): 取消/错误状态

参考文件：
- `app/src/main/kotlin/com/tally/app/ui/theme/Color.kt`
- `app/src/main/kotlin/com/tally/app/ui/theme/Theme.kt`

## 导航

项目使用 Jetpack Navigation Compose 管理页面导航。

### 当前定义的页面

- `chat`: 聊天界面（主界面）
- `calendar`: 日历视图（Phase 4 实现）
- `expense`: 费用视图（Phase 4 实现）

参考文件：`app/src/main/kotlin/com/tally/app/navigation/NavGraph.kt`

## 权限

### 已配置的权限

- `android.permission.INTERNET`: 网络请求
- `android.permission.RECORD_AUDIO`: 语音识别
- `android:usesCleartextTraffic="true"`: 允许 HTTP 连接（开发阶段，用于连接本地 WSL 服务）

## 开发规范

1. **代码规范**:
   - 单个文件最大 800 行
   - 函数最大 50 行
   - 按 feature/domain 组织代码，不按类型

2. **提交信息格式**:
   ```
   <type>: <description>
   
   <optional body>
   ```
   类型: `feat`, `fix`, `refactor`, `docs`, `test`, `chore`, `perf`, `ci`

3. **测试覆盖率**:
   - 最低 80% 测试覆盖率
   - 包含单元测试、集成测试、E2E 测试

## 下一步

### Phase 1 完成项：
- ✅ Gradle 项目骨架
- ✅ Compose + Material3 基础配置
- ✅ 主题和颜色定义
- ✅ 导航框架
- ✅ 占位 UI

### Phase 2（即将开始）：
- 数据模型定义
- 网络 API 集成
- 数据库设置

### Phase 3：
- 聊天 UI 和逻辑
- 语音识别集成
- AI 对话处理

### Phase 4：
- 日历视图（WebView）
- 费用视图（WebView）
- 数据同步

## 调试和开发工具

### Compose 预览

在 IDE 中使用 `@Preview` 注解预览 UI 组件。

### 日志

使用 Kotlin 的 `Log` 类：

```kotlin
import android.util.Log

Log.d("TAG", "Debug message")
Log.e("TAG", "Error message", exception)
```

### ProGuard/R8 混淆

发布版本使用 R8 自动混淆（`isMinifyEnabled = false` 在调试版本）。

参考 `app/proguard-rules.pro` 配置 ProGuard 规则。

## 故障排除

### Gradle 编译失败

1. 清理构建缓存：
   ```bash
   ./gradlew clean
   ```

2. 重新同步 Gradle：
   ```bash
   ./gradlew --refresh-dependencies
   ```

3. 检查 Java 版本：
   ```bash
   java -version
   ```
   应该是 Java 11 或更高。

### 依赖冲突

使用 Gradle 依赖检查：

```bash
./gradlew dependencies
```

### 清单文件错误

检查 `app/src/main/AndroidManifest.xml` 中的权限和 Activity 配置。

## 参考资源

- [Android Developer Documentation](https://developer.android.com)
- [Jetpack Compose Documentation](https://developer.android.com/develop/ui/compose)
- [Material Design 3](https://m3.material.io)
- [Kotlin Documentation](https://kotlinlang.org/docs)

---

**创建日期**: 2026-04-06

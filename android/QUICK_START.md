# Tally Android - 快速开始指南

## 项目要求（已配置）

- **语言**: Kotlin 1.9.22
- **UI Framework**: Jetpack Compose
- **Android SDK**: minSdk 26, targetSdk 35
- **Build Tool**: Gradle 8.6 with Kotlin DSL
- **Java**: Java 11+

## 立即开始

### 1. 检查环境

```bash
# 检查 Java 版本（需要 Java 11+）
java -version

# 进入项目目录
cd /home/realw/Tally/android
```

### 2. 首次编译

```bash
# 给 gradlew 添加执行权限（Linux/macOS）
chmod +x gradlew

# 编译调试版本（~3-5 分钟，首次会下载依赖）
./gradlew assembleDebug

# 或使用简化命令
./gradlew build
```

### 3. 构建成功标志

```
> Task :app:assembleDebug
BUILD SUCCESSFUL in 3m 45s
```

APK 位置: `app/build/outputs/apk/debug/app-debug.apk`

### 4. 常用命令

| 命令 | 说明 |
|------|------|
| `./gradlew build` | 编译完整项目 |
| `./gradlew assembleDebug` | 构建调试 APK |
| `./gradlew assembleRelease` | 构建发布 APK |
| `./gradlew clean` | 清理构建缓存 |
| `./gradlew test` | 运行单元测试 |
| `./gradlew tasks` | 列出所有可用任务 |
| `./gradlew --refresh-dependencies` | 重新下载依赖 |

## 项目结构

```
app/
├── build.gradle.kts              # 构建配置
├── proguard-rules.pro            # 混淆规则
└── src/main/
    ├── AndroidManifest.xml       # App 清单（权限、Activity）
    ├── kotlin/com/tally/app/     # Kotlin 源代码
    │   ├── MainActivity.kt        # 主 Activity
    │   ├── ui/theme/
    │   │   ├── Color.kt          # 颜色定义
    │   │   ├── Theme.kt          # Compose 主题
    │   │   └── Type.kt           # 排版样式
    │   └── navigation/
    │       └── NavGraph.kt       # 导航配置
    └── res/
        ├── drawable/
        │   └── ic_launcher_background.xml
        └── values/
            ├── strings.xml       # 字符串资源
            └── themes.xml        # 兼容主题
```

## 核心类说明

### MainActivity.kt
应用入口，配置 Compose 内容和主题。

```kotlin
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        setContent {
            TallyTheme {
                NavGraph()  // 导航图
            }
        }
    }
}
```

### TallyTheme
Material3 深色主题，自定义色彩和排版。

```kotlin
TallyTheme {
    // 所有 UI 组件都使用这个主题
}
```

### NavGraph
应用导航结构，定义所有屏幕路由。

```kotlin
NavHost(navController, startDestination = Screen.Chat.route) {
    composable(Screen.Chat.route) { /* 聊天界面 */ }
    composable(Screen.Calendar.route) { /* 日历界面 */ }
    composable(Screen.Expense.route) { /* 费用界面 */ }
}
```

## 主题色彩

项目使用深色主题，Tally 品牌色彩：

- **绿色** (#1DB954) - 主要色彩，确认按钮
- **深黑** (#111111) - 背景色
- **深灰** (#1E1E1E) - 卡片色
- **白色** (#FFFFFF) - 主文字
- **中灰** (#888888) - 辅助文字
- **红色** (#FF4444) - 错误/取消

修改位置: `app/src/main/kotlin/com/tally/app/ui/theme/Color.kt`

## 添加新的 Composable

```kotlin
// app/src/main/kotlin/com/tally/app/screens/ChatScreen.kt
@Composable
fun ChatScreen(navController: NavController) {
    Column(modifier = Modifier.fillMaxSize()) {
        Text("Hello Tally!")
    }
}
```

在 `NavGraph.kt` 中添加路由：

```kotlin
composable(Screen.Chat.route) {
    ChatScreen(navController)
}
```

## 添加依赖

编辑 `app/build.gradle.kts`：

```kotlin
dependencies {
    // 例如添加 Hilt 依赖注入
    implementation("com.google.dagger:hilt-android:2.48")
    kapt("com.google.dagger:hilt-compiler:2.48")
}
```

## 故障排查

### 编译失败：Java 版本不支持

```bash
# 检查 Java 版本
java -version

# 如果不是 Java 11+，更新 JAVA_HOME
export JAVA_HOME=/usr/lib/jvm/java-11-openjdk
```

### 编译缓慢

```bash
# 清理所有缓存并重新下载
./gradlew clean
./gradlew --refresh-dependencies
```

### 找不到 AndroidManifest.xml

确保目录结构正确：
```
app/src/main/AndroidManifest.xml
```

### Gradle 同步失败

```bash
# 重新生成 Gradle 包装器
./gradlew wrapper --gradle-version=8.6
```

## 下一步

1. **Phase 2**: 添加数据模型、Room 数据库、Retrofit API
2. **Phase 3**: 实现聊天 UI 和语音识别
3. **Phase 4**: WebView 集成（日历和费用）

详见 `/home/realw/Tally/ANDROID_PHASE1_SUMMARY.md`

## IDE 配置（可选）

### Android Studio
1. File > Open > `/home/realw/Tally/android`
2. 等待 Gradle 同步（首次 ~2 分钟）
3. 设置本地 SDK：File > Project Structure > Android SDK

### VS Code
1. 安装扩展: "Extension Pack for Java"
2. 打开项目文件夹
3. 运行: Terminal > New Terminal > `./gradlew build`

## 资源链接

- [Android Developer Docs](https://developer.android.com)
- [Jetpack Compose](https://developer.android.com/develop/ui/compose)
- [Kotlin Language](https://kotlinlang.org)
- [Material Design 3](https://m3.material.io)

---

**Last Updated**: 2026-04-06

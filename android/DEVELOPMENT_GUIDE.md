# Tally Android - 开发指南

## 遵循的全局规则

所有开发必须遵守 `~/.claude/rules/common/` 中的规则，以及本项目的特定指南。

## 开发工作流

### 每个特性开始前

1. 使用 **Plan Mode** 生成实现计划
2. 获得用户批准后再开始开发

### 强制 TDD 工作流

所有新功能必须遵循 TDD 模式：

```
RED → GREEN → REFACTOR
1. 先写测试（测试失败）
2. 编写最小实现使测试通过（GREEN）
3. 重构优化代码（IMPROVE）
4. 验证测试覆盖率 >= 80%
```

### 提交代码前的必检清单

- ✅ 所有单元测试通过 (`./gradlew test`)
- ✅ 所有 UI 测试通过 (`./gradlew connectedAndroidTest`)
- ✅ 测试覆盖率 >= 80%
- ✅ 使用 code-reviewer agent 审查代码
- ✅ 无硬编码的密钥、密码或凭据
- ✅ 遵循提交信息格式：`<type>: <description>`
- ✅ 代码遵循 Kotlin 编码风格

## 编码标准

### 文件组织

- **最大行数**: 单文件 800 行
- **最大函数**: 单函数 50 行
- **组织方式**: 按 feature/domain，不按类型

目录结构示例：
```
app/src/main/kotlin/com/tally/app/
├── ui/
│   ├── theme/           # 主题 + 样式
│   ├── screens/         # 屏幕组件
│   └── components/      # 通用 UI 组件
├── data/
│   ├── repository/      # 数据访问
│   ├── api/            # 网络接口
│   └── local/          # 本地存储
├── domain/
│   ├── model/          # 业务实体
│   ├── usecase/        # 业务逻辑
│   └── repository/     # 仓库接口
├── navigation/         # 导航配置
└── MainActivity.kt     # 应用入口
```

### Kotlin 编码风格

#### 1. 优先使用 `val` 而不是 `var`

```kotlin
// 不推荐
var count = 0

// 推荐
val count = 0
```

#### 2. 使用数据类表示值对象

```kotlin
// 推荐
data class Message(
    val id: String,
    val content: String,
    val timestamp: Long
)
```

#### 3. 避免使用 `!!` 操作符

```kotlin
// 不推荐
val name = user!!.name

// 推荐
val name = user?.name ?: "Unknown"
val name = requireNotNull(user) { "User must not be null" }.name
```

#### 4. 命名约定

```kotlin
// 类和接口：PascalCase
class ChatViewModel
interface MessageRepository

// 函数和变量：camelCase
fun sendMessage(content: String)
val messageCount = 0

// 常量：SCREAMING_SNAKE_CASE
const val DEFAULT_TIMEOUT = 30000L
```

#### 5. 使用密封类表示受限的类型层次

```kotlin
// 表示网络请求的不同状态
sealed class UiState<out T> {
    data object Loading : UiState<Nothing>()
    data class Success<T>(val data: T) : UiState<T>()
    data class Error(val throwable: Throwable) : UiState<Nothing>()
}
```

### 不可变性设计

遵循共同规则中的不可变性原则：

```kotlin
// 不推荐：修改原对象
fun updateUser(user: User, name: String) {
    user.name = name  // 副作用！
}

// 推荐：返回新副本
fun updateUser(user: User, name: String): User {
    return user.copy(name = name)
}
```

### 错误处理

始终显式处理错误，不要静默吞掉异常：

```kotlin
// 不推荐
try {
    fetchData()
} catch (e: Exception) {
    // 忽略
}

// 推荐
try {
    fetchData()
} catch (e: NetworkException) {
    // 处理网络错误
    showErrorMessage("Network error: ${e.message}")
    log.error("Failed to fetch data", e)
} catch (e: Exception) {
    // 处理其他异常
    throw e  // 如果无法处理，重新抛出
}
```

## 安全指南

### 禁止硬编码密钥

```kotlin
// 禁止！
const val API_KEY = "sk-abc123..."

// 允许：从构建时生成的 BuildConfig
val apiKey = BuildConfig.API_KEY

// 允许：从安全存储读取
val token = secureStorage.get("auth_token")
```

### 网络安全

- 所有生产环境必须使用 HTTPS
- 开发环境中的 cleartext 流量仅限于 localhost

### 用户输入验证

```kotlin
// 对所有用户输入进行验证
fun sendMessage(content: String) {
    val trimmed = content.trim()
    
    // 验证不为空
    require(trimmed.isNotEmpty()) { "Message cannot be empty" }
    
    // 验证长度限制
    require(trimmed.length <= MAX_MESSAGE_LENGTH) { "Message too long" }
    
    // 继续处理...
}
```

## 测试要求

### 最低覆盖率：80%

### 测试类型

1. **单元测试** - 测试函数、业务逻辑
2. **集成测试** - 测试 API、数据库操作
3. **UI 测试** - 测试 Composable 和用户交互

### 测试示例

```kotlin
// ViewModel 测试
@Test
fun `loading state then success state`() = runTest {
    val viewModel = ChatViewModel(fakeChatRepository)
    
    viewModel.state.test {
        assertEquals(ChatState.Empty, awaitItem())
        viewModel.loadMessages()
        assertEquals(ChatState.Loading, awaitItem())
        assertEquals(ChatState.Success(testMessages), awaitItem())
    }
}

// Repository 测试
@Test
fun `get messages returns success`() = runTest {
    val messages = fakeRepository.getMessages()
    assertEquals(listOf(testMessage), messages)
}

// Composable 测试
@Test
fun `chat screen displays messages`() {
    composeTestRule.setContent {
        ChatScreen(messages = testMessages)
    }
    
    composeTestRule
        .onNodeWithText(testMessages[0].content)
        .assertIsDisplayed()
}
```

## Git 提交规范

### 提交信息格式

```
<type>: <description>

<optional detailed body>
```

### 允许的类型

| 类型 | 说明 |
|------|------|
| `feat` | 新功能 |
| `fix` | Bug 修复 |
| `refactor` | 代码重构（不改变功能） |
| `test` | 添加或修改测试 |
| `docs` | 文档更新 |
| `chore` | 依赖更新、配置变更 |
| `perf` | 性能优化 |
| `ci` | CI/CD 配置变更 |

### 提交信息示例

```
feat: add voice message support to chat screen

- Implement voice recording with MediaRecorder
- Add playback UI component
- Integrate with message model
- Add tests for voice recording lifecycle

Closes #123
```

```
fix: prevent message duplication on retry

The retry mechanism was not deduplicating messages
based on timestamp, causing duplicates in the list.

Now using message ID for deduplication.
```

## 代码审查

### 审查清单

在审查任何代码前检查：

- [ ] 代码可读且命名清晰
- [ ] 函数 < 50 行
- [ ] 文件 < 800 行
- [ ] 无深层嵌套 (> 4 层)
- [ ] 错误显式处理
- [ ] 无硬编码密钥
- [ ] 无 `console.log` 或调试语句
- [ ] 新功能有测试
- [ ] 测试覆盖率 >= 80%

### 何时使用审查代理

**立即使用 code-reviewer 代理当：**

- 刚编写或修改了代码
- 代码将被提交或合并
- 需要检查代码质量

**使用 security-reviewer 代理当：**

- 编写认证/授权代码
- 处理用户输入
- 进行数据库操作
- 调用外部 API
- 实现密码学操作

## 权限管理

### 预授权操作（无需确认）

- Read/Edit/Write 文件操作
- `./gradlew build/test/assembleDebug/assembleRelease`
- 本地开发工作

### 需要确认的操作

- `git commit` - 需要手动确认（可一次性批准多个）
- `git push` - 需要确认
- `git rebase` 或 `git reset` - 需要确认

### 禁止的操作（自动阻止）

- `rm -rf` - 自动阻止
- `git push --force` - 自动阻止
- `git reset --hard` - 自动阻止
- 硬编码 API 密钥 - 代码审查时检测并拒绝

## 构建和部署

### 构建类型

```bash
# 调试版本（可调试、包含符号表）
./gradlew assembleDebug

# 发布版本（优化、混淆）
./gradlew assembleRelease

# 完整编译（运行测试）
./gradlew build
```

### 版本管理

在 `app/build.gradle.kts` 中更新版本：

```kotlin
defaultConfig {
    versionCode = 2        // 每个发布递增
    versionName = "1.1.0"  // 遵循 semantic versioning
}
```

## 调试

### 使用日志

```kotlin
import android.util.Log

Log.d("ChatVM", "Loading messages...")
Log.e("ChatVM", "Error: ${e.message}", e)
```

### Compose 预览

```kotlin
@Preview(showBackground = true)
@Composable
fun ChatScreenPreview() {
    TallyTheme {
        ChatScreen(messages = emptyList())
    }
}
```

### Android Studio 调试器

1. 在代码行号旁点击设置断点
2. 运行 > Debug 'app'
3. 执行到断点时暂停

## 性能优化

### Compose 优化

- 使用 `key()` 优化列表重组
- 避免在 Composable 中计算耗时操作
- 使用 `remember` 缓存计算结果

### 网络优化

- 实现请求缓存（Retrofit + OkHttp）
- 使用分页加载大型列表
- 实现离线支持

### 数据库优化

- 为频繁查询的字段建立索引
- 使用 Room 的 `@Embedded` 进行 JOIN 优化
- 避免 N+1 查询问题

## 资源和文档

- [Android Developer Guide](https://developer.android.com)
- [Kotlin Coroutines](https://kotlinlang.org/docs/coroutines-overview.html)
- [Jetpack Compose](https://developer.android.com/jetpack/compose)
- [Material Design 3](https://m3.material.io)
- 本项目规则：`~/.claude/rules/kotlin/`

## 问题排查

### 编译失败

1. 检查 Java 版本: `java -version` (需要 11+)
2. 清理缓存: `./gradlew clean`
3. 更新依赖: `./gradlew --refresh-dependencies`

### 测试失败

1. 检查测试隔离（是否有全局状态）
2. 验证 mock 配置正确
3. 查看完整错误堆栈

### 网络请求失败

1. 检查 AndroidManifest.xml 中的权限
2. 验证 API 端点 URL 正确
3. 检查请求拦截器配置

---

**最后更新**: 2026-04-06

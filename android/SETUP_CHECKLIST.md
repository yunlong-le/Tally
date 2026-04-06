# Tally Android - 初始化检查清单

完成以下所有步骤以确保开发环境正确配置。

## 1. 系统环境检查

- [ ] Java 11+ 已安装
  ```bash
  java -version
  # 预期输出: java version "11.0.x" 或更高
  ```

- [ ] Git 已安装
  ```bash
  git --version
  # 预期输出: git version 2.x.x 或更高
  ```

- [ ] 网络连接良好（首次构建需要下载 Gradle 和依赖）

## 2. 项目目录检查

- [ ] 项目位置: `/home/realw/Tally/android/`
- [ ] `settings.gradle.kts` 存在
- [ ] `build.gradle.kts` 存在
- [ ] `app/` 子目录存在
- [ ] `app/src/main/` 标准 Android 目录结构完整

```bash
# 验证目录结构
ls -la /home/realw/Tally/android/
ls -la /home/realw/Tally/android/app/src/main/
```

## 3. Gradle 包装器权限

- [ ] 给 gradlew 脚本添加执行权限（Linux/macOS）

```bash
chmod +x /home/realw/Tally/android/gradlew
ls -l /home/realw/Tally/android/gradlew
# 预期: -rwxr-xr-x (有 x 权限)
```

## 4. 首次编译测试

- [ ] 尝试编译调试版本
```bash
cd /home/realw/Tally/android
./gradlew assembleDebug
```

**预期结果**:
- 首次编译时间: 3-5 分钟（取决于网络）
- 成功输出: `BUILD SUCCESSFUL`
- APK 生成: `app/build/outputs/apk/debug/app-debug.apk`

## 5. 依赖验证

- [ ] 所有依赖正确下载
```bash
./gradlew dependencies
```

**预期**:
- 显示完整的依赖树
- 无错误或冲突警告

## 6. Kotlin 编译检查

- [ ] Kotlin 代码编译无错误
```bash
./gradlew compileDebugKotlin
```

**预期**:
- 编译成功，无 Kotlin 错误

## 7. 资源验证

- [ ] Android 资源文件有效
```bash
./gradlew mergeDebugResources
```

**预期**:
- 资源合并成功

## 8. 清单验证

- [ ] AndroidManifest.xml 有效

```bash
./gradlew validateSigningDebug
```

**预期**:
- 清单验证通过

## 9. 测试框架检查（可选）

```bash
./gradlew compileDebugUnitTestKotlin
```

**预期**:
- 测试代码编译成功

## 10. 配置文件检查

- [ ] `gradle.properties` 存在或使用默认配置
- [ ] `local.properties` 不存在或不包含密钥

```bash
# 检查是否存在敏感信息
grep -E "API_KEY|PASSWORD|SECRET|TOKEN" /home/realw/Tally/android/local.properties 2>/dev/null
# 预期: 无输出（没有敏感信息）
```

## 11. IDE 配置（可选）

### Android Studio
- [ ] 安装最新版本 Android Studio
- [ ] File > Open > 选择 `/home/realw/Tally/android`
- [ ] Gradle 同步完成（可能需要 2-5 分钟）
- [ ] 无 Gradle 同步错误

### VS Code
- [ ] 安装 Extension Pack for Java
- [ ] 安装 Android 相关扩展（可选）
- [ ] 打开项目文件夹
- [ ] 验证 Gradle 插件自动激活

## 12. Git 初始化（如果尚未初始化）

```bash
cd /home/realw/Tally/android

# 初始化 Git（可选，如果父级已初始化可跳过）
git init

# 或添加到现有项目
git add .
```

- [ ] `.gitignore` 正确配置
  ```bash
  cat /home/realw/Tally/android/.gitignore
  # 预期: 包含 build/, .gradle/, *.apk 等
  ```

## 13. 验证编码规范

```bash
# 检查 Kotlin 代码风格（如果安装了 ktlint）
./gradlew ktlintCheck
```

- [ ] 代码风格检查通过（或可以自动修复）

## 14. 快速验证所有步骤

```bash
#!/bin/bash
cd /home/realw/Tally/android

echo "=== 验证 Java 版本 ==="
java -version

echo "=== 验证 Gradle 包装器权限 ==="
[ -x ./gradlew ] && echo "✓ gradlew 有执行权限" || echo "✗ 需要 chmod +x"

echo "=== 验证项目结构 ==="
[ -f settings.gradle.kts ] && echo "✓ settings.gradle.kts 存在" || echo "✗ 缺失"
[ -f build.gradle.kts ] && echo "✓ build.gradle.kts 存在" || echo "✗ 缺失"
[ -f app/build.gradle.kts ] && echo "✓ app/build.gradle.kts 存在" || echo "✗ 缺失"

echo "=== 验证源代码 ==="
[ -f app/src/main/kotlin/com/tally/app/MainActivity.kt ] && echo "✓ MainActivity.kt 存在" || echo "✗ 缺失"

echo "=== 执行编译测试 ==="
./gradlew assembleDebug --quiet && echo "✓ 编译成功" || echo "✗ 编译失败"
```

保存为 `verify-setup.sh` 并运行：
```bash
chmod +x verify-setup.sh
./verify-setup.sh
```

## 15. 故障排除

如果遇到问题，按以下顺序尝试：

### 问题: "command not found: gradlew"
```bash
chmod +x /home/realw/Tally/android/gradlew
```

### 问题: "JAVA_HOME 未设置"
```bash
export JAVA_HOME=/usr/lib/jvm/java-11-openjdk-amd64  # Linux
# 或
export JAVA_HOME=/Library/Java/JavaVirtualMachines/jdk-11.jdk/Contents/Home  # macOS
```

### 问题: "Build failed"
```bash
./gradlew clean
./gradlew --refresh-dependencies
./gradlew assembleDebug
```

### 问题: "Gradle sync failed"
1. 删除 `.gradle` 缓存: `rm -rf .gradle`
2. 删除 `build` 目录: `rm -rf app/build`
3. 重新同步: `./gradlew sync`

## 16. 准备开发

一切就绪后，你可以：

- [ ] 阅读 `QUICK_START.md` - 快速开始指南
- [ ] 阅读 `DEVELOPMENT_GUIDE.md` - 开发规范
- [ ] 阅读 `README.md` - 详细项目文档
- [ ] 开始 Phase 2 开发（数据层）

## 验证检查清单完成

最后，请确认：

- [ ] Java 版本 >= 11
- [ ] `./gradlew assembleDebug` 编译成功
- [ ] 无编译错误或警告
- [ ] APK 文件生成成功
- [ ] 所有文档已阅读
- [ ] 理解 TDD 工作流和提交规范

## 联系支持

如果在初始化过程中遇到任何问题：

1. 检查 Java 版本和 JAVA_HOME
2. 查看完整的编译错误信息
3. 尝试 `./gradlew clean` + `./gradlew --refresh-dependencies`
4. 参考项目文档中的故障排除部分

---

**检查列表最后更新**: 2026-04-06

**完成日期**: _______________（填入完成日期）

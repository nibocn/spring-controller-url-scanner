# JetBrains Marketplace 发布检查清单

## 项目元信息

- 插件 ID：`me.nibo.spring-url-scanner`
- 展示名称：`Spring URL Scanner`
- Java 包名空间：`me.nibo.springurlscanner`
- 开发者/厂商：`NiBo`
- 许可证：MIT
- 源码地址：`https://github.com/nibocn/spring-controller-url-scanner`
- 当前版本：`0.2.0`
- 最低兼容版本：`242` / IntelliJ IDEA 2024.2+

## 构建命令

```bash
./gradlew verifyPluginProjectConfiguration
./gradlew verifyPluginStructure
./gradlew verifyPlugin
./gradlew buildPlugin
```

插件 ZIP 包会生成在：

```text
build/distributions/
```

## Marketplace 上传准备

- 创建或选择 JetBrains Marketplace 的开发者/厂商资料。
- 接受 JetBrains Marketplace Developer Agreement。
- 如果按 MIT 许可证发布，Marketplace 页面可填写公开源码地址：`https://github.com/nibocn/spring-controller-url-scanner`。
- 选择准确的标签，例如：`Spring`、`Java`、`Tools`、`Navigation`。
- 上传截图，当前已准备：
  - `docs/marketplace/screenshots/tool-window-menu.png`
  - `docs/marketplace/screenshots/scan-results.png`
- 插件介绍文案已准备在 `docs/marketplace-listing.md`，包含英文和中文两个版本。
- 在插件页面明确说明：扫描在本地 IDE 内完成，不上传源码，不收集遥测数据。
- 首次上传建议手动完成；后续版本可以配置 `JETBRAINS_PUBLISH_TOKEN` 后使用 `./gradlew publishPlugin` 自动发布。

## 可选签名配置

IntelliJ Platform Gradle Plugin 默认可以从以下环境变量读取签名和发布配置：

```bash
export JETBRAINS_PUBLISH_TOKEN='...'
export PRIVATE_KEY='...'
export PRIVATE_KEY_PASSWORD='...'
export CERTIFICATE_CHAIN='...'
```

不要将签名密钥或发布 Token 提交到 Git 仓库。

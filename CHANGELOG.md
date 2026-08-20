# 更新日志

## 0.2.0 - 2026-08-20

- 系统复制快捷键默认复制当前选中的单元格内容。
- 右键菜单支持复制当前单元格或复制整行接口信息。
- 整行复制内容使用带表头的 TSV 格式，方便粘贴到文本、表格或工单中。
- 扫描过程中显示加载动画和状态提示。
- 新增 `Export All`，可将全部扫描结果导出为 CSV 文件。
- 自动发布 Token 环境变量改为 `JETBRAINS_PUBLISH_TOKEN`。

## 0.1.0 - 2026-08-19

首个面向 JetBrains Marketplace 发布准备的版本。

- 从项目源码和依赖 JAR 中扫描 Spring MVC Controller 映射。
- 识别常见 Spring 映射注解，包括 `@RequestMapping`、`@GetMapping`、`@PostMapping`、`@PutMapping`、`@DeleteMapping` 和 `@PatchMapping`。
- 自动合并类级别和方法级别路径。
- 可选扫描 OpenFeign 客户端接口。
- 展示接口类型、HTTP 方法、URL、Controller/Client 类、处理方法和来源位置。
- 支持过滤扫描结果，并跳转到源码或反编译类。

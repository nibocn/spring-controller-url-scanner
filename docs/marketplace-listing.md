# JetBrains Marketplace 插件介绍文案

## English

### Short description

Find Spring MVC controller endpoints and OpenFeign client mappings directly inside IntelliJ IDEA.

### Full description

Spring URL Scanner helps Java and Spring developers quickly discover HTTP endpoints in an IntelliJ IDEA project.

It scans project sources and dependency JARs, recognizes common Spring MVC mapping annotations, combines class-level and method-level paths, and lists the discovered endpoints in a dedicated `Spring URLs` tool window.

The plugin can also include OpenFeign clients when the `Include @FeignClient` option is enabled, making it easier to inspect both incoming controller routes and outgoing client API declarations from one place.

All scanning runs locally inside the IDE. The plugin does not upload source code, collect telemetry, or contact external services.

### Key features

- Scan Spring MVC controller mappings from project sources and dependency JARs.
- Recognize `@RestController`, `@Controller`, `@RequestMapping`, `@GetMapping`, `@PostMapping`, `@PutMapping`, `@DeleteMapping`, and `@PatchMapping`.
- Combine class-level and method-level paths into complete endpoint URLs.
- Optionally scan OpenFeign interfaces annotated with `@FeignClient`.
- Filter results by type, URL, controller/client class, handler method, or source JAR.
- Double-click a result row to navigate to source code or a decompiled dependency class.

### Suggested tags

`Spring`, `Java`, `Tools`, `Navigation`, `REST`, `Feign`

### Screenshots

- `docs/marketplace/screenshots/tool-window-menu.png`: shows the `Spring URLs` entry in the IntelliJ IDEA Tool Windows menu.
- `docs/marketplace/screenshots/scan-results.png`: shows scanned endpoint results in the `Spring URLs` tool window.

## 中文

### 简短描述

在 IntelliJ IDEA 中快速查看 Spring MVC Controller 接口和 OpenFeign 客户端映射。

### 完整介绍

Spring URL Scanner 是一个面向 Java/Spring 项目的 IntelliJ IDEA 插件，用于快速发现项目中的 HTTP 接口。

插件会扫描项目源码和依赖 JAR，识别常见的 Spring MVC 映射注解，自动合并类级别和方法级别路径，并将扫描结果展示在独立的 `Spring URLs` 工具窗口中。

开启 `Include @FeignClient` 选项后，插件也可以扫描 OpenFeign 客户端接口，方便在一个界面里同时查看 Controller 暴露的入口接口和 Feign 声明的外部调用接口。

所有扫描都在本地 IDE 内完成。插件不会上传源码，不收集遥测数据，也不会访问外部服务。

### 核心功能

- 从项目源码和依赖 JAR 中扫描 Spring MVC Controller 映射。
- 识别 `@RestController`、`@Controller`、`@RequestMapping`、`@GetMapping`、`@PostMapping`、`@PutMapping`、`@DeleteMapping` 和 `@PatchMapping`。
- 自动合并类级别和方法级别路径，生成完整接口 URL。
- 可选扫描带有 `@FeignClient` 注解的 OpenFeign 接口。
- 支持按类型、URL、Controller/Client 类、处理方法或来源 JAR 过滤结果。
- 双击结果行可跳转到源码或依赖中的反编译类。

### 建议标签

`Spring`、`Java`、`Tools`、`Navigation`、`REST`、`Feign`

### 截图说明

- `docs/marketplace/screenshots/tool-window-menu.png`：展示 IntelliJ IDEA Tool Windows 菜单中的 `Spring URLs` 入口。
- `docs/marketplace/screenshots/scan-results.png`：展示 `Spring URLs` 工具窗口中的接口扫描结果。

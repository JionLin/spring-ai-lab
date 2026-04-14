## Why

仓库缺少面向学习与面试的结构化说明；同时需要将「自然语言路由」的技术选型收敛为 **方案 A（Spring AI Tool / function calling）**，并把可对外表述的要点沉淀到技术文档中，便于复习与面试沟通。

## What Changes

- 在项目根目录新增 `docs/` 目录及一份 Markdown 技术文档（技术栈、模块说明、与方案 A 对齐的扩展方向说明）。
- 文档中新增 **「面试要点」** 小节：在承认工业界常见分层与混合（规则 + 模型）的前提下，明确本项目演示路径采用 **方案 A**，并给出可辩护的表述边界（可控性、权限、评测、数据层职责）。
- 不在本变更中实现新的业务 Controller、MySQL 接入或生产级路由代码（仅文档与 OpenSpec 工件；实现若需要另起变更）。

## Capabilities

### New Capabilities

- `tech-onboarding-docs`: 定义「开发者技术熟悉文档」应包含的章节与最低信息要求（含「面试要点」与方案 A 表述）。

### Modified Capabilities

- （无：`openspec/specs/` 下当前无既有能力规格。）

## Impact

- **文档**：新增 `docs/*.md`（具体文件名由 `design.md` / `tasks.md` 约定）。
- **代码**：无运行时行为变更；`pom.xml` 与源码不强制修改（除非后续 apply 扩展范围）。
- **依赖**：无新增 Maven 依赖（本变更范围内）。

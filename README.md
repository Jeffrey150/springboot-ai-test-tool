# Test AI

一个开源的 AI 测试用例生成平台。上传需求文档或设计文档，即可自动分析并生成结构化、可导出的测试用例。

---

## 目录

- [项目简介](#项目简介)
- [功能特性](#功能特性)
- [技术栈](#技术栈)
- [项目结构](#项目结构)
- [快速开始](#快速开始)
  - [环境要求](#环境要求)
  - [数据库准备](#数据库准备)
  - [后端启动](#后端启动)
  - [前端启动](#前端启动)
- [环境变量](#环境变量)
- [接口概览](#接口概览)
- [开发规范](#开发规范)
- [测试验证](#测试验证)
- [参与贡献](#参与贡献)
- [许可证](#许可证)

---

## 项目简介

Test AI 旨在帮助测试人员、产品经理和开发者将需求文档、设计文档快速转化为高质量的测试用例。系统支持 PDF、DOC、DOCX、Markdown 等常见文档格式，提供文档上传、任务管理、用例生成、结果下载与评价反馈等完整流程。

> **⚠️ 重要提示**：本项目当前版本未实现用户认证和权限控制功能，所有接口均可直接访问。如需在生产环境使用，请自行实现 JWT/Session 认证、接口权限控制等安全机制。本项目仅供学习和演示用途。

---

## 功能特性

- **文档上传**：支持 PDF / DOC / DOCX / Markdown 格式，单文件最大 50 MB。
- **AI 用例生成**：基于大语言模型自动提取功能点并生成测试用例。
- **任务管理**：查看生成任务状态、进度、用例数量与质量评分。
- **结果预览与下载**：支持 Markdown 格式预览与下载。
- **用户反馈**：对生成结果进行评分与反馈，持续优化生成质量。
- **统计看板**：展示任务总数、完成数、进行中数、失败数等关键指标。

---

## 技术栈

| 层级 | 技术 |
|------|------|
| 前端 | Vue 3 + TypeScript + Element Plus + Pinia + Vue Router + Axios |
| 后端 | Spring Boot 2.7 + MyBatis Plus + MySQL 8 |
| 文档解析 | Apache PDFBox、Apache POI |
| AI 接入 | 阿里云百炼 DashScope SDK，同时支持 OpenAI / Gemini 等自定义配置 |

---

## 项目结构

```text
test-ai/
├── test-ai-backend/          # 后端服务
│   ├── src/main/java/        # Java 源码
│   │   └── com/testai/
│   │       ├── controller/   # RESTful 控制器
│   │       ├── service/      # 业务逻辑
│   │       ├── mapper/       # 数据访问层
│   │       ├── entity/       # 实体类
│   │       ├── dto/          # 数据传输对象
│   │       ├── common/       # 通用结果封装、异常处理
│   │       ├── util/         # 工具类
│   │       └── task/         # 异步任务
│   ├── src/main/resources/
│   │   ├── application.yml   # 应用配置
│   │   ├── db/schema.sql     # 数据库初始化脚本
│   │   └── mapper/           # MyBatis XML
│   └── pom.xml
├── test-ai-frontend/         # 前端应用
│   ├── src/
│   │   ├── api/              # API 接口封装
│   │   ├── components/       # 公共组件
│   │   ├── views/            # 页面视图
│   │   ├── router/           # 路由配置
│   │   ├── utils/            # 工具函数
│   │   └── types/            # TypeScript 类型
│   ├── package.json
│   └── vite.config.ts
├── Agent.md                  # 项目开发规范
└── README.md                 # 本文件
```

---

## 快速开始

### 环境要求

- JDK 1.8+
- Maven 3.6+
- MySQL 8.0+
- Node.js 18+
- npm 9+

### 数据库准备

1. 创建数据库：

   ```sql
   CREATE DATABASE testai DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
   ```

2. 执行初始化脚本：

   ```bash
   mysql -u root -p testai < test-ai-backend/src/main/resources/db/schema.sql
   ```

### 后端启动

1. 进入后端目录：

   ```bash
   cd test-ai-backend
   ```

2. 配置环境变量（推荐）或在 `application.yml` 中使用默认值：

   | 变量 | 说明 |
   |------|------|
   | `DB_URL` | MySQL 连接地址 |
   | `DB_USERNAME` | 数据库用户名 |
   | `DB_PASSWORD` | 数据库密码 |
   | `AI_API_KEY` | AI 平台 API 密钥 |

3. 编译并运行：

   ```bash
   mvn clean compile
   mvn spring-boot:run
   ```

   后端默认运行在 `http://localhost:10010`。

### 前端启动

1. 进入前端目录：

   ```bash
   cd test-ai-frontend
   ```

2. 安装依赖：

   ```bash
   npm install
   ```

3. 启动开发服务器：

   ```bash
   npm run dev
   ```

   前端默认运行在 `http://localhost:3000`，并通过 Vite 代理将 `/api` 请求转发到后端。

4. 生产构建：

   ```bash
   npm run build
   ```

---

## 环境变量

### 后端

在启动前设置以下环境变量，避免将敏感信息提交到版本控制：

```bash
export DB_URL="jdbc:mysql://localhost:3306/testai?useUnicode=true&characterEncoding=utf8&zeroDateTimeBehavior=convertToNull&useSSL=true&serverTimezone=GMT%2B8"
export DB_USERNAME="root"
export DB_PASSWORD="your_password"
export AI_API_KEY="your_ai_api_key"
```

### 前端

前端通过 `.env` 文件或环境变量配置 API 地址：

```bash
VITE_API_BASE_URL=http://localhost:10010
```

---

## 接口概览

后端接口统一以 `/api/v1` 为前缀（部分文档相关接口以 `/documents` 为前缀），返回统一格式：

```json
{
  "code": 200,
  "msg": "操作成功",
  "data": {}
}
```

主要接口：

| 接口 | 方法 | 说明 |
|------|------|------|
| `/documents/upload` | POST | 批量上传文档 |
| `/api/v1/tasks/generate` | POST | 创建用例生成任务 |
| `/api/v1/tasks/list` | POST | 分页查询任务列表 |
| `/api/v1/tasks/{id}` | GET | 查询任务详情 |
| `/api/v1/tasks/{id}/download` | GET | 下载生成的用例文件 |
| `/api/v1/tasks/{id}/feedback` | POST | 提交任务反馈 |
| `/api/v1/tasks/statistics` | GET | 获取任务概览统计 |
| `/api/v1/task-statistics/statistics` | GET | 获取按时间范围统计 |
| `/api/v1/task-feedback/submit` | POST | 提交任务评价 |
| `/api/test-demo/list` | GET | 测试示例接口 |

---

## 开发规范

本项目遵循 [Agent.md](./Agent.md) 中的开发规范，核心原则包括：

1. **不要假设，不要隐藏困惑**：遇到模糊需求先确认。
2. **用最小代码解决问题**：避免过度设计。
3. **只改必须改的**：精确控制修改范围。
4. **定义成功标准，循环验证**：每完成一个功能单元立即自测。

详细的编码规范、安全规范与自测清单请参见 [Agent.md](./Agent.md)。

---

## 测试验证

### 后端

```bash
cd test-ai-backend
mvn test
```

### 前端

```bash
cd test-ai-frontend
npm run build
```

---

## 参与贡献

欢迎提交 Issue 和 Pull Request。贡献前请确保：

1. 已阅读 [Agent.md](./Agent.md) 中的开发规范。
2. 代码通过类型检查与单元测试。
3. 提交信息清晰说明改动原因与范围。

---

## 许可证

本项目采用 [MIT License](./LICENSE) 开源协议。

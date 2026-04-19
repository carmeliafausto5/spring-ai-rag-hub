<div align="center">

# Spring AI RAG Hub

**Production-grade RAG knowledge base · 生产级 RAG 知识库**

[![Java](https://img.shields.io/badge/Java-17+-orange.svg)](https://openjdk.org/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.4.4-brightgreen.svg)](https://spring.io/projects/spring-boot)
[![Spring AI](https://img.shields.io/badge/Spring%20AI-1.0.0-blue.svg)](https://spring.io/projects/spring-ai)
[![License](https://img.shields.io/badge/License-Apache%202.0-blue.svg)](LICENSE)

[English](#english) · [中文](#中文)

</div>

---

## English

### Overview

Spring AI RAG Hub is a modular, production-ready Retrieval-Augmented Generation (RAG) system built on **Spring Boot 3.4** + **Spring AI 1.0** + **pgvector**. It supports multiple AI providers (OpenAI, Anthropic Claude, Ollama) switchable via a single environment variable — no code changes required.

### Features

- **Multi-provider** — Switch between OpenAI / Anthropic / Ollama with `RAG_PROVIDER=xxx`
- **Streaming** — SSE-based token streaming via `Flux<String>`
- **Hexagonal architecture** — `rag-core` has zero framework dependencies; all ports are interfaces
- **Any file format** — PDF, DOCX, HTML, Markdown ingestion via Apache Tika
- **pgvector** — PostgreSQL vector store with automatic schema initialization
- **Observability** — Spring Boot Actuator endpoints out of the box

### Quick Start

**Prerequisites:** Docker, Java 17+, Maven 3.9+

```bash
# 1. Clone
git clone https://github.com/your-org/spring-ai-rag-hub.git
cd spring-ai-rag-hub

# 2. Configure
cp .env.example .env
# Edit .env — set RAG_PROVIDER and your API key

# 3. Start PostgreSQL (pgvector)
docker compose up postgres -d

# 4. Run
./mvnw spring-boot:run -pl rag-api
```

> **No API key?** Use Ollama locally:
> ```bash
> docker compose --profile ollama up -d
> RAG_PROVIDER=ollama ./mvnw spring-boot:run -pl rag-api
> ```

### API Reference

#### Upload a document
```bash
curl -X POST http://localhost:8080/api/v1/documents/upload \
  -F "file=@knowledge.pdf" \
  -F "title=My Knowledge Base"
```

#### Query (blocking)
```bash
curl -X POST http://localhost:8080/api/v1/rag/query \
  -H "Content-Type: application/json" \
  -d '{"question": "What is the refund policy?"}'
```

#### Query (streaming SSE)
```bash
curl -N -X POST http://localhost:8080/api/v1/rag/query/stream \
  -H "Content-Type: application/json" \
  -d '{"question": "Summarize the main topics"}'
```

### Architecture

```
spring-ai-rag-hub/
├── rag-core/        # Domain model + port interfaces (no framework deps)
├── rag-ingestion/   # Tika reader → token splitter → pgvector
├── rag-retrieval/   # QuestionAnswerAdvisor RAG pipeline
├── rag-providers/   # OpenAI / Anthropic / Ollama adapter config
└── rag-api/         # Spring MVC REST + SSE controllers (runnable jar)
```

### Configuration

| Variable | Default | Description |
|---|---|---|
| `RAG_PROVIDER` | `openai` | `openai` \| `anthropic` \| `ollama` |
| `OPENAI_API_KEY` | — | Required when `RAG_PROVIDER=openai` |
| `ANTHROPIC_API_KEY` | — | Required when `RAG_PROVIDER=anthropic` |
| `OLLAMA_BASE_URL` | `http://localhost:11434` | Ollama base URL |

### Tech Stack

| Layer | Technology |
|---|---|
| Framework | Spring Boot 3.4.4 |
| AI Abstraction | Spring AI 1.0.0 GA |
| Vector Store | PostgreSQL 16 + pgvector |
| Document Parsing | Apache Tika |
| Streaming | Project Reactor (Flux) |

### Contributing

1. Fork the repo
2. Create a feature branch (`git checkout -b feat/your-feature`)
3. Commit your changes
4. Open a Pull Request

Please open an issue first for major changes.

---

## 中文

### 项目简介

Spring AI RAG Hub 是一个基于 **Spring Boot 3.4** + **Spring AI 1.0** + **pgvector** 构建的模块化、生产就绪的检索增强生成（RAG）知识库系统。支持 OpenAI、Anthropic Claude、Ollama 多个 AI 提供商，通过单个环境变量即可切换，无需修改代码。

### 核心特性

- **多提供商支持** — 通过 `RAG_PROVIDER=xxx` 一键切换 OpenAI / Anthropic / Ollama
- **流式输出** — 基于 SSE 的 Token 流式响应，使用 `Flux<String>` 实现
- **六边形架构** — `rag-core` 模块零框架依赖，所有端口均为接口定义
- **任意文件格式** — 通过 Apache Tika 支持 PDF、DOCX、HTML、Markdown 等格式
- **pgvector 向量库** — PostgreSQL 向量存储，自动初始化 Schema
- **可观测性** — 开箱即用的 Spring Boot Actuator 监控端点

### 快速开始

**前置条件：** Docker、Java 17+、Maven 3.9+

```bash
# 1. 克隆项目
git clone https://github.com/your-org/spring-ai-rag-hub.git
cd spring-ai-rag-hub

# 2. 配置环境变量
cp .env.example .env
# 编辑 .env，填入 RAG_PROVIDER 和对应的 API Key

# 3. 启动 PostgreSQL（含 pgvector）
docker compose up postgres -d

# 4. 启动应用
./mvnw spring-boot:run -pl rag-api
```

> **没有 API Key？** 使用本地 Ollama：
> ```bash
> docker compose --profile ollama up -d
> RAG_PROVIDER=ollama ./mvnw spring-boot:run -pl rag-api
> ```

### API 接口

#### 上传文档
```bash
curl -X POST http://localhost:8080/api/v1/documents/upload \
  -F "file=@knowledge.pdf" \
  -F "title=我的知识库"
```

#### 问答查询（同步）
```bash
curl -X POST http://localhost:8080/api/v1/rag/query \
  -H "Content-Type: application/json" \
  -d '{"question": "退款政策是什么？"}'
```

#### 问答查询（流式 SSE）
```bash
curl -N -X POST http://localhost:8080/api/v1/rag/query/stream \
  -H "Content-Type: application/json" \
  -d '{"question": "总结主要内容"}'
```

### 项目架构

```
spring-ai-rag-hub/
├── rag-core/        # 领域模型 + 端口接口（零框架依赖）
├── rag-ingestion/   # Tika 文档解析 → Token 分割 → pgvector 存储
├── rag-retrieval/   # QuestionAnswerAdvisor RAG 检索 Pipeline
├── rag-providers/   # OpenAI / Anthropic / Ollama 适配器配置
└── rag-api/         # Spring MVC REST + SSE 控制器（可运行 Jar）
```

### 环境变量配置

| 变量名 | 默认值 | 说明 |
|---|---|---|
| `RAG_PROVIDER` | `openai` | `openai` \| `anthropic` \| `ollama` |
| `OPENAI_API_KEY` | — | 使用 OpenAI 时必填 |
| `ANTHROPIC_API_KEY` | — | 使用 Anthropic 时必填 |
| `OLLAMA_BASE_URL` | `http://localhost:11434` | Ollama 服务地址 |

### 技术栈

| 层次 | 技术选型 |
|---|---|
| 应用框架 | Spring Boot 3.4.4 |
| AI 抽象层 | Spring AI 1.0.0 GA |
| 向量数据库 | PostgreSQL 16 + pgvector |
| 文档解析 | Apache Tika |
| 响应式流 | Project Reactor (Flux) |

### 参与贡献

1. Fork 本仓库
2. 创建特性分支（`git checkout -b feat/your-feature`）
3. 提交代码
4. 发起 Pull Request

重大变更请先开 Issue 讨论。

---

## License

[Apache 2.0](LICENSE) © 2026 Spring AI RAG Hub Contributors

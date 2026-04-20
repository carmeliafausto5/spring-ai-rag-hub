<div align="center">

# Spring AI RAG Hub

**Production-grade RAG knowledge base · 生产级 RAG 知识库 · プロダクション対応 RAG ナレッジベース · 프로덕션급 RAG 지식 베이스 · Base de connaissances RAG · RAG Wissensdatenbank · Base de conocimientos RAG · 生產級 RAG 知識庫**

[![Java](https://img.shields.io/badge/Java-17+-orange.svg)](https://openjdk.org/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.4.4-brightgreen.svg)](https://spring.io/projects/spring-boot)
[![Spring AI](https://img.shields.io/badge/Spring%20AI-1.0.0-blue.svg)](https://spring.io/projects/spring-ai)
[![License](https://img.shields.io/badge/License-Apache%202.0-blue.svg)](LICENSE)

[English](#english) · [中文](#中文) · [日本語](#日本語) · [한국어](#한국어) · [Français](#français) · [Deutsch](#deutsch) · [Español](#español) · [繁體中文](#繁體中文)

</div>

---

## English

### Overview

Spring AI RAG Hub is a modular, production-ready Retrieval-Augmented Generation (RAG) system built on **Spring Boot 3.4** + **Spring AI 1.0** + **pgvector**. It supports multiple AI providers (OpenAI, Anthropic Claude, Ollama) switchable via a single environment variable — no code changes required.

### Features

- **Multi-provider** — Switch between OpenAI / Anthropic / Ollama with `RAG_PROVIDER=xxx`
- **Streaming** — SSE-based token streaming via `Flux<StreamChunk>`
- **Conversation history** — Multi-turn Q&A with `ConversationContext`
- **Hexagonal architecture** — `rag-core` has zero framework dependencies; all ports are interfaces
- **Any file format** — PDF, DOCX, HTML, Markdown ingestion via Apache Tika
- **pgvector** — PostgreSQL vector store with automatic schema initialization
- **Document management** — List and delete ingested documents via REST API
- **Web UI** — React + TypeScript frontend with streaming chat and document management
- **Observability** — Spring Boot Actuator endpoints out of the box

### Quick Start

**Prerequisites:** Docker, Java 17+, Maven 3.9+, Node.js 18+

```bash
# 1. Clone
git clone https://github.com/carmeliafausto5/spring-ai-rag-hub.git
cd spring-ai-rag-hub

# 2. Configure
cp .env.example .env
# Edit .env — set RAG_PROVIDER and your API key

# 3. Start PostgreSQL (pgvector)
docker compose up postgres -d

# 4. Run backend
./mvnw spring-boot:run -pl rag-api

# 5. Run frontend (new terminal)
cd rag-ui && npm install && npm run dev
# Open http://localhost:5173
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

#### List documents
```bash
curl http://localhost:8080/api/v1/documents
```

#### Delete a document
```bash
curl -X DELETE http://localhost:8080/api/v1/documents/{id}
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
  -d '{"question": "Summarize the main topics", "history": []}'
```

SSE events: `event: token` (text chunk) · `event: done` (sources + latency JSON)

### Architecture

```
spring-ai-rag-hub/
├── rag-core/        # Domain model + port interfaces (no framework deps)
├── rag-ingestion/   # Tika reader → token splitter → pgvector
├── rag-retrieval/   # QuestionAnswerAdvisor RAG pipeline
├── rag-providers/   # OpenAI / Anthropic / Ollama adapter config
├── rag-api/         # Spring MVC REST + SSE controllers (runnable jar)
└── rag-ui/          # React + TypeScript frontend (Vite)
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
| Frontend | React 18 + TypeScript + Vite |

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
- **流式输出** — 基于 SSE 的 Token 流式响应，使用 `Flux<StreamChunk>` 实现
- **多轮对话** — 通过 `ConversationContext` 支持对话历史
- **六边形架构** — `rag-core` 模块零框架依赖，所有端口均为接口定义
- **任意文件格式** — 通过 Apache Tika 支持 PDF、DOCX、HTML、Markdown 等格式
- **pgvector 向量库** — PostgreSQL 向量存储，自动初始化 Schema
- **文档管理** — 通过 REST API 列出和删除已入库文档
- **Web UI** — React + TypeScript 前端，支持流式问答和文档管理
- **可观测性** — 开箱即用的 Spring Boot Actuator 监控端点

### 快速开始

**前置条件：** Docker、Java 17+、Maven 3.9+、Node.js 18+

```bash
# 1. 克隆项目
git clone https://github.com/carmeliafausto5/spring-ai-rag-hub.git
cd spring-ai-rag-hub

# 2. 配置环境变量
cp .env.example .env
# 编辑 .env，填入 RAG_PROVIDER 和对应的 API Key

# 3. 启动 PostgreSQL（含 pgvector）
docker compose up postgres -d

# 4. 启动后端
./mvnw spring-boot:run -pl rag-api

# 5. 启动前端（新终端）
cd rag-ui && npm install && npm run dev
# 访问 http://localhost:5173
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

#### 列出文档
```bash
curl http://localhost:8080/api/v1/documents
```

#### 删除文档
```bash
curl -X DELETE http://localhost:8080/api/v1/documents/{id}
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
  -d '{"question": "总结主要内容", "history": []}'
```

SSE 事件：`event: token`（文本片段）· `event: done`（来源 + 延迟 JSON）

### 项目架构

```
spring-ai-rag-hub/
├── rag-core/        # 领域模型 + 端口接口（零框架依赖）
├── rag-ingestion/   # Tika 文档解析 → Token 分割 → pgvector 存储
├── rag-retrieval/   # QuestionAnswerAdvisor RAG 检索 Pipeline
├── rag-providers/   # OpenAI / Anthropic / Ollama 适配器配置
├── rag-api/         # Spring MVC REST + SSE 控制器（可运行 Jar）
└── rag-ui/          # React + TypeScript 前端（Vite）
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
| 前端 | React 18 + TypeScript + Vite |

### 参与贡献

1. Fork 本仓库
2. 创建特性分支（`git checkout -b feat/your-feature`）
3. 提交代码
4. 发起 Pull Request

重大变更请先开 Issue 讨论。

---

## 日本語

### 概要

Spring AI RAG Hub は、**Spring Boot 3.4** + **Spring AI 1.0** + **pgvector** をベースに構築された、モジュール型のプロダクション対応 RAG（検索拡張生成）ナレッジベースシステムです。OpenAI、Anthropic Claude、Ollama など複数の AI プロバイダーをサポートし、環境変数一つで切り替え可能です。コード変更は不要です。

### 主な機能

- **マルチプロバイダー対応** — `RAG_PROVIDER=xxx` で OpenAI / Anthropic / Ollama を切り替え
- **ストリーミング** — `Flux<StreamChunk>` による SSE トークンストリーミング
- **会話履歴** — `ConversationContext` によるマルチターン Q&A
- **ヘキサゴナルアーキテクチャ** — `rag-core` はフレームワーク依存ゼロ、全ポートはインターフェース
- **任意ファイル形式** — Apache Tika による PDF、DOCX、HTML、Markdown の取り込み
- **pgvector** — PostgreSQL ベクターストア、スキーマ自動初期化
- **ドキュメント管理** — REST API によるドキュメントの一覧・削除
- **Web UI** — ストリーミングチャットとドキュメント管理を備えた React + TypeScript フロントエンド
- **オブザーバビリティ** — Spring Boot Actuator エンドポイントをすぐに利用可能

### クイックスタート

**前提条件：** Docker、Java 17+、Maven 3.9+、Node.js 18+

```bash
# 1. クローン
git clone https://github.com/carmeliafausto5/spring-ai-rag-hub.git
cd spring-ai-rag-hub

# 2. 設定
cp .env.example .env
# .env を編集 — RAG_PROVIDER と API キーを設定

# 3. PostgreSQL（pgvector）起動
docker compose up postgres -d

# 4. バックエンド起動
./mvnw spring-boot:run -pl rag-api

# 5. フロントエンド起動（別ターミナル）
cd rag-ui && npm install && npm run dev
# http://localhost:5173 を開く
```

> **API キーがない場合は** ローカル Ollama を使用：
> ```bash
> docker compose --profile ollama up -d
> RAG_PROVIDER=ollama ./mvnw spring-boot:run -pl rag-api
> ```

### API リファレンス

#### ドキュメントのアップロード
```bash
curl -X POST http://localhost:8080/api/v1/documents/upload \
  -F "file=@knowledge.pdf" \
  -F "title=ナレッジベース"
```

#### ドキュメント一覧
```bash
curl http://localhost:8080/api/v1/documents
```

#### ドキュメント削除
```bash
curl -X DELETE http://localhost:8080/api/v1/documents/{id}
```

#### 質問応答（同期）
```bash
curl -X POST http://localhost:8080/api/v1/rag/query \
  -H "Content-Type: application/json" \
  -d '{"question": "返金ポリシーは何ですか？"}'
```

#### 質問応答（ストリーミング SSE）
```bash
curl -N -X POST http://localhost:8080/api/v1/rag/query/stream \
  -H "Content-Type: application/json" \
  -d '{"question": "主なトピックをまとめてください", "history": []}'
```

SSE イベント：`event: token`（テキストチャンク）· `event: done`（ソース + レイテンシ JSON）

### アーキテクチャ

```
spring-ai-rag-hub/
├── rag-core/        # ドメインモデル + ポートインターフェース（フレームワーク依存ゼロ）
├── rag-ingestion/   # Tika ドキュメント解析 → トークン分割 → pgvector 保存
├── rag-retrieval/   # QuestionAnswerAdvisor RAG パイプライン
├── rag-providers/   # OpenAI / Anthropic / Ollama アダプター設定
├── rag-api/         # Spring MVC REST + SSE コントローラー（実行可能 Jar）
└── rag-ui/          # React + TypeScript フロントエンド（Vite）
```

### 環境変数

| 変数名 | デフォルト | 説明 |
|---|---|---|
| `RAG_PROVIDER` | `openai` | `openai` \| `anthropic` \| `ollama` |
| `OPENAI_API_KEY` | — | `RAG_PROVIDER=openai` 時に必須 |
| `ANTHROPIC_API_KEY` | — | `RAG_PROVIDER=anthropic` 時に必須 |
| `OLLAMA_BASE_URL` | `http://localhost:11434` | Ollama ベース URL |

### 技術スタック

| レイヤー | 技術 |
|---|---|
| フレームワーク | Spring Boot 3.4.4 |
| AI 抽象化 | Spring AI 1.0.0 GA |
| ベクターストア | PostgreSQL 16 + pgvector |
| ドキュメント解析 | Apache Tika |
| リアクティブストリーム | Project Reactor (Flux) |
| フロントエンド | React 18 + TypeScript + Vite |

### コントリビューション

1. リポジトリをフォーク
2. フィーチャーブランチを作成（`git checkout -b feat/your-feature`）
3. 変更をコミット
4. プルリクエストを作成

大きな変更は先に Issue を開いてご相談ください。

---

## 한국어

### 개요

Spring AI RAG Hub는 **Spring Boot 3.4** + **Spring AI 1.0** + **pgvector** 기반의 모듈형 프로덕션급 RAG(검색 증강 생성) 지식 베이스 시스템입니다. OpenAI, Anthropic Claude, Ollama 등 여러 AI 프로바이더를 지원하며, 환경 변수 하나로 전환 가능합니다. 코드 변경이 필요 없습니다.

### 주요 기능

- **멀티 프로바이더** — `RAG_PROVIDER=xxx`로 OpenAI / Anthropic / Ollama 전환
- **스트리밍** — `Flux<StreamChunk>`를 통한 SSE 기반 토큰 스트리밍
- **대화 기록** — `ConversationContext`를 통한 멀티턴 Q&A
- **헥사고날 아키텍처** — `rag-core`는 프레임워크 의존성 없음
- **모든 파일 형식** — Apache Tika를 통한 PDF, DOCX, HTML, Markdown 수집
- **pgvector** — 자동 스키마 초기화가 포함된 PostgreSQL 벡터 스토어
- **문서 관리** — REST API를 통한 문서 목록 조회 및 삭제
- **Web UI** — 스트리밍 채팅 및 문서 관리 React + TypeScript 프론트엔드
- **관찰 가능성** — Spring Boot Actuator 엔드포인트 기본 제공

### 빠른 시작

**사전 요구 사항:** Docker, Java 17+, Maven 3.9+, Node.js 18+

```bash
# 1. 클론
git clone https://github.com/carmeliafausto5/spring-ai-rag-hub.git
cd spring-ai-rag-hub

# 2. 설정
cp .env.example .env
# .env 편집 — RAG_PROVIDER 및 API 키 설정

# 3. PostgreSQL(pgvector) 시작
docker compose up postgres -d

# 4. 백엔드 실행
./mvnw spring-boot:run -pl rag-api

# 5. 프론트엔드 실행 (새 터미널)
cd rag-ui && npm install && npm run dev
# http://localhost:5173 열기
```

> **API 키가 없으신가요?** 로컬 Ollama 사용:
> ```bash
> docker compose --profile ollama up -d
> RAG_PROVIDER=ollama ./mvnw spring-boot:run -pl rag-api
> ```

### API 참조

#### 문서 업로드
```bash
curl -X POST http://localhost:8080/api/v1/documents/upload \
  -F "file=@knowledge.pdf" \
  -F "title=내 지식 베이스"
```

#### 문서 목록
```bash
curl http://localhost:8080/api/v1/documents
```

#### 문서 삭제
```bash
curl -X DELETE http://localhost:8080/api/v1/documents/{id}
```

#### 질의응답 (동기)
```bash
curl -X POST http://localhost:8080/api/v1/rag/query \
  -H "Content-Type: application/json" \
  -d '{"question": "환불 정책이 무엇인가요?"}'
```

#### 질의응답 (스트리밍 SSE)
```bash
curl -N -X POST http://localhost:8080/api/v1/rag/query/stream \
  -H "Content-Type: application/json" \
  -d '{"question": "주요 주제를 요약해 주세요", "history": []}'
```

SSE 이벤트: `event: token` (텍스트 청크) · `event: done` (소스 + 지연 JSON)

### 아키텍처

```
spring-ai-rag-hub/
├── rag-core/        # 도메인 모델 + 포트 인터페이스 (프레임워크 의존성 없음)
├── rag-ingestion/   # Tika 문서 파싱 → 토큰 분할 → pgvector 저장
├── rag-retrieval/   # QuestionAnswerAdvisor RAG 파이프라인
├── rag-providers/   # OpenAI / Anthropic / Ollama 어댑터 설정
├── rag-api/         # Spring MVC REST + SSE 컨트롤러 (실행 가능한 Jar)
└── rag-ui/          # React + TypeScript 프론트엔드 (Vite)
```

### 환경 변수 설정

| 변수 | 기본값 | 설명 |
|---|---|---|
| `RAG_PROVIDER` | `openai` | `openai` \| `anthropic` \| `ollama` |
| `OPENAI_API_KEY` | — | `RAG_PROVIDER=openai` 시 필수 |
| `ANTHROPIC_API_KEY` | — | `RAG_PROVIDER=anthropic` 시 필수 |
| `OLLAMA_BASE_URL` | `http://localhost:11434` | Ollama 기본 URL |

### 기술 스택

| 레이어 | 기술 |
|---|---|
| 프레임워크 | Spring Boot 3.4.4 |
| AI 추상화 | Spring AI 1.0.0 GA |
| 벡터 스토어 | PostgreSQL 16 + pgvector |
| 문서 파싱 | Apache Tika |
| 스트리밍 | Project Reactor (Flux) |
| 프론트엔드 | React 18 + TypeScript + Vite |

### 기여하기

1. 저장소 포크
2. 기능 브랜치 생성 (`git checkout -b feat/your-feature`)
3. 변경 사항 커밋
4. Pull Request 열기

주요 변경 사항은 먼저 Issue를 열어 논의해 주세요.

---

## Français

### Présentation

Spring AI RAG Hub est un système RAG (Génération Augmentée par Récupération) modulaire et prêt pour la production, construit sur **Spring Boot 3.4** + **Spring AI 1.0** + **pgvector**. Il prend en charge plusieurs fournisseurs d'IA (OpenAI, Anthropic Claude, Ollama) commutables via une seule variable d'environnement — aucune modification de code requise.

### Fonctionnalités

- **Multi-fournisseur** — Basculez entre OpenAI / Anthropic / Ollama avec `RAG_PROVIDER=xxx`
- **Streaming** — Streaming de tokens SSE via `Flux<StreamChunk>`
- **Historique de conversation** — Q&R multi-tours avec `ConversationContext`
- **Architecture hexagonale** — `rag-core` sans dépendances framework
- **Tout format de fichier** — Ingestion PDF, DOCX, HTML, Markdown via Apache Tika
- **pgvector** — Stockage vectoriel PostgreSQL avec initialisation automatique du schéma
- **Gestion des documents** — Lister et supprimer des documents via l'API REST
- **Interface Web** — Frontend React + TypeScript avec chat en streaming
- **Observabilité** — Endpoints Spring Boot Actuator prêts à l'emploi

### Démarrage rapide

**Prérequis :** Docker, Java 17+, Maven 3.9+, Node.js 18+

```bash
# 1. Cloner
git clone https://github.com/carmeliafausto5/spring-ai-rag-hub.git
cd spring-ai-rag-hub

# 2. Configurer
cp .env.example .env
# Éditer .env — définir RAG_PROVIDER et votre clé API

# 3. Démarrer PostgreSQL (pgvector)
docker compose up postgres -d

# 4. Lancer le backend
./mvnw spring-boot:run -pl rag-api

# 5. Lancer le frontend (nouveau terminal)
cd rag-ui && npm install && npm run dev
# Ouvrir http://localhost:5173
```

> **Pas de clé API ?** Utilisez Ollama en local :
> ```bash
> docker compose --profile ollama up -d
> RAG_PROVIDER=ollama ./mvnw spring-boot:run -pl rag-api
> ```

### Référence API

#### Téléverser un document
```bash
curl -X POST http://localhost:8080/api/v1/documents/upload \
  -F "file=@knowledge.pdf" \
  -F "title=Ma base de connaissances"
```

#### Lister les documents
```bash
curl http://localhost:8080/api/v1/documents
```

#### Supprimer un document
```bash
curl -X DELETE http://localhost:8080/api/v1/documents/{id}
```

#### Requête (synchrone)
```bash
curl -X POST http://localhost:8080/api/v1/rag/query \
  -H "Content-Type: application/json" \
  -d '{"question": "Quelle est la politique de remboursement ?"}'
```

#### Requête (streaming SSE)
```bash
curl -N -X POST http://localhost:8080/api/v1/rag/query/stream \
  -H "Content-Type: application/json" \
  -d '{"question": "Résumez les sujets principaux", "history": []}'
```

Événements SSE : `event: token` (fragment de texte) · `event: done` (sources + JSON de latence)

### Architecture

```
spring-ai-rag-hub/
├── rag-core/        # Modèle de domaine + interfaces de port (sans dépendances)
├── rag-ingestion/   # Lecture Tika → découpage en tokens → stockage pgvector
├── rag-retrieval/   # Pipeline RAG QuestionAnswerAdvisor
├── rag-providers/   # Configuration des adaptateurs OpenAI / Anthropic / Ollama
├── rag-api/         # Contrôleurs Spring MVC REST + SSE (jar exécutable)
└── rag-ui/          # Frontend React + TypeScript (Vite)
```

### Configuration

| Variable | Défaut | Description |
|---|---|---|
| `RAG_PROVIDER` | `openai` | `openai` \| `anthropic` \| `ollama` |
| `OPENAI_API_KEY` | — | Requis si `RAG_PROVIDER=openai` |
| `ANTHROPIC_API_KEY` | — | Requis si `RAG_PROVIDER=anthropic` |
| `OLLAMA_BASE_URL` | `http://localhost:11434` | URL de base Ollama |

### Stack technique

| Couche | Technologie |
|---|---|
| Framework | Spring Boot 3.4.4 |
| Abstraction IA | Spring AI 1.0.0 GA |
| Stockage vectoriel | PostgreSQL 16 + pgvector |
| Analyse de documents | Apache Tika |
| Streaming | Project Reactor (Flux) |
| Frontend | React 18 + TypeScript + Vite |

### Contribuer

1. Forker le dépôt
2. Créer une branche de fonctionnalité (`git checkout -b feat/your-feature`)
3. Commiter vos modifications
4. Ouvrir une Pull Request

Veuillez ouvrir une issue en premier pour les changements majeurs.

---

## Deutsch

### Überblick

Spring AI RAG Hub ist ein modulares, produktionsreifes RAG-System (Retrieval-Augmented Generation), das auf **Spring Boot 3.4** + **Spring AI 1.0** + **pgvector** aufbaut. Es unterstützt mehrere KI-Anbieter (OpenAI, Anthropic Claude, Ollama), die über eine einzige Umgebungsvariable umgeschaltet werden können — keine Codeänderungen erforderlich.

### Funktionen

- **Multi-Anbieter** — Wechsel zwischen OpenAI / Anthropic / Ollama mit `RAG_PROVIDER=xxx`
- **Streaming** — SSE-basiertes Token-Streaming via `Flux<StreamChunk>`
- **Gesprächsverlauf** — Mehrrunden-Q&A mit `ConversationContext`
- **Hexagonale Architektur** — `rag-core` ohne Framework-Abhängigkeiten
- **Beliebige Dateiformate** — PDF, DOCX, HTML, Markdown-Ingestion via Apache Tika
- **pgvector** — PostgreSQL-Vektorspeicher mit automatischer Schema-Initialisierung
- **Dokumentenverwaltung** — Dokumente über REST API auflisten und löschen
- **Web-Oberfläche** — React + TypeScript Frontend mit Streaming-Chat
- **Beobachtbarkeit** — Spring Boot Actuator-Endpunkte sofort verfügbar

### Schnellstart

**Voraussetzungen:** Docker, Java 17+, Maven 3.9+, Node.js 18+

```bash
# 1. Klonen
git clone https://github.com/carmeliafausto5/spring-ai-rag-hub.git
cd spring-ai-rag-hub

# 2. Konfigurieren
cp .env.example .env
# .env bearbeiten — RAG_PROVIDER und API-Schlüssel setzen

# 3. PostgreSQL (pgvector) starten
docker compose up postgres -d

# 4. Backend starten
./mvnw spring-boot:run -pl rag-api

# 5. Frontend starten (neues Terminal)
cd rag-ui && npm install && npm run dev
# http://localhost:5173 öffnen
```

> **Kein API-Schlüssel?** Ollama lokal verwenden:
> ```bash
> docker compose --profile ollama up -d
> RAG_PROVIDER=ollama ./mvnw spring-boot:run -pl rag-api
> ```

### API-Referenz

#### Dokument hochladen
```bash
curl -X POST http://localhost:8080/api/v1/documents/upload \
  -F "file=@knowledge.pdf" \
  -F "title=Meine Wissensdatenbank"
```

#### Dokumente auflisten
```bash
curl http://localhost:8080/api/v1/documents
```

#### Dokument löschen
```bash
curl -X DELETE http://localhost:8080/api/v1/documents/{id}
```

#### Abfrage (synchron)
```bash
curl -X POST http://localhost:8080/api/v1/rag/query \
  -H "Content-Type: application/json" \
  -d '{"question": "Was ist die Rückgaberichtlinie?"}'
```

#### Abfrage (Streaming SSE)
```bash
curl -N -X POST http://localhost:8080/api/v1/rag/query/stream \
  -H "Content-Type: application/json" \
  -d '{"question": "Fassen Sie die Hauptthemen zusammen", "history": []}'
```

SSE-Ereignisse: `event: token` (Textfragment) · `event: done` (Quellen + Latenz-JSON)

### Architektur

```
spring-ai-rag-hub/
├── rag-core/        # Domänenmodell + Port-Interfaces (keine Framework-Abhängigkeiten)
├── rag-ingestion/   # Tika-Leser → Token-Splitter → pgvector-Speicher
├── rag-retrieval/   # QuestionAnswerAdvisor RAG-Pipeline
├── rag-providers/   # OpenAI / Anthropic / Ollama Adapter-Konfiguration
├── rag-api/         # Spring MVC REST + SSE Controller (ausführbares Jar)
└── rag-ui/          # React + TypeScript Frontend (Vite)
```

### Konfiguration

| Variable | Standard | Beschreibung |
|---|---|---|
| `RAG_PROVIDER` | `openai` | `openai` \| `anthropic` \| `ollama` |
| `OPENAI_API_KEY` | — | Erforderlich bei `RAG_PROVIDER=openai` |
| `ANTHROPIC_API_KEY` | — | Erforderlich bei `RAG_PROVIDER=anthropic` |
| `OLLAMA_BASE_URL` | `http://localhost:11434` | Ollama-Basis-URL |

### Technologie-Stack

| Schicht | Technologie |
|---|---|
| Framework | Spring Boot 3.4.4 |
| KI-Abstraktion | Spring AI 1.0.0 GA |
| Vektorspeicher | PostgreSQL 16 + pgvector |
| Dokumentenanalyse | Apache Tika |
| Streaming | Project Reactor (Flux) |
| Frontend | React 18 + TypeScript + Vite |

### Mitwirken

1. Repository forken
2. Feature-Branch erstellen (`git checkout -b feat/your-feature`)
3. Änderungen committen
4. Pull Request öffnen

Bitte zuerst ein Issue für größere Änderungen öffnen.

---

## Español

### Descripción general

Spring AI RAG Hub es un sistema RAG (Generación Aumentada por Recuperación) modular y listo para producción, construido sobre **Spring Boot 3.4** + **Spring AI 1.0** + **pgvector**. Soporta múltiples proveedores de IA (OpenAI, Anthropic Claude, Ollama) intercambiables mediante una sola variable de entorno — sin cambios de código.

### Características

- **Multi-proveedor** — Cambia entre OpenAI / Anthropic / Ollama con `RAG_PROVIDER=xxx`
- **Streaming** — Streaming de tokens SSE via `Flux<StreamChunk>`
- **Historial de conversación** — Q&A multi-turno con `ConversationContext`
- **Arquitectura hexagonal** — `rag-core` sin dependencias de framework
- **Cualquier formato de archivo** — Ingesta de PDF, DOCX, HTML, Markdown via Apache Tika
- **pgvector** — Almacén vectorial PostgreSQL con inicialización automática de esquema
- **Gestión de documentos** — Listar y eliminar documentos via API REST
- **Interfaz Web** — Frontend React + TypeScript con chat en streaming
- **Observabilidad** — Endpoints de Spring Boot Actuator listos para usar

### Inicio rápido

**Requisitos previos:** Docker, Java 17+, Maven 3.9+, Node.js 18+

```bash
# 1. Clonar
git clone https://github.com/carmeliafausto5/spring-ai-rag-hub.git
cd spring-ai-rag-hub

# 2. Configurar
cp .env.example .env
# Editar .env — establecer RAG_PROVIDER y tu clave API

# 3. Iniciar PostgreSQL (pgvector)
docker compose up postgres -d

# 4. Ejecutar backend
./mvnw spring-boot:run -pl rag-api

# 5. Ejecutar frontend (nueva terminal)
cd rag-ui && npm install && npm run dev
# Abrir http://localhost:5173
```

> **¿Sin clave API?** Usa Ollama localmente:
> ```bash
> docker compose --profile ollama up -d
> RAG_PROVIDER=ollama ./mvnw spring-boot:run -pl rag-api
> ```

### Referencia de API

#### Subir un documento
```bash
curl -X POST http://localhost:8080/api/v1/documents/upload \
  -F "file=@knowledge.pdf" \
  -F "title=Mi base de conocimientos"
```

#### Listar documentos
```bash
curl http://localhost:8080/api/v1/documents
```

#### Eliminar un documento
```bash
curl -X DELETE http://localhost:8080/api/v1/documents/{id}
```

#### Consulta (síncrona)
```bash
curl -X POST http://localhost:8080/api/v1/rag/query \
  -H "Content-Type: application/json" \
  -d '{"question": "¿Cuál es la política de reembolso?"}'
```

#### Consulta (streaming SSE)
```bash
curl -N -X POST http://localhost:8080/api/v1/rag/query/stream \
  -H "Content-Type: application/json" \
  -d '{"question": "Resume los temas principales", "history": []}'
```

Eventos SSE: `event: token` (fragmento de texto) · `event: done` (fuentes + JSON de latencia)

### Arquitectura

```
spring-ai-rag-hub/
├── rag-core/        # Modelo de dominio + interfaces de puerto (sin dependencias)
├── rag-ingestion/   # Lector Tika → divisor de tokens → almacén pgvector
├── rag-retrieval/   # Pipeline RAG QuestionAnswerAdvisor
├── rag-providers/   # Configuración de adaptadores OpenAI / Anthropic / Ollama
├── rag-api/         # Controladores Spring MVC REST + SSE (jar ejecutable)
└── rag-ui/          # Frontend React + TypeScript (Vite)
```

### Configuración

| Variable | Predeterminado | Descripción |
|---|---|---|
| `RAG_PROVIDER` | `openai` | `openai` \| `anthropic` \| `ollama` |
| `OPENAI_API_KEY` | — | Requerido cuando `RAG_PROVIDER=openai` |
| `ANTHROPIC_API_KEY` | — | Requerido cuando `RAG_PROVIDER=anthropic` |
| `OLLAMA_BASE_URL` | `http://localhost:11434` | URL base de Ollama |

### Stack tecnológico

| Capa | Tecnología |
|---|---|
| Framework | Spring Boot 3.4.4 |
| Abstracción IA | Spring AI 1.0.0 GA |
| Almacén vectorial | PostgreSQL 16 + pgvector |
| Análisis de documentos | Apache Tika |
| Streaming | Project Reactor (Flux) |
| Frontend | React 18 + TypeScript + Vite |

### Contribuir

1. Hacer fork del repositorio
2. Crear una rama de funcionalidad (`git checkout -b feat/your-feature`)
3. Hacer commit de los cambios
4. Abrir un Pull Request

Por favor, abre un issue primero para cambios importantes.

---

## 繁體中文

### 專案簡介

Spring AI RAG Hub 是一個基於 **Spring Boot 3.4** + **Spring AI 1.0** + **pgvector** 構建的模組化、生產就緒的檢索增強生成（RAG）知識庫系統。支援 OpenAI、Anthropic Claude、Ollama 多個 AI 供應商，透過單一環境變數即可切換，無需修改程式碼。

### 核心特性

- **多供應商支援** — 透過 `RAG_PROVIDER=xxx` 一鍵切換 OpenAI / Anthropic / Ollama
- **串流輸出** — 基於 SSE 的 Token 串流回應，使用 `Flux<StreamChunk>` 實現
- **多輪對話** — 透過 `ConversationContext` 支援對話歷史
- **六邊形架構** — `rag-core` 模組零框架依賴，所有埠均為介面定義
- **任意檔案格式** — 透過 Apache Tika 支援 PDF、DOCX、HTML、Markdown 等格式
- **pgvector 向量庫** — PostgreSQL 向量儲存，自動初始化 Schema
- **文件管理** — 透過 REST API 列出和刪除已入庫文件
- **Web UI** — React + TypeScript 前端，支援串流問答和文件管理
- **可觀測性** — 開箱即用的 Spring Boot Actuator 監控端點

### 快速開始

**前置條件：** Docker、Java 17+、Maven 3.9+、Node.js 18+

```bash
# 1. 克隆專案
git clone https://github.com/carmeliafausto5/spring-ai-rag-hub.git
cd spring-ai-rag-hub

# 2. 設定環境變數
cp .env.example .env
# 編輯 .env，填入 RAG_PROVIDER 和對應的 API Key

# 3. 啟動 PostgreSQL（含 pgvector）
docker compose up postgres -d

# 4. 啟動後端
./mvnw spring-boot:run -pl rag-api

# 5. 啟動前端（新終端機）
cd rag-ui && npm install && npm run dev
# 開啟 http://localhost:5173
```

> **沒有 API Key？** 使用本地 Ollama：
> ```bash
> docker compose --profile ollama up -d
> RAG_PROVIDER=ollama ./mvnw spring-boot:run -pl rag-api
> ```

### API 介面

#### 上傳文件
```bash
curl -X POST http://localhost:8080/api/v1/documents/upload \
  -F "file=@knowledge.pdf" \
  -F "title=我的知識庫"
```

#### 列出文件
```bash
curl http://localhost:8080/api/v1/documents
```

#### 刪除文件
```bash
curl -X DELETE http://localhost:8080/api/v1/documents/{id}
```

#### 問答查詢（同步）
```bash
curl -X POST http://localhost:8080/api/v1/rag/query \
  -H "Content-Type: application/json" \
  -d '{"question": "退款政策是什麼？"}'
```

#### 問答查詢（串流 SSE）
```bash
curl -N -X POST http://localhost:8080/api/v1/rag/query/stream \
  -H "Content-Type: application/json" \
  -d '{"question": "總結主要內容", "history": []}'
```

SSE 事件：`event: token`（文字片段）· `event: done`（來源 + 延遲 JSON）

### 專案架構

```
spring-ai-rag-hub/
├── rag-core/        # 領域模型 + 埠介面（零框架依賴）
├── rag-ingestion/   # Tika 文件解析 → Token 分割 → pgvector 儲存
├── rag-retrieval/   # QuestionAnswerAdvisor RAG 檢索 Pipeline
├── rag-providers/   # OpenAI / Anthropic / Ollama 適配器設定
├── rag-api/         # Spring MVC REST + SSE 控制器（可執行 Jar）
└── rag-ui/          # React + TypeScript 前端（Vite）
```

### 環境變數設定

| 變數名稱 | 預設值 | 說明 |
|---|---|---|
| `RAG_PROVIDER` | `openai` | `openai` \| `anthropic` \| `ollama` |
| `OPENAI_API_KEY` | — | 使用 OpenAI 時必填 |
| `ANTHROPIC_API_KEY` | — | 使用 Anthropic 時必填 |
| `OLLAMA_BASE_URL` | `http://localhost:11434` | Ollama 服務位址 |

### 技術堆疊

| 層次 | 技術選型 |
|---|---|
| 應用框架 | Spring Boot 3.4.4 |
| AI 抽象層 | Spring AI 1.0.0 GA |
| 向量資料庫 | PostgreSQL 16 + pgvector |
| 文件解析 | Apache Tika |
| 響應式串流 | Project Reactor (Flux) |
| 前端 | React 18 + TypeScript + Vite |

### 參與貢獻

1. Fork 本倉庫
2. 建立功能分支（`git checkout -b feat/your-feature`）
3. 提交程式碼
4. 發起 Pull Request

重大變更請先開 Issue 討論。

---

## License

[Apache 2.0](LICENSE) © 2026 Spring AI RAG Hub Contributors

-- Baseline: tables already created by Spring AI pgvector auto-init and docker/init.sql
-- This migration documents the existing schema for Flyway baseline

CREATE EXTENSION IF NOT EXISTS vector;

CREATE TABLE IF NOT EXISTS vector_store (
    id UUID PRIMARY KEY,
    content TEXT,
    metadata JSONB,
    embedding vector(1536)
);

CREATE INDEX IF NOT EXISTS vector_store_embedding_idx ON vector_store USING ivfflat (embedding vector_cosine_ops);

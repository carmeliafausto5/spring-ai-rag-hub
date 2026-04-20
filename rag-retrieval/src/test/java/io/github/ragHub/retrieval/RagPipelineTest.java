package io.github.ragHub.retrieval;

import io.github.ragHub.core.domain.SearchMode;
import io.github.ragHub.core.port.ProviderSettingsPort;
import io.github.ragHub.retrieval.pipeline.RagPipeline;
import io.github.ragHub.retrieval.reranker.KeywordReranker;
import io.github.ragHub.retrieval.retriever.BM25Retriever;
import org.junit.jupiter.api.Test;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.VectorStore;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class RagPipelineTest {

    private record PipelineFixture(RagPipeline pipeline, BM25Retriever bm25, VectorStore vs) {}

    private PipelineFixture buildFixture() {
        var builder = mock(ChatClient.Builder.class, RETURNS_DEEP_STUBS);
        when(builder.defaultSystem(anyString())).thenReturn(builder);
        when(builder.defaultAdvisors(anyList())).thenReturn(builder);

        var chatClient = mock(ChatClient.class, RETURNS_DEEP_STUBS);
        when(chatClient.prompt().messages(anyList()).user(anyString()).call().content()).thenReturn("answer");
        when(builder.build()).thenReturn(chatClient);

        var vs = mock(VectorStore.class);
        when(vs.similaritySearch(any())).thenReturn(List.of());

        var bm25 = mock(BM25Retriever.class);
        when(bm25.retrieve(anyString(), anyInt())).thenReturn(List.of());

        var settings = mock(ProviderSettingsPort.class);
        when(settings.get("rag.provider")).thenReturn("openai");

        return new PipelineFixture(
                new RagPipeline(builder, vs, bm25, new KeywordReranker(), settings),
                bm25, vs);
    }

    @Test
    void pipeline_constructsSuccessfully() {
        assertThat(buildFixture().pipeline()).isNotNull();
    }

    @Test
    void reranker_sortsDocumentsByKeywordOverlap() {
        var reranker = new KeywordReranker();
        var doc1 = new Document("spring ai vector store");
        var doc2 = new Document("unrelated content here");
        var result = reranker.rerank("spring vector", List.of(doc2, doc1));
        assertThat(result.get(0).getText()).contains("spring");
    }

    @Test
    void rrfFuse_deduplicatesAndRanksCorrectly() {
        var f = buildFixture();
        var sharedDoc = new Document("shared-id", "shared content", java.util.Map.of());
        // same doc appears rank-0 in both vector and BM25 results
        when(f.vs().similaritySearch(any())).thenReturn(List.of(sharedDoc));
        when(f.bm25().retrieve(anyString(), anyInt())).thenReturn(List.of(sharedDoc));

        var answer = f.pipeline().query("test", null, SearchMode.HYBRID);

        // sources are derived from reranked docs; shared doc must appear exactly once
        long count = answer.sources().stream()
                .filter(s -> "shared-id".equals(s.documentId()))
                .count();
        assertThat(count).isEqualTo(1);
        // and it must be first (highest RRF score = 2 * 1/61)
        assertThat(answer.sources().get(0).documentId()).isEqualTo("shared-id");
    }

    @Test
    void bm25Path_callsBm25Retriever() {
        var f = buildFixture();
        var doc = new Document("bm25-id", "bm25 result", java.util.Map.of());
        when(f.bm25().retrieve(anyString(), anyInt())).thenReturn(List.of(doc));

        f.pipeline().query("some question", null, SearchMode.BM25);

        verify(f.bm25()).retrieve(eq("some question"), anyInt());
    }
}

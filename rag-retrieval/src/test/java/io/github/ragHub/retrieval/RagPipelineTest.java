package io.github.ragHub.retrieval;

import io.github.ragHub.core.port.ProviderSettingsPort;
import io.github.ragHub.retrieval.pipeline.RagPipeline;
import io.github.ragHub.retrieval.reranker.KeywordReranker;
import org.junit.jupiter.api.Test;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.vectorstore.VectorStore;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

class RagPipelineTest {

    private RagPipeline buildPipeline() {
        var builder = mock(ChatClient.Builder.class, RETURNS_DEEP_STUBS);
        when(builder.defaultSystem(anyString())).thenReturn(builder);
        when(builder.defaultAdvisors(anyList())).thenReturn(builder);
        when(builder.build()).thenReturn(mock(ChatClient.class, RETURNS_DEEP_STUBS));
        var settings = mock(ProviderSettingsPort.class);
        when(settings.get("rag.provider")).thenReturn("openai");
        return new RagPipeline(builder, mock(VectorStore.class), new KeywordReranker(), settings);
    }

    @Test
    void pipeline_constructsSuccessfully() {
        assertThat(buildPipeline()).isNotNull();
    }

    @Test
    void reranker_sortsDocumentsByKeywordOverlap() {
        var reranker = new KeywordReranker();
        var doc1 = new org.springframework.ai.document.Document("spring ai vector store");
        var doc2 = new org.springframework.ai.document.Document("unrelated content here");
        var result = reranker.rerank("spring vector", java.util.List.of(doc2, doc1));
        assertThat(result.get(0).getText()).contains("spring");
    }
}

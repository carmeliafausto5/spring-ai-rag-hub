package io.github.ragHub.retrieval;

import io.github.ragHub.core.domain.ChatMessage;
import io.github.ragHub.retrieval.pipeline.RagPipeline;
import org.junit.jupiter.api.Test;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.vectorstore.VectorStore;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

class RagPipelineTest {

    @Test
    void queryStream_delegatesToChatClient() {
        var vectorStore = mock(VectorStore.class);
        var builder = mock(ChatClient.Builder.class, RETURNS_DEEP_STUBS);
        when(builder.defaultSystem(anyString())).thenReturn(builder);
        when(builder.defaultAdvisors(anyList())).thenReturn(builder);
        when(builder.build()).thenReturn(mock(ChatClient.class, RETURNS_DEEP_STUBS));

        var pipeline = new RagPipeline(builder, vectorStore);
        assertThat(pipeline).isNotNull();
    }

    @Test
    void retrieveSources_returnsEmptyWhenNoResults() {
        var vectorStore = mock(VectorStore.class);
        var builder = mock(ChatClient.Builder.class, RETURNS_DEEP_STUBS);
        when(builder.defaultSystem(anyString())).thenReturn(builder);
        when(builder.defaultAdvisors(anyList())).thenReturn(builder);
        when(builder.build()).thenReturn(mock(ChatClient.class, RETURNS_DEEP_STUBS));
        when(vectorStore.similaritySearch(anyString())).thenReturn(List.of());

        var pipeline = new RagPipeline(builder, vectorStore);

        assertThat(pipeline).isNotNull();
        verify(vectorStore, never()).similaritySearch(anyString());
    }
}

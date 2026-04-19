package io.github.ragHub.providers;

import org.springframework.ai.anthropic.AnthropicChatModel;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.ollama.OllamaChatModel;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

@Configuration
public class ProviderConfig {

    @Bean
    @Primary
    @ConditionalOnProperty(name = "rag.provider", havingValue = "openai", matchIfMissing = true)
    public ChatModel openAiChatModel(OpenAiChatModel model) {
        return model;
    }

    @Bean
    @Primary
    @ConditionalOnProperty(name = "rag.provider", havingValue = "anthropic")
    public ChatModel anthropicChatModel(AnthropicChatModel model) {
        return model;
    }

    @Bean
    @Primary
    @ConditionalOnProperty(name = "rag.provider", havingValue = "ollama")
    public ChatModel ollamaChatModel(OllamaChatModel model) {
        return model;
    }
}

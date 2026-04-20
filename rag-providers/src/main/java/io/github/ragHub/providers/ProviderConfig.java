package io.github.ragHub.providers;

import io.github.ragHub.core.port.ProviderSettingsPort;
import org.springframework.ai.anthropic.AnthropicChatModel;
import org.springframework.ai.anthropic.AnthropicChatOptions;
import org.springframework.ai.anthropic.api.AnthropicApi;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.ollama.OllamaChatModel;
import org.springframework.ai.ollama.api.OllamaApi;
import org.springframework.ai.ollama.api.OllamaOptions;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.ai.openai.OpenAiChatOptions;
import org.springframework.ai.openai.api.OpenAiApi;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

@Configuration
public class ProviderConfig {

    @Bean
    @Primary
    public ChatModel chatModel(ProviderSettingsPort settings) {
        return switch (settings.get("rag.provider")) {
            case "anthropic" -> AnthropicChatModel.builder()
                    .anthropicApi(AnthropicApi.builder()
                            .baseUrl(settings.get("anthropic.base-url"))
                            .apiKey(settings.get("anthropic.api-key"))
                            .build())
                    .defaultOptions(AnthropicChatOptions.builder()
                            .model(settings.get("anthropic.model")).build())
                    .build();
            case "ollama" -> OllamaChatModel.builder()
                    .ollamaApi(OllamaApi.builder()
                            .baseUrl(settings.get("ollama.base-url"))
                            .build())
                    .defaultOptions(OllamaOptions.builder()
                            .model(settings.get("ollama.model")).build())
                    .build();
            default -> OpenAiChatModel.builder()
                    .openAiApi(OpenAiApi.builder()
                            .baseUrl(settings.get("openai.base-url"))
                            .apiKey(settings.get("openai.api-key"))
                            .build())
                    .defaultOptions(OpenAiChatOptions.builder()
                            .model(settings.get("openai.model")).build())
                    .build();
        };
    }
}

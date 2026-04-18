package owpk.jloki.advisor.llm.client;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import owpk.jloki.advisor.llm.LlmAdvisorConfig;
import owpk.jloki.advisor.llm.client.dto.ChatRequest;
import owpk.jloki.advisor.llm.client.dto.ChatResponse;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;
import tools.jackson.databind.ObjectMapper;

public class OpenAiClient {

    private static final Logger log = LoggerFactory.getLogger(OpenAiClient.class);

    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;
    private final LlmAdvisorConfig config;

    public OpenAiClient(LlmAdvisorConfig config) {
        this.config = config;
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(config.timeout())
                .build();
        this.objectMapper = new ObjectMapper();
    }

    public Mono<String> complete(String userContent) {
        var chatRequest = new ChatRequest(
                config.model(),
                List.of(
                        new ChatRequest.Message("system", config.systemPrompt()),
                        new ChatRequest.Message("user", userContent)),
                null,
                null);

        return Mono.fromCallable(() -> objectMapper.writeValueAsString(chatRequest))
                .flatMap(body -> {
                    var httpRequest = HttpRequest.newBuilder()
                            .uri(URI.create(config.baseUrl() + "/v1/chat/completions"))
                            .header("Content-Type", "application/json")
                            .timeout(config.timeout())
                            .POST(HttpRequest.BodyPublishers.ofString(body))
                            .build();

                    return Mono.fromFuture(
                            httpClient.sendAsync(httpRequest, HttpResponse.BodyHandlers.ofString()));
                })
                .map(response -> {
                    if (response.statusCode() != 200) {
                        throw new RuntimeException("LLM API error: HTTP " + response.statusCode()
                                + " — " + response.body());
                    }
                    try {
                        var chatResponse = objectMapper.readValue(response.body(), ChatResponse.class);
                        return chatResponse.choices().get(0).message().content();
                    } catch (Exception e) {
                        throw new RuntimeException("Failed to parse LLM response: " + response.body(), e);
                    }
                })
                .doOnError(e -> log.error("LLM request failed: {}", e.getMessage()))
                .subscribeOn(Schedulers.boundedElastic());
    }
}

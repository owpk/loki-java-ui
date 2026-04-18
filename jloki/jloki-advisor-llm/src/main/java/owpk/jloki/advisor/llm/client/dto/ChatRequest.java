package owpk.jloki.advisor.llm.client.dto;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

public record ChatRequest(
        String model,
        List<Message> messages,
        @JsonProperty("max_tokens") Integer maxTokens,
        Double temperature) {

    public record Message(String role, String content) {}
}

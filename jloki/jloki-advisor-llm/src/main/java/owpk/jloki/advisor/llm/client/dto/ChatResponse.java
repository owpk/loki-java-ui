package owpk.jloki.advisor.llm.client.dto;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record ChatResponse(List<Choice> choices) {

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Choice(Message message) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Message(String content) {}
}

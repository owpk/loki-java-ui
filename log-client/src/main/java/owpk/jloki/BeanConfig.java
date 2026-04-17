package owpk.jloki;

import java.util.List;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.socket.client.ReactorNettyWebSocketClient;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import owpk.jloki.core.LokiTemplate;
import owpk.jloki.core.advisor.defaults.BatchAdvisor;
import reactor.netty.http.client.HttpClient;
import reactor.netty.http.client.WebsocketClientSpec;

@Configuration
public class BeanConfig {

    @Bean
    ReactorNettyWebSocketClient reactorNettyWebSocketClient() {
        var spec = WebsocketClientSpec.builder().maxFramePayloadLength(10048576);
        var httpClient = HttpClient.create();
        return new ReactorNettyWebSocketClient(httpClient, () -> spec);
    }

    @Bean
    LokiTemplate lokiTemplate(ReactorNettyWebSocketClient webSocketClient) {
        var lokiUrl = "http://localhost:3100";
        var mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        var wc = WebClient.create();
        return new LokiTemplate(webSocketClient, wc,
                mapper, lokiUrl,
                List.of(new BatchAdvisor(5, list -> System.out.println("--- Batch received: " + list + "\n"))));
    }

}

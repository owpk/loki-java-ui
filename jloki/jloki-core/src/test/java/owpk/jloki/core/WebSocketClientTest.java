package owpk.jloki.core;

import org.junit.jupiter.api.Test;
import reactor.core.publisher.Flux;
import reactor.test.StepVerifier;

import java.net.URI;

class WebSocketClientTest {
    @Test
    void interfaceShouldBeImplementable() {
        WebSocketClient mockClient = new WebSocketClient() {
            @Override
            public Flux<String> connectAndReceive(URI uri) {
                return Flux.just("test");
            }
        };

        StepVerifier.create(mockClient.connectAndReceive(URI.create("ws://test")))
                .expectNext("test")
                .verifyComplete();
    }
}

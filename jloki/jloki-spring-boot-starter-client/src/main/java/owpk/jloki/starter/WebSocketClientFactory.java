package owpk.jloki.starter;

import org.springframework.web.reactive.socket.WebSocketMessage;
import org.springframework.web.reactive.socket.client.ReactorNettyWebSocketClient;

import io.netty.resolver.DefaultAddressResolverGroup;
import owpk.jloki.core.WebSocketClient;
import reactor.core.publisher.Flux;
import reactor.netty.http.client.HttpClient;

/**
 * Фабрика создания WebSocketClient реализаций на основе Reactor Netty.
 */
public class WebSocketClientFactory {

    public WebSocketClient createReactorNettyClient() {
        var reactorClient = new ReactorNettyWebSocketClient(
                HttpClient.create()
                        .resolver(DefaultAddressResolverGroup.INSTANCE)
        );

        return uri -> Flux.<String>create(sink -> {
            reactorClient.execute(uri, session -> session.receive()
                    .doOnNext(msg -> {
                        if (msg.getType() == WebSocketMessage.Type.TEXT) {
                            sink.next(msg.getPayloadAsText());
                        }
                    })
                    .doOnError(sink::error)
                    .doOnComplete(sink::complete)
                    .then()
            ).subscribe();
        });
    }
}

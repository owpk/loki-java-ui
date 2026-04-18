package owpk.jloki.core;

import java.net.URI;
import reactor.core.publisher.Flux;

/**
 * Абстракция над WebSocket клиентом для независимости от Reactor Netty.
 * Позволяет подменять реализацию для тестирования и кроссплатформенности.
 */
public interface WebSocketClient {
    /**
     * Подключается к WebSocket URI и возвращает поток текстовых сообщений.
     *
     * @param uri WebSocket URI для подключения
     * @return Flux строк - полученных сообщений
     */
    Flux<String> connectAndReceive(URI uri);
}

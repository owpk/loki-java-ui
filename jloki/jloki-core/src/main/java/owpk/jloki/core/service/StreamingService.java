package owpk.jloki.core.service;

import org.jspecify.annotations.NonNull;

import owpk.jloki.core.dsl.LokiTailRequest;
import owpk.jloki.core.model.LogFilterStreamRequest;
import reactor.core.publisher.Flux;

/**
 * Сервис для стриминга логов из Loki.
 * Интерфейс позволяет пользователю реализовать свою собственную логику стриминга.
 */
public interface StreamingService<T> {

    /**
     * Возвращает поток событий логов на основе фильтров.
     *
     * @param filter  фильтры для запроса
     * @param delaySec задержка в секундах для ускорения логгеров
     */
    Flux<T> stream(@NonNull LogFilterStreamRequest filter, int delaySec);

    Flux<T> stream(@NonNull LokiTailRequest request);
}

package owpk.jloki.core.service;

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
     * @return поток LogEvent
     */
    Flux<T> stream(LogFilterStreamRequest filter, int delaySec);
}

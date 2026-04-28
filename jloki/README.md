# jloki

Реактивная Java-библиотека для работы с [Grafana Loki](https://grafana.com/oss/loki/), построенная на Spring Boot и Project Reactor.  
Предоставляет fluent DSL для построения LogQL-запросов, стриминг логов через WebSocket, расширяемый advisor-пайплайн и опциональный LLM-анализ логов.

---

## Модули

| Модуль | Описание |
|--------|----------|
| `jloki-core-api` | Публичные интерфейсы и модели |
| `jloki-core` | Основная реализация: `LokiTemplate`, LogQL DSL, advisors |
| `jloki-spring-boot-starter-client` | Spring Boot автоконфигурация клиента |
| `jloki-spring-boot-starter-web` | REST-эндпоинты для доступа к Loki через HTTP/SSE |
| `jloki-advisor-llm` | Advisor для анализа логов через OpenAI-совместимый LLM |

---

## Требования

- Java 25+
- Spring Boot 4.x
- Запущенный экземпляр [Grafana Loki](https://grafana.com/docs/loki/latest/)

---

## Быстрый старт

### 1. Добавьте клиентский стартер

**Gradle:**
```groovy
dependencies {
    implementation 'owpk.jloki:jloki-spring-boot-starter-client:1.0.0'
}
```

**Maven:**
```xml
<dependency>
    <groupId>owpk.jloki</groupId>
    <artifactId>jloki-spring-boot-starter-client</artifactId>
    <version>1.0.0</version>
</dependency>
```

### 2. Настройте `application.yml`

```yaml
loki:
  base-url: http://localhost:3100   # значение по умолчанию
```

Готово — бины `LokiTemplate`, `QueryService` и `StreamingService` зарегистрируются автоматически.

---

## Использование

### Точечный запрос

```java
@Autowired LokiTemplate lokiTemplate;

var request = LokiQueryDSL.queryRequest()
        .query(new LokiQuery(
                new LabelSelector(List.of(new LabelEntry("app", LabelMatcher.EQ, "my-service"))),
                List.of()))
        .limit(100)
        .build();

lokiTemplate.query(request, MyResponse.class).subscribe(System.out::println);
```

### Запрос по диапазону времени

```java
lokiTemplate.queryRange(builder -> builder
        .query("{app=\"my-service\"}")
        .start(Instant.now().minusSeconds(3600))
        .end(Instant.now())
        .limit(500),
        new ParameterizedTypeReference<MyResponse>() {})
    .subscribe(System.out::println);
```

### Потоковый tail (WebSocket)

```java
var tailRequest = LokiQueryDSL.tailRequest()
        .query("{app=\"my-service\"}")
        .build();

lokiTemplate.tailLogsStream(tailRequest, new TypeReference<LokiTailResponse<LogEvent>>() {})
        .subscribe(event -> System.out.println(event));
```

### Запись логов

```java
lokiTemplate.push(new PushLogRequest(...)).subscribe();
```

---

## LogQL DSL

`LokiQueryDSL` предоставляет enum-ы и builder-ы, которые напрямую отображаются на конструкции LogQL:

```java
// Матчеры лейблов:  =  !=  =~  !~
LabelMatcher.EQ, NOT_EQ, REG_EQ, REG_NOTEQ

// Фильтры строк:  |=  !=
Filter.CONTAINS, NOT_CONTAINS

// Regex-фильтры:  |~  !~
Regex.REG_CONTAINS, REG_NOT_CONTAINS

// Метрические функции: sum, count_over_time, avg_over_time, without, …
LokiQueryDSL.Fn.SUM, COUNTER_OVER_TIME, AVG_OVER_TIME, …
```

`LokiQuery` / `LabelSelector` / `FilterExpr` / `PipeExpr` / `FnExpr` — компонуемые объекты типа `Expression` с методами `.eval()` и `.pretty()`.

---

## Advisor-пайплайн

`LokiStreamAdvisor` — интерфейс-перехватчик для реактивного потока:

```java
public interface LokiStreamAdvisor {
    <T> Mono<T> aroundMono(URI uri, Mono<T>  upstream, LokiExecutionContext ctx);
    <T> Flux<T> aroundFlux(URI uri, Flux<T>  upstream, LokiExecutionContext ctx);
}
```

Встроенные advisors в `jloki-core`:

| Advisor | Поведение |
|---------|-----------|
| `BatchAdvisor` | Буферизует события перед передачей дальше по потоку |
| `DedupAdvisor` | Удаляет дубликаты лог-строк |
| `LoggingAdvisor` | Логирует каждое событие через SLF4J |

Чтобы добавить свой advisor — достаточно объявить его Spring-бином, `JLokiAutoConfiguration` подберёт его автоматически.

---

## Web-стартер

Добавьте `jloki-spring-boot-starter-web`, чтобы открыть доступ к Loki через REST / SSE:

**Gradle:**
```groovy
dependencies {
    implementation 'owpk.jloki:jloki-spring-boot-starter-web:1.0.0'
}
```

**Maven:**
```xml
<dependency>
    <groupId>owpk.jloki</groupId>
    <artifactId>jloki-spring-boot-starter-web</artifactId>
    <version>1.0.0</version>
</dependency>
```

Базовый путь по умолчанию: `/api/loki` (переопределяется через `loki.api-path`).

| Метод | Путь | Описание |
|-------|------|----------|
| `POST` | `/api/loki/stream` | SSE-поток логов (`text/event-stream`) |
| `POST` | `/api/loki/query` | Точечный запрос |
| `POST` | `/api/loki/queryRange` | Запрос по диапазону |
| `GET`  | `/api/loki/analysis/stream` | SSE-поток результатов LLM-анализа |

---

## LLM-анализ логов

`jloki-advisor-llm` добавляет advisor, который группирует лог-строки в батчи и отправляет их в любой OpenAI-совместимый эндпоинт (по умолчанию — Ollama).

**Gradle:**
```groovy
dependencies {
    implementation 'owpk.jloki:jloki-advisor-llm:1.0.0'
}
```

**Maven:**
```xml
<dependency>
    <groupId>owpk.jloki</groupId>
    <artifactId>jloki-advisor-llm</artifactId>
    <version>1.0.0</version>
</dependency>
```

```yaml
loki:
  llm:
    enabled: true
    base-url: http://localhost:11434   # Ollama
    model: llama3
    batch-size: 20
    timeout-sec: 60
    system-prompt: "You are a log analyzer. …"
```

Основной поток логов проходит насквозь без изменений; анализ выполняется параллельно, результаты публикуются в `AnalysisEventBus` (доступны через `/api/loki/analysis/stream`).

Для добавления собственного обработчика результатов реализуйте `AnalysisResultHandler`:

```java
@Component
public class MyHandler implements AnalysisResultHandler {
    @Override
    public void handle(LogAnalysisResult result) {
        // сохранить, отправить алерт, и т.д.
    }
}
```

---

## Справочник по конфигурации

### Клиент (`loki.*`)

| Свойство | По умолчанию | Описание |
|----------|-------------|----------|
| `loki.base-url` | `http://localhost:3100` | Базовый URL Loki |
| `loki.query-path` | `/loki/api/v1/query` | Путь точечного запроса |
| `loki.query-range-path` | `/loki/api/v1/query_range` | Путь запроса по диапазону |
| `loki.tail-path` | `/loki/api/v1/tail` | Путь WebSocket tail |
| `loki.push-path` | `/loki/api/v1/push` | Путь записи логов |
| `loki.api-path` | `/api/loki` | Базовый путь web-стартера |

### LLM advisor (`loki.llm.*`)

| Свойство | По умолчанию | Описание |
|----------|-------------|----------|
| `loki.llm.enabled` | `true` | Включить/выключить advisor |
| `loki.llm.base-url` | `http://localhost:11434` | OpenAI-совместимый эндпоинт |
| `loki.llm.model` | `llama3` | Название модели |
| `loki.llm.batch-size` | `20` | Лог-строк в одном запросе к LLM |
| `loki.llm.timeout-sec` | `60` | Таймаут HTTP-запроса |
| `loki.llm.system-prompt` | *(встроенный)* | Системный промпт для модели |

---

## Сборка

```bash
./gradlew build
```

Публикация в локальный Maven-репозиторий:

```bash
./gradlew publishToMavenLocal
```

---

## Лицензия

MIT

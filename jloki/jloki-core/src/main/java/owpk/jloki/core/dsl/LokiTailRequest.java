package owpk.jloki.core.dsl;

import java.time.Instant;
import java.util.function.Consumer;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Builder;
import owpk.jloki.core.dsl.expresson.Expression;
import owpk.jloki.core.dsl.utils.LokiQueryBuilder;
import owpk.jloki.core.dsl.utils.LokiUrlCompatable;

@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
public class LokiTailRequest implements LokiUrlCompatable {

    public static class LokiTailRequestBuilder {
        private String queryExpression;

        public LokiTailRequestBuilder queryExpression(Consumer<LokiQueryBuilder> consumer) {
            var queryBuilder = new LokiQueryBuilder();
            consumer.accept(queryBuilder);
            this.queryExpression = queryBuilder.build().eval();
            return this;
        }

        public LokiTailRequestBuilder queryExpression(Expression req) {
            this.queryExpression = req.eval();
            return this;
        }

    }

    // * query: The LogQL query to perform.
    @JsonProperty("query_expression")
    private String queryExpression;

    // start: The start time for the query as a nanosecond Unix epoch. Defaults to
    // one hour ago.
    @JsonProperty("start")
    private Instant start;

    // * limit: The max number of entries to return. It defaults to 100. Only
    // applies to query types which produce a stream (log lines) response.
    @JsonProperty("limit")
    private Integer limit;

    // delay_for: The number of seconds to delay retrieving logs to let slow loggers
    // catch up. Defaults to 0 and cannot be larger than 5.
    @JsonProperty("delay_for")
    private Integer delayFor;

    @Override
    public Consumer<StringBuilder> buildUrl() {
        return sb -> {
            append(sb, "query", urlEncode(queryExpression));
            append(sb, "limit", limit);
            append(sb, "delay_for", delayFor);
            append(sb, "start", start);
        };
    }
}
package com.example.logclient.dsl;

import java.time.Instant;
import java.util.function.Consumer;

import com.example.logclient.dsl.LokiQueryExpression.Expression;

import lombok.Builder;

@Builder
public class LokiTailRequest implements LokiUrlCompatable {

    public static class LokiTailRequestBuilder {
        private Expression queryExpression;

        public LokiTailRequestBuilder queryExpression(Consumer<LokiQueryExpression.Builder> consumer) {
            var queryBuilder = LokiQueryExpression.builder();
            consumer.accept(queryBuilder);
            this.queryExpression = queryBuilder.asExpr();
            return this;
        }

    }

    // * query: The LogQL query to perform.
    private Expression queryExpression;

    // * query: The LogQL query to perform.
    private String query;

    // start: The start time for the query as a nanosecond Unix epoch. Defaults to
    // one hour ago.
    private Instant start;

    // * limit: The max number of entries to return. It defaults to 100. Only
    // applies to query types which produce a stream (log lines) response.
    private Integer limit;

    // delay_for: The number of seconds to delay retrieving logs to let slow loggers
    // catch up. Defaults to 0 and cannot be larger than 5.
    private Integer delayFor;

    @Override
    public Consumer<StringBuilder> buildUrl() {
        return sb -> {
            append(sb, "query", queryExpression);
            append(sb, "query", query);
            append(sb, "limit", limit);
            append(sb, "delay_for", delayFor);
            append(sb, "start", start);
        };
    }
}

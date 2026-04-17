package owpk.jloki.core.dsl;

import java.util.function.Consumer;

import lombok.Builder;
import owpk.jloki.core.dsl.expresson.Expression;
import owpk.jloki.core.dsl.utils.LokiQueryBuilder;
import owpk.jloki.core.dsl.utils.LokiUrlCompatable;

@Builder
public class LokiQueryRequest implements LokiUrlCompatable {

    public static class LokiQueryRequestBuilder {
        private Expression queryExpression;

        public LokiQueryRequestBuilder queryExpression(Consumer<LokiQueryBuilder> consumer) {
            var queryBuilder = new LokiQueryBuilder();
            consumer.accept(queryBuilder);
            this.queryExpression = queryBuilder.build();
            return this;
        }

        public LokiQueryRequestBuilder queryExpression(Expression req) {
            this.queryExpression = req;
            return this;
        }
    }

    // * query: The LogQL query to perform.
    private Expression queryExpression;

    // * limit: The max number of entries to return. It defaults to 100. Only
    // applies to query types which produce a stream (log lines) response.
    private Integer limit;

    // * time: The evaluation time for the query as a nanosecond Unix epoch or
    // another supported format. Defaults to now.
    private String time;

    // * direction: Determines the sort order of logs. Supported values are forward
    // or backward. Defaults to backward.
    private String direction;

    @Override
    public Consumer<StringBuilder> buildUrl() {
        return sb -> {
            append(sb, "query", queryExpression);
            append(sb, "direction", direction);
            append(sb, "limit", limit);
            append(sb, "time", time);
        };
    }

}

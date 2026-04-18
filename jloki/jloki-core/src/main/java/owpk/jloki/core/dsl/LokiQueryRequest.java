package owpk.jloki.core.dsl;

import java.util.function.Consumer;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Builder;
import owpk.jloki.core.dsl.expresson.Expression;
import owpk.jloki.core.dsl.utils.LokiQueryBuilder;
import owpk.jloki.core.dsl.utils.LokiUrlCompatable;

@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
public class LokiQueryRequest implements LokiUrlCompatable {

    public static class LokiQueryRequestBuilder {
        private String queryExpression;

        public LokiQueryRequestBuilder queryExpression(Consumer<LokiQueryBuilder> consumer) {
            var queryBuilder = new LokiQueryBuilder();
            consumer.accept(queryBuilder);
            this.queryExpression = queryBuilder.build().eval();
            return this;
        }

        public LokiQueryRequestBuilder queryExpression(Expression req) {
            this.queryExpression = req.eval();
            return this;
        }
    }

    // * query: The LogQL query to perform.
    @JsonProperty("query_expression")
    private String queryExpression;

    // * limit: The max number of entries to return. It defaults to 100. Only
    // applies to query types which produce a stream (log lines) response.
    @JsonProperty("limit")
    private Integer limit;

    // * time: The evaluation time for the query as a nanosecond Unix epoch or
    // another supported format. Defaults to now.
    @JsonProperty("time")
    private String time;

    // * direction: Determines the sort order of logs. Supported values are forward
    // or backward. Defaults to backward.
    @JsonProperty("direction")
    private String direction;

    @Override
    public Consumer<StringBuilder> buildUrl() {
        return sb -> {
            append(sb, "query", urlEncode(queryExpression));
            append(sb, "direction", direction);
            append(sb, "limit", limit);
            append(sb, "time", time);
        };
    }

}
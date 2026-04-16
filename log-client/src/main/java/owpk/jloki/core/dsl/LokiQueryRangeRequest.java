package owpk.jloki.core.dsl;

import java.util.function.Consumer;

import lombok.Builder;
import lombok.ToString;
import owpk.jloki.core.dsl.LokiQueryExpression.Expression;

@Builder
@ToString
public class LokiQueryRangeRequest implements LokiUrlCompatable {

    public static class LokiQueryRangeRequestBuilder {
        private Expression queryExpression;

        public LokiQueryRangeRequestBuilder queryExpression(Consumer<LokiQueryExpression.Builder> consumer) {
            var queryBuilder = LokiQueryExpression.builder();
            consumer.accept(queryBuilder);
            this.queryExpression = queryBuilder.asExpr();
            return this;
        }

        public LokiQueryRangeRequestBuilder queryExpression(Expression req) {
            this.queryExpression = req;
            return this;
        }

        public LokiQueryRangeRequestBuilder queryExpression(LokiQueryExpression.Builder req) {
            this.queryExpression = req.asExpr();
            return this;
        }
    }

    // * query: The LogQL query to perform.
    private Expression queryExpression;

    // * start: The start time for the query as a nanosecond Unix epoch or another
    // supported format. Defaults to one hour ago. Loki returns results with
    // timestamp greater or equal to this value.
    private String start;

    // * end: The end time for the query as a nanosecond Unix epoch or another
    // supported format. Defaults to now. Loki returns results with timestamp lower
    // than this value.
    private String end;

    // * since: A duration used to calculate start relative to end. If end is in the
    // future, start is calculated as this duration before now. Any value specified
    // for start supersedes this parameter.
    private String since;

    // * limit: The max number of entries to return. It defaults to 100. Only
    // applies to query types which produce a stream (log lines) response.
    private Integer limit;

    // * step: Query resolution step width in duration format or float number of
    // seconds. duration refers to Prometheus duration strings of the form
    // [0-9]+[smhdwy]. For example, 5m refers to a duration of 5 minutes. Defaults
    // to a dynamic value based on start and end. Only applies to query types which
    // produce a matrix response.
    private String step;

    // * interval: Only return entries at (or greater than) the specified interval,
    // can be a duration format or float number of seconds. Only applies to queries
    // which produce a stream response. Not to be confused with step, see the
    // explanation under Step versus interval.
    private String interval;

    // * direction: Determines the sort order of logs. Supported values are forward
    // or backward. Defaults to backward.
    private String direction;

    @Override
    public Consumer<StringBuilder> buildUrl() {
        return sb -> {
            append(sb, "query", queryExpression);
            append(sb, "start", start);
            append(sb, "end", end);
            append(sb, "since", since, true);
            append(sb, "limit", limit);
            append(sb, "step", step);
            append(sb, "interval", interval);
            append(sb, "direction", direction);
        };
    }

}
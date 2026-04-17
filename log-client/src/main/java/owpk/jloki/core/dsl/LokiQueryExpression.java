package owpk.jloki.core.dsl;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

/**
 * LogQL naive java dsl implementation
 * 
 * see official docs: https://grafana.com/docs/loki/latest/query/log_queries/
 * 
 * @author Vyacheslav Vorobev
 */
@Slf4j
public final class LokiQueryExpression {

    private LokiQueryExpression() {
    }

    public static Builder builder() {
        return new Builder();
    }

    public static LabelQueryBuilder labelQuery() {
        return new LabelQueryBuilder();
    }

    public static FnExpr fnExpr(Fn fn, Expression expr) {
        return new FnExpr(fn, expr);
    }

    public static FnExpr fnExpr(Fn fn, String expr) {
        return new FnExpr(fn, new QueryExpr(expr));
    }

    public static QueryExpr queryExpr(String value) {
        return new QueryExpr(value);
    }

    public static CompareExpr compareExpr(String left, String match, String right) {
        return new CompareExpr(new QueryExpr(left), Matcher.of(match), new QueryExpr(right));
    }

    public static CompareExpr compareExpr(Expression left, String match, String right) {
        return new CompareExpr(left, Matcher.of(match), new QueryExpr(right));
    }

    public static CompareExpr compareExpr(Expression left, String match, Expression right) {
        return new CompareExpr(left, Matcher.of(match), right);
    }

    public static CompareExpr compareExpr(Expression left, Matcher match, Expression right) {
        return new CompareExpr(left, match, right);
    }

    public static class Builder {
        private final StringBuilder sb;

        public Builder() {
            this(new StringBuilder());
        }

        public Builder(StringBuilder sb) {
            this.sb = sb;
        }

        public Builder query(String query) {
            if (!sb.isEmpty())
                sb.append(" ");
            sb.append(query);
            return this;
        }

        public Builder query(Consumer<LokiQueryExpression.Builder> queryFn) {
            var builder = LokiQueryExpression.builder();
            queryFn.accept(builder);
            return this.query(builder.build());
        }

        public Builder query(Expression query) {
            return this.query(query.eval());
        }

        public Builder labelQuery(Consumer<LabelQueryBuilder> queryFn) {
            var labelQueryBuilder = new LabelQueryBuilder();
            queryFn.accept(labelQueryBuilder);
            return query(labelQueryBuilder.build());
        }

        public Builder labelQuery(LabelQueryBuilder queryFn) {
            return query(queryFn.build());
        }

        public Builder wrapFn(Fn func) {
            var wrapped = func.curry().apply(sb.toString());
            sb.delete(0, sb.length());
            sb.append(wrapped);
            return this;
        }

        public Builder compare(Compare compare) {
            return query(compare.getValue());
        }

        public Builder match(Matcher matcher) {
            return query(matcher.getValue());
        }

        public Builder filter(Filter filter) {
            return query(filter.getValue());
        }

        public Builder pipe() {
            return query("|");
        }

        public String build() {
            return sb.toString();
        }

        public Builder log() {
            log.info(sb.toString());
            return this;
        }

        public String encode() {
            return URLEncoder.encode(sb.toString(), StandardCharsets.UTF_8);
        }

        public QueryExpr asExpr() {
            return new QueryExpr(sb.toString());
        }
    }

    public enum Compare {
        EQ("=="),
        NOTEQ("!+"),
        GR(">"),
        GREQ(">="),
        LESS("<"),
        LESSEQ("<=");

        @Getter
        private final String value;

        Compare(String value) {
            this.value = value;
        }
    }

    public enum Fn {
        SUM_CTE("sum", (fnName, query) -> fnName + " " + query),
        SUM("sum"),
        COUNTER_OVER_TIME("count_over_time"),
        AVG_OVER_TIME("avg_over_time"),
        WITHOUT("without");

        private final BiFunction<String, String, String> call;

        @Getter
        private final String functionName;

        Fn(String name, BiFunction<String, String, String> functionValue) {
            this.call = functionValue;
            this.functionName = name;
        }

        Fn(String name) {
            this(name, (fnName, query) -> fn(fnName, query));
        }

        public BiFunction<String, String, String> asFunction() {
            return call;
        }

        public Function<String, String> curry() {
            return query -> call.apply(functionName, query);
        }
    }

    public enum Filter {
        /**
         * |=: Log line contains string
         * !=: Log line does not contain string
         * |~: Log line contains a match to the regular expression
         * !~: Log line does not contain a match to the regular expression
         */
        REG_NOT_CONTAINS("!~"),
        REG_CONTAINS("|~"),
        NOT_CONTAINS("!="),
        CONTAINS("|=");

        @Getter
        private final String value;

        Filter(String value) {
            this.value = value;
        }
    }

    public enum Matcher {
        MATCH("|>"), NOTMATCH("!>");

        @Getter
        private final String value;

        Matcher(String value) {
            this.value = value;
        }

        private static final Map<String, Matcher> valuesMap = Arrays.stream(Matcher.values())
                .collect(Collectors.toMap(it -> it.getValue(), Function.identity()));

        public static Matcher of(String value) {
            return valuesMap.get(value);
        }
    }

    public enum LabelMatcher {
        // =: exactly equal
        // !=: not equal
        // =~: regex matches
        // !~: regex does not match
        EQ("="), 
        NOT_EQ("!="), 
        REG_EQ("=~"), 
        REG_NOTEQ("!~");

        @Getter
        private final String value;

        LabelMatcher(String value) {
            this.value = value;
        }
        private static final Map<String, LabelMatcher> labelMatcherMap = Arrays.stream(LabelMatcher.values())
            .collect(Collectors.toMap(it -> it.getValue(), Function.identity()));

        public static LabelMatcher of(String value) {
            return labelMatcherMap.get(value);
        }
    }

    public record LabelEntry(String key, LabelMatcher matcher, String value) {
    }

    public static class LabelQueryBuilder {
        private Map<String, LabelEntry> labels = new HashMap<>();

        public LabelQueryBuilder label(String key, String value) {
            return label(key, LabelMatcher.EQ, value);
        }

        public LabelQueryBuilder label(String key, LabelMatcher matcher, String value) {
            if (value != null)
                this.labels.put(key, new LabelEntry(key, matcher, value));
            return this;
        }

        public String build() {
            return labels.values().stream()
                    .map(e -> e.key() + e.matcher().getValue() + "\"" + e.value() + "\"")
                    .collect(Collectors.joining(", ", "{", "}"));
        }

        public QueryExpr asExpr() {
            return new QueryExpr(build());
        }
    }

    public interface Expression {
        String eval();

        String pretty(int tab);
    }

    private static String fn(String fnName, String query) {
        return fnName + "(" + query + ")";
    }

    public record CompareExpr(Expression left, Matcher matcher, Expression right) implements Expression {
        @Override
        public String eval() {
            return left.eval() + " " + matcher.value + " " + right.eval();
        }

        @Override
        public String pretty(int tab) {
            return left.pretty(tab) + "\n" + "\t".repeat(tab) + matcher.value + "\n" + "\t".repeat(tab)
                    + right.pretty(tab);
        }
    }

    public record FnExpr(Fn func, Expression wrapped) implements Expression {
        @Override
        public String eval() {
            return func.curry().apply(wrapped.eval());
        }

        @Override
        public String pretty(int tab) {
            var inner = wrapped.pretty(tab + 1);
            return "\t".repeat(tab) + func.curry().apply("\n" + inner);
        }
    }

    public record QueryExpr(String query) implements Expression {

        @Override
        public String eval() {
            return query;
        }

        @Override
        public String pretty(int tab) {
            return "\t".repeat(tab) + eval();
        }
    }
}

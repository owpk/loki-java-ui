package owpk.jloki.core.dsl;

import java.util.Arrays;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.stream.Collectors;

import owpk.jloki.core.dsl.LokiQueryRangeRequest.LokiQueryRangeRequestBuilder;
import owpk.jloki.core.dsl.LokiQueryRequest.LokiQueryRequestBuilder;
import owpk.jloki.core.dsl.LokiTailRequest.LokiTailRequestBuilder;
import owpk.jloki.core.dsl.expresson.FnExpr;

public final class LokiQueryDSL {

    private LokiQueryDSL() {
    }

    public static LokiTailRequestBuilder tailRequest() {
        return LokiTailRequest.builder();
    }

    public static LokiQueryRangeRequestBuilder queryRangeRequest() {
        return LokiQueryRangeRequest.builder();
    }

    public static LokiQueryRequestBuilder queryRequest() {
        return LokiQueryRequest.builder();
    }

    public enum Regex {
        /**
         * |~: Log line contains a match to the regular expression
         * !~: Log line does not contain a match to the regular expression
         */
        REG_NOT_CONTAINS("!~"),
        REG_CONTAINS("|~");

        private final String value;

        Regex(String value) {
            this.value = value;
        }

        public String getValue() {
            return value;
        }
    }

    public enum Filter {
        /**
         * |=: Log line contains string
         * !=: Log line does not contain string
         */
        NOT_CONTAINS("!="),
        CONTAINS("|=");

        private final String value;

        Filter(String value) {
            this.value = value;
        }

        public String getValue() {
            return value;
        }
    }

    public enum Matcher {
        MATCH("|>"), NOTMATCH("!>");

        private final String value;

        Matcher(String value) {
            this.value = value;
        }

        public String getValue() {
            return value;
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

        private final String value;

        LabelMatcher(String value) {
            this.value = value;
        }

        public String getValue() {
            return value;
        }

        private static final Map<String, LabelMatcher> labelMatcherMap = Arrays.stream(LabelMatcher.values())
                .collect(Collectors.toMap(it -> it.getValue(), Function.identity()));

        public static LabelMatcher of(String value) {
            return labelMatcherMap.get(value);
        }
    }

    public enum Fn {
        SUM_CTE("sum", (fnName, query) -> fnName + " " + query),
        SUM("sum"),
        COUNTER_OVER_TIME("count_over_time"),
        AVG_OVER_TIME("avg_over_time"),
        WITHOUT("without");

        private final BinaryOperator<String> call;

        private final String functionName;

        Fn(String name, BinaryOperator<String> functionValue) {
            this.call = functionValue;
            this.functionName = name;
        }

        Fn(String name) {
            this(name, FnExpr::fn);
        }

        public String getFunctionName() {
            return functionName;
        }

        public BiFunction<String, String, String> asFunction() {
            return call;
        }

        public Function<String, String> curry() {
            return query -> call.apply(functionName, query);
        }
    }
}
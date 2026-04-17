package owpk.jloki.core.moddsl;

import java.util.Arrays;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.stream.Collectors;

import lombok.Getter;
import owpk.jloki.core.moddsl.expresson.LokiQuery;
import owpk.jloki.core.moddsl.steps.FnStep;

public final class LokiQueryDSL {

    private LokiQueryDSL() {
    }

    public static LokiQueryBuilder query() {
        return new LokiQueryBuilder();
    }

    public enum Regex {
        REG_NOT_CONTAINS("!~"),
        REG_CONTAINS("|~");

        @Getter
        private final String value;

        Regex(String value) {
            this.value = value;
        }
    }

    public enum Filter {
        /**
         * |=: Log line contains string
         * !=: Log line does not contain string
         * |~: Log line contains a match to the regular expression
         * !~: Log line does not contain a match to the regular expression
         */
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

    public enum Fn {
        SUM_CTE("sum", (fnName, query) -> fnName + " " + query),
        SUM("sum"),
        COUNTER_OVER_TIME("count_over_time"),
        AVG_OVER_TIME("avg_over_time"),
        WITHOUT("without");

        private final BinaryOperator<String> call;

        @Getter
        private final String functionName;

        Fn(String name, BinaryOperator<String> functionValue) {
            this.call = functionValue;
            this.functionName = name;
        }

        Fn(String name) {
            this(name, FnStep::fn);
        }

        public BiFunction<String, String, String> asFunction() {
            return call;
        }

        public Function<String, String> curry() {
            return query -> call.apply(functionName, query);
        }
    }

    public static void main(String[] args) {
        LokiQuery query = new LokiQueryBuilder()
                .label("app", "api")
                .label("env", "prod")
                .filter(Filter.CONTAINS, "error")
                .regex("timeout.*")
                .json()
                .build();
        System.out.println(query.pretty());
        System.out.println(query.render());
    }

}
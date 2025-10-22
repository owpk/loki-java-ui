package com.example.logclient.dsl;

import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.function.Consumer;
import java.util.function.Function;

import com.example.logclient.dsl.LokiQueryExpression.Expression;

public interface LokiUrlCompatable {

    Consumer<StringBuilder> buildUrl();

    default URI toURI() {
        StringBuilder sb = new StringBuilder();
        buildUrl().accept(sb);
        return URI.create(sb.toString());
    }

    default Function<String, URI> toURIFn() {
        StringBuilder sb = new StringBuilder();
        buildUrl().accept(sb);
        return baseUrl -> URI.create(baseUrl + sb.toString());
    }

    default void append(StringBuilder sb, String key, String value) {
        this.append(sb, key, value, false);
    };

    default void append(StringBuilder sb, String key, String value, boolean encode) {
        if (value == null || value.isEmpty())
            return;

        if (sb.length() > 0)
            sb.append('&');
        else
            sb.append('?');

        sb.append(key)
                .append('=')
                .append(encode ? urlEncode(value) : value);
    };

    default String urlEncode(String value) {
        return URLEncoder.encode(value, StandardCharsets.UTF_8);
    }

    default void append(StringBuilder sb, String key, Instant value) {
        if (value == null)
            return;

        append(sb, key, value.toEpochMilli());
    };

    default void append(StringBuilder sb, String key, Number value) {
        if (value == null)
            return;

        append(sb, key, String.valueOf(value));
    }

    default void append(StringBuilder sb, String key, Expression value) {
        if (value == null)
            return;

        append(sb, key, urlEncode(value.eval()));
    };

    default String instantToEpochNanos(Instant t) {
        if (t == null)
            return null;
        return String.valueOf(t.getEpochSecond() * 1_000_000_000L + t.getNano());
    }
}

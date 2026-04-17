package owpk.jloki.core.model;

import java.util.List;

public record LokiRawQueryResponse<T>(Data<T> data) {
    public record Data<T>(List<Result<T>> result) {}
    public record Result<T>(T stream, List<List<String>> values) {}
}

package owpk.jloki.core.model;

import java.util.List;

public record LokiTailResponse<T>(List<Stream<T>> streams) {
    public record Stream<T>(
            T stream,
            List<List<String>> values
    ) {}
}

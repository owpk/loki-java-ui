package owpk.jloki.core.settings;

public record LokiTemplateSettings(
        String lokiBaseUrl,
        String queryRangePath,
        String queryPath,
        String tailPath,
        String pushPath) {

    public LokiTemplateSettings(String lokiBaseUrl) {
        this(lokiBaseUrl,
                "/loki/api/v1/query_range",
                "/loki/api/v1/query",
                "/loki/api/v1/tail",
                "/loki/api/v1/push");
    }

}

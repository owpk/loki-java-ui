package owpk.jloki.starter;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "loki")
public class JLokiProperties {
    private String baseUrl = "http://localhost:3100";
    private String queryPath = "/loki/api/v1/query";
    private String queryRangePath = "/loki/api/v1/query_range";
    private String tailPath = "/loki/api/v1/tail";
    private String pushPath = "/loki/api/v1/push";

    public String getBaseUrl() {
        return baseUrl;
    }

    public void setBaseUrl(String baseUrl) {
        this.baseUrl = baseUrl;
    }

    public String getQueryPath() {
        return queryPath;
    }

    public void setQueryPath(String queryPath) {
        this.queryPath = queryPath;
    }

    public String getQueryRangePath() {
        return queryRangePath;
    }

    public void setQueryRangePath(String queryRangePath) {
        this.queryRangePath = queryRangePath;
    }

    public String getTailPath() {
        return tailPath;
    }

    public void setTailPath(String tailPath) {
        this.tailPath = tailPath;
    }

    public String getPushPath() {
        return pushPath;
    }

    public void setPushPath(String pushPath) {
        this.pushPath = pushPath;
    }
}

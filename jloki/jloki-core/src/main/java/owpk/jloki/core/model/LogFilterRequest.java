package owpk.jloki.core.model;

import java.util.List;

import lombok.Builder;

@Builder
public record LogFilterRequest(
        List<LogQueryFilter> filters,
        String query, // Basicaly has LogQl format, may not be supported by implementaion
        String timeRange,
        String start,
        String end,
        String order,
        Integer limit) {

    public LogFilterRequest {
        if (order == null)
            order = "DESC";
    }
}
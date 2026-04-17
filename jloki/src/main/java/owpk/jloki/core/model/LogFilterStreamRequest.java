package owpk.jloki.core.model;

import java.util.List;

import lombok.Builder;

@Builder
public record LogFilterStreamRequest(
        List<LogQueryFilter> filters,
        String query, // Basicaly has LogQl format, may not be supported by implementaion
        Long start,
        Integer limit) {
}

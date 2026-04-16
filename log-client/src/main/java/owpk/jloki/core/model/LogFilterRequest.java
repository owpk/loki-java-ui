package owpk.jloki.core.model;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LogFilterRequest {
    private List<LogQueryFilter> filters;
    private String query; // Basicaly has LogQl format, may not be supported by implementaion
    private String timeRange;
    private String start;
    private String end;
    @Builder.Default
    private String order = "DESC";
    private Integer limit;
}

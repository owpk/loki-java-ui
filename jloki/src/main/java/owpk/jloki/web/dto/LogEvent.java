package owpk.jloki.web.dto;

import java.util.Map;

public record LogEvent(
        String timestamp,
        String line,
        Map<String, String> labels
) {}
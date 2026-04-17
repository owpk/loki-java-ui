package owpk.jloki.core.model;

import java.util.List;
import java.util.Map;

public record PushLogRequest(List<Stream> streams) {
	public record Stream(Map<String, String> stream, List<List<String>> values) {}
}

package owpk.jloki.core.model;

import java.util.List;
import java.util.Map;

import lombok.Data;

@Data
public class PushLogRequest {
	private List<Stream> streams;

	@Data
	public static class Stream {
		private Map<String, String> stream;
		private List<List<String>> values;
	}
}

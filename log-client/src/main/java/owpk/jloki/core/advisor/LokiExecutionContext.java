package owpk.jloki.core.advisor;

import java.util.Map;

public record LokiExecutionContext(
		String operation, // tail / query / range
		Map<String, Object> attributes) {

	public LokiExecutionContext() {
		this(null, Map.of());
	}
}

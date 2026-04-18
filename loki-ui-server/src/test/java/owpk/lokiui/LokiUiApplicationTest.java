package owpk.lokiui;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        properties = "loki.url=http://localhost:13100")
class LokiUiApplicationTest {

    @Test
    void contextLoads() {
        // verifies Spring context starts without errors
    }
}

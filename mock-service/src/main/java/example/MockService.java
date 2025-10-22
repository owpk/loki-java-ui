package example;

import java.util.Map;
import java.util.Random;
import java.util.function.Consumer;

import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import lombok.extern.slf4j.Slf4j;

@SpringBootApplication
@Configuration
@Slf4j
public class MockService {
   private static final String[] LEVELS = { "INFO", "WARN", "ERROR", "DEBUG", "TRACE" };

   private final Map<String, Consumer<String>> LEVELS_MAP = Map.of(
         "INFO", msg -> log.info(msg),
         "WARN", msg -> log.warn(msg),
         "ERROR", msg -> log.error(msg),
         "DEBUG", msg -> log.debug(msg),
         "TRACE", msg -> log.trace(msg));

   private static final String[] MESSAGES = {
         "User login successful",
         "Payment processed",
         "Connection timeout",
         "Invalid request",
         "Order created",
         "Database connection lost",
         "Cache refreshed"
   };

   public static void main(String[] args) throws Exception {
      SpringApplication.run(MockService.class, args);
   }

   @Bean
   public ApplicationRunner runner() {
      return args -> {
         var random = new Random();
         while (true) {
            var level = LEVELS[random.nextInt(LEVELS.length)];
            var msg = MESSAGES[random.nextInt(MESSAGES.length)];
            var handelr = LEVELS_MAP.get(level);
            handelr.accept(msg);
            Thread.sleep(1000);
         }
      };
   }

}

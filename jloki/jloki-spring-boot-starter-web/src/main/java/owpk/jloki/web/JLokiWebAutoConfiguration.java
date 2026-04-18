package owpk.jloki.web;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.context.annotation.Import;

import owpk.jloki.starter.JLokiAutoConfiguration;

@AutoConfiguration(after = JLokiAutoConfiguration.class)
@Import(LokiController.class)
public class JLokiWebAutoConfiguration {
}

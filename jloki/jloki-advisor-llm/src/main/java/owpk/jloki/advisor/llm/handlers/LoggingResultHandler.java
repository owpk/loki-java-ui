package owpk.jloki.advisor.llm.handlers;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import owpk.jloki.advisor.llm.AnalysisResultHandler;
import owpk.jloki.advisor.llm.LogAnalysisResult;

public class LoggingResultHandler implements AnalysisResultHandler {

    private static final Logger log = LoggerFactory.getLogger(LoggingResultHandler.class);

    @Override
    public void handle(LogAnalysisResult result) {
        log.info("[LLM Analysis] model={} analyzed={} logs\n{}",
                result.model(),
                result.analyzedLogs().size(),
                result.analysis());
    }
}

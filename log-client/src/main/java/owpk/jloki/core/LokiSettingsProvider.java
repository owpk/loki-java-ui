package owpk.jloki.core;

public interface LokiSettingsProvider {
    LokiTemplateSettings provide();
    void set(LokiTemplateSettings settings);
}

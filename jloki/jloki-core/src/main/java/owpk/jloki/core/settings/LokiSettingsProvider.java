package owpk.jloki.core.settings;

public interface LokiSettingsProvider {
    LokiTemplateSettings provide();
    void set(LokiTemplateSettings settings);
}

package owpk.jloki.core;

import java.util.concurrent.atomic.AtomicReference;

public record DefaultLokiSettingsProvider(AtomicReference<LokiTemplateSettings> settings)
        implements LokiSettingsProvider {

    public DefaultLokiSettingsProvider(LokiTemplateSettings settings) {
        this(new AtomicReference<>(settings));
    }

    @Override
    public LokiTemplateSettings provide() {
        return settings.get();
    }

    @Override
    public void set(LokiTemplateSettings settings) {
        this.settings.set(settings);
    }
}

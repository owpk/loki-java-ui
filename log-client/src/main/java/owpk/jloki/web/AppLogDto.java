package owpk.jloki.web;

import java.security.Timestamp;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;

@Data
public class AppLogDto {
    
    // @Schema(title = "Система")
    @JsonProperty(value = "system")
    private String funcSystem;

    // @Schema(title = "Инстанция")
    @JsonProperty(value = "instance")
    private String instance;

    // @Schema(title = "Версия")
    @JsonProperty("version")
    private String appVersion;

    // @Schema(title = "Пользователь")
    private String userLogin;

    // @Schema(title = "Дата / Время")
    @JsonProperty("timestamp")
    private Timestamp logTs;

    // @Schema(title = "Тип сообщения")
    private String logLevel;

    private String mdc;

    // @Schema(title = "Имя логгера")
    private String logger;

    // @Schema(title = "Имя потока")
    private String logThread;

    // @Schema(title = "Текст сообщения")
    private String logMessage;

    // @Schema(title = "Исключение")
    private String logException;

    // @Schema(title = "Источник вызова")
    private String logStack;
}

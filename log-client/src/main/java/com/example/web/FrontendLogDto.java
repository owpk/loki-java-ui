package com.example.web;

import java.time.ZonedDateTime;

import lombok.Data;

@Data
public class FrontendLogDto {
    
    private Long id;

    /**
     * Данные заполняются из токена
     */
    // //@Schema(title = "Пользователь")
    private String userLogin = "";

    /**
     * Данные приходят в структуре
     */
    //@Schema(title = "Система")
    private String system = "";

    //@Schema(title = "Дата / Время")
    private ZonedDateTime timestamp;

    //@Schema(title = "Локация")
    private String location = "";

    //@Schema(title = "User Agent")
    private String browser = "";

    //@Schema(title = "Тип ошибки")
    private String errorType = "";

    //@Schema(title = "Сообщение об ошибке")
    private String errorMessage = "";

    //@Schema(title = "Детальное описание ошибки")
    private String errorStack = "";

    //@Schema(title = "Версия образа")
    private String version = "";

    //@Schema(title = "Трассировка")
    private String tracking = "";

}

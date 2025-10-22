package com.example.logclient.model;

import java.util.List;

import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Data
public class LokiRawQueryResponse<T> {
    private Data<T> data;

    @ToString
    @Getter
    @Setter
    @NoArgsConstructor
    public static class Data<T> {
        private List<Result<T>> result;
    }

    @ToString
    @Getter
    @Setter
    @NoArgsConstructor
    public static class Result<T> {
        private T stream;
        private List<List<String>> values;
    }
}

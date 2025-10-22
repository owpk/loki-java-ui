package com.example.web.dto;

import java.util.List;

import com.example.model.BackendLog;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LogListResponse {
    private List<BackendLog> items;
    private long total;
}

package com.example.lab1.domain.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ImportHistoryUpdate {
    private Long importHistoryId;
    private boolean status;
    private String processingStatus;
    private long countElement;
    private String errorMessage;
} 
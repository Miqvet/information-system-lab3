package com.example.importservice.domain.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ImportMessage implements Serializable {
    private String fileName;
    private Long importHistoryId;
    private Long userId;
    private String contentType;
}
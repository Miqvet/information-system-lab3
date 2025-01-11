package com.example.importservice.service;

import com.example.importservice.dto.ImportHistoryUpdate;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
@RequiredArgsConstructor
public class ImportHistoryService {
    private final RestTemplate restTemplate;

    @Value("${main-service.url}")
    private String mainServiceUrl;

    public void updateImportStatus(ImportHistoryUpdate update) {
        String url = mainServiceUrl + "/api/import-history/update";
        restTemplate.postForObject(url, update, Void.class);
    }
}
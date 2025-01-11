package com.example.lab1.controller;


import com.example.lab1.config.LockProvider;
import com.example.lab1.service.ImportService;
import com.example.lab1.service.MinioService;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.PlatformTransactionManager;

import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;

@Controller
@RequiredArgsConstructor
public class ImportController {
    private final MinioService minioService;
    private final ImportService importService;


    @GetMapping("/user/download/{fileName}")
    public ResponseEntity<Resource> downloadFile(@PathVariable String fileName) throws Exception {
        InputStream fileStream = minioService.downloadFile(fileName);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + fileName + "\"")
                .body(new InputStreamResource(fileStream));
    }

    @PostMapping("/user/import")
    public String importStudyGroups(@RequestParam("file") MultipartFile file, Model model) throws Exception {

//        DefaultTransactionDefinition definition = new DefaultTransactionDefinition();
//        definition.setName("vehicleImportTransaction");
//        definition.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);
//        definition.setIsolationLevel(TransactionDefinition.ISOLATION_REPEATABLE_READ);
//
//        TransactionStatus status = transactionManager.getTransaction(definition);
//
//        String fileKey = file.getOriginalFilename();
//        long savedCount = 0;
//        boolean fileUploaded = false;
//        InputStream originalFileStream = null;
//
//        try {
//            if (minioService.fileExists(fileKey)) {
//                originalFileStream = minioService.downloadFile(fileKey);
//            }
//            minioService.uploadFile(fileKey, file.getInputStream(), file.getContentType());
//            fileUploaded = true;
//
//            lockProvider.getReentLock().lock();
//            try {
//                savedCount = importService.saveDataFromFile(file);
//            } finally {
//                lockProvider.getReentLock().unlock();
//            }
//            transactionManager.commit(status);
//
//            model.addAttribute("message", "Импорт успешно завершен");
//            System.out.println("Импорт успешно завершен");
//            return "redirect:/user";
//        } catch (Exception e) {
//            try {
//                transactionManager.rollback(status);
//                if (fileUploaded) {
//                    minioService.deleteFile(fileKey);
//                }
//                if (originalFileStream != null) {
//                    minioService.uploadFile(fileKey, originalFileStream, file.getContentType());
//                }
//            } catch (Exception rollbackException) {
//                System.err.println("Ошибка при откате транзакции: " + rollbackException.getMessage());
//            }
//            System.err.println("Ошибка при импорте: " + e.getMessage());
//            model.addAttribute("message", "Импорт не выполнен, выполнен откат.");
//            throw e;
//        } finally {
//            importService.saveImportHistory(file, savedCount);
//        }
        importService.saveDataFromFile(file);
        return "redirect:/user";
    }
}

package com.example.lab1.service;

import com.example.lab1.domain.entity.StudyGroup;
import com.example.lab1.domain.entity.ImportHistory;
import com.example.lab1.domain.entity.Person;
import com.example.lab1.domain.entity.auth.User;
import com.example.lab1.repository.ImportHistoryRepository;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import com.google.j2objc.annotations.Property;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.DefaultTransactionDefinition;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.ArrayList;

@Service
@RequiredArgsConstructor
public class ImportService {
    private final PlatformTransactionManager transactionManager;

    private final StudyGroupService studyGroupService;
    private final PersonService personService;
    private final UserService userService;
    private final ObjectMapper objectMapper;
    private final ImportHistoryRepository importHistoryRepository;
    private final MinioService minioService;

    @Transactional(isolation = Isolation.REPEATABLE_READ, rollbackFor = Exception.class)
    public void saveImportHistory(MultipartFile file, long savedElementsCount) {
        ImportHistory importHistory = new ImportHistory();
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        User currentUser = userService.findByUsername(username);

        importHistory.setFileName(file.getOriginalFilename());
        importHistory.setImportDate(LocalDateTime.now());
        importHistory.setAddedBy(currentUser);
        importHistory.setStatus(savedElementsCount > 0);
        importHistory.setCountElement(savedElementsCount);

        importHistoryRepository.save(importHistory);
    }

//    @Transactional(isolation = Isolation.REPEATABLE_READ, rollbackFor = Exception.class)
    public void saveDataFromFile(MultipartFile file) throws Exception {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        User currentUser = userService.findByUsername(username);

        DefaultTransactionDefinition definition = new DefaultTransactionDefinition();
        definition.setName("vehicleImportTransaction");
        definition.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);
        definition.setIsolationLevel(TransactionDefinition.ISOLATION_REPEATABLE_READ);
        definition.setTimeout(10);

        TransactionStatus status = transactionManager.getTransaction(definition);

        String fileKey = file.getOriginalFilename();
        long savedCount = 0;
        boolean fileUploaded = false;
        InputStream originalFileStream = null;

        try {
            if (minioService.fileExists(fileKey)) {
                originalFileStream = minioService.downloadFile(fileKey);
            }
            minioService.uploadFile(fileKey, file.getInputStream(), file.getContentType());
            fileUploaded = true;

            List<StudyGroup> studyGroups = parseStudyGroups(file);
            for (StudyGroup group : studyGroups) {
                validateStudyGroup(group);
                Person groupAdmin = group.getGroupAdmin();
                if (groupAdmin == null) {
                    throw new RuntimeException("GroupAdmin не может быть null");
                }
                groupAdmin.setCreatedBy(currentUser);
                Optional<Person> existingPerson = personService.findExistingPerson(groupAdmin);
                if (existingPerson.isPresent()) {
                    group.setGroupAdmin(existingPerson.get());
                } else {
                    personService.savePerson(groupAdmin);
                }
                group.setCreatedBy(currentUser);
                studyGroupService.save(group);
                savedCount++;
            }
//            int asd = 12;
//            int buff = 0;
//            int buff1;
//            if(true) buff1 = asd/buff;

            transactionManager.commit(status);

        } catch (Exception e) {
            try {
                transactionManager.rollback(status);
                if (fileUploaded) {
                    minioService.deleteFile(fileKey);
                }
                if (originalFileStream != null) {
                    minioService.uploadFile(fileKey, originalFileStream, file.getContentType());
                }
            } catch (Exception rollbackException) {
                System.err.println("Ошибка при откате транзакции: " + rollbackException.getMessage());
            }
            System.err.println("Ошибка при импорте: " + e.getMessage());
            throw e;
        } finally {
            saveImportHistory(file, savedCount);
        }
    }
    
    private List<StudyGroup> parseStudyGroups(MultipartFile file) throws IOException {
        List<StudyGroup> studyGroups;
        try {
            studyGroups = objectMapper.readValue(file.getInputStream(),
                    new TypeReference<List<StudyGroup>>() {});
        } catch (com.fasterxml.jackson.core.JsonParseException e) {
            throw new RuntimeException("Неверный формат JSON файла: " + e.getMessage(), e);
        } catch (com.fasterxml.jackson.databind.exc.InvalidFormatException e) {
            throw new RuntimeException("Неверный формат значения для поля " + e.getPath().get(0).getFieldName() +
                    ": ожидалось " + e.getTargetType().getSimpleName(), e);
        } catch (com.fasterxml.jackson.databind.exc.UnrecognizedPropertyException e) {
            throw new RuntimeException("Неизвестное поле в JSON объекте: " + e.getPropertyName(), e);
        } catch (com.fasterxml.jackson.databind.exc.MismatchedInputException e) {
            throw new RuntimeException("Ожидался массив объектов StudyGroup в JSON формате", e);
        }

        return studyGroups;
    }

    private void validateStudyGroup(StudyGroup group) {
        List<String> errors = new ArrayList<>();

        if (group.getName() == null) {
            errors.add("Название группы не может быть null");
        } else if (group.getName().trim().isEmpty()) {
            errors.add("Название группы не должно быть пустым");
        } else if (!group.getName().matches("^[a-zA-Zа-яА-Я0-9]+(?:\\s[a-zA-Zа-яА-Я0-9]+)*$")) {
            errors.add("Название группы должно содержать только буквы, цифры и пробелы (максимум один пробел между словами)");
        }

        if (group.getCoordinates() == null) {
            errors.add("Coordinates не может быть null");
        } else {
            if (group.getCoordinates().getX() == null) {
                errors.add("X координата обязательна");
            } else if (group.getCoordinates().getX() < -407 || group.getCoordinates().getX() > 500) {
                errors.add("X должна быть в диапазоне от -407 до 500");
            }
            if (group.getCoordinates().getY() == null) {
                errors.add("Y координата не может быть null");
            }
        }
        if (group.getStudentsCount() == null) {
            errors.add("Число студентов не может быть null");
        } else if (group.getStudentsCount() < 0) {
            errors.add("Число студентов должно быть больше 0");
        }
        
        if (group.getExpelledStudents() == null) {
            errors.add("Число исключённых студентов не может быть null");
        } else if (group.getExpelledStudents() < 0) {
            errors.add("Число исключённых студентов должно быть больше 0");
        }
        
        if (group.getTransferredStudents() == null) {
            errors.add("Число переведённых студентов не может быть null");
        } else if (group.getTransferredStudents() < 0) {
            errors.add("Число переведённых студентов должно быть больше 0");
        }
        
        if (group.getShouldBeExpelled() == null) {
            errors.add("Поле 'shouldBeExpelled' не может быть null");
        } else if (group.getShouldBeExpelled() < 0) {
            errors.add("Количество студентов, которые должны быть исключены, должно быть больше 0");
        }
        
        if (group.getAverageMark() < 1) {
            errors.add("Средняя оценка должна быть больше 0");
        }
        if (group.getFormOfEducation() == null) {
            errors.add("Форма обучения не может быть null");
        }

        if (group.getGroupAdmin() == null) {
            errors.add("Администратор группы не может быть null");
        } else {
            Person admin = group.getGroupAdmin();
            
            if (admin.getPassportID() == null || admin.getPassportID().trim().isEmpty()) {
                errors.add("Паспорт администратора не может быть пустым");
            } else if (!admin.getPassportID().matches("^[1234567890a-zA-Zа-яА-Я]+$")) {
                errors.add("Паспорт администратора должен состоять из букв и цифр");
            } else if (admin.getPassportID().length() > 42) {
                errors.add("Паспорт администратора должен содержать от 1 до 42 символов");
            }
            
            if (admin.getName() == null) {
                errors.add("Имя администратора не может быть null");
            } else if (admin.getName().trim().isEmpty()) {
                errors.add("Имя администратора не должно быть пустым");
            } else if (!admin.getName().matches("^[a-zA-Zа-яА-Я]+(?:\\s[a-zA-Zа-яА-Я]+)*$")) {
                errors.add("Имя администратора должно содержать только буквы и пробелы (максимум один пробел между словами)");
            }
            
            if (admin.getHairColor() == null) {
                errors.add("Цвет волос администратора не может быть null");
            }
            
            if (admin.getLocation() == null) {
                errors.add("Локация администратора не может быть null");
            } else {
                if (admin.getLocation().getX() == null) {
                    errors.add("X координата локации администратора не может быть null");
                }
                if (admin.getLocation().getY() == null) {
                    errors.add("Y координата локации администратора не может быть null");
                }
                if (admin.getLocation().getName() == null || admin.getLocation().getName().trim().isEmpty()) {
                    errors.add("Название локации администратора не может быть пустым");
                }
            }
            
            if (admin.getNationality() == null) {
                errors.add("Национальность администратора не может быть null");
            }
        }
        
        if (!errors.isEmpty()) {
            throw new RuntimeException(String.join(", ", errors));
        }
    }
}
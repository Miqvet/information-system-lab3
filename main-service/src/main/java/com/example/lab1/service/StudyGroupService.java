package com.example.lab1.service;
import com.example.lab1.domain.entity.Person;
import com.example.lab1.domain.entity.StudyGroup;
import com.example.lab1.repository.StudyGroupRepository;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
public class StudyGroupService {
    private final StudyGroupRepository studyGroupRepository;

    public StudyGroupService(StudyGroupRepository studyGroupRepository) {
        this.studyGroupRepository = studyGroupRepository;
    }
    @Cacheable(value = "studyGroups", key = "'all'")
    public List<StudyGroup> findAll() {
        return studyGroupRepository.findAll();
    }

    @Cacheable(value = "studyGroups", key = "#id")
    public StudyGroup getById(int id) throws NoSuchElementException {
        return studyGroupRepository.findById(id)
            .orElseThrow(() -> new NoSuchElementException("Group " + id + " not found"));
    }

    @Transactional(isolation = Isolation.REPEATABLE_READ)
    @CacheEvict(value = "studyGroups", allEntries = true)
    public void save(StudyGroup studyGroup) throws DataIntegrityViolationException {
        if(studyGroupRepository.existsByName(studyGroup.getName())) {
            studyGroup.setName(studyGroup.getName() + " " + generateUniqueId(LocalDateTime.now()));
            studyGroupRepository.saveAndFlush(studyGroup);
        }else{
            studyGroupRepository.saveAndFlush(studyGroup);
        }
    }

    @Transactional
    @CacheEvict(value = "studyGroups", allEntries = true)
    public void deleteById(int id) {
        studyGroupRepository.deleteById(id);
    }

    @Transactional(isolation = Isolation.REPEATABLE_READ)
    @CacheEvict(value = "studyGroups", allEntries = true)
    public void update(int id, StudyGroup updatedStudyGroup) throws NoSuchElementException {
        StudyGroup existingStudyGroup = studyGroupRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("StudyGroup with id " + id + " not found"));
        System.out.println(existingStudyGroup);
        List<StudyGroup> allNamedGroups = studyGroupRepository.getStudyGroupsByName(updatedStudyGroup.getName());
        if(studyGroupRepository.existsByName(updatedStudyGroup.getName()) &&
            allNamedGroups.size() >= 1 && allNamedGroups.get(0).getId() != id)
            existingStudyGroup.setName(updatedStudyGroup.getName() + " " + generateUniqueId(LocalDateTime.now()));
        else existingStudyGroup.setName(updatedStudyGroup.getName());
        if(!Objects.equals(existingStudyGroup.getCoordinates().getX(), updatedStudyGroup.getCoordinates().getX())) {
            existingStudyGroup.getCoordinates().setX(updatedStudyGroup.getCoordinates().getX());
        }
        if(!Objects.equals(existingStudyGroup.getCoordinates().getY(), updatedStudyGroup.getCoordinates().getY())){
            existingStudyGroup.getCoordinates().setY(updatedStudyGroup.getCoordinates().getY());
        }
        if(!Objects.equals(existingStudyGroup.getStudentsCount(), updatedStudyGroup.getStudentsCount())){
            existingStudyGroup.setStudentsCount(updatedStudyGroup.getStudentsCount());
        }
        if(!Objects.equals(existingStudyGroup.getExpelledStudents(), updatedStudyGroup.getExpelledStudents())){
            existingStudyGroup.setExpelledStudents(updatedStudyGroup.getExpelledStudents());
        }
        if(!Objects.equals(existingStudyGroup.getTransferredStudents(), updatedStudyGroup.getTransferredStudents())){
            existingStudyGroup.setTransferredStudents(updatedStudyGroup.getTransferredStudents());
        }
        if(existingStudyGroup.getFormOfEducation() != updatedStudyGroup.getFormOfEducation()){
            existingStudyGroup.setFormOfEducation(updatedStudyGroup.getFormOfEducation());
        }
        if(!Objects.equals(existingStudyGroup.getShouldBeExpelled(), updatedStudyGroup.getShouldBeExpelled())){
            existingStudyGroup.setShouldBeExpelled(updatedStudyGroup.getShouldBeExpelled());
        }
        if(existingStudyGroup.getAverageMark() != updatedStudyGroup.getAverageMark()){
            existingStudyGroup.setAverageMark(updatedStudyGroup.getAverageMark());
        }
        if(existingStudyGroup.getSemesterEnum() != updatedStudyGroup.getSemesterEnum()){
            existingStudyGroup.setSemesterEnum(updatedStudyGroup.getSemesterEnum());
        }
        if(existingStudyGroup.getGroupAdmin() != updatedStudyGroup.getGroupAdmin()){
            existingStudyGroup.setGroupAdmin(updatedStudyGroup.getGroupAdmin());
        }
        System.out.println(existingStudyGroup);
    }

    @Cacheable(value = "studyGroups", key = "'filtered:' + #filterField + ':' + #filterValue + ':' + #sortBy + ':' + #page + ':' + #pageSize")
    public Page<StudyGroup> findFilteredAndSorted(int page, int pageSize, String filterField, String filterValue, String sortBy) {
        List<StudyGroup> allGroups = findAll();

        // Фильтрация
        List<StudyGroup> filteredGroups = allGroups;
        if (!filterField.isEmpty() && !filterValue.isEmpty()) {
            filteredGroups = allGroups.stream()
                    .filter(group -> matchesFilter(group, filterField, filterValue))
                    .collect(Collectors.toList());
        }

        // Сортировка
        filteredGroups.sort((g1, g2) -> compareByField(g1, g2, sortBy));

        // Пагинация
        int start = (page - 1) * pageSize;
        int end = Math.min(start + pageSize, filteredGroups.size());

        if (start >= filteredGroups.size()) {
            return new PageImpl<>(Collections.emptyList(), PageRequest.of(page - 1, pageSize), filteredGroups.size());
        }

        List<StudyGroup> pageContent = filteredGroups.subList(start, end);
        return new PageImpl<>(pageContent, PageRequest.of(page - 1, pageSize), filteredGroups.size());
    }


    private boolean matchesFilter(StudyGroup group, String field, String value) {
        try {
            return switch (field) {
                case "id" -> group.getId() == Integer.parseInt(value);
                case "name" -> group.getName().equals(value);
                case "studentsCount" -> group.getStudentsCount() == Integer.parseInt(value);
                case "expelledStudents" -> group.getExpelledStudents() == Integer.parseInt(value);
                case "transferredStudents" -> group.getTransferredStudents() == Integer.parseInt(value);
                case "shouldBeExpelled" -> group.getShouldBeExpelled() == Integer.parseInt(value);
                case "averageMark" -> group.getAverageMark() == Double.parseDouble(value);
                case "groupAdmin.location.name" -> group.getGroupAdmin().getLocation().getName().equals(value);
                case "groupAdmin.name" -> group.getGroupAdmin().getName().equals(value);
                default -> false;
            };
        } catch (Exception e) {
            return false;
        }
    }

    private int compareByField(StudyGroup g1, StudyGroup g2, String field) {
        try {
            return switch (field) {
                case "id" -> Integer.compare(g1.getId(), g2.getId());
                case "name" -> g1.getName().compareTo(g2.getName());
                case "studentsCount" -> Integer.compare(g1.getStudentsCount(), g2.getStudentsCount());
                case "expelledStudents" -> Integer.compare(g1.getExpelledStudents(), g2.getExpelledStudents());
                case "transferredStudents" -> Integer.compare(g1.getTransferredStudents(), g2.getTransferredStudents());
                case "shouldBeExpelled" -> Integer.compare(g1.getShouldBeExpelled(), g2.getShouldBeExpelled());
                case "averageMark" -> Double.compare(g1.getAverageMark(), g2.getAverageMark());
                case "groupAdmin.location.name" -> g1.getGroupAdmin().getLocation().getName()
                        .compareTo(g2.getGroupAdmin().getLocation().getName());
                case "groupAdmin.name" -> g1.getGroupAdmin().getName().compareTo(g2.getGroupAdmin().getName());
                default -> 0;
            };
        } catch (Exception e) {
            return 0;
        }
    }


    public long countByShouldBeExpelledLessThan(int threshold) {
        return studyGroupRepository.countByShouldBeExpelledLessThan(threshold);
    }

    public long countByShouldBeExpelledGreaterThan(int threshold) {
        return studyGroupRepository.countByShouldBeExpelledGreaterThan(threshold);
    }

    public List<Person> findUniqueGroupAdmins() {
        return studyGroupRepository.findAll()
                .stream()
                .map(StudyGroup::getGroupAdmin)
                .distinct()
                .collect(Collectors.toList());
    }

    @Transactional
    @CacheEvict(value = "studyGroups", allEntries = true)
    public void expelGroupStudents(int groupId) throws NoSuchElementException {
        StudyGroup group = studyGroupRepository.findById(groupId).orElseThrow();
        group.setExpelledStudents((group.getStudentsCount() + group.getExpelledStudents()));
        group.setStudentsCount(0);
        group.setShouldBeExpelled(0);
        studyGroupRepository.save(group);
    }

    @Transactional
    @CacheEvict(value = "studyGroups", allEntries = true)
    public void transferStudents(int fromGroupId, int toGroupId) {
        StudyGroup fromGroup = studyGroupRepository.findById(fromGroupId).orElseThrow();
        StudyGroup toGroup = studyGroupRepository.findById(toGroupId).orElseThrow();

        int studentsToTransfer =  fromGroup.getStudentsCount();

        fromGroup.setStudentsCount(0);
        fromGroup.setShouldBeExpelled(0);

        toGroup.setStudentsCount(toGroup.getStudentsCount() + studentsToTransfer);
        toGroup.setTransferredStudents(toGroup.getTransferredStudents() + studentsToTransfer);

        studyGroupRepository.save(fromGroup);
        studyGroupRepository.save(toGroup);
    }

    public long countThereGroupAdmin(long adminId) {
        List<StudyGroup> allGroup = findAll();
        long count = allGroup.stream()
                .filter(studyGroup -> studyGroup.getGroupAdmin().getId() == adminId)
                .count();
        System.out.println(count);
        return count;
    }
    public static String generateUniqueId(LocalDateTime dateTime) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");
        return dateTime.format(formatter) + dateTime.getNano();
    }
}
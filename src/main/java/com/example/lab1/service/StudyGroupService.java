package com.example.lab1.service;
import com.example.lab1.domain.entity.Person;
import com.example.lab1.domain.entity.StudyGroup;
import com.example.lab1.domain.entity.auth.User;
import com.example.lab1.repository.StudyGroupRepository;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class StudyGroupService {

    private final StudyGroupRepository studyGroupRepository;

    public StudyGroupService(StudyGroupRepository studyGroupRepository) {
        this.studyGroupRepository = studyGroupRepository;
    }

    public List<StudyGroup> findAll() {
        return studyGroupRepository.findAll();
    }

    public StudyGroup getById(int id) throws NoSuchElementException {
        return studyGroupRepository.findById(id).orElseThrow(() -> new NoSuchElementException("Group " + id + " not found"));
    }

    public void save(StudyGroup studyGroup) throws DataIntegrityViolationException {
        studyGroupRepository.save(studyGroup);
    }

    public void deleteById(int id) {
        studyGroupRepository.deleteById(id);
    }

    public void update(int id, StudyGroup updatedStudyGroup) throws NoSuchElementException{
        StudyGroup existingStudyGroup = studyGroupRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("StudyGroup with id " + id + " not found"));
        existingStudyGroup.setName(updatedStudyGroup.getName());
        existingStudyGroup.setCoordinates(updatedStudyGroup.getCoordinates());
        existingStudyGroup.setStudentsCount(updatedStudyGroup.getStudentsCount());
        existingStudyGroup.setExpelledStudents(updatedStudyGroup.getExpelledStudents());
        existingStudyGroup.setTransferredStudents(updatedStudyGroup.getTransferredStudents());
        existingStudyGroup.setFormOfEducation(updatedStudyGroup.getFormOfEducation());
        existingStudyGroup.setShouldBeExpelled(updatedStudyGroup.getShouldBeExpelled());
        existingStudyGroup.setAverageMark(updatedStudyGroup.getAverageMark());
        existingStudyGroup.setSemesterEnum(updatedStudyGroup.getSemesterEnum());
        existingStudyGroup.setGroupAdmin(updatedStudyGroup.getGroupAdmin());
        studyGroupRepository.save(existingStudyGroup); // Сохраняем обновленный объект
    }

    public Page<StudyGroup> findFilteredAndSorted(int page, int pageSize, String filterField, String filterValue, String sortBy) {
        List<StudyGroup> allGroups = studyGroupRepository.findAll();

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

    public void expelGroupStudents(int groupId) throws NoSuchElementException {
        StudyGroup group = studyGroupRepository.findById(groupId).orElseThrow();
        group.setExpelledStudents((group.getStudentsCount() + group.getExpelledStudents()));
        group.setStudentsCount(0);
        group.setShouldBeExpelled(0);
        studyGroupRepository.save(group);
    }

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
}
package com.example.lab1.service;
import com.example.lab1.entity.Person;
import com.example.lab1.entity.StudyGroup;
import com.example.lab1.repository.StudyGroupRepository;
import com.example.lab1.util.StudyGroupSpecifications;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import org.springframework.dao.DataIntegrityViolationException;

import java.util.List;
import java.util.NoSuchElementException;
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
    public Page<StudyGroup> findFilteredAndSorted(int page, int pageSize, String filterField, String filterValue, String sortBy){
        // Проверяем, вложенное ли это поле (например, "person.name" или "location.name")
        Sort sort;
        if (sortBy.contains(".")) {
            sort = Sort.by(Sort.Order.asc(sortBy));
        } else {
            sort = Sort.by(sortBy);
        }

        Pageable pageable = PageRequest.of(page - 1, pageSize, sort);

        Specification<StudyGroup> spec = Specification.where(null);

        if (!filterField.isEmpty() && !filterValue.isEmpty()) {
            spec = spec.and(StudyGroupSpecifications.filterByStringField(filterField, filterValue));
        }

        return studyGroupRepository.findAll(spec, pageable);
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
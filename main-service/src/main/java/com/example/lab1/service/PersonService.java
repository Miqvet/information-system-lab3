package com.example.lab1.service;

import com.example.lab1.common.AppConstants;
import com.example.lab1.config.LockProvider;
import com.example.lab1.domain.entity.Person;
import com.example.lab1.repository.PersonRepository;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

@Service
@Transactional(readOnly = true)
public class PersonService {

    private final PersonRepository personRepository;
    private final StudyGroupService studyGroupService;

    public PersonService(PersonRepository personRepository, StudyGroupService studyGroupService) {
        this.personRepository = personRepository;
        this.studyGroupService = studyGroupService;
    }

    public List<Person> findAll() {
        return personRepository.findAllWithLocation();
    }

    public Person getById(Long id) throws NoSuchElementException {
        return personRepository.findById(id).orElseThrow(() -> new NoSuchElementException("Person with id " + id + " not found"));
    }

    @Transactional
    public void savePerson(Person person) throws IllegalArgumentException {
//        LockProvider lockProvider = new LockProvider();
//        lockProvider.getReentLock().lock();
        var existingPerson = personRepository.findByPassportID(person.getPassportID());
        if (existingPerson.isPresent() &&
                Duration.between(existingPerson.get().getUpdatedTime(), LocalDateTime.now()).getSeconds()
                        <= AppConstants.durationInSecondsForCreate)
            person.setPassportID(person.getPassportID() + LocalDateTime.now().getNano());
        else if (existingPerson.isPresent())
            throw new IllegalArgumentException("Человек с таким passportID уже существует");
        person.setUpdatedTime(LocalDateTime.now());
        personRepository.save(person);
//        lockProvider.getReentLock().unlock();
    }

    @Transactional
    public void update(long id, Person person) {
        Person existingPerson = personRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Person with id " + id + " not found"));
        var existingPersonByPassport = personRepository.findByPassportID(person.getPassportID());
        if (existingPersonByPassport.isPresent() &&
                Duration.between(existingPersonByPassport.get().getUpdatedTime(), LocalDateTime.now()).getSeconds()
                        <= AppConstants.durationInSecondsForCreate)
            person.setPassportID(person.getPassportID() + LocalDateTime.now().getNano());
        else if (existingPersonByPassport.isPresent())
            throw new IllegalArgumentException("Человек с таким passportID уже существует");
        existingPerson.setName(person.getName());
        existingPerson.setEyeColor(person.getEyeColor());
        existingPerson.setHairColor(person.getHairColor());
        existingPerson.setLocation(person.getLocation());
        existingPerson.setPassportID(person.getPassportID());
        existingPerson.setNationality(person.getNationality());
        existingPerson.setUpdatedTime(LocalDateTime.now());
    }

    @Transactional
    public void deleteById(long id) {
        if (studyGroupService.countThereGroupAdmin(id) != 0) {
            throw new IllegalArgumentException("У данного админа есть группы");
        }
        personRepository.deleteById(id);
    }

    public Optional<Person> findExistingPerson(Person person) {
        Optional<Person> existingPerson = personRepository.findByPassportID(person.getPassportID());
        if (existingPerson.isPresent() && existingPerson.get().equals(person)) {
            return existingPerson;
        }
        return Optional.empty();
    }
}

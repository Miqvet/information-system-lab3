package com.example.lab1.service;

import com.example.lab1.entity.Person;
import com.example.lab1.repository.PersonRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class PersonService {

    private final PersonRepository personRepository;
    private final StudyGroupService studyGroupService;

    public PersonService(PersonRepository personRepository, StudyGroupService studyGroupService) {
        this.personRepository = personRepository;
        this.studyGroupService = studyGroupService;
    }

    public List<Person> findAll() {
        return personRepository.findAll();
    }

    public Person getById(Long id) {
        return personRepository.findById(id).orElse(null);
    }

    public void save(Person person) {
        personRepository.save(person);
    }

    public void savePerson(Person person) throws IllegalArgumentException {
        if (personRepository.existsByPassportID(person.getPassportID())) {
            throw new IllegalArgumentException("Человек с таким passportID уже существует");
        }
        personRepository.save(person);
    }

    // Метод для обновления объекта
    public void update(long id, Person person) {
        Person existingPerson = personRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Person with id " + id + " not found"));
        // Обновляем поля
        existingPerson.setName(person.getName());
        existingPerson.setEyeColor(person.getEyeColor());
        existingPerson.setHairColor(person.getHairColor());
        existingPerson.setLocation(person.getLocation());
        existingPerson.setPassportID(person.getPassportID());
        existingPerson.setNationality(person.getNationality());
        personRepository.save(existingPerson);
    }
    public void deleteById(long id) {
        if(studyGroupService.countThereGroupAdmin(id) != 0){
            throw new IllegalArgumentException("У данного админа есть группы");
        }
        personRepository.deleteById(id);
    }

}

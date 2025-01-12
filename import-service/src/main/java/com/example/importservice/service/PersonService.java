package com.example.importservice.service;

import com.example.importservice.domain.entity.Person;
import com.example.importservice.repository.PersonRepository;
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
    public PersonService(PersonRepository personRepository) {
        this.personRepository = personRepository;
    }

    public List<Person> findAll() {
        return personRepository.findAllWithLocation();
    }

    public Person getById(Long id) throws NoSuchElementException {
        return personRepository.findById(id).orElseThrow(() -> new NoSuchElementException("Person with id " + id + " not found"));
    }

    @Transactional
    public void savePerson(Person person) throws IllegalArgumentException {
        var existingPerson = personRepository.findByPassportID(person.getPassportID());
        if (existingPerson.isPresent() &&
                Duration.between(existingPerson.get().getUpdatedTime(), LocalDateTime.now()).getSeconds()
                        <= 20)
            person.setPassportID(person.getPassportID() + LocalDateTime.now().getNano());
        else if (existingPerson.isPresent())
            throw new IllegalArgumentException("Человек с таким passportID уже существует");
        person.setUpdatedTime(LocalDateTime.now());
        personRepository.save(person);
    }
}

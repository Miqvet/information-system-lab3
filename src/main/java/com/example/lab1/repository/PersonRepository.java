package com.example.lab1.repository;

import com.example.lab1.domain.entity.Person;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PersonRepository extends JpaRepository<Person, Long> {
    boolean existsByPassportID(String passportID);
    Person getPersonByPassportID(String passportID);
    Optional<Person> findByPassportID(String passportID);
}

package com.example.lab1.repository;

import com.example.lab1.entity.Person;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PersonRepository extends JpaRepository<Person, Long> {
    boolean existsByPassportID(String passportID);
}

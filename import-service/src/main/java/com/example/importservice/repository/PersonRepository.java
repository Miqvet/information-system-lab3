package com.example.importservice.repository;

import com.example.importservice.domain.entity.Person;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface PersonRepository extends JpaRepository<Person, Long> {
    boolean existsByPassportID(String passportID);
    Person getPersonByPassportID(String passportID);
    Optional<Person> findByPassportID(String passportID);

    @Query("SELECT p FROM Person p LEFT JOIN FETCH p.location")
    List<Person> findAllWithLocation();
}

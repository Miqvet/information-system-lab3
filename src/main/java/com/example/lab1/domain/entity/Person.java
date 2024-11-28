package com.example.lab1.domain.entity;

import com.example.lab1.domain.entity.enums.Color;
import com.example.lab1.domain.entity.enums.Country;
import com.example.lab1.domain.entity.auth.User;
import jakarta.persistence.*;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;
import java.io.Serializable;
import java.util.Objects;
@Entity
@Data
@Table(name = "person")
public class Person implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotEmpty(message = "Паспорт не может быть пустым")
    @Column(unique = true, nullable = false)
    @Size(min = 1, max = 42, message = "Паспорт должен содержать от 1 до 42 символов")
    @Pattern(regexp = "^[1234567890a-zA-Zа-яА-Я]+$", message = "Паспорт должен состоять из букв и цифр")
    private String passportID;

    @NotNull(message = "Имя не может быть null")
    @NotEmpty(message = "Имя не должно быть пустым")
    @Pattern(regexp = "^[a-zA-Zа-яА-Я]+(?:\\s[a-zA-Zа-яА-Я]+)*$", message = "Имя должно содержать только буквы и пробелы(максимум один пробел между словами)")
    private String name;

    @Enumerated(EnumType.STRING)
    private Color eyeColor; // Может быть null

    @NotNull(message = "Цвет волос не может быть null")
    @Enumerated(EnumType.STRING)
    private Color hairColor;

    @Valid
    @NotNull(message = "Локация не может быть null")
    @OneToOne(cascade = CascadeType.ALL)
    private Location location;

    @NotNull(message = "Национальность не может быть null")
    @Enumerated(EnumType.STRING)
    private Country nationality;

    @Valid
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "created_by", nullable = false)
    private User createdBy; 

    @NotNull
    private boolean canBeChanged = true; 

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Person person = (Person) o;
        return eyeColor == person.eyeColor &&
               hairColor == person.hairColor &&
               nationality == person.nationality &&
               Objects.equals(passportID, person.passportID) &&
               Objects.equals(name, person.name) &&
               Objects.equals(location.getX(), person.location.getX()) &&
               Objects.equals(location.getY(), person.location.getY()) &&
               Objects.equals(location.getName(), person.location.getName());
    }

    @Override
    public int hashCode() {
        return Objects.hash(passportID, name, eyeColor, hairColor, nationality, location.getX(), location.getY(), location.getName());
    }
}

package com.example.importservice.domain.entity;

import com.example.importservice.domain.entity.auth.User;
import com.example.importservice.domain.entity.enums.FormOfEducation;
import com.example.importservice.domain.entity.enums.Semester;
import jakarta.persistence.*;
import jakarta.validation.*;
import jakarta.validation.constraints.*;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

@Entity
@Data
@Table(name = "studyGroup")
public class StudyGroup implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @NotNull(message = "Название группы не может быть null")
    @NotEmpty(message = "Название группы не должно быть пустым")
    @Pattern(regexp = "^[a-zA-Zа-яА-Я0-9]+(?:\\s[a-zA-Zа-яА-Я0-9]+)*$", message = "Имя должно содержать только буквы и пробелы(максимум один пробел между словами)")
    private String name;

    @Valid
    @NotNull(message = "Координаты не могут быть null")
    @OneToOne(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private Coordinates coordinates;

    @Temporal(TemporalType.TIMESTAMP)
    private Date creationDate;

    @Min(value = 0, message = "Число студентов должно быть больше 0")
    @NotNull(message = "Число студентов не может быть null")
    private Integer studentsCount;

    @Min(value = 0, message = "Число исключённых студентов должно быть больше 0")
    @NotNull(message = "Число исключённых студентов не может быть null")
    private Integer expelledStudents;

    @Min(value = 0, message = "Число переведённых студентов должно быть больше 0")
    @NotNull(message = "Число переведённых студентов не может быть null")
    private Integer transferredStudents;

    @NotNull(message = "Форма обучения не может быть null")
    @Enumerated(EnumType.STRING)
    private FormOfEducation formOfEducation;

    @Min(value = 0, message = "Количество студентов, которые должны быть исключены, должно быть больше 0")
    @NotNull(message = "Поле 'shouldBeExpelled' не может быть null")
    private Integer shouldBeExpelled;

    @Min(value = 1, message = "Средняя оценка должна быть больше 0")
    @NotNull(message = "Средняя оценка не может быть null")
    private double averageMark;

    @Enumerated(EnumType.STRING)
    private Semester semesterEnum;

    @Valid
    @NotNull(message = "Администратор должен существовать")
    @ManyToOne()
    @JoinColumn(name = "groupAdmin_id")
    private Person groupAdmin;

    @PrePersist
    protected void onCreate() {
        creationDate = new Date();
    }

    @Valid
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "created_by", nullable = false)
    private User createdBy;

    @NotNull
    private boolean canBeChanged = true; 
}
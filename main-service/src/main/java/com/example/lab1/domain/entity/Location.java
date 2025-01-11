package com.example.lab1.domain.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.io.Serializable;
@Entity
@Data
@Table(name = "location")
public class Location implements Serializable {
    @Id
    @GeneratedValue()
    private Long id;

    @NotNull
    private Integer x;
    @NotNull
    private Double y;

    @NotEmpty
    private String name;
}

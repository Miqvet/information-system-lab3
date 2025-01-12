package com.example.importservice.domain.entity;


import jakarta.persistence.*;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.io.Serializable;


@Entity
@Data
@Table(name = "coordinates")
public class Coordinates implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull(message = "X координата обязательна")
    @Min(value = -407, message = "X должна быть больше -407")
    @Max(value = 500, message = "X должна быть меньше 500")
    private Long x; 

    @NotNull
    private Integer y; 
}

package com.example.lab1.domain.entity;

import java.io.Serializable;
import java.time.LocalDateTime;

import com.example.lab1.domain.entity.auth.User;
import jakarta.persistence.*;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Setter;


@Entity
@Table(name = "import_history")
@Setter
public class ImportHistory implements Serializable {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    private boolean status;

    @NotEmpty
    private String fileName;

    @NotNull
    private LocalDateTime importDate;

    @Valid
    @ManyToOne
    @JoinColumn(name = "user_id")
    private User addedBy;
}

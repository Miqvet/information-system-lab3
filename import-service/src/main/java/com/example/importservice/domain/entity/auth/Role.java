package com.example.importservice.domain.entity.auth;

import com.example.importservice.domain.entity.enums.RoleName;
import com.example.importservice.domain.entity.enums.RoleName;
import lombok.Data;
import jakarta.persistence.*;
import java.io.Serializable;

@Entity
@Data
@Table(name = "role")
public class Role implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    private RoleName name;
}



package com.example.attendancesystem.model;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Data
@Table(name="users")
public class Users {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name="user_id")
    private Integer userId;

    @Column(nullable = false,unique = true, length=50)
    private String username;

    @Column(name="password_hash", nullable = false)
    private String passwordHash;

    @Column(length=20, nullable = false)
    @Enumerated(EnumType.STRING)
    private Role role;

    public enum Role{
        PRINCIPAL, TEACHER
    }
}

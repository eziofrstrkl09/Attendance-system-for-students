package com.example.attendancesystem.dto;

import com.example.attendancesystem.model.Classes;
import com.example.attendancesystem.model.Users;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserResponse {
    private Integer userId;
    private String username;
    private String name;
    private Users.Role role;
    private Classes classEntity;
}



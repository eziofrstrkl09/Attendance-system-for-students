package com.example.attendancesystem.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class CreateTeacherRequest {
    @NotBlank private String name;
    @NotNull private Integer classId;
    @NotBlank private String username;
    @NotBlank private String password;
}


package com.example.attendancesystem.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class UpdateTeacherRequest {
    @NotBlank
    private String name;

    @NotNull
    private Integer classId;
}


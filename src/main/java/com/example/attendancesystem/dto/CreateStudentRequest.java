package com.example.attendancesystem.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class CreateStudentRequest {
    @NotBlank(message = "UID is required and must be unique")
    private String uid;

    @NotBlank(message = "Name is required")
    private String name;

    @NotNull(message = "Roll number is required")
    @Min(value = 1, message = "Roll number must be at least 1")
    private Integer rollNo;

    @NotBlank(message = "Class is required")
    private String className;

    @NotBlank(message = "Section is required")
    private String section;

    @Email(message = "Please provide a valid email address")
    private String email;
    private String phone;

}
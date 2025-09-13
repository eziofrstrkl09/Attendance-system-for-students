package com.example.attendancesystem.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.Data;

import java.util.List;

@Data
@Entity
@Table(name="classes",uniqueConstraints = @UniqueConstraint(columnNames = {"class_name","section"}))
public class Classes {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name="class_id")
    private Integer classId;

    @Column(name="class_name",nullable = false,length = 20)
    private String className;

    @Column(nullable = false, length=5)
    private String section;

    @OneToOne
    @JoinColumn(name="teacher_id")
    private Teachers teacher;

    @OneToMany(mappedBy = "classEntity",cascade = CascadeType.ALL)
    @JsonIgnore // Prevent serialization of students to avoid deep nesting
    private List<Students> students;

}

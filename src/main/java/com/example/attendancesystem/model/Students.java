    package com.example.attendancesystem.model;

    import jakarta.persistence.*;
    import lombok.Data;

    @Data
    @Entity
    @Table(name="students",uniqueConstraints = @UniqueConstraint(columnNames = {"roll_no","class_id"}))
    public class Students {

        @Id
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        @Column(name = "student_id")
        private Integer studentId;

        @Column(nullable=false, length=100)
        private String name;

        @Column(name = "roll_no", nullable=false, length=20)
        private Integer rollNo;

        @Column(nullable=false,unique = true, length=50)
        private String uid;

        @ManyToOne
        @JoinColumn(name = "class_id",nullable = false)
        private Classes classEntity;

    //    @Column(nullable=false)
    //    private String className;
    //    @Column(nullable = false, columnDefinition = "TEXT DEFAULT 'A'")
    //    private String section;
    }

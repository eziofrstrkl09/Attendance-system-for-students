package com.example.attendancesystem.dto;

import com.example.attendancesystem.model.Attendance;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalTime;

@Data
public class AttendanceResponse {
    private Integer attendanceId;
    private Integer studentId;
    private LocalDate date;
    private boolean status;
    private LocalTime timeEntry;
    private LocalTime timeExit;

    public AttendanceResponse(Attendance attendance) {
        this.attendanceId = attendance.getAttendanceId();
        this.studentId = attendance.getStudent().getStudentId();
        this.date = attendance.getDate();
        this.status = attendance.isStatus();
        this.timeEntry = attendance.getTimeEntry();
        this.timeExit = attendance.getTimeExit();
    }
}

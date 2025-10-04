package com.example.attendancesystem.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * This controller provides a simple, public endpoint to check if the
 * backend application is running.
 */
@RestController
@RequestMapping("/api/public") // Base path for this controller
public class HealthCheckController {

    /**
     * Responds to GET requests at /api/public/health.
     * @return A 200 OK response with a confirmation message.
     */
    @GetMapping("/health")
    public ResponseEntity<String> healthCheck() {
        return ResponseEntity.ok("Backend Service is Up and Running!");
    }
}
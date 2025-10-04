package com.example.attendancesystem.controller;

import com.example.attendancesystem.dto.RfidScanRequest;
import com.example.attendancesystem.service.RfidWebSocketHandler;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.Map;

@RestController
@RequestMapping("/api/public/rfid")
public class RfidController {

    private final RfidWebSocketHandler webSocketHandler;

    public RfidController(RfidWebSocketHandler webSocketHandler) {
        this.webSocketHandler = webSocketHandler;
    }

    @PostMapping("/scan")
    public ResponseEntity<Void> receiveRfidScan(@RequestBody RfidScanRequest payload) {
        String uid = payload.getUid();
        if (uid != null && !uid.isEmpty()) {
            try {
                System.out.println("Received UID via HTTP: " + uid + ", broadcasting via WebSocket.");
                webSocketHandler.broadcast(uid);
                return ResponseEntity.ok().build();
            } catch (IOException e) {
                System.err.println("Error broadcasting UID via WebSocket: " + e.getMessage());
                return ResponseEntity.internalServerError().build();
            }
        }
        return ResponseEntity.badRequest().build();
    }
}
